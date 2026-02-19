# HX-Qute HTMX Cleanup

## What This Is

A Quarkus + HTMX reference application demonstrating server-rendered web development with the hypermedia-driven application (HDA) pattern. It uses Qute templates, UIkit, PostgreSQL with Panache, and Flyway migrations. The app manages People, their relationships, and lookup tables (Gender, Title, Relationship) with modal-based CRUD and a relationship graph visualization.

## Core Value

Demonstrate correct HTMX patterns — the app is a reference implementation, so its patterns must be worth copying.

## Requirements

### Validated

- ✓ Person CRUD with filtering, sorting, modal-based create/edit/delete — existing
- ✓ Lookup table CRUD (Gender, Title, Relationship) — admin-only, existing
- ✓ Person-to-person relationships with graph visualization — existing
- ✓ Form authentication with BCrypt hashing and NIST SP 800-63B-4 password policy — existing
- ✓ Flyway database migrations with PostgreSQL — existing
- ✓ Qute fragment-based partial updates with OOB swaps — existing
- ✓ UIkit CSS framework with responsive sidebar layout — existing
- ✓ Global exception handling with HTML/JSON content negotiation — existing

### Active

- [ ] Remove HX-Request header checking antipattern from all resources
- [ ] Add hx-boost with hx-select to navigation for proper HTMX page transitions
- [ ] Eliminate redundant template files created to support the header-checking pattern
- [ ] Ensure all navigation works both with and without JavaScript (progressive enhancement)

### Out of Scope

- Adding test coverage — deferred to a separate milestone
- New features or entities — this is a cleanup-only milestone
- Changing the CRUD modal pattern — OOB swap pattern for in-page updates is correct
- Migrating away from UIkit or changing the CSS framework

## Context

The HTMX analysis (stored in project memory `htmx_analysis_and_findings`) identified that the current codebase checks the `HX-Request` header in 6 resource `list()` methods to decide between returning a full page or a fragment. This is an antipattern per HTMX philosophy — the server should return consistent HTML and let the client decide what to extract.

**Affected resources:**
- `GenderResource.java` — list()
- `TitleResource.java` — list()
- `RelationshipResource.java` — list()
- `PersonResource.java` — list()
- `PersonRelationshipResource.java` — list()
- `GraphResource.java` — list()

**The fix:** Add `hx-boost="true"`, `hx-target="#main-content"`, and `hx-select="#main-content"` to sidebar navigation. Resources always return the full page. HTMX extracts `#main-content` during boosted navigation. Direct URL access renders normally. CRUD modal operations (create/edit/delete) are unaffected — they already return fragments targeted at `#modal-content`.

## Constraints

- **Tech stack**: Quarkus 3.30.3, HTMX 2.0.8, UIkit 3.25.4, Java 21 — no version changes
- **Progressive enhancement**: Pages must work without JavaScript (full page loads)
- **Backward compatibility**: All existing URLs and CRUD flows must continue to work
- **No new dependencies**: Fix uses built-in HTMX attributes only

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Use hx-boost + hx-select over server-side header checking | Follows HTMX philosophy — server returns consistent HTML, client extracts what it needs | — Pending |
| Keep OOB swap pattern for CRUD operations | OOB swaps for modal success/table refresh are correct HTMX usage, not an antipattern | — Pending |

---
*Last updated: 2026-02-19 after initialization*
