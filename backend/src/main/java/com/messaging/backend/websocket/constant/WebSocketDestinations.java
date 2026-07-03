package com.messaging.backend.websocket.constant;

/**
 * Utility class containing constants for all STOMP WebSocket destinations.
 */
public final class WebSocketDestinations {

    private WebSocketDestinations() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Broker Prefixes
    public static final String APP_PREFIX = "/app";
    public static final String TOPIC_PREFIX = "/topic";
    public static final String USER_QUEUE_PREFIX = "/user";

    // Application Endpoints (Inbound from Client)
    public static final String CHAT_SEND = "/chat.send";
    
    // Broker Destinations (Outbound to Client)
    public static final String CHAT_TOPIC = "/topic/conversations."; // Appended with Conversation ID
    public static final String PRESENCE_TOPIC = "/topic/presence";
    public static final String FRIENDSHIP_TOPIC = "/topic/friendships";
    public static final String GROUP_TOPIC = "/topic/groups";
    public static final String USER_ERROR_QUEUE = "/queue/errors";
}
