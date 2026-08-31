package com.messaging.backend.auth.entity;

import com.messaging.backend.auth.enums.RoleType;
import com.messaging.backend.auth.enums.UserStatus;
import com.messaging.backend.common.entity.BaseDocument;
import com.messaging.backend.users.entity.UserProfile;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
 * or status changes.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "users")
public class User extends BaseDocument {

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;

    private UserStatus status = UserStatus.ACTIVE;

    private boolean emailVerified = true;

    private Set<RoleType> roles = new HashSet<>();

    private UserProfile profile;

    @Builder
    public User(String email, String username, String passwordHash, UserStatus status, boolean emailVerified) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.emailVerified = emailVerified;
        if (this.roles == null || this.roles.isEmpty()) {
            this.roles = new HashSet<>();
            this.roles.add(RoleType.USER);
        }
    }

    public void addRole(RoleType role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public void removeRole(RoleType role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    public void markAsVerified() {
        this.status = UserStatus.ACTIVE;
        this.emailVerified = true;
    }
}
