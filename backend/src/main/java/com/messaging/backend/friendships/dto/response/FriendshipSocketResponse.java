package com.messaging.backend.friendships.dto.response;

import com.messaging.backend.friendships.enums.FriendshipStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound WebSocket DTO for broadcasting friendship updates.
 */
public record FriendshipSocketResponse(
        UUID friendshipId,
        UUID requesterId,
        UUID addresseeId,
        FriendshipStatus status,
        Instant respondedAt,
        Instant blockedAt,
        Instant createdAt
) {
}
