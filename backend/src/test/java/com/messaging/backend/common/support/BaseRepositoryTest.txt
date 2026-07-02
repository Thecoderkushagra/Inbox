package com.messaging.backend.common.support;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Common foundation for repository tests.
 * 
 * Purpose:
 * Provides a sliced Spring context specifically tuned for JPA testing,
 * ensuring tests execute securely against the schema without booting the web layer.
 * 
 * Usage:
 * Extend this class for any repository or database query tests.
 * 
 * Extension points:
 * Can be extended to configure specific database dialects or Testcontainers.
 */
@DataJpaTest
@ActiveProfiles("test")
public abstract class BaseRepositoryTest {
}
