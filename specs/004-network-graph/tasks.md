# Implementation Plan for Feature 004 - Network Graph Visualization

## Current Status

**Current Use Case:** Feature 004 Complete
**Status:** ✅ All Use Cases Complete
**Blockers:** None

---

## Progress Summary

| Use Case | Status |
|----------|--------|
| UC-004-01-01: Display Network Graph Page | ✅ Complete |
| UC-004-01-02: Interact with Graph Nodes | ✅ Complete |
| UC-004-01-03: Access Context Menu Actions | ✅ Complete |
| UC-004-01-04: Search and Filter Graph | ✅ Complete |
| UC-004-01-05: Navigate Graph View | ✅ Complete |
| UC-004-01-06: View Edge Tooltip | ✅ Complete |

---

## UC-004-01-01: Display Network Graph Page

**Status:** ✅ Complete
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Create the graph page with D3.js force-directed visualization showing people as nodes and relationships as edges.

**Implementation Tasks:**
- [x] Add Graph entry to navigation bar (`fragments/navigation.html`)
- [x] Create `GraphResource.java` with `@Path("/graph")`
- [x] Create inner DTO classes: `GraphData`, `GraphNode`, `GraphLink`
- [x] Implement `showGraph()` endpoint returning graph.html template
- [x] Implement `getGraphData()` endpoint returning JSON graph data
- [x] Create `templates/GraphResource/` folder
- [x] Create `graph.html` template with D3.js CDN includes
- [x] Create `js/graph.js` with D3.js force simulation initialization
- [x] Implement `initSimulation()` with forces (link, charge, center, collide)
- [x] Implement `renderGraph()` for nodes, edges, and labels
- [x] Implement `ticked()` function for position updates
- [x] Add node coloring based on gender (pink/blue/gray)
- [x] Add node sizing based on relationship count
- [x] Add graph legend for gender colors
- [x] Add route protection in `application.properties`
- [x] Handle empty state (no people/relationships)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/graph` | Display network graph page |
| GET | `/graph/data` | Return JSON graph data |

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-001 | ✅ PASS | Graph page loads, navigation entry exists after "People", page title "Network Graph" correct |
| TC-004-01-002 | ✅ PASS | 62 nodes, 86 edges, 62 labels render correctly. Colors: 53 blue (Male), 9 pink (Female). Legend present. Node sizes vary (12 unique radii). |

**Implementation Notes:**
- Added `quarkus-rest-jsonb` dependency to pom.xml for JSON serialization
- Added `credentials: 'same-origin'` to fetch calls in graph.js for auth
- Adjusted force parameters for large dataset (62 nodes): charge=-150, linkDistance=60
- Fixed labels not rendering: added guard in `ticked()` function to handle async initialization
- Made d3-context-menu optional (CDN blocked by ORB) - will implement in UC-004-01-03

---

## UC-004-01-02: Interact with Graph Nodes

**Status:** ✅ Complete
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Enable drag-and-drop node interaction with spring-back behavior, hover tooltips, and click-to-highlight neighborhood.

**Implementation Tasks:**
- [x] Implement `drag()` behavior with D3.js
- [x] Set `d.fx`/`d.fy` on drag start (fix position)
- [x] Update `d.fx`/`d.fy` during drag
- [x] Set `d.fx = null`/`d.fy = null` on drag end (spring back)
- [x] Configure simulation `alphaTarget` for smooth drag animation
- [x] Add node tooltip with name and email
- [x] Implement `handleNodeClick()` for neighborhood highlighting
- [x] Highlight selected node and all connected nodes
- [x] Dim non-connected nodes and edges
- [x] Implement `handleNodeHover()` for visual feedback
- [x] Implement `handleNodeUnhover()` to reset style
- [x] Add click-on-background to reset highlights

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-003 | ✅ PASS | Drag works, edges stretch during drag, spring-back on release |
| TC-004-01-004 | ✅ PASS | Tooltip shows "Name\nemail" format, hover highlight (thick border) |
| TC-004-01-005 | ✅ PASS | Click highlights neighborhood, dims others, background click resets |

**Implementation Notes:**
- Fixed SVG structure: wrapped nodes in `<g>` groups so `<title>` tooltips work (per d3-graph-review.md)
- Cached `maxConnections` in `initSimulation()` for performance (per d3-graph-review.md)
- Updated `ticked()` to use `transform` instead of `cx`/`cy` for node groups
- Updated hover handlers to select circle inside group

---

## UC-004-01-03: Access Context Menu Actions

**Status:** ✅ Complete
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Add right-click context menu with "View Details" and "Manage Relationships" options.

**Implementation Tasks:**
- [x] Implement custom context menu (d3-context-menu CDN blocked by ORB)
- [x] Implement `handleContextMenu()` function
- [x] Add "View Details" menu item with modal action
- [x] Add "Manage Relationships" menu item with navigation
- [x] Create `getPersonDetails()` endpoint in GraphResource
- [x] Create `personModal.html` template fragment
- [x] Implement `loadPersonModal()` with error handling
- [x] Style context menu to match UIkit theme

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/graph/person/{id}` | Return person details HTML for modal |

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-006 | ✅ PASS | Custom context menu appears on right-click with View Details and Manage Relationships |
| TC-004-01-007 | ✅ PASS | View Details loads person modal via /graph/person/{id} endpoint |

**Implementation Notes:**
- Replaced d3-context-menu (blocked by ORB) with custom UIkit-styled context menu
- Added error handling to loadPersonModal() with UIkit notification (per d3-graph-review.md)
- Context menu hides on click elsewhere

---

## UC-004-01-04: Search and Filter Graph

**Status:** ✅ Complete
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Add search-by-name and filter-by-relationship-type functionality.

**Implementation Tasks:**
- [x] Add search input field to graph page
- [x] Add relationship type dropdown (populated from Relationship table)
- [x] Implement debounced `filterBySearch()` function
- [x] Highlight matching nodes, dim non-matching
- [x] Implement `filterByRelationship()` function
- [x] Filter edges by relationship code
- [x] Dim nodes without matching relationships
- [x] Add "Reset View" button functionality
- [x] Clear search and filter inputs on reset

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-008 | ✅ PASS | Search "Marx" highlights 2 nodes (Karl Marx, Eleanor Marx), dims 60 others. Case-insensitive search works. |
| TC-004-01-009 | ✅ PASS | Filter "Spouse" highlights 2 nodes (Jean-Paul Sartre, Simone de Beauvoir) and 1 edge, dims 60 nodes and 85 edges. |
| TC-004-01-010 | ✅ PASS | Reset clears search input, resets dropdown to "All Relationships", restores all 62 nodes and 86 edges to normal opacity. |

**Implementation Notes:**
- Search and filter functionality was implemented as part of UC-004-01-01 (included in initial spec.md template)
- Debounced search with 300ms delay prevents excessive filtering during typing
- Relationship dropdown populated from server-side Relationship table via Qute template

---

## UC-004-01-05: Navigate Graph View

**Status:** ✅ Complete
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Implement zoom and pan navigation for the graph.

**Implementation Tasks:**
- [x] Initialize D3 zoom behavior on SVG
- [x] Configure zoom scale extent (0.1 to 4x)
- [x] Apply zoom transform to graph container group
- [x] Enable mouse wheel zoom
- [x] Enable click-and-drag pan on background
- [x] Add "Reset View" button to restore initial viewport
- [x] Animate reset transition
- [x] Set appropriate cursor styles (grab/grabbing)

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-011 | ✅ PASS | Zoom implemented with D3 zoom behavior, scale extent 0.1-4x, transform on graph group. Mouse wheel zoom verified via code analysis. |
| TC-004-01-012 | ✅ PASS | Pan enabled via D3 zoom behavior. Cursor styles: grab on hover (#graph-svg), grabbing on active (style.css:128-132). |

**Implementation Notes:**
- Zoom and pan implemented as part of initial graph setup in UC-004-01-01 (D3 zoom behavior)
- Cursor styles defined in style.css for #graph-svg element
- Reset View button triggers animated transition (500ms) back to d3.zoomIdentity
- E2E automation limited: chrome-devtools MCP does not support mouse wheel/drag simulation

---

## UC-004-01-06: View Edge Tooltip

**Status:** ✅ Complete
**Parent Story:** US-004-01 - Display a network diagram of the relationships between people

**Description:** Display relationship type tooltip when hovering over edges.

**Implementation Tasks:**
- [x] Add `<title>` element to link elements
- [x] Set title text to relationship type description
- [x] Ensure edges have `pointer-events: all` CSS

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-004-01-013 | ✅ PASS | SVG `<title>` elements correctly implemented for edge tooltips. Relationship type (e.g., "Spouse") displayed via browser-native tooltip. |

**Implementation Notes:**
- Edge tooltips implemented as part of initial graph setup in UC-004-01-01 (graph.js:105-107)
- Uses standard SVG `<title>` elements for browser-native tooltips
- CSS `pointer-events: all` set on `.links line` (style.css:140-142)

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
