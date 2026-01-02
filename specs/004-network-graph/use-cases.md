# Use Cases for Feature 004: Network Graph Visualization

This feature provides an interactive force-directed network graph visualization of people and their relationships, using D3.js for rendering.

## Actors

| Actor | Description |
|-------|-------------|
| User | Authenticated user with "user" role |

---

# US-004-01: Display a network diagram of the relationships between people

## UC-004-01-01: Display Network Graph Page

| Attribute | Value |
|-----------|-------|
| Actor | User |
| Precondition | User is authenticated; Person and PersonRelationship data exists |
| Trigger | User clicks "Graph" in the navigation bar |

**Main Flow:**
1. User navigates to the Graph page via navbar
2. System displays the graph page with an empty SVG container
3. System fetches graph data from `/graph/data` endpoint
4. System renders nodes (circles) representing people
5. System renders edges (lines) representing relationships
6. System applies force-directed layout algorithm
7. Graph animates to equilibrium position

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 4a | No people in database | Display empty state message "No people to display" |
| 4b | No relationships exist | Display nodes without edges |

**Postcondition:** Interactive network graph is displayed showing all people and their relationships

---

## UC-004-01-02: Interact with Graph Nodes

| Attribute | Value |
|-----------|-------|
| Actor | User |
| Precondition | Network graph is displayed |
| Trigger | User interacts with a node (drag, hover, click) |

**Main Flow (Drag):**
1. User clicks and holds a node
2. Node becomes "fixed" at cursor position
3. User drags node to new position
4. Other nodes and edges adjust via force simulation
5. User releases node
6. Node becomes "unfixed" and springs back toward equilibrium
7. Force simulation continues until stable

**Main Flow (Hover):**
1. User hovers mouse over a node
2. System displays tooltip with person's name and email
3. User moves mouse away
4. Tooltip disappears

**Main Flow (Click):**
1. User clicks on a node
2. System highlights the selected node
3. System highlights all connected nodes (neighbors)
4. System highlights all edges connected to selected node
5. Non-connected nodes and edges are dimmed

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 5a | User clicks elsewhere | Remove highlight, restore all nodes/edges |

**Postcondition:** Node interaction feedback is provided

---

## UC-004-01-03: Access Context Menu Actions

| Attribute | Value |
|-----------|-------|
| Actor | User |
| Precondition | Network graph is displayed |
| Trigger | User right-clicks on a node |

**Main Flow (View Details):**
1. User right-clicks on a node
2. System displays context menu with options
3. User selects "View Details"
4. System displays modal with person's full details (name, email, phone, date of birth, gender, title)
5. User closes modal

**Main Flow (Manage Relationships):**
1. User right-clicks on a node
2. System displays context menu with options
3. User selects "Manage Relationships"
4. System navigates to `/persons/{id}/relationships` page

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | Right-click on empty space | No context menu displayed |

**Postcondition:** User can access detailed information or relationship management for any person

---

## UC-004-01-04: Search and Filter Graph

| Attribute | Value |
|-----------|-------|
| Actor | User |
| Precondition | Network graph is displayed |
| Trigger | User enters search term or selects filter |

**Main Flow (Search):**
1. User types name in search box
2. System filters nodes as user types (debounced)
3. Matching nodes are highlighted
4. Non-matching nodes are dimmed
5. User clears search
6. All nodes restored to normal state

**Main Flow (Filter by Relationship):**
1. User selects relationship type from dropdown
2. System filters edges to show only selected relationship type
3. Nodes without matching relationships are dimmed
4. User selects "All" or clears filter
5. All edges and nodes restored

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 3a | No matches found | All nodes dimmed, message displayed |

**Postcondition:** Graph displays filtered subset of data

---

## UC-004-01-05: Navigate Graph View

| Attribute | Value |
|-----------|-------|
| Actor | User |
| Precondition | Network graph is displayed |
| Trigger | User scrolls or drags background |

**Main Flow (Zoom):**
1. User scrolls mouse wheel over graph
2. System zooms in/out centered on cursor position
3. Nodes and edges scale appropriately

**Main Flow (Pan):**
1. User clicks and drags on empty graph area
2. System pans the viewport in drag direction
3. User releases mouse
4. New viewport position is maintained

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Touch device | Pinch to zoom, drag to pan |

**Postcondition:** User can navigate to any part of the graph at desired zoom level

---

## UC-004-01-06: View Edge Tooltip

| Attribute | Value |
|-----------|-------|
| Actor | User |
| Precondition | Network graph is displayed with relationships |
| Trigger | User hovers over an edge |

**Main Flow:**
1. User hovers mouse over an edge (relationship line)
2. System displays tooltip showing relationship type (e.g., "Spouse", "Parent")
3. User moves mouse away
4. Tooltip disappears

**Postcondition:** User understands the relationship type between connected people

---

## Use Case Summary

| ID | Use Case | Parent Story | Actor(s) |
|----|----------|--------------|----------|
| UC-004-01-01 | Display Network Graph Page | US-004-01 | User |
| UC-004-01-02 | Interact with Graph Nodes | US-004-01 | User |
| UC-004-01-03 | Access Context Menu Actions | US-004-01 | User |
| UC-004-01-04 | Search and Filter Graph | US-004-01 | User |
| UC-004-01-05 | Navigate Graph View | US-004-01 | User |
| UC-004-01-06 | View Edge Tooltip | US-004-01 | User |

---

## Traceability Matrix

| User Story | Use Cases |
|------------|-----------|
| US-004-01: Display a network diagram | UC-004-01-01, UC-004-01-02, UC-004-01-03, UC-004-01-04, UC-004-01-05, UC-004-01-06 |
