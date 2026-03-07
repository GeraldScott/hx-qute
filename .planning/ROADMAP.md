# Roadmap: HX-Qute Reference Application

## Overview

This milestone retrofits a comprehensive test suite onto an existing, functionally complete Quarkus + HTMX reference application. Phase 1 establishes test infrastructure, fixes referential integrity gaps, and covers unit/repository layers. Phase 2 builds on that foundation to test all CRUD endpoints, auth flows, security boundaries, and HTMX-specific fragment/OOB patterns. The result is a reference application that demonstrates not just how to build with Quarkus + HTMX, but how to test it.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Test Foundation and Referential Integrity** - Establish test infrastructure, fix delete integrity gaps, and cover unit/repository layers
- [ ] **Phase 2: Endpoint and HTMX Pattern Coverage** - Test all CRUD endpoints, auth flows, security boundaries, and HTMX fragment patterns

## Phase Details

### Phase 1: Test Foundation and Referential Integrity
**Goal**: Developers can run a reliable test suite that validates data layer behavior and referential integrity
**Depends on**: Nothing (first phase)
**Requirements**: TEST-01, TEST-02, TEST-03, TEST-04, RINT-01, RINT-02, RINT-03, RINT-04, RINT-05, UNIT-01, UNIT-02, UNIT-03, REPO-01, REPO-02, REPO-03, REPO-04, REPO-05, REPO-06
**Success Criteria** (what must be TRUE):
  1. `./mvnw test` runs green with all test dependencies resolved and DevServices PostgreSQL provisioned automatically
  2. Deleting a Gender, Title, or Relationship that is referenced by existing records returns a user-friendly error instead of a 500 page
  3. Deleting a Person correctly handles any existing PersonRelationship records without database errors
  4. Unit tests validate password rules (min/max length, null/empty, valid) and entity lifecycle callbacks (timestamps, email normalization, display name)
  5. Repository tests verify all custom query methods return correct results after flush/clear to avoid Hibernate cache false positives
**Plans**: TBD

Plans:
- [ ] 01-01: TBD
- [ ] 01-02: TBD

### Phase 2: Endpoint and HTMX Pattern Coverage
**Goal**: Every HTTP endpoint is tested for both full-page and HTMX fragment responses, proving the application works end-to-end
**Depends on**: Phase 1
**Requirements**: ENDP-01, ENDP-02, ENDP-03, ENDP-04, ENDP-05, ENDP-06, ENDP-07, ENDP-08, ENDP-09, HTMX-01, HTMX-02, HTMX-03, HTMX-04
**Success Criteria** (what must be TRUE):
  1. Every CRUD endpoint (Gender, Title, Relationship, Person, PersonRelationship) has tests covering list, create, edit, and delete operations
  2. Auth flow tests verify signup validation, successful registration, and logout behavior
  3. Unauthenticated requests redirect to login; user-role requests to admin-only routes receive 403
  4. Requests with HX-Request header receive fragment-only responses (no base layout), and mutations include correct hx-swap-oob attributes
  5. Form validation errors re-render within modal context with error messages and preserved input values
**Plans**: TBD

Plans:
- [ ] 02-01: TBD
- [ ] 02-02: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Test Foundation and Referential Integrity | 0/0 | Not started | - |
| 2. Endpoint and HTMX Pattern Coverage | 0/0 | Not started | - |
