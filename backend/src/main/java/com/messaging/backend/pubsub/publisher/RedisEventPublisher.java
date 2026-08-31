package com.messaging.backend.pubsub.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messaging.backend.pubsub.dto.RedisEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.ObjectProvider;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;

@Service
public class RedisEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisEventPublisher.class);
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final String instanceId = UUID.randomUUID().toString();
    private final ObjectProvider<Tracer> tracerProvider;
    private final ObjectProvider<Propagator> propagatorProvider;

    public RedisEventPublisher(StringRedisTemplate stringRedisTemplate,
                               ObjectMapper objectMapper,
                               ObjectProvider<Tracer> tracerProvider,
                               ObjectProvider<Propagator> propagatorProvider) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.tracerProvider = tracerProvider;
        this.propagatorProvider = propagatorProvider;
    }

    public void publish(String channel, RedisEvent event) {
        if (event == null || channel == null) {
            return;
        }

        Map<String, String> traceContext = new HashMap<>();
        if (event.traceContext() != null) {
            traceContext.putAll(event.traceContext());
        }
        
        Tracer tracer = tracerProvider.getIfAvailable();
        Propagator propagator = propagatorProvider.getIfAvailable();
        
        if (tracer != null && propagator != null && tracer.currentSpan() != null) {
            propagator.inject(tracer.currentSpan().context(), traceContext, Map::put);
        }

        // Ensure sourceInstanceId is populated if not provided
        RedisEvent eventToPublish = new RedisEvent(
                event.eventId(),
                event.eventType(),
                event.sourceInstanceId() != null ? event.sourceInstanceId() : this.instanceId,
                event.payload(),
                event.createdAt(),
                traceContext
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
