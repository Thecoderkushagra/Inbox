package com.messaging.backend.users.dto.response;

import com.messaging.backend.users.enums.ProfileVisibility;
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
        ProfileVisibility profileVisibility,
        boolean verified
) {
}
