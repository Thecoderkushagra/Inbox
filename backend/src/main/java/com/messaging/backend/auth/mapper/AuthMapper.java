package com.messaging.backend.auth.mapper;

import com.messaging.backend.auth.dto.response.LoginResponse;
import com.messaging.backend.auth.dto.response.RefreshTokenResponse;
import com.messaging.backend.auth.dto.response.RegisterResponse;
import com.messaging.backend.auth.dto.response.VerifyEmailResponse;
import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;



/**
 * Mapper for Authentication related DTOs and Entities.
 *
 * <p>Purpose:
 * Isolates the logic of converting entities to DTOs and vice versa.
 *
 * <p>Responsibilities:
 * Safely copies allowed fields from entity to response models.
 * Ensures passwords and private tokens never leak into responses.
 *
 * <p>Extension points:
 * Can be extended to map Login Responses or Token Refresh objects.
 */
@Component
public class AuthMapper {

    /**
     * Converts a User entity into a RegisterResponse.
     *
     * @param user the saved User entity
     * @return RegisterResponse containing safe fields
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
            user.getStatus() == UserStatus.PENDING_VERIFICATION
        );
    }

    /**
     * Converts a User entity into a VerifyEmailResponse.
     *
     * @param user the verified User entity
     * @param verifiedAt the time the verification occurred
     * @return VerifyEmailResponse containing safe fields
     */
    public VerifyEmailResponse toVerifyEmailResponse(User user, Instant verifiedAt) {
        if (user == null) {
            return null;
        }

        return new VerifyEmailResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getStatus(),
            verifiedAt
        );
    }
    /**
     * Converts a User entity and tokens into a LoginResponse.
     *
     * @param user the authenticated User entity
     * @param accessToken the generated access token
     * @param refreshToken the generated refresh token
     * @param accessTokenExpiresAt expiration of the access token
     * @param refreshTokenExpiresAt expiration of the refresh token
     * @return LoginResponse containing tokens and safe user fields
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
                .map(role -> role.getName().name())
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
     *
     * @param accessToken the new access token
     * @param refreshToken the new refresh token
     * @param accessTokenExpiresAt expiration of the new access token
     * @param refreshTokenExpiresAt expiration of the new refresh token
     * @return RefreshTokenResponse containing the new tokens
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
