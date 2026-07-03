package com.messaging.backend.media.repository;

import com.messaging.backend.media.entity.MediaAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaAttachmentRepository extends JpaRepository<MediaAttachment, UUID> {

    List<MediaAttachment> findByMessageId(UUID messageId);

    List<MediaAttachment> findByMessageIdInAndDeletedFalse(List<UUID> messageIds);

    boolean existsByStorageKey(UUID storageKey);

    Optional<MediaAttachment> findByStorageKey(UUID storageKey);

    List<MediaAttachment> findByDeletedFalse();
}
