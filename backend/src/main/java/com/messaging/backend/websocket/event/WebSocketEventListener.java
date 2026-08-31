package com.messaging.backend.websocket.event;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.presence.service.PresenceService;
import lombok.extern.slf4j.Slf4j;
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
 */
@Slf4j
@Component
public class WebSocketEventListener {

    private final PresenceService presenceService;

    public WebSocketEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        AuthenticatedUser user = extractAuthenticatedUser(event.getUser());
        if (user != null) {
            log.info("WebSocket CONNECT attempt: sessionId={}, userId={}, username={}", 
                    sessionId, user.getId(), user.getUsername());
        } else {
            log.info("WebSocket CONNECT attempt: sessionId={}, user=UNAUTHENTICATED", sessionId);
        }
    }

    @EventListener
    public void handleSessionConnectedEvent(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        AuthenticatedUser user = extractAuthenticatedUser(event.getUser());
        if (user != null) {
            log.info("WebSocket CONNECTED successfully: sessionId={}, userId={}, username={}", 
                    sessionId, user.getId(), user.getUsername());
            
            try {
                presenceService.markOnline(user.getId());
                log.info("User marked ONLINE: userId={}", user.getId());
            } catch (Exception e) {
                log.error("Failed to mark user ONLINE: sessionId={}, userId={}", sessionId, user.getId(), e);
            }
        }
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        AuthenticatedUser user = extractAuthenticatedUser(event.getUser());
        if (user != null) {
            log.info("WebSocket DISCONNECTED: sessionId={}, userId={}, username={}", 
                    sessionId, user.getId(), user.getUsername());
            
            try {
                presenceService.markOffline(user.getId());
                log.info("User marked OFFLINE: userId={}", user.getId());
            } catch (Exception e) {
                log.error("Failed to mark user OFFLINE: sessionId={}, userId={}", sessionId, user.getId(), e);
            }
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
