package com.messaging.backend.media.dto.response;

import java.time.Instant;

public record MediaAttachmentResponse(
        String attachmentId,
        String storageKey,
        String url,
        String originalFilename,
        String contentType,
        String mediaType,
        Long fileSize,
        String checksum,
        Instant createdAt
) {
}
