package com.messaging.backend.readreceipts.repository;

import com.messaging.backend.readreceipts.entity.ReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, UUID> {

    List<ReadReceipt> findByMessageId(UUID messageId);

    Optional<ReadReceipt> findByMessageIdAndUserId(UUID messageId, UUID userId);

    boolean existsByMessageIdAndUserId(UUID messageId, UUID userId);

    List<ReadReceipt> findByUserId(UUID userId);

    List<ReadReceipt> findByUserIdAndSeenAtIsNull(UUID userId);

    long countByUserIdAndSeenAtIsNull(UUID userId);

    List<ReadReceipt> findByMessageIdIn(Collection<UUID> messageIds);
}
