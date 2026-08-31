package com.messaging.backend.search.dto.response;

import com.messaging.backend.messaging.enums.ConversationType;

import java.time.Instant;

public record SearchConversationResponse(
        String conversationId,
        String title,
        String description,
        ConversationType conversationType,
        Instant updatedAt
) {}
