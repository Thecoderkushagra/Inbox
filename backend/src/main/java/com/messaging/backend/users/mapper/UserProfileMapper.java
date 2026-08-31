package com.messaging.backend.users.mapper;

import com.messaging.backend.users.dto.response.UserProfileResponse;
import com.messaging.backend.users.entity.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Mapper for UserProfile MongoDB documents.
 */
@Component
public class UserProfileMapper {

    /**
     * Converts a UserProfile document to its public representation.
     */
    public UserProfileResponse toResponse(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
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
