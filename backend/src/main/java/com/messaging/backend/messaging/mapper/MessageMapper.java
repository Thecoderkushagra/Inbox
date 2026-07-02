package com.messaging.backend.messaging.mapper;

import com.messaging.backend.messaging.dto.response.MessageResponse;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.websocket.dto.response.MessageSocketResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for Message entity to MessageResponse DTO.
 */
@Component
public class MessageMapper {

    /**
     * Maps a Message entity to a MessageResponse DTO.
     *
     * @param message the Message entity
     * @return the MessageResponse DTO
     */
    public MessageResponse toResponse(Message message) {
        if (message == null) {
            return null;
        }

        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .edited(message.isEdited())
                .deleted(message.isDeleted())
                .editedAt(message.getEditedAt())
                .deletedAt(message.getDeletedAt())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Maps a Message entity to a MessageSocketResponse DTO.
     *
     * @param message the Message entity
     * @return the MessageSocketResponse DTO
     */
    public MessageSocketResponse toSocketResponse(Message message) {
        if (message == null) {
            return null;
        }

        return new MessageSocketResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getMessageType(),
                message.getStatus(),
                message.isEdited(),
                message.isDeleted(),
                message.getCreatedAt(),
                message.getEditedAt()
        );
    }

    /**
     * Maps a list of Message entities to a list of MessageResponse DTOs.
     *
     * @param messages the list of Message entities
     * @return the list of MessageResponse DTOs
     */
    public List<MessageResponse> toResponseList(List<Message> messages) {
        if (messages == null) {
            return List.of();
        }

        return messages.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Maps a page of Message entities to a page of MessageResponse DTOs.
     *
     * @param messages the page of Message entities
     * @return the page of MessageResponse DTOs
     */
    public Page<MessageResponse> toResponsePage(Page<Message> messages) {
        if (messages == null) {
            return Page.empty();
        }

        return messages.map(this::toResponse);
    }
}
