# Use Cases: Feature 004 - Network Graph Visualization

This document describes the use cases for the Network Graph Visualization feature, providing an interactive graph display of people and their relationships using Cytoscape.js.

---

## Related User Stories

| User Story | Description |
|------------|-------------|
| US-004-01 | Display a network diagram of the relationships between people |
| US-004-02 | Export graph view as an image |

---

## UC-004-01-01: Display Network Graph Page

**Parent Story:** US-004-01

**Description:** Display an interactive graph visualization showing all people as nodes and their relationships as edges using Cytoscape.js with force-directed layout.

**Preconditions:**
- User is authenticated

**Main Flow:**
1. User clicks "Graph" in the navigation bar
2. System loads graph page with loading indicator
3. System fetches graph data (nodes and edges) from JSON API
4. System initializes Cytoscape.js with force-directed (CoSE) layout
5. Nodes represent people (labeled with display name)
6. Edges represent relationships (labeled with relationship type)
7. Connected people cluster together naturally due to force simulation
8. System displays minimap showing viewport position
9. User can interact with the graph (see UC-004-01-02)

**Postconditions:**
- Graph is displayed with all people and relationships
- Graph is interactive and responsive
- Minimap shows current viewport

**Error Handling:**
- If no people exist, display empty state message with link to add people
- If no relationships exist, display nodes without edges
- If data fetch fails, display error message with retry option

---

## UC-004-01-02: Graph Navigation and Interaction

**Parent Story:** US-004-01

**Description:** User can navigate and interact with the graph using standard gestures.

**Preconditions:**
- Graph is displayed with nodes and edges

**Main Flow:**
1. **Zoom:** User scrolls mouse wheel to zoom in/out
2. **Pan:** User clicks and drags on background to pan the view
3. **Drag Node:** User clicks and drags a node to reposition it
4. **Release Node:** When released, node settles back due to force simulation
5. **Select Node:** User clicks a node to select it
6. **Neighborhood Highlight:** Selected node and all connected nodes/edges are highlighted
7. **Deselect:** User clicks on background to deselect

**Alternative Flows:**
- **Touch Devices:** Pinch-to-zoom, touch-drag to pan
- **Keyboard:** Arrow keys for pan, +/- for zoom

**Postconditions:**
- Graph reflects user's navigation state
- Selection state is visually indicated

---

## UC-004-01-03: View Node Tooltip

**Parent Story:** US-004-01

**Description:** Display a tooltip with person summary when hovering over a node.

**Preconditions:**
- Graph is displayed
- At least one node is visible

**Main Flow:**
1. User hovers mouse over a person node
2. System displays tooltip after brief delay (300ms)
3. Tooltip shows:
   - Full name (with title if present)
   - Email address
4. User moves mouse away
5. Tooltip disappears

**Postconditions:**
- Tooltip provides quick person identification
- No modal or navigation occurs

---

## UC-004-01-04: View Edge Tooltip

**Parent Story:** US-004-01

**Description:** Display a tooltip with relationship type when hovering over an edge.

**Preconditions:**
- Graph is displayed
- At least one edge is visible

**Main Flow:**
1. User hovers mouse over a relationship edge
2. System displays tooltip showing relationship type (e.g., "Spouse", "Colleague")
3. User moves mouse away
4. Tooltip disappears

**Postconditions:**
- User understands the nature of the relationship

---

## UC-004-01-05: Node Context Menu

**Parent Story:** US-004-01

**Description:** Display a context menu with actions when user right-clicks on a node.

**Preconditions:**
- Graph is displayed
- At least one node is visible

**Main Flow:**
1. User right-clicks on a person node
2. System prevents default browser context menu
3. System displays custom context menu with options:
   - "View Details" - Opens person details modal
   - "Manage Relationships" - Navigates to relationship page
4. User clicks a menu option
5. System executes the selected action
6. Context menu closes

**Alternative Flow:**
- User clicks elsewhere to dismiss menu without action

**Postconditions:**
- Selected action is executed
- Context menu is dismissed

---

## UC-004-01-06: View Person Details Modal

**Parent Story:** US-004-01

**Description:** Display a modal with full person details from the context menu.

**Preconditions:**
- Context menu is displayed on a node

**Main Flow:**
1. User selects "View Details" from context menu
2. System displays modal with person details:
   - Full name (with title if present)
   - Email address (as mailto link)
   - Phone number (if present)
   - Date of birth (if present)
   - Gender (if present)
   - Notes/biography (if present)
3. Modal includes "Manage Relationships" button
4. User can close modal via X button or clicking outside

**Postconditions:**
- Modal displays complete person information
- User can navigate to relationship management from modal

---

## UC-004-01-07: Navigate to Relationship Management

**Parent Story:** US-004-01

**Description:** Navigate directly to relationship management for a person.

**Preconditions:**
- Context menu is displayed OR person details modal is open

**Main Flow (from Context Menu):**
1. User selects "Manage Relationships" from context menu
2. System navigates to `/persons/{personId}/relationships`

**Main Flow (from Modal):**
1. User clicks "Manage Relationships" button in modal
2. System closes modal
3. System navigates to `/persons/{personId}/relationships`

**Postconditions:**
- User is on the relationship management page for the selected person

---

## UC-004-01-08: Search and Highlight Nodes

**Parent Story:** US-004-01

**Description:** Search for people by name and highlight matching nodes.

**Preconditions:**
- Graph is displayed

**Main Flow:**
1. User types a name in the search input box
2. System filters nodes as user types (debounced)
3. Matching nodes are highlighted with distinct styling
4. Non-matching nodes are dimmed/faded
5. If single match, graph centers on that node
6. User clears search input
7. All nodes return to normal styling

**Alternative Flows:**
- **No Matches:** Display "No matches found" message
- **Empty Search:** All nodes displayed normally

**Postconditions:**
- Matching nodes are visually distinguished
- User can quickly locate specific people

---

## UC-004-01-09: Filter by Relationship Type

**Parent Story:** US-004-01

**Description:** Filter the graph to show only edges of a specific relationship type.

**Preconditions:**
- Graph is displayed with edges

**Main Flow:**
1. User selects a relationship type from the filter dropdown
2. System hides edges that don't match the selected type
3. Orphaned nodes (with no visible edges) are dimmed
4. User selects "All Relationships" option
5. All edges are displayed again

**Postconditions:**
- Only selected relationship types are visible
- Graph simplifies to show focused relationships

---

## UC-004-01-10: Switch Layout Algorithm

**Parent Story:** US-004-01

**Description:** Change the graph layout algorithm to reorganize nodes.

**Preconditions:**
- Graph is displayed

**Main Flow:**
1. User clicks a layout button in the toolbar
2. Available layouts:
   - **Force (CoSE):** Physics-based clustering (default)
   - **Circular:** Nodes arranged in a circle
   - **Grid:** Nodes arranged in a grid pattern
3. System animates transition to new layout
4. Graph settles into new arrangement

**Postconditions:**
- Nodes are rearranged according to selected layout
- User can compare different organizational views

---

## UC-004-01-11: Customize Graph Appearance

**Parent Story:** US-004-01

**Description:** Graph nodes and edges have visual styling based on data attributes.

**Preconditions:**
- Graph is displayed

**Main Flow:**
1. System colors nodes based on gender:
   - Female: Pink (#E91E63)
   - Male: Blue (#2196F3)
   - Not specified/Unknown: Gray (#9E9E9E)
2. Node size varies based on number of relationships (logarithmic scale)
3. Edges display relationship type as label
4. Selected/hovered elements have distinct highlight styling
5. Labels appear at appropriate zoom levels

**Postconditions:**
- Graph has meaningful visual encoding
- Users can identify patterns at a glance

---

## UC-004-02-01: Export Graph as Image

**Parent Story:** US-004-02

**Description:** Export the current graph view as a PNG image.

**Preconditions:**
- Graph is displayed

**Main Flow:**
1. User clicks "Export" button in toolbar
2. System captures current viewport as PNG
3. System triggers browser download
4. File is named `relationship-graph-YYYY-MM-DD.png`

**Postconditions:**
- PNG file is downloaded to user's device
- Image reflects current graph state (zoom, positions, selections)

---

## Use Case Summary

| UC ID | Name | Priority | Story |
|-------|------|----------|-------|
| UC-004-01-01 | Display Network Graph Page | High | US-004-01 |
| UC-004-01-02 | Graph Navigation and Interaction | High | US-004-01 |
| UC-004-01-03 | View Node Tooltip | High | US-004-01 |
| UC-004-01-04 | View Edge Tooltip | Medium | US-004-01 |
| UC-004-01-05 | Node Context Menu | High | US-004-01 |
| UC-004-01-06 | View Person Details Modal | High | US-004-01 |
| UC-004-01-07 | Navigate to Relationship Management | High | US-004-01 |
| UC-004-01-08 | Search and Highlight Nodes | Medium | US-004-01 |
| UC-004-01-09 | Filter by Relationship Type | Medium | US-004-01 |
| UC-004-01-10 | Switch Layout Algorithm | Low | US-004-01 |
| UC-004-01-11 | Customize Graph Appearance | Medium | US-004-01 |
| UC-004-02-01 | Export Graph as Image | Low | US-004-02 |

---

*Document Version: 2.0*
*Last Updated: January 2026*
*Technology: Cytoscape.js*
