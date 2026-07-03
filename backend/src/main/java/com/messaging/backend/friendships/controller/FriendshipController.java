package com.messaging.backend.friendships.controller;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.friendships.dto.request.SendFriendRequest;
import com.messaging.backend.friendships.dto.response.FriendshipResponse;
import com.messaging.backend.friendships.entity.Friendship;
import com.messaging.backend.friendships.mapper.FriendshipMapper;
import com.messaging.backend.friendships.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing friendship operations.
 */
@RestController
@RequestMapping("/api/v1/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final FriendshipMapper friendshipMapper;

    public FriendshipController(FriendshipService friendshipService, FriendshipMapper friendshipMapper) {
        this.friendshipService = friendshipService;
        this.friendshipMapper = friendshipMapper;
    }

    /**
     * Send a new friend request.
     *
     * @param request the request payload containing the addressee ID
     * @param currentUser the authenticated user sending the request
     * @return 201 Created with the created friendship
     */
    @PostMapping("/request")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> sendRequest(
            @Valid @RequestBody SendFriendRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        Friendship friendship = friendshipService.sendFriendRequest(currentUser.getId(), request.addresseeId());
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success(response));
    }

    /**
     * Accept a pending friend request.
     *
     * @param friendshipId the ID of the friendship to accept
     * @param currentUser the authenticated user accepting the request
     * @return 200 OK with the updated friendship
     */
    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> acceptRequest(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        Friendship friendship = friendshipService.acceptFriendRequest(friendshipId, currentUser.getId());
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.ok(SuccessResponse.success(response));
    }

    /**
     * Reject a pending friend request.
     *
     * @param friendshipId the ID of the friendship to reject
     * @param currentUser the authenticated user rejecting the request
     * @return 200 OK with the updated friendship
     */
    @PostMapping("/{friendshipId}/reject")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> rejectRequest(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        Friendship friendship = friendshipService.rejectFriendRequest(friendshipId, currentUser.getId());
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.ok(SuccessResponse.success(response));
    }

    /**
     * Block a user using an existing friendship.
     *
     * @param friendshipId the ID of the friendship to block
     * @param currentUser the authenticated user blocking the other user
     * @return 200 OK with the updated friendship
     */
    @PostMapping("/{friendshipId}/block")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> blockUser(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        Friendship friendship = friendshipService.blockUser(friendshipId, currentUser.getId());
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.ok(SuccessResponse.success(response));
    }

    /**
     * Retrieve a specific friendship by ID.
     *
     * @param friendshipId the ID of the friendship to retrieve
     * @return 200 OK with the friendship details
     */
    @GetMapping("/{friendshipId}")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> getFriendship(@PathVariable UUID friendshipId) {
        Friendship friendship = friendshipService.getFriendship(friendshipId);
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.ok(SuccessResponse.success(response));
    }

    /**
     * Retrieves all incoming pending friend requests for the authenticated user.
     *
     * @param currentUser the authenticated user
     * @return 200 OK with the list of incoming requests
     */
    @GetMapping("/incoming")
    public ResponseEntity<SuccessResponse<List<FriendshipResponse>>> getIncomingRequests(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        List<Friendship> incomingRequests = friendshipService.getIncomingRequests(currentUser.getId());
        List<FriendshipResponse> response = friendshipMapper.toResponseList(incomingRequests);

        return ResponseEntity.ok(SuccessResponse.success(response));
    }

    /**
     * Retrieves all outgoing pending friend requests from the authenticated user.
     *
     * @param currentUser the authenticated user
     * @return 200 OK with the list of outgoing requests
     */
    @GetMapping("/outgoing")
    public ResponseEntity<SuccessResponse<List<FriendshipResponse>>> getOutgoingRequests(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        List<Friendship> outgoingRequests = friendshipService.getOutgoingRequests(currentUser.getId());
        List<FriendshipResponse> response = friendshipMapper.toResponseList(outgoingRequests);

        return ResponseEntity.ok(SuccessResponse.success(response));
    }

    /**
     * Retrieves all accepted friendships for the authenticated user.
     *
     * @param currentUser the authenticated user
     * @return 200 OK with the list of friends
     */
    @GetMapping
    public ResponseEntity<SuccessResponse<List<FriendshipResponse>>> getFriends(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        List<Friendship> friends = friendshipService.getFriends(currentUser.getId());
        List<FriendshipResponse> response = friendshipMapper.toResponseList(friends);

        return ResponseEntity.ok(SuccessResponse.success(response));
    }

}
