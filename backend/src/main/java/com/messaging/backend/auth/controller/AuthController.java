package com.messaging.backend.auth.controller;

import com.messaging.backend.auth.dto.request.RegisterRequest;
import com.messaging.backend.auth.dto.response.RegisterResponse;
import com.messaging.backend.auth.service.AuthService;
import com.messaging.backend.common.dto.response.SuccessResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication endpoints.
 *
 * <p>Purpose:
 * Exposes REST APIs for user registration and future authentication flows.
 *
 * <p>Responsibilities:
 * Receives incoming HTTP requests, triggers validation, delegates business logic
 * to the service layer, and wraps the result in standard API responses.
 *
 * <p>Extension points:
 * Will be extended to include login, refresh, and logout endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     *
     * @param request the registration payload containing user details
     * @return 201 Created with the user's non-sensitive details
     */
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("User registered successfully", response));
    }

    /**
     * Verifies a user's email address using a verification token.
     *
     * @param request the verification payload containing the token
     * @return 200 OK with the verified user details
     */
    @PostMapping("/verify-email")
    public ResponseEntity<SuccessResponse<com.messaging.backend.auth.dto.response.VerifyEmailResponse>> verifyEmail(@Valid @RequestBody com.messaging.backend.auth.dto.request.VerifyEmailRequest request) {
        com.messaging.backend.auth.dto.response.VerifyEmailResponse response = authService.verifyEmail(request);
        return ResponseEntity.ok(SuccessResponse.success("Email verified successfully", response));
    }

    /**
     * Authenticates a user and issues access/refresh tokens.
     *
     * @param request the login payload containing identifier and password
     * @return 200 OK with the JWTs and non-sensitive user details
     */
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<com.messaging.backend.auth.dto.response.LoginResponse>> login(@Valid @RequestBody com.messaging.backend.auth.dto.request.LoginRequest request) {
        com.messaging.backend.auth.dto.response.LoginResponse response = authService.login(request);
        return ResponseEntity.ok(SuccessResponse.success("Login successful", response));
    }

    /**
     * Refreshes a user's session by issuing a new access/refresh token pair.
     *
     * @param request the refresh payload containing the old refresh token
     * @return 200 OK with the newly generated JWTs
     */
    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<com.messaging.backend.auth.dto.response.RefreshTokenResponse>> refresh(@Valid @RequestBody com.messaging.backend.auth.dto.request.RefreshTokenRequest request) {
        com.messaging.backend.auth.dto.response.RefreshTokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(SuccessResponse.success("Session refreshed successfully", response));
    }

    /**
     * Logs out the authenticated user by revoking the refresh token.
     *
     * @param request the logout payload containing the refresh token
     * @param authenticatedUser the currently authenticated user context
     * @return 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(
            @Valid @RequestBody com.messaging.backend.auth.dto.request.LogoutRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.messaging.backend.auth.security.AuthenticatedUser authenticatedUser) {
        authService.logout(request, authenticatedUser);
        return ResponseEntity.ok(SuccessResponse.success("Logged out successfully.", null));
    }
}
