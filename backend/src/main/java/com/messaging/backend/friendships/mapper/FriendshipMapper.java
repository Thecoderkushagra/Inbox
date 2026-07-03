package com.messaging.backend.friendships.mapper;

import com.messaging.backend.friendships.dto.response.FriendshipResponse;
import com.messaging.backend.friendships.dto.response.FriendshipSocketResponse;
import com.messaging.backend.friendships.entity.Friendship;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for friendship entities.
 */
@Component
public class FriendshipMapper {

    /**
     * Maps a Friendship entity to a FriendshipResponse DTO.
     *
     * @param entity the friendship entity
     * @return the mapped FriendshipResponse DTO
     */
    public FriendshipResponse toResponse(Friendship entity) {
        if (entity == null) {
            return null;
        }

        return new FriendshipResponse(
                entity.getId(),
                entity.getRequester().getId(),
                entity.getAddressee().getId(),
                entity.getStatus(),
                entity.getRespondedAt(),
                entity.getBlockedAt(),
                entity.getCreatedAt()
        );
    }

    /**
     * Maps a list of Friendship entities to a list of FriendshipResponse DTOs.
     *
     * @param entities the list of friendship entities
     * @return the list of mapped FriendshipResponse DTOs
     */
    public List<FriendshipResponse> toResponseList(List<Friendship> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a Friendship entity to a FriendshipSocketResponse DTO.
     *
     * @param entity the friendship entity
     * @return the mapped FriendshipSocketResponse DTO
     */
    public FriendshipSocketResponse toSocketResponse(Friendship entity) {
        if (entity == null) {
            return null;
        }

        return new FriendshipSocketResponse(
                entity.getId(),
                entity.getRequester().getId(),
                entity.getAddressee().getId(),
                entity.getStatus(),
                entity.getRespondedAt(),
                entity.getBlockedAt(),
                entity.getCreatedAt()
        );
    }
}
