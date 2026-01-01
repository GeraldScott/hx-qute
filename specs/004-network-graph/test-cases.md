# Test Cases: Feature 004 - Network Graph Visualization

This document defines browser-based E2E test cases for the Network Graph Visualization feature.

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
- Graph canvas is rendered

**Verification:**
```javascript
// Check URL
expect(window.location.pathname).toBe('/graph');
// Check heading
expect(document.querySelector('h2').textContent).toContain('Relationship Network');
// Check graph container exists
expect(document.getElementById('graph-container')).not.toBeNull();
// Check canvas exists (force-graph creates canvas)
expect(document.querySelector('#graph-container canvas')).not.toBeNull();
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
- Graph data JSON is embedded in page
- Nodes array contains people entries
- Links array contains relationship entries
- Nodes have id, name, and group properties
- Links have source, target, and label properties

**Verification:**
```javascript
// Parse embedded graph data
const graphData = JSON.parse(document.getElementById('graph-data').textContent);
expect(graphData.nodes.length).toBeGreaterThan(0);
expect(graphData.links.length).toBeGreaterThan(0);
expect(graphData.nodes[0]).toHaveProperty('id');
expect(graphData.nodes[0]).toHaveProperty('name');
expect(graphData.links[0]).toHaveProperty('source');
expect(graphData.links[0]).toHaveProperty('target');
```

---

## TC-004-01-003: Right-Click Opens Person Modal

**Related UC:** UC-004-01-02

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Wait for graph to render
3. Identify a node on the canvas
4. Right-click on the node (simulate via JavaScript)
5. Wait for modal to appear

**Expected Results:**
- Context menu is prevented
- Person details modal opens
- Modal shows person name in title
- Modal shows email address
- Modal has "Manage Relationships" button

**Verification:**
```javascript
// Check modal is visible
expect(UIkit.modal('#person-modal').isToggled()).toBe(true);
// Check modal contains person data
const modalBody = document.getElementById('person-modal-body');
expect(modalBody.querySelector('h2')).not.toBeNull();
expect(modalBody.textContent).toContain('@'); // Email contains @
expect(modalBody.querySelector('a[href*="/relationships"]')).not.toBeNull();
```

---

## TC-004-01-004: Modal Shows Correct Person Details

**Related UC:** UC-004-01-02

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Open person details modal (via right-click simulation)
3. Verify displayed information

**Expected Results:**
- Name matches node label
- Email is displayed as mailto link
- Phone is displayed if present
- Date of birth is displayed if present
- Gender is displayed if present

**Manual Verification:**
- Visually confirm data matches the person record

---

## TC-004-01-005: Navigate to Relationship Management

**Related UC:** UC-004-01-03

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Open person details modal
3. Click "Manage Relationships" button
4. Wait for page transition

**Expected Results:**
- Modal closes
- URL changes to `/persons/{id}/relationships`
- Relationship management page loads
- Page shows correct person's relationships

**Verification:**
```javascript
// Check URL pattern
expect(window.location.pathname).toMatch(/\/persons\/\d+\/relationships/);
// Check heading contains "Relationships for"
expect(document.querySelector('h2').textContent).toContain('Relationships');
```

---

## TC-004-01-006: Graph Visual Styling

**Related UC:** UC-004-01-04

**Steps:**
1. Ensure test data includes people with different genders
2. Navigate to `/graph` (authenticated)
3. Observe node colors

**Expected Results:**
- Female nodes are pink (#E91E63)
- Male nodes are blue (#2196F3)
- Unspecified gender nodes are gray (#9E9E9E)
- Nodes with more relationships are larger
- Labels appear when zoomed in

**Manual Verification:**
- Visually confirm color coding
- Zoom in to verify labels appear
- Compare node sizes

---

## TC-004-01-007: Graph Interactivity

**Related UC:** UC-004-01-01

**Steps:**
1. Navigate to `/graph` (authenticated)
2. Drag a node
3. Release the node
4. Scroll to zoom in/out
5. Click and drag background to pan

**Expected Results:**
- Node follows mouse during drag
- Node settles back after release (force simulation)
- Scroll wheel zooms graph in/out
- Background drag pans the view

**Manual Verification:**
- Test each interaction visually

---

## TC-004-01-008: Empty State

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
const container = document.getElementById('graph-container');
expect(container.textContent).toContain('No people found');
expect(container.querySelector('a[href="/persons"]')).not.toBeNull();
```

---

## TC-004-01-009: Authentication Required

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

| Test ID | Description | Priority |
|---------|-------------|----------|
| TC-004-01-001 | Navigate to Graph Page | High |
| TC-004-01-002 | Graph Displays Nodes and Edges | High |
| TC-004-01-003 | Right-Click Opens Person Modal | High |
| TC-004-01-004 | Modal Shows Correct Person Details | High |
| TC-004-01-005 | Navigate to Relationship Management | High |
| TC-004-01-006 | Graph Visual Styling | Medium |
| TC-004-01-007 | Graph Interactivity | Medium |
| TC-004-01-008 | Empty State | Medium |
| TC-004-01-009 | Authentication Required | High |

---

*Document Version: 1.0*
*Last Updated: January 2026*
