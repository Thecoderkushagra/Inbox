package com.messaging.backend.pubsub.constants;

public final class PubSubChannels {
    private PubSubChannels() {}

    public static final String CHAT_CHANNEL = "pubsub:chat";
    public static final String NOTIFICATION_CHANNEL = "pubsub:notification";
    public static final String PRESENCE_CHANNEL = "pubsub:presence";
    public static final String READ_RECEIPT_CHANNEL = "pubsub:read-receipt";
    public static final String MEDIA_CHANNEL = "pubsub:media";
    public static final String SEARCH_CHANNEL = "pubsub:search";
}
