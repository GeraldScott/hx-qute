# Feature Research

**Domain:** Production-quality Quarkus/HTMX reference application (scaffold)
**Researched:** 2026-03-07
**Confidence:** HIGH (based on direct codebase analysis, Quarkus documentation, and established reference app conventions)

## Feature Landscape

### Table Stakes (Users Expect These)

Features that any credible Quarkus reference application must have. Missing these undermines the entire purpose of being a "reference."

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Automated test suite (REST Assured + Jsoup) | A reference app without tests is a prototype, not a reference. Developers copy testing patterns. | HIGH | Zero tests exist today. Test strategy doc (`docs/TEST-STRATEGY-CI-CD.md`) already defines the approach. Missing dependencies: `rest-assured`, `jsoup` in pom.xml. Needs `@QuarkusTest`, `@TestTransaction`, test data seeding. |
| Referential integrity checks on delete | Master data deletion (Gender, Title, Relationship) silently orphans or corrupts Person records. Three `TODO` comments in code confirm this is a known gap. | MEDIUM | `ReferentialIntegrityException` class already exists. `GlobalExceptionMapper` already handles it (409 Conflict). Just need count queries in repositories and pre-delete checks in resource classes. |
| Input validation consistency | Current validation is manual and inconsistent across resources. Some fields validated, others not. No Bean Validation on entities. | MEDIUM | Manual validation in Resources is fine for HTMX UX (returns form with error), but validation rules are scattered and duplicated. Need consistent patterns across all form endpoints. |
| Health check endpoint | Already present via `quarkus-smallrye-health` dependency. Confirm it works and covers database connectivity. | LOW | Dependency exists. Default `/q/health` endpoint should be functional. Verify database health check is included. |
| Error handling for all HTTP error codes | `GlobalExceptionMapper` exists but only covers 404, 409, 500. Missing 400, 401, 403 handling for HTMX vs full-page contexts. | LOW | Existing mapper is solid. Need to verify behavior for auth-related errors (redirect vs error page). |
| Flyway migration best practices | Migrations exist and follow versioning. Need to verify they include proper constraints, indexes, and rollback considerations. | LOW | 12 migrations exist covering all entities. Schema looks clean with foreign keys. |
| Pagination on person list | Listed as "Active" in PROJECT.md. Person lists without pagination break at scale. Every CRUD reference app paginates. | MEDIUM | Need server-side `PanacheQuery` pagination + HTMX partial table replacement. Requires page size, current page, total count in template. |
| Structured logging | Production apps need structured, parseable logs. Quarkus uses JBoss Logging by default. | LOW | Already using `org.jboss.logging.Logger` in `GlobalExceptionMapper`. Need to verify JSON logging config for production profile. |
| Application configuration profiles | Dev, test, and prod profiles with appropriate settings for each environment. | LOW | Quarkus profiles are built-in. Verify `application.properties` has proper profile-specific configuration. |

### Differentiators (Competitive Advantage)

Features that make this reference app stand out from basic Quarkus starters and quickstarts.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| HTMX fragment testing patterns | No established pattern exists for testing HTMX fragment responses in Quarkus. Being the reference for this is valuable. | MEDIUM | REST Assured returns HTML string, Jsoup parses it. Test patterns for: full page vs fragment (HX-Request header), OOB swaps, modal content, table row updates. This is the single biggest gap in the Quarkus+HTMX ecosystem. |
| Qute CheckedTemplate testing | Demonstrating compile-time template validation with test coverage. Most reference apps skip template testing entirely. | MEDIUM | CheckedTemplate pattern is already used throughout. Tests should verify template data binding, fragment rendering, and type-safe template references. |
| Network graph visualization | D3 force-directed graph with interactive features (drag, zoom, context menu, filtering). Not found in any Quarkus reference. | LOW | Already implemented. Just needs test coverage for the JSON data endpoint. |
| HTMX OOB swap patterns | Out-of-band swap pattern for modal success + table update is a non-trivial HTMX pattern worth demonstrating. | LOW | Already implemented. Reference value is in the existing code + tests proving it works. |
| Relationship management with bidirectional data | Person-to-person relationships with typed relationship categories. More complex than typical CRUD demos. | LOW | Already implemented. Needs referential integrity protection and tests. |
| Modal-driven CRUD workflow | Full create/edit/delete via modals with HTMX, including form validation within modals. This is a real-world pattern most tutorials skip. | LOW | Already implemented. Tests would document this pattern. |
| Global exception mapper with HTMX awareness | Content-negotiated error responses (HTML for browser, JSON for API). Detects HTMX requests. | LOW | Already implemented. Needs test coverage demonstrating the behavior. |
| E2E test runner subagent pattern | Chrome DevTools MCP-based E2E testing demonstrates modern testing approaches. | LOW | Already defined in `.claude/agents/e2e-test-runner.md`. This is a developer tooling differentiator, not a runtime feature. |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem good but should be deliberately excluded from this reference application.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Client-side JavaScript framework (React, Vue) | "Modern" web apps use them | Defeats the purpose of demonstrating HTMX as an alternative. Adds build tooling complexity. Reference should prove HTMX sufficiency. | HTMX for all dynamic interactions. JavaScript only for D3 graph and UIkit modal triggers. |
| REST API with JSON responses | "APIs should return JSON" | This is a server-rendered application. The API IS the HTML. Adding a parallel JSON API creates maintenance burden and dilutes the HTMX message. | JSON only for graph data endpoint where D3 requires it. |
| OAuth/OIDC/Social login | "Production apps use OAuth" | Massive complexity increase (provider config, token management, callback URLs). Form-based auth demonstrates Quarkus Security patterns adequately. | Quarkus Security JPA with form-based auth. Document how to swap to OIDC in ARCHITECTURE.md. |
| WebSocket/SSE real-time features | "Modern apps are real-time" | HTMX polling (`hx-trigger="every 5s"`) covers most use cases. WebSocket adds infrastructure complexity inappropriate for a reference app. | HTMX polling if needed. The reference app has no real-time use case. |
| Microservices architecture | "Production should be microservices" | This is a monolith and should stay one. Reference apps demonstrate patterns, not infrastructure. A monolith is easier to clone and run. | Single deployable Quarkus application. |
| Docker Compose / Kubernetes manifests | "Need deployment examples" | Quarkus Dev Services auto-provisions PostgreSQL for development. Deployment manifests become stale quickly and distract from the application patterns. | Quarkus Dev Services for dev/test. Document deployment steps in prose, not YAML. |
| Soft deletes | "Never actually delete data" | Adds complexity to every query (WHERE active=true). Inappropriate for a reference app where the goal is demonstrating clean patterns, not enterprise data retention. | Hard deletes with referential integrity checks. The reference shows how to prevent invalid deletes. |
| Internationalization (i18n) | "Production apps need i18n" | Adds message bundle complexity to every template. Qute supports i18n but it obscures the template patterns this reference is demonstrating. | English only. Document Qute i18n capabilities in ARCHITECTURE.md. |
| Caching layer | "Performance requires caching" | Premature optimization. The dataset is small. Adding Hibernate second-level cache or HTTP caching adds configuration complexity without demonstrating meaningful patterns for the reference scope. | Let Hibernate and PostgreSQL handle caching at their layers. |
| Audit log / event sourcing | "Track all changes" | Audit fields (createdBy, updatedBy, timestamps) already exist on entities. A full audit log table adds significant complexity for minimal reference value. | Existing audit fields on entities are sufficient. |

## Feature Dependencies

```
[Automated Test Suite]
    └──requires──> [Test dependencies in pom.xml (rest-assured, jsoup)]
    └──requires──> [Test data seeding (import.sql or programmatic)]
    └──enhances──> [Every other feature - tests prove they work]

[Referential Integrity Checks]
    └──requires──> [Count queries in repositories]
    └──requires──> [ReferentialIntegrityException] (already exists)
    └──enhances──> [Master Data CRUD (Gender, Title, Relationship)]
    └──enhances──> [Person Delete] (cascade or prevent)

[Pagination]
    └──requires──> [Person list view] (already exists)
    └──requires──> [PanacheQuery pagination support]
    └──enhances──> [Person filtering] (filter + paginate together)
    └──enhances──> [Person sorting] (sort + paginate together)

[HTMX Fragment Testing]
    └──requires──> [Automated Test Suite]
    └──requires──> [Jsoup HTML parsing]
    └──enhances──> [All HTMX endpoints]

[Input Validation Consistency]
    └──enhances──> [All form endpoints]
    └──enhances──> [Automated Test Suite] (consistent validation = testable patterns)
```

### Dependency Notes

- **Automated Test Suite requires test dependencies**: `rest-assured` and `jsoup` must be added to pom.xml before any tests can be written. `quarkus-junit5` is already present.
- **Referential Integrity requires repository queries**: Gender, Title, and Relationship repositories need `countByXxx` methods to check usage before delete.
- **HTMX Fragment Testing requires test suite**: This is a specialization of the test suite, not a separate feature. It defines testing patterns specific to HTMX endpoints.
- **Pagination and filtering interact**: Applying a filter should reset to page 1. Sort order must persist across pages. These need to be tested together.

## MVP Definition

### Launch With (v1)

Minimum viable milestone -- what's needed to close the quality gap and make this a credible reference.

- [ ] Test dependencies added to pom.xml (rest-assured, jsoup) -- prerequisite for everything
- [ ] Referential integrity checks on Gender, Title, Relationship delete -- three TODOs in code demanding this
- [ ] Integration tests for all CRUD endpoints (Gender, Title, Relationship, Person) -- proves the app works
- [ ] Integration tests for auth flows (registration, login, logout) -- proves security works
- [ ] HTMX fragment test patterns documented through actual tests -- the differentiating contribution
- [ ] Person delete cascade/protect for relationships -- Person has PersonRelationships that need handling

### Add After Validation (v1.x)

Features to add once the test suite is stable and patterns are established.

- [ ] Pagination on person list -- listed as Active requirement, natural follow-on
- [ ] Person detail view in modal -- listed as Active requirement
- [ ] Network view for individual persons -- listed as Active requirement
- [ ] Validation consistency audit -- standardize validation patterns across all form endpoints
- [ ] Test coverage for graph data endpoint -- JSON response testing pattern

### Future Consideration (v2+)

Features to defer until the reference app's core quality is solid.

- [ ] `@QuarkusComponentTest` for unit-level service tests -- once integration tests prove patterns
- [ ] CI/CD pipeline configuration (GitHub Actions) -- once test suite is reliable
- [ ] Native image build verification -- once standard tests pass
- [ ] Performance testing patterns -- after pagination and larger dataset support
- [ ] Security testing patterns (auth bypass, CSRF) -- after core functional tests

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Test dependencies in pom.xml | HIGH | LOW | P1 |
| Referential integrity on master data delete | HIGH | LOW | P1 |
| Integration tests: master data CRUD | HIGH | MEDIUM | P1 |
| Integration tests: auth flows | HIGH | MEDIUM | P1 |
| Integration tests: person CRUD | HIGH | MEDIUM | P1 |
| HTMX fragment test patterns | HIGH | MEDIUM | P1 |
| Person delete relationship handling | HIGH | LOW | P1 |
| Pagination on person list | MEDIUM | MEDIUM | P2 |
| Validation consistency audit | MEDIUM | MEDIUM | P2 |
| Person detail modal | MEDIUM | LOW | P2 |
| Network view per person | LOW | MEDIUM | P3 |
| `@QuarkusComponentTest` patterns | LOW | MEDIUM | P3 |
| CI/CD pipeline | MEDIUM | LOW | P3 |

**Priority key:**
- P1: Must have for this milestone (quality + credibility)
- P2: Should have, add when test foundation is solid
- P3: Nice to have, future consideration

## Competitor Feature Analysis

| Feature | Quarkus Quickstarts | JHipster | Spring PetClinic | Our Approach |
|---------|---------------------|----------|-------------------|--------------|
| Test coverage | Basic REST tests, no HTML assertions | Comprehensive (unit + integration + E2E) | Good integration tests | REST Assured + Jsoup for HTML fragment testing (unique approach) |
| HTMX integration | None (JSON APIs) | None (Angular/React/Vue) | None (Thymeleaf, no HTMX) | Full HTMX with OOB swaps, modals, fragments (differentiator) |
| Referential integrity | Schema-level (FK constraints) | Service-layer checks + schema | Schema-level | Service-layer checks with user-friendly error messages in modals |
| Auth patterns | Token-based examples | Full OIDC/JWT/session | Basic Spring Security | Form-based auth with NIST-compliant passwords (appropriate scope) |
| Relationship modeling | Simple entity relationships | Complex entity generator | One-to-many only | Many-to-many with typed relationships + graph visualization |
| Error handling | Basic exception mappers | Global error handling | Controller advice | Content-negotiated (HTML/JSON) with HTMX awareness |
| Pagination | Present in some quickstarts | Generated with all lists | Present | Not yet implemented (P2 priority) |

## Sources

- Direct codebase analysis of `/home/geraldo/Workspace/quarkus/hx-qute/` (27 Java source files, 12 Flyway migrations, 0 test files)
- `docs/USER-STORIES.md` -- 4 features, 19 user stories defining functional scope
- `docs/TEST-STRATEGY-CI-CD.md` -- existing test strategy document (REST Assured + Jsoup approach)
- `docs/ARCHITECTURE.md` -- established architectural patterns and conventions
- `.planning/PROJECT.md` -- validated requirements and active backlog
- Quarkus Quickstarts: https://github.com/quarkusio/quarkus-quickstarts (HIGH confidence -- official)
- Quarkus Testing Guide: https://quarkus.io/guides/getting-started-testing (HIGH confidence -- official)
- HTMX documentation: https://htmx.org/docs/ (HIGH confidence -- official)
- JHipster feature set: https://www.jhipster.tech/ (MEDIUM confidence -- competitor analysis)

---
*Feature research for: Quarkus/HTMX reference application quality milestone*
*Researched: 2026-03-07*
