# Stage 1: Build GraalVM native image
FROM ghcr.io/graalvm/native-image-community:21 AS builder

WORKDIR /build

# Copy Maven wrapper and POM
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Compile Native Executable with optimizations for low memory (< 512MB RAM)
RUN ./mvnw -Pnative native:compile -DskipTests -B

# Stage 2: Minimal runtime image
FROM debian:bookworm-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root application user
RUN useradd -u 1001 -r -s /bin/false appuser

COPY --from=builder /build/target/backend /app/backend
RUN chown -R appuser:appuser /app

USER appuser

ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/app/backend"]
