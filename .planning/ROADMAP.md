# Roadmap: HX-Qute Investigation Tool

## Overview

This roadmap delivers person-centered network exploration and evidence capture capabilities to the existing investigation tool. We start with navigation improvements and pagination infrastructure, then build the core network discovery feature with configurable depth, and finish with file and note attachments for comprehensive evidence tracking.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3, 4): Planned milestone work
- Decimal phases (e.g., 2.1): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Quick Actions** - Direct navigation to person views from list
- [ ] **Phase 2: Pagination** - Manageable page sizes for person list
- [ ] **Phase 3: Network Discovery** - Person-centered connection network with configurable depth
- [ ] **Phase 4: Evidence Capture** - File and note attachments to people and relationships

## Phase Details

### Phase 1: Quick Actions
**Goal**: Users can navigate directly to key person views from the person list
**Depends on**: Nothing (first phase)
**Requirements**: ACT-01, ACT-02
**Success Criteria** (what must be TRUE):
  1. User can click a link on person list row to view that person's connection network
  2. User can click a link on person list row to view that person's detail page
**Plans**: 1 plan

Plans:
- [ ] 01-01-PLAN.md — Add View Network and View Details action buttons to person list rows

### Phase 2: Pagination
**Goal**: Person list displays manageable page sizes instead of unbounded results
**Depends on**: Phase 1
**Requirements**: INFR-01, INFR-02
**Success Criteria** (what must be TRUE):
  1. Person list displays configurable number of results per page (10, 25, 50, 100)
  2. User can navigate forward and backward through pages of person records
  3. Page controls show current page and total pages
**Plans**: TBD

Plans:
- [ ] TBD

### Phase 3: Network Discovery
**Goal**: Users can explore a person's connections at configurable depth
**Depends on**: Phase 2
**Requirements**: NET-01, NET-02, NET-03
**Success Criteria** (what must be TRUE):
  1. User can view a person's direct connections (1 degree of separation)
  2. User can configure network depth to show 1, 2, or 3+ degrees of separation
  3. Connection links display relationship types (parent, spouse, sibling, etc.)
  4. Network view shows who connects to whom through what relationship
**Plans**: TBD

Plans:
- [ ] TBD

### Phase 4: Evidence Capture
**Goal**: Users can attach files and notes to people and relationships
**Depends on**: Phase 3
**Requirements**: EVID-01, EVID-02, EVID-03, EVID-04, EVID-05, EVID-06
**Success Criteria** (what must be TRUE):
  1. User can upload files (PDF, images, documents) to a person record
  2. User can view list of files attached to a person
  3. User can download files attached to a person
  4. User can delete files attached to a person
  5. User can add freeform text notes to a relationship
  6. User can view notes on a relationship
  7. User can edit and delete notes on a relationship
**Plans**: TBD

Plans:
- [ ] TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Quick Actions | 0/1 | Planned | - |
| 2. Pagination | 0/TBD | Not started | - |
| 3. Network Discovery | 0/TBD | Not started | - |
| 4. Evidence Capture | 0/TBD | Not started | - |

---
*Created: 2026-02-14*
*Last updated: 2026-02-14 after Phase 1 planning*
