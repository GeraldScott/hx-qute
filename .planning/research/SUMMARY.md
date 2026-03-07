# Project Research Summary

**Project:** hx-qute (Quarkus/HTMX Reference Application)
**Domain:** Test suite retrofit and quality improvements for server-rendered web application
**Researched:** 2026-03-07
**Confidence:** HIGH

## Executive Summary

This project is an existing Quarkus 3.30.3 reference application using HTMX + Qute for server-rendered HTML, backed by PostgreSQL with Flyway migrations. The application layer (entities, repositories, resources, templates, auth, error handling) is functionally complete with 27 Java source files, 12 Flyway migrations, and zero tests. The immediate goal is to retrofit a comprehensive test suite and fix known quality gaps -- primarily missing referential integrity checks on master data deletion (three TODO comments in code) and inconsistent validation patterns.

The recommended approach is to treat this as a two-phase effort: first establish test infrastructure, patterns, and referential integrity fixes simultaneously; then expand coverage to CRUD endpoints, security boundaries, and HTMX-specific fragment testing. The testing stack is well-established within the Quarkus ecosystem: JUnit 5, REST Assured, Jsoup for HTML parsing, `@TestSecurity` for auth simulation, and `@TestTransaction` for test isolation -- all BOM-managed except Jsoup. The architecture already follows clean layer separation (Resource -> Service -> Repository -> Entity), so the test structure mirrors it directly.

The key risks are subtle framework behaviors that cause false-positive tests: Hibernate first-level cache returning in-memory objects instead of database state, `@TestTransaction` not covering `@BeforeEach` fixture setup, and Flyway migration errors silently causing zero tests to run with a green build. All three have straightforward mitigations (flush/clear utility, in-method fixtures, canary test) but must be addressed in the infrastructure phase before writing production tests.

## Key Findings

### Recommended Stack

The testing stack is entirely Quarkus-native with one external addition. All versions are verified against the Quarkus 3.30.3 BOM.

**Core technologies:**
- **JUnit 5 (5.13.4):** Test framework -- BOM-managed, no alternative viable in Quarkus
- **REST Assured (5.5.6):** HTTP endpoint testing -- Quarkus auto-configures base URI and test port; de facto standard
- **Jsoup (1.20.1):** HTML DOM parsing -- critical for asserting server-rendered HTML structure with CSS selectors; NOT in BOM, requires explicit version
- **`@TestSecurity`:** Auth simulation -- injects identity directly into Quarkus security context, avoiding brittle cookie/session management
- **`@TestTransaction`:** Test isolation -- auto-rollback after each test method against real DevServices PostgreSQL
- **Mockito (5.20.0):** CDI bean mocking via `@InjectMock` -- BOM-managed, only for service isolation tests

**Dependencies to add:** `rest-assured`, `jsoup` (1.20.1), `quarkus-junit5-mockito`, `quarkus-test-security`

### Expected Features

**Must have (table stakes):**
- Automated test suite covering all CRUD endpoints -- zero tests exist; a reference app without tests is a prototype
- Referential integrity checks on Gender/Title/Relationship delete -- three TODOs in code; currently returns 500 on FK violation
- Person delete cascade/protect for relationships -- PersonRelationship references must be handled
- Integration tests for auth flows (registration, login, logout) -- proves security works
- HTMX fragment test patterns -- tests must cover both full-page and fragment response paths

**Should have (differentiators):**
- HTMX fragment testing patterns with Jsoup -- no established pattern exists in the Quarkus+HTMX ecosystem; this is the single biggest value-add
- Qute CheckedTemplate testing -- demonstrating compile-time template validation with test coverage
- OOB swap verification patterns -- testing multi-element updates via `hx-swap-oob`
- Modal-driven CRUD workflow test patterns -- real-world pattern most tutorials skip

**Defer (v2+):**
- Pagination on person list (P2, requires PanacheQuery pagination + HTMX partial replacement)
- `@QuarkusComponentTest` for unit-level service tests
- CI/CD pipeline configuration
- Native image build verification
- Performance and security penetration testing patterns

### Architecture Approach

The existing architecture is a clean four-layer stack (Resource -> Service -> Repository -> Entity) with Qute templates rendering HTML. Resources discriminate between full-page and fragment responses via the `HX-Request` header. Mutations follow a modal-based CRUD pattern with OOB swaps for simultaneous modal close and table refresh. The test architecture mirrors this with five layers: unit tests (no CDI), repository tests (`@TestTransaction`), service tests (`@TestTransaction`), resource/endpoint tests (REST Assured + Jsoup + `@TestSecurity`), and optional E2E browser tests.

**Major components:**
1. **Resource layer (router/)** -- HTTP handling, HTMX detection, form validation, template rendering; primary test target
2. **Repository layer (repository/)** -- Panache data access, custom queries, referential integrity queries (to be added)
3. **Service layer (service/)** -- Only exists for UserLogin (BCrypt), Network (BFS), PasswordValidator (NIST rules)
4. **Template layer (templates/)** -- Qute templates with `{#fragment}` definitions for HTMX partial updates
5. **Error handler (error/)** -- Global exception mapper producing HTML or JSON based on Accept header

### Critical Pitfalls

1. **`@TestTransaction` does not cover `@BeforeEach`** -- Fixture data created in `@BeforeEach` persists across tests, causing duplicate key violations. Place all fixture creation inside the test method body or use a helper method called within each test.
2. **Hibernate first-level cache masks data issues** -- Persisted entities are returned from cache without hitting the database. Always call `entityManager.flush()` then `clear()` before re-querying in repository tests.
3. **Missing `@TestSecurity` causes 302 redirects instead of content** -- All endpoints except public ones require auth. Without `@TestSecurity`, tests get redirect responses. Add `quarkus-test-security` dependency and annotate test classes/methods.
4. **Fragment vs full-page response testing gap** -- Every list endpoint serves two response shapes. Testing only full-page responses leaves the primary HTMX interaction path (90% of user experience) completely untested. Write paired tests for every endpoint.
5. **Flyway migration errors silently skip all tests** -- A known Quarkus bug (issue #43926) causes migration errors to result in zero tests running with a green build. Add a canary test and CI check for minimum test count.
6. **Missing referential integrity causes 500 errors** -- Deleting referenced Gender/Title/Relationship hits database FK constraints producing raw error pages. Must implement application-level checks BEFORE writing delete tests.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Test Infrastructure and Referential Integrity

**Rationale:** Everything depends on having working test infrastructure (dependencies, patterns, utilities) and correct application behavior (referential integrity). Writing tests against broken delete behavior would codify bugs. These must be done together and first.

**Delivers:**
- Test dependencies added to pom.xml (rest-assured, jsoup, quarkus-junit5-mockito, quarkus-test-security)
- `TestHelper` utility with `flushAndClear()` method
- Referential integrity checks on Gender, Title, Relationship, and Person delete
- Repository-level `isReferencedByXxx()` methods with tests
- Unit tests for PasswordValidator and entity lifecycle callbacks
- Repository integration tests for all custom queries
- Canary test to detect Flyway silent failures

**Addresses features:** Test dependencies (P1), referential integrity on master data delete (P1), person delete relationship handling (P1)

**Avoids pitfalls:** `@TestTransaction`/`@BeforeEach` mismatch, Hibernate cache false positives, Flyway silent failures, referential integrity 500 errors

### Phase 2: Endpoint Tests and HTMX Pattern Coverage

**Rationale:** With test infrastructure proven and referential integrity working, endpoint tests can cover the full application surface including the corrected delete behavior. HTMX fragment testing patterns are the primary differentiator and belong here.

**Delivers:**
- Resource/endpoint tests for all CRUD operations (Gender, Title, Relationship, Person, PersonRelationship)
- Auth flow tests (signup, login, logout, role boundaries)
- HTMX fragment test patterns (full-page vs fragment for every list endpoint)
- OOB swap verification patterns
- Modal CRUD workflow tests (success, validation error, uniqueness violation, referential integrity error)
- Security boundary tests (403 for wrong role, 302 for unauthenticated)

**Addresses features:** Integration tests for all CRUD (P1), auth flow tests (P1), HTMX fragment test patterns (P1), modal-driven CRUD testing (differentiator)

**Avoids pitfalls:** Fragment-only testing gap, missing `@TestSecurity`, OOB swap ID drift, validation path coverage gaps

### Phase 3: Quality Polish and Extended Coverage (v1.x)

**Rationale:** After core test coverage is solid, address remaining quality items and features that depend on a stable test foundation.

**Delivers:**
- Pagination on person list with HTMX partial table replacement
- Validation consistency audit across all form endpoints
- Service-layer tests for UserLoginService and NetworkService
- Graph data endpoint test (JSON response pattern)
- Test coverage metrics and CI pipeline checks

**Addresses features:** Pagination (P2), validation consistency (P2), graph endpoint testing (P2), CI/CD pipeline (P3)

### Phase Ordering Rationale

- **Phase 1 before Phase 2:** Endpoint tests depend on correct application behavior (referential integrity) and working test infrastructure (dependencies, utilities, patterns). Writing endpoint tests against broken behavior wastes effort.
- **Referential integrity in Phase 1, not Phase 2:** Delete tests must assert on correct error responses. If referential integrity is not implemented first, tests would either skip delete scenarios or codify 500-error behavior.
- **Phase 3 deferred:** Pagination, CI/CD, and service tests add value but are not required for the application to be a credible reference. They can safely follow once the test foundation proves stable.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 2 (HTMX fragment testing):** No established community patterns exist for testing HTMX fragment responses in Quarkus. The research identified the approach (REST Assured + Jsoup + `parseBodyFragment()`), but implementation will require validating patterns against actual template output. Recommend `/gsd:research-phase` to produce concrete test examples.

Phases with standard patterns (skip research-phase):
- **Phase 1 (test infrastructure + referential integrity):** Well-documented Quarkus patterns. `@QuarkusTest`, `@TestTransaction`, `@TestSecurity`, Panache repository queries, and DevServices are all covered in official guides. No novel patterns needed.
- **Phase 3 (pagination + quality):** Standard Panache pagination API, standard CI/CD patterns. No research needed.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | All versions verified against Quarkus 3.30.3 BOM effective POM; REST Assured, Jsoup, Mockito are established libraries with stable APIs |
| Features | HIGH | Based on direct codebase analysis (27 source files, 0 tests, 3 TODOs); feature gaps are concrete and measurable |
| Architecture | HIGH | Existing architecture is clean and well-documented; test layer architecture mirrors it directly; no novel patterns required except HTMX fragment testing |
| Pitfalls | HIGH | All critical pitfalls verified against Quarkus GitHub issues, official docs, and community discussions; specific issue numbers cited |

**Overall confidence:** HIGH

### Gaps to Address

- **HTMX fragment test patterns:** No community reference exists. The approach (Jsoup `parseBodyFragment()` + OOB attribute selectors) is theoretically sound but needs validation against actual Qute template output during Phase 2 planning.
- **Person delete cascade strategy:** Research identified the need but did not prescribe whether to cascade-delete PersonRelationships or prevent Person deletion when relationships exist. This is a product decision needed before Phase 1 implementation.
- **Test data strategy:** Whether to rely on Flyway seed data or create all test data programmatically needs a decision. Research recommends programmatic creation for mutation tests and seed data only for read-only tests, but the boundary needs explicit definition.
- **CI minimum test count threshold:** Research recommends asserting non-zero test count, but the specific threshold and CI tool (GitHub Actions, etc.) needs definition during Phase 3 planning.

## Sources

### Primary (HIGH confidence)
- Quarkus BOM 3.30.3 effective POM -- all dependency versions verified
- Quarkus Testing Guide: https://quarkus.io/guides/getting-started-testing -- `@QuarkusTest`, `@TestTransaction`, DevServices
- Quarkus Security Testing Guide: https://quarkus.io/guides/security-testing -- `@TestSecurity` annotation
- Context7 `/quarkusio/quarkusio.github.io` -- test annotation patterns, DevServices integration
- Context7 `/rest-assured/rest-assured` -- REST Assured 5.x API
- Context7 `/jhy/jsoup` v1.20.1 -- CSS selector API for HTML assertion
- HTMX documentation: https://htmx.org/docs/ -- OOB swap behavior, `HX-Request` header
- Direct codebase analysis: 27 Java source files, 12 Flyway migrations, 0 test files

### Secondary (MEDIUM confidence)
- Quarkus GitHub discussions #40119, #33330 -- `@TestTransaction` boundary behavior, Hibernate cache in tests
- Quarkus GitHub issue #43926 -- Flyway migration errors causing silent test skip
- JHipster feature comparison -- competitor analysis for feature landscape

---
*Research completed: 2026-03-07*
*Ready for roadmap: yes*
