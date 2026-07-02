package com.messaging.backend.websocket.dto.response;

import com.messaging.backend.presence.enums.PresenceStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound WebSocket DTO for broadcasting presence updates.
 * Exposes only safe fields.
 */
public record PresenceSocketResponse(
        UUID userId,
        PresenceStatus status,
        Instant lastSeen
) {
}
