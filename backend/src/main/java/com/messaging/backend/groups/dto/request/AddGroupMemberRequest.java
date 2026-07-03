package com.messaging.backend.groups.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddGroupMemberRequest(
        @NotNull(message = "User ID is required")
        UUID userId
) {
}
