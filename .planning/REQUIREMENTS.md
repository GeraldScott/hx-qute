# Requirements: HX-Qute Investigation Tool

**Defined:** 2026-02-14
**Core Value:** Investigators can explore a person's network of relationships at configurable depth and trace the path of connections between any two people.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Network Discovery

- [ ] **NET-01**: User can view a person's connection network showing all directly connected people
- [ ] **NET-02**: User can configure the depth of the connection network (1, 2, or 3+ degrees of separation)
- [ ] **NET-03**: Connection network displays relationship types on each link

### Quick Actions

- [ ] **ACT-01**: User can navigate to a person's connection network from the person list
- [ ] **ACT-02**: User can navigate to a person's detail view from the person list

### Evidence

- [ ] **EVID-01**: User can upload files (PDF, images, documents) attached to a person
- [ ] **EVID-02**: User can view and download files attached to a person
- [ ] **EVID-03**: User can delete files attached to a person
- [ ] **EVID-04**: User can add freeform text notes to a relationship
- [ ] **EVID-05**: User can view notes attached to a relationship
- [ ] **EVID-06**: User can edit and delete notes on a relationship

### Infrastructure

- [ ] **INFR-01**: Person list displays paginated results with configurable page size
- [ ] **INFR-02**: User can navigate between pages of person records

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Network Discovery

- **NET-04**: User can find the path of connections between any two people (path finder)
- **NET-05**: Graph visualization is person-centered replacing current show-all view

### Search

- **SRCH-01**: User can search across all person fields (name, email, phone, notes, date of birth)
- **SRCH-02**: User can combine multiple search filters

### Evidence

- **EVID-07**: User can upload files attached to a relationship
- **EVID-08**: User can add freeform text notes to a person

### Infrastructure

- **INFR-03**: Relationship list displays paginated results

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| Case/investigation management | Defer to future milestone; focus on data and discovery first |
| Real-time notifications | Not needed for investigation workflow |
| Multi-tenant data isolation | Single shared dataset for collaborative team |
| Mobile-native app | Web-first, responsive design sufficient |
| Full audit logging | Basic attribution (createdBy/updatedBy) sufficient for now |
| Structured evidence records | Files and freeform notes cover current needs |
| OAuth/SSO login | Email/password with form auth sufficient for v1 |
| Cloud file storage (S3/MinIO) | Local filesystem for v1; can migrate later |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| ACT-01 | Phase 1 | Pending |
| ACT-02 | Phase 1 | Pending |
| INFR-01 | Phase 2 | Pending |
| INFR-02 | Phase 2 | Pending |
| NET-01 | Phase 3 | Pending |
| NET-02 | Phase 3 | Pending |
| NET-03 | Phase 3 | Pending |
| EVID-01 | Phase 4 | Pending |
| EVID-02 | Phase 4 | Pending |
| EVID-03 | Phase 4 | Pending |
| EVID-04 | Phase 4 | Pending |
| EVID-05 | Phase 4 | Pending |
| EVID-06 | Phase 4 | Pending |

**Coverage:**
- v1 requirements: 13 total
- Mapped to phases: 13
- Unmapped: 0 ✓

---
*Requirements defined: 2026-02-14*
*Last updated: 2026-02-14 after roadmap creation*
