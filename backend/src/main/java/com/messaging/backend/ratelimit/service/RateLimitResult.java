package com.messaging.backend.ratelimit.service;

public record RateLimitResult(boolean allowed, long remainingTokens, long retryAfterSeconds) {
}
