package com.messaging.backend.auth.repository;

import com.messaging.backend.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing RefreshToken entity persistence.
 *
 * <p>Responsibility:
 * Handles the retrieval, deletion, and lookup of refresh tokens associated 
 * with long-lived user sessions.
 *
 * <p>Aggregate Root:
 * While User is the main aggregate root, RefreshToken acts as an aggregate 
 * for token lifecycle management (e.g., revoking all tokens for a compromised user).
 *
 * <p>Intended Future Usage:
 * Will be utilized by background scheduled jobs to purge expired tokens, 
 * and by authentication services during token rotation flows and logout events.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);

    List<RefreshToken> findAllByRevokedFalse();

    List<RefreshToken> findAllByExpiresAtBefore(Instant instant);
}
