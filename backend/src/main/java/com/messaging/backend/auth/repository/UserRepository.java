package com.messaging.backend.auth.repository;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing User entity persistence.
 *
 * <p>Responsibility:
 * Provides CRUD operations and derived queries for User entities, such as lookup 
 * by email/username and querying by account status.
 *
 * <p>Aggregate Root:
 * User serves as the aggregate root for identity and credential management.
 *
 * <p>Intended Future Usage:
 * As the application grows, this repository will support queries for user searching,
 * filtering active vs inactive accounts, and enforcing uniqueness constraints during registration.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findAllByStatus(UserStatus status);

    Page<User> findAllByStatus(UserStatus status, Pageable pageable);

    /**
     * Searches users by username, display name, or email, filtering by status.
     * Required for User Search to exclude inactive/deleted users and support partial matching.
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT u FROM User u LEFT JOIN u.profile p WHERE u.status = :status AND u.id != :currentUserId AND " +
        "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
        "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
        "LOWER(p.displayName) LIKE LOWER(CONCAT('%', :query, '%')))"
    )
    Page<User> searchUsersByKeywordAndStatus(
            @org.springframework.data.repository.query.Param("currentUserId") UUID currentUserId,
            @org.springframework.data.repository.query.Param("query") String query, 
            @org.springframework.data.repository.query.Param("status") UserStatus status, 
            Pageable pageable);
}
