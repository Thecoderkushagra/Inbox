package com.messaging.backend.websocket.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Request payload for sending a message via WebSocket STOMP.
 * Excludes senderId and timestamps as they are strictly resolved on the server side.
 */
public record SendMessageSocketRequest(
        @NotNull(message = "Conversation ID is required")
        UUID conversationId,

        @NotBlank(message = "Message content must not be blank")
        @Size(max = 5000, message = "Message content must not exceed 5000 characters")
        @NoHtml
        String content
) {
}
