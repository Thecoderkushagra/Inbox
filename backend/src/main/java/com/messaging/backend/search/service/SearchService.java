package com.messaging.backend.search.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.enums.UserStatus;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.friendships.entity.Friendship;
import com.messaging.backend.friendships.enums.FriendshipStatus;
import com.messaging.backend.friendships.repository.FriendshipRepository;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.enums.ConversationType;
import com.messaging.backend.messaging.enums.ParticipantStatus;
import com.messaging.backend.messaging.repository.ConversationParticipantRepository;
import com.messaging.backend.messaging.repository.ConversationRepository;
import com.messaging.backend.messaging.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.messaging.backend.cache.constants.CacheConstants;
import org.springframework.cache.annotation.Cacheable;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final MessageRepository messageRepository;

    public SearchService(UserRepository userRepository,
                         FriendshipRepository friendshipRepository,
                         ConversationRepository conversationRepository,
                         ConversationParticipantRepository conversationParticipantRepository,
                         MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.conversationRepository = conversationRepository;
        this.conversationParticipantRepository = conversationParticipantRepository;
        this.messageRepository = messageRepository;
    }

    public record SuggestionResult(java.util.List<User> users, java.util.List<Conversation> groups) {}

    /**
     * Gets real-time search suggestions.
     * Reuses existing logic to fetch top 5 users and top 5 groups matching the keyword.
     */
    public SuggestionResult getSuggestions(UUID currentUserId, String keyword) {
        String cleanKeyword = requireKeyword(keyword);
        Pageable limit = PageRequest.of(0, 5);

        java.util.List<User> users = searchUsers(currentUserId, cleanKeyword, limit).getContent();
        java.util.List<Conversation> groups = searchGroups(currentUserId, cleanKeyword, limit).getContent();

        return new SuggestionResult(users, groups);
    }

    /**
     * Searches for active users by username, email, or display name.
     * Excludes the authenticated user from results.
     */
    @Cacheable(value = CacheConstants.SEARCH_CACHE, key = "'search:users:' + #currentUserId + ':' + #keyword + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<User> searchUsers(UUID currentUserId, String keyword, Pageable pageable) {
        String cleanKeyword = requireKeyword(keyword);
        return userRepository.searchUsersByKeywordAndStatus(currentUserId, cleanKeyword, UserStatus.ACTIVE, pageable);
    }

    /**
     * Searches for friends of the authenticated user by username, email, or display name.
     * Only considers accepted friendships and excludes the authenticated user.
     */
    public Page<User> searchFriends(UUID currentUserId, String keyword, Pageable pageable) {
        String cleanKeyword = requireKeyword(keyword);
        
        Page<Friendship> friendships = friendshipRepository.searchAcceptedFriendshipsByKeyword(
                currentUserId, FriendshipStatus.ACCEPTED, cleanKeyword, pageable);
                
        return friendships.map(friendship -> getFriendFromFriendship(friendship, currentUserId));
    }

    /**
     * Searches for conversations (DM or GROUP) the user is an active participant in by title.
     */
    @Cacheable(value = CacheConstants.SEARCH_CACHE, key = "'search:conversations:' + #currentUserId + ':' + #keyword + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<Conversation> searchConversations(UUID currentUserId, String keyword, Pageable pageable) {
        String cleanKeyword = requireKeyword(keyword);
        return conversationRepository.searchConversationsByTitle(
                currentUserId, ParticipantStatus.ACTIVE, cleanKeyword, pageable);
    }

    /**
     * Searches for GROUP conversations the user is an active participant in by title.
     */
    @Cacheable(value = CacheConstants.SEARCH_CACHE, key = "'search:groups:' + #currentUserId + ':' + #keyword + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<Conversation> searchGroups(UUID currentUserId, String keyword, Pageable pageable) {
        String cleanKeyword = requireKeyword(keyword);
        return conversationRepository.searchConversationsByTypeAndTitle(
                currentUserId, ParticipantStatus.ACTIVE, ConversationType.GROUP, cleanKeyword, pageable);
    }

    /**
     * Searches for active messages within a specific conversation by keyword.
     * Verifies that the conversation exists and the user is an active participant.
     */
    public Page<Message> searchMessages(UUID currentUserId, UUID conversationId, String keyword, Pageable pageable) {
        String cleanKeyword = requireKeyword(keyword);
        requireConversation(conversationId);
        requireMembership(currentUserId, conversationId);

        return messageRepository.findByConversationIdAndDeletedFalseAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
                conversationId, cleanKeyword, pageable);
    }

    // --- Centralized Validation Helpers ---

    private String requireKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("Search keyword cannot be empty or whitespace");
        }
        return keyword.trim();
    }

    private void requireConversation(UUID conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ResourceNotFoundException("Conversation not found");
        }
    }

    private void requireMembership(UUID userId, UUID conversationId) {
        boolean isActiveParticipant = conversationParticipantRepository.findByConversationIdAndUserId(conversationId, userId)
                .map(p -> p.getStatus() == ParticipantStatus.ACTIVE)
                .orElse(false);

        if (!isActiveParticipant) {
            throw new ForbiddenException("Must be an active participant to perform this action");
        }
    }

    // --- Helper Methods ---

    private User getFriendFromFriendship(Friendship friendship, UUID currentUserId) {
        return friendship.getRequester().getId().equals(currentUserId) ? friendship.getAddressee() : friendship.getRequester();
    }
}
