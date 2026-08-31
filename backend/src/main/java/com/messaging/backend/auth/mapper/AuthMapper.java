package com.messaging.backend.auth.mapper;

import com.messaging.backend.auth.dto.response.LoginResponse;
import com.messaging.backend.auth.dto.response.RefreshTokenResponse;
import com.messaging.backend.auth.dto.response.RegisterResponse;
import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.UserStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Authentication related DTOs and MongoDB User documents.
 */
@Component
public class AuthMapper {

    /**
     * Converts a User document into a RegisterResponse.
     */
    public RegisterResponse toRegisterResponse(User user) {
        if (user == null) {
            return null;
        }

        return new RegisterResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getStatus(),
            user.getCreatedAt(),
            false // No email verification required on $0 setup
        );
    }

    /**
     * Converts a User document and tokens into a LoginResponse.
     */
    public LoginResponse toLoginResponse(
            User user,
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt) {
        if (user == null) {
            return null;
        }

        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        return new LoginResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            roles,
            accessToken,
            refreshToken,
            accessTokenExpiresAt,
            refreshTokenExpiresAt
        );
    }

    /**
     * Converts raw tokens and expirations into a RefreshTokenResponse.
     */
    public RefreshTokenResponse toRefreshTokenResponse(
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt) {
        return new RefreshTokenResponse(
                accessToken,
                refreshToken,
                accessTokenExpiresAt,
                refreshTokenExpiresAt
        );
    }
}
