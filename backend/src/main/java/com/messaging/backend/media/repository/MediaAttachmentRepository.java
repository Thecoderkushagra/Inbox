package com.messaging.backend.media.repository;

import com.messaging.backend.media.entity.MediaAttachment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaAttachmentRepository extends MongoRepository<MediaAttachment, String> {

    List<MediaAttachment> findByMessageId(String messageId);

    List<MediaAttachment> findByMessageIdInAndDeletedFalse(List<String> messageIds);

    boolean existsByStorageKey(String storageKey);

    Optional<MediaAttachment> findByStorageKey(String storageKey);

    List<MediaAttachment> findByDeletedFalse();
}
