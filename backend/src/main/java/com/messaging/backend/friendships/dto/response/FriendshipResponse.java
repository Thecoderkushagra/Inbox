package com.messaging.backend.friendships.dto.response;

import com.messaging.backend.friendships.enums.FriendshipStatus;

import java.time.Instant;

public record FriendshipResponse(
        String id,
        String requesterId,
        String addresseeId,
        FriendshipStatus status,
        Instant respondedAt,
        Instant blockedAt,
        Instant createdAt
) {}
