package com.messaging.backend.messaging.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for creating a private (direct) conversation.
 */
public record CreatePrivateConversationRequest(
        @NotNull(message = "Recipient ID must not be null")
        UUID recipientId
) {
}
