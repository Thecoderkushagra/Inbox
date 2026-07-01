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
}
