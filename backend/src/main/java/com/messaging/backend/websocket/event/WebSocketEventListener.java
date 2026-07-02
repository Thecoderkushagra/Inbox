package com.messaging.backend.websocket.event;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.presence.entity.UserPresence;
import com.messaging.backend.presence.mapper.PresenceMapper;
import com.messaging.backend.presence.service.PresenceService;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import com.messaging.backend.websocket.dto.response.PresenceSocketResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

/**
 * Listener for WebSocket lifecycle events.
 * Observes and logs connections and disconnections strictly for monitoring and debugging.
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final PresenceService presenceService;
    private final PresenceMapper presenceMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketEventListener(PresenceService presenceService,
                                  PresenceMapper presenceMapper,
                                  SimpMessagingTemplate simpMessagingTemplate) {
        this.presenceService = presenceService;
        this.presenceMapper = presenceMapper;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

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
            
            try {
                presenceService.markOnline(user.getId());
                logger.info("User marked ONLINE");
                broadcastPresence(user.getId());
            } catch (Exception e) {
                logger.error("Failed to mark user ONLINE: sessionId={}, userId={}", sessionId, user.getId(), e);
            }
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
            
            try {
                presenceService.markOffline(user.getId());
                logger.info("User marked OFFLINE");
                broadcastPresence(user.getId());
            } catch (Exception e) {
                logger.error("Failed to mark user OFFLINE: sessionId={}, userId={}", sessionId, user.getId(), e);
            }
        } else {
            logger.info("WebSocket DISCONNECTED: sessionId={}, user=UNAUTHENTICATED, status={}", 
                    sessionId, event.getCloseStatus());
        }
    }

    private void broadcastPresence(UUID userId) {
        try {
            UserPresence presence = presenceService.getPresence(userId);
            PresenceSocketResponse response = presenceMapper.toSocketResponse(presence);
            simpMessagingTemplate.convertAndSend(WebSocketDestinations.PRESENCE_TOPIC, response);
        } catch (Exception e) {
            logger.error("Failed to broadcast presence update for userId={}", userId, e);
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
