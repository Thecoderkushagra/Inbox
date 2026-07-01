package com.messaging.backend.auth.repository;

import com.messaging.backend.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing EmailVerificationToken entity persistence.
 *
 * <p>Responsibility:
 * Supports querying email verification tokens by hash, discovering pending 
 * requests for a user, and cleaning up expired tokens.
 *
 * <p>Aggregate Root:
 * Functions under the User aggregate, but acts as its own entity for the 
 * purpose of temporal token verification.
 *
 * <p>Intended Future Usage:
 * Designed for use in the registration completion flow and email update processes.
 * Will also support maintenance jobs querying for unused and expired tokens.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    List<EmailVerificationToken> findAllByUserId(UUID userId);

    List<EmailVerificationToken> findAllByUsedFalse();

    List<EmailVerificationToken> findAllByExpiresAtBefore(Instant instant);
}
