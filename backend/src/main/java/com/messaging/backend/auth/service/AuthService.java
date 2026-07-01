package com.messaging.backend.auth.service;

import com.messaging.backend.auth.dto.request.LoginRequest;
import com.messaging.backend.auth.dto.request.LogoutRequest;
import com.messaging.backend.auth.dto.request.RefreshTokenRequest;
import com.messaging.backend.auth.dto.request.RegisterRequest;
import com.messaging.backend.auth.dto.request.VerifyEmailRequest;
import com.messaging.backend.auth.dto.response.LoginResponse;
import com.messaging.backend.auth.dto.response.RefreshTokenResponse;
import com.messaging.backend.auth.dto.response.RegisterResponse;
import com.messaging.backend.auth.dto.response.VerifyEmailResponse;
import com.messaging.backend.auth.entity.EmailVerificationToken;
import com.messaging.backend.auth.entity.RefreshToken;
import com.messaging.backend.auth.entity.Role;
import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.RoleType;
import com.messaging.backend.auth.enums.UserStatus;
import com.messaging.backend.auth.mapper.AuthMapper;
import com.messaging.backend.auth.repository.EmailVerificationTokenRepository;
import com.messaging.backend.auth.repository.RefreshTokenRepository;
import com.messaging.backend.auth.repository.RoleRepository;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




/**
 * Service handling authentication-related business logic.
 *
 * <p>Purpose:
 * Manages user registration, email verification, and login logic.
 *
 * <p>Responsibilities:
 * Validates inputs, hashes passwords, generates JWT tokens securely,
 * and maintains token/account state.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    
    private static final int TOKEN_ENTROPY_BYTES = 32;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       EmailVerificationTokenRepository tokenRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthMapper authMapper,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenRepository = tokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
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

        switch (user.getStatus()) {
            case PENDING_VERIFICATION -> throw new ConflictException("Account is pending verification");
            case ACTIVE -> {} // Allowed
            default -> throw new ForbiddenException("Account is inactive");
        }

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        String subject = user.getId().toString();
        String accessToken = jwtTokenProvider.generateAccessToken(subject, roles);
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken(subject);
        
        // Use JwtProperties to compute expirations
        Instant accessTokenExpiresAt = Instant.now().plusMillis(jwtProperties.getAccessTokenExpiration());
        Instant refreshTokenExpiresAt = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration());
        
        String hashedRefreshToken = hashToken(rawRefreshToken);
        
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .tokenHash(hashedRefreshToken)
                .expiresAt(refreshTokenExpiresAt)
                .revoked(false)
                .user(user)
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
     * Registers a new user.
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

        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new InternalServerException("Default user role not found in database"));

        String hashedPassword = passwordEncoder.encode(request.password());

        User newUser = User.builder()
                .username(request.username())
                .email(normalizedEmail)
                .passwordHash(hashedPassword)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();

        newUser.addRole(userRole);

        User savedUser = userRepository.save(newUser);

        String rawToken = generateSecureToken();
        String hashedToken = hashToken(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().plusSeconds(24 * 60 * 60))
                .used(false)
                .user(savedUser)
                .build();

        tokenRepository.save(verificationToken);

        return authMapper.toRegisterResponse(savedUser);
    }

    /**
     * Verifies an email address using a token.
     *
     * @param request The verification request with the token
     * @return VerifyEmailResponse with user verification info
     */
    @Transactional
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {
        String hashedToken = hashToken(request.token());

        EmailVerificationToken tokenEntity = tokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new ResourceNotFoundException("Verification token not found or invalid"));

        if (tokenEntity.isUsed()) {
            throw new BadRequestException("Token has already been used");
        }

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        User user = tokenEntity.getUser();

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new ConflictException("Account is already verified");
        }

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new BadRequestException("Account is in an invalid state for verification");
        }

        user.markAsVerified();
        userRepository.save(user);

        Instant usedAt = Instant.now();
        tokenEntity.markAsUsed(usedAt);
        tokenRepository.save(tokenEntity);

        return authMapper.toVerifyEmailResponse(user, usedAt);
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

        User user = oldTokenEntity.getUser();

        switch (user.getStatus()) {
            case PENDING_VERIFICATION -> throw new ConflictException("Account is pending verification");
            case ACTIVE -> {} // Allowed
            default -> throw new ForbiddenException("Account is inactive");
        }

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        String subject = user.getId().toString();
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
                .user(user)
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
        JwtTokenClaims claims;
        try {
            claims = jwtTokenProvider.parseAndValidateToken(request.refreshToken());
        } catch (JwtExpiredTokenException e) {
            // An expired token is technically already unusable. Idempotent success.
            return;
        } catch (JwtAuthenticationException e) {
            throw new BadRequestException("Invalid refresh token");
        }

        if (!JwtTokenClaims.TOKEN_TYPE_REFRESH.equals(claims.getTokenType())) {
            throw new BadRequestException("Invalid token type");
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

        if (!tokenEntity.getUser().getId().equals(authenticatedUser.getId())) {
            throw new ForbiddenException("Cannot revoke a token belonging to another user");
        }

        tokenEntity.revoke(Instant.now());
        refreshTokenRepository.save(tokenEntity);
    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_ENTROPY_BYTES];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
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
}
