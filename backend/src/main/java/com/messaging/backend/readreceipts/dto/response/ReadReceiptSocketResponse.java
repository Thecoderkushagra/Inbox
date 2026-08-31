package com.messaging.backend.readreceipts.dto.response;

import java.time.Instant;

public record ReadReceiptSocketResponse(
        String receiptId,
        String messageId,
        String conversationId,
        String userId,
        Instant deliveredAt,
        Instant seenAt
) {}
