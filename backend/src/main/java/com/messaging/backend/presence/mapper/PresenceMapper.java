package com.messaging.backend.presence.mapper;

import com.messaging.backend.presence.dto.PresenceResponse;
import com.messaging.backend.presence.enums.PresenceStatus;
import com.messaging.backend.websocket.dto.response.PresenceSocketResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper for presence data.
 */
@Component
public class PresenceMapper {

    public PresenceResponse toResponse(String userId, PresenceStatus status, Instant lastSeen) {
        return new PresenceResponse(
                userId,
                status != null ? status : PresenceStatus.OFFLINE,
                lastSeen
        );
    }

    public PresenceSocketResponse toSocketResponse(String userId, PresenceStatus status, Instant lastSeen) {
        return new PresenceSocketResponse(
                userId,
                status != null ? status : PresenceStatus.OFFLINE,
                lastSeen
        );
    }
}
