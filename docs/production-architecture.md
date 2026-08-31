# Production Architecture

This document outlines the production deployment topology for the messaging platform. 

## Current Planned Topology

The production architecture is designed around containerized services communicating within an isolated Docker network.

1. **Internet**: External client traffic (Web/Mobile).
2. **Nginx**: Acts as the reverse proxy, handling SSL termination, rate limiting, and routing incoming traffic to the appropriate application endpoints.
3. **Spring Boot (Backend) & React (Frontend)**: The application layer serving REST APIs, WebSocket connections, and static frontend assets.
4. **PostgreSQL**: The sole persistent data store, housed in an isolated network segment accessible only by the backend.

**Flow:**
`Internet` -> `Nginx` -> `Spring Boot / Frontend` -> `PostgreSQL`

## Future Optional Services

The architecture is designed to accommodate the following future enhancements as scaling demands increase:
- **Redis**: For caching, WebSocket session management across multiple backend instances, and rate limiting.
- **Kafka**: For asynchronous event-driven messaging, push notification queues, and reliable offline message delivery.
- **Object Storage**: An S3-compatible service for scalable attachment and media storage.
- **Prometheus & Grafana**: For metrics aggregation and visualization.

*Note: These optional services are NOT currently implemented.*
