# Test Cases for Feature 004: Network Graph Visualization

## Prerequisites

- Application running at `http://localhost:9080`
- At least 3 Person records with relationships exist
- Person records have different genders assigned (Male, Female, and one without)
- Test user credentials available

## Test Data

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | AdminPassword123 | admin |

## Test Data Setup

Before running tests, ensure the following test data exists:

**People:**
| First Name | Last Name | Email | Gender |
|------------|-----------|-------|--------|
| John | Smith | john@example.com | Male |
| Jane | Smith | jane@example.com | Female |
| Alex | Johnson | alex@example.com | (none) |

**Relationships:**
| Source | Related | Type |
|--------|---------|------|
| John Smith | Jane Smith | Spouse |
| John Smith | Alex Johnson | Colleague |

---

# US-004-01: Display a network diagram of the relationships between people

### TC-004-01-001: Verify graph page loads with navigation entry
**Parent Use Case:** [UC-004-01-01: Display Network Graph Page](use-cases.md#uc-004-01-01-display-network-graph-page)

**Objective:** Verify the Graph navigation link exists and loads the graph page

**Steps:**
1. Navigate to `http://localhost:9080`
2. Log in as admin@example.com
3. Locate the navigation sidebar
4. Click on "Graph" link

**Expected:**
- [ ] "Graph" entry appears in navigation after "People"
- [ ] Graph icon is displayed (git-fork icon)
- [ ] Clicking loads `/graph` page
- [ ] Page title shows "Network Graph"
- [ ] Graph container is visible

---

### TC-004-01-002: Verify nodes and edges render from data
**Parent Use Case:** [UC-004-01-01: Display Network Graph Page](use-cases.md#uc-004-01-01-display-network-graph-page)

**Objective:** Verify the graph correctly renders nodes and edges from data

**Precondition:** Test data with 3 people and 2 relationships exists

**Steps:**
1. Navigate to `/graph`
2. Wait for graph to load and stabilize
3. Count visible nodes (circles)
4. Count visible edges (lines)
5. Verify node colors

**Expected:**
- [ ] 3 nodes are displayed
- [ ] 2 edges are displayed connecting appropriate nodes
- [ ] John Smith node is blue (Male)
- [ ] Jane Smith node is pink (Female)
- [ ] Alex Johnson node is gray (no gender)
- [ ] Legend shows gender color mapping
- [ ] Node labeled "John Smith" is larger (more connections)

---

### TC-004-01-003: Verify node drag and spring-back behavior
**Parent Use Case:** [UC-004-01-02: Interact with Graph Nodes](use-cases.md#uc-004-01-02-interact-with-graph-nodes)

**Objective:** Verify nodes can be dragged and spring back when released

**Steps:**
1. Navigate to `/graph`
2. Wait for graph to stabilize
3. Click and hold on John Smith node
4. Drag node to a new position
5. Observe connected nodes and edges while dragging
6. Release the node
7. Observe node behavior after release

**Expected:**
- [ ] Node follows cursor while dragging
- [ ] Connected edges stretch/move with dragged node
- [ ] Other nodes adjust positions (force simulation active)
- [ ] After release, node begins moving back toward equilibrium
- [ ] Graph eventually stabilizes in new configuration
- [ ] Node does NOT stay fixed at dropped position

---

### TC-004-01-004: Verify node tooltip on hover
**Parent Use Case:** [UC-004-01-02: Interact with Graph Nodes](use-cases.md#uc-004-01-02-interact-with-graph-nodes)

**Objective:** Verify tooltip appears when hovering over a node

**Steps:**
1. Navigate to `/graph`
2. Hover mouse over John Smith node
3. Wait briefly for tooltip
4. Read tooltip content
5. Move mouse away from node

**Expected:**
- [ ] Tooltip appears after brief hover
- [ ] Tooltip shows "John Smith"
- [ ] Tooltip shows "john@example.com"
- [ ] Node gets visual highlight on hover (thicker border)
- [ ] Tooltip disappears when mouse moves away
- [ ] Node highlight removed when mouse moves away

---

### TC-004-01-005: Verify neighborhood highlighting on click
**Parent Use Case:** [UC-004-01-02: Interact with Graph Nodes](use-cases.md#uc-004-01-02-interact-with-graph-nodes)

**Objective:** Verify clicking a node highlights its neighborhood

**Steps:**
1. Navigate to `/graph`
2. Click on John Smith node
3. Observe highlighting behavior
4. Click on empty graph area

**Expected:**
- [ ] Clicked node (John) remains fully visible
- [ ] Connected nodes (Jane, Alex) remain fully visible
- [ ] Any unconnected nodes are dimmed
- [ ] Edges connected to John are highlighted
- [ ] Edges not connected to John are dimmed
- [ ] Clicking empty area resets all nodes to normal opacity

---

### TC-004-01-006: Verify context menu appears on right-click
**Parent Use Case:** [UC-004-01-03: Access Context Menu Actions](use-cases.md#uc-004-01-03-access-context-menu-actions)

**Objective:** Verify right-click on node shows context menu

**Steps:**
1. Navigate to `/graph`
2. Right-click on John Smith node
3. Observe context menu

**Expected:**
- [ ] Context menu appears at cursor position
- [ ] Menu shows "View Details" option
- [ ] Menu shows "Manage Relationships" option
- [ ] Menu has divider between options
- [ ] Clicking elsewhere closes menu

---

### TC-004-01-007: Verify View Details opens modal with person info
**Parent Use Case:** [UC-004-01-03: Access Context Menu Actions](use-cases.md#uc-004-01-03-access-context-menu-actions)

**Objective:** Verify View Details action opens modal with person information

**Steps:**
1. Navigate to `/graph`
2. Right-click on John Smith node
3. Click "View Details"
4. Observe modal content
5. Close modal

**Expected:**
- [ ] Modal opens with title "Person Details"
- [ ] Modal shows full name (with title if present)
- [ ] Modal shows email address
- [ ] Modal shows phone (if present)
- [ ] Modal shows date of birth (if present)
- [ ] Modal shows gender
- [ ] Close button works

---

### TC-004-01-008: Verify search highlights matching nodes
**Parent Use Case:** [UC-004-01-04: Search and Filter Graph](use-cases.md#uc-004-01-04-search-and-filter-graph)

**Objective:** Verify search box filters nodes by name

**Steps:**
1. Navigate to `/graph`
2. Type "Smith" in search box
3. Observe node highlighting
4. Clear search box

**Expected:**
- [ ] Both John Smith and Jane Smith remain highlighted
- [ ] Alex Johnson is dimmed
- [ ] Search is case-insensitive
- [ ] Clearing search restores all nodes to normal

---

### TC-004-01-009: Verify relationship filter shows correct edges
**Parent Use Case:** [UC-004-01-04: Search and Filter Graph](use-cases.md#uc-004-01-04-search-and-filter-graph)

**Objective:** Verify relationship dropdown filters by relationship type

**Steps:**
1. Navigate to `/graph`
2. Select "Spouse" from relationship dropdown
3. Observe edge filtering
4. Select "All Relationships"

**Expected:**
- [ ] Only "Spouse" edge remains visible/highlighted
- [ ] "Colleague" edge is dimmed
- [ ] John and Jane nodes remain highlighted (connected by Spouse)
- [ ] Alex node is dimmed (no Spouse relationship)
- [ ] Selecting "All" restores all edges and nodes

---

### TC-004-01-010: Verify reset clears search and filter
**Parent Use Case:** [UC-004-01-04: Search and Filter Graph](use-cases.md#uc-004-01-04-search-and-filter-graph)

**Objective:** Verify Reset View button clears all filters

**Steps:**
1. Navigate to `/graph`
2. Type "John" in search box
3. Select "Spouse" from dropdown
4. Zoom in on graph
5. Click "Reset View" button

**Expected:**
- [ ] Search box is cleared
- [ ] Dropdown shows "All Relationships"
- [ ] All nodes restored to normal opacity
- [ ] All edges restored to normal opacity
- [ ] Zoom is reset to initial level
- [ ] Graph recenters

---

### TC-004-01-011: Verify zoom in/out with mouse wheel
**Parent Use Case:** [UC-004-01-05: Navigate Graph View](use-cases.md#uc-004-01-05-navigate-graph-view)

**Objective:** Verify mouse wheel zooms the graph

**Steps:**
1. Navigate to `/graph`
2. Position mouse over graph center
3. Scroll mouse wheel up (zoom in)
4. Scroll mouse wheel down (zoom out)

**Expected:**
- [ ] Scrolling up zooms in (nodes appear larger)
- [ ] Scrolling down zooms out (nodes appear smaller)
- [ ] Zoom is centered on cursor position
- [ ] Zoom has minimum limit (not infinite zoom out)
- [ ] Zoom has maximum limit (not infinite zoom in)
- [ ] Node labels scale appropriately

---

### TC-004-01-012: Verify pan by dragging background
**Parent Use Case:** [UC-004-01-05: Navigate Graph View](use-cases.md#uc-004-01-05-navigate-graph-view)

**Objective:** Verify dragging empty area pans the view

**Steps:**
1. Navigate to `/graph`
2. Zoom in to make graph larger than viewport
3. Click and drag on empty area (not on a node)
4. Release mouse

**Expected:**
- [ ] Cursor changes to grab cursor on hover
- [ ] Cursor changes to grabbing cursor while dragging
- [ ] Graph viewport pans in drag direction
- [ ] Position is maintained after release
- [ ] Nodes do not move relative to each other (just viewport moves)

---

### TC-004-01-013: Verify edge tooltip shows relationship type
**Parent Use Case:** [UC-004-01-06: View Edge Tooltip](use-cases.md#uc-004-01-06-view-edge-tooltip)

**Objective:** Verify hovering over edge shows relationship type

**Steps:**
1. Navigate to `/graph`
2. Locate edge between John and Jane Smith
3. Hover mouse over the edge line
4. Wait for tooltip

**Expected:**
- [ ] Tooltip appears showing "Spouse"
- [ ] Tooltip disappears when mouse moves away

---

## Test Summary

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-004-01-001 | Graph page loads with navigation | UC-004-01-01 | [ ] | |
| TC-004-01-002 | Nodes and edges render | UC-004-01-01 | [ ] | |
| TC-004-01-003 | Node drag and spring-back | UC-004-01-02 | [ ] | |
| TC-004-01-004 | Node tooltip on hover | UC-004-01-02 | [ ] | |
| TC-004-01-005 | Neighborhood highlighting | UC-004-01-02 | [ ] | |
| TC-004-01-006 | Context menu appears | UC-004-01-03 | [ ] | |
| TC-004-01-007 | View Details modal | UC-004-01-03 | [ ] | |
| TC-004-01-008 | Search highlights nodes | UC-004-01-04 | [ ] | |
| TC-004-01-009 | Relationship filter | UC-004-01-04 | [ ] | |
| TC-004-01-010 | Reset clears filters | UC-004-01-04 | [ ] | |
| TC-004-01-011 | Zoom with mouse wheel | UC-004-01-05 | [ ] | |
| TC-004-01-012 | Pan by dragging | UC-004-01-05 | [ ] | |
| TC-004-01-013 | Edge tooltip | UC-004-01-06 | [ ] | |

---

## Traceability Matrix

| User Story | Use Cases | Test Cases |
|------------|-----------|------------|
| US-004-01: Display network diagram | UC-004-01-01 | TC-004-01-001, TC-004-01-002 |
| | UC-004-01-02 | TC-004-01-003, TC-004-01-004, TC-004-01-005 |
| | UC-004-01-03 | TC-004-01-006, TC-004-01-007 |
| | UC-004-01-04 | TC-004-01-008, TC-004-01-009, TC-004-01-010 |
| | UC-004-01-05 | TC-004-01-011, TC-004-01-012 |
| | UC-004-01-06 | TC-004-01-013 |
