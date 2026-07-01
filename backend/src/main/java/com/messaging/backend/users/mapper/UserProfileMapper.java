package com.messaging.backend.users.mapper;

import com.messaging.backend.users.dto.response.UserProfileResponse;
import com.messaging.backend.users.entity.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Mapper for UserProfile entities.
 *
 * <p>Purpose:
 * Isolates the logic of translating persistence entities into safe presentation DTOs.
 */
@Component
public class UserProfileMapper {

    /**
     * Converts a UserProfile entity to its public representation.
     *
     * @param profile the UserProfile entity
     * @return the UserProfileResponse DTO
     */
    public UserProfileResponse toResponse(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .displayName(profile.getDisplayName())
                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .bannerUrl(profile.getBannerUrl())
                .location(profile.getLocation())
                .website(profile.getWebsite())
                .birthDate(profile.getBirthDate())
                .gender(profile.getGender())
                .profileVisibility(profile.getProfileVisibility())
                .verified(profile.isVerified())
                .build();
    }
}
