# Technical Specification: Feature 004 - Network Graph Visualization

This document describes the technical implementation for the Network Graph Visualization feature using **Cytoscape.js** - a fully featured graph theory library with rich interactivity, context menus, tooltips, and multiple layout algorithms.

---

## 1. Technology Selection

### 1.1 Why Cytoscape.js?

After evaluating multiple JavaScript graph visualization libraries, **Cytoscape.js** was selected for this HTMX/Qute project because:

| Criterion | Cytoscape.js | force-graph (previous) |
|-----------|--------------|------------------------|
| **CDN availability** | ✅ Multiple CDNs (cdnjs, jsDelivr) | ✅ Single CDN |
| **Context menus** | ✅ Extension available | ❌ Manual implementation |
| **Tooltips** | ✅ Popper/Tippy extension | ❌ Manual implementation |
| **Multiple layouts** | ✅ 10+ built-in (CoSE, circle, grid, etc.) | ❌ Force-directed only |
| **Search/filter** | ✅ Native selector API | ❌ Manual implementation |
| **Graph algorithms** | ✅ Dijkstra, PageRank, etc. | ❌ None |
| **Performance** | ✅ Up to 100k nodes | ⚠️ Up to 5k nodes |
| **License** | MIT | MIT |
| **HTMX integration** | ✅ Vanilla JS, no framework required | ✅ Vanilla JS |

### 1.2 CDN Includes

```html
<!-- Cytoscape.js Core -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.30.4/cytoscape.min.js"></script>

<!-- Cytoscape Extensions -->
<!-- Context Menus -->
<script src="https://cdn.jsdelivr.net/npm/cytoscape-context-menus@4.1.0/cytoscape-context-menus.min.js"></script>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/cytoscape-context-menus@4.1.0/cytoscape-context-menus.min.css">

<!-- Popper.js for Tooltips -->
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/cytoscape-popper@2.0.0/cytoscape-popper.min.js"></script>

<!-- Tippy.js for Tooltip Styling -->
<script src="https://cdn.jsdelivr.net/npm/tippy.js@6.3.7/dist/tippy-bundle.umd.min.js"></script>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/tippy.js@6.3.7/dist/tippy.css">
```

### 1.3 Bundle Sizes

| Library | Size (minified) |
|---------|-----------------|
| Cytoscape.js | ~540 KB |
| cytoscape-context-menus | ~15 KB |
| cytoscape-popper | ~5 KB |
| Popper.js | ~7 KB |
| Tippy.js | ~30 KB |
| **Total** | ~600 KB |

---

## 2. Data Model

### 2.1 Graph Data Structure

Cytoscape.js expects JSON in this format (different from force-graph):

```json
{
  "nodes": [
    {
      "data": {
        "id": "1",
        "name": "John Doe",
        "email": "john@example.com",
        "phone": "555-1234",
        "dateOfBirth": "1980-05-15",
        "gender": "Male",
        "genderCode": "M",
        "notes": "Biography text...",
        "weight": 3
      }
    }
  ],
  "edges": [
    {
      "data": {
        "id": "e1-2",
        "source": "1",
        "target": "2",
        "label": "Spouse"
      }
    }
  ]
}
```

**Key Differences from force-graph:**
- Properties wrapped in `data` object
- Edges require explicit `id` field
- Node size controlled by `weight` property

### 2.2 GraphNode DTO

**File**: `src/main/java/io/archton/scaffold/dto/GraphNode.java`

```java
package io.archton.scaffold.dto;

import io.archton.scaffold.entity.Person;

/**
 * DTO for Cytoscape.js node data.
 * Wrapped in { data: {...} } structure by GraphData.
 */
public record GraphNode(
    String id,
    String name,
    String email,
    String phone,
    String dateOfBirth,
    String gender,
    String genderCode,  // M, F, X - for coloring
    String notes,
    int weight          // Relationship count - for sizing
) {
    public static GraphNode from(Person p, int relationshipCount) {
        return new GraphNode(
            String.valueOf(p.id),
            p.getDisplayName(),
            p.email,
            p.phone,
            p.dateOfBirth != null ? p.dateOfBirth.toString() : null,
            p.gender != null ? p.gender.description : null,
            p.gender != null ? p.gender.code : "X",
            p.notes,
            Math.max(1, relationshipCount)
        );
    }
}
```

### 2.3 GraphEdge DTO

**File**: `src/main/java/io/archton/scaffold/dto/GraphEdge.java`

```java
package io.archton.scaffold.dto;

import io.archton.scaffold.entity.PersonRelationship;

/**
 * DTO for Cytoscape.js edge data.
 * Wrapped in { data: {...} } structure by GraphData.
 */
public record GraphEdge(
    String id,
    String source,
    String target,
    String label
) {
    public static GraphEdge from(PersonRelationship pr) {
        return new GraphEdge(
            "e" + pr.sourcePerson.id + "-" + pr.relatedPerson.id + "-" + pr.relationship.id,
            String.valueOf(pr.sourcePerson.id),
            String.valueOf(pr.relatedPerson.id),
            pr.relationship.description
        );
    }
}
```

### 2.4 GraphData DTO

**File**: `src/main/java/io/archton/scaffold/dto/GraphData.java`

The DTO structures data in Cytoscape.js format with `data` wrapper:

```java
package io.archton.scaffold.dto;

import java.util.List;

/**
 * Root DTO for Cytoscape.js graph data.
 * Returns { nodes: [{data: {...}}, ...], edges: [{data: {...}}, ...] }
 */
public record GraphData(
    List<CyNode> nodes,
    List<CyEdge> edges,
    List<String> relationshipTypes  // For filter dropdown
) {
    /**
     * Wrapper for Cytoscape node format.
     */
    public record CyNode(GraphNode data) {
        public static CyNode from(GraphNode node) {
            return new CyNode(node);
        }
    }

    /**
     * Wrapper for Cytoscape edge format.
     */
    public record CyEdge(GraphEdge data) {
        public static CyEdge from(GraphEdge edge) {
            return new CyEdge(edge);
        }
    }
}
```

---

## 3. Repository Layer

### 3.1 PersonRepository Updates

The existing `countRelationshipsByPerson()` method is reused:

```java
/**
 * Count relationships where person is the source.
 */
public Map<Long, Integer> countRelationshipsByPerson() {
    List<Object[]> results = getEntityManager()
        .createQuery(
            "SELECT pr.sourcePerson.id, COUNT(pr) FROM PersonRelationship pr GROUP BY pr.sourcePerson.id",
            Object[].class
        )
        .getResultList();

    Map<Long, Integer> counts = new HashMap<>();
    for (Object[] row : results) {
        counts.put((Long) row[0], ((Long) row[1]).intValue());
    }
    return counts;
}
```

### 3.2 PersonRelationshipRepository Updates

The existing Entity Graph is reused for efficient loading:

```java
/**
 * Find all relationships with eager loading for graph visualization.
 * Uses the PersonRelationship.forGraph entity graph.
 */
public List<PersonRelationship> findAllForGraph() {
    EntityGraph<?> graph = getEntityManager().getEntityGraph("PersonRelationship.forGraph");

    return find("SELECT pr FROM PersonRelationship pr")
        .withHint("jakarta.persistence.fetchgraph", graph)
        .list();
}

/**
 * Get distinct relationship types for filter dropdown.
 */
public List<String> findDistinctRelationshipTypes() {
    return getEntityManager()
        .createQuery(
            "SELECT DISTINCT r.description FROM Relationship r ORDER BY r.description",
            String.class
        )
        .getResultList();
}
```

---

## 4. Service Layer

### 4.1 GraphService

**File**: `src/main/java/io/archton/scaffold/service/GraphService.java`

The service builds Cytoscape-formatted graph data:

```java
package io.archton.scaffold.service;

import io.archton.scaffold.dto.GraphData;
import io.archton.scaffold.dto.GraphData.CyEdge;
import io.archton.scaffold.dto.GraphData.CyNode;
import io.archton.scaffold.dto.GraphEdge;
import io.archton.scaffold.dto.GraphNode;
import io.archton.scaffold.entity.Person;
import io.archton.scaffold.entity.PersonRelationship;
import io.archton.scaffold.repository.PersonRelationshipRepository;
import io.archton.scaffold.repository.PersonRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GraphService {

    @Inject
    PersonRepository personRepository;

    @Inject
    PersonRelationshipRepository personRelationshipRepository;

    /**
     * Build Cytoscape.js compatible graph data.
     */
    public GraphData buildGraphData() {
        // Get relationship counts per person for node sizing
        Map<Long, Integer> relationshipCounts = personRepository.countRelationshipsByPerson();

        // Build nodes from all persons
        List<Person> persons = personRepository.listAll();
        List<CyNode> nodes = persons.stream()
            .map(p -> GraphNode.from(p, relationshipCounts.getOrDefault(p.id, 0)))
            .map(CyNode::from)
            .toList();

        // Build edges from all relationships
        List<PersonRelationship> relationships = personRelationshipRepository.findAllForGraph();
        List<CyEdge> edges = relationships.stream()
            .map(GraphEdge::from)
            .map(CyEdge::from)
            .toList();

        // Get relationship types for filter dropdown
        List<String> relationshipTypes = personRelationshipRepository.findDistinctRelationshipTypes();

        return new GraphData(nodes, edges, relationshipTypes);
    }
}
```

---

## 5. Resource Layer

### 5.1 GraphResource

**File**: `src/main/java/io/archton/scaffold/router/GraphResource.java`

```java
package io.archton.scaffold.router;

import io.archton.scaffold.dto.GraphData;
import io.archton.scaffold.service.GraphService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/graph")
@RolesAllowed({"user", "admin"})
public class GraphResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    GraphService graphService;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance graph(
            String title,
            String currentPage,
            String userName
        );
    }

    /**
     * Display the network graph page.
     * Graph data is loaded asynchronously via /graph/data endpoint.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showGraph() {
        String userName = securityIdentity.isAnonymous()
            ? null
            : securityIdentity.getPrincipal().getName();

        return Templates.graph(
            "Relationship Graph",
            "graph",
            userName
        );
    }

    /**
     * Return graph data as JSON in Cytoscape.js format.
     */
    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public GraphData getGraphData() {
        return graphService.buildGraphData();
    }
}
```

### 5.2 Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/graph` | `showGraph()` | Display network graph HTML page |
| GET | `/graph/data` | `getGraphData()` | Return Cytoscape.js formatted JSON |

---

## 6. Template Structure

### 6.1 Template File

**File**: `templates/GraphResource/graph.html`

```html
{@String title}
{@String currentPage}
{@String userName}
{#include base}

<div class="uk-flex uk-flex-between uk-flex-middle uk-margin-bottom">
    <h2 class="uk-heading-small uk-margin-remove">
        <span uk-icon="icon: git-branch; ratio: 1.5"></span>
        Relationship Network
    </h2>

    <!-- Toolbar -->
    <div class="uk-button-group">
        <button id="layout-cose" class="uk-button uk-button-small uk-button-default" title="Force Layout">
            <span uk-icon="move"></span>
        </button>
        <button id="layout-circle" class="uk-button uk-button-small uk-button-default" title="Circular Layout">
            <span uk-icon="refresh"></span>
        </button>
        <button id="layout-grid" class="uk-button uk-button-small uk-button-default" title="Grid Layout">
            <span uk-icon="grid"></span>
        </button>
        <button id="btn-export" class="uk-button uk-button-small uk-button-primary" title="Export PNG">
            <span uk-icon="download"></span> Export
        </button>
    </div>
</div>

<!-- Search and Filter Bar -->
<div class="uk-grid-small uk-margin-bottom" uk-grid>
    <div class="uk-width-1-3@m">
        <div class="uk-inline uk-width-1-1">
            <span class="uk-form-icon" uk-icon="icon: search"></span>
            <input id="graph-search" class="uk-input uk-form-small"
                   type="text" placeholder="Search by name...">
        </div>
    </div>
    <div class="uk-width-1-3@m">
        <select id="relationship-filter" class="uk-select uk-form-small">
            <option value="">All Relationships</option>
            <!-- Populated by JavaScript -->
        </select>
    </div>
    <div class="uk-width-1-3@m uk-text-muted uk-text-small uk-flex uk-flex-middle">
        <span id="graph-stats">Loading...</span>
    </div>
</div>

<!-- Graph Container -->
<div class="uk-position-relative">
    <!-- Loading State -->
    <div id="graph-loading" class="uk-flex uk-flex-center uk-flex-middle uk-background-muted"
         style="height: 600px;">
        <div class="uk-text-center">
            <div uk-spinner="ratio: 2"></div>
            <p class="uk-margin-top uk-text-muted">Loading graph data...</p>
        </div>
    </div>

    <!-- Cytoscape Container (hidden until loaded) -->
    <div id="cy" class="uk-background-muted uk-border-rounded"
         style="width: 100%; height: 600px; display: none;"></div>

    <!-- Minimap Container -->
    <div id="cy-minimap" style="position: absolute; bottom: 10px; right: 10px;
         width: 150px; height: 150px; background: rgba(255,255,255,0.9);
         border: 1px solid #ddd; border-radius: 4px; display: none;"></div>
</div>

<p class="uk-text-muted uk-text-small uk-margin-top">
    <span uk-icon="info"></span>
    Drag nodes to reposition. Scroll to zoom. Right-click for options.
</p>

<!-- Person Details Modal -->
<div id="person-modal" uk-modal>
    <div class="uk-modal-dialog uk-modal-body">
        <button class="uk-modal-close-default" type="button" uk-close></button>
        <div id="person-modal-body">
            <!-- Content populated by JavaScript -->
        </div>
    </div>
</div>

<!-- Cytoscape.js and Extensions -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/cytoscape/3.30.4/cytoscape.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/cytoscape-context-menus@4.1.0/cytoscape-context-menus.min.js"></script>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/cytoscape-context-menus@4.1.0/cytoscape-context-menus.min.css">
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/cytoscape-popper@2.0.0/cytoscape-popper.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/tippy.js@6.3.7/dist/tippy-bundle.umd.min.js"></script>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/tippy.js@6.3.7/dist/tippy.css">

<!-- Graph Initialization Script -->
{|
<style>
/* Cytoscape tooltip styling */
.tippy-box[data-theme~='cy'] {
    background-color: #333;
    color: white;
    font-size: 12px;
}
.tippy-box[data-theme~='cy'] .tippy-arrow {
    color: #333;
}
/* Context menu customization */
.cy-context-menus-cxt-menu {
    background: white;
    border-radius: 4px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.2);
}
.cy-context-menus-cxt-menuitem {
    padding: 8px 16px;
}
.cy-context-menus-cxt-menuitem:hover {
    background: #f0f0f0;
}
</style>

<script>
(function() {
    const container = document.getElementById('cy');
    const loadingEl = document.getElementById('graph-loading');
    const minimapEl = document.getElementById('cy-minimap');
    const searchInput = document.getElementById('graph-search');
    const filterSelect = document.getElementById('relationship-filter');
    const statsEl = document.getElementById('graph-stats');
    let cy = null;

    // Color mapping by gender
    const colorMap = {
        'F': '#E91E63',  // Pink for Female
        'M': '#2196F3',  // Blue for Male
        'X': '#9E9E9E'   // Gray for Unspecified
    };

    // Cytoscape style definitions
    const cyStyle = [
        {
            selector: 'node',
            style: {
                'background-color': ele => colorMap[ele.data('genderCode')] || '#9E9E9E',
                'label': 'data(name)',
                'width': ele => 20 + Math.log2(ele.data('weight') + 1) * 10,
                'height': ele => 20 + Math.log2(ele.data('weight') + 1) * 10,
                'font-size': '10px',
                'text-valign': 'bottom',
                'text-margin-y': '5px',
                'text-wrap': 'ellipsis',
                'text-max-width': '80px',
                'border-width': 2,
                'border-color': '#fff'
            }
        },
        {
            selector: 'node:selected',
            style: {
                'border-color': '#ffc107',
                'border-width': 4
            }
        },
        {
            selector: 'node.highlighted',
            style: {
                'opacity': 1,
                'z-index': 10
            }
        },
        {
            selector: 'node.faded',
            style: {
                'opacity': 0.2
            }
        },
        {
            selector: 'edge',
            style: {
                'width': 2,
                'line-color': '#aaa',
                'target-arrow-color': '#aaa',
                'target-arrow-shape': 'triangle',
                'curve-style': 'bezier',
                'label': 'data(label)',
                'font-size': '8px',
                'text-rotation': 'autorotate',
                'text-margin-y': '-10px',
                'text-opacity': 0.7
            }
        },
        {
            selector: 'edge.highlighted',
            style: {
                'line-color': '#ffc107',
                'target-arrow-color': '#ffc107',
                'width': 3,
                'opacity': 1,
                'z-index': 10
            }
        },
        {
            selector: 'edge.faded',
            style: {
                'opacity': 0.1
            }
        },
        {
            selector: 'edge:hidden',
            style: {
                'display': 'none'
            }
        }
    ];

    // Fetch and initialize graph
    fetch('/graph/data')
        .then(response => {
            if (!response.ok) throw new Error('Failed to load graph data');
            return response.json();
        })
        .then(graphData => {
            loadingEl.style.display = 'none';
            container.style.display = 'block';
            minimapEl.style.display = 'block';

            // Handle empty state
            if (graphData.nodes.length === 0) {
                container.innerHTML = `
                    <div class="uk-flex uk-flex-center uk-flex-middle" style="height: 100%;">
                        <div class="uk-text-center uk-text-muted">
                            <span uk-icon="icon: warning; ratio: 3"></span>
                            <p class="uk-margin-top">No people found.
                               <a href="/persons">Add some people</a> to see the network graph.</p>
                        </div>
                    </div>
                `;
                minimapEl.style.display = 'none';
                return;
            }

            // Populate filter dropdown
            graphData.relationshipTypes.forEach(type => {
                const option = document.createElement('option');
                option.value = type;
                option.textContent = type;
                filterSelect.appendChild(option);
            });

            // Update stats
            statsEl.textContent = `${graphData.nodes.length} people, ${graphData.edges.length} relationships`;

            // Initialize Cytoscape
            cy = cytoscape({
                container: container,
                elements: {
                    nodes: graphData.nodes,
                    edges: graphData.edges
                },
                style: cyStyle,
                layout: {
                    name: 'cose',
                    animate: true,
                    animationDuration: 500,
                    nodeRepulsion: 8000,
                    idealEdgeLength: 100
                },
                minZoom: 0.2,
                maxZoom: 3,
                wheelSensitivity: 0.3
            });

            // Make cy available globally for debugging
            window.cy = cy;

            // Initialize extensions
            initContextMenu();
            initTooltips();
            initEventHandlers();
        })
        .catch(error => {
            console.error('Graph loading error:', error);
            loadingEl.innerHTML = `
                <div class="uk-text-center uk-text-danger">
                    <span uk-icon="icon: warning; ratio: 3"></span>
                    <p class="uk-margin-top">Failed to load graph data.</p>
                    <button class="uk-button uk-button-default" onclick="location.reload()">
                        Retry
                    </button>
                </div>
            `;
        });

    // Context Menu Extension
    function initContextMenu() {
        cy.contextMenus({
            menuItems: [
                {
                    id: 'view-details',
                    content: '<span uk-icon="icon: user; ratio: 0.8"></span> View Details',
                    tooltipText: 'View person details',
                    selector: 'node',
                    onClickFunction: function(event) {
                        const node = event.target;
                        showPersonModal(node.data());
                    }
                },
                {
                    id: 'manage-relationships',
                    content: '<span uk-icon="icon: link; ratio: 0.8"></span> Manage Relationships',
                    tooltipText: 'Go to relationship management',
                    selector: 'node',
                    onClickFunction: function(event) {
                        const node = event.target;
                        window.location.href = '/persons/' + node.data('id') + '/relationships';
                    }
                }
            ]
        });
    }

    // Tooltips using Tippy.js
    function initTooltips() {
        // Node tooltips
        cy.nodes().forEach(node => {
            const ref = node.popperRef();
            const content = `<strong>${escapeHtml(node.data('name'))}</strong><br>${escapeHtml(node.data('email'))}`;

            tippy(ref, {
                content: content,
                allowHTML: true,
                theme: 'cy',
                trigger: 'manual',
                arrow: true,
                placement: 'top'
            });

            node.on('mouseover', () => node.tippy && node.tippy.show());
            node.on('mouseout', () => node.tippy && node.tippy.hide());
            node.tippy = node.popperRef()._tippy;
        });

        // Edge tooltips
        cy.edges().forEach(edge => {
            const ref = edge.popperRef();

            tippy(ref, {
                content: edge.data('label'),
                theme: 'cy',
                trigger: 'manual',
                arrow: true,
                placement: 'top'
            });

            edge.on('mouseover', () => edge.tippy && edge.tippy.show());
            edge.on('mouseout', () => edge.tippy && edge.tippy.hide());
            edge.tippy = edge.popperRef()._tippy;
        });
    }

    // Event handlers
    function initEventHandlers() {
        // Node click - highlight neighborhood
        cy.on('tap', 'node', function(evt) {
            const node = evt.target;
            highlightNeighborhood(node);
        });

        // Background click - reset highlighting
        cy.on('tap', function(evt) {
            if (evt.target === cy) {
                resetHighlighting();
            }
        });
    }

    // Highlight neighborhood
    function highlightNeighborhood(node) {
        cy.elements().removeClass('highlighted faded');
        const neighborhood = node.neighborhood().add(node);
        neighborhood.addClass('highlighted');
        cy.elements().not(neighborhood).addClass('faded');
    }

    // Reset highlighting
    function resetHighlighting() {
        cy.elements().removeClass('highlighted faded');
    }

    // Search functionality
    let searchDebounce;
    searchInput.addEventListener('input', function() {
        clearTimeout(searchDebounce);
        searchDebounce = setTimeout(() => {
            const query = this.value.toLowerCase().trim();

            if (!query) {
                resetHighlighting();
                return;
            }

            const matched = cy.nodes().filter(node =>
                node.data('name').toLowerCase().includes(query)
            );

            if (matched.length > 0) {
                cy.elements().removeClass('highlighted faded');
                matched.addClass('highlighted');
                cy.elements().not(matched).addClass('faded');

                // Center on single match
                if (matched.length === 1) {
                    cy.animate({
                        center: { eles: matched },
                        zoom: 1.5
                    }, { duration: 300 });
                }
            } else {
                cy.elements().addClass('faded');
            }
        }, 300);
    });

    // Filter by relationship type
    filterSelect.addEventListener('change', function() {
        const selectedType = this.value;

        if (!selectedType) {
            cy.edges().removeClass('hidden');
            cy.nodes().removeClass('faded');
            return;
        }

        cy.edges().forEach(edge => {
            if (edge.data('label') === selectedType) {
                edge.removeClass('hidden');
            } else {
                edge.addClass('hidden');
            }
        });

        // Fade orphaned nodes
        cy.nodes().forEach(node => {
            const visibleEdges = node.connectedEdges().filter(e => !e.hasClass('hidden'));
            if (visibleEdges.length === 0) {
                node.addClass('faded');
            } else {
                node.removeClass('faded');
            }
        });
    });

    // Layout buttons
    document.getElementById('layout-cose').addEventListener('click', () => {
        cy.layout({ name: 'cose', animate: true, animationDuration: 500 }).run();
    });

    document.getElementById('layout-circle').addEventListener('click', () => {
        cy.layout({ name: 'circle', animate: true, animationDuration: 500 }).run();
    });

    document.getElementById('layout-grid').addEventListener('click', () => {
        cy.layout({ name: 'grid', animate: true, animationDuration: 500 }).run();
    });

    // Export as PNG
    document.getElementById('btn-export').addEventListener('click', () => {
        const png = cy.png({ full: false, scale: 2, bg: '#f8f8f8' });
        const link = document.createElement('a');
        const date = new Date().toISOString().split('T')[0];
        link.download = `relationship-graph-${date}.png`;
        link.href = png;
        link.click();
    });

    // Show person details modal
    function showPersonModal(nodeData) {
        const modalBody = document.getElementById('person-modal-body');
        modalBody.innerHTML = `
            <h2 class="uk-modal-title">${escapeHtml(nodeData.name)}</h2>
            ${nodeData.notes ? `<p class="uk-text-muted uk-text-italic">${escapeHtml(nodeData.notes)}</p>` : ''}
            <dl class="uk-description-list uk-description-list-divider uk-margin-top">
                <dt>Email</dt>
                <dd><a href="mailto:${escapeHtml(nodeData.email)}">${escapeHtml(nodeData.email)}</a></dd>
                ${nodeData.phone ? `<dt>Phone</dt><dd>${escapeHtml(nodeData.phone)}</dd>` : ''}
                ${nodeData.dateOfBirth ? `<dt>Date of Birth</dt><dd>${escapeHtml(nodeData.dateOfBirth)}</dd>` : ''}
                ${nodeData.gender ? `<dt>Gender</dt><dd>${escapeHtml(nodeData.gender)}</dd>` : ''}
            </dl>
            <div class="uk-margin-top">
                <a href="/persons/${nodeData.id}/relationships"
                   class="uk-button uk-button-primary uk-modal-close">
                    <span uk-icon="link"></span> Manage Relationships
                </a>
            </div>
        `;
        UIkit.modal('#person-modal').show();
    }

    // HTML escape helper
    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // Cleanup on HTMX navigation
    document.body.addEventListener('htmx:beforeSwap', function(evt) {
        if (evt.detail.target.id === 'main-content' && cy) {
            cy.destroy();
            cy = null;
        }
    });
})();
</script>
|}

{/include}
```

---

## 7. Navigation Update

### 7.1 Sidebar Navigation

The existing navigation in `base.html` is reused:

```html
<li class="{#if currentPage?? == 'graph'}uk-active{/if}">
    <a href="/graph">
        <span uk-icon="icon: git-branch; ratio: 1.2"></span>
        <span class="uk-margin-small-left">Graph</span>
    </a>
</li>
```

---

## 8. Security Configuration

### 8.1 Route Protection

**application.properties** (existing):
```properties
quarkus.http.auth.permission.graph.paths=/graph,/graph/*
quarkus.http.auth.permission.graph.policy=authenticated
```

---

## 9. Performance Considerations

### 9.1 Graph Size Limits

| Node Count | Expected Performance |
|------------|---------------------|
| < 500 | Excellent (60 FPS) |
| 500-2000 | Good (30-60 FPS) |
| 2000-10000 | Acceptable (15-30 FPS) |
| > 10000 | Consider pagination or clustering |

### 9.2 Optimization Strategies

For large datasets:
1. **Lazy label rendering**: Labels only at zoom > 0.5
2. **Use WebGL renderer**: For graphs > 5000 nodes
3. **Server-side pagination**: Load subgraphs based on filters
4. **Clustering**: Use Cytoscape's clustering extension

---

## 10. HTMX Integration Notes

### 10.1 Script Execution

The template uses Qute's `{| |}` raw block to prevent escaping of JavaScript. The script is self-contained and executes when the fragment is swapped into the DOM.

### 10.2 Cleanup

The `htmx:beforeSwap` listener ensures Cytoscape is properly destroyed when navigating away, preventing memory leaks.

---

## 11. Traceability

| Use Case | Implementation Component |
|----------|-------------------------|
| UC-004-01-01: Display Network Graph | `GraphResource.showGraph()`, `graph.html`, Cytoscape init |
| UC-004-01-02: Navigation/Interaction | Cytoscape core pan/zoom/drag |
| UC-004-01-03: Node Tooltip | Tippy.js + cytoscape-popper |
| UC-004-01-04: Edge Tooltip | Tippy.js + cytoscape-popper |
| UC-004-01-05: Context Menu | cytoscape-context-menus extension |
| UC-004-01-06: Person Details Modal | `showPersonModal()`, `#person-modal` |
| UC-004-01-07: Navigate to Relationships | Context menu + modal link |
| UC-004-01-08: Search and Highlight | `searchInput` handler, CSS classes |
| UC-004-01-09: Filter by Relationship | `filterSelect` handler, edge hiding |
| UC-004-01-10: Switch Layout | Layout buttons, `cy.layout()` |
| UC-004-01-11: Visual Styling | `cyStyle` definition |
| UC-004-02-01: Export PNG | `cy.png()`, download link |

---

## 12. Dependencies

### 12.1 Backend Dependencies

**Existing** (no changes required):
- `quarkus-rest-jsonb` - JSON serialization
- `quarkus-hibernate-orm-panache` - Data access
- `quarkus-rest-qute` - Template rendering

### 12.2 Frontend Dependencies (CDN)

| Library | Version | CDN URL |
|---------|---------|---------|
| Cytoscape.js | 3.30.4 | cdnjs.cloudflare.com |
| cytoscape-context-menus | 4.1.0 | jsdelivr.net |
| cytoscape-popper | 2.0.0 | jsdelivr.net |
| Popper.js | 2.11.8 | jsdelivr.net |
| Tippy.js | 6.3.7 | jsdelivr.net |
| HTMX | 2.0.8 | Already in base.html |
| UIkit | 3.25 | Already in base.html |

### 12.3 Entity Dependencies

Existing entities from previous features:
- `Person` entity (Feature 003)
- `PersonRelationship` entity (Feature 003)
- `Relationship` entity (Feature 002)
- `Gender` entity (Feature 002)

---

## 13. Migration from force-graph

### 13.1 Changes Required

| Component | Action |
|-----------|--------|
| `GraphNode.java` | Update to include `weight` instead of `val` |
| `GraphLink.java` | Rename to `GraphEdge.java`, add `id` field |
| `GraphData.java` | Add `CyNode`/`CyEdge` wrappers, add `relationshipTypes` |
| `GraphService.java` | Update to new DTO structure |
| `graph.html` | Complete rewrite for Cytoscape.js |

### 13.2 Backward Compatibility

The `/graph/data` endpoint format changes. No backward compatibility is required as this is a complete feature replacement.

---

*Document Version: 2.0*
*Last Updated: January 2026*
*Technology: Cytoscape.js 3.30.4*
