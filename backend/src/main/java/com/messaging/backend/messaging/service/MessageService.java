package com.messaging.backend.messaging.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.media.entity.MediaAttachment;
import com.messaging.backend.media.service.MediaService;
import com.messaging.backend.messaging.dto.request.UpdateMessageRequest;
import com.messaging.backend.messaging.dto.response.MessageResponse;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import com.messaging.backend.messaging.mapper.MessageMapper;
import com.messaging.backend.messaging.repository.ConversationRepository;
import com.messaging.backend.messaging.repository.MessageRepository;
import com.messaging.backend.notifications.enums.NotificationType;
import com.messaging.backend.notifications.service.NotificationService;
import com.messaging.backend.presence.service.PresenceService;
import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.dto.RedisEvent;
import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.readreceipts.service.ReadReceiptService;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import com.messaging.backend.websocket.dto.response.MessageSocketResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service for handling messaging business logic in MongoDB.
 */
@Slf4j
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PresenceService presenceService;
    private final MediaService mediaService;
    private final MessageMapper messageMapper;
    private final ReadReceiptService readReceiptService;
    private final RedisEventPublisher redisEventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageService(MessageRepository messageRepository,
                          ConversationRepository conversationRepository,
                          ConversationService conversationService,
                          UserRepository userRepository,
                          NotificationService notificationService,
                          PresenceService presenceService,
                          MediaService mediaService,
                          MessageMapper messageMapper,
                          ReadReceiptService readReceiptService,
                          RedisEventPublisher redisEventPublisher,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.conversationService = conversationService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.presenceService = presenceService;
        this.mediaService = mediaService;
        this.messageMapper = messageMapper;
        this.readReceiptService = readReceiptService;
        this.redisEventPublisher = redisEventPublisher;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a new message within a conversation.
     */
    @Transactional
    public Message sendMessage(String senderId, String conversationId, String content) {
        if (!StringUtils.hasText(content)) {
            throw new BadRequestException("Message content must not be blank");
        }

        String trimmedContent = content.trim();
        if (trimmedContent.length() > 5000) {
            throw new BadRequestException("Message content must not exceed 5000 characters");
        }

        Conversation conversation = conversationService.getConversationForUser(conversationId, senderId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ForbiddenException("Sender is not a valid user"));

        Message message = Message.builder()
                .conversationId(conversation.getId())
                .senderId(sender.getId())
                .content(trimmedContent)
                .build();

        Message savedMessage = messageRepository.save(message);

        conversation.setLastMessageAt(Instant.now());
        conversationRepository.save(conversation);

        // Process delivery and notifications for active participants
        for (ConversationParticipant participant : conversation.getParticipants()) {
            String pUserId = participant.getUserId();

            if (pUserId != null && participant.getStatus() == ParticipantStatus.ACTIVE && !pUserId.equals(senderId)) {
                if (!presenceService.isOnline(pUserId)) {
                    notificationService.createNotification(
                            pUserId,
                            NotificationType.NEW_MESSAGE,
                            "New Message from " + sender.getUsername(),
                            trimmedContent.length() > 100 ? trimmedContent.substring(0, 97) + "..." : trimmedContent,
                            savedMessage.getId()
                    );
                } else {
                    readReceiptService.markDelivered(savedMessage.getId(), pUserId);
                }
            }
        }

        MessageSocketResponse response = messageMapper.toSocketResponse(savedMessage);

        try {
            messagingTemplate.convertAndSend(WebSocketDestinations.CHAT_TOPIC + savedMessage.getConversationId(), response);
            messagingTemplate.convertAndSend(WebSocketDestinations.GLOBAL_CHAT_TOPIC, response);
            redisEventPublisher.publish(PubSubChannels.CHAT_CHANNEL,
                    new RedisEvent(null, "CHAT", null, response, null));
        } catch (Exception e) {
            log.error("Failed to broadcast chat event to WebSocket / Redis", e);
        }

        return savedMessage;
    }

    /**
     * Retrieves active messages for a conversation chronologically.
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessages(String requesterId, String conversationId, Pageable pageable) {
        conversationService.getConversationForUser(conversationId, requesterId);

        Pageable sortedAsc = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "createdAt")
        );

        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, sortedAsc);
        List<String> messageIds = messages.stream().map(Message::getId).toList();

        Map<String, List<MediaAttachment>> attachments = mediaService.getAttachmentsForMessages(requesterId, conversationId, messageIds);

        return messageMapper.toResponsePage(messages, attachments);
    }

    /**
     * Retrieves a specific message if the user has access to its conversation.
     */
    @Transactional(readOnly = true)
    public Message getMessage(String requesterId, String conversationId, String messageId) {
        conversationService.getConversationForUser(conversationId, requesterId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        String msgConvId = message.getConversationId();

        if (!conversationId.equals(msgConvId)) {
            throw new ForbiddenException("Message does not belong to the specified conversation");
        }

        return message;
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageResponse(String requesterId, String conversationId, String messageId) {
        Message message = getMessage(requesterId, conversationId, messageId);
        Map<String, List<MediaAttachment>> attachments = mediaService.getAttachmentsForMessages(requesterId, conversationId, List.of(messageId));
        return messageMapper.toResponse(message, attachments.get(messageId));
    }

    /**
     * Checks if a user is the owner (sender) of a message.
     */
    @Transactional(readOnly = true)
    public boolean isMessageOwner(String userId, String messageId) {
        return messageRepository.findById(messageId)
                .map(message -> userId.equals(message.getSenderId()))
                .orElse(false);
    }

    /**
     * Updates an existing message.
     */
    @Transactional
    public Message updateMessage(String requesterId, String conversationId, String messageId, UpdateMessageRequest request) {
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
     */
    @Transactional
    public void deleteMessage(String requesterId, String conversationId, String messageId) {
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
