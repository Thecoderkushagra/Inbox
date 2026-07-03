package com.messaging.backend.ratelimit.filter;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.ratelimit.annotation.RateLimited;
import com.messaging.backend.ratelimit.exception.RateLimitExceededException;
import com.messaging.backend.ratelimit.service.RateLimitResult;
import com.messaging.backend.ratelimit.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (rateLimited == null) {
            return true;
        }

        String identifier = extractIdentifier(request);
        RateLimitResult result = rateLimitService.consume(rateLimited.policy(), identifier);

        if (!result.allowed()) {
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            throw new RateLimitExceededException("Too many requests. Please try again later.", result.retryAfterSeconds());
        }

        return true;
    }

    private String extractIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.getId().toString();
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
