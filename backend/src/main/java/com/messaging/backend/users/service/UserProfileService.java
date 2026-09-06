package com.messaging.backend.users.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.cache.constants.CacheConstants;
import com.messaging.backend.common.dto.pagination.PaginationRequest;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.users.dto.request.UpdateUserProfileRequest;
import com.messaging.backend.users.entity.UserProfile;
import com.messaging.backend.users.repository.UserProfileRepository;
import com.messaging.backend.media.service.MediaService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service responsible for managing user profiles in MongoDB.
 */
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final MediaService mediaService;

    public UserProfileService(UserProfileRepository userProfileRepository, MediaService mediaService) {
        this.userProfileRepository = userProfileRepository;
        this.mediaService = mediaService;
    }

    /**
     * Creates a default public profile for a newly registered user.
     */
    @Transactional
    @CachePut(value = CacheConstants.USERS_CACHE, key = "'user:' + #user.id")
    public UserProfile createProfile(User user) {
        if (userProfileRepository.existsByUserId(user.getId())) {
            throw new ConflictException("Profile already exists for user ID: " + user.getId());
        }

        UserProfile profile = UserProfile.builder()
                .userId(user.getId())
                .displayName(user.getUsername())
                .verified(false)
                .build();

        return userProfileRepository.save(profile);
    }

    /**
     * Retrieves a user profile by the user's String ID.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.USERS_CACHE, key = "'user:' + #userId")
    public UserProfile getProfileByUserId(String userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user ID: " + userId));
    }

    /**
     * Checks if a profile exists for a specific user ID.
     */
    @Transactional(readOnly = true)
    public boolean existsByUserId(String userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    /**
     * Updates an existing user profile with provided editable fields.
     */
    @Transactional
    @CachePut(value = CacheConstants.USERS_CACHE, key = "'user:' + #userId")
    public UserProfile updateProfile(String userId, UpdateUserProfileRequest request) {
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

        return userProfileRepository.save(profile);
    }

    /**
     * Uploads an avatar image to Cloudinary and saves the URL on the user's profile.
     */
    @Transactional
    @CachePut(value = CacheConstants.USERS_CACHE, key = "'user:' + #userId")
    public UserProfile uploadAvatar(String userId, MultipartFile file) {
        UserProfile profile = getProfileByUserId(userId);
        String avatarUrl = mediaService.uploadAvatar(userId, file);
        profile.setAvatarUrl(avatarUrl);
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
     * Retrieves the profile of another user by their user ID.
     * All user profiles are fully visible to all authenticated users.
     */
    @Transactional(readOnly = true)
    public UserProfile getPublicProfile(String userId) {
        return getProfileByUserId(userId);
    }

    /**
     * Searches for user profiles by display name.
     */
    @Transactional(readOnly = true)
    public Page<UserProfile> searchPublicProfiles(
            String query, PaginationRequest paginationRequest) {
        
        String normalizedQuery = query == null ? "" : query.trim();
        org.springframework.data.domain.Pageable pageable = paginationRequest.toPageable();

        if (normalizedQuery.isEmpty()) {
            return userProfileRepository.findAll(pageable);
        }
        
        return userProfileRepository.findByDisplayNameContainingIgnoreCase(
                normalizedQuery, 
                pageable
        );
    }
}
