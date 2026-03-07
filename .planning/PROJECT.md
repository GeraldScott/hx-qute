# HX-Qute Reference Application

## What This Is

A reference application for building server-rendered web applications with Quarkus, HTMX, and PostgreSQL. It demonstrates people management with relationship tracking and network visualization, serving as a reusable foundation for Quarkus/HTMX projects. Built for Archton as a scaffold for enterprise web applications.

## Core Value

Provide a complete, production-quality reference implementation of the Quarkus + HTMX + Qute stack that developers can use as a starting point for building server-rendered web applications with dynamic interactions.

## Requirements

### Validated

- ✓ Authentication infrastructure with BCrypt password hashing and NIST-compliant validation — existing
- ✓ Application shell with responsive layout, sidebar navigation, and UIkit styling — existing
- ✓ User registration with email/password at `/signup` — existing
- ✓ User login via modal with form-based auth (`/j_security_check`) — existing
- ✓ User logout with session destruction and cookie clearing — existing
- ✓ Gender CRUD management (view, create, edit, delete) with admin role — existing
- ✓ Title CRUD management (view, create, edit, delete) with admin role — existing
- ✓ Relationship type CRUD management (view, create, edit, delete) with admin role — existing
- ✓ Person list view with table display — existing
- ✓ Person creation with form validation and audit fields — existing
- ✓ Person editing with form validation — existing
- ✓ Person deletion with confirmation dialog — existing
- ✓ Person filtering by name and email with live search — existing
- ✓ Person sorting by name fields — existing
- ✓ Person relationship management (add, edit, remove related people) — existing
- ✓ Network graph visualization with D3 force-directed layout — existing
- ✓ Flyway database migrations for all entities — existing
- ✓ Global exception handling with HTML/JSON error responses — existing

### Active

- [ ] Automated test suite (unit, integration, E2E tests)
- [ ] Referential integrity checks on master data deletion (prevent deleting gender/title/relationship in use)
- [ ] Pagination on person list
- [ ] Person detail view in modal
- [ ] Network view for individual persons with depth control

### Out of Scope

- Mobile native app — web-first reference application
- OAuth/social login — form-based auth is sufficient for reference scope
- Real-time features (WebSocket) — HTMX polling sufficient for reference scope
- Email sending — not needed for reference application

## Context

- **Brownfield project**: All core features from USER-STORIES.md are already implemented
- **Codebase map**: Available at `.planning/codebase/` with 7 analysis documents
- **Architecture**: Server-rendered MVC with HTMX partial updates, Qute CheckedTemplate fragments, OOB swap pattern
- **Stack**: Java 21, Quarkus 3.30.3, HTMX 2.0.8, UIkit 3.25, PostgreSQL 17, Flyway
- **No tests exist**: The `src/test/` directory is empty — this is the primary gap
- **Existing user stories**: Documented in `docs/USER-STORIES.md` covering Features 000-004

## Constraints

- **Tech stack**: Quarkus 3.30.3, Java 21, HTMX 2.0.8, UIkit 3.25, PostgreSQL 17 — locked
- **Architecture**: Server-rendered with Qute templates, no client-side JS framework — established pattern
- **Authentication**: Quarkus Security JPA with form-based auth — established
- **Database**: Flyway migrations, Panache repositories — established patterns
- **Compatibility**: Must maintain existing URL structure and HTMX interaction patterns

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Panache Repository pattern over Active Record | Separation of concerns, testability | ✓ Good |
| Public fields on entities (Panache style) | Convention over boilerplate | ✓ Good |
| CDN-loaded frontend libraries (HTMX, UIkit) | Simplicity, no build tooling needed | ✓ Good |
| "router" package name for JAX-RS resources | Team convention | — Pending |
| Manual validation over Bean Validation on form params | Explicit control, better error UX | — Pending |
| No service layer for most entities | Simpler for CRUD, but limits reusability | ⚠️ Revisit |

---
*Last updated: 2026-03-07 after initialization*
