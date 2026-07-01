package com.messaging.backend.auth.entity;

import com.messaging.backend.auth.enums.UserStatus;
import com.messaging.backend.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user within the authentication domain.
 *
 * <p>Purpose:
 * Stores core user identity and credentials. Responsible for linking a user 
 * to their roles and tracking their account status.
 *
 * <p>Lifecycle:
 * Created during registration. Modified during profile updates, role assignments, 
 * or status changes (e.g., verification, locking). Soft deletion might be managed 
 * via the status field.
 *
 * <p>Future extension points:
 * Could be extended with fields for multi-factor authentication (MFA) secrets,
 * failed login attempt counters, or password reset tokens.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
    },
    indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_status", columnList = "status")
    }
)
public class User extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EmailVerificationToken> emailVerificationTokens = new HashSet<>();

    @Builder
    public User(String email, String username, String passwordHash, UserStatus status, boolean emailVerified) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = status;
        this.emailVerified = emailVerified;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public void markAsVerified() {
        this.status = UserStatus.ACTIVE;
        this.emailVerified = true;
    }
}
