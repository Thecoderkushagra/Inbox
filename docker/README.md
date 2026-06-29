# Docker Architecture

This directory contains the Docker infrastructure strategy and configuration for the messaging platform.
It establishes containerization across both development and production environments.

- **backend/**: Will house backend Dockerfiles and related container scripts.
- **frontend/**: Will house frontend Dockerfiles and related container scripts.
- **postgres/**: Will house database initialization scripts.
- **nginx/**: Will house the reverse proxy configuration.

See `docker-compose.dev.yml` and `docker-compose.prod.yml` at the project root for the orchestration definitions.
