package com.messaging.backend.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an email verification token for proving account ownership.
 *
 * <p>Purpose:
 * Stores hashed verification tokens used to securely confirm a user's email address 
 * without storing the plaintext token in the database.
 *
 * <p>Lifecycle:
 * Created upon user registration or email change request. Marked as used when the 
 * user clicks the verification link. Expired tokens can be safely purged.
 *
 * <p>Future extension points:
 * Additional token types could be supported (e.g., password reset, 2FA setup) 
 * by adding a TokenType enum, or extending this to a generic VerificationToken entity.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "email_verification_tokens",
    indexes = {
        @Index(name = "idx_email_token_hash", columnList = "token_hash"),
        @Index(name = "idx_email_token_expires", columnList = "expires_at")
    }
)
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Builder
    public EmailVerificationToken(String tokenHash, Instant expiresAt, boolean used, User user) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.used = used;
        this.user = user;
    }

    public void markAsUsed(Instant time) {
        this.used = true;
        this.usedAt = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailVerificationToken that)) return false;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
