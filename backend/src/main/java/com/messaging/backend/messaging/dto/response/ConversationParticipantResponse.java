package com.messaging.backend.messaging.dto.response;

import com.messaging.backend.messaging.enums.ParticipantRole;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for exposing a conversation participant.
 * Does not expose the underlying User entity or sensitive information.
 */
public record ConversationParticipantResponse(
        UUID userId,
        ParticipantRole role,
        ParticipantStatus status,
        Instant joinedAt
) {
}
