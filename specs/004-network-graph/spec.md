# Technical Specification: Feature 004 - Network Graph Visualization

This document describes the technical implementation requirements for the Network Graph Visualization feature, which displays an interactive force-directed graph of people and their relationships using D3.js.

---

## 1. Overview

The network graph provides a visual representation of the `Person` and `PersonRelationship` data as an interactive force-directed graph. The implementation uses:

- **D3.js v7** for graph rendering and force simulation
- **d3-context-menu** for right-click context menus
- **JSON-B** (Quarkus default) for JSON serialization
- **HTMX** for modal content loading

---

## 2. CDN Dependencies

### 2.1 Required Libraries

Add to graph template (not base.html to avoid loading on all pages):

```html
<!-- D3.js v7 -->
<script src="https://cdn.jsdelivr.net/npm/d3@7"></script>

<!-- D3 Context Menu Plugin -->
<script src="https://cdn.jsdelivr.net/npm/d3-context-menu@2.1.0/dist/d3-context-menu.min.js"></script>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/d3-context-menu@2.1.0/css/d3-context-menu.min.css">
```

---

## 3. Navigation Update

### 3.1 Add Graph Entry to Navigation

**File**: `src/main/resources/templates/fragments/navigation.html`

Add new navigation item after "People":

```html
<li class="{#if currentPage?? == 'graph'}uk-active{/if}">
    <a href="/graph">
        <span uk-icon="icon: git-fork; ratio: 1.2"></span>
        <span class="uk-margin-small-left">Graph</span>
    </a>
</li>
```

---

## 4. Resource Layer

### 4.1 GraphResource

**File**: `src/main/java/io/archton/scaffold/router/GraphResource.java`

**Security**: `@RolesAllowed({"user", "admin"})`

### 4.2 Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/graph` | `showGraph()` | Display network graph HTML page |
| GET | `/graph/data` | `getGraphData()` | Return formatted JSON for D3 |
| GET | `/graph/person/{id}` | `getPersonDetails()` | Return person details for modal |

### 4.3 GraphResource Implementation

```java
@Path("/graph")
@RolesAllowed({"user", "admin"})
public class GraphResource {

    @Inject
    PersonRepository personRepository;

    @Inject
    PersonRelationshipRepository personRelationshipRepository;

    @Inject
    RelationshipRepository relationshipRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance graph(
            String currentPage, String userName, List<Relationship> relationships);
        public static native TemplateInstance personModal(Person person);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showGraph() {
        List<Relationship> relationships = relationshipRepository.listAll(
            Sort.by("description"));
        return Templates.graph("graph", getCurrentUserName(), relationships);
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public GraphData getGraphData() {
        return buildGraphData();
    }

    @GET
    @Path("/person/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getPersonDetails(@PathParam("id") Long id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Templates.personModal(person);
    }

    private String getCurrentUserName() {
        return securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();
    }

    private GraphData buildGraphData() {
        // Implementation in section 5
    }
}
```

---

## 5. Graph Data Structure

### 5.1 JSON Response Format

The `/graph/data` endpoint returns JSON-B serialized data:

```json
{
  "nodes": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "genderCode": "M",
      "relationshipCount": 3
    },
    {
      "id": 2,
      "firstName": "Jane",
      "lastName": "Doe",
      "email": "jane@example.com",
      "genderCode": "F",
      "relationshipCount": 2
    }
  ],
  "links": [
    {
      "source": 1,
      "target": 2,
      "relationshipType": "Spouse",
      "relationshipCode": "SPOUSE"
    }
  ]
}
```

### 5.2 Data Transfer Objects

**File**: `src/main/java/io/archton/scaffold/router/GraphResource.java` (inner classes)

```java
public static class GraphData {
    public List<GraphNode> nodes;
    public List<GraphLink> links;
}

public static class GraphNode {
    public Long id;
    public String firstName;
    public String lastName;
    public String email;
    public String genderCode;  // "M", "F", or null
    public int relationshipCount;
}

public static class GraphLink {
    public Long source;
    public Long target;
    public String relationshipType;  // description
    public String relationshipCode;  // code
}
```

### 5.3 Building Graph Data

```java
private GraphData buildGraphData() {
    GraphData data = new GraphData();
    data.nodes = new ArrayList<>();
    data.links = new ArrayList<>();

    // Build node map for relationship counting
    Map<Long, GraphNode> nodeMap = new HashMap<>();
    Map<Long, Integer> relationshipCounts = new HashMap<>();

    // Count relationships per person
    List<PersonRelationship> allRelationships = personRelationshipRepository.listAll();
    for (PersonRelationship pr : allRelationships) {
        relationshipCounts.merge(pr.sourcePerson.id, 1, Integer::sum);
        relationshipCounts.merge(pr.relatedPerson.id, 1, Integer::sum);
    }

    // Build nodes
    List<Person> persons = personRepository.listAll();
    for (Person p : persons) {
        GraphNode node = new GraphNode();
        node.id = p.id;
        node.firstName = p.firstName;
        node.lastName = p.lastName;
        node.email = p.email;
        node.genderCode = p.gender != null ? p.gender.code : null;
        node.relationshipCount = relationshipCounts.getOrDefault(p.id, 0);
        data.nodes.add(node);
        nodeMap.put(p.id, node);
    }

    // Build links (avoiding duplicates for bidirectional relationships)
    Set<String> processedLinks = new HashSet<>();
    for (PersonRelationship pr : allRelationships) {
        // Create normalized key to avoid duplicate edges
        Long minId = Math.min(pr.sourcePerson.id, pr.relatedPerson.id);
        Long maxId = Math.max(pr.sourcePerson.id, pr.relatedPerson.id);
        String key = minId + "-" + maxId + "-" + pr.relationship.id;

        if (!processedLinks.contains(key)) {
            GraphLink link = new GraphLink();
            link.source = pr.sourcePerson.id;
            link.target = pr.relatedPerson.id;
            link.relationshipType = pr.relationship.description;
            link.relationshipCode = pr.relationship.code;
            data.links.add(link);
            processedLinks.add(key);
        }
    }

    return data;
}
```

---

## 6. Template Structure

### 6.1 Graph Page Template

**File**: `src/main/resources/templates/GraphResource/graph.html`

```html
{#include base.html}
{#title}Network Graph{/title}

<!-- D3.js and Context Menu loaded only on this page -->
<script src="https://cdn.jsdelivr.net/npm/d3@7"></script>
<script src="https://cdn.jsdelivr.net/npm/d3-context-menu@2.1.0/dist/d3-context-menu.min.js"></script>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/d3-context-menu@2.1.0/css/d3-context-menu.min.css">

<div class="uk-flex uk-flex-between uk-flex-middle uk-margin-bottom">
    <h1 class="uk-heading-small uk-margin-remove">Network Graph</h1>
</div>

<!-- Controls -->
<div class="uk-card uk-card-default uk-card-small uk-card-body uk-margin-bottom">
    <div class="uk-grid-small uk-flex-middle" uk-grid>
        <div class="uk-width-1-4@m">
            <input type="text" id="graph-search" class="uk-input uk-form-small"
                   placeholder="Search by name...">
        </div>
        <div class="uk-width-1-4@m">
            <select id="relationship-filter" class="uk-select uk-form-small">
                <option value="">All Relationships</option>
                {#for rel in relationships}
                <option value="{rel.code}">{rel.description}</option>
                {/for}
            </select>
        </div>
        <div class="uk-width-expand@m uk-text-right">
            <button id="reset-graph" class="uk-button uk-button-default uk-button-small">
                <span uk-icon="icon: refresh; ratio: 0.8"></span> Reset View
            </button>
        </div>
    </div>
</div>

<!-- Graph Container -->
<div id="graph-container" class="uk-card uk-card-default uk-card-body"
     style="height: 600px; padding: 0; overflow: hidden;">
    <div id="graph-empty" class="uk-flex uk-flex-center uk-flex-middle" style="height: 100%; display: none;">
        <div class="uk-text-center uk-text-muted">
            <span uk-icon="icon: users; ratio: 3"></span>
            <p class="uk-margin-small-top">No people to display</p>
        </div>
    </div>
    <svg id="graph-svg" style="width: 100%; height: 100%;"></svg>
</div>

<!-- Legend -->
<div class="uk-margin-top">
    <div class="uk-flex uk-flex-wrap uk-flex-middle" uk-grid>
        <div class="uk-flex uk-flex-middle uk-margin-small-right">
            <span style="display: inline-block; width: 16px; height: 16px; border-radius: 50%; background: #FF69B4; margin-right: 4px;"></span>
            <span class="uk-text-small">Female</span>
        </div>
        <div class="uk-flex uk-flex-middle uk-margin-small-right">
            <span style="display: inline-block; width: 16px; height: 16px; border-radius: 50%; background: #4169E1; margin-right: 4px;"></span>
            <span class="uk-text-small">Male</span>
        </div>
        <div class="uk-flex uk-flex-middle">
            <span style="display: inline-block; width: 16px; height: 16px; border-radius: 50%; background: #808080; margin-right: 4px;"></span>
            <span class="uk-text-small">Not Specified</span>
        </div>
    </div>
</div>

<!-- Person Details Modal -->
<div id="person-modal" uk-modal>
    <div class="uk-modal-dialog">
        <button class="uk-modal-close-default" type="button" uk-close></button>
        <div class="uk-modal-header">
            <h2 class="uk-modal-title">Person Details</h2>
        </div>
        <div id="person-modal-content" class="uk-modal-body">
            <!-- Loaded via HTMX -->
        </div>
    </div>
</div>

<script src="/js/graph.js"></script>
{/}
```

### 6.2 Person Modal Template

**File**: `src/main/resources/templates/GraphResource/personModal.html`

```html
<dl class="uk-description-list">
    <dt>Name</dt>
    <dd>{person.getDisplayName()}</dd>

    <dt>Email</dt>
    <dd>{person.email}</dd>

    {#if person.phone}
    <dt>Phone</dt>
    <dd>{person.phone}</dd>
    {/if}

    {#if person.dateOfBirth}
    <dt>Date of Birth</dt>
    <dd>{person.dateOfBirth}</dd>
    {/if}

    {#if person.gender}
    <dt>Gender</dt>
    <dd>{person.gender.description}</dd>
    {/if}
</dl>
```

---

## 7. D3.js Force-Directed Graph Implementation

### 7.1 JavaScript File

**File**: `src/main/resources/META-INF/resources/js/graph.js`

```javascript
(function() {
    'use strict';

    // Configuration
    const CONFIG = {
        nodeRadius: { min: 8, max: 25 },
        colors: {
            female: '#FF69B4',  // Hot Pink
            male: '#4169E1',    // Royal Blue
            unspecified: '#808080'  // Gray
        },
        forces: {
            charge: -300,
            linkDistance: 100,
            collideRadius: 30,
            centerStrength: 0.05
        },
        animation: {
            alphaTarget: 0.3,
            alphaMin: 0.001,
            velocityDecay: 0.4
        }
    };

    // State
    let simulation;
    let svg, g, link, node, labels;
    let zoom;
    let graphData = { nodes: [], links: [] };

    // Initialize graph
    async function init() {
        const container = document.getElementById('graph-container');
        const svgElement = document.getElementById('graph-svg');
        const width = container.clientWidth;
        const height = container.clientHeight;

        // Setup SVG with zoom
        svg = d3.select('#graph-svg');
        g = svg.append('g');

        zoom = d3.zoom()
            .scaleExtent([0.1, 4])
            .on('zoom', (event) => {
                g.attr('transform', event.transform);
            });

        svg.call(zoom);

        // Fetch data
        try {
            const response = await fetch('/graph/data');
            graphData = await response.json();

            if (graphData.nodes.length === 0) {
                document.getElementById('graph-empty').style.display = 'flex';
                document.getElementById('graph-svg').style.display = 'none';
                return;
            }

            initSimulation(width, height);
            renderGraph();
            setupEventHandlers();
        } catch (error) {
            console.error('Failed to load graph data:', error);
        }
    }

    // Initialize force simulation
    function initSimulation(width, height) {
        simulation = d3.forceSimulation(graphData.nodes)
            .force('link', d3.forceLink(graphData.links)
                .id(d => d.id)
                .distance(CONFIG.forces.linkDistance))
            .force('charge', d3.forceManyBody()
                .strength(CONFIG.forces.charge))
            .force('center', d3.forceCenter(width / 2, height / 2)
                .strength(CONFIG.forces.centerStrength))
            .force('collide', d3.forceCollide()
                .radius(d => getNodeRadius(d) + 5))
            .alphaMin(CONFIG.animation.alphaMin)
            .velocityDecay(CONFIG.animation.velocityDecay)
            .on('tick', ticked);
    }

    // Render graph elements
    function renderGraph() {
        // Links (edges)
        link = g.append('g')
            .attr('class', 'links')
            .selectAll('line')
            .data(graphData.links)
            .join('line')
            .attr('stroke', '#999')
            .attr('stroke-opacity', 0.6)
            .attr('stroke-width', 2);

        // Link tooltips
        link.append('title')
            .text(d => d.relationshipType);

        // Nodes
        node = g.append('g')
            .attr('class', 'nodes')
            .selectAll('circle')
            .data(graphData.nodes)
            .join('circle')
            .attr('r', d => getNodeRadius(d))
            .attr('fill', d => getNodeColor(d))
            .attr('stroke', '#fff')
            .attr('stroke-width', 2)
            .call(drag(simulation))
            .on('contextmenu', d3.contextMenu(getContextMenu))
            .on('click', handleNodeClick)
            .on('mouseover', handleNodeHover)
            .on('mouseout', handleNodeUnhover);

        // Node tooltips
        node.append('title')
            .text(d => `${d.firstName} ${d.lastName}\n${d.email}`);

        // Labels
        labels = g.append('g')
            .attr('class', 'labels')
            .selectAll('text')
            .data(graphData.nodes)
            .join('text')
            .text(d => `${d.firstName} ${d.lastName}`)
            .attr('font-size', 10)
            .attr('dx', d => getNodeRadius(d) + 5)
            .attr('dy', 4);
    }

    // Update positions on tick
    function ticked() {
        link
            .attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y);

        node
            .attr('cx', d => d.x)
            .attr('cy', d => d.y);

        labels
            .attr('x', d => d.x)
            .attr('y', d => d.y);
    }

    // Drag behavior with spring-back
    function drag(simulation) {
        function dragstarted(event, d) {
            if (!event.active) simulation.alphaTarget(CONFIG.animation.alphaTarget).restart();
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(event, d) {
            d.fx = event.x;
            d.fy = event.y;
        }

        function dragended(event, d) {
            if (!event.active) simulation.alphaTarget(0);
            // Spring back: unfix position
            d.fx = null;
            d.fy = null;
        }

        return d3.drag()
            .on('start', dragstarted)
            .on('drag', dragged)
            .on('end', dragended);
    }

    // Get node color based on gender
    function getNodeColor(d) {
        switch (d.genderCode) {
            case 'F': return CONFIG.colors.female;
            case 'M': return CONFIG.colors.male;
            default: return CONFIG.colors.unspecified;
        }
    }

    // Get node radius based on relationship count
    function getNodeRadius(d) {
        const { min, max } = CONFIG.nodeRadius;
        const maxConnections = Math.max(...graphData.nodes.map(n => n.relationshipCount), 1);
        return min + (d.relationshipCount / maxConnections) * (max - min);
    }

    // Context menu definition
    function getContextMenu(d) {
        return [
            {
                title: 'View Details',
                action: function(d, event) {
                    loadPersonModal(d.id);
                }
            },
            { divider: true },
            {
                title: 'Manage Relationships',
                action: function(d, event) {
                    window.location.href = `/persons/${d.id}/relationships`;
                }
            }
        ];
    }

    // Load person details into modal
    function loadPersonModal(personId) {
        fetch(`/graph/person/${personId}`)
            .then(response => response.text())
            .then(html => {
                document.getElementById('person-modal-content').innerHTML = html;
                UIkit.modal('#person-modal').show();
            });
    }

    // Handle node click - neighborhood highlighting
    function handleNodeClick(event, d) {
        event.stopPropagation();

        // Get connected node IDs
        const connectedIds = new Set([d.id]);
        graphData.links.forEach(l => {
            if (l.source.id === d.id) connectedIds.add(l.target.id);
            if (l.target.id === d.id) connectedIds.add(l.source.id);
        });

        // Highlight nodes
        node.attr('opacity', n => connectedIds.has(n.id) ? 1 : 0.2);

        // Highlight links
        link.attr('opacity', l =>
            (l.source.id === d.id || l.target.id === d.id) ? 1 : 0.1
        );

        // Highlight labels
        labels.attr('opacity', n => connectedIds.has(n.id) ? 1 : 0.2);
    }

    // Handle node hover
    function handleNodeHover(event, d) {
        d3.select(this)
            .attr('stroke-width', 4)
            .attr('stroke', '#333');
    }

    function handleNodeUnhover(event, d) {
        d3.select(this)
            .attr('stroke-width', 2)
            .attr('stroke', '#fff');
    }

    // Setup event handlers
    function setupEventHandlers() {
        // Click on background to reset highlight
        svg.on('click', () => {
            node.attr('opacity', 1);
            link.attr('opacity', 0.6);
            labels.attr('opacity', 1);
        });

        // Search
        const searchInput = document.getElementById('graph-search');
        let searchTimeout;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                filterBySearch(e.target.value);
            }, 300);
        });

        // Relationship filter
        document.getElementById('relationship-filter').addEventListener('change', (e) => {
            filterByRelationship(e.target.value);
        });

        // Reset view
        document.getElementById('reset-graph').addEventListener('click', resetView);
    }

    // Filter nodes by search term
    function filterBySearch(term) {
        const searchTerm = term.toLowerCase().trim();

        if (!searchTerm) {
            node.attr('opacity', 1);
            link.attr('opacity', 0.6);
            labels.attr('opacity', 1);
            return;
        }

        const matchingIds = new Set(
            graphData.nodes
                .filter(n =>
                    n.firstName.toLowerCase().includes(searchTerm) ||
                    n.lastName.toLowerCase().includes(searchTerm))
                .map(n => n.id)
        );

        node.attr('opacity', n => matchingIds.has(n.id) ? 1 : 0.2);
        labels.attr('opacity', n => matchingIds.has(n.id) ? 1 : 0.2);
    }

    // Filter by relationship type
    function filterByRelationship(code) {
        if (!code) {
            node.attr('opacity', 1);
            link.attr('opacity', 0.6);
            labels.attr('opacity', 1);
            return;
        }

        const matchingLinks = graphData.links.filter(l => l.relationshipCode === code);
        const connectedIds = new Set();
        matchingLinks.forEach(l => {
            connectedIds.add(l.source.id);
            connectedIds.add(l.target.id);
        });

        node.attr('opacity', n => connectedIds.has(n.id) ? 1 : 0.2);
        link.attr('opacity', l => l.relationshipCode === code ? 0.8 : 0.1);
        labels.attr('opacity', n => connectedIds.has(n.id) ? 1 : 0.2);
    }

    // Reset view to initial state
    function resetView() {
        // Reset zoom
        svg.transition().duration(500).call(
            zoom.transform,
            d3.zoomIdentity
        );

        // Reset filters
        document.getElementById('graph-search').value = '';
        document.getElementById('relationship-filter').value = '';

        // Reset opacity
        node.attr('opacity', 1);
        link.attr('opacity', 0.6);
        labels.attr('opacity', 1);

        // Reheat simulation
        simulation.alpha(0.3).restart();
    }

    // Initialize on DOM ready
    document.addEventListener('DOMContentLoaded', init);
})();
```

---

## 8. CSS Styles

### 8.1 Graph-Specific Styles

**File**: `src/main/resources/META-INF/resources/style.css` (additions)

```css
/* Graph Styles */
#graph-svg {
    cursor: grab;
}

#graph-svg:active {
    cursor: grabbing;
}

.nodes circle {
    cursor: pointer;
    transition: stroke-width 0.2s;
}

.links line {
    pointer-events: all;
}

.labels text {
    pointer-events: none;
    fill: #333;
    user-select: none;
}

/* Context menu theme matching UIkit */
.d3-context-menu-theme {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    border-radius: 4px;
    box-shadow: 0 5px 12px rgba(0,0,0,0.15);
}
```

---

## 9. Security Configuration

### 9.1 Route Protection

**File**: `src/main/resources/application.properties` (additions)

```properties
# Graph routes require authentication
quarkus.http.auth.permission.graph.paths=/graph,/graph/*
quarkus.http.auth.permission.graph.policy=authenticated
```

---

## 10. Traceability

| Use Case | Implementation Component |
|----------|-------------------------|
| UC-004-01-01 | GraphResource.showGraph(), graph.html, graph.js (init, renderGraph) |
| UC-004-01-02 | graph.js (drag, handleNodeClick, handleNodeHover) |
| UC-004-01-03 | graph.js (getContextMenu, loadPersonModal), personModal.html |
| UC-004-01-04 | graph.js (filterBySearch, filterByRelationship) |
| UC-004-01-05 | graph.js (zoom behavior, resetView) |
| UC-004-01-06 | graph.js (link tooltips) |

---

## 11. Dependencies

### 11.1 Existing Entities (Feature 003)
- `Person` - Node data source
- `PersonRelationship` - Edge data source
- `Relationship` - Edge label/type
- `Gender` - Node color determination

### 11.2 Existing Repositories
- `PersonRepository`
- `PersonRelationshipRepository`
- `RelationshipRepository`

### 11.3 CDN Libraries
- D3.js v7 (https://d3js.org/)
- d3-context-menu v2.1.0

---

## 12. Performance Considerations

### 12.1 Data Optimization
- Graph data endpoint returns minimal fields needed for visualization
- Relationship counting done server-side
- Duplicate edge prevention for bidirectional relationships

### 12.2 Rendering Optimization
- SVG-based rendering (better for <1000 nodes)
- Force simulation uses velocity decay to stabilize quickly
- Debounced search input (300ms)

### 12.3 Scalability Notes
- For very large datasets (>1000 nodes), consider:
  - Canvas rendering instead of SVG
  - Server-side clustering
  - Pagination or virtual scrolling

---

*Document Version: 1.0*
*Last Updated: 2026-01-02*
