package com.messaging.backend.readreceipts.mapper;

import com.messaging.backend.readreceipts.dto.response.ReadReceiptResponse;
import com.messaging.backend.readreceipts.dto.response.ReadReceiptSocketResponse;
import com.messaging.backend.readreceipts.entity.ReadReceipt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReadReceiptMapper {

    public ReadReceiptResponse toResponse(ReadReceipt receipt) {
        if (receipt == null) {
            return null;
        }

        return new ReadReceiptResponse(
                receipt.getId(),
                receipt.getMessage() != null ? receipt.getMessage().getId() : null,
                receipt.getUser() != null ? receipt.getUser().getId() : null,
                receipt.getDeliveredAt(),
                receipt.getSeenAt(),
                receipt.getCreatedAt()
        );
    }

    public List<ReadReceiptResponse> toResponseList(List<ReadReceipt> receipts) {
        if (receipts == null) {
            return Collections.emptyList();
        }

        return receipts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReadReceiptSocketResponse toSocketResponse(ReadReceipt receipt) {
        if (receipt == null) {
            return null;
        }

        return new ReadReceiptSocketResponse(
                receipt.getId(),
                receipt.getMessage() != null ? receipt.getMessage().getId() : null,
                (receipt.getMessage() != null && receipt.getMessage().getConversation() != null) 
                        ? receipt.getMessage().getConversation().getId() : null,
                receipt.getUser() != null ? receipt.getUser().getId() : null,
                receipt.getDeliveredAt(),
                receipt.getSeenAt()
        );
    }
}
