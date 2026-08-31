package com.messaging.backend.common.mapper;

import org.springframework.context.annotation.Configuration;

/**
 * Central configuration for application mappers.
 * 
 * Intended usage:
 * Keep mapping rules consistent across all domain boundaries.
 * 
 * Future extension points:
 * This class will serve as the base for centralizing MapStruct rules, ModelMapper beans,
 * or custom generic mapping components once the framework is introduced.
 */
@Configuration
public class MapperConfig {
    // Left intentionally empty until a mapping library is selected.
}
