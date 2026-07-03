package com.messaging.backend.readreceipts.controller;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.readreceipts.dto.response.ReadReceiptResponse;
import com.messaging.backend.readreceipts.mapper.ReadReceiptMapper;
import com.messaging.backend.readreceipts.service.ReadReceiptService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/read-receipts")
public class ReadReceiptController {

    private final ReadReceiptService readReceiptService;
    private final ReadReceiptMapper readReceiptMapper;

    public ReadReceiptController(ReadReceiptService readReceiptService, ReadReceiptMapper readReceiptMapper) {
        this.readReceiptService = readReceiptService;
        this.readReceiptMapper = readReceiptMapper;
    }

    @PatchMapping("/messages/{messageId}/delivered")
    public SuccessResponse<Void> markDelivered(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        readReceiptService.markDelivered(messageId, currentUser.getId());
        return SuccessResponse.success("Message marked as delivered", null);
    }

    @PatchMapping("/messages/{messageId}/seen")
    public SuccessResponse<Boolean> markSeen(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        boolean newlySeen = readReceiptService.markSeen(messageId, currentUser.getId());
        return SuccessResponse.success("Message seen status updated", newlySeen);
    }

    @PatchMapping("/conversations/{conversationId}/seen")
    public SuccessResponse<Integer> markConversationSeen(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        int newlySeenCount = readReceiptService.markConversationSeen(conversationId, currentUser.getId());
        return SuccessResponse.success("Conversation seen status updated", newlySeenCount);
    }

    @GetMapping("/messages/{messageId}")
    public SuccessResponse<List<ReadReceiptResponse>> getReceipts(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<ReadReceiptResponse> responses = readReceiptMapper.toResponseList(
                readReceiptService.getReceipts(messageId, currentUser.getId())
        );
        return SuccessResponse.success("Read receipts retrieved successfully", responses);
    }

    @GetMapping("/unread/count")
    public SuccessResponse<Long> getUnreadCount(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        long count = readReceiptService.getUnreadCount(currentUser.getId());
        return SuccessResponse.success("Unread count retrieved successfully", count);
    }
}
