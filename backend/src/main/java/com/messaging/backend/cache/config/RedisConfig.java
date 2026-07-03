package com.messaging.backend.cache.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.messaging.backend.cache.constants.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@EnableCaching
@Configuration
public class RedisConfig implements CachingConfigurer {

    private final ObjectMapper objectMapper;

    public RedisConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        log.info("Redis cache configuration initialized successfully.");
    }

    @SuppressWarnings("deprecation")
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper mapper = objectMapper.copy();
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return GenericJackson2JsonRedisSerializer.builder()
                .objectMapper(mapper)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();

        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(CacheConstants.TTL_DEFAULT)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .transactionAware()
                .enableStatistics()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler();
    }

    private static class CustomCacheErrorHandler implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.error("Redis Cache Get Error: key={} cache={}", key, cache.getName(), exception);
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.error("Redis Cache Put Error: key={} cache={}", key, cache.getName(), exception);
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.error("Redis Cache Evict Error: key={} cache={}", key, cache.getName(), exception);
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.error("Redis Cache Clear Error: cache={}", cache.getName(), exception);
        }
    }
}
