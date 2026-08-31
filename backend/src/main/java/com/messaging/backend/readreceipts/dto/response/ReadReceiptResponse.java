package com.messaging.backend.readreceipts.dto.response;

import java.time.Instant;

public record ReadReceiptResponse(
        String receiptId,
        String messageId,
        String userId,
        Instant deliveredAt,
        Instant seenAt,
        Instant createdAt
) {}
