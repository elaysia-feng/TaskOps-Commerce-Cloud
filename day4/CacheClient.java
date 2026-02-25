package com.elias.task.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Redis cache utility for:
 * 1) cache penetration  2) cache breakdown  3) cache avalanche.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClient {

    private static final String EMPTY_VALUE = "__NULL__";
    private static final long NULL_TTL_SECONDS = 120;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public <R, ID> R queryWithMutex(
            String keyPrefix,
            String lockPrefix,
            ID id,
            Class<R> type,
            Function<ID, R> dbFallback,
            long ttl,
            TimeUnit unit
    ) {
        // 1) 计算业务缓存Key并尝试读取缓存
        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key);

        // 1.1) 命中缓存：直接返回（含空值缓存判定）
        if (json != null) {
            if (EMPTY_VALUE.equals(json)) {
                return null;
            }
            return toBean(json, type);
        }

        // 2) 缓存未命中：尝试互斥锁，防止击穿时并发回源
        String lockKey = lockPrefix + id;
        boolean lock = tryLock(lockKey);
        if (!lock) {
            // 2.1) 未拿到锁：短暂等待后重试
            sleepMillis(50);
            return queryWithMutex(keyPrefix, lockPrefix, id, type, dbFallback, ttl, unit);
        }

        try {
            // 3) 拿到锁后进行二次检查，避免重复重建
            json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                if (EMPTY_VALUE.equals(json)) {
                    return null;
                }
                return toBean(json, type);
            }

            // 4) 回源数据库查询
            R result = dbFallback.apply(id);

            // 4.1) 数据库为空：写入空值缓存，防穿透
            if (result == null) {
                redisTemplate.opsForValue().set(key, EMPTY_VALUE, NULL_TTL_SECONDS, TimeUnit.SECONDS);
                return null;
            }

            // 4.2) 数据库非空：写入带随机抖动TTL，防雪崩
            setWithJitter(key, result, ttl, unit);
            return result;
        } finally {
            // 5) 无论成功失败都释放锁
            unlock(lockKey);
        }
    }

    public void delete(String key) {
        // 6) 业务写操作后主动失效缓存
        redisTemplate.delete(key);
    }

    private boolean tryLock(String lockKey) {
        // 7) setIfAbsent + 过期时间：避免死锁
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(ok);
    }

    private void unlock(String lockKey) {
        // 8) 删除互斥锁
        redisTemplate.delete(lockKey);
    }

    private void setWithJitter(String key, Object value, long ttl, TimeUnit unit) {
        // 9) TTL随机抖动：打散同批次key过期时间
        long baseSeconds = unit.toSeconds(ttl);
        long jitter = Math.max(1, baseSeconds / 5);
        long finalSeconds = baseSeconds + ThreadLocalRandom.current().nextLong(jitter + 1);
        redisTemplate.opsForValue().set(key, toJson(value), finalSeconds, TimeUnit.SECONDS);
    }

    private String toJson(Object value) {
        // 10) 对象序列化为JSON存入Redis
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Serialize cache value failed", e);
        }
    }

    private <R> R toBean(String json, Class<R> type) {
        // 11) JSON反序列化为目标对象
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Deserialize cache value failed", e);
        }
    }

    private void sleepMillis(long ms) {
        // 12) 获取锁失败时短暂休眠，降低CPU空转
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Cache rebuild wait interrupted", e);
        }
    }
}
