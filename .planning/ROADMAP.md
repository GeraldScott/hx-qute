# Roadmap: HX-Qute HTMX Cleanup

## Overview

This milestone removes the HX-Request header-checking antipattern from all six resource endpoints, deletes orphaned template files that only existed to support that pattern, and updates architecture documentation to reflect the simplified approach. The result is a cleaner codebase where every resource returns a full page consistently -- a pattern worth copying.

## Phases

**Phase Numbering:**
- Integer phases (1, 2): Planned milestone work
- Decimal phases (1.1, 1.2): Urgent insertions (marked with INSERTED)

- [ ] **Phase 1: Remove HX-Request Antipattern** - Strip header branching from all resources and delete orphaned templates
- [ ] **Phase 2: Update Documentation** - Reflect the simplified resource pattern in architecture docs

## Phase Details

### Phase 1: Remove HX-Request Antipattern
**Goal**: Every resource endpoint returns a full page unconditionally -- no branching on HX-Request header, no dead template files
**Depends on**: Nothing (first phase)
**Requirements**: CLEAN-01, CLEAN-02, CLEAN-03, CLEAN-04, CLEAN-05, CLEAN-06, TMPL-01
**Success Criteria** (what must be TRUE):
  1. Navigating to each resource URL (/genders, /titles, /relationships, /people, /person-relationships, /graph) in a browser renders the complete page with sidebar, header, and content
  2. No Java resource class contains `@HeaderParam("HX-Request")` or conditional logic branching on that header
  3. No Qute template file exists solely to serve the HX-Request fragment path (all orphaned templates removed)
  4. All existing CRUD operations (create, edit, delete via modals) continue to work unchanged on every resource page
  5. Search, filter, sort, and pagination on Person and PersonRelationship pages continue to function correctly
**Plans**: TBD

Plans:
- [ ] 01-01: TBD
- [ ] 01-02: TBD

### Phase 2: Update Documentation
**Goal**: Architecture documentation accurately describes the simplified resource pattern with no references to the removed HX-Request branching
**Depends on**: Phase 1
**Requirements**: DOCS-01
**Success Criteria** (what must be TRUE):
  1. docs/ARCHITECTURE.md Resource Layer section describes the "always return full page" pattern with no mention of HX-Request header branching
  2. Any code examples in documentation that showed the HX-Request if/else pattern are removed or replaced with the simplified version
**Plans**: TBD

Plans:
- [ ] 02-01: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Remove HX-Request Antipattern | 0/0 | Not started | - |
| 2. Update Documentation | 0/0 | Not started | - |
