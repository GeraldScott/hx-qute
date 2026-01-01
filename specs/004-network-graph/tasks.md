# Implementation Tasks: Feature 004 - Network Graph Visualization

## Current Status

**Feature:** 004 - Network Graph Visualization
**Status:** 🔲 Not Started
**Current Use Case:** UC-004-01-01

---

## Progress Summary

| Use Case | Status | Tests |
|----------|--------|-------|
| UC-004-01-01: Display Network Graph | 🔲 Not Started | - |
| UC-004-01-02: View Person Details Modal | 🔲 Not Started | - |
| UC-004-01-03: Navigate to Relationship Management | 🔲 Not Started | - |
| UC-004-01-04: Customize Graph Appearance | 🔲 Not Started | - |

**Total:** 0/4 use cases complete

---

## UC-004-01-01: Display Network Graph Page

**Status:** 🔲 Not Started

### Implementation Tasks

- [ ] Create `GraphNode` record DTO (`src/main/java/io/archton/scaffold/dto/GraphNode.java`)
- [ ] Create `GraphLink` record DTO (`src/main/java/io/archton/scaffold/dto/GraphLink.java`)
- [ ] Create `GraphData` record DTO (`src/main/java/io/archton/scaffold/dto/GraphData.java`)
- [ ] Add `countRelationshipsByPerson()` method to `PersonRepository`
- [ ] Add `findAllForGraph()` method to `PersonRelationshipRepository`
- [ ] Create `GraphService` (`src/main/java/io/archton/scaffold/service/GraphService.java`)
- [ ] Create `GraphResource` (`src/main/java/io/archton/scaffold/router/GraphResource.java`)
- [ ] Create graph template (`templates/GraphResource/graph.html`)
- [ ] Add force-graph CDN script to template
- [ ] Implement force-graph initialization JavaScript
- [ ] Add graph route to navigation in `base.html`
- [ ] Add security configuration for `/graph` route
- [ ] Test graph loads with sample data
- [ ] Verify drag, zoom, pan interactions work

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-001 | - | - |
| TC-004-01-002 | - | - |

---

## UC-004-01-02: View Person Details Modal

**Status:** 🔲 Not Started

### Implementation Tasks

- [ ] Implement `onNodeRightClick` handler in graph.html
- [ ] Implement `showPersonModal()` JavaScript function
- [ ] Create person details modal HTML structure
- [ ] Display person name, email, phone, DOB, gender
- [ ] Add "Manage Relationships" button to modal
- [ ] Test right-click opens modal with correct data
- [ ] Test modal closes properly

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-003 | - | - |
| TC-004-01-004 | - | - |

---

## UC-004-01-03: Navigate to Relationship Management

**Status:** 🔲 Not Started

### Implementation Tasks

- [ ] Add link to `/persons/{id}/relationships` in modal
- [ ] Test navigation works from modal
- [ ] Verify modal closes on navigation

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-005 | - | - |

---

## UC-004-01-04: Customize Graph Appearance

**Status:** 🔲 Not Started

### Implementation Tasks

- [ ] Implement gender-based node coloring (Pink/Blue/Gray)
- [ ] Implement node size based on relationship count
- [ ] Add edge labels on hover
- [ ] Add node highlight on hover/selection
- [ ] Implement zoom-dependent label visibility
- [ ] Test visual styling appears correctly

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-006 | - | - |

---

## Next Steps

1. Run `/validate-feature` to verify spec files
2. Run `/implement-feature` to start UC-004-01-01

---

*Last Updated: January 2026*
