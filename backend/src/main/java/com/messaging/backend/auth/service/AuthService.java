package com.messaging.backend.auth.service;

import com.messaging.backend.auth.dto.request.LoginRequest;
import com.messaging.backend.auth.dto.request.LogoutRequest;
import com.messaging.backend.auth.dto.request.RefreshTokenRequest;
import com.messaging.backend.auth.dto.request.RegisterRequest;
import com.messaging.backend.auth.dto.response.LoginResponse;
import com.messaging.backend.auth.dto.response.RefreshTokenResponse;
import com.messaging.backend.auth.dto.response.RegisterResponse;
import com.messaging.backend.auth.entity.RefreshToken;
import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.RoleType;
import com.messaging.backend.auth.enums.UserStatus;
import com.messaging.backend.auth.mapper.AuthMapper;
import com.messaging.backend.auth.repository.RefreshTokenRepository;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.common.config.JwtProperties;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.InternalServerException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.common.security.exception.JwtAuthenticationException;
import com.messaging.backend.common.security.exception.JwtExpiredTokenException;
import com.messaging.backend.common.security.jwt.JwtTokenClaims;
import com.messaging.backend.common.security.jwt.JwtTokenProvider;
import com.messaging.backend.presence.service.PresenceService;
import com.messaging.backend.users.entity.UserProfile;
import com.messaging.backend.users.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service handling authentication-related business logic in MongoDB.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PresenceService presenceService;

    public AuthService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthMapper authMapper,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties,
                       PresenceService presenceService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.presenceService = presenceService;
    }

    /**
     * Authenticates a user and returns a LoginResponse containing JWTs.
     *
     * @param request The login request details (username/email and password)
     * @return LoginResponse with access and refresh tokens
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user;

        if (request.identifier().contains("@")) {
            user = userRepository.findByEmailIgnoreCase(request.identifier().toLowerCase())
                    .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        } else {
            user = userRepository.findByUsername(request.identifier())
                    .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Account is inactive or suspended");
        }

        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        String subject = user.getId();
        String accessToken = jwtTokenProvider.generateAccessToken(subject, roles);
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken(subject);

        Instant accessTokenExpiresAt = Instant.now().plusMillis(jwtProperties.getAccessTokenExpiration());
        Instant refreshTokenExpiresAt = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration());

        String hashedRefreshToken = hashToken(rawRefreshToken);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .tokenHash(hashedRefreshToken)
                .expiresAt(refreshTokenExpiresAt)
                .revoked(false)
                .userId(user.getId())
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return authMapper.toLoginResponse(
                user,
                accessToken,
                rawRefreshToken,
                accessTokenExpiresAt,
                refreshTokenExpiresAt
        );
    }

    /**
     * Registers a new user with immediate activation (no email/OTP verification required).
     *
     * @param request The registration details
     * @return RegisterResponse containing safe user details
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        String normalizedEmail = request.email().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ConflictException("Email is already in use");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User newUser = User.builder()
                .username(request.username())
                .email(normalizedEmail)
                .passwordHash(hashedPassword)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        newUser.setRoles(Set.of(RoleType.USER));

        User savedUser = userRepository.save(newUser);

        UserProfile profile = UserProfile.builder()
                .userId(savedUser.getId())
                .displayName(savedUser.getUsername())
                .build();
        userProfileRepository.save(profile);

        savedUser.setProfile(profile);
        userRepository.save(savedUser);

        presenceService.initializePresence(savedUser.getId());

        return authMapper.toRegisterResponse(savedUser);
    }

    /**
     * Refreshes a user's session by issuing a new access and refresh token pair.
     *
     * @param request the payload containing the old refresh token
     * @return RefreshTokenResponse containing the new tokens
     */
    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        JwtTokenClaims claims;
        try {
            claims = jwtTokenProvider.parseAndValidateToken(request.refreshToken());
        } catch (JwtAuthenticationException e) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        if (!JwtTokenClaims.TOKEN_TYPE_REFRESH.equals(claims.getTokenType())) {
            throw new BadRequestException("Invalid token type");
        }

        String hashedToken = hashToken(request.refreshToken());

        RefreshToken oldTokenEntity = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (oldTokenEntity.isRevoked()) {
            throw new BadRequestException("Refresh token is revoked");
        }

        if (oldTokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token is expired");
        }

        String userId = oldTokenEntity.getUserId();

        if (userId == null) {
            throw new BadRequestException("Invalid token user reference");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Account is inactive or suspended");
        }

        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        String subject = user.getId();
        String newAccessToken = jwtTokenProvider.generateAccessToken(subject, roles);
        String newRawRefreshToken = jwtTokenProvider.generateRefreshToken(subject);

        Instant accessTokenExpiresAt = Instant.now().plusMillis(jwtProperties.getAccessTokenExpiration());
        Instant refreshTokenExpiresAt = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration());

        String newHashedRefreshToken = hashToken(newRawRefreshToken);

        oldTokenEntity.revoke(Instant.now());
        refreshTokenRepository.save(oldTokenEntity);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .tokenHash(newHashedRefreshToken)
                .expiresAt(refreshTokenExpiresAt)
                .revoked(false)
                .userId(user.getId())
                .build();

        refreshTokenRepository.save(newRefreshTokenEntity);

        return authMapper.toRefreshTokenResponse(
                newAccessToken,
                newRawRefreshToken,
                accessTokenExpiresAt,
                refreshTokenExpiresAt
        );
    }

    /**
     * Revokes a refresh token to end a user's session.
     *
     * @param request the logout payload containing the refresh token
     * @param authenticatedUser the currently authenticated user
     */
    @Transactional
    public void logout(LogoutRequest request, AuthenticatedUser authenticatedUser) {
        if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
            return; // Idempotent success
        }

        String hashedToken = hashToken(request.refreshToken());
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByTokenHash(hashedToken);

        if (optionalToken.isEmpty()) {
            return; // Idempotent success
        }

        RefreshToken tokenEntity = optionalToken.get();

        if (tokenEntity.isRevoked()) {
            return; // Idempotent success
        }

        String tokenUserId = tokenEntity.getUserId();

        if (tokenUserId != null && !tokenUserId.equals(authenticatedUser.getId())) {
            throw new ForbiddenException("Cannot revoke a token belonging to another user");
        }

        tokenEntity.revoke(Instant.now());
        refreshTokenRepository.save(tokenEntity);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerException("Failed to hash token due to missing algorithm", e);
        }
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
