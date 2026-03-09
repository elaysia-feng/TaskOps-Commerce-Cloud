package com.elias.task.membership;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskQuotaService {

    private static final String USER_LEVEL_KEY_PREFIX = "task:member:level:";
    private static final MembershipLevel DEFAULT_LEVEL = MembershipLevel.FREE;

    private final StringRedisTemplate redisTemplate;

    public TaskQuotaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public MembershipLevel getLevel(Long userId) {
        String value = redisTemplate.opsForValue().get(USER_LEVEL_KEY_PREFIX + userId);
        if (value == null || value.isEmpty()) {
            return DEFAULT_LEVEL;
        }
        try {
            return MembershipLevel.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return DEFAULT_LEVEL;
        }
    }

    public void setLevel(Long userId, MembershipLevel level) {
        redisTemplate.opsForValue().set(USER_LEVEL_KEY_PREFIX + userId, level.name());
    }

    public int maxTasks(Long userId) {
        return getLevel(userId).maxTasks();
    }
}
