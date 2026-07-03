package com.messaging.backend.search.dto.response;

import com.messaging.backend.messaging.enums.ConversationType;

import java.time.Instant;
import java.util.UUID;

public record SearchConversationResponse(
        UUID conversationId,
        String title,
        String description,
        ConversationType conversationType,
        Instant updatedAt
) {}
