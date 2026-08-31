package com.messaging.backend.common.support;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Common parent class for all pure unit tests.
 * 
 * Purpose:
 * Enforces a strict testing environment free of the Spring application context,
 * ensuring tests remain extremely fast and strictly isolated.
 * 
 * Usage:
 * Extend this class for any service layer or utility unit tests.
 * 
 * Extension points:
 * Can be augmented with custom JUnit 5 extensions or shared assertions in the future.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseUnitTest {
}
