# Use Cases: Feature 004 - Network Graph Visualization

This document describes the use cases for the Network Graph Visualization feature, providing an interactive force-directed graph display of people and their relationships.

---

## Related User Stories

| User Story | Description |
|------------|-------------|
| US-004-01 | Display a network diagram of the relationships between people |

---

## UC-004-01-01: Display Network Graph Page

**Parent Story:** US-004-01

**Description:** Display an interactive force-directed graph visualization showing all people as nodes and their relationships as edges.

**Preconditions:**
- User is authenticated

**Main Flow:**
1. User clicks "Graph" in the navigation bar
2. System loads all people and relationships from the database
3. System renders the graph page with force-graph visualization
4. Nodes represent people (labeled with display name)
5. Edges represent relationships (optionally labeled with relationship type)
6. Graph uses force-directed layout algorithm
7. Connected people cluster together naturally
8. User can zoom and pan the graph
9. User can drag nodes to reposition them
10. Nodes settle back into position when released

**Postconditions:**
- Graph is displayed with all people and relationships
- Graph is interactive (drag, zoom, pan)

**Error Handling:**
- If no people exist, display empty state message
- If no relationships exist, display nodes without edges

---

## UC-004-01-02: View Person Details Modal

**Parent Story:** US-004-01

**Description:** Display a modal with person details when user right-clicks on a node.

**Preconditions:**
- User is viewing the network graph
- At least one person node is visible

**Main Flow:**
1. User right-clicks on a person node
2. System prevents default browser context menu
3. System displays a modal with person details:
   - Full name (with title if present)
   - Email address
   - Phone number (if present)
   - Date of birth (if present)
   - Gender (if present)
4. Modal includes a "Manage Relationships" link
5. User can close the modal

**Alternative Flow:**
- User left-clicks node: Node is highlighted/selected (no modal)

**Postconditions:**
- Modal displays person details
- User can navigate to relationship management

---

## UC-004-01-03: Navigate to Relationship Management

**Parent Story:** US-004-01

**Description:** Navigate from the person details modal to the relationship management screen.

**Preconditions:**
- Person details modal is open

**Main Flow:**
1. User clicks "Manage Relationships" link in modal
2. System navigates to `/persons/{personId}/relationships`
3. Relationship management page loads for selected person

**Postconditions:**
- User is on the relationship management page for the selected person

---

## UC-004-01-04: Customize Graph Appearance

**Parent Story:** US-004-01

**Description:** Graph nodes and edges have visual styling based on data.

**Preconditions:**
- Graph is displayed

**Main Flow:**
1. System colors nodes based on gender (if assigned):
   - Female: Pink/Rose color
   - Male: Blue color
   - Not specified/Unknown: Gray color
2. Node size varies based on number of relationships (more connections = larger node)
3. Edges display relationship type on hover
4. Active/hovered node is highlighted

**Postconditions:**
- Graph has meaningful visual encoding

---

## Use Case Summary

| UC ID | Name | Priority |
|-------|------|----------|
| UC-004-01-01 | Display Network Graph Page | High |
| UC-004-01-02 | View Person Details Modal | High |
| UC-004-01-03 | Navigate to Relationship Management | High |
| UC-004-01-04 | Customize Graph Appearance | Medium |

---

*Document Version: 1.0*
*Last Updated: January 2026*
