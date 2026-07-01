package com.messaging.backend.users.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.common.dto.pagination.PaginationRequest;
import org.springframework.data.domain.Page;
import com.messaging.backend.users.dto.request.UpdateUserProfileRequest;
import com.messaging.backend.users.entity.UserProfile;
import com.messaging.backend.users.enums.ProfileVisibility;
import com.messaging.backend.users.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for managing user profiles.
 *
 * <p>Purpose:
 * Encapsulates the business logic for creating and retrieving UserProfile entities.
 * Ensures data integrity and enforces profile creation rules.
 */
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Creates a default public profile for a newly registered user.
     *
     * @param user the authentication user
     * @return the created and persisted UserProfile
     * @throws ConflictException if the user already has a profile
     */
    @Transactional
    public UserProfile createProfile(User user) {
        if (userProfileRepository.existsByUserId(user.getId())) {
            throw new ConflictException("Profile already exists for user ID: " + user.getId());
        }

        UserProfile profile = UserProfile.builder()
                .user(user)
                .displayName(user.getUsername())
                .profileVisibility(ProfileVisibility.PUBLIC)
                .verified(false)
                .build();

        return userProfileRepository.save(profile);
    }

    /**
     * Retrieves a user profile by the user's UUID.
     *
     * @param userId the authentication user ID
     * @return the UserProfile
     * @throws ResourceNotFoundException if no profile exists for the user ID
     */
    @Transactional(readOnly = true)
    public UserProfile getProfileByUserId(UUID userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));
    }

    /**
     * Checks if a profile exists for a specific user ID.
     *
     * @param userId the authentication user ID
     * @return true if the profile exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    /**
     * Updates an existing user profile with provided editable fields.
     *
     * @param userId the authentication user ID
     * @param request the update payload
     * @return the updated UserProfile
     * @throws ResourceNotFoundException if the profile does not exist
     * @throws BadRequestException if the display name is invalid after trimming
     */
    @Transactional
    public UserProfile updateProfile(UUID userId, UpdateUserProfileRequest request) {
        UserProfile profile = getProfileByUserId(userId);

        if (request.displayName() != null) {
            String trimmedName = request.displayName().trim();
            if (trimmedName.isEmpty()) {
                throw new BadRequestException("Display name cannot be blank");
            }
            profile.setDisplayName(trimmedName);
        }

        if (request.bio() != null) {
            profile.setBio(trimToNull(request.bio()));
        }

        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(trimToNull(request.avatarUrl()));
        }

        if (request.bannerUrl() != null) {
            profile.setBannerUrl(trimToNull(request.bannerUrl()));
        }

        if (request.location() != null) {
            profile.setLocation(trimToNull(request.location()));
        }

        if (request.website() != null) {
            profile.setWebsite(trimToNull(request.website()));
        }

        if (request.gender() != null) {
            profile.setGender(trimToNull(request.gender()));
        }

        if (request.birthDate() != null) {
            profile.setBirthDate(request.birthDate());
        }

        if (request.profileVisibility() != null) {
            profile.setProfileVisibility(request.profileVisibility());
        }

        return userProfileRepository.save(profile);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Retrieves a public profile for another user.
     * Respects visibility rules: only PUBLIC profiles are accessible.
     *
     * @param userId the authentication user ID of the target profile
     * @return the UserProfile if it is public
     * @throws ResourceNotFoundException if the profile does not exist
     * @throws ForbiddenException if the profile is PRIVATE or FRIENDS_ONLY
     */
    @Transactional(readOnly = true)
    public UserProfile getPublicProfile(UUID userId) {
        UserProfile profile = getProfileByUserId(userId);

        if (profile.getProfileVisibility() != ProfileVisibility.PUBLIC) {
            throw new ForbiddenException("This profile is not public");
        }

        return profile;
    }
    /**
     * Searches for public user profiles by display name.
     * Only returns profiles with PUBLIC visibility.
     *
     * @param query the partial display name to search for
     * @param paginationRequest pagination and sorting parameters
     * @return a paginated list of matching public profiles
     */
    @Transactional(readOnly = true)
    public Page<UserProfile> searchPublicProfiles(
            String query, PaginationRequest paginationRequest) {
        
        String normalizedQuery = query == null ? "" : query.trim();
        
        return userProfileRepository.findByProfileVisibilityAndDisplayNameContainingIgnoreCase(
                ProfileVisibility.PUBLIC, 
                normalizedQuery, 
                paginationRequest.toPageable()
        );
    }
}
