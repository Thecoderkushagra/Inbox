package com.messaging.backend.pubsub.publisher;

import com.messaging.backend.pubsub.dto.RedisEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private final String instanceId = UUID.randomUUID().toString();
    private final ObjectProvider<Tracer> tracerProvider;
    private final ObjectProvider<Propagator> propagatorProvider;

    public RedisEventPublisher(RedisTemplate<String, Object> redisTemplate,
                               ObjectProvider<Tracer> tracerProvider,
                               ObjectProvider<Propagator> propagatorProvider) {
        this.redisTemplate = redisTemplate;
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
            redisTemplate.convertAndSend(channel, eventToPublish);
        } catch (Exception e) {
            log.error("Failed to publish event {} to channel {}", eventToPublish.eventType(), channel, e);
        }
    }
    
    public String getInstanceId() {
        return instanceId;
    }
}
