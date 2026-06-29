package com.messaging.backend.common.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Common parent class for Spring Boot integration tests.
 * 
 * Purpose:
 * Boots the entire application context and provides a standardized testing environment 
 * strictly isolated to the "test" profile, executing within a transactional boundary.
 * 
 * Usage:
 * Extend this class for full-stack integration testing where interacting with 
 * a live application context is required. Data is automatically rolled back after each test.
 * 
 * Extension points:
 * Ready to integrate Testcontainers configuration for isolated database instances in the future.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
}
