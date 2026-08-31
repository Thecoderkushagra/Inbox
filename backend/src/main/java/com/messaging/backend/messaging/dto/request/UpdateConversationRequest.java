package com.messaging.backend.messaging.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating conversation settings (like renaming a group).
 */
public record UpdateConversationRequest(
        @NotBlank(message = "Conversation name must not be blank")
        @Size(max = 100, message = "Conversation name must not exceed 100 characters")
        @NoHtml
        String name
) {
}
