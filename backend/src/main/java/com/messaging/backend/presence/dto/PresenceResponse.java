package com.messaging.backend.presence.dto;

import com.messaging.backend.presence.enums.PresenceStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing a user's online presence to external clients.
 * Exposes only safe fields.
 */
public record PresenceResponse(
        UUID userId,
        PresenceStatus status,
        Instant lastSeen
) {
}
