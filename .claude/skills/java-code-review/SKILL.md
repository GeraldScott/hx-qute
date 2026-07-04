---
name: java-code-review
description: Use when reviewing Java code in this project — pull requests, pre-commit review, auditing a resource/entity/repository/migration for convention drift, or checking security of new endpoints.
---

# Project Code Review Checklist

Review against *this project's* conventions (defined in `java-patterns`, `postgresql-java`, `htmx-patterns`), not generic Java advice. In particular:

**Do NOT flag these — they are deliberate choices here:**
- Entities passed directly to Qute templates (there is no DTO layer; `@CheckedTemplate` provides the type safety)
- Public fields on entities without getters/setters (Panache style)
- CRUD validation living in resource methods rather than a service layer (correct for simple entities — `GenderResource` is the exemplar)

## Resource Endpoints

- [ ] Returns `TemplateInstance`, not `Response` wrappers
- [ ] `@Transactional` on every POST/PUT/DELETE method
- [ ] Class-level `@RolesAllowed`, **and** the path is listed in a `quarkus.http.auth.permission.*` entry in `application.properties` — a new endpoint path missing from route protection is a security finding, not a style nit
- [ ] List endpoint dispatches on `@HeaderParam("HX-Request")`: fragment for HTMX, full page otherwise
- [ ] Validation failures re-render the modal fragment with an error message *and the user's input preserved* — never throw for form validation
- [ ] Update-time uniqueness uses `existsBy…AndIdNot(value, id)`, not `existsBy…`
- [ ] DELETE checks referential integrity via the repository's `isReferencedBy…` method and returns a modal error instead of deleting (the `// TODO` blocks in `GenderResource.delete`, `TitleResource`, `RelationshipResource` are known violations of this rule — new code must not copy them)
- [ ] Audit fields set from `SecurityIdentity` with the `"system"` anonymous fallback

## Entities & Persistence

- [ ] `@Column` lengths/nullability/uniqueness mirror the Flyway DDL exactly
- [ ] Audit columns + `@PrePersist`/`@PreUpdate` callbacks present
- [ ] Unique constraints named `uk_{table}_{column}`
- [ ] Queries parameterized (`?1`) — string concatenation into JPQL/SQL is a critical finding
- [ ] Associations that render in lists use `@NamedEntityGraph` or explicit fetch to avoid N+1 (exemplar: `PersonRelationship`)

## Migrations

- [ ] Filename follows `V{major}.{entity-seq}.{step}__{Description}.sql`; step 0 = DDL, step 1 = seed
- [ ] No edits to already-applied migrations (checksum failure at startup)
- [ ] New table has the four audit columns and `TIMESTAMP WITH TIME ZONE`

## Templates & HTMX

- [ ] Fragment names and element IDs follow the `htmx-patterns` conventions (`{entity}$table`, `#{entity}-modal-body`, `create-{entity}-{field}` input IDs)
- [ ] No `{x.raw}` on user-supplied content (XSS); Qute escapes by default — keep it that way
- [ ] `rendered=false` on page fragments

## Spring-isms

- [ ] Run the Spring-ism smell checklist from the `quarkus-patterns` skill on any ported or Spring-flavored code (`org.springframework` imports, stereotype annotations, repository interfaces with derived queries, `@MockBean`, Spring cron strings)

## Security & Hygiene

- [ ] No credentials/secrets in code, config, or logs (dev credentials belong in `.env`, seed users in migrations)
- [ ] Passwords hashed with `BcryptUtil.bcryptHash(pw, 12)`; password rules honor `app.security.password.*` config (NIST SP 800-63B-4: length 15–128, no composition rules)
- [ ] New exceptions extend the `service.exception` hierarchy so `GlobalExceptionMapper` assigns the right status (404/409)
- [ ] Empty catch blocks, swallowed exceptions, or `printStackTrace` → finding (log via JBoss `Logger` as in `GlobalExceptionMapper`)

## Severity Labels

Use these in review output: 🔴 **CRITICAL** (security, data loss, missing route protection, SQL injection) · 🟡 **IMPORTANT** (convention violation, missing `@Transactional`, N+1) · 💡 **Suggestion**.

## Related Skills

- `java-patterns`, `postgresql-java`, `htmx-patterns` — the conventions this checklist enforces
