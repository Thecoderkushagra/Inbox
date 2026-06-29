# 1. Project Overview

Project Type:

* Greenfield
* Production-ready
* Real-time messaging platform
* End-to-End Encrypted (Signal Protocol)
* Docker-first deployment

Technology Stack

Backend

* Java 21
* Spring Boot 3.4.x
* Spring Security
* Spring Data JPA
* PostgreSQL
* Flyway
* Maven

Frontend

* React 18
* Vite
* TypeScript (strict)

Communication

* REST
* WebSocket
* STOMP

Authentication

* JWT Access Token
* Refresh Token Rotation
* BCrypt Password Hashing

Deployment

* Docker
* Docker Compose
* Nginx

---

# 2. Architectural Philosophy

The application is intentionally built as a **Modular Monolith**.

Do not introduce microservices.

Each module behaves like an internal service with clearly defined boundaries.

The architecture prioritizes:

* maintainability
* consistency
* testability
* security
* future scalability
* low coupling
* high cohesion

Never redesign an approved module unless explicitly instructed by the architect.

---

# 3. Architecture Principles

The following principles are mandatory.

## Principle 1

Business logic belongs only inside Services.

Controllers must never implement business logic.

Repositories must never implement business logic.

Entities must never implement business logic.

---

## Principle 2

Persistence belongs only inside repositories.

Controllers must never access repositories.

Services may access repositories.

---

## Principle 3

HTTP communication happens only through controllers.

Repositories never return HTTP responses.

Entities never return HTTP responses.

---

## Principle 4

Controllers are orchestration layers only.

Allowed responsibilities:

* request validation
* authentication context
* calling services
* returning responses

Forbidden:

* SQL
* transactions
* encryption
* email sending
* WebSocket publishing
* JWT creation
* repository access

---

## Principle 5

Services own:

* business rules
* validation
* authorization
* transactions
* orchestration

---

## Principle 6

Repositories own:

* database queries
* persistence
* entity retrieval

Repositories must never:

* send emails
* publish events
* manipulate JWTs
* perform authorization
* implement business rules

---

# 4. Layer Rules

The dependency direction is fixed.

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Forbidden:

```
Controller → Repository
Controller → Entity
Controller → Database
Repository → Controller
Repository → Service
Entity → Repository
```

Never violate dependency direction.

---

# 5. Package Organization

Packages are organized by **domain**, never by technical layer.

Correct

```
authentication/
users/
friends/
groups/
messages/
keys/
notifications/
websocket/
common/
```

Incorrect

```
controller/
service/
repository/
entity/
```

Each domain owns its own:

* controller
* service
* repository
* dto
* entity
* mapper
* validation

---

# 6. Module Ownership

Every business capability has exactly one owner.

Examples

Authentication owns

* login
* registration
* refresh token
* logout
* email verification

Messaging owns

* messages
* delivery
* read receipts

Groups own

* groups
* members
* permissions

Friends own

* requests
* relationships

Never duplicate ownership.

---

# 7. Cross Module Communication

Modules communicate through service interfaces.

Never access another module's repository directly.

Allowed

```
FriendService

↓

UserService Interface
```

Forbidden

```
FriendRepository

↓

UserRepository
```

---

# 8. DTO Rules

Entities never leave the service layer.

Always use DTOs.

Request Flow

```
HTTP

↓

Request DTO

↓

Service

↓

Entity

↓

Database
```

Response Flow

```
Database

↓

Entity

↓

Response DTO

↓

JSON
```

Never expose entities directly.

---

# 9. Entity Rules

Entities represent persistence only.

Entities may contain:

* fields
* relationships
* JPA annotations

Entities must never contain:

* HTTP logic
* repository access
* service access
* authentication logic
* email logic
* encryption logic

Keep entities lightweight.

---

# 10. Dependency Injection

Always use constructor injection.

Never use field injection.

Example

Correct

```
private final UserRepository repository;

public UserService(UserRepository repository) {
    this.repository = repository;
}
```

Forbidden

```
@Autowired
private UserRepository repository;
```

---

# 11. Single Responsibility

Every class has one responsibility.

Examples

Good

```
JwtService
MailService
FriendService
MessageService
```

Bad

```
ApplicationService

UtilityService

CommonService

ManagerService
```

Avoid "God Classes".

---

# 12. Common Module

The common module contains only reusable infrastructure.

Examples

* exceptions
* security utilities
* response wrappers
* constants
* shared configuration

Business logic must never live inside common.

---

# 13. End-to-End Encryption Boundary

The backend never decrypts messages.

Backend stores only:

* public identity keys
* signed prekeys
* one-time prekeys
* encrypted ciphertext
* encrypted attachments

The backend never stores:

* plaintext messages
* session keys
* decrypted attachments

Encryption is exclusively the responsibility of the React client.

---

# 14. Architectural Violations

The coding agent must reject implementations that introduce:

* duplicated services
* duplicated repositories
* duplicated DTOs
* circular dependencies
* hardcoded secrets
* placeholder implementations
* business logic inside controllers
* repository logic inside controllers
* entity business logic
* repository cross-calls
* direct database access from controllers

If uncertain, stop and request clarification instead of guessing.

---

# 15. General Philosophy

The codebase should always optimize for:

* readability
* maintainability
* explicitness
* consistency
* security
* scalability

Shorter code is **not** better if it reduces clarity.

Every generated change should leave the project in a cleaner state than before.

---

# 16. Java Coding Standards

The project uses:

* Java 21
* Spring Boot 3.4.x

Modern Java features are encouraged when they improve readability.

Allowed

* Records for immutable DTOs
* Pattern matching
* Switch expressions
* Text blocks
* Local variable type inference (`var`) when readability is not reduced

Avoid clever code.

Readable code is preferred over shorter code.

---

# 17. Naming Conventions

Classes

Use PascalCase.

Examples

```
UserService
AuthenticationController
FriendRequestRepository
JwtAuthenticationFilter
```

Interfaces

Do NOT prefix with "I".

Correct

```
UserService
MailService
TokenProvider
```

Incorrect

```
IUserService
IMailService
```

Methods

Use camelCase.

Examples

```
registerUser()
sendVerificationEmail()
findUserByEmail()
refreshAccessToken()
```

Variables

Use descriptive camelCase.

Bad

```
x
temp
obj
value
```

Good

```
authenticatedUser
refreshToken
verificationCode
friendRequest
```

Constants

Use UPPER_SNAKE_CASE.

```
ACCESS_TOKEN_EXPIRATION
MAX_LOGIN_ATTEMPTS
DEFAULT_PAGE_SIZE
```

Packages

Use lowercase only.

```
authentication
friends
messages
notification
```

Never use uppercase package names.

---

# 18. Class Size Guidelines

Target maximum:

Controller

* 200 lines

Service

* 300 lines

Repository

* 150 lines

DTO

* as small as possible

Entity

* persistence only

If a class grows significantly beyond these limits, consider extracting responsibilities.

Avoid "God Classes".

---

# 19. Method Design

Methods should do one thing.

Good

```
createUser()

validatePassword()

sendVerificationEmail()
```

Bad

```
createUserAndSendEmailAndGenerateJwtAndLogin()
```

Keep methods focused.

---

# 20. Constructor Injection

Always use constructor injection.

Never use:

```
@Autowired
private UserRepository repository;
```

Correct

```
private final UserRepository repository;

public UserService(UserRepository repository) {
    this.repository = repository;
}
```

Every dependency should be final whenever possible.

---

# 21. Dependency Rules

Allowed

```
Controller
↓

Service
↓

Repository
```

Services may depend on:

* repositories
* interfaces
* utility classes
* mappers

Repositories must never depend on:

* services
* controllers
* security
* web layer

---

# 22. DTO Rules

Create separate DTOs.

Examples

```
RegisterRequest

LoginRequest

RefreshTokenRequest

UserResponse

FriendRequestResponse

GroupResponse
```

Never reuse one DTO for unrelated APIs.

Never expose entity classes through REST.

---

# 23. Mapper Rules

Every domain owns its mapper.

Example

```
UserMapper

FriendMapper

GroupMapper
```

Mapping logic belongs only inside mapper classes.

Never map inside:

* controllers
* repositories

Prefer dedicated mapper methods over inline conversions.

---

# 24. Validation Rules

Validation happens in two stages.

Stage 1

Bean Validation

Examples

```
@NotBlank

@NotNull

@Email

@Pattern

@Size
```

Stage 2

Business validation

Examples

* email already exists
* username already taken
* invalid friendship state
* group limit exceeded

Business validation belongs inside services.

---

# 25. Exception Handling

Controllers must never catch business exceptions.

Use a global exception handler.

Each exception should represent one failure.

Examples

```
UserNotFoundException

InvalidCredentialsException

EmailAlreadyVerifiedException

FriendRequestNotFoundException
```

Avoid generic exceptions.

Never throw:

```
Exception

RuntimeException
```

unless absolutely necessary.

---

# 26. Logging Standards

Use SLF4J.

Log important events.

Examples

* login success
* login failure
* registration
* refresh token rotation
* websocket connect
* websocket disconnect

Never log:

* passwords
* JWT tokens
* refresh tokens
* verification codes
* private keys
* plaintext messages

Logs must never expose secrets.

---

# 27. Transaction Rules

Only services may be transactional.

Repositories must not declare transactions.

Controllers must not declare transactions.

Keep transactions as small as possible.

---

# 28. Repository Rules

Repositories should contain only persistence logic.

Allowed

```
findByEmail()

existsByUsername()

findAllByGroupId()
```

Forbidden

```
sendEmail()

createJwt()

publishMessage()

validatePassword()
```

---

# 29. Configuration Rules

Configuration belongs only inside configuration classes.

Never hardcode:

* URLs
* secrets
* credentials
* ports
* expiration times

Everything must come from configuration properties or environment variables.

---

# 30. Constants

Magic values are forbidden.

Bad

```
jwtExpiration = 900000
```

Good

```
JwtProperties.getAccessTokenExpiration()
```

Use named constants or configuration properties.

---

# 31. Utility Classes

Utility classes must be:

* stateless
* deterministic
* reusable

Utility classes must never:

* access repositories
* call REST APIs
* access HTTP requests
* contain business rules

---

# 32. Documentation

Every public class should include a short JavaDoc describing its responsibility.

Complex methods should explain *why*, not *what*.

Avoid redundant comments.

Bad

```java
// increment i
i++;
```

Good

```java
// Retry count is incremented before persisting to enforce account lockout policy.
retryCount++;
```

---

# 33. Code Smells (Forbidden)

Do not introduce:

* duplicated code
* duplicated validation
* giant methods
* giant classes
* nested if chains
* deeply nested loops
* copy-pasted business logic
* unused methods
* dead code
* commented-out code
* placeholder implementations
* TODO comments
* FIXME comments

Either implement the feature or stop and report that more architectural guidance is required.

---

# 34. Self Review Before Finishing Any Task

Before completing any implementation, verify:

* Business logic is only in services.
* Controllers are thin.
* DTOs are used correctly.
* Entities are not exposed.
* No duplicated code exists.
* No hardcoded values exist.
* Constructor injection is used everywhere.
* No unused imports remain.
* No compiler warnings remain.
* No placeholder code exists.
* Logging does not expose secrets.
* Code follows this AGENTS.md document.

If any item fails, fix it before reporting the task as complete.
---

# 35. Security First

Security is a feature, not an afterthought.

Every implementation must follow the principle of **Defense in Depth**.

Priority order:

1. Prevent vulnerabilities.
2. Validate all inputs.
3. Authenticate every request.
4. Authorize every action.
5. Log security events.
6. Fail securely.

Never implement shortcuts for convenience.

---

# 36. Authentication Rules

Authentication is handled exclusively by Spring Security.

Authentication mechanisms:

* JWT Access Token
* Refresh Token Rotation
* BCrypt password hashing

Never implement:

* Session authentication
* Plaintext passwords
* Basic Authentication
* Custom password hashing algorithms

Every protected endpoint must require authentication unless explicitly marked as public.

---

# 37. Authorization Rules

Authentication tells **who** the user is.

Authorization determines **what** the user may do.

Authorization belongs inside the service layer.

Never rely solely on frontend restrictions.

Examples:

Allowed

* User can delete only their own message.
* Group admin can remove members.
* Sender can edit their own pending message.

Forbidden

* Assuming a user owns a resource because the frontend hides the button.

Every sensitive action must verify ownership or permissions.

---

# 38. Password Rules

Passwords are never stored or logged.

Requirements:

* BCrypt hashing only
* Password comparison through Spring Security
* No reversible encryption
* Never expose password fields in DTOs

Password strength validation belongs in the registration flow.

---

# 39. JWT Rules

Access Tokens

* Short-lived
* Stateless
* Signed using a secret from environment variables

Refresh Tokens

* Rotated after every successful refresh
* Stored securely in the database
* Revoked on logout
* Revoked after suspicious activity

Never:

* Embed sensitive information in JWT claims.
* Store passwords in JWTs.
* Trust expired tokens.

---

# 40. End-to-End Encryption Rules

The backend never decrypts messages.

The backend stores only:

* Identity public keys
* Signed prekeys
* One-time prekeys
* Ciphertext
* Encrypted attachments

The backend must never store:

* Plaintext messages
* Private keys
* Session keys
* Decrypted attachments

Signal Protocol is implemented entirely on the React client.

---

# 41. Secret Management

Never hardcode:

* Passwords
* API keys
* JWT secrets
* SMTP credentials
* Database credentials
* Encryption secrets
* OAuth credentials

Everything comes from environment variables.

Never commit secrets to Git.

---

# 42. Environment Variables

Environment-specific configuration must live outside the application.

Examples:

* Database URL
* JWT secret
* SMTP host
* SMTP username
* SMTP password
* Access token lifetime
* Refresh token lifetime
* Allowed CORS origins

Never switch environments using code changes.

---

# 43. Database Rules

PostgreSQL is the only supported database.

Schema changes must use Flyway.

Never modify the database manually in production.

Every schema change requires:

* Forward migration
* Versioned migration file
* Descriptive migration name

Example:

```text
V1__initial_schema.sql
V2__create_user_table.sql
V3__create_friend_request_table.sql
```

Never edit an already-applied migration.

Create a new migration instead.

---

# 44. Entity Design

Entities represent database state.

Requirements:

* Explicit table names
* Explicit column names when appropriate
* Proper indexes
* Foreign key constraints
* Optimistic locking when needed

Avoid unnecessary eager loading.

Default relationships should favor `LAZY` fetching unless there is a clear reason otherwise.

---

# 45. Database Performance

Repositories should be optimized.

Avoid:

* N+1 queries
* SELECT *
* unnecessary joins
* loading entire tables

Always paginate large result sets.

Indexes should exist for:

* email
* username
* foreign keys
* frequently searched columns
* refresh token lookup
* message lookup

---

# 46. API Design

REST endpoints should be predictable.

Examples:

```text
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh

GET    /api/v1/users/me

POST   /api/v1/friends/request
GET    /api/v1/friends

POST   /api/v1/groups
GET    /api/v1/groups/{id}
```

Avoid verbs inside URLs unless required.

Prefer nouns.

---

# 47. API Response Format

Every REST response should follow a consistent structure.

Success responses should include:

* timestamp
* status
* message
* data

Error responses should include:

* timestamp
* status
* error code
* message
* request path

Avoid inconsistent response structures.

---

# 48. Validation Strategy

Validation occurs in multiple layers.

Layer 1

Bean Validation

Layer 2

Business Validation

Layer 3

Database Constraints

Never rely on only one layer.

---

# 49. WebSocket Rules

WebSocket connections must be authenticated.

Authentication occurs during the handshake.

Never trust a client because it has an open socket.

Every incoming message must still be authorized.

---

# 50. Docker Rules

Docker is mandatory.

Development

* docker-compose.dev.yml

Production

* docker-compose.prod.yml

Containers communicate using Docker service names.

Never use:

```text
localhost
127.0.0.1
```

inside containers.

Containers must be stateless.

---

# 51. Logging Rules

Log:

* Startup
* Shutdown
* Authentication success/failure
* Security violations
* Unexpected exceptions
* WebSocket connections

Never log:

* Passwords
* JWTs
* Refresh Tokens
* Verification codes
* Private keys
* Plaintext messages
* Sensitive headers

---

# 52. External Services

Email

Treat SMTP as an external dependency.

Failures should:

* be logged
* be retried where appropriate
* not crash the application

Design integrations behind interfaces so providers can be replaced.

---

# 53. Error Handling

Do not expose:

* Stack traces
* SQL errors
* Internal exception names
* Framework internals

Return meaningful, user-safe error messages.

Detailed diagnostics belong in logs, not API responses.

---

# 54. Dependency Management

Only introduce new libraries when they provide clear value.

Before adding a dependency, verify:

* It is actively maintained.
* It has an appropriate license.
* It has no obvious security concerns.
* It is not already provided by Spring Boot or the JDK.

Prefer built-in framework capabilities over additional libraries.

---

# 55. Infrastructure Principles

Application containers should remain immutable.

Configuration should be external.

State belongs in:

* PostgreSQL
* Persistent volumes (where appropriate)

Application containers should not store persistent business data.

---

# 56. Security Review Checklist

Before completing any task, verify:

* No secrets are hardcoded.
* Every protected endpoint is authenticated.
* Every sensitive action is authorized.
* Passwords are hashed with BCrypt.
* JWTs contain only necessary claims.
* No plaintext messages are stored.
* No sensitive data is logged.
* Flyway is used for schema changes.
* Database queries are optimized.
* Docker networking uses service names.
* API responses follow the project standard.
* Environment variables are used correctly.

If any check fails, correct it before considering the implementation complete.

---

# 57. React Architecture

The frontend is responsible for:

* User Interface
* Signal Protocol
* Local encryption/decryption
* WebSocket client
* Authentication state
* API communication
* Local caching where appropriate

The frontend is **not** responsible for:

* Authorization decisions
* Business validation
* Trusting client-side state

The backend remains the source of truth.

---

# 58. React Project Structure

Organize React by feature, not by file type.

Example

```text
src/

    app/

    authentication/

    users/

    friends/

    groups/

    chat/

    websocket/

    signal/

    shared/

    hooks/

    services/

    routes/

    assets/
```

Avoid large "components" folders containing unrelated files.

---

# 59. Component Rules

Components should have one responsibility.

Prefer small reusable components over large page components.

Avoid components exceeding approximately 250 lines unless justified.

Separate:

* UI
* state management
* API calls
* business logic

---

# 60. State Management

Global state should contain only shared application state.

Examples

Allowed

* authenticated user
* authentication status
* websocket status
* theme
* notifications

Avoid placing temporary form state into global state.

Keep local state local.

---

# 61. API Layer

Never call Axios directly from React components.

Use dedicated API service classes.

Example

```text
AuthenticationApi

FriendApi

GroupApi

MessageApi
```

React components communicate only with the API layer.

---

# 62. Signal Protocol Responsibilities

Signal implementation belongs entirely inside the frontend.

Responsibilities include:

* Identity key generation
* Signed prekey generation
* One-time prekeys
* Session establishment
* Encryption
* Decryption

Never move cryptographic logic into the backend.

---

# 63. WebSocket Client Rules

The WebSocket client should be isolated.

Responsibilities

* Connection lifecycle
* Reconnection
* Authentication
* Subscription management

Business logic should remain outside the socket layer.

---

# 64. Error Handling

Frontend errors should:

* Display meaningful messages
* Log unexpected errors for debugging
* Never expose internal backend details

Avoid generic

```text
Something went wrong.
```

when more specific feedback is available.

---

# 65. Accessibility

UI should follow basic accessibility practices.

Examples

* Semantic HTML
* Labels for inputs
* Keyboard navigation
* Focus management
* Sufficient color contrast

Accessibility should not be deferred until the end of the project.

---

# 66. Git Workflow

Main branches

```text
main

develop
```

Feature branches

```text
feature/authentication

feature/groups

feature/websocket

feature/signal
```

Bug fixes

```text
bugfix/login

bugfix/token-refresh
```

Hotfixes

```text
hotfix/security-patch
```

Never commit directly to `main`.

---

# 67. Commit Message Convention

Use Conventional Commits.

Examples

```text
feat(auth): implement email verification

fix(jwt): correct refresh token rotation

refactor(chat): extract websocket service

test(auth): add login integration tests

docs: update architecture

chore: update dependencies
```

Avoid vague messages.

Bad

```text
Update

Changes

Fix

Done
```

---

# 68. Pull Request Expectations

Every pull request should:

* Have one clear purpose
* Build successfully
* Pass all tests
* Follow AGENTS.md
* Avoid unrelated changes

Large features should be split into multiple pull requests.

---

# 69. Testing Strategy

Testing pyramid

1. Unit Tests
2. Integration Tests
3. End-to-End Tests

Business logic should have unit tests.

Repository behavior should have integration tests where appropriate.

Critical authentication flows should be tested end-to-end.

---

# 70. Unit Testing Rules

Focus on:

* Services
* Validators
* Mappers
* Utility classes

Avoid unnecessary controller unit tests when integration tests provide better coverage.

Mock only external dependencies.

---

# 71. Integration Testing Rules

Use integration tests for:

* Repository behavior
* Security configuration
* Authentication flow
* Flyway migrations
* Database interactions

Integration tests should use isolated test data.

---

# 72. Performance Guidelines

Avoid premature optimization.

However:

* Use pagination
* Avoid N+1 queries
* Avoid repeated API calls
* Cache only when justified
* Measure before optimizing

Performance decisions should be evidence-based.

---

# 73. CI/CD Expectations

Every pipeline should verify:

* Project builds successfully
* Tests pass
* Formatting is consistent
* No compilation warnings
* Flyway migrations validate
* Docker images build successfully

Deployment should not proceed if any required check fails.

---

# 74. Documentation Rules

Documentation is part of the deliverable.

Update documentation whenever:

* APIs change
* Architecture changes
* Environment variables change
* Deployment changes
* Security assumptions change

Outdated documentation is considered a defect.

---

# 75. Refactoring Rules

Refactoring must not change observable behavior unless explicitly requested.

When refactoring:

* Preserve tests
* Reduce complexity
* Improve readability
* Remove duplication

Avoid unnecessary architectural redesign.

---

# 76. Definition of Done

A task is complete only when:

✓ Requirements are implemented.

✓ Code compiles.

✓ Tests pass.

✓ No TODOs remain.

✓ No placeholder implementations remain.

✓ No duplicated code exists.

✓ Logging follows standards.

✓ Security requirements are satisfied.

✓ Documentation is updated.

✓ AGENTS.md rules are followed.

---

# 77. Autonomous Agent Execution Policy

Before generating code, the agent must:

1. Read AGENTS.md.
2. Identify impacted modules.
3. Follow existing architecture.
4. Avoid introducing duplication.
5. Reuse existing abstractions.
6. Ask for clarification instead of guessing when architecture is unclear.

The agent must **never** silently redesign approved architecture.

---

# 78. Task Completion Report

After every implementation, the agent should provide a concise report including:

Completed

* Files created
* Files modified

Architecture

* Modules affected
* New dependencies introduced (if any)

Verification

* Build status
* Tests executed
* Security considerations

Self Review

Confirm that:

* Controllers remain thin.
* Business logic resides in services.
* DTO isolation is maintained.
* No hardcoded secrets exist.
* No architectural violations were introduced.

---

# 79. When Unsure

If requirements are ambiguous:

STOP.

Do not invent architecture.

Do not generate placeholder implementations.

Do not assume business rules.

Instead:

* Explain the ambiguity.
* Present available options.
* Wait for architectural guidance.

Correctness is preferred over speed.

---

# 80. Final Principle

This repository values:

* correctness over convenience
* maintainability over cleverness
* explicitness over magic
* security over shortcuts
* consistency over personal preference

Every change should make the codebase easier to understand than it was before.

If a proposed implementation violates these principles, reject it and request clarification.

---
