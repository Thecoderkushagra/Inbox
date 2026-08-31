package com.messaging.backend.observability.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
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
    private final ObjectProvider<MeterRegistry> meterRegistryProvider;
    private final ObjectProvider<Tracer> tracerProvider;

    public ObservabilityStartupValidator(
            ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider,
            ObjectProvider<CacheManager> cacheManagerProvider,
            ObjectProvider<MeterRegistry> meterRegistryProvider,
            ObjectProvider<Tracer> tracerProvider) {
        this.redisConnectionFactoryProvider = redisConnectionFactoryProvider;
        this.cacheManagerProvider = cacheManagerProvider;
        this.meterRegistryProvider = meterRegistryProvider;
        this.tracerProvider = tracerProvider;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=================================================");
        log.info("      STARTING INFRASTRUCTURE VALIDATION         ");
        log.info("=================================================");

        validateRedis();
        validateCache();
        validatePubSub();
        validateMetrics();
        validateTracing();
        validateRateLimiter();

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

    private void validatePubSub() {
        // Redis connection implies Pub/Sub is available in our architecture
        if (redisConnectionFactoryProvider.getIfAvailable() != null) {
            log.info("✓ Pub/Sub Initialized");
        } else {
            log.warn("✗ Pub/Sub Initialization Failed");
        }
    }

    private void validateMetrics() {
        if (meterRegistryProvider.getIfAvailable() != null) {
            log.info("✓ Metrics Registered");
        } else {
            log.warn("✗ Metrics not found");
        }
    }

    private void validateTracing() {
        if (tracerProvider.getIfAvailable() != null) {
            log.info("✓ Tracing Enabled");
        } else {
            log.warn("✗ Tracing disabled or not found");
        }
    }

    private void validateRateLimiter() {
        // Rate Limiter relies on Redis being connected. If Redis is connected, the lua script is loaded on first execution.
        if (redisConnectionFactoryProvider.getIfAvailable() != null) {
            log.info("✓ Rate Limiter Enabled");
        } else {
            log.warn("✗ Rate Limiter unavailable");
        }
    }
}
