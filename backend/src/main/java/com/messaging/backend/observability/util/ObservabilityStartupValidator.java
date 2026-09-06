package com.messaging.backend.observability.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Validates and logs the initialization of production-critical infrastructure components.
 */
@Slf4j
@Component
public class ObservabilityStartupValidator implements ApplicationListener<ApplicationReadyEvent> {

    private final ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider;
    private final ObjectProvider<CacheManager> cacheManagerProvider;

    public ObservabilityStartupValidator(
            ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider,
            ObjectProvider<CacheManager> cacheManagerProvider) {
        this.redisConnectionFactoryProvider = redisConnectionFactoryProvider;
        this.cacheManagerProvider = cacheManagerProvider;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=================================================");
        log.info("      STARTING INFRASTRUCTURE VALIDATION         ");
        log.info("=================================================");

        validateRedis();
        validateCache();

        log.info("=================================================");
        log.info("      INFRASTRUCTURE VALIDATION COMPLETE         ");
        log.info("=================================================");
    }

    private void validateRedis() {
        try {
            RedisConnectionFactory factory = redisConnectionFactoryProvider.getIfAvailable();
            if (factory != null && factory.getConnection() != null) {
                factory.getConnection().close();
                log.info("✓ Redis Connected");
            } else {
                log.warn("✗ Redis Connection Failed");
            }
        } catch (Exception e) {
            log.warn("✗ Redis Connection Failed: {}", e.getMessage());
        }
    }

    private void validateCache() {
        if (cacheManagerProvider.getIfAvailable() != null) {
            log.info("✓ Cache Initialized");
        } else {
            log.warn("✗ Cache Manager not found");
        }
    }
}
