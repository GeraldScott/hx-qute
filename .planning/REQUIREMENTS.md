# Requirements: HX-Qute Reference Application

**Defined:** 2026-03-07
**Core Value:** Provide a complete, production-quality reference implementation of the Quarkus + HTMX + Qute stack

## v1 Requirements

Requirements for this milestone. Each maps to roadmap phases.

### Test Infrastructure

- [ ] **TEST-01**: Test dependencies added to pom.xml (rest-assured, jsoup, quarkus-junit5-mockito, quarkus-test-security)
- [ ] **TEST-02**: Test utility class with flushAndClear() helper for Hibernate cache management in tests
- [ ] **TEST-03**: Canary test that validates Flyway migrations run successfully and database is accessible
- [ ] **TEST-04**: Test directory structure mirrors main source structure (io.archton.scaffold.*)

### Referential Integrity

- [ ] **RINT-01**: Gender delete checks if gender is referenced by any Person record and returns user-friendly error
- [ ] **RINT-02**: Title delete checks if title is referenced by any Person record and returns user-friendly error
- [ ] **RINT-03**: Relationship delete checks if relationship is referenced by any PersonRelationship record and returns user-friendly error
- [ ] **RINT-04**: Person delete handles existing PersonRelationship records (cascade delete or prevent with error)
- [ ] **RINT-05**: Repository methods exist for checking referential usage (countByGender, countByTitle, countByRelationship, etc.)

### Unit Tests

- [ ] **UNIT-01**: PasswordValidator tests cover minimum length, maximum length, null/empty, and valid passwords
- [ ] **UNIT-02**: Entity lifecycle callback tests verify createdAt/updatedAt timestamps and email normalization
- [ ] **UNIT-03**: Person.getDisplayName() tests cover all combinations (with/without title)

### Repository Tests

- [ ] **REPO-01**: PersonRepository custom query tests (findByFilterPaged, existsByEmail, existsByEmailAndIdNot, findByFilter)
- [ ] **REPO-02**: GenderRepository custom query tests (listAllOrdered, existsByCode, existsByCodeAndIdNot)
- [ ] **REPO-03**: TitleRepository custom query tests (listAllOrdered, existsByCode, existsByCodeAndIdNot)
- [ ] **REPO-04**: RelationshipRepository custom query tests
- [ ] **REPO-05**: PersonRelationshipRepository custom query tests
- [ ] **REPO-06**: UserLoginRepository custom query tests (emailExists)

### Endpoint Tests

- [ ] **ENDP-01**: Gender CRUD endpoint tests (list, create, edit, delete) with both full-page and HTMX fragment responses
- [ ] **ENDP-02**: Title CRUD endpoint tests (list, create, edit, delete) with both full-page and HTMX fragment responses
- [ ] **ENDP-03**: Relationship CRUD endpoint tests (list, create, edit, delete) with both full-page and HTMX fragment responses
- [ ] **ENDP-04**: Person CRUD endpoint tests (list, create, edit, delete) with both full-page and HTMX fragment responses
- [ ] **ENDP-05**: PersonRelationship endpoint tests with HTMX fragment responses
- [ ] **ENDP-06**: Auth flow endpoint tests (signup validation, signup success, logout)
- [ ] **ENDP-07**: Security boundary tests (unauthenticated redirect, admin-only routes return 403 for user role)
- [ ] **ENDP-08**: Graph endpoint tests (HTML page, JSON data endpoint)
- [ ] **ENDP-09**: Index/landing page endpoint test

### HTMX Pattern Tests

- [ ] **HTMX-01**: Fragment response tests verify HX-Request header triggers fragment-only response (no base layout)
- [ ] **HTMX-02**: OOB swap tests verify hx-swap-oob attributes on create/update/delete success responses
- [ ] **HTMX-03**: Modal workflow tests verify form validation errors return within modal context
- [ ] **HTMX-04**: Validation error tests verify form re-renders with error message and preserved input values

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Quality Polish

- **QUAL-01**: Centralize duplicate email regex validation between PersonResource and AuthResource
- **QUAL-02**: Standardize validation patterns across all form endpoints
- **QUAL-03**: Add functional indexes on PostgreSQL for LOWER() filter queries

### Extended Features

- **FEAT-01**: Pagination on person list with HTMX partial table replacement
- **FEAT-02**: Person detail view in modal
- **FEAT-03**: Network view depth control for individual persons
- **FEAT-04**: CI/CD pipeline configuration (GitHub Actions)
- **FEAT-05**: Test coverage metrics reporting

## Out of Scope

| Feature | Reason |
|---------|--------|
| Client-side JS framework | HTMX sufficiency is the core reference value |
| Parallel JSON REST API | This is a server-rendered application; HTML is the API |
| OAuth/OIDC/Social login | Form-based auth demonstrates Quarkus Security patterns adequately |
| WebSocket/SSE real-time | No real-time use case; HTMX polling covers future needs |
| Docker Compose / K8s manifests | Quarkus DevServices handles dev/test; deployment docs suffice |
| Soft deletes | Adds query complexity; hard deletes with referential integrity is the pattern |
| Internationalization (i18n) | Obscures template patterns the reference is demonstrating |
| Caching layer | Premature optimization for reference scope |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| TEST-01 | Phase 1 | Pending |
| TEST-02 | Phase 1 | Pending |
| TEST-03 | Phase 1 | Pending |
| TEST-04 | Phase 1 | Pending |
| RINT-01 | Phase 1 | Pending |
| RINT-02 | Phase 1 | Pending |
| RINT-03 | Phase 1 | Pending |
| RINT-04 | Phase 1 | Pending |
| RINT-05 | Phase 1 | Pending |
| UNIT-01 | Phase 1 | Pending |
| UNIT-02 | Phase 1 | Pending |
| UNIT-03 | Phase 1 | Pending |
| REPO-01 | Phase 1 | Pending |
| REPO-02 | Phase 1 | Pending |
| REPO-03 | Phase 1 | Pending |
| REPO-04 | Phase 1 | Pending |
| REPO-05 | Phase 1 | Pending |
| REPO-06 | Phase 1 | Pending |
| ENDP-01 | Phase 2 | Pending |
| ENDP-02 | Phase 2 | Pending |
| ENDP-03 | Phase 2 | Pending |
| ENDP-04 | Phase 2 | Pending |
| ENDP-05 | Phase 2 | Pending |
| ENDP-06 | Phase 2 | Pending |
| ENDP-07 | Phase 2 | Pending |
| ENDP-08 | Phase 2 | Pending |
| ENDP-09 | Phase 2 | Pending |
| HTMX-01 | Phase 2 | Pending |
| HTMX-02 | Phase 2 | Pending |
| HTMX-03 | Phase 2 | Pending |
| HTMX-04 | Phase 2 | Pending |

**Coverage:**
- v1 requirements: 31 total
- Mapped to phases: 31
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-07*
*Last updated: 2026-03-07 after initial definition*
