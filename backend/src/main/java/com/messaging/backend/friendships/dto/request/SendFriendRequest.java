package com.messaging.backend.friendships.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SendFriendRequest(
        @NotBlank(message = "Addressee ID must not be blank")
        String addresseeId
) {}
