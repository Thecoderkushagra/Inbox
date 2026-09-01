package com.messaging.backend.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.util.List;

/**
 * Ensures any faulty legacy indexes created from previously embedded entities
 * (e.g. user.* on refresh_tokens, sender.* on messages) are removed cleanly on startup.
 */
@Slf4j
@Configuration
public class MongoIndexCleanupConfig {

    @Bean
    public CommandLineRunner cleanupFaultyMongoIndexes(MongoTemplate mongoTemplate) {
        return args -> {
            cleanupCollectionIndexes(mongoTemplate, "refresh_tokens", List.of("user.", "user_"));
            cleanupCollectionIndexes(mongoTemplate, "messages", List.of("sender.", "sender_", "conversation."));
            cleanupCollectionIndexes(mongoTemplate, "read_receipts", List.of("user.", "user_", "message."));
            cleanupCollectionIndexes(mongoTemplate, "conversations", List.of("participants.user"));
            cleanupCollectionIndexes(mongoTemplate, "media_attachments", List.of("message."));
            cleanupCollectionIndexes(mongoTemplate, "users", List.of("profile."));
        };
    }

    private void cleanupCollectionIndexes(MongoTemplate mongoTemplate, String collectionName, List<String> invalidPrefixes) {
        try {
            if (!mongoTemplate.collectionExists(collectionName)) {
                return;
            }
            List<IndexInfo> indexInfoList = mongoTemplate.indexOps(collectionName).getIndexInfo();
            for (IndexInfo info : indexInfoList) {
                String indexName = info.getName();
                if ("_id_".equals(indexName)) {
                    continue;
                }

                boolean shouldDrop = invalidPrefixes.stream().anyMatch(prefix ->
                        indexName.toLowerCase().contains(prefix.toLowerCase())
                );

                if (!shouldDrop && info.getIndexFields() != null) {
                    for (IndexField field : info.getIndexFields()) {
                        String fieldKey = field.getKey();
                        if (fieldKey != null && invalidPrefixes.stream().anyMatch(prefix -> fieldKey.toLowerCase().startsWith(prefix.toLowerCase()) || fieldKey.toLowerCase().contains(prefix.toLowerCase()))) {
                            shouldDrop = true;
                            break;
                        }
                    }
                }

                if (shouldDrop) {
                    log.info("Dropping invalid legacy index '{}' from collection '{}'", indexName, collectionName);
                    mongoTemplate.indexOps(collectionName).dropIndex(indexName);
                }
            }
        } catch (Exception e) {
            log.warn("Index cleanup on collection '{}' encountered a non-fatal warning: {}", collectionName, e.getMessage());
        }
    }
}
