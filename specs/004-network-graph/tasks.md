# Implementation Tasks: Feature 004 - Network Graph Visualization

## Current Status

**Feature:** 004 - Network Graph Visualization
**Status:** 🔄 Migration Required
**Technology Change:** force-graph → Cytoscape.js
**Current Phase:** Implementation pending

---

## Migration Summary

The graph visualization is being upgraded from `force-graph` to `Cytoscape.js` to provide:
- Context menus (right-click actions)
- Tooltips on hover
- Multiple layout algorithms
- Search and filtering
- PNG export
- Better scalability

---

## Progress Summary

| Use Case | Status | Tests |
|----------|--------|-------|
| UC-004-01-01: Display Network Graph | ⏳ Pending | ⏳ Pending |
| UC-004-01-02: Graph Navigation | ⏳ Pending | ⏳ Pending |
| UC-004-01-03: Node Tooltip | ⏳ Pending | ⏳ Pending |
| UC-004-01-04: Edge Tooltip | ⏳ Pending | ⏳ Pending |
| UC-004-01-05: Node Context Menu | ⏳ Pending | ⏳ Pending |
| UC-004-01-06: Person Details Modal | ⏳ Pending | ⏳ Pending |
| UC-004-01-07: Navigate to Relationships | ⏳ Pending | ⏳ Pending |
| UC-004-01-08: Search and Highlight | ⏳ Pending | ⏳ Pending |
| UC-004-01-09: Filter by Relationship | ⏳ Pending | ⏳ Pending |
| UC-004-01-10: Switch Layout | ⏳ Pending | ⏳ Pending |
| UC-004-01-11: Visual Styling | ⏳ Pending | ⏳ Pending |
| UC-004-02-01: Export PNG | ⏳ Pending | ⏳ Pending |

**Total:** 0/12 use cases complete

---

## Phase 1: Backend DTO Updates

**Status:** ⏳ Pending

### Tasks

- [ ] Update `GraphNode.java` record:
  - [ ] Rename `val` field to `weight`
  - [ ] Add `genderCode` field (separate from `gender` description)
  - [ ] Update `from()` factory method

- [ ] Create `GraphEdge.java` record (rename from `GraphLink.java`):
  - [ ] Add `id` field for Cytoscape edge identification
  - [ ] Keep `source`, `target`, `label` fields
  - [ ] Update `from()` factory method

- [ ] Update `GraphData.java` record:
  - [ ] Create nested `CyNode` wrapper record with `data` field
  - [ ] Create nested `CyEdge` wrapper record with `data` field
  - [ ] Add `relationshipTypes` field for filter dropdown
  - [ ] Update constructor to use new wrapper types

- [ ] Delete `GraphLink.java` (replaced by `GraphEdge.java`)

---

## Phase 2: Repository Updates

**Status:** ⏳ Pending

### Tasks

- [ ] Add `findDistinctRelationshipTypes()` method to `PersonRelationshipRepository`:
  ```java
  public List<String> findDistinctRelationshipTypes() {
      return getEntityManager()
          .createQuery("SELECT DISTINCT r.description FROM Relationship r ORDER BY r.description", String.class)
          .getResultList();
  }
  ```

- [ ] Verify `findAllForGraph()` uses entity graph correctly

---

## Phase 3: Service Layer Updates

**Status:** ⏳ Pending

### Tasks

- [ ] Update `GraphService.buildGraphData()`:
  - [ ] Use new `CyNode` and `CyEdge` wrapper types
  - [ ] Add relationship types for filter dropdown
  - [ ] Update stream mappings for new DTO structure

---

## Phase 4: Template Rewrite

**Status:** ⏳ Pending

### Tasks

- [ ] Rewrite `templates/GraphResource/graph.html`:
  - [ ] Replace force-graph CDN with Cytoscape.js and extensions:
    - [ ] cytoscape.min.js (3.30.4)
    - [ ] cytoscape-context-menus (4.1.0)
    - [ ] cytoscape-popper (2.0.0)
    - [ ] @popperjs/core (2.11.8)
    - [ ] tippy.js (6.3.7)
  - [ ] Add toolbar with layout buttons and export
  - [ ] Add search input field
  - [ ] Add relationship filter dropdown
  - [ ] Add stats display (node/edge counts)
  - [ ] Add minimap container
  - [ ] Update graph container element (id="cy")
  - [ ] Rewrite JavaScript initialization:
    - [ ] Cytoscape style definitions
    - [ ] Context menu configuration
    - [ ] Tooltip initialization (Tippy.js)
    - [ ] Event handlers (tap, mouseover, etc.)
    - [ ] Search handler with debounce
    - [ ] Filter handler
    - [ ] Layout button handlers
    - [ ] Export PNG handler
    - [ ] Neighborhood highlighting
    - [ ] HTMX cleanup handler

---

## Phase 5: Feature Implementation by Use Case

### UC-004-01-01: Display Network Graph

**Status:** ⏳ Pending

- [ ] Fetch graph data from `/graph/data`
- [ ] Initialize Cytoscape with CoSE layout
- [ ] Handle empty state
- [ ] Show loading indicator
- [ ] Display node/edge stats

**Tests:**
- [ ] TC-004-01-001: Navigate to Graph Page
- [ ] TC-004-01-002: Graph Displays Nodes and Edges

---

### UC-004-01-02: Graph Navigation

**Status:** ⏳ Pending

- [ ] Enable pan (background drag)
- [ ] Enable zoom (scroll wheel)
- [ ] Enable node drag
- [ ] Enable node selection with neighborhood highlight
- [ ] Enable background click to deselect

**Tests:**
- [ ] TC-004-01-011: Node Click Neighborhood Highlight
- [ ] TC-004-01-015: Drag and Pan Interactions

---

### UC-004-01-03: Node Tooltip

**Status:** ⏳ Pending

- [ ] Initialize cytoscape-popper extension
- [ ] Create Tippy.js tooltips for nodes
- [ ] Show name and email on hover
- [ ] Delay before showing (300ms)

**Tests:**
- [ ] TC-004-01-003: Node Tooltip on Hover

---

### UC-004-01-04: Edge Tooltip

**Status:** ⏳ Pending

- [ ] Create Tippy.js tooltips for edges
- [ ] Show relationship type on hover

**Tests:**
- [ ] TC-004-01-004: Edge Tooltip on Hover

---

### UC-004-01-05: Node Context Menu

**Status:** ⏳ Pending

- [ ] Initialize cytoscape-context-menus extension
- [ ] Add "View Details" menu item
- [ ] Add "Manage Relationships" menu item
- [ ] Prevent browser context menu

**Tests:**
- [ ] TC-004-01-005: Right-Click Opens Context Menu

---

### UC-004-01-06: Person Details Modal

**Status:** ⏳ Pending

- [ ] Implement `showPersonModal()` function
- [ ] Display name, email, phone, DOB, gender, notes
- [ ] Include "Manage Relationships" button
- [ ] Use UIkit modal

**Tests:**
- [ ] TC-004-01-006: Context Menu - View Details Opens Modal

---

### UC-004-01-07: Navigate to Relationships

**Status:** ⏳ Pending

- [ ] Navigate from context menu
- [ ] Navigate from modal button
- [ ] Use correct person ID in URL

**Tests:**
- [ ] TC-004-01-007: Context Menu - Manage Relationships
- [ ] TC-004-01-008: Modal Navigate to Relationships

---

### UC-004-01-08: Search and Highlight

**Status:** ⏳ Pending

- [ ] Add search input handler with debounce
- [ ] Filter nodes by name (case-insensitive)
- [ ] Highlight matching nodes
- [ ] Fade non-matching nodes
- [ ] Center on single match
- [ ] Reset on empty search

**Tests:**
- [ ] TC-004-01-009: Search Highlights Nodes

---

### UC-004-01-09: Filter by Relationship Type

**Status:** ⏳ Pending

- [ ] Populate filter dropdown from API data
- [ ] Add filter select handler
- [ ] Hide non-matching edges
- [ ] Fade orphaned nodes
- [ ] Reset on "All Relationships"

**Tests:**
- [ ] TC-004-01-010: Filter by Relationship Type

---

### UC-004-01-10: Switch Layout

**Status:** ⏳ Pending

- [ ] Add Force (CoSE) layout button
- [ ] Add Circular layout button
- [ ] Add Grid layout button
- [ ] Animate layout transitions

**Tests:**
- [ ] TC-004-01-012: Switch Layout

---

### UC-004-01-11: Visual Styling

**Status:** ⏳ Pending

- [ ] Color nodes by gender (Pink/Blue/Gray)
- [ ] Size nodes by relationship count (logarithmic)
- [ ] Style selected nodes (yellow border)
- [ ] Style highlighted/faded states
- [ ] Style edges with arrows
- [ ] Show edge labels

**Tests:**
- [ ] TC-004-01-013: Graph Visual Styling

---

### UC-004-02-01: Export PNG

**Status:** ⏳ Pending

- [ ] Add Export button to toolbar
- [ ] Use `cy.png()` to capture graph
- [ ] Trigger download with date-stamped filename

**Tests:**
- [ ] TC-004-01-016: Export Graph as PNG

---

## Phase 6: Additional Features

**Status:** ⏳ Pending

### Minimap

- [ ] Add minimap container to template
- [ ] Consider using cytoscape-navigator extension (optional)
- [ ] Or implement simple viewport indicator

**Tests:**
- [ ] TC-004-01-014: Minimap Display

---

## Phase 7: Testing & Verification

**Status:** ⏳ Pending

- [ ] Run E2E tests with chrome-devtools MCP
- [ ] Verify all 18 test cases pass
- [ ] Test with sample data (62 people, 116 relationships)
- [ ] Test empty state
- [ ] Test authentication redirect
- [ ] Performance test with larger datasets

---

## Phase 8: Cleanup

**Status:** ⏳ Pending

- [ ] Remove force-graph CDN reference (if any remain)
- [ ] Delete old `GraphLink.java` file
- [ ] Update any documentation referencing force-graph
- [ ] Verify no console errors

---

## Implementation Order

Recommended implementation sequence:

1. **Phase 1-3**: Backend changes (DTO, Repository, Service)
2. **UC-004-01-01**: Basic graph display
3. **UC-004-01-02**: Navigation (pan, zoom, drag)
4. **UC-004-01-11**: Visual styling
5. **UC-004-01-03**: Node tooltips
6. **UC-004-01-04**: Edge tooltips
7. **UC-004-01-05**: Context menu
8. **UC-004-01-06**: Person modal
9. **UC-004-01-07**: Navigation to relationships
10. **UC-004-01-08**: Search functionality
11. **UC-004-01-09**: Relationship filter
12. **UC-004-01-10**: Layout switching
13. **UC-004-02-01**: PNG export
14. **Phase 7**: Testing
15. **Phase 8**: Cleanup

---

## Dependencies

### New CDN Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| Cytoscape.js | 3.30.4 | Core graph library |
| cytoscape-context-menus | 4.1.0 | Right-click menus |
| cytoscape-popper | 2.0.0 | Tooltip positioning |
| Popper.js | 2.11.8 | Tooltip positioning engine |
| Tippy.js | 6.3.7 | Tooltip styling |

### Existing (No Changes)

- quarkus-rest-jsonb
- quarkus-hibernate-orm-panache
- quarkus-rest-qute
- UIkit 3.25
- HTMX 2.0.8

---

## Risk Assessment

| Risk | Mitigation |
|------|------------|
| CDN availability | Use multiple CDN sources (cdnjs, jsdelivr) |
| Bundle size (~600KB) | Acceptable for feature richness |
| Learning curve | Comprehensive spec.md documentation |
| Cytoscape API changes | Pin specific versions |

---

*Last Updated: January 2026*
*Technology: Cytoscape.js 3.30.4*
