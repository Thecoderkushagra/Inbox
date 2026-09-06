package com.messaging.backend.ratelimit.service;

import com.messaging.backend.ratelimit.config.RateLimitProperties;
import com.messaging.backend.ratelimit.constants.RateLimitPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    
    // Sliding window algorithm via Redis Lua Script
    private static final String SCRIPT = 
        "local key = KEYS[1]\n" +
        "local window_ms = tonumber(ARGV[1])\n" +
        "local limit = tonumber(ARGV[2])\n" +
        "local now_ms = tonumber(ARGV[3])\n" +
        "local random_id = ARGV[4]\n" +
        "local clear_before = now_ms - window_ms\n" +
        "redis.call('ZREMRANGEBYSCORE', key, 0, clear_before)\n" +
        "local count = redis.call('ZCARD', key)\n" +
        "if count >= limit then\n" +
        "    return -1\n" +
        "end\n" +
        "redis.call('ZADD', key, now_ms, now_ms .. '-' .. random_id)\n" +
        "redis.call('PEXPIRE', key, window_ms + 1000)\n" +
        "return limit - count - 1";

    private static class WindowBucket {
        final ReentrantLock lock = new ReentrantLock();
        final Deque<Long> timestamps = new ArrayDeque<>();
    }

    private final ConcurrentHashMap<String, WindowBucket> localBuckets = new ConcurrentHashMap<>();
    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimitProperties properties;
    private final DefaultRedisScript<Long> redisScript;

    public RateLimitService(StringRedisTemplate stringRedisTemplate, RateLimitProperties properties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.properties = properties;
        this.redisScript = new DefaultRedisScript<>(SCRIPT, Long.class);
    }

    public RateLimitResult consume(RateLimitPolicy policyEnum, String identifier) {
        if (!properties.isEnabled()) {
            return new RateLimitResult(true, 1, 0);
        }

        String policyKey = policyEnum.name().toLowerCase();
        RateLimitProperties.Policy policy = properties.getPolicies().get(policyKey);
        
        if (policy == null) {
            log.warn("Rate limit policy not found for: {}, failing OPEN", policyKey);
            return new RateLimitResult(true, 1, 0);
        }

        String rateKey = "rate:" + policyKey + ":" + identifier;
        long windowMs = policy.getWindowSeconds() * 1000L;
        long limit = policy.getLimit();
        long nowMs = Instant.now().toEpochMilli();

        // Use local in-memory sliding window when useRedis is false (saves Upstash daily quotas)
        if (!properties.isUseRedis()) {
            return consumeLocally(rateKey, windowMs, limit, policy.getWindowSeconds(), nowMs);
        }

        String randomId = UUID.randomUUID().toString();
        try {
            Long remaining = stringRedisTemplate.execute(
                    redisScript,
                    Collections.singletonList(rateKey),
                    String.valueOf(windowMs),
                    String.valueOf(limit),
                    String.valueOf(nowMs),
                    randomId
            );

            if (remaining == null || remaining < 0) {
                log.warn("Rate limit EXCEEDED for key: {}", rateKey);
                return new RateLimitResult(false, 0, policy.getWindowSeconds());
            }

            log.debug("Rate limit CONSUMED for key: {}, remaining: {}", rateKey, remaining);
            return new RateLimitResult(true, remaining, 0);
            
        } catch (Exception e) {
            log.error("Redis failure during rate limiting for key: {}, falling back to local memory", rateKey, e);
            return consumeLocally(rateKey, windowMs, limit, policy.getWindowSeconds(), nowMs);
        }
    }

    private RateLimitResult consumeLocally(String rateKey, long windowMs, long limit, int windowSeconds, long nowMs) {
        WindowBucket bucket = localBuckets.computeIfAbsent(rateKey, k -> new WindowBucket());
        bucket.lock.lock();
        try {
            long clearBefore = nowMs - windowMs;
            while (!bucket.timestamps.isEmpty() && bucket.timestamps.peekFirst() < clearBefore) {
                bucket.timestamps.pollFirst();
            }
            if (bucket.timestamps.size() >= limit) {
                log.warn("Rate limit EXCEEDED locally for key: {}", rateKey);
                return new RateLimitResult(false, 0, windowSeconds);
            }
            bucket.timestamps.addLast(nowMs);
            long remaining = limit - bucket.timestamps.size();
            log.debug("Rate limit CONSUMED locally for key: {}, remaining: {}", rateKey, remaining);
            return new RateLimitResult(true, remaining, 0);
        } finally {
            bucket.lock.unlock();
        }
    }
}
