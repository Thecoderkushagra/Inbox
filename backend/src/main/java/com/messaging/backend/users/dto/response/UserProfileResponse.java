package com.messaging.backend.users.dto.response;

import lombok.Builder;

import java.time.LocalDate;

/**
 * Data Transfer Object for transferring public user profile information.
 */
@Builder
public record UserProfileResponse(
        String id,
        String userId,
        String displayName,
        String bio,
        String avatarUrl,
        String bannerUrl,
        String location,
        String website,
        LocalDate birthDate,
        String gender,
        boolean verified
) {
}
