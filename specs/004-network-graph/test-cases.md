# Test Cases: Feature 004 - Network Graph Visualization

This document defines browser-based E2E test cases for the Network Graph Visualization feature using Cytoscape.js.

---

## Test Environment

| Setting | Value |
|---------|-------|
| Base URL | http://localhost:9080 |
| Test User | admin@example.com / AdminPassword123 |
| Browser | Chrome (via chrome-devtools MCP) |

---

## Prerequisites

Before running tests:
1. Application is running on port 9080
2. Database contains at least 3 people with relationships
3. Test user can log in

---

## TC-004-01-001: Navigate to Graph Page

**Related UC:** UC-004-01-01

**Steps:**
1. Navigate to http://localhost:9080
2. Log in as admin@example.com
3. Click "Graph" link in navigation sidebar
4. Wait for page to load

**Expected Results:**
- URL changes to `/graph`
- Page title contains "Relationship Network"
- Graph container element is visible
- Cytoscape.js canvas is rendered

**Verification:**
```javascript
// Check URL
expect(window.location.pathname).toBe('/graph');
// Check heading
expect(document.querySelector('h2').textContent).toContain('Relationship Network');
// Check graph container exists
expect(document.getElementById('cy')).not.toBeNull();
// Check Cytoscape initialized (cy instance exists)
expect(typeof cy).toBe('object');
expect(cy.nodes().length).toBeGreaterThan(0);
```

---

## TC-004-01-002: Graph Displays Nodes and Edges

**Related UC:** UC-004-01-01

**Prerequisites:**
- At least 3 people exist in database
- At least 2 relationships exist

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Wait for graph to render
3. Inspect graph data

**Expected Results:**
- Graph JSON data is fetched from `/graph/data`
- Cytoscape contains nodes for each person
- Cytoscape contains edges for each relationship
- Nodes have id, name, and group properties
- Edges have source, target, and label properties

**Verification:**
```javascript
// Check nodes exist
const nodes = cy.nodes();
expect(nodes.length).toBeGreaterThan(0);
// Check edges exist
const edges = cy.edges();
expect(edges.length).toBeGreaterThan(0);
// Check node data structure
const firstNode = nodes[0].data();
expect(firstNode).toHaveProperty('id');
expect(firstNode).toHaveProperty('name');
// Check edge data structure
const firstEdge = edges[0].data();
expect(firstEdge).toHaveProperty('source');
expect(firstEdge).toHaveProperty('target');
expect(firstEdge).toHaveProperty('label');
```

---

## TC-004-01-003: Node Tooltip on Hover

**Related UC:** UC-004-01-03

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Wait for graph to render
3. Hover mouse over a node
4. Wait for tooltip to appear

**Expected Results:**
- Tooltip appears after brief delay
- Tooltip shows person's name
- Tooltip shows person's email
- Tooltip disappears when mouse leaves node

**Verification:**
```javascript
// Simulate mouseover on first node
cy.nodes()[0].trigger('mouseover');
// Wait for tooltip
await new Promise(r => setTimeout(r, 400));
// Check tooltip is visible
const tooltip = document.querySelector('.cy-tooltip');
expect(tooltip).not.toBeNull();
expect(tooltip.style.display).not.toBe('none');
expect(tooltip.textContent).toContain('@'); // Email contains @
```

---

## TC-004-01-004: Edge Tooltip on Hover

**Related UC:** UC-004-01-04

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Wait for graph to render
3. Hover mouse over an edge
4. Wait for tooltip to appear

**Expected Results:**
- Tooltip appears showing relationship type
- Tooltip displays text like "Spouse", "Colleague", etc.

**Verification:**
```javascript
// Simulate mouseover on first edge
cy.edges()[0].trigger('mouseover');
await new Promise(r => setTimeout(r, 400));
const tooltip = document.querySelector('.cy-tooltip');
expect(tooltip).not.toBeNull();
// Should contain relationship type (not empty)
expect(tooltip.textContent.trim().length).toBeGreaterThan(0);
```

---

## TC-004-01-005: Right-Click Opens Context Menu

**Related UC:** UC-004-01-05

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Wait for graph to render
3. Right-click on a node
4. Observe context menu

**Expected Results:**
- Browser context menu is prevented
- Custom context menu appears near the node
- Menu contains "View Details" option
- Menu contains "Manage Relationships" option

**Verification:**
```javascript
// Simulate right-click on first node
cy.nodes()[0].trigger('cxttap');
// Check context menu is visible
const contextMenu = document.querySelector('.cy-context-menu');
expect(contextMenu).not.toBeNull();
expect(contextMenu.textContent).toContain('View Details');
expect(contextMenu.textContent).toContain('Manage Relationships');
```

---

## TC-004-01-006: Context Menu - View Details Opens Modal

**Related UC:** UC-004-01-06

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Right-click on a node to open context menu
3. Click "View Details" option
4. Observe modal

**Expected Results:**
- Context menu closes
- Person details modal opens
- Modal shows person name in title
- Modal shows email as mailto link
- Modal shows phone, DOB, gender if present
- Modal shows notes/biography if present
- Modal has "Manage Relationships" button

**Verification:**
```javascript
// Check modal is visible
expect(UIkit.modal('#person-modal').isToggled()).toBe(true);
// Check modal contains person data
const modalBody = document.getElementById('person-modal-body');
expect(modalBody.querySelector('h2')).not.toBeNull();
expect(modalBody.querySelector('a[href^="mailto:"]')).not.toBeNull();
expect(modalBody.querySelector('a[href*="/relationships"]')).not.toBeNull();
```

---

## TC-004-01-007: Context Menu - Manage Relationships Navigates

**Related UC:** UC-004-01-07

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Right-click on a node
3. Click "Manage Relationships" option
4. Wait for navigation

**Expected Results:**
- Context menu closes
- URL changes to `/persons/{id}/relationships`
- Relationship management page loads

**Verification:**
```javascript
// Check URL pattern
expect(window.location.pathname).toMatch(/\/persons\/\d+\/relationships/);
// Check heading
expect(document.querySelector('h2').textContent).toContain('Relationships');
```

---

## TC-004-01-008: Modal Navigate to Relationships

**Related UC:** UC-004-01-07

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Open person details modal (via context menu)
3. Click "Manage Relationships" button in modal
4. Wait for navigation

**Expected Results:**
- Modal closes
- URL changes to `/persons/{id}/relationships`
- Relationship management page loads

**Verification:**
```javascript
expect(window.location.pathname).toMatch(/\/persons\/\d+\/relationships/);
```

---

## TC-004-01-009: Search Highlights Matching Nodes

**Related UC:** UC-004-01-08

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Type a partial name in the search input
3. Observe node highlighting
4. Clear search input
5. Observe nodes return to normal

**Expected Results:**
- Matching nodes are highlighted (brighter/larger)
- Non-matching nodes are dimmed (faded opacity)
- If single match, graph centers on that node
- Clearing search restores all nodes to normal

**Verification:**
```javascript
// Type in search
const searchInput = document.getElementById('graph-search');
searchInput.value = 'Rosa';
searchInput.dispatchEvent(new Event('input'));
await new Promise(r => setTimeout(r, 500));
// Check that some nodes are highlighted
const highlighted = cy.nodes('.highlighted');
expect(highlighted.length).toBeGreaterThan(0);
// Check non-matching are faded
const faded = cy.nodes('.faded');
expect(faded.length).toBeGreaterThan(0);
```

---

## TC-004-01-010: Filter by Relationship Type

**Related UC:** UC-004-01-09

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Select a specific relationship type from filter dropdown
3. Observe graph changes
4. Select "All Relationships"
5. Observe graph restored

**Expected Results:**
- Only edges of selected type are visible
- Other edges are hidden
- Nodes with no visible edges are dimmed
- Selecting "All" restores all edges

**Verification:**
```javascript
// Select filter
const filterSelect = document.getElementById('relationship-filter');
filterSelect.value = 'Spouse';
filterSelect.dispatchEvent(new Event('change'));
await new Promise(r => setTimeout(r, 300));
// Check only Spouse edges visible
const visibleEdges = cy.edges(':visible');
visibleEdges.forEach(edge => {
    expect(edge.data('label')).toBe('Spouse');
});
```

---

## TC-004-01-011: Node Click Highlights Neighborhood

**Related UC:** UC-004-01-02

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Click on a node (not right-click)
3. Observe highlighting
4. Click on background
5. Observe highlighting removed

**Expected Results:**
- Clicked node is highlighted/selected
- Connected nodes are also highlighted
- Edges to connected nodes are highlighted
- Non-connected elements are dimmed
- Clicking background deselects and restores normal styling

**Verification:**
```javascript
// Click node
cy.nodes()[0].trigger('tap');
// Check selection
expect(cy.$(':selected').length).toBe(1);
// Check neighborhood highlighting
const selected = cy.$(':selected');
const neighborhood = selected.neighborhood().add(selected);
neighborhood.forEach(el => {
    expect(el.hasClass('highlighted')).toBe(true);
});
```

---

## TC-004-01-012: Switch Layout - Force to Circular

**Related UC:** UC-004-01-10

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Note initial node positions (Force layout)
3. Click "Circular" layout button
4. Wait for animation to complete
5. Observe node arrangement

**Expected Results:**
- Nodes animate to new positions
- Nodes are arranged in a circle pattern
- All nodes are visible

**Verification:**
```javascript
// Click circular layout button
document.getElementById('layout-circular').click();
await new Promise(r => setTimeout(r, 1000));
// Verify circular arrangement (nodes roughly equidistant from center)
const positions = cy.nodes().map(n => n.position());
const centerX = positions.reduce((sum, p) => sum + p.x, 0) / positions.length;
const centerY = positions.reduce((sum, p) => sum + p.y, 0) / positions.length;
const distances = positions.map(p =>
    Math.sqrt((p.x - centerX)**2 + (p.y - centerY)**2)
);
const avgDist = distances.reduce((a,b) => a+b) / distances.length;
// Most nodes should be within 20% of average distance (circular pattern)
const nearAvg = distances.filter(d => Math.abs(d - avgDist) < avgDist * 0.3);
expect(nearAvg.length / distances.length).toBeGreaterThan(0.7);
```

---

## TC-004-01-013: Graph Visual Styling

**Related UC:** UC-004-01-11

**Steps:**
1. Ensure test data includes people with different genders
2. Navigate to `/graph` (authenticated)
3. Observe node colors and sizes

**Expected Results:**
- Female nodes are pink (#E91E63)
- Male nodes are blue (#2196F3)
- Unspecified gender nodes are gray (#9E9E9E)
- Nodes with more relationships are larger
- Edge labels show relationship type

**Manual Verification:**
- Visually confirm color coding
- Compare node sizes for people with different relationship counts
- Hover over edges to see relationship labels

---

## TC-004-01-014: Minimap Display

**Related UC:** UC-004-01-01

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Observe minimap in corner
3. Pan/zoom the main graph
4. Observe minimap updates

**Expected Results:**
- Minimap shows overview of entire graph
- Viewport indicator shows current view area
- Minimap updates when panning/zooming

**Verification:**
```javascript
// Check minimap container exists
const minimap = document.getElementById('cy-minimap');
expect(minimap).not.toBeNull();
// Check minimap has content (Cytoscape navigator extension)
expect(minimap.querySelector('canvas')).not.toBeNull();
```

---

## TC-004-01-015: Graph Interactivity - Drag and Pan

**Related UC:** UC-004-01-02

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Drag a node to a new position
3. Release the node
4. Observe node behavior
5. Click and drag on background
6. Scroll mouse wheel

**Expected Results:**
- Node follows mouse during drag
- Node may drift back slightly (force layout settling)
- Background drag pans the entire view
- Scroll wheel zooms in/out

**Manual Verification:**
- Test each interaction visually

---

## TC-004-01-016: Export Graph as PNG

**Related UC:** UC-004-02-01

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Optionally adjust zoom/pan
3. Click "Export" button
4. Check downloads

**Expected Results:**
- PNG file is downloaded
- Filename format: `relationship-graph-YYYY-MM-DD.png`
- Image shows current graph view
- Image includes visible nodes, edges, and labels

**Verification:**
```javascript
// Click export button
document.getElementById('btn-export').click();
// Check that download was triggered (this may need manual verification)
// The cy.png() function is called and blob is downloaded
```

---

## TC-004-01-017: Empty State

**Related UC:** UC-004-01-01

**Prerequisites:**
- No people exist in database (or temporarily empty)

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Observe empty state

**Expected Results:**
- Empty state message is displayed
- Message includes link to add people
- No JavaScript errors in console

**Verification:**
```javascript
// Check for empty state message
const container = document.getElementById('cy');
expect(container.textContent).toContain('No people found');
expect(container.querySelector('a[href="/persons"]')).not.toBeNull();
```

---

## TC-004-01-018: Authentication Required

**Related UC:** UC-004-01-01

**Steps:**
1. Log out or use incognito browser
2. Navigate directly to `/graph`

**Expected Results:**
- User is redirected to login
- After login, user reaches graph page

**Verification:**
```javascript
// Check redirect to login
expect(window.location.search).toContain('login');
```

---

## Test Summary

| Test ID | Description | Priority | UC |
|---------|-------------|----------|-----|
| TC-004-01-001 | Navigate to Graph Page | High | UC-004-01-01 |
| TC-004-01-002 | Graph Displays Nodes and Edges | High | UC-004-01-01 |
| TC-004-01-003 | Node Tooltip on Hover | High | UC-004-01-03 |
| TC-004-01-004 | Edge Tooltip on Hover | Medium | UC-004-01-04 |
| TC-004-01-005 | Right-Click Opens Context Menu | High | UC-004-01-05 |
| TC-004-01-006 | Context Menu - View Details | High | UC-004-01-06 |
| TC-004-01-007 | Context Menu - Manage Relationships | High | UC-004-01-07 |
| TC-004-01-008 | Modal Navigate to Relationships | High | UC-004-01-07 |
| TC-004-01-009 | Search Highlights Nodes | Medium | UC-004-01-08 |
| TC-004-01-010 | Filter by Relationship Type | Medium | UC-004-01-09 |
| TC-004-01-011 | Node Click Neighborhood Highlight | Medium | UC-004-01-02 |
| TC-004-01-012 | Switch Layout | Low | UC-004-01-10 |
| TC-004-01-013 | Visual Styling | Medium | UC-004-01-11 |
| TC-004-01-014 | Minimap Display | Medium | UC-004-01-01 |
| TC-004-01-015 | Drag and Pan Interactions | Medium | UC-004-01-02 |
| TC-004-01-016 | Export Graph as PNG | Low | UC-004-02-01 |
| TC-004-01-017 | Empty State | Medium | UC-004-01-01 |
| TC-004-01-018 | Authentication Required | High | UC-004-01-01 |

---

*Document Version: 2.0*
*Last Updated: January 2026*
*Technology: Cytoscape.js*
