package com.elias.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import com.elias.common.mq.event.TaskRejectedEvent;
import com.elias.common.mq.event.TaskSettlementRequestedEvent;
import com.elias.common.mq.event.TaskSubmittedEvent;
import com.elias.task.cache.CacheClient;
import com.elias.task.client.AuthClient;
import com.elias.task.dto.CreateTaskRequest;
import com.elias.task.dto.RejectTaskRequest;
import com.elias.task.dto.SubmitTaskRequest;
import com.elias.task.dto.TaskQueryRequest;
import com.elias.task.dto.UserInfoDTO;
import com.elias.task.entity.InternshipTask;
import com.elias.task.entity.TaskSettlement;
import com.elias.task.entity.TaskSubmission;
import com.elias.task.mapper.InternshipTaskMapper;
import com.elias.task.mapper.TaskSettlementMapper;
import com.elias.task.mapper.TaskSubmissionMapper;
import com.elias.task.mq.producer.TaskEventProducer;
import com.elias.task.service.TaskAppService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskAppServiceImpl implements TaskAppService {

    private static final String HOT_KEY = "task:hot:zset";
    private static final String TASK_DETAIL_CACHE_KEY_PREFIX = "task:detail:";
    private static final String TASK_DETAIL_LOCK_KEY_PREFIX = "lock:task:detail:";
    private static final long TASK_DETAIL_CACHE_TTL_MINUTES = 10;

    private final InternshipTaskMapper taskMapper;
    private final StringRedisTemplate redisTemplate;
    private final AuthClient authClient;
    private final CacheClient cacheClient;
    private final TaskSubmissionMapper taskSubmissionMapper;
    private final TaskSettlementMapper taskSettlementMapper;
    private final TaskEventProducer taskEventProducer;

    @Resource(name = "taskQueryExecutor")
    private Executor taskQueryExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CreateTaskRequest request, Long ownerId) {
        validateCreateTaskRequest(request);
        CreateTaskContext context = prepareCreateTaskContext(request, ownerId);
        executeTaskCreation(context);
        return buildCreatedTaskId(context);
    }

    @Override
    public IPage<InternshipTask> search(TaskQueryRequest request) {
        TaskQueryRequest query = normalizeTaskQueryRequest(request);
        Page<InternshipTask> page = buildTaskPage(query);
        LambdaQueryWrapper<InternshipTask> wrapper = buildSearchTaskWrapper(query);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipTask detail(Long id) {
        InternshipTask task = loadTaskById(id);
        refreshTaskDetailStats(task);
        refreshHotTaskCache(task);
        return task;
    }

    @Override
    public List<InternshipTask> hot() {
        List<Long> hotTaskIds = loadHotTaskIds();
        return loadHotTasks(hotTaskIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptTask(Long id) {
        InternshipTask task = loadTaskById(id);
        Long currentUserId = requireCurrentUserId();
        validateTaskAcceptance(task);
        markTaskAccepted(task, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long id, Long uid, String reason) {
        InternshipTask task = loadTaskById(id);
        validateTaskCancellation(task, uid);
        markTaskCancelled(task, reason);
    }

    @Override
    public IPage<InternshipTask> publishedTasks(Long uid, TaskQueryRequest request) {
        TaskQueryRequest query = normalizeTaskQueryRequest(request);
        Page<InternshipTask> page = buildTaskPage(query);
        LambdaQueryWrapper<InternshipTask> wrapper = buildPublishedTaskWrapper(uid, query);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<InternshipTask> acceptedTasks(Long uid, TaskQueryRequest request) {
        TaskQueryRequest query = normalizeTaskQueryRequest(request);
        Page<InternshipTask> page = buildTaskPage(query);
        LambdaQueryWrapper<InternshipTask> wrapper = buildAcceptedTaskWrapper(uid, query);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<InternshipTask> reviewTasks(Long uid, TaskQueryRequest request) {
        TaskQueryRequest query = normalizeTaskQueryRequest(request);
        Page<InternshipTask> page = buildTaskPage(query);
        LambdaQueryWrapper<InternshipTask> wrapper = buildReviewTaskWrapper(uid);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTask(Long id, Long uid, SubmitTaskRequest submitTaskRequest) {
        validateSubmitTaskRequest(submitTaskRequest);
        SubmitTaskContext context = prepareSubmitTaskContext(id, uid, submitTaskRequest);
        executeTaskSubmission(context);
        handleAfterTaskSubmitted(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(Long id, Long uid) {
        ApproveTaskContext context = prepareApproveTaskContext(id, uid);
        validateTaskApproval(context);
        executeTaskApproval(context);
        handleAfterTaskApproved(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTask(Long id, Long uid, RejectTaskRequest request) {
        RejectTaskContext context = prepareRejectTaskContext(id, uid, request);
        validateTaskRejection(context);
        executeTaskRejection(context);
        handleAfterTaskRejected(context);
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void rebuildHot() {
        List<InternshipTask> hotCandidates = loadHotCandidateTasks();
        refreshHotRanking(hotCandidates);
    }

    private void validateCreateTaskRequest(CreateTaskRequest request) {
        if (request == null) {
            throw new BizException(4001, "create task request can not be null");
        }
        if (request.getDeadline() != null && request.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BizException(4107, "deadline must be in the future");
        }
    }

    private CreateTaskContext prepareCreateTaskContext(CreateTaskRequest request, Long ownerId) {
        ApiResponse<UserInfoDTO> userInfoResp = authClient.userInfo(ownerId);
        if (userInfoResp == null || userInfoResp.getData() == null) {
            throw new BizException(ErrorCode.OWNER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        CreateTaskContext context = new CreateTaskContext();
        context.setRequest(request);
        context.setOwnerId(ownerId);
        context.setOwnerInfo(userInfoResp.getData());
        context.setTask(buildCreatedTask(request, ownerId, now));
        return context;
    }

    private InternshipTask buildCreatedTask(CreateTaskRequest request, Long ownerId, LocalDateTime now) {
        InternshipTask task = new InternshipTask();
        task.setPublisherId(ownerId);
        task.setAcceptorId(null);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory().toUpperCase() : "GENERAL");
        task.setTags(request.getTags());
        task.setLocation(request.getLocation());
        task.setContactInfo(request.getContactInfo());
        task.setRewardAmount(normalizeAmount(request.getRewardAmount()));
        task.setServiceFee(normalizeAmount(request.getServiceFee()));
        task.setSettleAmount(normalizeAmount(task.getRewardAmount().subtract(task.getServiceFee())));
        task.setTradeOrderNo(generateTradeOrderNo());
        task.setPriority(request.getPriority() == null ? 3 : request.getPriority());
        task.setStatus("OPEN");
        task.setDeadline(request.getDeadline());
        task.setProofRequired(Boolean.TRUE.equals(request.getProofRequired()));
        task.setViewCount(0);
        task.setCommentCount(0);
        task.setQualityScore(0);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return task;
    }

    private void executeTaskCreation(CreateTaskContext context) {
        taskMapper.insert(context.getTask());
    }

    private Long buildCreatedTaskId(CreateTaskContext context) {
        return context.getTask().getId();
    }

    private TaskQueryRequest normalizeTaskQueryRequest(TaskQueryRequest request) {
        return request == null ? new TaskQueryRequest() : request;
    }

    private Page<InternshipTask> buildTaskPage(TaskQueryRequest query) {
        return new Page<>(Math.max(1, query.getPage()), Math.max(1, Math.min(50, query.getSize())));
    }

    private LambdaQueryWrapper<InternshipTask> buildSearchTaskWrapper(TaskQueryRequest query) {
        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InternshipTask::getRewardAmount)
                .orderByAsc(InternshipTask::getPriority)
                .orderByDesc(InternshipTask::getCreatedAt);

        applyKeywordFilters(wrapper, query);
        applySearchStatusFilter(wrapper, query);
        applyCategoryAndPriorityFilters(wrapper, query);
        return wrapper;
    }

    private void applyKeywordFilters(LambdaQueryWrapper<InternshipTask> wrapper, TaskQueryRequest query) {
        if (!StringUtils.hasText(query.getKeyword())) {
            return;
        }

        wrapper.and(w -> w.like(InternshipTask::getTitle, query.getKeyword())
                .or()
                .like(InternshipTask::getDescription, query.getKeyword())
                .or()
                .like(InternshipTask::getTags, query.getKeyword())
                .or()
                .like(InternshipTask::getLocation, query.getKeyword()));
    }

    private void applySearchStatusFilter(LambdaQueryWrapper<InternshipTask> wrapper, TaskQueryRequest query) {
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(InternshipTask::getStatus, query.getStatus().toUpperCase());
            return;
        }

        wrapper.eq(InternshipTask::getStatus, "OPEN");
    }

    private void applyTaskQueryFilters(LambdaQueryWrapper<InternshipTask> wrapper, TaskQueryRequest query) {
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(InternshipTask::getStatus, query.getStatus().toUpperCase());
        }
        applyKeywordFilters(wrapper, query);
        applyCategoryAndPriorityFilters(wrapper, query);
    }

    private void applyCategoryAndPriorityFilters(LambdaQueryWrapper<InternshipTask> wrapper, TaskQueryRequest query) {
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(InternshipTask::getCategory, query.getCategory().toUpperCase());
        }
        if (query.getMinPriority() != null) {
            wrapper.ge(InternshipTask::getPriority, query.getMinPriority());
        }
        if (query.getMaxPriority() != null) {
            wrapper.le(InternshipTask::getPriority, query.getMaxPriority());
        }
    }

    private InternshipTask loadTaskById(Long id) {
        InternshipTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        return task;
    }

    private void refreshTaskDetailStats(InternshipTask task) {
        task.setViewCount((task.getViewCount() == null ? 0 : task.getViewCount()) + 1);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private void refreshHotTaskCache(InternshipTask task) {
        cacheClient.delete(TASK_DETAIL_CACHE_KEY_PREFIX + task.getId());
        redisTemplate.opsForZSet().add(HOT_KEY, String.valueOf(task.getId()), calculateHotScore(task));
    }

    private List<Long> loadHotTaskIds() {
        Set<String> ids = queryHotTaskIds();
        if (ids == null || ids.isEmpty()) {
            rebuildHot();
            ids = queryHotTaskIds();
        }
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        return ids.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    private Set<String> queryHotTaskIds() {
        return redisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, 9);
    }

    private List<InternshipTask> loadHotTasks(List<Long> taskIds) {
        if (taskIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<CompletableFuture<InternshipTask>> futures = taskIds.stream()
                .map(taskId -> CompletableFuture.supplyAsync(
                        () -> queryTaskWithCacheProtection(taskId),
                        taskQueryExecutor
                ))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    private Long requireCurrentUserId() {
        Long uid = UserContext.userId();
        if (uid == null) {
            throw new BizException(401, "user not logged in");
        }
        return uid;
    }

    private void validateTaskAcceptance(InternshipTask task) {
        if ("CANCELLED".equals(task.getStatus())) {
            throw new BizException(ErrorCode.TASK_CANCELLED);
        }
        if (!"OPEN".equals(task.getStatus())) {
            throw new BizException(4100, "only open task can be accepted");
        }
    }

    private void markTaskAccepted(InternshipTask task, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        task.setAcceptorId(userId);
        task.setAcceptedAt(now);
        task.setStatus("TAKEN");
        task.setUpdatedAt(now);
        taskMapper.updateById(task);
    }

    private void validateTaskCancellation(InternshipTask task, Long userId) {
        if (userId == null || !userId.equals(task.getPublisherId())) {
            throw new BizException(4033, "only publisher can cancel task");
        }
        if (!"OPEN".equals(task.getStatus())) {
            throw new BizException(4108, "only open task can be cancelled");
        }
    }

    private void markTaskCancelled(InternshipTask task, String reason) {
        LocalDateTime now = LocalDateTime.now();
        task.setCancelledAt(now);
        task.setCancelReason(reason);
        task.setStatus("CANCELLED");
        task.setUpdatedAt(now);
        taskMapper.updateById(task);
    }

    private LambdaQueryWrapper<InternshipTask> buildPublishedTaskWrapper(Long userId, TaskQueryRequest query) {
        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipTask::getPublisherId, userId)
                .orderByDesc(InternshipTask::getCreatedAt);
        applyTaskQueryFilters(wrapper, query);
        return wrapper;
    }

    private LambdaQueryWrapper<InternshipTask> buildAcceptedTaskWrapper(Long userId, TaskQueryRequest query) {
        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipTask::getAcceptorId, userId)
                .orderByDesc(InternshipTask::getCreatedAt);
        applyTaskQueryFilters(wrapper, query);
        return wrapper;
    }

    private LambdaQueryWrapper<InternshipTask> buildReviewTaskWrapper(Long userId) {
        return new LambdaQueryWrapper<InternshipTask>()
                .eq(InternshipTask::getPublisherId, userId)
                .eq(InternshipTask::getStatus, "SUBMITTED")
                .orderByDesc(InternshipTask::getSubmittedAt);
    }

    private void validateSubmitTaskRequest(SubmitTaskRequest request) {
        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw new BizException(4002, "submit content can not be blank");
        }
    }

    private SubmitTaskContext prepareSubmitTaskContext(Long taskId, Long userId, SubmitTaskRequest request) {
        InternshipTask task = loadTaskById(taskId);
        validateTaskSubmission(task, userId);

        LocalDateTime now = LocalDateTime.now();
        SubmitTaskContext context = new SubmitTaskContext();
        context.setTask(task);
        context.setUserId(userId);
        context.setRequest(request);
        context.setRoundNo(loadNextSubmissionRound(taskId));
        context.setSubmittedAt(now);
        context.setSubmission(buildTaskSubmission(taskId, userId, request, context.getRoundNo(), now));
        return context;
    }

    private void validateTaskSubmission(InternshipTask task, Long userId) {
        if (userId == null || !userId.equals(task.getAcceptorId())) {
            throw new BizException(4031, "only acceptor can submit task");
        }
        if (!"TAKEN".equals(task.getStatus())) {
            throw new BizException(4101, "only taken task can be submitted");
        }
    }

    private Integer loadNextSubmissionRound(Long taskId) {
        Long count = taskSubmissionMapper.selectCount(new LambdaQueryWrapper<TaskSubmission>()
                .eq(TaskSubmission::getTaskId, taskId));
        return (count == null ? 0 : count.intValue()) + 1;
    }

    private TaskSubmission buildTaskSubmission(Long taskId,
                                               Long userId,
                                               SubmitTaskRequest request,
                                               Integer roundNo,
                                               LocalDateTime submittedAt) {
        TaskSubmission submission = new TaskSubmission();
        submission.setTaskId(taskId);
        submission.setSubmitUserId(userId);
        submission.setRoundNo(roundNo);
        submission.setContent(request.getContent());
        submission.setProofUrls(request.getProofUrls());
        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(submittedAt);
        submission.setCreatedAt(submittedAt);
        submission.setUpdatedAt(submittedAt);
        return submission;
    }

    private void executeTaskSubmission(SubmitTaskContext context) {
        taskSubmissionMapper.insert(context.getSubmission());

        InternshipTask task = context.getTask();
        task.setStatus("SUBMITTED");
        task.setSubmittedAt(context.getSubmittedAt());
        task.setUpdatedAt(context.getSubmittedAt());
        taskMapper.updateById(task);
    }

    private void handleAfterTaskSubmitted(SubmitTaskContext context) {
        InternshipTask task = context.getTask();
        TaskSubmission submission = context.getSubmission();

        TaskSubmittedEvent taskSubmittedEvent = new TaskSubmittedEvent(
                UUID.randomUUID().toString().replace("-", ""),
                task.getId(),
                submission.getId(),
                task.getPublisherId(),
                context.getUserId(),
                context.getRoundNo(),
                context.getRequest().getContent(),
                context.getRequest().getProofUrls(),
                context.getSubmittedAt()
        );
        taskEventProducer.sendTaskSubmitted(taskSubmittedEvent);
    }

    private ApproveTaskContext prepareApproveTaskContext(Long taskId, Long userId) {
        ApproveTaskContext context = new ApproveTaskContext();
        context.setTask(loadTaskById(taskId));
        context.setUserId(userId);
        context.setLatestSubmission(loadLatestSubmission(taskId));
        context.setPendingSettlement(loadPendingTaskSettlement(taskId));
        context.setApprovedAt(LocalDateTime.now());
        return context;
    }

    private TaskSubmission loadLatestSubmission(Long taskId) {
        TaskSubmission latestSubmission = taskSubmissionMapper.selectOne(
                new LambdaQueryWrapper<TaskSubmission>()
                        .eq(TaskSubmission::getTaskId, taskId)
                        .orderByDesc(TaskSubmission::getRoundNo)
                        .last("limit 1")
        );
        if (latestSubmission == null) {
            throw new BizException(4045, "task submission not found");
        }
        return latestSubmission;
    }

    private TaskSettlement loadPendingTaskSettlement(Long taskId) {
        return taskSettlementMapper.selectOne(
                new LambdaQueryWrapper<TaskSettlement>()
                        .eq(TaskSettlement::getTaskId, taskId)
                        .eq(TaskSettlement::getStatus, "PENDING")
                        .last("limit 1")
        );
    }

    private void validateTaskApproval(ApproveTaskContext context) {
        InternshipTask task = context.getTask();
        if (context.getUserId() == null || !context.getUserId().equals(task.getPublisherId())) {
            throw new BizException(4032, "only publisher can approve task");
        }
        if (!"SUBMITTED".equals(task.getStatus())) {
            throw new BizException(4102, "only submitted task can be approved");
        }
        if (!"SUBMITTED".equals(context.getLatestSubmission().getStatus())) {
            throw new BizException(4103, "latest submission is not in submitted status");
        }
        if (context.getPendingSettlement() != null) {
            throw new BizException(4106, "task settlement already pending");
        }
    }

    private void executeTaskApproval(ApproveTaskContext context) {
        LocalDateTime approvedAt = context.getApprovedAt();
        TaskSubmission latestSubmission = context.getLatestSubmission();
        latestSubmission.setStatus("APPROVED");
        latestSubmission.setReviewedAt(approvedAt);
        latestSubmission.setUpdatedAt(approvedAt);
        taskSubmissionMapper.updateById(latestSubmission);

        TaskSettlement settlement = buildPendingTaskSettlement(context.getTask(), latestSubmission, approvedAt);
        taskSettlementMapper.insert(settlement);
        context.setSettlement(settlement);

        InternshipTask task = context.getTask();
        task.setStatus("SETTLEMENT_PENDING");
        task.setApprovedAt(approvedAt);
        task.setUpdatedAt(approvedAt);
        taskMapper.updateById(task);
    }

    private TaskSettlement buildPendingTaskSettlement(InternshipTask task,
                                                      TaskSubmission submission,
                                                      LocalDateTime approvedAt) {
        TaskSettlement settlement = new TaskSettlement();
        settlement.setTaskId(task.getId());
        settlement.setSubmissionId(submission.getId());
        settlement.setPublisherId(task.getPublisherId());
        settlement.setAcceptorId(task.getAcceptorId());
        settlement.setSettleAmount(task.getSettleAmount());
        settlement.setStatus("PENDING");
        settlement.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        settlement.setCreatedAt(approvedAt);
        settlement.setUpdatedAt(approvedAt);
        return settlement;
    }

    private void handleAfterTaskApproved(ApproveTaskContext context) {
        TaskSettlement settlement = context.getSettlement();
        InternshipTask task = context.getTask();
        TaskSubmission latestSubmission = context.getLatestSubmission();

        TaskSettlementRequestedEvent event = new TaskSettlementRequestedEvent(
                settlement.getMessageId(),
                settlement.getId(),
                task.getId(),
                latestSubmission.getId(),
                task.getPublisherId(),
                task.getAcceptorId(),
                task.getTradeOrderNo(),
                task.getSettleAmount(),
                context.getApprovedAt()
        );
        taskEventProducer.sendTaskSettlementRequested(event);
    }

    private RejectTaskContext prepareRejectTaskContext(Long taskId, Long userId, RejectTaskRequest request) {
        RejectTaskContext context = new RejectTaskContext();
        context.setTask(loadTaskById(taskId));
        context.setUserId(userId);
        context.setRequest(request);
        context.setLatestSubmission(loadLatestSubmission(taskId));
        context.setReason(request == null ? null : request.getReason());
        context.setReviewedAt(LocalDateTime.now());
        return context;
    }

    private void validateTaskRejection(RejectTaskContext context) {
        InternshipTask task = context.getTask();
        if (context.getUserId() == null || !context.getUserId().equals(task.getPublisherId())) {
            throw new BizException(4032, "only publisher can reject task");
        }
        if (!"SUBMITTED".equals(task.getStatus())) {
            throw new BizException(4103, "only submitted task can be rejected");
        }
        if (!"SUBMITTED".equals(context.getLatestSubmission().getStatus())) {
            throw new BizException(4104, "latest submission is not in submitted status");
        }
        if (!StringUtils.hasText(context.getReason())) {
            throw new BizException(4105, "reject reason can not be blank");
        }
    }

    private void executeTaskRejection(RejectTaskContext context) {
        LocalDateTime reviewedAt = context.getReviewedAt();
        TaskSubmission latestSubmission = context.getLatestSubmission();
        latestSubmission.setStatus("REJECTED");
        latestSubmission.setRejectReason(context.getReason());
        latestSubmission.setReviewedAt(reviewedAt);
        latestSubmission.setUpdatedAt(reviewedAt);
        taskSubmissionMapper.updateById(latestSubmission);

        InternshipTask task = context.getTask();
        task.setStatus("TAKEN");
        task.setRejectReason(context.getReason());
        task.setUpdatedAt(reviewedAt);
        taskMapper.updateById(task);
    }

    private void handleAfterTaskRejected(RejectTaskContext context) {
        InternshipTask task = context.getTask();
        TaskSubmission latestSubmission = context.getLatestSubmission();

        TaskRejectedEvent taskRejectedEvent = new TaskRejectedEvent(
                UUID.randomUUID().toString().replace("-", ""),
                task.getId(),
                latestSubmission.getId(),
                task.getPublisherId(),
                task.getAcceptorId(),
                latestSubmission.getRoundNo(),
                context.getReason(),
                context.getReviewedAt()
        );
        taskEventProducer.sendTaskRejected(taskRejectedEvent);
    }

    private List<InternshipTask> loadHotCandidateTasks() {
        return taskMapper.selectList(new LambdaQueryWrapper<InternshipTask>()
                .in(InternshipTask::getStatus, "OPEN", "TAKEN", "SUBMITTED")
                .orderByDesc(InternshipTask::getRewardAmount)
                .orderByDesc(InternshipTask::getCreatedAt)
                .last("limit 200"));
    }

    private void refreshHotRanking(List<InternshipTask> tasks) {
        redisTemplate.delete(HOT_KEY);
        for (InternshipTask task : tasks) {
            redisTemplate.opsForZSet().add(HOT_KEY, String.valueOf(task.getId()), calculateHotScore(task));
        }
    }

    private double calculateHotScore(InternshipTask task) {
        double reward = task.getRewardAmount() == null ? 0D : task.getRewardAmount().doubleValue();
        double views = task.getViewCount() == null ? 0D : task.getViewCount();
        double comments = task.getCommentCount() == null ? 0D : task.getCommentCount();
        return reward * 100 + views * 0.5 + comments * 2;
    }

    private InternshipTask queryTaskWithCacheProtection(Long taskId) {
        return cacheClient.queryWithMutex(
                TASK_DETAIL_CACHE_KEY_PREFIX,
                TASK_DETAIL_LOCK_KEY_PREFIX,
                taskId,
                InternshipTask.class,
                taskMapper::selectById,
                TASK_DETAIL_CACHE_TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return amount;
    }

    private String generateTradeOrderNo() {
        return "TRD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Data
    private static class CreateTaskContext {
        private CreateTaskRequest request;
        private Long ownerId;
        private UserInfoDTO ownerInfo;
        private InternshipTask task;
    }

    @Data
    private static class SubmitTaskContext {
        private InternshipTask task;
        private Long userId;
        private SubmitTaskRequest request;
        private Integer roundNo;
        private LocalDateTime submittedAt;
        private TaskSubmission submission;
    }

    @Data
    private static class ApproveTaskContext {
        private InternshipTask task;
        private Long userId;
        private LocalDateTime approvedAt;
        private TaskSubmission latestSubmission;
        private TaskSettlement pendingSettlement;
        private TaskSettlement settlement;
    }

    @Data
    private static class RejectTaskContext {
        private InternshipTask task;
        private Long userId;
        private RejectTaskRequest request;
        private String reason;
        private LocalDateTime reviewedAt;
        private TaskSubmission latestSubmission;
    }
}
