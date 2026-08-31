package com.messaging.backend.readreceipts.repository;

import com.messaging.backend.readreceipts.entity.ReadReceipt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReadReceiptRepository extends MongoRepository<ReadReceipt, String> {

    List<ReadReceipt> findByMessageId(String messageId);

    Optional<ReadReceipt> findByMessageIdAndUserId(String messageId, String userId);

    boolean existsByMessageIdAndUserId(String messageId, String userId);

    List<ReadReceipt> findByUserId(String userId);

    List<ReadReceipt> findByUserIdAndSeenAtIsNull(String userId);

    long countByUserIdAndSeenAtIsNull(String userId);

    List<ReadReceipt> findByMessageIdIn(Collection<String> messageIds);
}
