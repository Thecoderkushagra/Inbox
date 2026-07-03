package com.messaging.backend.groups.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GroupSocketResponse(
        UUID groupId,
        String name,
        String description,
        String conversationType,
        Instant updatedAt
) {
}
