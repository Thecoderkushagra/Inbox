package com.messaging.backend.media.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MediaAttachmentResponse(
        UUID attachmentId,
        UUID storageKey,
        String originalFilename,
        String contentType,
        String mediaType,
        Long fileSize,
        String checksum,
        Instant createdAt
) {
}
