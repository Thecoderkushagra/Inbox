package com.messaging.backend.presence.mapper;

import com.messaging.backend.presence.dto.PresenceResponse;
import com.messaging.backend.presence.entity.UserPresence;
import com.messaging.backend.websocket.dto.response.PresenceSocketResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for presence entities.
 * Manually implemented to avoid reflection and external dependencies.
 */
@Component
public class PresenceMapper {

    /**
     * Maps a UserPresence entity to a PresenceResponse DTO.
     * 
     * @param entity the presence entity
     * @return the mapped response DTO
     */
    public PresenceResponse toResponse(UserPresence entity) {
        if (entity == null) {
            return null;
        }
        return new PresenceResponse(
                entity.getUser().getId(),
                entity.getStatus(),
                entity.getLastSeen()
        );
    }

    /**
     * Maps a UserPresence entity to a PresenceSocketResponse DTO.
     * 
     * @param entity the presence entity
     * @return the mapped WebSocket response DTO
     */
    public PresenceSocketResponse toSocketResponse(UserPresence entity) {
        if (entity == null) {
            return null;
        }
        return new PresenceSocketResponse(
                entity.getUser().getId(),
                entity.getStatus(),
                entity.getLastSeen()
        );
    }
}
