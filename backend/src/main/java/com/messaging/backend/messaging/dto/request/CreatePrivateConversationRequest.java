package com.messaging.backend.messaging.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a private (direct) conversation.
 */
public record CreatePrivateConversationRequest(
        @NotBlank(message = "Recipient ID must not be blank")
        String recipientId
) {
}
