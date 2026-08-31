package com.messaging.backend.presence.dto;

import com.messaging.backend.presence.enums.PresenceStatus;

import java.time.Instant;

/**
 * DTO representing a user's online presence to external clients.
 */
public record PresenceResponse(
        String userId,
        PresenceStatus status,
        Instant lastSeen
) {
}
