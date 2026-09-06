package com.messaging.backend.auth.repository;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User MongoDB document persistence.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findAllByStatus(UserStatus status);

    Page<User> findAllByStatus(UserStatus status, Pageable pageable);

    /**
     * Searches users by username, display name, or email, filtering by status.
     */
    @Query("{'status': ?2, '_id': {$ne: ?0}, $or: [{'username': {$regex: ?1, $options: 'i'}}, {'email': {$regex: ?1, $options: 'i'}}, {'profile.displayName': {$regex: ?1, $options: 'i'}}]}")
    Page<User> searchUsersByKeywordAndStatus(String currentUserId, String query, UserStatus status, Pageable pageable);

    /**
     * Retrieves all users with a specific status except the given user ID.
     */
    @Query("{'status': ?1, '_id': {$ne: ?0}}")
    Page<User> findAllByStatusAndIdNot(String currentUserId, UserStatus status, Pageable pageable);
}
