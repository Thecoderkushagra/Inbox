package com.messaging.backend.groups.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateGroupDescriptionRequest(
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
