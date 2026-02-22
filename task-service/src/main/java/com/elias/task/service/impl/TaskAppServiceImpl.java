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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskAppServiceImpl implements TaskAppService {

    private static final String HOT_KEY = "task:hot:zset";

    private final InternshipTaskMapper taskMapper;
    private final StringRedisTemplate redisTemplate;
    private final AuthClient authClient;

    public TaskAppServiceImpl(InternshipTaskMapper taskMapper, StringRedisTemplate redisTemplate, AuthClient authClient) {
        this.taskMapper = taskMapper;
        this.redisTemplate = redisTemplate;
        this.authClient = authClient;
    }

    @Override
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
    public void rebuildHot() {
        List<InternshipTask> list = taskMapper.selectList(new LambdaQueryWrapper<InternshipTask>()
                .orderByDesc(InternshipTask::getUpdatedAt)
                .last("limit 200"));
        redisTemplate.delete(HOT_KEY);
        for (InternshipTask task : list) {
            redisTemplate.opsForZSet().add(HOT_KEY, String.valueOf(task.getId()), hotScore(task));
        }
    }

    private int calculateQuality(String title, String desc) {
        int score = 40 + Math.min(title.length(), 40) / 2 + Math.min(desc.length(), 1000) / 50;
        return Math.min(100, score);
    }

    private double hotScore(InternshipTask task) {
        return task.getQualityScore() + task.getProgress() * 0.5 + task.getCommentCount() * 2 + (6 - task.getPriority()) * 3;
    }
}
