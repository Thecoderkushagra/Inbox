package com.messaging.backend.groups.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GroupResponse(
        UUID groupId,
        String title,
        String description,
        String type,
        Instant createdAt
) {
}
