package com.messaging.backend.groups.dto.response;

import java.time.Instant;

public record GroupResponse(
        String groupId,
        String title,
        String description,
        String type,
        Instant createdAt
) {
}
