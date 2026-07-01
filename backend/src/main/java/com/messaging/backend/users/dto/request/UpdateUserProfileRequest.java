package com.messaging.backend.users.dto.request;

import com.messaging.backend.common.validation.annotation.NoHtml;
import com.messaging.backend.users.enums.ProfileVisibility;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Data Transfer Object for updating a user's public profile.
 *
 * <p>Purpose:
 * Accepts incoming JSON payloads for profile edits, applying strict Bean Validation 
 * to ensure database constraints and security (e.g. XSS prevention via NoHtml) are respected.
 */
public record UpdateUserProfileRequest(

        @Size(max = 64)
        @NoHtml
        String displayName,

        @Size(max = 500)
        @NoHtml
        String bio,

        @Size(max = 1024)
        String avatarUrl,

        @Size(max = 1024)
        String bannerUrl,

        @Size(max = 128)
        @NoHtml
        String location,

        @Size(max = 255)
        String website,

        @Past
        LocalDate birthDate,

        @Size(max = 32)
        @NoHtml
        String gender,

        ProfileVisibility profileVisibility
) {
}
