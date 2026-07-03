package com.messaging.backend.friendships.dto.response;

import com.messaging.backend.friendships.enums.FriendshipStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload containing safe friendship information.
 */
public record FriendshipResponse(
        UUID id,
        UUID requesterId,
        UUID addresseeId,
        FriendshipStatus status,
        Instant respondedAt,
        Instant blockedAt,
        Instant createdAt
) {
}
