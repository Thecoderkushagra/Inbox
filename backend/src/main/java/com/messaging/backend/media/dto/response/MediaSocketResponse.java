package com.messaging.backend.media.dto.response;

import java.time.Instant;

public record MediaSocketResponse(
        String attachmentId,
        String messageId,
        String conversationId,
        String storageKey,
        String url,
        String originalFilename,
        String contentType,
        String mediaType,
        Long fileSize,
        Instant createdAt
) {
}
