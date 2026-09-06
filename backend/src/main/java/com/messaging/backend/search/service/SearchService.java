package com.messaging.backend.search.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.UserStatus;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.cache.constants.CacheConstants;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.enums.ConversationType;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import com.messaging.backend.messaging.repository.ConversationRepository;
import com.messaging.backend.messaging.repository.MessageRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public SearchService(UserRepository userRepository,
                         ConversationRepository conversationRepository,
                         MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public record SuggestionResult(List<User> users, List<Conversation> groups) {}

    /**
     * Gets real-time search suggestions.
     */
    public SuggestionResult getSuggestions(String currentUserId, String keyword) {
        String cleanKeyword = requireKeyword(keyword);
        Pageable limit = PageRequest.of(0, 5);

        List<User> users = searchUsers(currentUserId, cleanKeyword, limit).getContent();
        List<Conversation> groups = searchGroups(currentUserId, cleanKeyword, limit).getContent();

        return new SuggestionResult(users, groups);
    }

    /**
     * Searches for active users by username or email.
     * If keyword is empty, returns all active users.
     */
    public Page<User> searchUsers(String currentUserId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAllByStatusAndIdNot(currentUserId, UserStatus.ACTIVE, pageable);
        }
        String cleanKeyword = keyword.trim();
        return userRepository.searchUsersByKeywordAndStatus(currentUserId, cleanKeyword, UserStatus.ACTIVE, pageable);
    }

    /**
     * Searches for conversations the user is an active participant in by title.
     */
    public Page<Conversation> searchConversations(String currentUserId, String keyword, Pageable pageable) {
        String cleanKeyword = requireKeyword(keyword);
        return conversationRepository.searchConversationsByTitle(
                currentUserId, ParticipantStatus.ACTIVE, cleanKeyword, pageable);
    }

    /**
     * Searches for GROUP conversations the user is an active participant in by title.
     */
    public Page<Conversation> searchGroups(String currentUserId, String keyword, Pageable pageable) {
        String cleanKeyword = requireKeyword(keyword);
        return conversationRepository.searchConversationsByTypeAndTitle(
                currentUserId, ParticipantStatus.ACTIVE, ConversationType.GROUP, cleanKeyword, pageable);
    }

    /**
     * Searches for active messages within a specific conversation by keyword.
     */
    public Page<Message> searchMessages(String currentUserId, String conversationId, String keyword, Pageable pageable) {
        String cleanKeyword = requireKeyword(keyword);
        Conversation conversation = requireConversation(conversationId);
        requireMembership(currentUserId, conversation);

        return messageRepository.findByConversationIdAndDeletedFalseAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
                conversationId, cleanKeyword, pageable);
    }

    private String requireKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("Search keyword cannot be empty or whitespace");
        }
        return keyword.trim();
    }

    private Conversation requireConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
    }

    private void requireMembership(String userId, Conversation conversation) {
        boolean isParticipant = conversation.getParticipantUserIds().contains(userId) ||
                conversation.getParticipants().stream().anyMatch(p -> userId.equals(p.getUserId()));

        if (!isParticipant) {
            throw new ForbiddenException("Must be an active participant to perform this action");
        }
    }
}
