package com.elias.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elias.common.ApiResponse;
import com.elias.common.exception.BizException;
import com.elias.task.cache.CacheClient;
import com.elias.task.client.AuthClient;
import com.elias.task.dto.CreateTaskRequest;
import com.elias.task.dto.TaskQueryRequest;
import com.elias.task.dto.UserInfoDTO;
import com.elias.task.entity.InternshipTask;
import com.elias.task.mapper.InternshipTaskMapper;
import com.elias.task.service.TaskAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    @Override
    public Long create(CreateTaskRequest request, Long ownerId) {
        // 1) 先通过auth-service校验owner是否存在
        ApiResponse<UserInfoDTO> userInfoResp = authClient.userInfo(ownerId);
        // 1.1) 远程校验失败时终止创建
        if (userInfoResp == null || userInfoResp.getData() == null) {
            throw new BizException(4004, "owner not found via auth-service");
        }

        // 2) 组装任务基础字段
        InternshipTask task = new InternshipTask();
        task.setOwnerId(ownerId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTechStack(request.getTechStack());

        // 2.1) 设置默认业务字段
        task.setPriority(request.getPriority() == null ? 3 : request.getPriority());
        task.setStatus("TODO");
        task.setProgress(0);
        task.setCommentCount(0);

        // 2.2) 计算质量分并设置时间字段
        task.setQualityScore(calculateQuality(request.getTitle(), request.getDescription()));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        // 3) 入库并返回新任务ID
        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    public IPage<InternshipTask> search(TaskQueryRequest request) {
        // 4) 构建分页参数（页码最小1，每页最多50）
        Page<InternshipTask> page = new Page<>(Math.max(1, request.getPage()), Math.max(1, Math.min(50, request.getSize())));

        // 4.1) 构建动态查询条件并按更新时间倒序
        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InternshipTask::getUpdatedAt);

        // 4.2) 关键词条件（标题/描述/技术栈）
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(InternshipTask::getTitle, request.getKeyword())
                    .or()
                    .like(InternshipTask::getDescription, request.getKeyword())
                    .or()
                    .like(InternshipTask::getTechStack, request.getKeyword()));
        }

        // 4.3) 状态条件
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(InternshipTask::getStatus, request.getStatus());
        }

        // 4.4) 优先级区间条件
        if (request.getMinPriority() != null) {
            wrapper.ge(InternshipTask::getPriority, request.getMinPriority());
        }
        if (request.getMaxPriority() != null) {
            wrapper.le(InternshipTask::getPriority, request.getMaxPriority());
        }

        // 5) 执行分页查询并返回标准分页对象
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public InternshipTask detail(Long id) {
        // 6) 查询任务详情
        InternshipTask task = taskMapper.selectById(id);
        // 6.1) 任务不存在则抛业务异常
        if (task == null) {
            throw new BizException(4041, "task not found");
        }

        // 6.2) 每次详情访问更新进度与更新时间（示例业务逻辑）
        task.setProgress(Math.min(100, task.getProgress() + 1));
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        // 6.3) 主动删除详情缓存，避免旧值
        cacheClient.delete(TASK_DETAIL_CACHE_KEY_PREFIX + id);

        // 6.4) 刷新热榜分值
        redisTemplate.opsForZSet().add(HOT_KEY, String.valueOf(task.getId()), hotScore(task));
        return task;
    }

    @Override
    public List<InternshipTask> hot() {
        // 7) 先从Redis读取热榜Top10,reverseRange这个是按照分数从低到高取
        Set<String> ids = redisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, 9);

        // 7.1) 热榜为空时触发重建并再读一次
        if (ids == null || ids.isEmpty()) {
            rebuildHot();
            ids = redisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, 9);
        }

        // 7.2) 仍为空则返回空列表
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // 7.3) 批量回填任务详情（带缓存防护：穿透/击穿/雪崩）
        return ids.stream()
                .map(Long::valueOf)
                .map(this::queryTaskWithCacheProtection)
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void rebuildHot() {
        // 8) 拉取最近更新的任务作为热榜候选
        List<InternshipTask> list = taskMapper.selectList(new LambdaQueryWrapper<InternshipTask>()
                .orderByDesc(InternshipTask::getUpdatedAt)
                .last("limit 200"));

        // 8.1) 清空旧热榜
        redisTemplate.delete(HOT_KEY);

        // 8.2) 重新写入热榜分值
        for (InternshipTask task : list) {
            redisTemplate.opsForZSet().add(HOT_KEY, String.valueOf(task.getId()), hotScore(task));
        }
    }

    private int calculateQuality(String title, String desc) {
        // 9) 根据标题与描述长度给出基础质量分（演示规则）
        int score = 40 + Math.min(title.length(), 40) / 2 + Math.min(desc.length(), 1000) / 50;
        return Math.min(100, score);
    }

    private double hotScore(InternshipTask task) {
        // 10) 综合质量、进度、评论、优先级计算热度分
        return task.getQualityScore() + task.getProgress() * 0.5 + task.getCommentCount() * 2 + (6 - task.getPriority()) * 3;
    }

    private InternshipTask queryTaskWithCacheProtection(Long taskId) {
        // 11) 查询任务详情缓存：
        // 11.1) 空值缓存防穿透 11.2) 互斥锁防击穿 11.3) TTL抖动防雪崩
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
}
