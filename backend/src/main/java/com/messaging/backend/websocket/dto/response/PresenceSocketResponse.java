package com.messaging.backend.websocket.dto.response;

import com.messaging.backend.presence.enums.PresenceStatus;

import java.time.Instant;

/**
 * Outbound WebSocket DTO for broadcasting presence updates with String userId.
 */
public record PresenceSocketResponse(
        String userId,
        PresenceStatus status,
        Instant lastSeen
) {
}
