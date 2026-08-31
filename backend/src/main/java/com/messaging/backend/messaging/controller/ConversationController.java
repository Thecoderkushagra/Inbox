package com.messaging.backend.messaging.controller;

import com.messaging.backend.auth.entity.User;
import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.auth.service.AuthService;
import com.messaging.backend.common.dto.pagination.PaginationRequest;
import com.messaging.backend.common.dto.response.PageResponse;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.messaging.dto.request.AddParticipantRequest;
import com.messaging.backend.messaging.dto.request.CreateGroupConversationRequest;
import com.messaging.backend.messaging.dto.request.CreatePrivateConversationRequest;
import com.messaging.backend.messaging.dto.request.UpdateConversationRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing conversations.
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
     * Retrieves all conversations for the authenticated user with pagination.
     */
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponse<ConversationResponse>>> getUserConversations(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid PaginationRequest paginationRequest) {

        Page<Conversation> conversations = conversationService.getUserConversations(
                authenticatedUser.getId(), paginationRequest.toPageable());
        Page<ConversationResponse> responsePage = conversations.map(conversationMapper::toConversationResponse);

        return ResponseEntity.ok(SuccessResponse.success(PageResponse.of(responsePage)));
    }

    /**
     * Creates a private (direct) conversation with another user.
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
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<SuccessResponse<ConversationResponse>> getConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId) {

        Conversation conversation = conversationService.getConversationForUser(conversationId, authenticatedUser.getId());
        ConversationResponse response = conversationMapper.toConversationResponse(conversation);

        return ResponseEntity.ok(SuccessResponse.success("Conversation retrieved successfully", response));
    }

    /**
     * Retrieves participants for a specific conversation.
     */
    @GetMapping("/{conversationId}/participants")
    public ResponseEntity<SuccessResponse<List<ConversationParticipantResponse>>> getParticipants(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId) {

        Conversation conversation = conversationService.getConversationForUser(conversationId, authenticatedUser.getId());
        List<ConversationParticipantResponse> responses = conversationMapper.toParticipantResponseList(
                conversation.getParticipants().stream().toList());

        return ResponseEntity.ok(SuccessResponse.success("Participants retrieved successfully", responses));
    }

    /**
     * Adds a new participant to a conversation.
     */
    @PostMapping("/{conversationId}/participants")
    public ResponseEntity<SuccessResponse<ConversationParticipantResponse>> addParticipant(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId,
            @Valid @RequestBody AddParticipantRequest request) {

        User newParticipant = authService.getUserById(request.userId());

        ConversationParticipant participant = conversationService.addParticipant(
                authenticatedUser.getId(), conversationId, newParticipant);

        ConversationParticipantResponse response = conversationMapper.toParticipantResponse(participant);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Participant added successfully", response));
    }

    /**
     * Removes a participant from a conversation.
     */
    @DeleteMapping("/{conversationId}/participants/{userId}")
    public ResponseEntity<SuccessResponse<Void>> removeParticipant(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId,
            @PathVariable String userId) {

        conversationService.removeParticipant(authenticatedUser.getId(), conversationId, userId);

        return ResponseEntity.ok(SuccessResponse.success("Participant removed successfully", null));
    }

    /**
     * Allows the authenticated user to leave a conversation.
     */
    @PostMapping("/{conversationId}/leave")
    public ResponseEntity<SuccessResponse<Void>> leaveConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId) {

        conversationService.leaveConversation(authenticatedUser.getId(), conversationId);

        return ResponseEntity.ok(SuccessResponse.success("Successfully left the conversation", null));
    }

    /**
     * Updates a conversation's settings.
     */
    @PutMapping("/{conversationId}")
    public ResponseEntity<SuccessResponse<ConversationResponse>> updateConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId,
            @Valid @RequestBody UpdateConversationRequest request) {

        Conversation conversation = conversationService.updateConversation(
                authenticatedUser.getId(), conversationId, request.name());

        ConversationResponse response = conversationMapper.toConversationResponse(conversation);

        return ResponseEntity.ok(SuccessResponse.success("Conversation updated successfully", response));
    }

    /**
     * Archives a conversation.
     */
    @PostMapping("/{conversationId}/archive")
    public ResponseEntity<SuccessResponse<Void>> archiveConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId) {

        conversationService.archiveConversation(authenticatedUser.getId(), conversationId);

        return ResponseEntity.ok(SuccessResponse.success("Conversation archived successfully", null));
    }

    /**
     * Unarchives a conversation.
     */
    @PostMapping("/{conversationId}/unarchive")
    public ResponseEntity<SuccessResponse<Void>> unarchiveConversation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable String conversationId) {

        conversationService.unarchiveConversation(authenticatedUser.getId(), conversationId);

        return ResponseEntity.ok(SuccessResponse.success("Conversation unarchived successfully", null));
    }
}
