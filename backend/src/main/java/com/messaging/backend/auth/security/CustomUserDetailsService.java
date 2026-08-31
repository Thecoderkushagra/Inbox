package com.messaging.backend.auth.security;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.UserStatus;
import com.messaging.backend.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their String ID.
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return buildAuthenticatedUser(user);
    }

    /**
     * Loads a user by their email or username.
     */
    public UserDetails loadUserByIdentifier(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier)));

        return buildAuthenticatedUser(user);
    }

    private AuthenticatedUser buildAuthenticatedUser(User user) {
        boolean isEnabled = user.getStatus() == UserStatus.ACTIVE;
        boolean isAccountNonLocked = user.getStatus() != UserStatus.LOCKED;
        
        return AuthenticatedUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .status(user.getStatus())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toList()))
                .enabled(isEnabled)
                .accountNonLocked(isAccountNonLocked)
                .accountNonExpired(user.getStatus() != UserStatus.DELETED)
                .credentialsNonExpired(true)
                .build();
    }
}
