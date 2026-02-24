package com.elias.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elias.common.ApiResponse;
import com.elias.common.exception.BizException;
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
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
/**
 * 文件说明：任务服务业务实现。
 * 组件职责：
 * 1) 创建、查询、详情、热榜等任务核心能力。
 * 2) 调用 auth-service 校验任务归属用户。
 * 3) 基于 Redis ZSet 维护任务热度榜。
 */
public class TaskAppServiceImpl implements TaskAppService {

    private static final String HOT_KEY = "task:hot:zset";

    private final InternshipTaskMapper taskMapper;
    private final StringRedisTemplate redisTemplate;
    private final AuthClient authClient;

    @Override
    /**
     * 创建任务：
     * 1) 通过 Feign 调 auth-service 校验 owner；
     * 2) 组装任务默认字段并写库。
     */
    public Long create(CreateTaskRequest request, Long ownerId) {
        ApiResponse<UserInfoDTO> userInfoResp = authClient.userInfo(ownerId);
        if (userInfoResp == null || userInfoResp.getData() == null) {
            throw new BizException(4004, "owner not found via auth-service");
        }
        InternshipTask task = new InternshipTask();
        task.setOwnerId(ownerId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTechStack(request.getTechStack());
        task.setPriority(request.getPriority() == null ? 3 : request.getPriority());
        task.setStatus("TODO");
        task.setProgress(0);
        task.setCommentCount(0);
        task.setQualityScore(calculateQuality(request.getTitle(), request.getDescription()));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.insert(task);
        return task.getId();
    }

    @Override
    /**
     * 条件分页检索任务，支持关键字、状态、优先级区间。
     */
    public IPage<InternshipTask> search(TaskQueryRequest request) {
        Page<InternshipTask> page = new Page<>(Math.max(1, request.getPage()), Math.max(1, Math.min(50, request.getSize())));
        LambdaQueryWrapper<InternshipTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(InternshipTask::getUpdatedAt);
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(InternshipTask::getTitle, request.getKeyword())
                    .or()
                    .like(InternshipTask::getDescription, request.getKeyword())
                    .or()
                    .like(InternshipTask::getTechStack, request.getKeyword()));
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(InternshipTask::getStatus, request.getStatus());
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
    /**
     * 查询任务详情，并顺带更新进度与热度分。
     */
    public InternshipTask detail(Long id) {
        InternshipTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException(4041, "task not found");
        }
        task.setProgress(Math.min(100, task.getProgress() + 1));
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        redisTemplate.opsForZSet().add(HOT_KEY, String.valueOf(task.getId()), hotScore(task));
        return task;
    }

    @Override
    /**
     * 获取热榜 Top10：
     * 1) 先读 Redis；
     * 2) 缓存为空则触发重建。
     */
    public List<InternshipTask> hot() {
        Set<String> ids = redisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, 9);
        if (ids == null || ids.isEmpty()) {
            rebuildHot();
            ids = redisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, 9);
        }
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream().map(Long::valueOf).map(taskMapper::selectById).filter(item -> item != null).collect(Collectors.toList());
    }

    @Scheduled(cron = "0 */5 * * * ?")
    /**
     * 定时重建热榜缓存，降低热榜查询压力。
     */
    public void rebuildHot() {
        List<InternshipTask> list = taskMapper.selectList(new LambdaQueryWrapper<InternshipTask>()
                .orderByDesc(InternshipTask::getUpdatedAt)
                .last("limit 200"));
        redisTemplate.delete(HOT_KEY);
        for (InternshipTask task : list) {
            redisTemplate.opsForZSet().add(HOT_KEY, String.valueOf(task.getId()), hotScore(task));
        }
    }

    /**
     * 计算任务质量分，用于热榜打分。
     */
    private int calculateQuality(String title, String desc) {
        int score = 40 + Math.min(title.length(), 40) / 2 + Math.min(desc.length(), 1000) / 50;
        return Math.min(100, score);
    }

    /**
     * 计算任务热度分。
     */
    private double hotScore(InternshipTask task) {
        return task.getQualityScore() + task.getProgress() * 0.5 + task.getCommentCount() * 2 + (6 - task.getPriority()) * 3;
    }
}
