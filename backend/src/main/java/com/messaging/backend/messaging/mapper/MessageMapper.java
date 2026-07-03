package com.messaging.backend.messaging.mapper;

import com.messaging.backend.messaging.dto.response.MessageResponse;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.media.dto.response.MediaAttachmentResponse;
import com.messaging.backend.media.mapper.MediaMapper;
import com.messaging.backend.media.entity.MediaAttachment;
import com.messaging.backend.websocket.dto.response.MessageSocketResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper for Message entity to MessageResponse DTO.
 */
@Component
public class MessageMapper {

    private final MediaMapper mediaMapper;

    public MessageMapper(MediaMapper mediaMapper) {
        this.mediaMapper = mediaMapper;
    }

    /**
     * Maps a Message entity to a MessageResponse DTO.
     *
     * @param message the Message entity
     * @return the MessageResponse DTO
     */
    public MessageResponse toResponse(Message message, List<MediaAttachment> attachments) {
        if (message == null) {
            return null;
        }

        List<MediaAttachmentResponse> attachmentResponses = mediaMapper.toResponseList(
                attachments != null ? attachments : Collections.emptyList()
        );

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
                .attachments(attachmentResponses)
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
    public List<MessageResponse> toResponseList(List<Message> messages, Map<UUID, List<MediaAttachment>> attachmentsMap) {
        if (messages == null) {
            return List.of();
        }

        return messages.stream()
                .map(msg -> toResponse(msg, attachmentsMap != null ? attachmentsMap.get(msg.getId()) : null))
                .toList();
    }

    /**
     * Maps a page of Message entities to a page of MessageResponse DTOs.
     *
     * @param messages the page of Message entities
     * @return the page of MessageResponse DTOs
     */
    public Page<MessageResponse> toResponsePage(Page<Message> messages, Map<UUID, List<MediaAttachment>> attachmentsMap) {
        if (messages == null) {
            return Page.empty();
        }

        return messages.map(msg -> toResponse(msg, attachmentsMap != null ? attachmentsMap.get(msg.getId()) : null));
    }
}
