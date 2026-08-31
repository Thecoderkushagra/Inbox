package com.messaging.backend.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.util.List;

/**
 * Ensures any faulty legacy indexes created from previously embedded entities
 * (e.g. user.email unique index on refresh_tokens) are removed cleanly on startup.
 */
@Slf4j
@Configuration
public class MongoIndexCleanupConfig {

    @Bean
    public CommandLineRunner cleanupFaultyMongoIndexes(MongoTemplate mongoTemplate) {
        return args -> {
            cleanupCollectionIndexes(mongoTemplate, "refresh_tokens", List.of("user.email", "user_email", "user.username", "user_username"));
            cleanupCollectionIndexes(mongoTemplate, "messages", List.of("sender.email", "sender_email", "conversation.type", "sender.username"));
            cleanupCollectionIndexes(mongoTemplate, "read_receipts", List.of("user.email", "user_email", "message.content"));
            cleanupCollectionIndexes(mongoTemplate, "conversations", List.of("participants.user.email", "participants.user.username"));
        };
    }

    private void cleanupCollectionIndexes(MongoTemplate mongoTemplate, String collectionName, List<String> patterns) {
        try {
            if (!mongoTemplate.collectionExists(collectionName)) {
                return;
            }
            List<IndexInfo> indexInfoList = mongoTemplate.indexOps(collectionName).getIndexInfo();
            for (IndexInfo info : indexInfoList) {
                String indexName = info.getName();
                boolean matches = patterns.stream().anyMatch(p -> indexName.toLowerCase().contains(p.toLowerCase()));
                if (matches) {
                    log.info("Dropping invalid legacy index '{}' from collection '{}'", indexName, collectionName);
                    mongoTemplate.indexOps(collectionName).dropIndex(indexName);
                }
            }
        } catch (Exception e) {
            log.warn("Index cleanup on collection '{}' encountered a non-fatal warning: {}", collectionName, e.getMessage());
        }
    }
}
