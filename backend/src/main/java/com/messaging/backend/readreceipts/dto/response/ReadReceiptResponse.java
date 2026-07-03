package com.messaging.backend.readreceipts.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReadReceiptResponse(
        UUID receiptId,
        UUID messageId,
        UUID userId,
        Instant deliveredAt,
        Instant seenAt,
        Instant createdAt
) {}
