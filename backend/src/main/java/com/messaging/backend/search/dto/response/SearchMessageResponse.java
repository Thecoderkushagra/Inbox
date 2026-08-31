package com.messaging.backend.search.dto.response;

import java.time.Instant;

public record SearchMessageResponse(
        String messageId,
        String conversationId,
        String senderId,
        String content,
        Instant createdAt,
        boolean edited
) {}
