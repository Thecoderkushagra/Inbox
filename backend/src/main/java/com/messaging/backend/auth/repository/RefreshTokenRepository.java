package com.messaging.backend.auth.repository;

import com.messaging.backend.auth.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing RefreshToken document persistence in MongoDB.
 */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserId(String userId);

    void deleteAllByUserId(String userId);

    List<RefreshToken> findAllByRevokedFalse();

    List<RefreshToken> findAllByExpiresAtBefore(Instant instant);
}
