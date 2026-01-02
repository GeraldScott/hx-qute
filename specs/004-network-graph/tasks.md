# Implementation Plan for Feature 004 - Network Graph Visualization

## Current Status

**Current Use Case:** UC-004-01-01: Display Network Graph Page
**Status:** 🔲 Not Started
**Blockers:** None

---

## Progress Summary

| Use Case | Status |
|----------|--------|
| UC-004-01-01: Display Network Graph Page | 🔲 Not Started |
| UC-004-01-02: Interact with Graph Nodes | 🔲 Not Started |
| UC-004-01-03: Access Context Menu Actions | 🔲 Not Started |
| UC-004-01-04: Search and Filter Graph | 🔲 Not Started |
| UC-004-01-05: Navigate Graph View | 🔲 Not Started |
| UC-004-01-06: View Edge Tooltip | 🔲 Not Started |

---

## UC-004-01-01: Display Network Graph Page

**Status:** 🔲 Not Started
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Create the graph page with D3.js force-directed visualization showing people as nodes and relationships as edges.

**Implementation Tasks:**
- [ ] Add Graph entry to navigation bar (`fragments/navigation.html`)
- [ ] Create `GraphResource.java` with `@Path("/graph")`
- [ ] Create inner DTO classes: `GraphData`, `GraphNode`, `GraphLink`
- [ ] Implement `showGraph()` endpoint returning graph.html template
- [ ] Implement `getGraphData()` endpoint returning JSON graph data
- [ ] Create `templates/GraphResource/` folder
- [ ] Create `graph.html` template with D3.js CDN includes
- [ ] Create `js/graph.js` with D3.js force simulation initialization
- [ ] Implement `initSimulation()` with forces (link, charge, center, collide)
- [ ] Implement `renderGraph()` for nodes, edges, and labels
- [ ] Implement `ticked()` function for position updates
- [ ] Add node coloring based on gender (pink/blue/gray)
- [ ] Add node sizing based on relationship count
- [ ] Add graph legend for gender colors
- [ ] Add route protection in `application.properties`
- [ ] Handle empty state (no people/relationships)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/graph` | Display network graph page |
| GET | `/graph/data` | Return JSON graph data |

**Test Results:**
- Test ID: TC-004-01-001, TC-004-01-002
- Status: 🔲 Not Tested
- Notes:

---

## UC-004-01-02: Interact with Graph Nodes

**Status:** 🔲 Not Started
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Enable drag-and-drop node interaction with spring-back behavior, hover tooltips, and click-to-highlight neighborhood.

**Implementation Tasks:**
- [ ] Implement `drag()` behavior with D3.js
- [ ] Set `d.fx`/`d.fy` on drag start (fix position)
- [ ] Update `d.fx`/`d.fy` during drag
- [ ] Set `d.fx = null`/`d.fy = null` on drag end (spring back)
- [ ] Configure simulation `alphaTarget` for smooth drag animation
- [ ] Add node tooltip with name and email
- [ ] Implement `handleNodeClick()` for neighborhood highlighting
- [ ] Highlight selected node and all connected nodes
- [ ] Dim non-connected nodes and edges
- [ ] Implement `handleNodeHover()` for visual feedback
- [ ] Implement `handleNodeUnhover()` to reset style
- [ ] Add click-on-background to reset highlights

**Test Results:**
- Test ID: TC-004-01-003, TC-004-01-004, TC-004-01-005
- Status: 🔲 Not Tested
- Notes:

---

## UC-004-01-03: Access Context Menu Actions

**Status:** 🔲 Not Started
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Add right-click context menu with "View Details" and "Manage Relationships" options.

**Implementation Tasks:**
- [ ] Include d3-context-menu CDN (JS + CSS)
- [ ] Implement `getContextMenu()` function
- [ ] Add "View Details" menu item with modal action
- [ ] Add "Manage Relationships" menu item with navigation
- [ ] Create `getPersonDetails()` endpoint in GraphResource
- [ ] Create `personModal.html` template fragment
- [ ] Implement `loadPersonModal()` to fetch and display details
- [ ] Style context menu to match UIkit theme

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/graph/person/{id}` | Return person details HTML for modal |

**Test Results:**
- Test ID: TC-004-01-006, TC-004-01-007
- Status: 🔲 Not Tested
- Notes:

---

## UC-004-01-04: Search and Filter Graph

**Status:** 🔲 Not Started
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Add search-by-name and filter-by-relationship-type functionality.

**Implementation Tasks:**
- [ ] Add search input field to graph page
- [ ] Add relationship type dropdown (populated from Relationship table)
- [ ] Implement debounced `filterBySearch()` function
- [ ] Highlight matching nodes, dim non-matching
- [ ] Implement `filterByRelationship()` function
- [ ] Filter edges by relationship code
- [ ] Dim nodes without matching relationships
- [ ] Add "Reset View" button functionality
- [ ] Clear search and filter inputs on reset

**Test Results:**
- Test ID: TC-004-01-008, TC-004-01-009, TC-004-01-010
- Status: 🔲 Not Tested
- Notes:

---

## UC-004-01-05: Navigate Graph View

**Status:** 🔲 Not Started
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Implement zoom and pan navigation for the graph.

**Implementation Tasks:**
- [ ] Initialize D3 zoom behavior on SVG
- [ ] Configure zoom scale extent (0.1 to 4x)
- [ ] Apply zoom transform to graph container group
- [ ] Enable mouse wheel zoom
- [ ] Enable click-and-drag pan on background
- [ ] Add "Reset View" button to restore initial viewport
- [ ] Animate reset transition
- [ ] Set appropriate cursor styles (grab/grabbing)

**Test Results:**
- Test ID: TC-004-01-011, TC-004-01-012
- Status: 🔲 Not Tested
- Notes:

---

## UC-004-01-06: View Edge Tooltip

**Status:** 🔲 Not Started
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Display relationship type tooltip when hovering over edges.

**Implementation Tasks:**
- [ ] Add `<title>` element to link elements
- [ ] Set title text to relationship type description
- [ ] Ensure edges have `pointer-events: all` CSS

**Test Results:**
- Test ID: TC-004-01-013
- Status: 🔲 Not Tested
- Notes:

---

## Test Cases Reference

### Feature 004 Test Cases

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-004-01-001 | Verify graph page loads with navigation entry | UC-004-01-01 | 🔲 |
| TC-004-01-002 | Verify nodes and edges render from data | UC-004-01-01 | 🔲 |
| TC-004-01-003 | Verify node drag and spring-back behavior | UC-004-01-02 | 🔲 |
| TC-004-01-004 | Verify node tooltip on hover | UC-004-01-02 | 🔲 |
| TC-004-01-005 | Verify neighborhood highlighting on click | UC-004-01-02 | 🔲 |
| TC-004-01-006 | Verify context menu appears on right-click | UC-004-01-03 | 🔲 |
| TC-004-01-007 | Verify View Details opens modal with person info | UC-004-01-03 | 🔲 |
| TC-004-01-008 | Verify search highlights matching nodes | UC-004-01-04 | 🔲 |
| TC-004-01-009 | Verify relationship filter shows correct edges | UC-004-01-04 | 🔲 |
| TC-004-01-010 | Verify reset clears search and filter | UC-004-01-04 | 🔲 |
| TC-004-01-011 | Verify zoom in/out with mouse wheel | UC-004-01-05 | 🔲 |
| TC-004-01-012 | Verify pan by dragging background | UC-004-01-05 | 🔲 |
| TC-004-01-013 | Verify edge tooltip shows relationship type | UC-004-01-06 | 🔲 |

---

## Status Legend

| Symbol | Meaning |
|--------|---------|
| 🔲 | Not Started |
| 🔄 | In Progress |
| ✅ | Complete |
| ❌ | Blocked |
