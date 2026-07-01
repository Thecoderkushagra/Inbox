package com.messaging.backend.users.dto.response;

import com.messaging.backend.users.enums.ProfileVisibility;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object for transferring public user profile information.
 *
 * <p>Purpose:
 * Safely exposes a user's profile data to clients. Ensures sensitive fields 
 * like email, password hash, and authorities are never leaked.
 */
@Builder
public record UserProfileResponse(
        UUID id,
        UUID userId,
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
