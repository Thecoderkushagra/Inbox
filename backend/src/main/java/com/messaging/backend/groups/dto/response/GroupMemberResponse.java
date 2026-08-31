package com.messaging.backend.groups.dto.response;

import java.time.Instant;

public record GroupMemberResponse(
        String userId,
        String role,
        Instant joinedAt
) {
}
