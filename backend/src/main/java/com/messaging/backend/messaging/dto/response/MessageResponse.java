package com.messaging.backend.messaging.dto.response;

import com.messaging.backend.messaging.enums.MessageStatus;
import com.messaging.backend.messaging.enums.MessageType;
import com.messaging.backend.media.dto.response.MediaAttachmentResponse;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO representing a chat message.
 */
@Builder
public record MessageResponse(
        String id,
        String conversationId,
        String senderId,
        String content,
        MessageType messageType,
        MessageStatus status,
        boolean edited,
        boolean deleted,
        Instant editedAt,
        Instant deletedAt,
        Instant createdAt,
        List<MediaAttachmentResponse> attachments
) {
}
