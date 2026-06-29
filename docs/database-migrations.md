# Database Migration Strategy

This project uses **Flyway** for database schema evolution. All schema changes are strictly versioned, deterministic, and automatically applied upon application startup across all environments.

## Naming Convention
Every migration script must follow this exact format:
`V<version_number>__<description>.sql`

### Examples
- `V1__baseline.sql`
- `V2__create_users_table.sql`
- `V3__create_friendships_table.sql`

*Note: The separator is a double underscore (`__`).*

## Versioning Strategy
- **Sequential Monotonically Increasing**: Versions must strictly increase (e.g., `V1`, `V2`, `V3`).
- **Out-of-Order Execution**: Disabled (`out-of-order: false`). A migration will fail if a lower-versioned script is added after a higher-versioned script has already been applied.
- **Immutable History**: Once a migration is committed and merged into a shared environment (e.g., `develop` or `main`), it **must never be edited or renamed**.

## Migration Lifecycle
1. Developer creates a new `.sql` file in `src/main/resources/db/migration/`.
2. Developer writes forward-only DDL/DML.
3. Spring Boot runs Flyway automatically on startup.
4. Flyway records the execution and checksum in the `flyway_schema_history` table.
5. If the checksum of an applied migration changes later, Flyway validation will fail and the application will refuse to start.

## Validation Strategy
- **Validate on Migrate**: Enabled (`validate-on-migrate: true`). Flyway verifies that all previously applied migrations match their original checksums on disk before applying new migrations.
- **Clean Disabled**: Enabled (`clean-disabled: true`). Flyway is explicitly forbidden from dropping database objects. This prevents catastrophic data loss in production.

## Rollback & Repair Philosophy
- **Rollbacks**: We do **not** use Flyway `U` (Undo) migrations. The database schema must always move forward. If a mistake is deployed, it must be fixed by creating a *new* forward migration (e.g., `V5__fix_mistake.sql` or `V6__drop_erroneous_table.sql`).
- **Repairs**: If a migration fails midway (e.g., a syntax error), the failed entry in `flyway_schema_history` must be manually deleted by a DBA, the SQL script fixed, and the application restarted.

## Team Workflow & Production Deployment Rules
- **No Manual Changes**: The production database schema must **never** be modified manually. All changes must pass through Flyway.
- **No Business Logic in V1**: The baseline migration (`V1__baseline.sql`) is empty by design to initialize the versioning table without tightly coupling it to early business logic.
- **Code Review**: Migration scripts require rigorous code review. Pay attention to locking, index creation, and large table alterations.
