package com.messaging.backend.groups.mapper;

import com.messaging.backend.groups.dto.response.GroupMemberResponse;
import com.messaging.backend.groups.dto.response.GroupResponse;
import com.messaging.backend.groups.dto.response.GroupSocketResponse;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupMapper {

    public GroupResponse toGroupResponse(Conversation conversation) {
        if (conversation == null) {
            return null;
        }

        return new GroupResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getDescription(),
                conversation.getType() != null ? conversation.getType().name() : null,
                conversation.getCreatedAt()
        );
    }

    public GroupSocketResponse toSocketResponse(Conversation conversation) {
        if (conversation == null) {
            return null;
        }

        return new GroupSocketResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getDescription(),
                conversation.getType() != null ? conversation.getType().name() : null,
                conversation.getUpdatedAt() != null ? conversation.getUpdatedAt() : conversation.getCreatedAt()
        );
    }

    public GroupMemberResponse toMemberResponse(ConversationParticipant participant) {
        if (participant == null) {
            return null;
        }

        return new GroupMemberResponse(
                participant.getUser().getId(),
                participant.getRole() != null ? participant.getRole().name() : null,
                participant.getJoinedAt()
        );
    }

    public List<GroupResponse> toGroupResponseList(List<Conversation> conversations) {
        if (conversations == null) {
            return null;
        }
        return conversations.stream()
                .map(this::toGroupResponse)
                .collect(Collectors.toList());
    }

    public List<GroupMemberResponse> toMemberResponseList(List<ConversationParticipant> participants) {
        if (participants == null) {
            return null;
        }
        return participants.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }
}
