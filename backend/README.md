# Backend Architecture

This backend implements a **Modular Monolith** architecture based on Domain-Driven Design principles.

## Responsibilities
The backend serves as the trustless orchestrator and data persistence layer for the messaging platform. It is responsible for user authentication, real-time message delivery (via WebSocket), persistence, and authorization. It explicitly *does not* handle message decryption or encryption, acting only as a relay for end-to-end encrypted ciphertexts.

## Package Philosophy
Packages are organized strictly by business domain, never by technical layer. The root package is `com.messaging.backend`. 
Each capability (e.g., `users`, `messages`, `auth`) maintains its own self-contained package hierarchy.

## Dependency Rules & Ownership Boundaries
- **Domain Independence**: Domains cannot directly query another domain's repository. Cross-module communication is exclusively permitted via service interfaces.
- **Layer Enforcement**: Controllers must be thin and handle only HTTP concerns. Business rules reside exclusively in Services. Repositories handle only persistence.
- **DTO Isolation**: Entities never leave the service boundary; they must be mapped to DTOs before being exposed via REST or WebSocket endpoints.

## The Common Module
The `common` module provides shared infrastructure across all domains. It contains global exception handlers, security configurations, shared validation rules, and utilities. Business logic is strictly prohibited from living in the `common` module.
