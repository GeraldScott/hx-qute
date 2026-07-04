---
name: postgresql-java
description: Use when adding or changing database schema in this project — writing Flyway migrations, creating tables, seed data, designing entities' persistence mapping, writing Panache repository queries, or connecting to the dev database.
---

# Project PostgreSQL & Flyway Conventions

PostgreSQL 17.7, Flyway migrations, Panache **repository** pattern (not active record). Hibernate never touches the schema: `quarkus.hibernate-orm.schema-management.strategy=none` — Flyway owns all DDL.

## Flyway Versioning Scheme

`V{major}.{entity-seq}.{step}__{Description}.sql` in `src/main/resources/db/migration/`:

- `major` — currently `1`
- `entity-seq` — one number per entity, allocated in creation order (gender=0, user_login=2, title=3, person=4, relationship=5, person_relationship=6)
- `step` — `0` = schema (`Create_{table}_table`), `1` = seed data (`Insert_{table}_data`)
- Description is `Capitalized_with_underscores`

```
V1.4.0__Create_person_table.sql
V1.4.1__Insert_person_data.sql
```

For a new entity, take the next entity-seq **above the highest existing one** (historical gaps like `1` stay unused — Flyway skips versions that sort below already-applied migrations). Never edit an applied migration — schema changes to existing tables get a new version (e.g. `V1.4.2__Add_person_phone_column.sql`). Seed data lives in its own `.1` migration, never mixed with DDL.

## Table DDL Conventions

From `V1.0.0__Create_gender_table.sql`:

```sql
CREATE TABLE gender (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(1) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);
```

- Table names: singular, snake_case
- PK: `id BIGSERIAL PRIMARY KEY`
- Audit columns on **every** table: `created_at`, `created_by`, `updated_at`, `updated_by` exactly as above
- Timestamps are always `TIMESTAMP WITH TIME ZONE`
- Unique constraints named `uk_{table}_{column}` when declared at entity level; the entity `@Column`/`@Table` mapping must mirror the DDL (lengths, nullability, constraint names)
- FK columns: `{referenced_table}_id BIGINT REFERENCES {referenced_table}(id)`

The admin seed user in `V1.2.1__Insert_admin_user.sql` (`admin@example.com` / `AdminPassword123`, BCrypt cost 12) is used by the `e2e-test-runner` agent — keep them in sync if it changes.

## Repository Pattern

`@ApplicationScoped` classes implementing `PanacheRepository<T>`. All query logic lives here; resources never write JPQL. Method naming from `GenderRepository`:

| Method shape | Purpose |
|--------------|---------|
| `findByCode(x)` → `Optional<T>` | natural-key lookup via `firstResultOptional()` |
| `listAllOrdered()` | default list ordering for tables |
| `existsByCode(x)` | create-time uniqueness check (`count(...) > 0`) |
| `existsByCodeAndIdNot(x, id)` | update-time uniqueness check excluding self |
| `isReferencedByPerson(id)` | referential-integrity check before delete — inject the referencing repository |

Queries are always parameterized (`count("code = ?1 AND id != ?2", code, id)`) — never string-concatenated.

## Pagination & Fetching

- Pagination via `PanacheQuery`: `query.page(Page.of(page, size))` with size clamped to 10/25/50/100 (see `PersonResource.list`)
- Avoid N+1 on associations with `@NamedEntityGraph` (see `PersonRelationship.java` for nested subgraphs)

## Dev Database

Connection settings come from `.env` (gitignored — see `.env` locally for credentials; Quarkus reads `QUARKUS_DATASOURCE_*` vars). Database `hx_qute` on localhost. Flyway runs at startup (`quarkus.flyway.migrate-at-start=true`); a failed migration checksum means someone edited an applied file — restore it and add a new version instead.

## Related Skills

- `java-patterns` — entity field conventions and the audit-callback pattern
- `maven-java` — the Flyway/JDBC extensions in the pom
