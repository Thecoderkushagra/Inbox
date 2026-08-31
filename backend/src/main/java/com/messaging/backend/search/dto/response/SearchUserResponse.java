package com.messaging.backend.search.dto.response;

public record SearchUserResponse(
        String userId,
        String username,
        String displayName,
        String avatarUrl
) {}
