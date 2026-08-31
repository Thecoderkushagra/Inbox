package com.messaging.backend.messaging.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for adding a new participant to a conversation.
 */
public record AddParticipantRequest(
        @NotBlank(message = "User ID must not be blank")
        String userId
) {
}
