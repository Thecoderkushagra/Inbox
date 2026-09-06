package com.messaging.backend.users.repository;

import com.messaging.backend.users.entity.UserProfile;
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

    Page<UserProfile> findByDisplayNameContainingIgnoreCase(String displayName, Pageable pageable);

    boolean existsByDisplayNameIgnoreCase(String displayName);
}
