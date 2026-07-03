package com.messaging.backend.cache.constants;

import java.time.Duration;

public final class CacheConstants {

    private CacheConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Cache TTL Defaults
    public static final Duration TTL_DEFAULT = Duration.ofMinutes(10);
    public static final Duration TTL_SHORT = Duration.ofMinutes(2);
    public static final Duration TTL_LONG = Duration.ofMinutes(30);

    // Named Caches
    public static final String USERS_CACHE = "users";
    public static final String CONVERSATIONS_CACHE = "conversations";
    public static final String MESSAGES_CACHE = "messages";
    public static final String GROUPS_CACHE = "groups";
    public static final String FRIENDSHIPS_CACHE = "friendships";
    public static final String PRESENCE_CACHE = "presence";
    public static final String SEARCH_CACHE = "search";
    public static final String MEDIA_CACHE = "media";
    public static final String NOTIFICATION_CACHE = "notifications";
    public static final String READ_RECEIPTS_CACHE = "read_receipts";
}
