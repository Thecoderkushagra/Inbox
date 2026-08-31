package com.messaging.backend.groups.mapper;

import com.messaging.backend.groups.dto.response.GroupMemberResponse;
import com.messaging.backend.groups.dto.response.GroupResponse;
import com.messaging.backend.groups.dto.response.GroupSocketResponse;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import org.springframework.stereotype.Component;

import java.util.Collections;
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

    public List<GroupResponse> toGroupResponseList(List<Conversation> conversations) {
        if (conversations == null) {
            return Collections.emptyList();
        }

        return conversations.stream()
                .map(this::toGroupResponse)
                .collect(Collectors.toList());
    }

    public GroupMemberResponse toGroupMemberResponse(ConversationParticipant participant) {
        if (participant == null) {
            return null;
        }

        return new GroupMemberResponse(
                participant.getUserId(),
                participant.getRole() != null ? participant.getRole().name() : null,
                participant.getJoinedAt()
        );
    }

    public List<GroupMemberResponse> toGroupMemberResponseList(List<ConversationParticipant> participants) {
        if (participants == null) {
            return Collections.emptyList();
        }

        return participants.stream()
                .map(this::toGroupMemberResponse)
                .collect(Collectors.toList());
    }

    public GroupSocketResponse toSocketResponse(Conversation group) {
        if (group == null) {
            return null;
        }

        return new GroupSocketResponse(
                group.getId(),
                group.getTitle(),
                group.getDescription(),
                group.getType() != null ? group.getType().name() : null,
                group.getUpdatedAt() != null ? group.getUpdatedAt() : group.getCreatedAt()
        );
    }
}
