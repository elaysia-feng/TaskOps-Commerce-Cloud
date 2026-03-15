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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
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
    public Long create(CreateTaskRequest request, Long ownerId) {
        ApiResponse<UserInfoDTO> userInfoResp = authClient.userInfo(ownerId);
        if (userInfoResp == null || userInfoResp.getData() == null) {
            throw new BizException(4004, "publisher not found via auth-service");
        }

        if (request.getDeadline() != null && request.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BizException(4107, "deadline must be in the future");
        }

        LocalDateTime now = LocalDateTime.now();
        InternshipTask task = new InternshipTask();
        task.setPublisherId(ownerId);
        task.setAcceptorId(null);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory().toUpperCase() : "GENERAL");
        task.setTags(request.getTags());
        task.setLocation(request.getLocation());
        task.setContactInfo(request.getContactInfo());
        task.setRewardAmount(defaultAmount(request.getRewardAmount()));
        task.setServiceFee(defaultAmount(request.getServiceFee()));
        task.setSettleAmount(defaultAmount(task.getRewardAmount().subtract(task.getServiceFee())));
        task.setTradeOrderNo("TRD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        task.setPriority(request.getPriority() == null ? 3 : request.getPriority());
        task.setStatus("OPEN");
        task.setDeadline(request.getDeadline());
        task.setProofRequired(Boolean.TRUE.equals(request.getProofRequired()));
        task.setViewCount(0);
        task.setCommentCount(0);
        task.setQualityScore(0);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    public IPage<InternshipTask> search(TaskQueryRequest request) {
        TaskQueryRequest query = request == null ? new TaskQueryRequest() : request;
        Page<InternshipTask> page = new Page<>(Math.max(1, query.getPage()), Math.max(1, Math.min(50, query.getSize())));

        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InternshipTask::getRewardAmount)
                .orderByAsc(InternshipTask::getPriority)
                .orderByDesc(InternshipTask::getCreatedAt);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(InternshipTask::getTitle, query.getKeyword())
                    .or()
                    .like(InternshipTask::getDescription, query.getKeyword())
                    .or()
                    .like(InternshipTask::getTags, query.getKeyword())
                    .or()
                    .like(InternshipTask::getLocation, query.getKeyword()));
        }

        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(InternshipTask::getStatus, query.getStatus().toUpperCase());
        } else {
            wrapper.eq(InternshipTask::getStatus, "OPEN");
        }

        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(InternshipTask::getCategory, query.getCategory().toUpperCase());
        }
        if (query.getMinPriority() != null) {
            wrapper.ge(InternshipTask::getPriority, query.getMinPriority());
        }
        if (query.getMaxPriority() != null) {
            wrapper.le(InternshipTask::getPriority, query.getMaxPriority());
        }

        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public InternshipTask detail(Long id) {
        InternshipTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(4041, "task not found");
        }

        task.setViewCount((task.getViewCount() == null ? 0 : task.getViewCount()) + 1);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        cacheClient.delete(TASK_DETAIL_CACHE_KEY_PREFIX + id);
        redisTemplate.opsForZSet().add(HOT_KEY, String.valueOf(task.getId()), hotScore(task));
        return task;
    }

    @Override
    public List<InternshipTask> hot() {
        Set<String> ids = redisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, 9);
        if (ids == null || ids.isEmpty()) {
            rebuildHot();
            ids = redisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, 9);
        }
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<CompletableFuture<InternshipTask>> futures = new ArrayList<>();
        for (String id : ids) {
            final Long taskId = Long.valueOf(id);
            futures.add(CompletableFuture.supplyAsync(new Supplier<InternshipTask>() {
                @Override
                public InternshipTask get() {
                    return queryTaskWithCacheProtection(taskId);
                }
            }, taskQueryExecutor));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    @Override
    public void acceptTask(Long id) {
        InternshipTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(4041, "task not found");
        }

        Long uid = UserContext.userId();
        if (uid == null) {
            throw new BizException(401, "user not logged in");
        }
        if ("CANCELLED".equals(task.getStatus())) {
            throw new BizException(ErrorCode.TASK_CANCELLED);
        }
        if (!"OPEN".equals(task.getStatus())) {
            throw new BizException(4100, "only open task can be accepted");
        }

        LocalDateTime now = LocalDateTime.now();
        task.setAcceptorId(uid);
        task.setAcceptedAt(now);
        task.setStatus("TAKEN");
        task.setUpdatedAt(now);
        taskMapper.updateById(task);
    }

    @Override
    public void cancelTask(Long id, Long uid, String reason) {
        InternshipTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(4041, "task not found");
        }
        if (!uid.equals(task.getPublisherId())) {
            throw new BizException(4033, "only publisher can cancel task");
        }
        if (!"OPEN".equals(task.getStatus())) {
            throw new BizException(4108, "only open task can be cancelled");
        }

        LocalDateTime now = LocalDateTime.now();
        task.setCancelledAt(now);
        task.setCancelReason(reason);
        task.setStatus("CANCELLED");
        task.setUpdatedAt(now);
        taskMapper.updateById(task);
    }

    @Override
    public IPage<InternshipTask> publishedTasks(Long uid, TaskQueryRequest request) {
        TaskQueryRequest query = request == null ? new TaskQueryRequest() : request;
        Page<InternshipTask> page = new Page<>(Math.max(1, query.getPage()), Math.max(1, Math.min(50, query.getSize())));

        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipTask::getPublisherId, uid)
                .orderByDesc(InternshipTask::getCreatedAt);

        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(InternshipTask::getStatus, query.getStatus().toUpperCase());
        }
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(InternshipTask::getCategory, query.getCategory().toUpperCase());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(InternshipTask::getTitle, query.getKeyword())
                    .or()
                    .like(InternshipTask::getDescription, query.getKeyword())
                    .or()
                    .like(InternshipTask::getTags, query.getKeyword())
                    .or()
                    .like(InternshipTask::getLocation, query.getKeyword()));
        }
        if (query.getMinPriority() != null) {
            wrapper.ge(InternshipTask::getPriority, query.getMinPriority());
        }
        if (query.getMaxPriority() != null) {
            wrapper.le(InternshipTask::getPriority, query.getMaxPriority());
        }

        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<InternshipTask> acceptedTasks(Long uid, TaskQueryRequest request) {
        TaskQueryRequest query = request == null ? new TaskQueryRequest() : request;
        Page<InternshipTask> page = new Page<>(Math.max(1, query.getPage()), Math.max(1, Math.min(50, query.getSize())));

        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipTask::getAcceptorId, uid)
                .orderByDesc(InternshipTask::getCreatedAt);

        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(InternshipTask::getStatus, query.getStatus().toUpperCase());
        }
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(InternshipTask::getCategory, query.getCategory().toUpperCase());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(InternshipTask::getTitle, query.getKeyword())
                    .or()
                    .like(InternshipTask::getDescription, query.getKeyword())
                    .or()
                    .like(InternshipTask::getTags, query.getKeyword())
                    .or()
                    .like(InternshipTask::getLocation, query.getKeyword()));
        }
        if (query.getMinPriority() != null) {
            wrapper.ge(InternshipTask::getPriority, query.getMinPriority());
        }
        if (query.getMaxPriority() != null) {
            wrapper.le(InternshipTask::getPriority, query.getMaxPriority());
        }

        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<InternshipTask> reviewTasks(Long uid, TaskQueryRequest request) {
        TaskQueryRequest query = request == null ? new TaskQueryRequest() : request;
        Page<InternshipTask> page = new Page<>(Math.max(1, query.getPage()), Math.max(1, Math.min(50, query.getSize())));

        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipTask::getPublisherId, uid)
                .eq(InternshipTask::getStatus, "SUBMITTED")
                .orderByDesc(InternshipTask::getSubmittedAt);

        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public void submitTask(Long id, Long uid, SubmitTaskRequest submitTaskRequest) {
        InternshipTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        if (!uid.equals(task.getAcceptorId())) {
            throw new BizException(4031, "only acceptor can submit task");
        }
        if (!"TAKEN".equals(task.getStatus())) {
            throw new BizException(4101, "only taken task can be submitted");
        }

        Long count = taskSubmissionMapper.selectCount(
                new LambdaQueryWrapper<TaskSubmission>()
                        .eq(TaskSubmission::getTaskId, id)
        );
        Integer roundNo = (count == null ? 0 : count.intValue()) + 1;
        LocalDateTime now = LocalDateTime.now();

        TaskSubmission submission = new TaskSubmission();
        submission.setTaskId(id);
        submission.setSubmitUserId(uid);
        submission.setRoundNo(roundNo);
        submission.setContent(submitTaskRequest.getContent());
        submission.setProofUrls(submitTaskRequest.getProofUrls());
        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(now);
        submission.setCreatedAt(now);
        submission.setUpdatedAt(now);
        taskSubmissionMapper.insert(submission);

        task.setStatus("SUBMITTED");
        task.setSubmittedAt(now);
        task.setUpdatedAt(now);
        taskMapper.updateById(task);

        TaskSubmittedEvent taskSubmittedEvent = new TaskSubmittedEvent(
                UUID.randomUUID().toString().replace("-", ""),
                task.getId(),
                submission.getId(),
                task.getPublisherId(),
                uid,
                roundNo,
                submitTaskRequest.getContent(),
                submitTaskRequest.getProofUrls(),
                now
        );
        taskEventProducer.sendTaskSubmitted(taskSubmittedEvent);
    }

    @Override
    public void approveTask(Long id, Long uid) {
        InternshipTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        if (!uid.equals(task.getPublisherId())) {
            throw new BizException(4032, "only publisher can approve task");
        }
        if (!"SUBMITTED".equals(task.getStatus())) {
            throw new BizException(4102, "only submitted task can be approved");
        }

        TaskSubmission latestSubmission = taskSubmissionMapper.selectOne(
                new LambdaQueryWrapper<TaskSubmission>()
                        .eq(TaskSubmission::getTaskId, id)
                        .orderByDesc(TaskSubmission::getRoundNo)
                        .last("limit 1")
        );
        if (latestSubmission == null) {
            throw new BizException(4045, "task submission not found");
        }
        if (!"SUBMITTED".equals(latestSubmission.getStatus())) {
            throw new BizException(4103, "latest submission is not in submitted status");
        }

        TaskSettlement existedSettlement = taskSettlementMapper.selectOne(
                new LambdaQueryWrapper<TaskSettlement>()
                        .eq(TaskSettlement::getTaskId, task.getId())
                        .eq(TaskSettlement::getStatus, "PENDING")
                        .last("limit 1")
        );
        if (existedSettlement != null) {
            throw new BizException(4106, "task settlement already pending");
        }

        LocalDateTime now = LocalDateTime.now();
        latestSubmission.setStatus("APPROVED");
        latestSubmission.setReviewedAt(now);
        latestSubmission.setUpdatedAt(now);
        taskSubmissionMapper.updateById(latestSubmission);

        TaskSettlement settlement = new TaskSettlement();
        settlement.setTaskId(task.getId());
        settlement.setSubmissionId(latestSubmission.getId());
        settlement.setPublisherId(task.getPublisherId());
        settlement.setAcceptorId(task.getAcceptorId());
        settlement.setSettleAmount(task.getSettleAmount());
        settlement.setStatus("PENDING");
        settlement.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        settlement.setCreatedAt(now);
        settlement.setUpdatedAt(now);
        taskSettlementMapper.insert(settlement);

        task.setStatus("SETTLEMENT_PENDING");
        task.setApprovedAt(now);
        task.setUpdatedAt(now);
        taskMapper.updateById(task);

        TaskSettlementRequestedEvent event = new TaskSettlementRequestedEvent(
                settlement.getMessageId(),
                settlement.getId(),
                task.getId(),
                latestSubmission.getId(),
                task.getPublisherId(),
                task.getAcceptorId(),
                task.getTradeOrderNo(),
                task.getSettleAmount(),
                now
        );
        taskEventProducer.sendTaskSettlementRequested(event);
    }

    @Override
    public void rejectTask(Long id, Long uid, RejectTaskRequest request) {
        InternshipTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }
        if (!uid.equals(task.getPublisherId())) {
            throw new BizException(4032, "only publisher can reject task");
        }
        if (!"SUBMITTED".equals(task.getStatus())) {
            throw new BizException(4103, "only submitted task can be rejected");
        }

        TaskSubmission latestSubmission = taskSubmissionMapper.selectOne(
                new LambdaQueryWrapper<TaskSubmission>()
                        .eq(TaskSubmission::getTaskId, id)
                        .orderByDesc(TaskSubmission::getRoundNo)
                        .last("limit 1")
        );
        if (latestSubmission == null) {
            throw new BizException(4045, "task submission not found");
        }
        if (!"SUBMITTED".equals(latestSubmission.getStatus())) {
            throw new BizException(4104, "latest submission is not in submitted status");
        }

        String reason = request == null ? null : request.getReason();
        if (!StringUtils.hasText(reason)) {
            throw new BizException(4105, "reject reason can not be blank");
        }

        LocalDateTime now = LocalDateTime.now();
        latestSubmission.setStatus("REJECTED");
        latestSubmission.setRejectReason(reason);
        latestSubmission.setReviewedAt(now);
        latestSubmission.setUpdatedAt(now);
        taskSubmissionMapper.updateById(latestSubmission);

        task.setStatus("TAKEN");
        task.setRejectReason(reason);
        task.setUpdatedAt(now);
        taskMapper.updateById(task);

        TaskRejectedEvent taskRejectedEvent = new TaskRejectedEvent(
                UUID.randomUUID().toString().replace("-", ""),
                task.getId(),
                latestSubmission.getId(),
                task.getPublisherId(),
                task.getAcceptorId(),
                latestSubmission.getRoundNo(),
                reason,
                now
        );
        taskEventProducer.sendTaskRejected(taskRejectedEvent);
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void rebuildHot() {
        List<InternshipTask> list = taskMapper.selectList(new LambdaQueryWrapper<InternshipTask>()
                .in(InternshipTask::getStatus, "OPEN", "TAKEN", "SUBMITTED")
                .orderByDesc(InternshipTask::getRewardAmount)
                .orderByDesc(InternshipTask::getCreatedAt)
                .last("limit 200"));

        redisTemplate.delete(HOT_KEY);
        for (InternshipTask task : list) {
            redisTemplate.opsForZSet().add(HOT_KEY, String.valueOf(task.getId()), hotScore(task));
        }
    }

    private double hotScore(InternshipTask task) {
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

    private BigDecimal defaultAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return amount;
    }
}