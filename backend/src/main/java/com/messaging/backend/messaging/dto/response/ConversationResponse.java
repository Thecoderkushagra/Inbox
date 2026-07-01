package com.messaging.backend.messaging.dto.response;

import com.messaging.backend.messaging.enums.ConversationType;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for exposing basic conversation metadata.
 * Does not expose underlying entities.
 */
public record ConversationResponse(
        UUID id,
        ConversationType type,
        String name,
        boolean active,
        Instant createdAt
) {
}
