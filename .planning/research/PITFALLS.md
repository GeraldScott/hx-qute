# Pitfalls Research

**Domain:** Retrofitting test suite and quality improvements onto existing Quarkus/HTMX server-rendered application
**Researched:** 2026-03-07
**Confidence:** HIGH (verified against Quarkus official docs, GitHub issues, and codebase analysis)

## Critical Pitfalls

### Pitfall 1: @TestTransaction Does Not Cover @BeforeEach Fixture Setup

**What goes wrong:**
Tests use `@TestTransaction` for rollback isolation but create fixture data in `@BeforeEach` methods. The fixture data persists across tests because `@TestTransaction` only wraps the test method itself -- `@BeforeEach` runs in a separate transaction context. Tests pass individually but fail when run together due to duplicate data from prior fixtures.

**Why it happens:**
Developers assume `@TestTransaction` works like Spring's `@Transactional` on test classes, covering the entire test lifecycle. In Quarkus, the transaction boundary is strictly the test method. This is compounded by the fact that moving `@TestTransaction` to the class level or adding it to the `@BeforeEach` method does not change the behavior.

**How to avoid:**
- Place all fixture creation inside the test method body, not in `@BeforeEach`
- For shared setup, create a `private void setupFixtures()` helper method called at the start of each test method (within the `@TestTransaction` boundary)
- If using `@BeforeEach` is unavoidable, use programmatic transaction management with `QuarkusTransaction.requiringNew()` and manually clean up in `@AfterEach`

**Warning signs:**
- Tests pass in isolation (`mvn test -Dtest=ClassName#method`) but fail when run as a class or suite
- `ConstraintViolationException` on unique columns during the second test in a class
- Test data accumulates across test runs when using `@Transactional` instead of `@TestTransaction`

**Phase to address:**
Phase 1 (test infrastructure setup) -- establish the fixture pattern before writing any tests

---

### Pitfall 2: Testing Authenticated Endpoints Without @TestSecurity

**What goes wrong:**
Tests call endpoints protected by `@RolesAllowed` and receive 401/302 responses instead of the expected HTML. Developers try to work around this by performing actual login flows via `/j_security_check` with REST Assured cookies, creating brittle tests coupled to the authentication mechanism.

**Why it happens:**
This application uses form-based authentication with cookie sessions. All resource endpoints except `/`, `/login`, `/signup`, `/logout` require authentication. The `PersonResource` requires `user` or `admin` roles, while `GenderResource`, `TitleResource`, and `RelationshipResource` require `admin`. Without understanding Quarkus's `@TestSecurity` annotation, developers either skip auth testing or implement fragile session-based workarounds.

**How to avoid:**
- Add `quarkus-test-security` dependency to `pom.xml` (currently missing)
- Use `@TestSecurity(user = "testuser@example.com", roles = "user")` on test methods for user-level endpoints
- Use `@TestSecurity(user = "admin@example.com", roles = "admin")` for admin endpoints
- Apply `@TestSecurity` at the class level when all tests in a class share the same role
- For the `SecurityIdentity` injection used in resources (audit fields like `createdBy`), `@TestSecurity` populates this automatically

**Warning signs:**
- Tests receive HTTP 302 redirects to `/?login=true` instead of HTML content
- REST Assured assertions fail on status code (expected 200, got 401)
- Tests use cookie jars or session management to pass authentication

**Phase to address:**
Phase 1 (test infrastructure setup) -- add `quarkus-test-security` dependency and establish auth test patterns before writing endpoint tests

---

### Pitfall 3: Asserting Full HTML Documents When Endpoint Returns Fragments

**What goes wrong:**
Tests always request full-page HTML and parse it with Jsoup, missing the fact that the same endpoint returns different content based on the `HX-Request` header. Fragment responses are never tested, leading to false confidence. Or conversely, tests parse fragment HTML as if it were a complete document and Jsoup silently wraps it, causing selector-based assertions to pass on incorrect structure.

**Why it happens:**
Every resource in this codebase checks for `HX-Request: true` header and returns either a full page (with base template, navigation, etc.) or a Qute fragment (just the table, modal content, etc.). For example, `PersonResource.list()` returns `Templates.person(...)` for full pages or `Templates.person$table(...)` for HTMX requests. Tests that only check one path leave the other completely untested.

**How to avoid:**
- Write paired tests for every list endpoint: one without `HX-Request` header (full page), one with `.header("HX-Request", "true")` (fragment)
- For fragment responses, use Jsoup's `Jsoup.parseBodyFragment()` instead of `Jsoup.parse()` to avoid unexpected document wrapping
- Assert that full-page responses contain the base layout elements (navigation, modal shell) while fragment responses do NOT
- Validate OOB elements: success fragments (like `person$modal_success`) contain both a primary response AND elements with `hx-swap-oob` attributes

**Warning signs:**
- Tests only assert status codes without checking HTML content structure
- All endpoint tests omit the `HX-Request` header
- No tests validate OOB swap element IDs match the expected DOM targets

**Phase to address:**
Phase 1 (test patterns) -- define the dual-response test pattern as a standard before writing endpoint tests

---

### Pitfall 4: Hibernate First-Level Cache Masking Data Issues in Tests

**What goes wrong:**
Repository tests persist an entity, then immediately query for it and get back the same in-memory instance from Hibernate's first-level cache. The test passes, but the actual database state may differ (e.g., `@PrePersist` callbacks not reflected, column truncation not caught, constraint violations deferred). When the same operation runs in production with separate transactions, it fails.

**Why it happens:**
Under `@TestTransaction`, the test method and the code being tested share the same Hibernate `Session` and first-level cache. Entities persisted in the test are returned from cache on subsequent queries without hitting the database. This is especially problematic for entities like `Person` which have `@PrePersist` callbacks that normalize email to lowercase -- the test may see the un-normalized version from cache.

**How to avoid:**
- Call `entityManager.flush()` after persist operations to force SQL execution and trigger constraint checks
- Call `entityManager.clear()` before re-querying to force database reads, bypassing the cache
- Create a test utility method: `flushAndClear()` that does both in sequence
- For entities with `@PrePersist`/`@PreUpdate` callbacks (Person, UserLogin), always flush+clear before asserting on computed fields

**Warning signs:**
- Tests pass but the same operations fail in the running application
- Unique constraint violations not caught in tests but triggered in production
- Assertions on `createdAt`, `updatedAt`, or normalized `email` pass with `null` values

**Phase to address:**
Phase 1 (test infrastructure) -- create a `TestHelper` utility with `flushAndClear()` used across all repository tests

---

### Pitfall 5: Missing Referential Integrity Checks Cause Silent Data Corruption

**What goes wrong:**
Deleting a Gender, Title, or Relationship that is referenced by Person records succeeds at the application level but throws a database-level `PersistenceException` wrapping a FK violation. The user sees a generic 500 error page instead of a friendly message. The existing `GenderResource.delete()` has a TODO comment acknowledging this gap (line 232-238).

**Why it happens:**
The application currently relies solely on PostgreSQL FK constraints (`fk_person_gender`, `fk_person_title`) to enforce referential integrity. There are no application-level checks before deletion. The `ReferentialIntegrityException` class exists but is never thrown for Gender, Title, or Relationship entities. The architecture document describes the pattern (section 6.2) but it was never implemented for these entities.

**How to avoid:**
- Before writing delete tests, implement referential integrity checks in repositories: `PersonRepository.countByGenderId(Long)`, `PersonRepository.countByTitleId(Long)`
- Implement the checks in the resource delete methods (or introduce service layers) before attempting deletion
- Write tests that specifically verify: "attempting to delete a Gender used by a Person returns an error message, not a 500"
- The test should seed a Person with a Gender, attempt to delete that Gender, and assert the appropriate error response

**Warning signs:**
- Delete operations return HTTP 500 instead of a user-friendly error
- `PersistenceException` or `ConstraintViolationException` in server logs during delete operations
- The TODO at `GenderResource.java:232-238` remains unresolved

**Phase to address:**
Phase 1 (referential integrity) -- must be implemented BEFORE writing delete tests, otherwise tests would codify the broken behavior

---

### Pitfall 6: Flyway Migration Errors Silently Swallowed in Tests

**What goes wrong:**
A syntax error in a Flyway migration SQL script causes an exception during test startup, but due to a class-loader issue, the migration error is hidden. All tests are skipped (not failed) and the build succeeds. This creates a false green build where no tests actually ran.

**Why it happens:**
This is a known Quarkus bug (GitHub issue #43926). When Flyway encounters a SQL syntax error during test initialization, the exception handling code itself fails because the class loader cannot find the migration script to report the error. The result is that the test class is "ignored" rather than "failed," and Maven Surefire reports success.

**How to avoid:**
- Always check the Surefire report for "Tests run: 0" -- zero tests run means something went wrong at startup
- Add a CI pipeline check that fails if zero tests were executed
- Consider adding a simple "canary" test that asserts `true` to detect when the entire test suite is being skipped
- Pin Flyway migration scripts to a known-good state before adding test infrastructure

**Warning signs:**
- `mvn test` output shows "Tests run: 0, Failures: 0, Errors: 0, Skipped: 0"
- Build succeeds but no test classes are mentioned in the output
- New migration scripts added alongside new tests, and both "work"

**Phase to address:**
Phase 1 (CI/build setup) -- add minimum test count assertion to build pipeline

---

## Technical Debt Patterns

Shortcuts that seem reasonable but create long-term problems.

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Testing only full-page responses, not fragments | Faster test writing, less complexity | HTMX fragment bugs go undetected; OOB swap regressions missed | Never -- fragments are the primary interaction path |
| Using `@Transactional` instead of `@TestTransaction` | Simpler setup, no cache issues | Test data leaks between tests, causing ordering-dependent failures | Never -- always prefer `@TestTransaction` |
| Hardcoding test data IDs (e.g., `findById(1L)`) | Quick fixture setup | Breaks when Flyway seed data changes; couples tests to migration state | Only for read-only tests against seed data that is version-controlled |
| Skipping `entityManager.flush()` in repository tests | Tests run faster | Deferred constraint violations not caught; `@PrePersist` behavior untested | Never for entities with lifecycle callbacks or unique constraints |
| Inline validation in Resources without service layer | No extra classes, direct flow | Validation logic cannot be tested without HTTP layer; duplicate logic across resources | Acceptable for simple lookup tables (Gender, Title) if resource tests are thorough |

## Integration Gotchas

Common mistakes when connecting to external services.

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| DevServices PostgreSQL | Configuring `quarkus.datasource.jdbc.url` in `application.properties` without `%prod` prefix, disabling DevServices for tests | Use `%prod.quarkus.datasource.jdbc.url=...` so DevServices auto-provisions in dev/test mode |
| Flyway + DevServices | Setting `%test.quarkus.flyway.locations` with multiple paths -- only the first is applied | Use a single migration location or verify all locations are applied; avoid test-specific migration folders |
| Flyway + Hibernate ORM | Setting `%test.quarkus.hibernate-orm.database.generation=update` alongside Flyway -- ordering conflict | Use `quarkus.hibernate-orm.schema-management.strategy=none` (already correct in this project) and let Flyway manage schema exclusively |
| REST Assured + Form Auth | Sending requests without `@TestSecurity`, getting redirect responses | Add `quarkus-test-security` dependency; annotate tests with `@TestSecurity(user, roles)` |
| Jsoup + HTML Fragments | Parsing HTMX fragments with `Jsoup.parse()` which adds `<html><body>` wrapper | Use `Jsoup.parseBodyFragment()` for fragment responses; use `Jsoup.parse()` for full-page responses |

## Performance Traps

Patterns that work at small scale but fail as usage grows.

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Starting a full Quarkus instance for every test class | Test suite takes minutes; developers skip running tests | Use `@QuarkusTest` which reuses the same instance across test classes (default behavior) | Immediate -- even 10 test classes will be slow if each restarts Quarkus |
| Loading all entities in repository tests to assert count | Tests pass with seed data | Assert specific entities by unique field, not by count; counts change with seed data | When seed data migrations add more records |
| Not using `@TestTransaction` causing test pollution | Flaky tests that depend on execution order | Always use `@TestTransaction` for any test that mutates data | As soon as you have 2+ tests that write to the same table |
| Full page renders in every test (no fragment testing) | Slow test execution from rendering complete page templates | Test fragments directly where possible -- they render faster and test the actual HTMX contract | When template complexity grows (15+ parameters per render) |

## Security Mistakes

Domain-specific security issues beyond general web security.

| Mistake | Risk | Prevention |
|---------|------|------------|
| Not testing role-based access control boundaries | Admin-only endpoints (Gender, Title, Relationship CRUD) accessible to regular users | Write explicit tests: `@TestSecurity(roles = "user")` accessing `/genders` should return 403 |
| Testing only happy-path authentication | Missing tests for unauthenticated access to protected routes | Test that unauthenticated GET `/persons` returns 302 redirect to login page |
| Not testing `SecurityIdentity` propagation in audit fields | `createdBy`/`updatedBy` fields may be null or "system" in production | After POST/PUT operations, verify the audit field matches the `@TestSecurity` user |
| Ignoring password validation in tests | `PasswordValidator` has NIST-compliant rules (min 15 chars) but is never tested | Write unit tests for `PasswordValidator` edge cases: 14 chars (fail), 15 chars (pass), 128 chars (pass), 129 chars (fail) |

## UX Pitfalls

Common user experience mistakes in this domain.

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Testing only the success path for CRUD modals | Users encounter unhandled error states (500 pages instead of inline error messages) | Test validation errors: submit empty forms, duplicate codes, invalid emails -- verify error message appears in modal |
| Not testing OOB table refresh after CRUD operations | Table shows stale data after create/edit/delete | Assert that success responses contain `hx-swap-oob` elements with correct table container IDs |
| Ignoring the delete-in-use scenario | Users see a 500 error when deleting referenced data | Test delete of Gender/Title in use by Person -- verify error message in modal, not server error |
| Not testing pagination edge cases | Users see empty pages or broken navigation | Test page 0, last page, page beyond range, size changes |

## "Looks Done But Isn't" Checklist

Things that appear complete but are missing critical pieces.

- [ ] **Repository tests:** Often missing `flush()/clear()` before assertions -- verify that `@PrePersist` callbacks (email normalization, timestamps) are actually tested against database state
- [ ] **Endpoint tests:** Often missing the `HX-Request: true` variant -- verify both full-page and fragment responses are tested for every list endpoint
- [ ] **CRUD tests:** Often missing the "entity in use" delete scenario -- verify that delete of referenced Gender/Title/Relationship returns user-friendly error
- [ ] **Auth tests:** Often missing negative cases -- verify that unauthenticated and wrong-role access returns proper responses (302/403)
- [ ] **OOB swap tests:** Often missing ID validation -- verify that `hx-swap-oob` elements reference correct container IDs (`#gender-table-container`, `#person-row-{id}`)
- [ ] **Form validation tests:** Often only test one error at a time -- verify the first validation error is returned (not all at once, matching the sequential validation pattern in resources)
- [ ] **Test count verification:** Often no check that tests actually ran -- verify Surefire reports non-zero test count in CI

## Recovery Strategies

When pitfalls occur despite prevention, how to recover.

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Test data leaking between tests | LOW | Switch from `@Transactional` to `@TestTransaction` on all test methods; remove any manual cleanup code |
| Hibernate cache giving false positives | LOW | Add `flushAndClear()` calls before assertions; re-run failed tests to verify |
| Auth tests getting 302 instead of content | LOW | Add `quarkus-test-security` dependency; annotate test class with `@TestSecurity` |
| Fragment tests parsing wrong HTML | LOW | Switch from `Jsoup.parse()` to `Jsoup.parseBodyFragment()` for fragment responses |
| Flyway migration error silently passing build | MEDIUM | Add CI check for minimum test count; add canary test; review build logs for "Tests run: 0" |
| Referential integrity not checked before delete tests written | MEDIUM | Implement repository count methods (`countByGenderId`, etc.) and pre-delete checks in resources; rewrite delete tests to expect error responses |
| OOB swap IDs out of sync between template and test | HIGH | Audit all templates for `hx-swap-oob` element IDs; create constants or test utilities that reference canonical ID patterns; consider extracting ID patterns to a shared location |

## Pitfall-to-Phase Mapping

How roadmap phases should address these pitfalls.

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| `@TestTransaction`/`@BeforeEach` mismatch | Phase 1: Test infrastructure | First repository test passes in isolation AND as part of suite |
| Missing `@TestSecurity` for authenticated endpoints | Phase 1: Test infrastructure | `quarkus-test-security` in pom.xml; first endpoint test returns 200 with HTML |
| Full-page-only testing (missing fragment tests) | Phase 1: Test patterns | Every resource test class has both full-page and `HX-Request: true` test variants |
| Hibernate cache masking issues | Phase 1: Test utilities | `TestHelper.flushAndClear()` utility exists and is used in all repository tests |
| Missing referential integrity checks | Phase 1: Referential integrity | Gender/Title/Relationship delete with referenced Person returns error, not 500 |
| Flyway silent failure | Phase 1: CI/build | Build script asserts minimum test count > 0 |
| OOB swap ID drift | Phase 2: Template/integration tests | Dedicated tests parse success responses for `hx-swap-oob` elements with matching IDs |
| Validation path coverage | Phase 2: CRUD tests | Each form endpoint has tests for empty fields, invalid format, duplicate values |
| Role-based access boundaries | Phase 2: Security tests | Explicit 403 assertions for wrong-role access to admin endpoints |
| Pagination edge cases | Phase 2: Person endpoint tests | Tests for page=0, last page, out-of-range page values |

## Sources

- [Testing Your Application - Quarkus](https://quarkus.io/guides/getting-started-testing) -- official testing guide, `@TestTransaction` behavior
- [Security Testing - Quarkus](https://quarkus.io/guides/security-testing) -- `@TestSecurity` annotation, `quarkus-test-security` dependency
- [Can @TestTransaction be made to work with Jupiter @BeforeEach?](https://github.com/quarkusio/quarkus/discussions/40119) -- confirms `@BeforeEach` runs outside transaction boundary
- [Difference in behavior when running a test under TestTransaction](https://github.com/quarkusio/quarkus/discussions/33330) -- Hibernate cache issues with `@TestTransaction`
- [Flyway migration errors cause tests to be ignored](https://github.com/quarkusio/quarkus/issues/43926) -- silent test failure bug
- [LazyInitializationException with Many-To-Many](https://github.com/quarkusio/quarkus/issues/13604) -- Panache lazy loading in tests
- [htmx hx-swap-oob Attribute](https://htmx.org/attributes/hx-swap-oob/) -- OOB swap behavior and testing considerations
- [Simplified Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache) -- repository pattern, flush/clear guidance
- Codebase analysis: `GenderResource.java:232-238` (TODO for referential integrity), `PersonResource.java` (dual response pattern), `application.properties` (auth configuration)

---
*Pitfalls research for: Quarkus/HTMX test suite retrofit and quality improvements*
*Researched: 2026-03-07*
