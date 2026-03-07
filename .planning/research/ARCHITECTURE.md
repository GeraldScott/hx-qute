# Architecture Research

**Domain:** Testing layers, referential integrity, and quality improvements for Quarkus/HTMX server-rendered application
**Researched:** 2026-03-07
**Confidence:** HIGH

## Standard Architecture

### System Overview

```
+---------------------------------------------------------------------+
|                        Browser Layer                                 |
|  +----------+  +-----------+  +----------+  +--------------------+  |
|  | HTMX     |  | UIkit CSS |  | D3.js    |  | Form Submissions   |  |
|  | Requests |  | + Modals  |  | (Graph)  |  | (j_security_check) |  |
|  +-----+----+  +-----+-----+  +----+-----+  +--------+-----------+  |
|        |              |             |                  |              |
+--------+--------------+-------------+------------------+-------------+
         |              |             |                  |
+--------v--------------v-------------v------------------v-------------+
|                     Resource Layer (JAX-RS)                           |
|  router/ package                                                     |
|  +---------------+ +---------------+ +---------------+ +---------+  |
|  | PersonResource| | GenderResource| | GraphResource | | Auth    |  |
|  |               | |               | |               | | Resource|  |
|  +-------+-------+ +-------+-------+ +-------+-------+ +----+----+  |
|          |                  |                 |              |        |
+----------+------------------+-----------------+--------------+-------+
           |                  |                 |              |
+----------v------------------v-----------------v--------------v-------+
|                     Service Layer (CDI)                               |
|  service/ package                                                    |
|  +----------------+ +------------------+ +-------------------+       |
|  | UserLogin      | | NetworkService   | | PasswordValidator |       |
|  | Service        | | (BFS traversal)  | | (NIST rules)      |       |
|  +-------+--------+ +--------+---------+ +-------------------+       |
|          |                    |                                       |
+----------+--------------------+--------------------------------------+
           |                    |
+----------v--------------------v--------------------------------------+
|                    Repository Layer (Panache)                         |
|  repository/ package                                                 |
|  +----------+ +----------+ +----------+ +----------+ +----------+   |
|  | Person   | | Gender   | | Title    | | Relation | | PersonRel|   |
|  | Repo     | | Repo     | | Repo     | | Repo     | | Repo     |   |
|  +----+-----+ +----+-----+ +----+-----+ +----+-----+ +----+-----+   |
|       |             |            |            |            |         |
+-------+-------------+------------+------------+------------+---------+
        |             |            |            |            |
+-------v-------------v------------v------------v------------v---------+
|                      Entity Layer (JPA)                              |
|  entity/ package -- Plain POJOs with @Entity, public fields          |
|  Person, Gender, Title, Relationship, PersonRelationship, UserLogin  |
+----------------------------------------------------------------------+
        |
+-------v--------------------------------------------------------------+
|                     Database (PostgreSQL 17)                          |
|  Flyway migrations: V1.0.0 through V1.6.1                           |
|  Tables: person, gender, title, relationship, person_relationship,   |
|          user_login                                                  |
+----------------------------------------------------------------------+
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| Resource (router/) | HTTP request handling, HTMX detection, form validation, template rendering | JAX-RS `@Path` classes with `@CheckedTemplate` inner class; returns `TemplateInstance` |
| Service (service/) | Security-sensitive operations, multi-entity coordination, business rules | CDI `@ApplicationScoped` beans; only exists when justified (UserLogin, Network, PasswordValidator) |
| Repository (repository/) | Data access, custom queries, uniqueness checks, referential integrity queries | `PanacheRepository<Entity>` with `@ApplicationScoped`; custom finders, filters, sort builders |
| Entity (entity/) | Data structure, JPA mapping, audit lifecycle callbacks | Plain POJOs with public fields; `@PrePersist`/`@PreUpdate` for timestamps; no business logic |
| Template (templates/) | HTML generation, fragment definitions for HTMX partial updates | Qute templates with `{#fragment id='...' rendered=false}` for HTMX swap targets |
| Error Handler (error/) | Global exception mapping to HTTP responses | `ExceptionMapper<Throwable>` producing HTML or JSON based on `Accept` header |
| Migration (db/migration/) | Schema evolution | Flyway SQL scripts: DDL at `.0`, seed data at `.1` |

## Recommended Project Structure

```
src/
+-- main/java/io/archton/scaffold/
|   +-- entity/                    # JPA entities (plain POJOs)
|   +-- repository/                # PanacheRepository implementations
|   +-- service/                   # Business logic (only when justified)
|   |   +-- exception/             # Custom runtime exceptions
|   +-- router/                    # JAX-RS resource endpoints
|   +-- error/                     # Global exception mapper
+-- main/resources/
|   +-- application.properties     # App + security config
|   +-- db/migration/              # Flyway SQL migrations
|   +-- templates/                 # Qute templates
|   |   +-- base.html              # Layout template
|   |   +-- error.html             # Error page
|   |   +-- fragments/             # Shared fragments (navigation)
|   |   +-- {Resource}Resource/    # Per-resource template directories
|   +-- META-INF/resources/        # Static assets (CSS, JS, images)
+-- test/java/io/archton/scaffold/
    +-- repository/                # Repository tests (@QuarkusTest + @TestTransaction)
    +-- service/                   # Service tests (unit + integration)
    +-- router/                    # Resource/endpoint tests (REST Assured + HTML parsing)
    +-- entity/                    # Entity validation tests (if Bean Validation added)
```

### Structure Rationale

- **test/ mirrors main/ packages:** Each layer gets its own test package, matching production code organization for discoverability.
- **repository/ tests use @TestTransaction:** Auto-rollback ensures test isolation against the real DevServices PostgreSQL instance.
- **router/ tests use REST Assured:** Full HTTP stack testing validates HTMX header detection, HTML content, status codes, and security annotations.
- **service/ tests mix unit and integration:** `PasswordValidator` is pure unit (no CDI needed); `UserLoginService` and `NetworkService` need `@QuarkusTest` for database access.

## Architectural Patterns

### Pattern 1: HTMX Fragment Discrimination

**What:** Resource methods inspect the `HX-Request` header to return either a full page (with base layout) or a fragment-only partial response for the same URL.
**When to use:** Every list/table endpoint that supports both initial page load and HTMX-driven filtering, sorting, or pagination.
**Trade-offs:** Simple and effective; but means the same endpoint serves two response shapes, requiring tests for both paths.

**Example:**
```java
@GET
@Produces(MediaType.TEXT_HTML)
public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
    List<Entity> items = repository.listAllOrdered();
    if ("true".equals(hxRequest)) {
        return Templates.entity$table(items);  // Fragment only
    }
    return Templates.entity("Title", "page", userName, items);  // Full page
}
```

**Testing implication:** Every list endpoint needs two test cases -- one without `HX-Request` header (assert full HTML page structure) and one with the header (assert fragment-only response without `<html>` wrapper).

### Pattern 2: OOB (Out-of-Band) Swap for Multi-Element Updates

**What:** HTMX responses include `hx-swap-oob` attributes to update multiple DOM elements in a single response. Used after create/update/delete to simultaneously close a modal and refresh the data table.
**When to use:** Any mutation that needs to update the modal content AND the underlying table/list.
**Trade-offs:** Powerful but creates tight coupling between response HTML and specific DOM element IDs. Tests must verify both the primary content and OOB elements are present.

**Testing implication:** Success responses must be parsed to verify they contain both the success message AND the OOB-swapped elements (table container or row). Jsoup `select()` on `[hx-swap-oob]` attributes is the verification strategy.

### Pattern 3: Modal-Based CRUD with Error Re-rendering

**What:** CRUD operations use modal dialogs. On validation failure, the server re-renders the form inside the modal with error messages and preserved field values. On success, it closes the modal and updates the page.
**When to use:** All entity create/edit/delete flows.
**Trade-offs:** Great UX, but the Resource method becomes complex with branching logic for validation errors vs. success.

**Testing implication:** Each mutation endpoint needs tests for: (1) happy path success, (2) each validation error case (verifying the error message appears and field values are preserved), (3) uniqueness constraint violations.

## Data Flow

### Request Flow

```
[Browser Action]
    |
    v
[Resource] --> checks HX-Request header --> [Full page OR fragment]
    |
    v
[Validation] --> error? --> [Re-render form with error message]
    |
    v (valid)
[Repository/Service] --> [Database operation]
    |
    v
[Success Response] --> [Modal close + OOB table/row update]
```

### Testing Data Flow

```
[Test Method]
    |
    +-- @TestSecurity(user="admin", roles="admin")  -- simulates auth
    |
    v
[REST Assured] --> HTTP request with/without HX-Request header
    |
    +-- Form params for POST/PUT (application/x-www-form-urlencoded)
    |
    v
[Quarkus Test Server] --> full CDI + JPA + DevServices PostgreSQL
    |
    v
[Response] --> assert status code
    |
    +-- [HTML body] --> Jsoup.parse() --> select() for content verification
    |
    +-- [HX-Request responses] --> verify fragment structure (no <html> wrapper)
    |
    +-- [OOB elements] --> select("[hx-swap-oob]") --> verify presence
```

### Key Data Flows

1. **Full page load:** Browser GET -> Resource detects no HX-Request -> queries Repository -> renders full Qute template extending base.html -> returns complete HTML page.
2. **HTMX partial update:** HTMX GET with `HX-Request: true` -> Resource detects header -> queries Repository -> renders fragment only (e.g., `person$table`) -> HTMX swaps into DOM.
3. **Modal CRUD:** Button click -> HTMX GET for form fragment -> modal shows -> form submit -> POST/PUT -> validation -> success fragment with OOB swap -> modal closes + table updates.
4. **Referential integrity check:** DELETE request -> Resource/Service checks if entity is referenced -> if yes, re-render delete modal with error message -> if no, delete and return OOB row removal.

## Testing Layer Architecture

### Layer 1: Unit Tests (No CDI Container)

| Target | What to Test | Framework | Notes |
|--------|-------------|-----------|-------|
| `PasswordValidator` | Min/max length rules, edge cases | JUnit 5 only | Pure logic, no injections needed; instantiate directly |
| Entity lifecycle callbacks | `@PrePersist`/`@PreUpdate` set timestamps, normalize email | JUnit 5 only | Call `onCreate()`/`onUpdate()` directly on entity instances |
| `computePageWindow()` | Pagination window calculation | JUnit 5 only | Extract as static/package-private utility if not already |

**Build order:** First -- no dependencies, instant feedback, validate business rules.

### Layer 2: Repository Integration Tests (@QuarkusTest + @TestTransaction)

| Target | What to Test | Framework | Notes |
|--------|-------------|-----------|-------|
| `PersonRepository` | `findByFilterPaged`, `existsByEmail`, `existsByEmailAndIdNot`, sort/filter queries | @QuarkusTest + @TestTransaction | DevServices PostgreSQL auto-provisioned |
| `GenderRepository` | `listAllOrdered`, uniqueness checks | @QuarkusTest + @TestTransaction | Test with seed data from Flyway migrations |
| `TitleRepository` | Same patterns as Gender | @QuarkusTest + @TestTransaction | |
| `RelationshipRepository` | Same patterns | @QuarkusTest + @TestTransaction | |
| `PersonRelationshipRepository` | `findBySourcePersonWithFilter`, Entity Graph loading, `exists` checks | @QuarkusTest + @TestTransaction | Complex queries with multiple joins |
| Referential integrity queries | `isReferencedByXxx` methods (to be added) | @QuarkusTest + @TestTransaction | Verify before-delete checks work |

**Build order:** Second -- depends on entities and Flyway migrations being correct. Tests validate query logic against real PostgreSQL.

### Layer 3: Service Integration Tests (@QuarkusTest + @TestTransaction)

| Target | What to Test | Framework | Notes |
|--------|-------------|-----------|-------|
| `UserLoginService` | Create with BCrypt hashing, duplicate email exception | @QuarkusTest + @TestTransaction | Verify `UniqueConstraintException` thrown |
| `NetworkService` | BFS traversal at depth 1, 2, 3; disconnected nodes; cycles | @QuarkusTest + @TestTransaction | Requires multi-entity test data setup |

**Build order:** Third -- depends on repositories working correctly.

### Layer 4: Resource/Endpoint Tests (@QuarkusTest + @TestSecurity + REST Assured)

| Target | What to Test | Framework | Notes |
|--------|-------------|-----------|-------|
| Full page responses | Status 200, HTML structure, page title, navigation | REST Assured + Jsoup | Without `HX-Request` header |
| HTMX fragment responses | Fragment-only HTML (no `<html>` wrapper), correct content | REST Assured + Jsoup | With `HX-Request: true` header |
| Form validation errors | Error messages rendered, field values preserved | REST Assured + Jsoup | POST/PUT with invalid data |
| Uniqueness violations | Duplicate email/code returns error in modal | REST Assured + Jsoup | Create duplicate records |
| Delete with referential integrity | Error message when entity in use; success when not | REST Assured + Jsoup | After referential integrity is implemented |
| OOB swap responses | `hx-swap-oob` elements present in success responses | REST Assured + Jsoup | Parse HTML for OOB attributes |
| Security enforcement | 401/redirect for unauthenticated, 403 for wrong role | REST Assured | Test without @TestSecurity, or with wrong role |
| Auth flows | Signup, login redirect, logout | REST Assured | Test form submission and redirect behavior |

**Build order:** Fourth -- depends on all lower layers. These are the most comprehensive tests, exercising the full stack.

### Layer 5: E2E Browser Tests (Chrome DevTools MCP)

| Target | What to Test | Framework | Notes |
|--------|-------------|-----------|-------|
| Modal open/close | UIkit modal shows on button click, closes on success | chrome-devtools MCP | JavaScript behavior cannot be tested with REST Assured |
| HTMX swap visual verification | Table updates after filter/sort/CRUD | chrome-devtools MCP | Verifies actual DOM manipulation |
| Navigation flow | Sidebar links, page transitions | chrome-devtools MCP | Tests HTMX `hx-push-url` behavior |
| D3 graph rendering | Network visualization loads and displays | chrome-devtools MCP | JavaScript-dependent feature |

**Build order:** Fifth (optional) -- highest cost, lowest ROI for a reference app. Use sparingly for JavaScript-dependent flows only.

## Referential Integrity Architecture

### Current State

Referential integrity checks are **missing from the application layer**. The database has FK constraints, so violations cause raw PostgreSQL errors surfaced as 500 Internal Server Error rather than user-friendly messages.

### Required Pattern

```
DELETE /genders/{id}
    |
    v
GenderResource.delete(id)
    |
    v
Check: genderRepository.isReferencedByPerson(id)
    |
    +-- YES --> return modal_delete with error "Cannot delete: used by N person(s)"
    |
    +-- NO  --> genderRepository.deleteById(id)
                return modal_delete_success(id)
```

### Entities Requiring Referential Integrity Checks

| Parent Entity | Referenced By | Check Method to Add |
|---------------|--------------|---------------------|
| Gender | Person.gender | `GenderRepository.isReferencedByPerson(Long genderId)` |
| Title | Person.title | `TitleRepository.isReferencedByPerson(Long titleId)` |
| Relationship | PersonRelationship.relationship | `RelationshipRepository.isReferencedByPersonRelationship(Long relationshipId)` |
| Person | PersonRelationship.sourcePerson, PersonRelationship.relatedPerson | `PersonRepository.isReferencedByPersonRelationship(Long personId)` |

### Build Order for Referential Integrity

1. Add `isReferencedByXxx()` methods to repositories (inject referencing repository, use `count()`)
2. Add repository tests verifying the check methods
3. Update Resource delete methods to call the check before deletion
4. Add Resource tests for both "in use" and "not in use" delete scenarios

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| 0-1k users | Current monolith is fine. DevServices PostgreSQL for dev/test. Focus on test coverage. |
| 1k-100k users | Add functional indexes for LOWER() filter queries. Consider connection pooling config. |
| 100k+ users | Beyond scope for reference app. Would need caching, CDN for assets, read replicas. |

### Scaling Priorities

1. **First bottleneck:** Missing tests -- regressions from code changes are the immediate risk. Automated tests are the single highest-value investment.
2. **Second bottleneck:** Missing referential integrity checks at application layer -- causes confusing 500 errors on FK violations instead of user-friendly messages.

## Anti-Patterns

### Anti-Pattern 1: Testing HTML Responses with String Matching

**What people do:** `body(containsString("<td>John</td>"))` in REST Assured tests.
**Why it's wrong:** Brittle -- breaks on whitespace changes, attribute reordering, or extra classes. Cannot navigate DOM structure.
**Do this instead:** Parse with Jsoup: `Document doc = Jsoup.parse(response.body().asString()); Elements rows = doc.select("table tbody tr");` then assert on structured elements.

### Anti-Pattern 2: Skipping HTMX Header Tests

**What people do:** Only test full page responses, ignoring the `HX-Request: true` code path.
**Why it's wrong:** The HTMX fragment path is what users experience 90% of the time (filtering, sorting, CRUD). Bugs here are invisible in full-page-only tests.
**Do this instead:** Every list endpoint gets two tests: one without `HX-Request` (full page) and one with it (fragment only). Verify the fragment does NOT contain `<html>` or `<body>` tags.

### Anti-Pattern 3: Testing Against Seed Data Without Knowing Contents

**What people do:** Assert `assertEquals(5, results.size())` based on Flyway seed migration counts, without checking what the seed data contains.
**Why it's wrong:** Seed data changes break tests silently. Tests become coupled to migration contents.
**Do this instead:** For repository tests, create test data explicitly with `@TestTransaction`. For endpoint tests that need existing data (e.g., dropdown options), either create data in test setup or assert on known seed values with clear comments explaining the dependency.

### Anti-Pattern 4: Testing Referential Integrity at the Database Level Only

**What people do:** Rely on PostgreSQL FK constraints to prevent invalid deletions, catching `PersistenceException` in a global handler.
**Why it's wrong:** Database exceptions are opaque -- the error message is a raw SQL constraint name, not a user-friendly message. The exception type varies by database vendor.
**Do this instead:** Check referential integrity explicitly in the application layer before attempting deletion. Return a specific `ReferentialIntegrityException` with a human-readable message.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| PostgreSQL 17 | JDBC via Quarkus DevServices (test), direct connection (prod) | Flyway manages schema; DevServices auto-provisions test containers |
| CDN (jsdelivr.net) | Static HTML references to HTMX 2.0.8 and UIkit 3.25 | Not testable server-side; E2E tests verify loading |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| Resource <-> Repository | Direct CDI injection (simple CRUD) | No service intermediary for Gender, Title, Relationship, Person |
| Resource <-> Service | CDI injection (complex operations) | Only for UserLogin (BCrypt), Network (BFS), PasswordValidator |
| Resource <-> Template | `@CheckedTemplate` compile-time binding | Fragment method signatures validated at build time |
| Template <-> HTMX | `hx-*` attributes referencing endpoint URLs and DOM IDs | Coupling validated only at E2E/browser level |
| Repository <-> Database | Panache/Hibernate ORM | Entity graphs for eager loading; DevServices for test isolation |

### Test Infrastructure Dependencies

| Dependency | Purpose | Already Available |
|------------|---------|-------------------|
| `quarkus-junit5` | `@QuarkusTest`, `@TestTransaction`, `@TestSecurity` | YES (in pom.xml) |
| REST Assured | HTTP endpoint testing | YES (transitive via quarkus-junit5) |
| Jsoup | HTML parsing for content verification | NO -- must be added to pom.xml as test dependency |
| DevServices PostgreSQL | Auto-provisioned test database | YES (via quarkus-jdbc-postgresql; requires Docker) |
| Flyway test migrations | Schema setup in test | YES (runs automatically with DevServices) |

## Build Order Summary

The dependency chain for implementing tests, referential integrity, and quality improvements:

```
1. Add Jsoup test dependency to pom.xml
   |
2. Unit tests (PasswordValidator, entity callbacks, pagination utility)
   |   No dependencies on other layers
   |
3. Repository tests (custom queries, filters, sort, uniqueness checks)
   |   Depends on: entities + migrations being correct
   |
4. Referential integrity -- repository methods
   |   Depends on: repository test patterns established in step 3
   |
5. Referential integrity -- resource integration
   |   Depends on: repository methods from step 4
   |
6. Service tests (UserLoginService, NetworkService)
   |   Depends on: repository layer tested in step 3
   |
7. Resource/endpoint tests (full page, fragments, validation, auth)
   |   Depends on: all lower layers + Jsoup from step 1
   |
8. Quality fixes (validation centralization, email regex dedup)
   |   Can happen in parallel with steps 3-7
   |
9. E2E browser tests (optional, for JavaScript-dependent flows)
      Depends on: all layers working + running dev server
```

## Sources

- Quarkus Testing Guide: https://quarkus.io/guides/getting-started-testing (HIGH confidence -- official docs)
- Quarkus Security Testing: `@TestSecurity` annotation verified via Context7 (HIGH confidence)
- `@TestTransaction` annotation: verified via Context7 Quarkus docs (HIGH confidence)
- REST Assured: transitive dependency of `quarkus-junit5` (HIGH confidence -- standard Quarkus testing stack)
- Jsoup: https://jsoup.org/ (HIGH confidence -- standard Java HTML parser, recommended in project's CLAUDE.md)
- DevServices: https://quarkus.io/guides/datasource#devservices (HIGH confidence -- auto-provisions PostgreSQL for tests)
- Existing codebase analysis from `.planning/codebase/` documents (HIGH confidence -- direct code inspection)
- `docs/ARCHITECTURE.md` section 11 testing patterns (HIGH confidence -- project's own architecture guide)

---
*Architecture research for: Testing layers, referential integrity, and quality in Quarkus/HTMX application*
*Researched: 2026-03-07*
