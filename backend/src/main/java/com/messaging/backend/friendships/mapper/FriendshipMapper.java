package com.messaging.backend.friendships.mapper;

import com.messaging.backend.friendships.dto.response.FriendshipResponse;
import com.messaging.backend.friendships.dto.response.FriendshipSocketResponse;
import com.messaging.backend.friendships.entity.Friendship;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FriendshipMapper {

    public FriendshipResponse toResponse(Friendship friendship) {
        if (friendship == null) {
            return null;
        }

        String reqId = friendship.getRequesterId() != null ? friendship.getRequesterId() :
                (friendship.getRequester() != null ? friendship.getRequester().getId() : null);

        String addrId = friendship.getAddresseeId() != null ? friendship.getAddresseeId() :
                (friendship.getAddressee() != null ? friendship.getAddressee().getId() : null);

        return new FriendshipResponse(
                friendship.getId(),
                reqId,
                addrId,
                friendship.getStatus(),
                friendship.getRespondedAt(),
                friendship.getBlockedAt(),
                friendship.getCreatedAt()
        );
    }

    public List<FriendshipResponse> toResponseList(List<Friendship> friendships) {
        if (friendships == null) {
            return Collections.emptyList();
        }

        return friendships.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FriendshipSocketResponse toSocketResponse(Friendship friendship) {
        if (friendship == null) {
            return null;
        }

        String reqId = friendship.getRequesterId() != null ? friendship.getRequesterId() :
                (friendship.getRequester() != null ? friendship.getRequester().getId() : null);

        String addrId = friendship.getAddresseeId() != null ? friendship.getAddresseeId() :
                (friendship.getAddressee() != null ? friendship.getAddressee().getId() : null);

        return new FriendshipSocketResponse(
                friendship.getId(),
                reqId,
                addrId,
                friendship.getStatus(),
                friendship.getRespondedAt(),
                friendship.getBlockedAt(),
                friendship.getCreatedAt()
        );
    }
}
