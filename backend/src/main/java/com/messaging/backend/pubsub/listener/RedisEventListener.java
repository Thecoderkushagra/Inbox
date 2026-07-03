package com.messaging.backend.pubsub.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messaging.backend.pubsub.dto.RedisEvent;
import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import com.messaging.backend.websocket.dto.response.MessageSocketResponse;
import com.messaging.backend.websocket.dto.response.PresenceSocketResponse;
import com.messaging.backend.notifications.dto.response.NotificationSocketResponse;
import com.messaging.backend.readreceipts.dto.response.ReadReceiptSocketResponse;
import com.messaging.backend.media.dto.response.MediaSocketResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.propagation.Propagator;
import java.util.Map;

@Component
public class RedisEventListener {

    private static final Logger log = LoggerFactory.getLogger(RedisEventListener.class);
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisEventPublisher redisEventPublisher;
    private final ObjectProvider<Tracer> tracerProvider;
    private final ObjectProvider<Propagator> propagatorProvider;

    public RedisEventListener(ObjectMapper objectMapper,
                              SimpMessagingTemplate messagingTemplate,
                              RedisEventPublisher redisEventPublisher,
                              ObjectProvider<Tracer> tracerProvider,
                              ObjectProvider<Propagator> propagatorProvider) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.redisEventPublisher = redisEventPublisher;
        this.tracerProvider = tracerProvider;
        this.propagatorProvider = propagatorProvider;
    }

    public void handleMessage(Object message) {
        try {
            RedisEvent event;
            if (message instanceof RedisEvent redisEvent) {
                event = redisEvent;
            } else if (message instanceof String jsonString) {
                event = objectMapper.readValue(jsonString, RedisEvent.class);
            } else if (message instanceof byte[] bytes) {
                event = objectMapper.readValue(bytes, RedisEvent.class);
            } else {
                event = objectMapper.convertValue(message, RedisEvent.class);
            }
            
            if (event == null || event.eventType() == null) {
                log.warn("Received invalid or null event payload: {}", message);
                return;
            }

            // Ignore events that originated from this exact JVM instance to prevent broadcast loops
            if (redisEventPublisher.getInstanceId().equals(event.sourceInstanceId())) {
                log.debug("Ignoring local event {} from this instance", event.eventType());
                return;
            }

            log.debug("Received event {} from instance {}", event.eventType(), event.sourceInstanceId());
            
            Tracer tracer = tracerProvider.getIfAvailable();
            Propagator propagator = propagatorProvider.getIfAvailable();
            
            if (tracer != null && propagator != null && event.traceContext() != null && !event.traceContext().isEmpty()) {
                Span.Builder spanBuilder = propagator.extract(event.traceContext(), Map::get);
                Span span = spanBuilder.name("redis-pubsub-receive").start();
                try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
                    dispatch(event);
                } finally {
                    span.end();
                }
            } else {
                dispatch(event);
            }
        } catch (Exception e) {
            log.warn("Failed to deserialize or process Redis Pub/Sub message. Ignoring malformed payload.", e);
        }
    }

    private void dispatch(RedisEvent event) {
        try {
            switch (event.eventType()) {
                case "CHAT" -> handleChatEvent(event);
                case "NOTIFICATION" -> handleNotificationEvent(event);
                case "READ_RECEIPT" -> handleReadReceiptEvent(event);
                case "MEDIA" -> handleMediaEvent(event);
                case "PRESENCE" -> handlePresenceEvent(event);
                default -> log.debug("Unhandled event type: {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Failed to route/broadcast Redis event: {}", event.eventType(), e);
        }
    }

    private void handleChatEvent(RedisEvent event) {
        MessageSocketResponse payload = objectMapper.convertValue(event.payload(), MessageSocketResponse.class);
        String destination = WebSocketDestinations.CHAT_TOPIC + payload.conversationId();
        messagingTemplate.convertAndSend(destination, payload);
    }

    private void handleNotificationEvent(RedisEvent event) {
        NotificationSocketResponse payload = objectMapper.convertValue(event.payload(), NotificationSocketResponse.class);
        messagingTemplate.convertAndSendToUser(
                payload.recipientId().toString(),
                WebSocketDestinations.NOTIFICATION_QUEUE,
                payload
        );
    }

    private void handleReadReceiptEvent(RedisEvent event) {
        ReadReceiptSocketResponse payload = objectMapper.convertValue(event.payload(), ReadReceiptSocketResponse.class);
        messagingTemplate.convertAndSend(WebSocketDestinations.READ_RECEIPT_TOPIC, payload);
    }

    private void handleMediaEvent(RedisEvent event) {
        MediaSocketResponse payload = objectMapper.convertValue(event.payload(), MediaSocketResponse.class);
        messagingTemplate.convertAndSend(WebSocketDestinations.MEDIA_TOPIC, payload);
    }

    private void handlePresenceEvent(RedisEvent event) {
        PresenceSocketResponse payload = objectMapper.convertValue(event.payload(), PresenceSocketResponse.class);
        messagingTemplate.convertAndSend(WebSocketDestinations.PRESENCE_TOPIC, payload);
    }
}
