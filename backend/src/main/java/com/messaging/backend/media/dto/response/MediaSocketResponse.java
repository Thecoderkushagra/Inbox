package com.messaging.backend.media.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MediaSocketResponse(
        UUID attachmentId,
        UUID messageId,
        UUID conversationId,
        UUID storageKey,
        String originalFilename,
        String contentType,
        String mediaType,
        Long fileSize,
        Instant createdAt
) {
}
