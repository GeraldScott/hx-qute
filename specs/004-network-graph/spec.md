# Technical Specification: Feature 004 - Network Graph Visualization

This document describes the technical implementation for the Network Graph Visualization feature, providing an interactive force-directed graph display of people and their relationships using the `force-graph` JavaScript library.

---

## 1. Technology Selection

### 1.1 Why force-graph?

After evaluating multiple JavaScript graph visualization libraries, **force-graph** (by vasturiano) was selected for this HTMX/Qute project because:

| Criterion | force-graph | Alternatives |
|-----------|-------------|--------------|
| **CDN availability** | Single script, no dependencies | Many require npm/bundler |
| **API simplicity** | Minimal - one constructor | More complex setup |
| **HTMX integration** | Works with vanilla JS | Some need React/Vue |
| **Performance** | Good (Canvas-based, up to 5k nodes) | Varies widely |
| **Bundle size** | ~150KB | 200KB - 1MB+ |
| **Learning curve** | Low | Medium to High |

### 1.2 CDN Include

```html
<script src="https://cdn.jsdelivr.net/npm/force-graph@1.47.4/dist/force-graph.min.js"></script>
```

---

## 2. Data Model

### 2.1 Graph Data Structure

The force-graph library expects JSON in this format:

```json
{
  "nodes": [
    { "id": "1", "name": "John Doe", "val": 3, "group": "M" },
    { "id": "2", "name": "Jane Doe", "val": 5, "group": "F" }
  ],
  "links": [
    { "source": "1", "target": "2", "label": "Spouse" }
  ]
}
```

### 2.2 GraphNode DTO

**File**: `src/main/java/io/archton/scaffold/dto/GraphNode.java`

```java
package io.archton.scaffold.dto;

public record GraphNode(
    String id,
    String name,
    int val,      // Node size (based on relationship count)
    String group, // Gender code for coloring (M, F, X)
    String email,
    String phone,
    String dateOfBirth,
    String gender
) {
    public static GraphNode from(io.archton.scaffold.entity.Person p, int relationshipCount) {
        return new GraphNode(
            String.valueOf(p.id),
            p.getDisplayName(),
            Math.max(1, relationshipCount), // Minimum size 1
            p.gender != null ? p.gender.code : "X",
            p.email,
            p.phone,
            p.dateOfBirth != null ? p.dateOfBirth.toString() : null,
            p.gender != null ? p.gender.description : null
        );
    }
}
```

### 2.3 GraphLink DTO

**File**: `src/main/java/io/archton/scaffold/dto/GraphLink.java`

```java
package io.archton.scaffold.dto;

public record GraphLink(
    String source,
    String target,
    String label
) {
    public static GraphLink from(io.archton.scaffold.entity.PersonRelationship pr) {
        return new GraphLink(
            String.valueOf(pr.sourcePerson.id),
            String.valueOf(pr.relatedPerson.id),
            pr.relationship.description
        );
    }
}
```

### 2.4 GraphData DTO

**File**: `src/main/java/io/archton/scaffold/dto/GraphData.java`

```java
package io.archton.scaffold.dto;

import java.util.List;

public record GraphData(
    List<GraphNode> nodes,
    List<GraphLink> links
) {}
```

---

## 3. Repository Layer

### 3.1 PersonRepository Updates

Add method to count relationships per person:

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

Add method to fetch all relationships efficiently:

```java
/**
 * Find all relationships with eager loading for graph visualization.
 */
public List<PersonRelationship> findAllForGraph() {
    return getEntityManager()
        .createQuery(
            "SELECT pr FROM PersonRelationship pr " +
            "JOIN FETCH pr.sourcePerson " +
            "JOIN FETCH pr.relatedPerson " +
            "JOIN FETCH pr.relationship",
            PersonRelationship.class
        )
        .getResultList();
}
```

---

## 4. Service Layer

### 4.1 GraphService

**File**: `src/main/java/io/archton/scaffold/service/GraphService.java`

The service layer builds graph data by aggregating data from multiple repositories. JSON serialization is handled automatically by JSON-B via the REST endpoint.

```java
package io.archton.scaffold.service;

import io.archton.scaffold.dto.GraphData;
import io.archton.scaffold.dto.GraphLink;
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
     * Build graph data for visualization.
     * Returns a DTO that JSON-B will automatically serialize.
     */
    public GraphData buildGraphData() {
        // Get relationship counts per person
        Map<Long, Integer> relationshipCounts = personRepository.countRelationshipsByPerson();

        // Build nodes from all persons
        List<Person> persons = personRepository.listAll();
        List<GraphNode> nodes = persons.stream()
            .map(p -> GraphNode.from(p, relationshipCounts.getOrDefault(p.id, 0)))
            .toList();

        // Build links from all relationships
        List<PersonRelationship> relationships = personRelationshipRepository.findAllForGraph();
        List<GraphLink> links = relationships.stream()
            .map(GraphLink::from)
            .toList();

        return new GraphData(nodes, links);
    }
}
```

**Note**: No manual JSON serialization is needed. JSON-B (Jakarta JSON Binding) automatically serializes Java records to JSON when returned from a REST endpoint with `@Produces(MediaType.APPLICATION_JSON)`.

---

## 5. Resource Layer

### 5.1 GraphResource

**File**: `src/main/java/io/archton/scaffold/router/GraphResource.java`

The resource provides two endpoints:
1. `GET /graph` - Returns the HTML page (Qute template)
2. `GET /graph/data` - Returns graph data as JSON (for JavaScript fetch)

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
     * Return graph data as JSON.
     * JSON-B automatically serializes the GraphData record.
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
| GET | `/graph/data` | `getGraphData()` | Return graph data as JSON |

### 5.3 JSON-B Serialization

The `GraphData`, `GraphNode`, and `GraphLink` records are automatically serialized by JSON-B. Example response from `GET /graph/data`:

```json
{
  "nodes": [
    {
      "id": "1",
      "name": "John Doe",
      "val": 3,
      "group": "M",
      "email": "john@example.com",
      "phone": "555-1234",
      "dateOfBirth": "1980-05-15",
      "gender": "Male"
    }
  ],
  "links": [
    {
      "source": "1",
      "target": "2",
      "label": "Spouse"
    }
  ]
}
```

---

## 6. Template Structure

### 6.1 Template File

**File**: `templates/GraphResource/graph.html`

The template fetches graph data asynchronously from `/graph/data` using the Fetch API. This approach:
- Separates HTML rendering from JSON data
- Allows the page to show a loading state
- Uses JSON-B for proper serialization (no manual JSON building)

```html
{@String title}
{@String currentPage}
{@String userName}
{#include base}

<h2 class="uk-heading-small">
    <span uk-icon="icon: git-branch; ratio: 1.5"></span>
    Relationship Network
</h2>

<p class="uk-text-muted uk-margin-bottom">
    Drag nodes to reposition. Right-click a node to view details. Scroll to zoom.
</p>

<!-- Graph Container with Loading State -->
<div id="graph-container"
     class="uk-background-muted uk-border-rounded"
     style="width: 100%; height: 600px; position: relative;">
    <!-- Loading indicator shown while fetching data -->
    <div id="graph-loading" class="uk-flex uk-flex-center uk-flex-middle" style="height: 100%;">
        <div class="uk-text-center">
            <div uk-spinner="ratio: 2"></div>
            <p class="uk-margin-top uk-text-muted">Loading graph data...</p>
        </div>
    </div>
</div>

<!-- Person Details Modal -->
<div id="person-modal" uk-modal>
    <div class="uk-modal-dialog uk-modal-body">
        <button class="uk-modal-close-default" type="button" uk-close></button>
        <div id="person-modal-body">
            <!-- Content populated by JavaScript -->
        </div>
    </div>
</div>

<!-- Force-Graph Library -->
<script src="https://cdn.jsdelivr.net/npm/force-graph@1.47.4/dist/force-graph.min.js"></script>

<!-- Graph Initialization Script -->
<script>
(function() {
    const container = document.getElementById('graph-container');
    const loadingEl = document.getElementById('graph-loading');
    let graph = null;

    // Color mapping by gender group
    const colorMap = {
        'F': '#E91E63',  // Pink for Female
        'M': '#2196F3',  // Blue for Male
        'X': '#9E9E9E'   // Gray for Not Specified
    };

    // Fetch graph data from JSON endpoint
    fetch('/graph/data')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load graph data');
            }
            return response.json();
        })
        .then(graphData => {
            // Remove loading indicator
            loadingEl.remove();

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
                return;
            }

            // Initialize force-graph
            graph = new ForceGraph(container)
                .graphData(graphData)
                .nodeId('id')
                .nodeLabel('name')
                .nodeVal('val')
                .nodeColor(node => colorMap[node.group] || '#9E9E9E')
                .nodeCanvasObject((node, ctx, globalScale) => {
                    // Draw circle
                    const size = Math.sqrt(node.val) * 4 + 4;
                    ctx.beginPath();
                    ctx.arc(node.x, node.y, size, 0, 2 * Math.PI);
                    ctx.fillStyle = colorMap[node.group] || '#9E9E9E';
                    ctx.fill();

                    // Draw border
                    ctx.strokeStyle = '#fff';
                    ctx.lineWidth = 1.5 / globalScale;
                    ctx.stroke();

                    // Draw label if zoomed in enough
                    if (globalScale > 0.7) {
                        ctx.font = `${12/globalScale}px Sans-Serif`;
                        ctx.textAlign = 'center';
                        ctx.textBaseline = 'middle';
                        ctx.fillStyle = '#333';
                        ctx.fillText(node.name, node.x, node.y + size + 10/globalScale);
                    }
                })
                .nodeCanvasObjectMode(() => 'replace')
                .linkLabel('label')
                .linkColor(() => '#aaa')
                .linkWidth(1.5)
                .linkDirectionalArrowLength(6)
                .linkDirectionalArrowRelPos(1)
                .linkCurvature(0.1)
                .d3AlphaDecay(0.02)
                .d3VelocityDecay(0.3)
                .warmupTicks(50)
                .cooldownTicks(100)
                .onNodeClick((node, event) => {
                    // Left click - center on node
                    graph.centerAt(node.x, node.y, 500);
                    graph.zoom(2, 500);
                })
                .onNodeRightClick((node, event) => {
                    event.preventDefault();
                    showPersonModal(node);
                });

            // Handle window resize
            window.addEventListener('resize', handleResize);
        })
        .catch(error => {
            console.error('Graph loading error:', error);
            loadingEl.innerHTML = `
                <div class="uk-text-center uk-text-danger">
                    <span uk-icon="icon: warning; ratio: 3"></span>
                    <p class="uk-margin-top">Failed to load graph data. Please try refreshing the page.</p>
                </div>
            `;
        });

    // Prevent context menu on graph
    container.addEventListener('contextmenu', e => e.preventDefault());

    // Show person details modal
    function showPersonModal(node) {
        const modalBody = document.getElementById('person-modal-body');
        modalBody.innerHTML = `
            <h2 class="uk-modal-title">${escapeHtml(node.name)}</h2>
            <dl class="uk-description-list uk-description-list-divider">
                <dt>Email</dt>
                <dd><a href="mailto:${escapeHtml(node.email)}">${escapeHtml(node.email)}</a></dd>
                ${node.phone ? `<dt>Phone</dt><dd>${escapeHtml(node.phone)}</dd>` : ''}
                ${node.dateOfBirth ? `<dt>Date of Birth</dt><dd>${escapeHtml(node.dateOfBirth)}</dd>` : ''}
                ${node.gender ? `<dt>Gender</dt><dd>${escapeHtml(node.gender)}</dd>` : ''}
            </dl>
            <div class="uk-margin-top">
                <a href="/persons/${node.id}/relationships"
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

    // Handle window resize
    function handleResize() {
        if (graph) {
            graph.width(container.clientWidth);
            graph.height(container.clientHeight);
        }
    }

    // Cleanup on page unload (for HTMX navigation)
    document.body.addEventListener('htmx:beforeSwap', function(evt) {
        if (evt.detail.target.id === 'main-content') {
            window.removeEventListener('resize', handleResize);
        }
    });
})();
</script>

{/include}
```

---

## 7. Navigation Update

### 7.1 Add Graph to Sidebar

Update `templates/base.html` navigation fragment:

```html
<!-- Add after People menu item -->
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

**application.properties** (add):
```properties
quarkus.http.auth.permission.graph.paths=/graph,/graph/*
quarkus.http.auth.permission.graph.policy=authenticated
```

Both the HTML page (`/graph`) and JSON API (`/graph/data`) require authentication.

---

## 9. Empty State Handling

Empty state handling is integrated into the template's fetch callback (see Section 6.1). When no people exist, the graph container displays a friendly message with a link to add people.

---

## 10. Performance Considerations

### 10.1 Graph Size Limits

| Node Count | Expected Performance |
|------------|---------------------|
| < 100 | Excellent (60 FPS) |
| 100-500 | Good (30-60 FPS) |
| 500-1000 | Acceptable (15-30 FPS) |
| > 1000 | Consider pagination or clustering |

### 10.2 Optimization Strategies

For large datasets:
1. **Limit initial load**: Add pagination or limit query
2. **Hide labels at low zoom**: Already implemented
3. **Use simpler node shapes**: Circles are fastest
4. **Reduce link curvature**: Set to 0 for straight lines

---

## 11. HTMX Integration Notes

### 11.1 Graph Reinitialization

If the graph page is loaded via HTMX (partial swap), the script runs in the swapped content. Key considerations:

1. **Script in fragment**: The `<script>` tag is inside the template, so it executes on swap
2. **Cleanup on navigation**: The `htmx:beforeSwap` listener cleans up event handlers
3. **No global state**: The IIFE pattern keeps variables local

### 11.2 Alternative: htmx:afterSettle Event

If you prefer external scripts:

```html
<div id="graph-container"
     hx-on::after-settle="initGraph()">
</div>
```

---

## 12. Traceability

| Use Case | Implementation Component |
|----------|-------------------------|
| UC-004-01-01: Display Network Graph | `GraphResource.showGraph()`, `GraphResource.getGraphData()`, `graph.html`, force-graph init |
| UC-004-01-02: View Person Details | `onNodeRightClick()`, `showPersonModal()`, `#person-modal` |
| UC-004-01-03: Navigate to Relationships | Link in modal: `/persons/{id}/relationships` |
| UC-004-01-04: Customize Appearance | `colorMap`, `nodeCanvasObject()`, `nodeVal()` |

---

## 13. Dependencies

### 13.1 Backend Dependencies

**New dependency required** (add to `pom.xml`):

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-jsonb</artifactId>
</dependency>
```

This extension enables JSON-B (Jakarta JSON Binding) for automatic JSON serialization of Java objects returned from REST endpoints with `@Produces(MediaType.APPLICATION_JSON)`.

**Already included in project:**
- `quarkus-hibernate-orm-panache`
- `quarkus-rest-qute`
- `quarkus-security-jpa`

### 13.2 Frontend Dependencies (CDN)

| Library | Version | CDN URL |
|---------|---------|---------|
| force-graph | 1.47.4 | `https://cdn.jsdelivr.net/npm/force-graph@1.47.4/dist/force-graph.min.js` |
| HTMX | 2.0.8 | Already in base.html |
| UIkit | 3.25 | Already in base.html |

### 13.3 Entity Dependencies

- `Person` entity (Feature 003)
- `PersonRelationship` entity (Feature 003)
- `Relationship` entity (Feature 002)
- `Gender` entity (Feature 002)

---

*Document Version: 1.1*
*Last Updated: January 2026*
*Change: Replaced manual JSON serialization with JSON-B endpoint*
