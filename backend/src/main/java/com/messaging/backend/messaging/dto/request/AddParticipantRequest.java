package com.messaging.backend.messaging.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for adding a new participant to a conversation.
 */
public record AddParticipantRequest(
        @NotNull(message = "User ID must not be null")
        UUID userId
) {
}
