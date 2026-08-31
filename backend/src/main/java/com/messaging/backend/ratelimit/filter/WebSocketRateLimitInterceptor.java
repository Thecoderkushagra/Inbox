package com.messaging.backend.ratelimit.filter;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.ratelimit.constants.RateLimitPolicy;
import com.messaging.backend.ratelimit.service.RateLimitResult;
import com.messaging.backend.ratelimit.service.RateLimitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Component
public class WebSocketRateLimitInterceptor implements HandshakeInterceptor {

    private final RateLimitService rateLimitService;

    public WebSocketRateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String identifier = extractIdentifier(request);
        RateLimitResult result = rateLimitService.consume(RateLimitPolicy.WEBSOCKET_HANDSHAKE, identifier);

        if (!result.allowed()) {
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().add("Retry-After", String.valueOf(result.retryAfterSeconds()));
            return false;
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Nothing to do here
    }

    private String extractIdentifier(ServerHttpRequest request) {
        Principal principal = request.getPrincipal();
        if (principal instanceof UsernamePasswordAuthenticationToken auth && 
            auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user.getId().toString();
        }
        
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String ip = servletRequest.getServletRequest().getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
            return servletRequest.getServletRequest().getRemoteAddr();
        }
        
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }
}
