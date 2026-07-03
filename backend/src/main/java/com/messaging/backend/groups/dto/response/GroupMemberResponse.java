package com.messaging.backend.groups.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GroupMemberResponse(
        UUID userId,
        String role,
        Instant joinedAt
) {
}
