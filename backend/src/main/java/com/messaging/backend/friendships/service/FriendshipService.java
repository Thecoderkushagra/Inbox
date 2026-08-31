package com.messaging.backend.friendships.service;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.repository.UserRepository;
import com.messaging.backend.cache.constants.CacheConstants;
import com.messaging.backend.common.exception.BadRequestException;
import com.messaging.backend.common.exception.ConflictException;
import com.messaging.backend.common.exception.ForbiddenException;
import com.messaging.backend.common.exception.ResourceNotFoundException;
import com.messaging.backend.friendships.dto.response.FriendshipSocketResponse;
import com.messaging.backend.friendships.entity.Friendship;
import com.messaging.backend.friendships.enums.FriendshipStatus;
import com.messaging.backend.friendships.mapper.FriendshipMapper;
import com.messaging.backend.friendships.repository.FriendshipRepository;
import com.messaging.backend.notifications.enums.NotificationType;
import com.messaging.backend.notifications.service.NotificationService;
import com.messaging.backend.pubsub.constants.PubSubChannels;
import com.messaging.backend.pubsub.dto.RedisEvent;
import com.messaging.backend.pubsub.publisher.RedisEventPublisher;
import com.messaging.backend.websocket.constant.WebSocketDestinations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FriendshipMapper friendshipMapper;
    private final NotificationService notificationService;
    private final RedisEventPublisher redisEventPublisher;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserRepository userRepository,
                             SimpMessagingTemplate messagingTemplate,
                             FriendshipMapper friendshipMapper,
                             NotificationService notificationService,
                             RedisEventPublisher redisEventPublisher) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.friendshipMapper = friendshipMapper;
        this.notificationService = notificationService;
        this.redisEventPublisher = redisEventPublisher;
    }

    @Transactional
    public Friendship sendFriendRequest(String requesterId, String addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new BadRequestException("Cannot send a friend request to yourself");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new ResourceNotFoundException("Addressee not found"));

        friendshipRepository.findByRequesterIdAndAddresseeId(requesterId, addresseeId)
                .ifPresent(f -> {
                    throw new ConflictException("Friendship relationship already exists");
                });

        friendshipRepository.findByRequesterIdAndAddresseeId(addresseeId, requesterId)
                .ifPresent(f -> {
                    throw new ConflictException("A reverse friend request already exists");
                });

        Friendship friendship = Friendship.builder()
                .requesterId(requesterId)
                .addresseeId(addresseeId)
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.PENDING)
                .build();

        Friendship saved = friendshipRepository.save(friendship);

        notificationService.createNotification(
                addresseeId,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                "New Friend Request",
                requester.getUsername() + " sent you a friend request.",
                saved.getId()
        );

        broadcastFriendshipEvent(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.FRIENDSHIPS_CACHE, allEntries = true)
    public Friendship acceptFriendRequest(String friendshipId, String currentUserId) {
        Friendship friendship = getFriendship(friendshipId);

        if (!currentUserId.equals(friendship.getAddresseeId())) {
            throw new ForbiddenException("Only the addressee can accept a friend request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ConflictException("Friend request is not pending");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setRespondedAt(Instant.now());
        Friendship saved = friendshipRepository.save(friendship);

        notificationService.createNotification(
                friendship.getRequesterId(),
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                "Friend Request Accepted",
                "Your friend request has been accepted.",
                saved.getId()
        );

        broadcastFriendshipEvent(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.FRIENDSHIPS_CACHE, allEntries = true)
    public Friendship rejectFriendRequest(String friendshipId, String currentUserId) {
        Friendship friendship = getFriendship(friendshipId);

        if (!currentUserId.equals(friendship.getAddresseeId())) {
            throw new ForbiddenException("Only the addressee can reject a friend request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ConflictException("Friend request is not pending");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendship.setRespondedAt(Instant.now());
        Friendship saved = friendshipRepository.save(friendship);
        broadcastFriendshipEvent(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.FRIENDSHIPS_CACHE, allEntries = true)
    public Friendship blockUser(String friendshipId, String currentUserId) {
        Friendship friendship = getFriendship(friendshipId);

        if (!currentUserId.equals(friendship.getRequesterId()) && !currentUserId.equals(friendship.getAddresseeId())) {
            throw new ForbiddenException("Not a participant in this friendship");
        }

        friendship.setStatus(FriendshipStatus.BLOCKED);
        friendship.setBlockedAt(Instant.now());
        Friendship saved = friendshipRepository.save(friendship);
        broadcastFriendshipEvent(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Friendship getFriendship(String friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship not found"));
    }

    @Transactional(readOnly = true)
    public List<Friendship> getIncomingRequests(String userId) {
        return friendshipRepository.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Friendship> getOutgoingRequests(String userId) {
        return friendshipRepository.findByRequesterIdAndStatus(userId, FriendshipStatus.PENDING);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.FRIENDSHIPS_CACHE, key = "'friends:' + #userId")
    public List<Friendship> getFriends(String userId) {
        return friendshipRepository.findFriendshipsByStatusAndUserId(FriendshipStatus.ACCEPTED, userId);
    }

    @Transactional(readOnly = true)
    public Page<Friendship> getFriends(String userId, Pageable pageable) {
        return friendshipRepository.findFriendshipsByStatusAndUserId(FriendshipStatus.ACCEPTED, userId, pageable);
    }

    private void broadcastFriendshipEvent(Friendship friendship) {
        try {
            FriendshipSocketResponse response = friendshipMapper.toSocketResponse(friendship);
            messagingTemplate.convertAndSend(WebSocketDestinations.FRIENDSHIP_TOPIC, response);
            redisEventPublisher.publish(PubSubChannels.CHAT_CHANNEL,
                    new RedisEvent(null, "FRIENDSHIP", null, response, null));
        } catch (Exception e) {
            log.error("Failed to broadcast friendship event", e);
        }
    }
}
