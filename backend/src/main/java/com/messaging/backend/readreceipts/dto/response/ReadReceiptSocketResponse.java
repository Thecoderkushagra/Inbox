package com.messaging.backend.readreceipts.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReadReceiptSocketResponse(
        UUID receiptId,
        UUID messageId,
        UUID conversationId,
        UUID userId,
        Instant deliveredAt,
        Instant seenAt
) {}
