package com.messaging.backend.pubsub.dto;

import java.time.Instant;
import java.util.UUID;

public record RedisEvent(
        UUID eventId,
        String eventType,
        String sourceInstanceId,
        Object payload,
        Instant createdAt,
        java.util.Map<String, String> traceContext
) {
    public RedisEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (traceContext == null) {
            traceContext = new java.util.HashMap<>();
        }
    }

    public RedisEvent(UUID eventId, String eventType, String sourceInstanceId, Object payload, Instant createdAt) {
        this(eventId, eventType, sourceInstanceId, payload, createdAt, new java.util.HashMap<>());
    }
}
