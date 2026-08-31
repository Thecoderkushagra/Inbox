package com.messaging.backend.users.controller;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.auth.service.AuthService;
import com.messaging.backend.common.dto.pagination.PaginationRequest;
import com.messaging.backend.common.dto.response.PageResponse;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.users.dto.request.UpdateUserProfileRequest;
import com.messaging.backend.users.dto.response.UserProfileResponse;
import com.messaging.backend.users.entity.UserProfile;
import com.messaging.backend.users.mapper.UserProfileMapper;
import com.messaging.backend.users.service.UserProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing REST APIs for managing user profiles.
 */
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AuthService authService;
    private final UserProfileMapper userProfileMapper;

    public UserProfileController(UserProfileService userProfileService, 
                                 AuthService authService, 
                                 UserProfileMapper userProfileMapper) {
        this.userProfileService = userProfileService;
        this.authService = authService;
        this.userProfileMapper = userProfileMapper;
    }

    /**
     * Retrieves the currently authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        
        UserProfile profile = userProfileService.getProfileByUserId(authenticatedUser.getId());
        UserProfileResponse response = userProfileMapper.toResponse(profile);
        
        return ResponseEntity.ok(SuccessResponse.success("Profile retrieved successfully", response));
    }

    /**
     * Creates a profile for the currently authenticated user.
     */
    @PostMapping("/me/profile")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> createMyProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        
        User user = authService.getUserById(authenticatedUser.getId());
        UserProfile profile = userProfileService.createProfile(user);
        UserProfileResponse response = userProfileMapper.toResponse(profile);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Profile created successfully", response));
    }

    /**
     * Updates the currently authenticated user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        
        UserProfile profile = userProfileService.updateProfile(authenticatedUser.getId(), request);
        UserProfileResponse response = userProfileMapper.toResponse(profile);
        
        return ResponseEntity.ok(SuccessResponse.success("Profile updated successfully", response));
    }

    /**
     * Retrieves the public profile of another user by their String ID.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> getPublicProfile(
            @PathVariable String userId) {
        
        UserProfile profile = userProfileService.getPublicProfile(userId);
        UserProfileResponse response = userProfileMapper.toResponse(profile);
        
        return ResponseEntity.ok(SuccessResponse.success("Profile retrieved successfully", response));
    }

    /**
     * Searches for public user profiles by display name.
     */
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponse<UserProfileResponse>>> searchPublicProfiles(
            @RequestParam(required = false) @Size(max = 100) String query,
            @Valid PaginationRequest paginationRequest) {
        
        Page<UserProfile> profiles = userProfileService.searchPublicProfiles(query, paginationRequest);
        Page<UserProfileResponse> responsePage = profiles.map(userProfileMapper::toResponse);
        
        return ResponseEntity.ok(SuccessResponse.success("Profiles retrieved successfully", PageResponse.of(responsePage)));
    }
}
