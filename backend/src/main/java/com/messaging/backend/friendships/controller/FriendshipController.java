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

@RestController
@RequestMapping("/api/v1/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final FriendshipMapper friendshipMapper;

    public FriendshipController(FriendshipService friendshipService, FriendshipMapper friendshipMapper) {
        this.friendshipService = friendshipService;
        this.friendshipMapper = friendshipMapper;
    }

    @PostMapping("/request")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> sendFriendRequest(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody SendFriendRequest request) {

        Friendship friendship = friendshipService.sendFriendRequest(currentUser.getId(), request.addresseeId());
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Friend request sent successfully", response));
    }

    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> acceptFriendRequest(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String friendshipId) {

        Friendship friendship = friendshipService.acceptFriendRequest(friendshipId, currentUser.getId());
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.ok(SuccessResponse.success("Friend request accepted", response));
    }

    @PostMapping("/{friendshipId}/reject")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> rejectFriendRequest(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String friendshipId) {

        Friendship friendship = friendshipService.rejectFriendRequest(friendshipId, currentUser.getId());
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.ok(SuccessResponse.success("Friend request rejected", response));
    }

    @PostMapping("/{friendshipId}/block")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> blockUser(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String friendshipId) {

        Friendship friendship = friendshipService.blockUser(friendshipId, currentUser.getId());
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.ok(SuccessResponse.success("User blocked", response));
    }

    @GetMapping("/{friendshipId}")
    public ResponseEntity<SuccessResponse<FriendshipResponse>> getFriendship(@PathVariable String friendshipId) {
        Friendship friendship = friendshipService.getFriendship(friendshipId);
        FriendshipResponse response = friendshipMapper.toResponse(friendship);

        return ResponseEntity.ok(SuccessResponse.success("Friendship retrieved successfully", response));
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<SuccessResponse<List<FriendshipResponse>>> getIncomingRequests(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<Friendship> requests = friendshipService.getIncomingRequests(currentUser.getId());
        List<FriendshipResponse> response = friendshipMapper.toResponseList(requests);

        return ResponseEntity.ok(SuccessResponse.success("Incoming friend requests retrieved", response));
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<SuccessResponse<List<FriendshipResponse>>> getOutgoingRequests(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<Friendship> requests = friendshipService.getOutgoingRequests(currentUser.getId());
        List<FriendshipResponse> response = friendshipMapper.toResponseList(requests);

        return ResponseEntity.ok(SuccessResponse.success("Outgoing friend requests retrieved", response));
    }

    @GetMapping("/friends")
    public ResponseEntity<SuccessResponse<List<FriendshipResponse>>> getFriends(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<Friendship> friends = friendshipService.getFriends(currentUser.getId());
        List<FriendshipResponse> response = friendshipMapper.toResponseList(friends);

        return ResponseEntity.ok(SuccessResponse.success("Friends retrieved successfully", response));
    }
}
