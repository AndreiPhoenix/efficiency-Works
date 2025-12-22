package com.example.caching.service;

import com.example.caching.repository.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheStats cacheStats;

    public void put(String key, Object value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public Object get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            cacheStats.recordHit();
        } else {
            cacheStats.recordMiss();
        }
        return value;
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Long getRemainingTTL(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public Set<String> getAllKeys() {
        return redisTemplate.keys("*");
    }

    public void clearCache() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public Long getCacheSize() {
        Set<String> keys = redisTemplate.keys("*");
        return keys != null ? (long) keys.size() : 0L;
    }
}