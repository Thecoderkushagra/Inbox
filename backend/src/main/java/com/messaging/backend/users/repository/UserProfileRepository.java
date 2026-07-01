package com.messaging.backend.users.repository;

import com.messaging.backend.users.entity.UserProfile;
import com.messaging.backend.users.enums.ProfileVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence layer for UserProfile entities.
 *
 * <p>Purpose:
 * Provides CRUD operations and derived query methods for accessing 
 * user profiles without exposing database queries to the business layer.
 * All methods rely on Spring Data JPA derived query generation.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Retrieves a user's profile by their authentication user ID.
     *
     * @param userId the UUID of the authentication User
     * @return an Optional containing the UserProfile if found
     */
    Optional<UserProfile> findByUserId(UUID userId);

    /**
     * Checks if a profile exists for a specific authentication user ID.
     *
     * @param userId the UUID of the authentication User
     * @return true if the profile exists, false otherwise
     */
    boolean existsByUserId(UUID userId);

    /**
     * Searches for user profiles matching an exact display name, ignoring case differences.
     * Returns a pageable list since display names are not unique across the platform.
     *
     * @param displayName the display name to search for
     * @param pageable pagination parameters
     * @return a page of matching user profiles
     */
    Page<UserProfile> findByDisplayNameIgnoreCase(String displayName, Pageable pageable);

    /**
     * Checks if any profile uses the specified display name, ignoring case differences.
     *
     * @param displayName the display name to check
     * @return true if at least one profile uses the name, false otherwise
     */
    boolean existsByDisplayNameIgnoreCase(String displayName);

    /**
     * Retrieves a paginated list of user profiles filtered by their exact visibility setting.
     * Useful for fetching all public profiles or isolating private ones.
     *
     * @param profileVisibility the visibility level (e.g., PUBLIC)
     * @param pageable pagination parameters
     * @return a page of user profiles matching the given visibility
     */
    Page<UserProfile> findByProfileVisibility(ProfileVisibility profileVisibility, Pageable pageable);

    /**
     * Searches for user profiles matching a partial display name, constrained by visibility.
     * Used for public directory searches where private profiles must be excluded.
     *
     * @param visibility the visibility level required (e.g., PUBLIC)
     * @param displayName the partial display name to search for (case-insensitive)
     * @param pageable pagination parameters
     * @return a page of matching user profiles
     */
    Page<UserProfile> findByProfileVisibilityAndDisplayNameContainingIgnoreCase(
            ProfileVisibility visibility, String displayName, Pageable pageable);
}
