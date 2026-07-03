package com.messaging.backend.pubsub.config;

import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.listener.RedisEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Configuration for Redis Pub/Sub infrastructure.
 * Enables distributed message passing across multiple application instances.
 */
@Configuration
public class RedisPubSubConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisPubSubConfig.class);

    /**
     * Configures the message listener adapter with the correct serializer and target listener method.
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisEventListener redisEventListener, RedisTemplate<String, Object> redisTemplate) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(redisEventListener, "handleMessage");
        // Reuse the explicitly configured JSON serializer from the existing RedisTemplate
        adapter.setSerializer(redisTemplate.getValueSerializer());
        return adapter;
    }

    /**
     * Registers the Redis message listener container with specific channels for the application domains.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
            
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        container.addMessageListener(listenerAdapter, new ChannelTopic(PubSubChannels.CHAT_CHANNEL));
        container.addMessageListener(listenerAdapter, new ChannelTopic(PubSubChannels.NOTIFICATION_CHANNEL));
        container.addMessageListener(listenerAdapter, new ChannelTopic(PubSubChannels.PRESENCE_CHANNEL));
        container.addMessageListener(listenerAdapter, new ChannelTopic(PubSubChannels.READ_RECEIPT_CHANNEL));
        container.addMessageListener(listenerAdapter, new ChannelTopic(PubSubChannels.MEDIA_CHANNEL));
        container.addMessageListener(listenerAdapter, new ChannelTopic(PubSubChannels.SEARCH_CHANNEL));
        
        log.info("Redis Pub/Sub configuration initialized successfully with all channels.");
        return container;
    }
}
