package com.messaging.backend.auth.security;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.UserStatus;
import com.messaging.backend.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 *
 * <p>Purpose:
 * Bridges Spring Security's authentication needs with the application's User entity persistence.
 *
 * <p>Responsibilities:
 * Locates the user by UUID (or username/email) and maps them to an immutable 
 * {@link AuthenticatedUser} suitable for the security context.
 *
 * <p>Extension points:
 * Can be extended to load users by email/username during future login flows.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their UUID. Used primarily by the JWT authentication filter.
     *
     * @param id The UUID of the user as a string
     * @return AuthenticatedUser UserDetails instance
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        UUID userId;
        try {
            userId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid user ID format");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return buildAuthenticatedUser(user);
    }

    /**
     * Loads a user by their email or username. 
     * Useful for future login processes where the client provides an identifier.
     *
     * @param identifier Email or username
     * @return AuthenticatedUser UserDetails instance
     * @throws UsernameNotFoundException if user is not found
     */
    public UserDetails loadUserByIdentifier(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier)));

        return buildAuthenticatedUser(user);
    }

    private AuthenticatedUser buildAuthenticatedUser(User user) {
        boolean isEnabled = user.getStatus() == UserStatus.ACTIVE || user.getStatus() == UserStatus.PENDING_VERIFICATION;
        boolean isAccountNonLocked = user.getStatus() != UserStatus.LOCKED;
        
        return AuthenticatedUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .status(user.getStatus())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                        .collect(Collectors.toList()))
                .enabled(isEnabled)
                .accountNonLocked(isAccountNonLocked)
                .accountNonExpired(user.getStatus() != UserStatus.DELETED)
                .credentialsNonExpired(true)
                .build();
    }
}
