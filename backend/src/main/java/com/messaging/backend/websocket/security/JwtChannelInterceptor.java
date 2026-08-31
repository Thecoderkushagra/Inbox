package com.messaging.backend.websocket.security;

import com.messaging.backend.auth.security.CustomUserDetailsService;
import com.messaging.backend.common.security.exception.JwtAuthenticationException;
import com.messaging.backend.common.security.jwt.JwtTokenClaims;
import com.messaging.backend.common.security.jwt.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Intercepts STOMP messages to authenticate the WebSocket connection.
 * Validates the JWT strictly during the CONNECT phase.
 */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtChannelInterceptor(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
            if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
                throw new JwtAuthenticationException("Missing or malformed Authorization header");
            }

            String jwt = authorizationHeader.substring(BEARER_PREFIX.length());
            
            JwtTokenClaims claims = tokenProvider.parseAndValidateToken(jwt);

            if (!JwtTokenClaims.TOKEN_TYPE_ACCESS.equals(claims.getTokenType())) {
                throw new JwtAuthenticationException("Invalid token type for WebSocket authentication");
            }

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(claims.getSubject());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authentication);
        }

        return message;
    }
}
