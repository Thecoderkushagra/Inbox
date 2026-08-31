package com.messaging.backend.groups.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PromoteAdminRequest(
        @NotBlank(message = "User ID is required")
        String userId
) {
}
