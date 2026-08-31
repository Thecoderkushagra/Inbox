package com.messaging.backend.auth.entity;

import com.messaging.backend.common.entity.BaseDocument;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a refresh token stored in MongoDB.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "refresh_tokens")
public class RefreshToken extends BaseDocument {

    @Indexed(unique = true)
    private String tokenHash;

    @Indexed(expireAfter = "0s")
    private Instant expiresAt;

    private boolean revoked;

    @Indexed
    private String userId;

    private Instant revokedAt;

    @Builder
    public RefreshToken(String tokenHash, Instant expiresAt, boolean revoked, String userId) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.userId = userId;
    }

    public void revoke(Instant time) {
        this.revoked = true;
        this.revokedAt = time;
    }
}
