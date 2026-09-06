package com.messaging.backend.pubsub.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messaging.backend.pubsub.dto.RedisEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class RedisEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisEventPublisher.class);
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final String instanceId = UUID.randomUUID().toString();

    @Value("${app.redis.pubsub.enabled:false}")
    private boolean pubSubEnabled;

    public RedisEventPublisher(StringRedisTemplate stringRedisTemplate,
                               ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String channel, RedisEvent event) {
        if (!pubSubEnabled) {
            log.trace("Redis Pub/Sub is disabled (single-instance deployment). Skipping publish to {}", channel);
            return;
        }

        if (event == null || channel == null) {
            return;
        }

        // Ensure sourceInstanceId is populated if not provided
        RedisEvent eventToPublish = new RedisEvent(
                event.eventId(),
                event.eventType(),
                event.sourceInstanceId() != null ? event.sourceInstanceId() : this.instanceId,
                event.payload(),
                event.createdAt(),
                event.traceContext() != null ? event.traceContext() : Collections.emptyMap()
        );

        log.debug("Publishing event {} to channel {}", eventToPublish.eventType(), channel);
        
        try {
            String jsonPayload = objectMapper.writeValueAsString(eventToPublish);
            stringRedisTemplate.convertAndSend(channel, jsonPayload);
        } catch (Exception e) {
            log.error("Failed to publish event {} to channel {}", eventToPublish.eventType(), channel, e);
        }
    }
    
    public String getInstanceId() {
        return instanceId;
    }
}
