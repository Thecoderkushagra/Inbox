package com.messaging.backend.users.repository;

import com.messaging.backend.users.entity.UserProfile;
import com.messaging.backend.users.enums.ProfileVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence layer for UserProfile MongoDB documents.
 */
@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

    Optional<UserProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Page<UserProfile> findByDisplayNameIgnoreCase(String displayName, Pageable pageable);

    boolean existsByDisplayNameIgnoreCase(String displayName);

    Page<UserProfile> findByProfileVisibility(ProfileVisibility profileVisibility, Pageable pageable);

    Page<UserProfile> findByProfileVisibilityAndDisplayNameContainingIgnoreCase(
            ProfileVisibility visibility, String displayName, Pageable pageable);
}
