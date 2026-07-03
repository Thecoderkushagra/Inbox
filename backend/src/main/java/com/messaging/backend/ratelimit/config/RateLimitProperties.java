package com.messaging.backend.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    
    // Default policies
    private Map<String, Policy> policies = Map.of(
            "login", new Policy(5, 60),
            "register", new Policy(3, 60),
            "otp", new Policy(5, 600),
            "send_message", new Policy(100, 60),
            "media_upload", new Policy(20, 60),
            "websocket_handshake", new Policy(20, 60)
    );

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(Map<String, Policy> policies) {
        this.policies = policies;
    }

    public static class Policy {
        private int limit;
        private int windowSeconds;

        public Policy() {
        }

        public Policy(int limit, int windowSeconds) {
            this.limit = limit;
            this.windowSeconds = windowSeconds;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(int windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }
}
