package com.messaging.backend.messaging.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating (editing) a message.
 */
public record UpdateMessageRequest(
        @NotBlank(message = "Content must not be blank")
        @Size(max = 5000, message = "Content must not exceed 5000 characters")
        @NoHtml
        String content
) {
}
