package com.messaging.backend.messaging.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.messaging.dto.request.UpdateMessageRequest;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import com.messaging.backend.messaging.repository.MessageRepository;
import com.messaging.backend.messaging.dto.response.MessageResponse;
import com.messaging.backend.messaging.mapper.MessageMapper;
import com.messaging.backend.media.entity.MediaAttachment;
import com.messaging.backend.media.service.MediaService;
import com.messaging.backend.notifications.enums.NotificationType;
import com.messaging.backend.notifications.service.NotificationService;
import com.messaging.backend.presence.service.PresenceService;
import com.messaging.backend.readreceipts.service.ReadReceiptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Service for handling messaging business logic.
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;
    private final NotificationService notificationService;
    private final PresenceService presenceService;
    private final MediaService mediaService;
    private final MessageMapper messageMapper;
    private final ReadReceiptService readReceiptService;

    public MessageService(MessageRepository messageRepository, ConversationService conversationService,
                          NotificationService notificationService, PresenceService presenceService,
                          MediaService mediaService, MessageMapper messageMapper,
                          ReadReceiptService readReceiptService) {
        this.messageRepository = messageRepository;
        this.conversationService = conversationService;
        this.notificationService = notificationService;
        this.presenceService = presenceService;
        this.mediaService = mediaService;
        this.messageMapper = messageMapper;
        this.readReceiptService = readReceiptService;
    }

    /**
     * Sends a new message within a conversation.
     *
     * @param senderId       the ID of the user sending the message
     * @param conversationId the ID of the target conversation
     * @param content        the raw string content of the message
     * @return the persisted Message entity
     * @throws BadRequestException if the content is blank or exceeds 5000 characters
     */
    @Transactional
    public Message sendMessage(UUID senderId, UUID conversationId, String content) {
        if (!StringUtils.hasText(content)) {
            throw new BadRequestException("Message content must not be blank");
        }
        
        String trimmedContent = content.trim();
        if (trimmedContent.length() > 5000) {
            throw new BadRequestException("Message content must not exceed 5000 characters");
        }

        Conversation conversation = conversationService.getConversationForUser(conversationId, senderId);

        User sender = conversation.getParticipants().stream()
                .filter(participant -> participant.getUser().getId().equals(senderId))
                .map(ConversationParticipant::getUser)
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("Sender is not a valid participant"));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(trimmedContent)
                .build();
        // Note: Message builder sets messageType = TEXT and status = SENT by default.

        Message savedMessage = messageRepository.save(message);

        for (ConversationParticipant participant : conversation.getParticipants()) {
            if (participant.getStatus() == ParticipantStatus.ACTIVE && !participant.getUser().getId().equals(senderId)) {
                if (!presenceService.isOnline(participant.getUser().getId())) {
                    notificationService.createNotification(
                            participant.getUser().getId(),
                            NotificationType.NEW_MESSAGE,
                            "New Message",
                            "You have received a new message.",
                            savedMessage.getId()
                    );
                } else {
                    readReceiptService.markDelivered(savedMessage.getId(), participant.getUser().getId());
                }
            }
        }

        return savedMessage;
    }

    /**
     * Retrieves active messages for a conversation chronologically.
     *
     * @param requesterId    the ID of the user requesting the messages
     * @param conversationId the ID of the conversation
     * @param pageable       pagination information
     * @return a page of non-deleted messages
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessages(UUID requesterId, UUID conversationId, Pageable pageable) {
        conversationService.getConversationForUser(conversationId, requesterId);
        
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);
        List<UUID> messageIds = messages.stream().map(Message::getId).toList();
        
        java.util.Map<UUID, List<MediaAttachment>> attachments = mediaService.getAttachmentsForMessages(requesterId, conversationId, messageIds);
        
        return messageMapper.toResponsePage(messages, attachments);
    }

    /**
     * Retrieves a specific message if the user has access to its conversation.
     *
     * @param requesterId    the ID of the user making the request
     * @param conversationId the ID of the conversation
     * @param messageId      the ID of the message
     * @return the Message entity
     * @throws ResourceNotFoundException if the message does not exist
     * @throws ForbiddenException if the message does not belong to the specified conversation
     */
    @Transactional(readOnly = true)
    public Message getMessage(UUID requesterId, UUID conversationId, UUID messageId) {
        conversationService.getConversationForUser(conversationId, requesterId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getConversation().getId().equals(conversationId)) {
            throw new ForbiddenException("Message does not belong to the specified conversation");
        }

        return message;
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageResponse(UUID requesterId, UUID conversationId, UUID messageId) {
        Message message = getMessage(requesterId, conversationId, messageId);
        java.util.Map<UUID, List<MediaAttachment>> attachments = mediaService.getAttachmentsForMessages(requesterId, conversationId, List.of(messageId));
        return messageMapper.toResponse(message, attachments.get(messageId));
    }

    /**
     * Lightweight check to see if a user is the owner (sender) of a message.
     *
     * @param userId    the ID of the user
     * @param messageId the ID of the message
     * @return true if the user sent the message, false otherwise or if message doesn't exist
     */
    @Transactional(readOnly = true)
    public boolean isMessageOwner(UUID userId, UUID messageId) {
        return messageRepository.findById(messageId)
                .map(message -> message.getSender().getId().equals(userId))
                .orElse(false);
    }

    /**
     * Updates an existing message.
     *
     * @param requesterId    the ID of the user requesting the edit
     * @param conversationId the ID of the conversation
     * @param messageId      the ID of the message to update
     * @param request        the request payload containing the new content
     * @return the updated Message entity
     */
    @Transactional
    public Message updateMessage(UUID requesterId, UUID conversationId, UUID messageId, UpdateMessageRequest request) {
        Message message = getMessage(requesterId, conversationId, messageId);

        if (!isMessageOwner(requesterId, messageId)) {
            throw new ForbiddenException("Only the original sender may edit this message");
        }

        if (message.isDeleted()) {
            throw new ConflictException("Deleted messages cannot be edited");
        }

        String content = request.content();
        if (!StringUtils.hasText(content)) {
            throw new BadRequestException("Message content must not be blank");
        }

        String trimmedContent = content.trim();
        if (trimmedContent.length() > 5000) {
            throw new BadRequestException("Message content must not exceed 5000 characters");
        }

        message.setContent(trimmedContent);
        message.markEdited();

        return messageRepository.save(message);
    }

    /**
     * Soft deletes an existing message.
     *
     * @param requesterId    the ID of the user requesting the deletion
     * @param conversationId the ID of the conversation
     * @param messageId      the ID of the message to delete
     */
    @Transactional
    public void deleteMessage(UUID requesterId, UUID conversationId, UUID messageId) {
        Message message = getMessage(requesterId, conversationId, messageId);

        if (!isMessageOwner(requesterId, messageId)) {
            throw new ForbiddenException("Only the original sender may delete this message");
        }

        if (message.isDeleted()) {
            return;
        }

        message.markDeleted();
        messageRepository.save(message);
    }
}
