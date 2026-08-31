package com.messaging.backend.readreceipts.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.repository.ConversationRepository;
import com.messaging.backend.messaging.repository.MessageRepository;
import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.dto.RedisEvent;
import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.readreceipts.entity.ReadReceipt;
import com.messaging.backend.readreceipts.mapper.ReadReceiptMapper;
import com.messaging.backend.readreceipts.repository.ReadReceiptRepository;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReadReceiptService {

    private final ReadReceiptRepository readReceiptRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ReadReceiptMapper readReceiptMapper;
    private final RedisEventPublisher redisEventPublisher;

    public ReadReceiptService(ReadReceiptRepository readReceiptRepository,
                              MessageRepository messageRepository,
                              ConversationRepository conversationRepository,
                              UserRepository userRepository,
                              SimpMessagingTemplate messagingTemplate,
                              ReadReceiptMapper readReceiptMapper,
                              RedisEventPublisher redisEventPublisher) {
        this.readReceiptRepository = readReceiptRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.readReceiptMapper = readReceiptMapper;
        this.redisEventPublisher = redisEventPublisher;
    }

    @Transactional
    public void markDelivered(String messageId, String recipientId) {
        Message message = requireMessage(messageId);
        String conversationId = getConversationId(message);
        requireParticipant(recipientId, conversationId);

        String senderId = message.getSenderId();

        if (senderId != null && senderId.equals(recipientId)) {
            return; // Benign no-op for sender's own message
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
    public boolean markSeen(String messageId, String recipientId) {
        Message message = requireMessage(messageId);
        String conversationId = getConversationId(message);
        requireParticipant(recipientId, conversationId);

        String senderId = message.getSenderId();

        if (senderId != null && senderId.equals(recipientId)) {
            return false; // Benign no-op for sender's own message
        }

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return markSeenInternal(message, recipient, null);
    }

    @Transactional
    public int markConversationSeen(String conversationId, String recipientId) {
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
                    .filter(m -> {
                        String senderId = m.getSenderId();
                        return senderId != null && !senderId.equals(recipientId);
                    })
                    .collect(Collectors.toList());

            if (!eligibleMessages.isEmpty()) {
                List<String> messageIds = eligibleMessages.stream().map(Message::getId).collect(Collectors.toList());
                List<ReadReceipt> existingReceipts = readReceiptRepository.findByMessageIdIn(messageIds).stream()
                        .filter(r -> recipientId.equals(r.getUserId()))
                        .collect(Collectors.toList());

                for (Message msg : eligibleMessages) {
                    ReadReceipt existing = existingReceipts.stream()
                            .filter(r -> msg.getId().equals(r.getMessageId()))
                            .findFirst()
                            .orElse(null);

                    if (markSeenInternal(msg, recipient, existing)) {
                        newSeen++;
                    }
                }
            }
            pageNumber++;
        } while (page.hasNext());

        return newSeen;
    }

    @Transactional(readOnly = true)
    public List<ReadReceipt> getReceipts(String messageId, String requesterId) {
        return getReceiptsForMessage(requesterId, messageId);
    }

    @Transactional(readOnly = true)
    public List<ReadReceipt> getReceiptsForMessage(String requesterId, String messageId) {
        Message message = requireMessage(messageId);
        String conversationId = getConversationId(message);
        requireParticipant(requesterId, conversationId);
        return readReceiptRepository.findByMessageId(messageId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String recipientId) {
        return readReceiptRepository.countByUserIdAndSeenAtIsNull(recipientId);
    }

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
        String convId = getConversationId(message);
        return readReceiptRepository.findByMessageIdAndUserId(message.getId(), recipient.getId())
                .orElseGet(() -> {
                    ReadReceipt newReceipt = ReadReceipt.builder()
                            .messageId(message.getId())
                            .conversationId(convId)
                            .userId(recipient.getId())
                            .build();
                    return readReceiptRepository.save(newReceipt);
                });
    }

    private Message requireMessage(String messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
    }

    private String getConversationId(Message message) {
        return message.getConversationId();
    }

    private void requireParticipant(String userId, String conversationId) {
        if (conversationId == null) {
            throw new BadRequestException("Invalid conversation reference");
        }
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isParticipant = conversation.getParticipantUserIds().contains(userId) ||
                conversation.getParticipants().stream().anyMatch(p -> userId.equals(p.getUserId()));

        if (!isParticipant) {
            throw new ForbiddenException("Must be an active participant to perform this action");
        }
    }

    private void broadcastReadReceipt(ReadReceipt receipt) {
        try {
            var response = readReceiptMapper.toSocketResponse(receipt);
            messagingTemplate.convertAndSend(WebSocketDestinations.READ_RECEIPT_TOPIC, response);

            redisEventPublisher.publish(PubSubChannels.READ_RECEIPT_CHANNEL,
                    new RedisEvent(null, "READ_RECEIPT", null, response, null));
        } catch (Exception ex) {
            log.error("Failed to broadcast read receipt", ex);
        }
    }
}
