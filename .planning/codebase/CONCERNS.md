# Concerns

## Technical Debt

### No Tests
The test directory is completely empty. No unit, integration, or E2E tests exist. This is the highest-priority concern for code quality and regression prevention.

### Empty Packages
`dto/` and `filter/` packages exist but are empty - reserved placeholders that may indicate incomplete design.

### Validation Inconsistency
- `UserLogin` uses Bean Validation annotations (`@NotBlank`, `@Email`, `@Size`) because `quarkus-security-jpa` requires them
- All other entities rely on manual validation in Resource methods
- No consistent validation strategy across the codebase

### Stale TODO in GenderResource
`GenderResource.java:232-238` contains a commented-out TODO for referential integrity checking on gender delete. The Person entity already exists and references Gender, but the check is not implemented - deleting a gender used by persons will cause a database-level FK violation instead of a user-friendly error.

### Duplicated Validation Logic
Email regex validation is duplicated between `PersonResource` (line 211) and `AuthResource` (line 35) with slightly different patterns. Should be centralized.

### No Service Layer for Most Entities
Only `UserLogin` has a dedicated service (`UserLoginService`). Other entities (Person, Gender, Title, Relationship) have their business logic embedded directly in Resource classes, mixing HTTP concerns with business rules.

## Security Considerations

### Form Auth Redirect Pattern
Auth error handling uses query parameter redirects (`/signup?error=email_required`) which expose error types in URLs. Not a vulnerability but slightly unusual.

### JSON Error Response - Manual String Formatting
`GlobalExceptionMapper.java:119` builds JSON responses with `String.format()` instead of a proper JSON library. The `message.replace("\"", "\\\"")` escaping is fragile and could miss edge cases.

### CDN Dependencies
UIkit and HTMX are loaded from `cdn.jsdelivr.net` - external dependency for frontend assets. HTMX has SRI integrity check, UIkit does not.

## Performance

### N+1 Query Potential
`GraphResource.buildGraphData()` loads all persons and all relationships separately, then iterates. With large datasets, this could be slow. The `personRepository.listAll()` call may trigger lazy loading of `gender` for each person's `genderCode`.

### No Pagination on Graph/Network
The graph endpoint loads ALL persons and relationships into memory. No pagination or limit on the D3 visualization data.

### Missing Database Indexes
Filter queries in `PersonRepository.findByFilterPaged()` use `LOWER(firstName) LIKE`, `LOWER(lastName) LIKE`, `LOWER(email) LIKE` - these require functional indexes on PostgreSQL for performance at scale.

## Fragile Areas

### OOB Swap Coupling
The HTMX OOB (out-of-band) swap pattern tightly couples server responses to specific DOM element IDs (`#person-row-{id}`, `#person-table-container`). Template changes must carefully preserve these IDs or the CRUD operations break silently.

### Modal State Management
Modal show/hide relies on `hx-on::after-request` and `hx-on::load` JavaScript events coordinating with UIkit's modal API. This pattern works but is fragile - timing issues or UIkit version changes could cause modals to get stuck.

### Fragment Parameter Explosion
`PersonResource.Templates.person()` has 15 parameters. Fragment methods like `person$modal_success()` have 9. This makes the API surface error-prone and hard to refactor.
