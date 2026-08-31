package com.messaging.backend.friendships.dto.response;

import com.messaging.backend.friendships.enums.FriendshipStatus;

import java.time.Instant;

public record FriendshipSocketResponse(
        String friendshipId,
        String requesterId,
        String addresseeId,
        FriendshipStatus status,
        Instant respondedAt,
        Instant blockedAt,
        Instant createdAt
) {}
