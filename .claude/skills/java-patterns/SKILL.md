---
name: java-patterns
description: Use when writing or modifying Java code in this project — creating entities, repositories, resources, or services; deciding which layer logic belongs in; handling validation, exceptions, transactions, or audit fields.
---

# Project Java Conventions

How Java code is layered and written in this codebase. These are *this project's* choices — some deliberately differ from generic Java advice (e.g. entities go straight to templates; there are no DTOs).

## Package Layout

| Package | Contains | Exemplar |
|---------|----------|----------|
| `entity` | JPA entities, public fields, no getters/setters | `Gender.java` |
| `repository` | `PanacheRepository<T>` implementations | `GenderRepository.java` |
| `router` | JAX-RS resources returning `TemplateInstance` | `GenderResource.java` |
| `service` | `@ApplicationScoped` business logic | `UserLoginService.java` |
| `service.exception` | Domain exceptions | `UniqueConstraintException.java` |
| `error` | `GlobalExceptionMapper` | — |

**Copy-paste exemplars:** `Gender` (simple lookup table) → `Person` (pagination, filters, foreign keys) → `PersonRelationship` (join entity with `@NamedEntityGraph`). Start from the simplest one that matches your entity's shape.

## Layering Rules

- **Resource-only CRUD is fine.** Simple per-entity validation and persistence live directly in the resource method (see `GenderResource.create`). Do not introduce a service class just to forward calls.
- **Create a service when** logic is reused across resources, involves security-sensitive work (password hashing), or coordinates multiple repositories. Services are `@ApplicationScoped`, throw domain exceptions, and carry `@Transactional` on mutating methods.
- **Repositories** hold all query logic. Resources never write JPQL inline.

## Entity Conventions

From `Gender.java` — every entity follows this shape:

- Public fields, no accessors (Panache style)
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;`
- `@Table` with **named** unique constraints: `uk_{table}_{column}`
- `@Column` attributes mirror the Flyway DDL exactly (`nullable`, `length`, `unique`)
- Audit fields on every table: `createdAt`, `updatedAt` (`Instant`), `createdBy`, `updatedBy` (`String`), maintained by `@PrePersist`/`@PreUpdate` callbacks
- A no-arg constructor plus a convenience constructor for the natural fields

## Resource Conventions

- `@Path("/plural-noun")`, class-level `@RolesAllowed` — and the path **must also appear** in the route-protection config in `application.properties` (`quarkus.http.auth.permission.*`)
- Nested `@CheckedTemplate public static class Templates` declaring the full page + fragments (see `htmx-patterns` skill for fragment naming and the HX-Request dispatch pattern)
- Return `TemplateInstance`, never `Response` (`PersonRelationshipResource` still has `Response` wrappers — that is the known deviation, don't copy it)
- `@Transactional` on every POST/PUT/DELETE endpoint method
- Audit values from identity: `securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName()`

## Validation

User-facing validation **returns the modal fragment with an error string and the user's input preserved** — it does not throw:

```java
if (code == null || code.isBlank()) {
    return Templates.gender$modal_create(gender, "Code is required.");
}
if (genderRepository.existsByCode(gender.code)) {
    return Templates.gender$modal_create(gender, "Code already exists.");
}
```

On update, uniqueness checks exclude the current row: `existsByCodeAndIdNot(code, id)`.

## Exceptions

Domain exceptions are for service-layer failures, not form validation. `GlobalExceptionMapper` maps them and content-negotiates HTML (`error.html`) vs JSON, logging with a UUID reference ID:

| Exception | HTTP status |
|-----------|-------------|
| `EntityNotFoundException` | 404 |
| `UniqueConstraintException` | 409 |
| `ReferentialIntegrityException` | 409 |
| anything else | 500, generic message (stack trace only in dev profile) |

## Java 21 Notes

Records are welcome for value types (search criteria, projections); pattern matching and switch expressions are fine. But do **not** add DTO layers between resources and templates — Qute templates consume entities directly, and `@CheckedTemplate` gives compile-time safety.

## Related Skills

- `htmx-patterns` — fragment naming, OOB swaps, HX-Request dispatch
- `postgresql-java` — migrations, DDL conventions, repository query naming
- `java-code-review` — the review checklist that enforces all of the above
