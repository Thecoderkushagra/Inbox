package com.messaging.backend.search.dto.response;

import java.util.UUID;

public record SearchUserResponse(
        UUID userId,
        String username,
        String displayName,
        String avatarUrl
) {}
