# Messaging Platform

## Project Overview
This repository contains the source code for a production-ready, real-time messaging platform. It features end-to-end encryption using the Signal Protocol, real-time WebSocket communication, and robust JWT-based authentication.

## Technology Stack
- **Backend**: Java 21, Spring Boot 3.4.x, Spring Security, Spring Data JPA, PostgreSQL, Flyway, Maven
- **Frontend**: React 18, Vite, TypeScript (strict)
- **Communication**: REST, WebSocket, STOMP
- **Deployment**: Docker, Docker Compose, Nginx

## Architecture Summary
The application is built as a **Modular Monolith**. It follows domain-oriented packaging, enforces thin controllers, and restricts business logic exclusively to the service layer. The backend acts as a trustless relay for messages; it never decrypts or stores plaintext messages, offloading all cryptographic responsibilities to the React client.

## Repository Standards
- **`AGENTS.md`**: This file acts as the project's engineering constitution. It dictates all architectural and coding rules.
- **Directory Ownership**: Directories are organized by domain capability rather than technical layers.
- **Documentation Ownership**: All architecture docs are maintained in the `/docs` directory.
- **Configuration Ownership**: Environment variables (`.env`) are the exclusive source of truth for runtime configuration.
- **Code Ownership**: Each domain module has strict boundaries. Cross-module communication happens only through service interfaces.
- **Architecture Ownership**: All architectural changes must be reviewed against `AGENTS.md` before implementation.

## Prerequisites
- Docker and Docker Compose
- Java 21 (for local non-containerized development)
- Node.js 18+ (for local non-containerized development)

## Repository Layout
- `backend/`: Java Spring Boot application source code.
- `frontend/`: React TypeScript application source code.
- `docker/`: Docker infrastructure strategy and configurations.
- `config/`: Configuration architecture rules.
- `docs/`: Project documentation.
- `scripts/`: Development and automation utility scripts.
- `.github/`: GitHub automation and templates.

## Development Philosophy
The codebase optimizes for readability, maintainability, explicitness, and security. Every generated change must leave the project in a cleaner state than before. "God classes", placeholder implementations, and hardcoded secrets are strictly forbidden.

## Links to Docs
- [AGENTS.md](./AGENTS.md) - Engineering Constitution
- [Development Workflow](./docs/development-workflow.md)
- [Branching Strategy](./docs/branching-strategy.md)
- [Versioning Strategy](./docs/versioning.md)
- [Configuration Strategy](./docs/configuration.md)

## High-Level Roadmap
1. Establish Architecture and Repository Standards
2. Implement Core Backend Authentication and Security
3. Develop Frontend Foundation and State Management
4. Build Real-time WebSocket Messaging Infrastructure
5. Implement Signal Protocol End-to-End Encryption
6. Finalize Docker Infrastructure and Reverse Proxy
7. QA, Security Audit, and Production Deployment
