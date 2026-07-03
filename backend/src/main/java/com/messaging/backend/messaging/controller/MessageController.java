package com.messaging.backend.messaging.controller;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.common.dto.pagination.PaginationRequest;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.messaging.dto.request.SendMessageRequest;
import com.messaging.backend.messaging.dto.response.MessageResponse;
import com.messaging.backend.messaging.entity.Message;
import com.messaging.backend.messaging.mapper.MessageMapper;
import com.messaging.backend.messaging.service.MessageService;
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
import com.messaging.backend.messaging.dto.request.UpdateMessageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller for handling message-related endpoints.
 */
@RestController
@RequestMapping("/api/v1/conversations")
public class MessageController {

    private final MessageService messageService;
    private final MessageMapper messageMapper;

    public MessageController(MessageService messageService, MessageMapper messageMapper) {
        this.messageService = messageService;
        this.messageMapper = messageMapper;
    }

    /**
     * Sends a new message in a conversation.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId    the ID of the conversation
     * @param request           the message payload
     * @return 201 Created with the sent message
     */
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<SuccessResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request) {

        Message message = messageService.sendMessage(authenticatedUser.getId(), conversationId, request.content());
        MessageResponse response = messageMapper.toResponse(message, null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Message sent successfully", response));
    }

    /**
     * Retrieves paginated messages for a conversation.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId    the ID of the conversation
     * @param paginationRequest pagination parameters
     * @return 200 OK with a page of messages
     */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<SuccessResponse<Page<MessageResponse>>> getMessages(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId,
            @Valid PaginationRequest paginationRequest) {

        Page<MessageResponse> responsePage = messageService.getConversationMessages(
                authenticatedUser.getId(), conversationId, paginationRequest.toPageable());

        return ResponseEntity.ok(SuccessResponse.success("Messages retrieved successfully", responsePage));
    }

    /**
     * Retrieves a specific message.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId    the ID of the conversation
     * @param messageId         the ID of the message
     * @return 200 OK with the message
     */
    @GetMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<SuccessResponse<MessageResponse>> getMessage(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId) {

        MessageResponse response = messageService.getMessageResponse(authenticatedUser.getId(), conversationId, messageId);

        return ResponseEntity.ok(SuccessResponse.success("Message retrieved successfully", response));
    }

    /**
     * Updates an existing message.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId    the ID of the conversation
     * @param messageId         the ID of the message to update
     * @param request           the update payload
     * @return 200 OK with the updated message
     */
    @PutMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<SuccessResponse<MessageResponse>> updateMessage(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId,
            @Valid @RequestBody UpdateMessageRequest request) {

        Message message = messageService.updateMessage(authenticatedUser.getId(), conversationId, messageId, request);
        MessageResponse response = messageMapper.toResponse(message, null);

        return ResponseEntity.ok(SuccessResponse.success("Message updated successfully", response));
    }
    /**
     * Soft deletes a specific message.
     *
     * @param authenticatedUser the currently authenticated user
     * @param conversationId    the ID of the conversation
     * @param messageId         the ID of the message to delete
     * @return 200 OK
     */
    @DeleteMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<SuccessResponse<Void>> deleteMessage(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId) {

        messageService.deleteMessage(authenticatedUser.getId(), conversationId, messageId);

        return ResponseEntity.ok(SuccessResponse.success("Message deleted successfully", null));
    }
}
