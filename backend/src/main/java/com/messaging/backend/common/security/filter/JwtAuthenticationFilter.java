package com.messaging.backend.common.security.filter;

import com.messaging.backend.auth.security.CustomUserDetailsService;
import com.messaging.backend.common.security.exception.JwtInvalidTokenException;
import com.messaging.backend.common.security.jwt.JwtTokenClaims;
import com.messaging.backend.common.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;




/**
 * Security filter to intercept incoming requests and validate JWT access tokens.
 *
 * <p>Purpose:
 * Extracts the Bearer token from the Authorization header, validates it using the {@link JwtTokenProvider},
 * and populates the {@link org.springframework.security.core.context.SecurityContext} if valid.
 *
 * <p>Responsibilities:
 * Coordinates parsing the token, loading the AuthenticatedUser, and asserting it into the security context.
 *
 * <p>Extension points:
 * Can be extended to check a Redis blacklist for revoked tokens before granting access.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService customUserDetailsService,
                                   AuthenticationEntryPoint authenticationEntryPoint) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractToken(request);
            if (StringUtils.hasText(jwt)) {
                JwtTokenClaims claims = tokenProvider.parseAndValidateToken(jwt);

                if (JwtTokenClaims.TOKEN_TYPE_ACCESS.equals(claims.getTokenType())) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(claims.getSubject());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            if (ex instanceof AuthenticationException authEx) {
                authenticationEntryPoint.commence(request, response, authEx);
            } else {
                authenticationEntryPoint.commence(request, response, new JwtInvalidTokenException("Authentication failed due to token issues", ex));
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
