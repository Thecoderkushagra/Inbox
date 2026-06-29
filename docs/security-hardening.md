# Security Hardening

Security is prioritized across all layers of the application. This document outlines the planned security hardening measures for production.

## Production Requirements

- **HTTPS Only**: All external communication must be encrypted via TLS. Nginx will handle SSL termination and enforce HTTP-to-HTTPS redirects.
- **Security Headers**: Responses will include strict CSP, HSTS, X-Frame-Options, and X-Content-Type-Options headers.
- **Rate Limiting**: To mitigate brute-force and DDoS attacks, Nginx and future Redis integrations will enforce API rate limits, particularly on authentication endpoints.
- **Firewall**: Network access will be restricted to essential ports (80/443). The database must never be exposed to the public internet.
- **Docker Isolation**: Containers will run with the principle of least privilege, utilizing non-root users, read-only filesystems where possible, and dropped capabilities.
- **Secrets Management**: No secrets in the codebase. All sensitive data will be injected at runtime via secure environment variable provisioning.
- **Log Retention & Audit Logging**: Security events (login attempts, permission changes) will be explicitly logged and retained for compliance auditing.
- **Account Lockout**: Repeated failed login attempts will result in temporary account lockouts.
- **Dependency Updates & Image Scanning**: Dependencies and base Docker images will be regularly scanned for known CVEs.

*Note: These are planned requirements. Specific implementations (e.g., SSL cert provisioning, rate limit configurations) will occur in future milestones.*
