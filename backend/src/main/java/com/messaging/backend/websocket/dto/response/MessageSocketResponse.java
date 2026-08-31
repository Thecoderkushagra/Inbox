package com.messaging.backend.websocket.dto.response;

import com.messaging.backend.messaging.enums.MessageStatus;
import com.messaging.backend.messaging.enums.MessageType;
import java.time.Instant;

/**
 * Outbound DTO representing a Message sent via WebSocket STOMP with String IDs.
 */
public record MessageSocketResponse(
        String id,
        String conversationId,
        String senderId,
        String content,
        MessageType messageType,
        MessageStatus status,
        boolean edited,
        boolean deleted,
        Instant createdAt,
        Instant editedAt
) {
}
