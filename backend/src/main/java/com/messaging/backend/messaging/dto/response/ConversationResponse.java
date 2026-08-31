package com.messaging.backend.messaging.dto.response;

import com.messaging.backend.messaging.enums.ConversationType;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for exposing conversation metadata.
 */
@Builder
public record ConversationResponse(
        String id,
        ConversationType type,
        String title,
        String description,
        boolean archived,
        Instant lastMessageAt,
        Instant createdAt,
        List<ConversationParticipantResponse> participants
) {
}
