package com.messaging.backend.websocket.dto.response;

import com.messaging.backend.messaging.enums.MessageStatus;
import com.messaging.backend.messaging.enums.MessageType;
import java.time.Instant;
import java.util.UUID;

/**
 * Outbound DTO representing a Message sent via WebSocket STOMP.
 * Strictly flattens relational entities and excludes sensitive metadata.
 */
public record MessageSocketResponse(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String content,
        MessageType messageType,
        MessageStatus status,
        boolean edited,
        boolean deleted,
        Instant createdAt,
        Instant editedAt
) {
}
