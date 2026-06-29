# Configuration Architecture

This document establishes the configuration strategy and rules for the messaging platform, ensuring adherence to the 12-Factor App methodology.

## Environment Variables as the Single Source of Truth
Environment variables are the exclusive source of runtime configuration for this project. The application must not contain any hardcoded configurations, endpoints, or credentials.

## Secret Management Strategy
- **No Secrets in Git**: No `.env` files containing real secrets, passwords, or keys shall ever be committed to version control.
- **Example Files**: Only `.env.*.example` files containing placeholders (e.g., `<placeholder>`) are committed to the repository to serve as templates.
- **Runtime Injection**: Secrets are injected securely into the containerized environments via Docker Compose and environment variables at runtime.

## Environment Separation
The project maintains distinct configuration profiles for different deployment stages. This is enforced by providing templates for each environment:
- `.env.development.example`: Local development environment configuration templates.
- `.env.production.example`: Production environment configuration templates.
- `.env.test.example`: Automated testing configuration templates.
- `.env.example`: Generic configuration template.

## Naming Conventions
- All environment variables use `UPPER_SNAKE_CASE`.
- Frontend environment variables consumed by React must be prefixed with `VITE_` (e.g., `VITE_API_BASE_URL`).

## Variable Ownership
- **Spring Boot**: Consumes generic backend variables (e.g., `DB_HOST`, `JWT_SECRET`). Future milestones will map these using `@ConfigurationProperties` to provide type safety and centralized validation.
- **React**: Consumes only `VITE_*` prefixed variables. These are replaced at build-time/dev-time by Vite.
- **Docker**: Consumes deployment variables (e.g., `COMPOSE_PROJECT_NAME`) directly in `docker-compose.yml` files.

## Workflows

### Local Development Workflow
1. Developers copy `.env.development.example` to `.env` in the root directory.
2. Developers replace placeholders with local development values (e.g., local database passwords).
3. Docker Compose reads the `.env` file to start infrastructure and pass variables to the containers.

### Production Deployment Workflow
1. The `.env.production.example` serves as a guide for CI/CD pipelines or platform administrators.
2. Production secrets are managed by a secure secret manager (e.g., AWS Secrets Manager, GitHub Secrets) and injected into the runtime environment.
3. Containers are started without local `.env` files, relying entirely on the environment variables provided by the host or orchestrator.
