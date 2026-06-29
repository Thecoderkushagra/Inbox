# Configuration Strategy

## Purpose
This directory (`config/`) is reserved for the configuration architecture of the messaging platform. It is intended to hold configuration definitions, property classes, and configuration setup files rather than the raw configuration values themselves.

## Architecture vs. Implementation
This directory represents the configuration architecture. It defines *how* configuration is loaded, validated, and structured across the application. It is distinct from the implementation (the actual values), which are strictly provided at runtime via environment variables (`.env` files) and are never committed to version control.

## Future Implementation
In future milestones, Spring Boot `@ConfigurationProperties` classes, security configuration files, and other global setup components will be added here to strongly type and map the environment variables into the application context.
