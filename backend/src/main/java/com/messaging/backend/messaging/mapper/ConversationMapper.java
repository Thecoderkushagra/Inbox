package com.messaging.backend.messaging.mapper;

import com.messaging.backend.messaging.dto.response.ConversationParticipantResponse;
import com.messaging.backend.messaging.dto.response.ConversationResponse;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Conversation MongoDB documents and DTOs.
 */
@Component
public class ConversationMapper {

    /**
     * Converts a Conversation document to a ConversationResponse DTO.
     */
    public ConversationResponse toConversationResponse(Conversation conversation) {
        if (conversation == null) {
            return null;
        }

        List<ConversationParticipantResponse> participantResponses = toParticipantResponseList(
                conversation.getParticipants() != null ? conversation.getParticipants().stream().toList() : Collections.emptyList()
        );

        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getTitle(),
                conversation.getDescription(),
                conversation.isArchived(),
                conversation.getLastMessageAt(),
                conversation.getCreatedAt(),
                participantResponses
        );
    }

    /**
     * Converts a ConversationParticipant to a ConversationParticipantResponse DTO.
     */
    public ConversationParticipantResponse toParticipantResponse(ConversationParticipant participant) {
        if (participant == null) {
            return null;
        }

        return new ConversationParticipantResponse(
                participant.getUserId(),
                participant.getRole(),
                participant.getStatus(),
                participant.getJoinedAt()
        );
    }

    /**
     * Converts a list of ConversationParticipant to a list of ConversationParticipantResponse DTOs.
     */
    public List<ConversationParticipantResponse> toParticipantResponseList(List<ConversationParticipant> participants) {
        if (participants == null) {
            return Collections.emptyList();
        }
        return participants.stream()
                .map(this::toParticipantResponse)
                .collect(Collectors.toList());
    }
}
