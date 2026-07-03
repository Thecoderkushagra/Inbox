package com.messaging.backend.presence.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.presence.entity.UserPresence;
import com.messaging.backend.presence.enums.PresenceStatus;
import com.messaging.backend.presence.repository.UserPresenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.dto.RedisEvent;
import com.messaging.backend.presence.mapper.PresenceMapper;

import java.util.UUID;

/**
 * Encapsulates all business logic related to user presence management.
 */
@Service
public class PresenceService {

    private final UserPresenceRepository userPresenceRepository;
    private final RedisEventPublisher redisEventPublisher;
    private final PresenceMapper presenceMapper;

    public PresenceService(UserPresenceRepository userPresenceRepository,
                           RedisEventPublisher redisEventPublisher,
                           PresenceMapper presenceMapper) {
        this.userPresenceRepository = userPresenceRepository;
        this.redisEventPublisher = redisEventPublisher;
        this.presenceMapper = presenceMapper;
    }

    /**
     * Creates the initial UserPresence record for a newly registered user.
     * Safely ignores the request if a record already exists.
     */
    @Transactional
    public void initializePresence(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!userPresenceRepository.existsByUserId(user.getId())) {
            UserPresence presence = new UserPresence(user);
            userPresenceRepository.save(presence);
        }
    }

    /**
     * Marks a user as ONLINE.
     * Idempotent: returns immediately if already ONLINE.
     */
    @Transactional
    public void markOnline(UUID userId) {
        UserPresence presence = getPresence(userId);
        if (presence.getStatus() == PresenceStatus.ONLINE) {
            return;
        }
        presence.updateStatus(PresenceStatus.ONLINE);
        publishPresenceEvent(presence);
    }

    /**
     * Marks a user as AWAY.
     * Idempotent: returns immediately if already AWAY.
     */
    @Transactional
    public void markAway(UUID userId) {
        UserPresence presence = getPresence(userId);
        if (presence.getStatus() == PresenceStatus.AWAY) {
            return;
        }
        presence.updateStatus(PresenceStatus.AWAY);
        publishPresenceEvent(presence);
    }

    /**
     * Marks a user as OFFLINE and updates their lastSeen timestamp.
     * Idempotent: returns immediately if already OFFLINE.
     */
    @Transactional
    public void markOffline(UUID userId) {
        UserPresence presence = getPresence(userId);
        if (presence.getStatus() == PresenceStatus.OFFLINE) {
            return;
        }
        presence.updateStatus(PresenceStatus.OFFLINE);
        publishPresenceEvent(presence);
    }

    /**
     * Retrieves a user's presence record.
     * Throws ResourceNotFoundException if it does not exist.
     */
    @Transactional(readOnly = true)
    public UserPresence getPresence(UUID userId) {
        return userPresenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Presence record not found for user: " + userId));
    }

    /**
     * Returns true if the user is currently ONLINE.
     */
    @Transactional(readOnly = true)
    public boolean isOnline(UUID userId) {
        return getPresence(userId).getStatus() == PresenceStatus.ONLINE;
    }

    private void publishPresenceEvent(UserPresence presence) {
        try {
            var response = presenceMapper.toSocketResponse(presence);
            redisEventPublisher.publish(PubSubChannels.PRESENCE_CHANNEL, 
                new RedisEvent(null, "PRESENCE", null, response, null));
        } catch (Exception e) {
            // Log and ignore to prevent transaction rollback
        }
    }
}
