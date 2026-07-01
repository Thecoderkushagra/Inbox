package com.messaging.backend.messaging.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a group conversation.
 */
public record CreateGroupConversationRequest(
        @NotBlank(message = "Group name must not be blank")
        @Size(max = 100, message = "Group name must not exceed 100 characters")
        @NoHtml
        String name
) {
}
