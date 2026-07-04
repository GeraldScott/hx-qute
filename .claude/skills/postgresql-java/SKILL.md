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

Every new migration takes the next entity-seq **above the highest existing one** (historical gaps like `1` stay unused, and back-filling an old entity's seq like `V1.2.2` fails Flyway's out-of-order validation). Seed data lives in its own `.1` migration, never mixed with DDL.

**Development-phase policy: migrations are mutable.** This project is pre-release and the database is disposable — small changes (seed values, column tweaks) are edited into the existing migration, NOT shipped as a new version. After editing an already-applied migration, recreate the database (or fix `flyway_schema_history` by hand); Flyway's checksum validation will otherwise fail at startup. Do not accumulate patch migrations during development. Once the project ships, this inverts: applied migrations become immutable and every change is a new version.

## Table DDL Conventions

**New tables (forward-only rules):**

```sql
CREATE TABLE widget (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by TEXT NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by TEXT,
    CONSTRAINT uk_widget_code UNIQUE (code)
);
```

- Table names: singular, snake_case
- PK: `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY` — maps to Java `Long` with `GenerationType.IDENTITY`. Never `BIGSERIAL` (legacy spelling), never UUID without an explicit decision
- Strings: always `TEXT`, never `VARCHAR(n)` — length limits are validation concerns, enforced in code
- Audit columns on **every** table: `created_at`, `created_by`, `updated_at`, `updated_by` exactly as above
- Timestamps are always `TIMESTAMP WITH TIME ZONE`; in entities they map to `java.time.Instant` — never `OffsetDateTime`, `ZonedDateTime`, or `LocalDateTime` (PostgreSQL stores a UTC instant; the offset is discarded, so `Instant` is the honest type)
- Unique constraints named `uk_{table}_{column}`, declared in DDL and mirrored at entity level; the entity `@Column`/`@Table` mapping must mirror the DDL (nullability, constraint names)
- FK columns: `{referenced_table}_id BIGINT REFERENCES {referenced_table}(id)`

> **Legacy note:** tables created before these rules (`gender`, `title`, `person`, `user_login`, `relationship`, `person_relationship`) use `BIGSERIAL` and `VARCHAR(n)`. Do **not** retrofit them — applied migrations are history. Match the new rules in new migrations only; when altering a legacy table, new columns follow the new rules.

The admin seed user in `V1.2.1__Insert_admin_user.sql` (`admin@example.com` / `MyAdminPassword`, BCrypt cost 12) is used by the `e2e-test-runner` agent and `GenderResourceTest` — keep them in sync if it changes.

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
