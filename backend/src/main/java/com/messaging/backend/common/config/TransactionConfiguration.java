package com.messaging.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Centralized transaction management configuration.
 * 
 * Intended usage:
 * This class ensures transaction management is globally enabled across the application.
 * 
 * Future extension points:
 * Provides an extension point for customizing the TransactionManager, Isolation levels, 
 * or propagation defaults in the future.
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfiguration {
    // Ready for future transaction customizations.
}
