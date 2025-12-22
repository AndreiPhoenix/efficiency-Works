package com.example.caching.repository;

import lombok.Data;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Data
public class CacheStats {

    private final RedisTemplate<String, Object> redisTemplate;

    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long totalRequests = 0;

    public void recordHit() {
        cacheHits++;
        totalRequests++;
    }

    public void recordMiss() {
        cacheMisses++;
        totalRequests++;
    }

    public double getHitRate() {
        if (totalRequests == 0) return 0.0;
        return (double) cacheHits / totalRequests * 100;
    }

    public void clearStats() {
        cacheHits = 0;
        cacheMisses = 0;
        totalRequests = 0;
    }

    @Scheduled(fixedRate = 60000) // Каждую минуту
    public void logCacheStatistics() {
        System.out.println("=== Cache Statistics ===");
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Cache Hits: " + cacheHits);
        System.out.println("Cache Misses: " + cacheMisses);
        System.out.println("Hit Rate: " + String.format("%.2f", getHitRate()) + "%");
        System.out.println("Redis Keys: " + getRedisKeyCount());
        System.out.println("========================\n");
    }

    private long getRedisKeyCount() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    public Set<String> getAllCacheKeys() {
        return redisTemplate.keys("*");
    }

    public DataType getKeyType(String key) {
        return redisTemplate.type(key);
    }

    public Long getKeyTTL(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void clearAllCache() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}