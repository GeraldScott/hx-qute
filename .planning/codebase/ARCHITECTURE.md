# Architecture

## Pattern

**Server-rendered MVC with HTMX partial updates.** All HTML is generated server-side using Quarkus Qute templates. HTMX handles dynamic interactions (filtering, modals, inline updates) by swapping HTML fragments without full page reloads.

No client-side JavaScript framework. The only custom JS is `graph.js` for D3-based relationship visualization.

## Layers

```
Browser (HTMX + UIkit)
    |
Resource (JAX-RS endpoints) ── router/
    |
Service (business logic) ── service/
    |
Repository (data access) ── repository/
    |
Entity (JPA/Panache) ── entity/
    |
PostgreSQL (Flyway migrations) ── db/migration/
```

### Layer Responsibilities

- **Resource** (`src/main/java/io/archton/scaffold/router/`): JAX-RS endpoints that handle HTTP requests, validate form params, detect HTMX requests via `HX-Request` header, and return either full pages or Qute fragments.
- **Service** (`src/main/java/io/archton/scaffold/service/`): Business logic - `UserLoginService` (registration), `NetworkService` (BFS graph traversal), `PasswordValidator` (NIST-compliant validation).
- **Repository** (`src/main/java/io/archton/scaffold/repository/`): PanacheRepository implementations with custom queries (filtering, sorting, uniqueness checks).
- **Entity** (`src/main/java/io/archton/scaffold/entity/`): JPA entities with public fields (Panache style), lifecycle callbacks (`@PrePersist`, `@PreUpdate`), and audit fields.
- **Error** (`src/main/java/io/archton/scaffold/error/`): Global exception mapper producing HTML error pages or JSON responses.

## Data Flow

### Full Page Request
```
GET /persons → PersonResource.list() → full page template (base.html + person.html)
```

### HTMX Partial Request
```
GET /persons (HX-Request: true) → PersonResource.list() → person$table fragment only
```

### Modal CRUD Pattern
```
1. Button click → hx-get="/persons/create" → returns modal_create fragment → UIkit modal shows
2. Form submit → hx-post="/persons" → validates → returns modal_success fragment
3. modal_success uses hx-swap-oob to update table AND hx-on::load to close modal
```

### OOB (Out-of-Band) Swap Pattern
Used for updating multiple DOM elements in a single response:
- **Create**: Returns success message + full table refresh via `hx-swap-oob="innerHTML"` on `#person-table-container`
- **Update**: Returns success + single row replacement via `hx-swap-oob="outerHTML"` on `#person-row-{id}`
- **Delete**: Returns `hx-swap-oob="delete"` on `#person-row-{id}` to remove the row

## Key Abstractions

### CheckedTemplate + Fragments
Each Resource defines a `@CheckedTemplate` inner class `Templates` with:
- Full page methods: `person(...)` - renders complete page extending `base.html`
- Fragment methods: `person$table(...)`, `person$modal_create(...)` - renders partial HTML for HTMX

Fragment naming convention: `templateName$fragmentId` maps to `{#fragment id='fragmentId'}` in the `.html` file.

### Authentication
- Form-based auth via `quarkus-security-jpa` with `@UserDefinition` on `UserLogin` entity
- BCrypt password hashing (cost factor 12) via `BcryptUtil`
- Role-based access: `@RolesAllowed({"user", "admin"})` on resources
- Session-based with `quarkus-credential` cookie (strict SameSite, HttpOnly)

### Error Handling
`GlobalExceptionMapper` catches all `Throwable`:
- Custom exceptions (`EntityNotFoundException`, `UniqueConstraintException`, `ReferentialIntegrityException`) map to specific HTTP status codes
- HTML error page in browser, JSON in API contexts
- Stack traces shown only in dev profile
- UUID reference IDs for troubleshooting

## Entry Points

| Path | Resource | Description |
|------|----------|-------------|
| `/` | `IndexResource` | Landing page |
| `/signup`, `/logout` | `AuthResource` | Authentication flows |
| `/persons` | `PersonResource` | Person CRUD with pagination |
| `/persons/{id}/relationships` | `PersonRelationshipResource` | Person relationship management |
| `/genders` | `GenderResource` | Gender lookup (admin only) |
| `/titles` | `TitleResource` | Title lookup (admin only) |
| `/relationships` | `RelationshipResource` | Relationship type lookup (admin only) |
| `/graph` | `GraphResource` | D3 graph visualization + network view |
