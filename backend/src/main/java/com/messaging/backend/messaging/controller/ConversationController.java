package com.messaging.backend.messaging.controller;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.auth.service.AuthService;
import com.messaging.backend.common.dto.pagination.PaginationRequest;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.messaging.dto.request.CreateGroupConversationRequest;
import com.messaging.backend.messaging.dto.request.CreatePrivateConversationRequest;
import com.messaging.backend.messaging.dto.response.ConversationParticipantResponse;
import com.messaging.backend.messaging.dto.response.ConversationResponse;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import com.messaging.backend.messaging.mapper.ConversationMapper;
import com.messaging.backend.messaging.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing conversations.
 *
 * <p>Purpose:
 * Exposes endpoints for creating private and group conversations,
 * retrieving conversation details, and fetching conversation participants.
 */
@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final ConversationMapper conversationMapper;
    private final AuthService authService;

    public ConversationController(ConversationService conversationService,
                                  ConversationMapper conversationMapper,
                                  AuthService authService) {
        this.conversationService = conversationService;
        this.conversationMapper = conversationMapper;
        this.authService = authService;
    }

    /**
     * Creates a private (direct) conversation with another user.
     *
     * @param authenticatedUser the currently authenticated user
     * @param request the request payload containing the recipient ID
     * @return 201 Created with the conversation response
     */
    @PostMapping("/private")
    public ResponseEntity<SuccessResponse<ConversationResponse>> createPrivateConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreatePrivateConversationRequest request) {

        User creator = authService.getUserById(authenticatedUser.getId());
        User recipient = authService.getUserById(request.recipientId());

        Conversation conversation = conversationService.createPrivateConversation(creator, recipient);
        ConversationResponse response = conversationMapper.toConversationResponse(conversation);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Private conversation created successfully", response));
    }

    /**
     * Creates a new group conversation.
     *
     * @param authenticatedUser the currently authenticated user
     * @param request the request payload containing the group name
     * @return 201 Created with the conversation response
     */
    @PostMapping("/group")
    public ResponseEntity<SuccessResponse<ConversationResponse>> createGroupConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateGroupConversationRequest request) {

        User owner = authService.getUserById(authenticatedUser.getId());

        Conversation conversation = conversationService.createGroupConversation(owner, request.name());
        ConversationResponse response = conversationMapper.toConversationResponse(conversation);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Group conversation created successfully", response));
    }

    /**
     * Retrieves conversation details for an authorized user.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId the UUID of the conversation
     * @return 200 OK with the conversation response
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<SuccessResponse<ConversationResponse>> getConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId) {

        Conversation conversation = conversationService.getConversationForUser(conversationId, authenticatedUser.getId());
        ConversationResponse response = conversationMapper.toConversationResponse(conversation);

        return ResponseEntity.ok(SuccessResponse.success("Conversation retrieved successfully", response));
    }

    /**
     * Retrieves participants for a specific conversation.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId the UUID of the conversation
     * @param paginationRequest the pagination parameters
     * @return 200 OK with the list of participants
     */
    @GetMapping("/{conversationId}/participants")
    public ResponseEntity<SuccessResponse<List<ConversationParticipantResponse>>> getParticipants(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId,
            @Valid PaginationRequest paginationRequest) {

        // Ensure the caller has access before listing participants
        conversationService.getConversationForUser(conversationId, authenticatedUser.getId());

        Page<ConversationParticipant> participantsPage = conversationService.getParticipants(conversationId, paginationRequest.toPageable());
        List<ConversationParticipantResponse> responses = conversationMapper.toParticipantResponseList(participantsPage.getContent());

        return ResponseEntity.ok(SuccessResponse.success("Participants retrieved successfully", responses));
    }

    /**
     * Adds a new participant to a conversation.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId the UUID of the conversation
     * @param request the request payload containing the new participant's user ID
     * @return 201 Created with the participant response
     */
    @PostMapping("/{conversationId}/participants")
    public ResponseEntity<SuccessResponse<ConversationParticipantResponse>> addParticipant(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId,
            @Valid @RequestBody com.messaging.backend.messaging.dto.request.AddParticipantRequest request) {

        User newParticipant = authService.getUserById(request.userId());

        ConversationParticipant participant = conversationService.addParticipant(
                authenticatedUser.getId(), conversationId, newParticipant);

        ConversationParticipantResponse response = conversationMapper.toParticipantResponse(participant);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Participant added successfully", response));
    }

    /**
     * Removes a participant from a conversation.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId the UUID of the conversation
     * @param userId the UUID of the user to remove
     * @return 200 OK
     */
    @DeleteMapping("/{conversationId}/participants/{userId}")
    public ResponseEntity<SuccessResponse<Void>> removeParticipant(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId,
            @PathVariable UUID userId) {

        conversationService.removeParticipant(authenticatedUser.getId(), conversationId, userId);

        return ResponseEntity.ok(SuccessResponse.success("Participant removed successfully", null));
    }

    /**
     * Allows the authenticated user to leave a conversation.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId the UUID of the conversation
     * @return 200 OK
     */
    @PostMapping("/{conversationId}/leave")
    public ResponseEntity<SuccessResponse<Void>> leaveConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId) {

        conversationService.leaveConversation(authenticatedUser.getId(), conversationId);

        return ResponseEntity.ok(SuccessResponse.success("Successfully left the conversation", null));
    }

    /**
     * Updates a conversation's settings.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId the UUID of the conversation
     * @param request the update payload
     * @return 200 OK with the updated conversation response
     */
    @PutMapping("/{conversationId}")
    public ResponseEntity<SuccessResponse<ConversationResponse>> updateConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId,
            @Valid @RequestBody com.messaging.backend.messaging.dto.request.UpdateConversationRequest request) {

        Conversation conversation = conversationService.updateConversation(
                authenticatedUser.getId(), conversationId, request.name());

        ConversationResponse response = conversationMapper.toConversationResponse(conversation);

        return ResponseEntity.ok(SuccessResponse.success("Conversation updated successfully", response));
    }

    /**
     * Archives a conversation.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId the UUID of the conversation
     * @return 200 OK
     */
    @PostMapping("/{conversationId}/archive")
    public ResponseEntity<SuccessResponse<Void>> archiveConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId) {

        conversationService.archiveConversation(authenticatedUser.getId(), conversationId);

        return ResponseEntity.ok(SuccessResponse.success("Conversation archived successfully", null));
    }

    /**
     * Unarchives a conversation.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId the UUID of the conversation
     * @return 200 OK
     */
    @PostMapping("/{conversationId}/unarchive")
    public ResponseEntity<SuccessResponse<Void>> unarchiveConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId) {

        conversationService.unarchiveConversation(authenticatedUser.getId(), conversationId);

        return ResponseEntity.ok(SuccessResponse.success("Conversation unarchived successfully", null));
    }
}
