package com.messaging.backend.readreceipts.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import com.messaging.backend.messaging.repository.ConversationParticipantRepository;
import com.messaging.backend.messaging.repository.MessageRepository;
import com.messaging.backend.readreceipts.entity.ReadReceipt;
import com.messaging.backend.readreceipts.mapper.ReadReceiptMapper;
import com.messaging.backend.readreceipts.repository.ReadReceiptRepository;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.dto.RedisEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReadReceiptService {

    private final ReadReceiptRepository readReceiptRepository;
    private final MessageRepository messageRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ReadReceiptMapper readReceiptMapper;
    private final RedisEventPublisher redisEventPublisher;

    public ReadReceiptService(ReadReceiptRepository readReceiptRepository,
                              MessageRepository messageRepository,
                              ConversationParticipantRepository conversationParticipantRepository,
                              UserRepository userRepository,
                              SimpMessagingTemplate messagingTemplate,
                              ReadReceiptMapper readReceiptMapper,
                              RedisEventPublisher redisEventPublisher) {
        this.readReceiptRepository = readReceiptRepository;
        this.messageRepository = messageRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.readReceiptMapper = readReceiptMapper;
        this.redisEventPublisher = redisEventPublisher;
    }

    @Transactional
    public void markDelivered(UUID messageId, UUID recipientId) {
        Message message = requireMessage(messageId);
        requireParticipant(recipientId, message.getConversation().getId());

        if (message.getSender().getId().equals(recipientId)) {
            throw new ForbiddenException("Sender cannot receive a receipt for their own message");
        }

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ReadReceipt receipt = createReceiptIfMissing(message, recipient);

        if (receipt.getDeliveredAt() == null) {
            receipt.setDeliveredAt(Instant.now());
            readReceiptRepository.save(receipt);
            broadcastReadReceipt(receipt);
        }
    }

    @Transactional
    public boolean markSeen(UUID messageId, UUID recipientId) {
        Message message = requireMessage(messageId);
        requireParticipant(recipientId, message.getConversation().getId());

        if (message.getSender().getId().equals(recipientId)) {
            throw new ForbiddenException("Sender cannot mark own message as seen");
        }

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return markSeenInternal(message, recipient, null);
    }

    @Transactional
    public int markConversationSeen(UUID conversationId, UUID recipientId) {
        requireParticipant(recipientId, conversationId);
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        int newSeen = 0;
        int pageNumber = 0;
        int pageSize = 500;
        Page<Message> page;

        do {
            page = messageRepository.findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(
                    conversationId, PageRequest.of(pageNumber, pageSize));

            List<Message> eligibleMessages = page.getContent().stream()
                    .filter(m -> !m.getSender().getId().equals(recipientId))
                    .collect(Collectors.toList());

            if (!eligibleMessages.isEmpty()) {
                List<UUID> messageIds = eligibleMessages.stream().map(Message::getId).collect(Collectors.toList());
                List<ReadReceipt> existingReceipts = readReceiptRepository.findByMessageIdIn(messageIds).stream()
                        .filter(r -> r.getUser().getId().equals(recipientId))
                        .collect(Collectors.toList());

                for (Message message : eligibleMessages) {
                    ReadReceipt receipt = existingReceipts.stream()
                            .filter(r -> r.getMessage().getId().equals(message.getId()))
                            .findFirst()
                            .orElse(null);

                    if (markSeenInternal(message, recipient, receipt)) {
                        newSeen++;
                    }
                }
            }
            pageNumber++;
        } while (page.hasNext());

        return newSeen;
    }

    @Transactional(readOnly = true)
    public List<ReadReceipt> getReceipts(UUID messageId, UUID requesterId) {
        Message message = requireMessage(messageId);
        requireParticipant(requesterId, message.getConversation().getId());
        return readReceiptRepository.findByMessageId(messageId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientId) {
        return readReceiptRepository.countByUserIdAndSeenAtIsNull(recipientId);
    }

    // --- Private Helpers ---

    private boolean markSeenInternal(Message message, User recipient, ReadReceipt receipt) {
        if (receipt == null) {
            receipt = createReceiptIfMissing(message, recipient);
        }

        boolean newlySeen = false;
        if (receipt.getSeenAt() == null) {
            receipt.setSeenAt(Instant.now());
            newlySeen = true;
        }
        
        if (receipt.getDeliveredAt() == null) {
            receipt.setDeliveredAt(receipt.getSeenAt());
        }

        if (newlySeen) {
            readReceiptRepository.save(receipt);
            broadcastReadReceipt(receipt);
        }

        return newlySeen;
    }

    private ReadReceipt createReceiptIfMissing(Message message, User recipient) {
        return readReceiptRepository.findByMessageIdAndUserId(message.getId(), recipient.getId())
                .orElseGet(() -> {
                    ReadReceipt newReceipt = ReadReceipt.builder()
                            .message(message)
                            .user(recipient)
                            .build();
                    return readReceiptRepository.save(newReceipt);
                });
    }

    private Message requireMessage(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
    }

    private void requireParticipant(UUID userId, UUID conversationId) {
        boolean isActive = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId)
                .map(p -> p.getStatus() == ParticipantStatus.ACTIVE)
                .orElse(false);

        if (!isActive) {
            throw new ForbiddenException("Must be an active participant to perform this action");
        }
    }

    private ReadReceipt requireReceipt(UUID messageId, UUID userId) {
        return readReceiptRepository.findByMessageIdAndUserId(messageId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Read receipt not found"));
    }

    private void broadcastReadReceipt(ReadReceipt receipt) {
        try {
            var response = readReceiptMapper.toSocketResponse(receipt);
            messagingTemplate.convertAndSend(WebSocketDestinations.READ_RECEIPT_TOPIC, response);
            
            redisEventPublisher.publish(PubSubChannels.READ_RECEIPT_CHANNEL, 
                new RedisEvent(null, "READ_RECEIPT", null, response, null));
        } catch (Exception ex) {
            log.error("Failed to broadcast read receipt for messageId: {}", receipt.getMessage().getId(), ex);
        }
    }
}
