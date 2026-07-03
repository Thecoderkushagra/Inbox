package com.messaging.backend.search.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SearchMessageResponse(
        UUID messageId,
        UUID conversationId,
        UUID senderId,
        String content,
        Instant createdAt,
        boolean edited
) {}
