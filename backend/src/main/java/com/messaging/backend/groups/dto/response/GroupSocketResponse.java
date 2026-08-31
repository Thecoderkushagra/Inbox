package com.messaging.backend.groups.dto.response;

import java.time.Instant;

public record GroupSocketResponse(
        String groupId,
        String name,
        String description,
        String conversationType,
        Instant updatedAt
) {
}
