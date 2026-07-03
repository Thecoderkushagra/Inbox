package com.messaging.backend.friendships.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request payload for sending a friend request.
 */
public record SendFriendRequest(
        @NotNull(message = "Addressee ID is required")
        UUID addresseeId
) {
}
