package com.messaging.backend.common.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Common foundation for controller tests.
 * 
 * Purpose:
 * Provides a standardized setup for simulating HTTP requests and verifying JSON responses
 * without spinning up a full embedded web server.
 * 
 * Usage:
 * Subclass and annotate with @WebMvcTest(TargetController.class).
 * 
 * Extension points:
 * Can be extended with shared MockMvc request builders or custom authentication injectors.
 */
@ActiveProfiles("test")
public abstract class BaseWebMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
