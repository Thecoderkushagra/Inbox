package com.messaging.backend.presence.service;

import com.messaging.backend.presence.dto.PresenceResponse;
import com.messaging.backend.presence.enums.PresenceStatus;
import com.messaging.backend.presence.mapper.PresenceMapper;
import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.dto.RedisEvent;
import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Service managing user presence exclusively in Upstash Redis.
 * Key pattern: presence:{userId} -> Hash with status, lastSeen
 */
@Slf4j
@Service
public class PresenceService {

    private static final String PRESENCE_KEY_PREFIX = "presence:";
    private static final Duration PRESENCE_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisEventPublisher redisEventPublisher;
    private final PresenceMapper presenceMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceService(RedisTemplate<String, Object> redisTemplate,
                           RedisEventPublisher redisEventPublisher,
                           PresenceMapper presenceMapper,
                           SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisEventPublisher = redisEventPublisher;
        this.presenceMapper = presenceMapper;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Initializes a user's presence state as OFFLINE.
     */
    public void initializePresence(String userId) {
        if (userId == null) {
            return;
        }
        String key = PRESENCE_KEY_PREFIX + userId;
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.FALSE.equals(exists)) {
            Instant now = Instant.now();
            redisTemplate.opsForHash().put(key, "status", PresenceStatus.OFFLINE.name());
            redisTemplate.opsForHash().put(key, "lastSeen", String.valueOf(now.toEpochMilli()));
            redisTemplate.expire(key, PRESENCE_TTL);
        }
    }

    /**
     * Marks a user as ONLINE and refreshes presence TTL.
     */
    public void markOnline(String userId) {
        if (userId == null) return;
        String key = PRESENCE_KEY_PREFIX + userId;
        Instant now = Instant.now();
        redisTemplate.opsForHash().put(key, "status", PresenceStatus.ONLINE.name());
        redisTemplate.opsForHash().put(key, "lastSeen", String.valueOf(now.toEpochMilli()));
        redisTemplate.expire(key, PRESENCE_TTL);

        publishPresenceEvent(userId, PresenceStatus.ONLINE, now);
    }

    /**
     * Marks a user as AWAY.
     */
    public void markAway(String userId) {
        if (userId == null) return;
        String key = PRESENCE_KEY_PREFIX + userId;
        Instant now = Instant.now();
        redisTemplate.opsForHash().put(key, "status", PresenceStatus.AWAY.name());
        redisTemplate.opsForHash().put(key, "lastSeen", String.valueOf(now.toEpochMilli()));
        redisTemplate.expire(key, PRESENCE_TTL);

        publishPresenceEvent(userId, PresenceStatus.AWAY, now);
    }

    /**
     * Marks a user as OFFLINE and updates lastSeen.
     */
    public void markOffline(String userId) {
        if (userId == null) return;
        String key = PRESENCE_KEY_PREFIX + userId;
        Instant now = Instant.now();
        redisTemplate.opsForHash().put(key, "status", PresenceStatus.OFFLINE.name());
        redisTemplate.opsForHash().put(key, "lastSeen", String.valueOf(now.toEpochMilli()));
        redisTemplate.expire(key, PRESENCE_TTL);

        publishPresenceEvent(userId, PresenceStatus.OFFLINE, now);
    }

    /**
     * Retrieves a user's presence state from Redis.
     */
    public PresenceResponse getPresence(String userId) {
        if (userId == null) {
            return new PresenceResponse(null, PresenceStatus.OFFLINE, null);
        }
        String key = PRESENCE_KEY_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return new PresenceResponse(userId, PresenceStatus.OFFLINE, null);
        }

        String statusStr = (String) entries.get("status");
        PresenceStatus status = PresenceStatus.OFFLINE;
        if (statusStr != null) {
            try {
                status = PresenceStatus.valueOf(statusStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        String lastSeenStr = (String) entries.get("lastSeen");
        Instant lastSeen = null;
        if (lastSeenStr != null) {
            try {
                lastSeen = Instant.ofEpochMilli(Long.parseLong(lastSeenStr));
            } catch (NumberFormatException ignored) {
            }
        }

        return presenceMapper.toResponse(userId, status, lastSeen);
    }

    /**
     * Checks if a user is currently ONLINE.
     */
    public boolean isOnline(String userId) {
        return getPresence(userId).status() == PresenceStatus.ONLINE;
    }

    private void publishPresenceEvent(String userId, PresenceStatus status, Instant lastSeen) {
        try {
            var response = presenceMapper.toSocketResponse(userId, status, lastSeen);
            messagingTemplate.convertAndSend(WebSocketDestinations.PRESENCE_TOPIC, response);
            redisEventPublisher.publish(PubSubChannels.PRESENCE_CHANNEL, 
                new RedisEvent(null, "PRESENCE", null, response, null));
        } catch (Exception e) {
            log.error("Failed to publish presence event for user: {}", userId, e);
        }
    }
}
