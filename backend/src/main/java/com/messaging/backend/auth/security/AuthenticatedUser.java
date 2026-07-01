package com.messaging.backend.auth.security;

import com.messaging.backend.auth.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Immutable authenticated principal.
 *
 * <p>Purpose:
 * Represents the authenticated user within the Spring Security context without
 * holding onto unnecessary sensitive data (e.g. passwords).
 *
 * <p>Responsibilities:
 * Provides essential details like user ID, roles (authorities), and account status flags 
 * required by Spring Security for authorization checks.
 *
 * <p>Extension points:
 * None typically required. Any further complex checks should be done in custom 
 * authorization beans or services rather than bloating the principal.
 */
@Getter
@Builder
public class AuthenticatedUser implements UserDetails {

    private final UUID id;
    private final String username;
    private final String email;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String password;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final boolean accountNonExpired;
    private final boolean credentialsNonExpired;

    @Override
    public String getPassword() {
        return password;
    }
}
