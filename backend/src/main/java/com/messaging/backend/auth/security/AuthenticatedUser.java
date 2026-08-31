package com.messaging.backend.auth.security;

import com.messaging.backend.auth.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Immutable authenticated principal with String ID.
 */
@Getter
@Builder
public class AuthenticatedUser implements UserDetails {

    private final String id;
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
