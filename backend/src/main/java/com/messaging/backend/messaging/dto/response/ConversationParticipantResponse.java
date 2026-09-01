package com.messaging.backend.messaging.dto.response;

import com.messaging.backend.messaging.enums.ParticipantRole;
import com.messaging.backend.messaging.enums.ParticipantStatus;

import java.time.Instant;

/**
 * Response DTO for exposing a conversation participant.
 */
public record ConversationParticipantResponse(
        String userId,
        String username,
        String displayName,
        String avatarUrl,
        ParticipantRole role,
        ParticipantStatus status,
        Instant joinedAt
) {
    public ConversationParticipantResponse(String userId, ParticipantRole role, ParticipantStatus status, Instant joinedAt) {
        this(userId, null, null, null, role, status, joinedAt);
    }
}
