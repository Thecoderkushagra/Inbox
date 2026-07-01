package com.messaging.backend.messaging.mapper;

import com.messaging.backend.messaging.dto.response.ConversationParticipantResponse;
import com.messaging.backend.messaging.dto.response.ConversationResponse;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Conversation domain entities and DTOs.
 */
@Component
public class ConversationMapper {

    /**
     * Converts a Conversation entity to a ConversationResponse DTO.
     *
     * @param conversation the domain entity
     * @return the response DTO
     */
    public ConversationResponse toConversationResponse(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getTitle(),
                !conversation.isArchived(),
                conversation.getCreatedAt()
        );
    }

    /**
     * Converts a ConversationParticipant entity to a ConversationParticipantResponse DTO.
     *
     * @param participant the domain entity
     * @return the response DTO
     */
    public ConversationParticipantResponse toParticipantResponse(ConversationParticipant participant) {
        if (participant == null) {
            return null;
        }
        return new ConversationParticipantResponse(
                participant.getUser().getId(),
                participant.getRole(),
                participant.getStatus(),
                participant.getJoinedAt()
        );
    }

    /**
     * Converts a list of ConversationParticipant entities to a list of ConversationParticipantResponse DTOs.
     *
     * @param participants the list of domain entities
     * @return the list of response DTOs
     */
    public List<ConversationParticipantResponse> toParticipantResponseList(List<ConversationParticipant> participants) {
        if (participants == null) {
            return null;
        }
        return participants.stream()
                .map(this::toParticipantResponse)
                .collect(Collectors.toList());
    }
}
