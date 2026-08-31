package com.messaging.backend.websocket.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for sending a message via WebSocket STOMP.
 */
public record SendMessageSocketRequest(
        @NotBlank(message = "Conversation ID is required")
        String conversationId,

        @NotBlank(message = "Message content must not be blank")
        @Size(max = 5000, message = "Message content must not exceed 5000 characters")
        @NoHtml
        String content
) {
}
