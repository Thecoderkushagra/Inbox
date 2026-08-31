package com.messaging.backend.readreceipts.entity;

import com.messaging.backend.common.entity.BaseDocument;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a delivery and read receipt document in MongoDB.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "read_receipts")
@CompoundIndex(name = "idx_receipt_msg_user", def = "{'messageId': 1, 'userId': 1}", unique = true)
public class ReadReceipt extends BaseDocument {

    @Indexed
    private String messageId;

    @Indexed
    private String conversationId;

    @Indexed
    private String userId;

    private Instant deliveredAt;

    private Instant seenAt;

    @Builder
    public ReadReceipt(String messageId, String conversationId, String userId, Instant deliveredAt, Instant seenAt) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.userId = userId;
        this.deliveredAt = deliveredAt;
        this.seenAt = seenAt;
    }
}
