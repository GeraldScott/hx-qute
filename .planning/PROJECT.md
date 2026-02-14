# HX-Qute Investigation Tool

## What This Is

A web-based people and relationships investigation tool for investigators and lawyers. It captures information about individuals and manages the connections between them, helping users understand who knows whom and how people are related. Built with Quarkus, HTMX, and PostgreSQL as a server-rendered hypermedia application.

## Core Value

Investigators can explore a person's network of relationships at configurable depth and trace the path of connections between any two people.

## Requirements

### Validated

<!-- Shipped and confirmed valuable. -->

- ✓ Person CRUD with filtering and sorting — existing
- ✓ Relationship type management (parent, sibling, spouse, etc.) — existing
- ✓ Person-to-person relationship linking — existing
- ✓ Relationship graph visualization — existing
- ✓ Form-based authentication with user/admin roles — existing
- ✓ User registration with NIST-compliant password policy — existing
- ✓ Admin management of reference data (titles, genders, relationships) — existing
- ✓ Basic attribution (createdBy/updatedBy on records) — existing

### Active

<!-- Current scope. Building toward these. -->

- [ ] Person-centered connection network with configurable depth (1, 2, 3+ degrees)
- [ ] Path finding between any two people through the relationship chain
- [ ] Advanced search across all person fields with filters
- [ ] Evidence/document attachment to people (files + freeform notes)
- [ ] Evidence/document attachment to relationships (files + freeform notes)
- [ ] Pagination for person and relationship lists

### Out of Scope

- Real-time notifications — not needed for investigation workflow
- Case/investigation management — defer to future milestone; focus on data and discovery first
- Multi-tenant data isolation — single shared dataset for collaborative team
- Mobile-native app — web-first, responsive design sufficient
- Full audit logging — basic attribution sufficient for now
- Structured evidence records (typed entries with schemas) — files and notes cover current needs

## Context

This is a brownfield project with an established codebase. The application follows a layered hypermedia-driven architecture (HDA) pattern:

- **Stack**: Java 21, Quarkus 3.30.3, HTMX 2.0.8, UIkit 3.25, PostgreSQL 17.7
- **Architecture**: Server-rendered HTML via Qute templates, HTMX for partial updates, Panache ORM repositories
- **Existing entities**: Person, UserLogin, Gender, Title, Relationship, PersonRelationship
- **Existing graph**: A relationship graph page exists (`/graph`) but shows all data at once — needs to become person-centered with configurable depth
- **Existing search**: Basic text filter on person list — needs expansion to advanced multi-field search
- **Users**: Collaborative team of investigators and lawyers sharing the same dataset
- **Attribution**: `createdBy`/`updatedBy` fields exist on entities; sufficient for current needs

Known technical concerns from codebase analysis:
- No test coverage (test directory exists but is empty)
- No pagination on queries (unbounded result sets)
- Repeated dropdown queries without caching
- Inconsistent email validation patterns across resources
- Silent date parsing failures in person forms

## Constraints

- **Tech stack**: Quarkus 3.30.3, HTMX 2.0.8, UIkit 3.25, PostgreSQL 17.7, Java 21 — established, no framework changes
- **Architecture**: Server-rendered hypermedia (HDA) pattern — no SPA or client-side rendering
- **File storage**: Local filesystem for uploaded evidence files (no cloud storage dependency for v1)
- **Security**: Form-based authentication with existing role model (user/admin) — no new auth providers
- **Database**: Flyway migrations, Hibernate Panache repositories — follow established patterns

## Key Decisions

<!-- Decisions that constrain future work. Add throughout project lifecycle. -->

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Person-centered network as top priority | Investigators need to explore connections outward from a subject | — Pending |
| Configurable depth on network view | Different investigations need different scope; let users control | — Pending |
| Evidence as files + notes (not structured records) | Simpler model covers current needs; structured records can come later | — Pending |
| Local filesystem for file storage | Avoids cloud dependency for v1; can migrate to S3/MinIO later | — Pending |
| Basic attribution over full audit trail | createdBy/updatedBy already exists; full audit is future work | — Pending |

---
*Last updated: 2026-02-14 after initialization*
