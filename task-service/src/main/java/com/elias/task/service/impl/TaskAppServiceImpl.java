package com.elias.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elias.common.ApiResponse;
import com.elias.common.context.UserContext;
import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import com.elias.task.cache.CacheClient;
import com.elias.task.client.AuthClient;
import com.elias.task.config.UserContextConfig;
import com.elias.task.dto.CreateTaskRequest;
import com.elias.task.dto.TaskQueryRequest;
import com.elias.task.dto.UserInfoDTO;
import com.elias.task.entity.InternshipTask;
import com.elias.task.mapper.InternshipTaskMapper;
import com.elias.task.service.TaskAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
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

        InternshipTask task = new InternshipTask();
        task.setPublisherId(ownerId);
        task.setAcceptorId(null);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory().toUpperCase() : "GENERAL");
        task.setTags(request.getTechStack());
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
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    public IPage<InternshipTask> search(TaskQueryRequest request) {
        Page<InternshipTask> page = new Page<>(Math.max(1, request.getPage()), Math.max(1, Math.min(50, request.getSize())));

        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InternshipTask::getRewardAmount)
                .orderByAsc(InternshipTask::getPriority)
                .orderByDesc(InternshipTask::getCreatedAt);

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(InternshipTask::getTitle, request.getKeyword())
                    .or()
                    .like(InternshipTask::getDescription, request.getKeyword())
                    .or()
                    .like(InternshipTask::getTags, request.getKeyword())
                    .or()
                    .like(InternshipTask::getLocation, request.getKeyword()));
        }

        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(InternshipTask::getStatus, request.getStatus().toUpperCase());
        } else {
            wrapper.eq(InternshipTask::getStatus, "OPEN");
        }

        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(InternshipTask::getCategory, request.getCategory().toUpperCase());
        }
        if (request.getMinPriority() != null) {
            wrapper.ge(InternshipTask::getPriority, request.getMinPriority());
        }
        if (request.getMaxPriority() != null) {
            wrapper.le(InternshipTask::getPriority, request.getMaxPriority());
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

    //接单
    @Override
    public void acceptTask(Long id) {
        InternshipTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(4041, "task not found");
        }
        Long UID = UserContext.userId();
        if (UID == null){
            throw new BizException(401, "用户不存在, 请先登录正确的账户");
        }
        if ("CANCELLED".equals(task.getStatus())) {
            throw new BizException(ErrorCode.TASK_CANCELLED);
        }
        task.setAcceptorId(UID);
        task.setAcceptedAt(LocalDateTime.now());
    }
    //取消任务
    @Override
    public void cancelTask(Long id, Long uid, String reason) {
        InternshipTask internshipTask = taskMapper.selectById(id);
        if (internshipTask == null) {
            throw new BizException(4041, "task not found");
        }
        LocalDateTime now = LocalDateTime.now();
        internshipTask.setCancelledAt(now);
        internshipTask.setCancelReason(reason);
        internshipTask.setStatus("CANCELLED");
    }
    // 查询当前用户创建的任务
    public IPage<InternshipTask> publishedTasks(Long uid, TaskQueryRequest request) {
        long pageNum = request == null || request.getPage() == null ? 1 : request.getPage();
        long pageSize = request == null || request.getSize() == null ? 10 : request.getSize();

        Page<InternshipTask> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipTask::getPublisherId, uid)
                .orderByDesc(InternshipTask::getCreatedAt);

        if (request != null) {
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                wrapper.eq(InternshipTask::getStatus, request.getStatus().toUpperCase());
            }

            if (request.getCategory() != null && !request.getCategory().isEmpty()) {
                wrapper.eq(InternshipTask::getCategory, request.getCategory().toUpperCase());
            }

            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                wrapper.and(w -> w.like(InternshipTask::getTitle, request.getKeyword())
                        .or()
                        .like(InternshipTask::getDescription, request.getKeyword())
                        .or()
                        .like(InternshipTask::getTags, request.getKeyword())
                        .or()
                        .like(InternshipTask::getLocation, request.getKeyword()));
            }

            if (request.getMinPriority() != null) {
                wrapper.ge(InternshipTask::getPriority, request.getMinPriority());
            }

            if (request.getMaxPriority() != null) {
                wrapper.le(InternshipTask::getPriority, request.getMaxPriority());
            }
        }

        return taskMapper.selectPage(page, wrapper);
    }


    @Override
    public IPage<InternshipTask> acceptedTasks(Long uid, TaskQueryRequest request) {
        // 创建分页对象
        // 第一个参数是当前页码，最小为 1
        // 第二个参数是每页条数，最小为 1，最大限制为 50，防止一次查太多数据
        Page<InternshipTask> page = new Page<>(
                Math.max(1, request.getPage()),
                Math.max(1, Math.min(50, request.getSize()))
        );

        // 构造查询条件
        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();

        // 只查询当前用户发布的任务
        // 并按照创建时间倒序排列，最新发布的任务排前面
        wrapper.eq(InternshipTask::getAcceptorId, uid)
                .orderByDesc(InternshipTask::getCreatedAt);

        // 如果前端传了任务状态，则按状态精确查询
        // toUpperCase() 是为了和数据库里统一的大写状态值匹配
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(InternshipTask::getStatus, request.getStatus().toUpperCase());
        }

        // 如果前端传了任务分类，则按分类精确查询
        // 同样转成大写，避免大小写不一致
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(InternshipTask::getCategory, request.getCategory().toUpperCase());
        }

        // 如果前端传了关键字，则做模糊搜索
        // 关键字会在以下字段中匹配：
        // 标题、描述、标签、地点
        // 这些条件之间是 or，也就是任意一个字段匹配都可以查出来
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(InternshipTask::getTitle, request.getKeyword())
                    .or()
                    .like(InternshipTask::getDescription, request.getKeyword())
                    .or()
                    .like(InternshipTask::getTags, request.getKeyword())
                    .or()
                    .like(InternshipTask::getLocation, request.getKeyword()));
        }

        // 如果传了最小优先级，则查询优先级大于等于该值的任务
        if (request.getMinPriority() != null) {
            wrapper.ge(InternshipTask::getPriority, request.getMinPriority());
        }

        // 如果传了最大优先级，则查询优先级小于等于该值的任务
        if (request.getMaxPriority() != null) {
            wrapper.le(InternshipTask::getPriority, request.getMaxPriority());
        }

        // 执行分页查询并返回结果
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<InternshipTask> reviewTasks(Long uid, TaskQueryRequest request) {
        Page<InternshipTask> page = new Page<>(
                Math.max(1, request.getPage()),
                Math.max(1, Math.min(50, request.getSize()))
        );

        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipTask::getPublisherId, uid)
                .eq(InternshipTask::getStatus, "SUBMITTED")
                .orderByDesc(InternshipTask::getSubmittedAt);

        return taskMapper.selectPage(page, wrapper);
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