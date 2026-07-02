package com.messaging.backend.websocket.event;

import com.messaging.backend.auth.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Listener for WebSocket lifecycle events.
 * Observes and logs connections and disconnections strictly for monitoring and debugging.
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        AuthenticatedUser user = extractAuthenticatedUser(event.getUser());
        if (user != null) {
            logger.info("WebSocket CONNECT attempt: sessionId={}, userId={}, username={}", 
                    sessionId, user.getId(), user.getUsername());
        } else {
            logger.info("WebSocket CONNECT attempt: sessionId={}, user=UNAUTHENTICATED", sessionId);
        }
    }

    @EventListener
    public void handleSessionConnectedEvent(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        AuthenticatedUser user = extractAuthenticatedUser(event.getUser());
        if (user != null) {
            logger.info("WebSocket CONNECTED successfully: sessionId={}, userId={}, username={}", 
                    sessionId, user.getId(), user.getUsername());
        } else {
            logger.info("WebSocket CONNECTED successfully: sessionId={}, user=UNAUTHENTICATED", sessionId);
        }
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        AuthenticatedUser user = extractAuthenticatedUser(event.getUser());
        if (user != null) {
            logger.info("WebSocket DISCONNECTED: sessionId={}, userId={}, username={}, status={}", 
                    sessionId, user.getId(), user.getUsername(), event.getCloseStatus());
        } else {
            logger.info("WebSocket DISCONNECTED: sessionId={}, user=UNAUTHENTICATED, status={}", 
                    sessionId, event.getCloseStatus());
        }
    }

    private AuthenticatedUser extractAuthenticatedUser(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof AuthenticatedUser user) {
                return user;
            }
        }
        return null;
    }
}
