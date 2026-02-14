# Phase 3: Network Discovery - Research

**Researched:** 2026-02-14
**Domain:** Person-centered graph traversal, configurable depth, server-rendered network view with HTMX
**Confidence:** HIGH

## Summary

Phase 3 transforms the existing "show-all" graph page (`/graph`) into a person-centered network discovery view where an investigator starts from a specific person and explores outward at configurable depth (1, 2, or 3+ degrees of separation). The implementation touches three key areas: (1) a graph traversal query that walks the `person_relationship` table outward from a focal person at a configurable depth, (2) a new or refactored resource endpoint that accepts a person ID and depth parameter and returns the network data, and (3) a server-rendered HTML view that displays the connection network with relationship type labels on each link.

The data model is already in place. The `person_relationship` table has `source_person_id`, `related_person_id`, and `relationship_id` columns with proper indexes. The table is directional (source -> related), and the seed data shows bidirectional entries for most relationships (e.g., Marx COLLABORATOR Engels AND Engels COLLABORATOR Marx). The existing `GraphResource` already builds node/link data structures (`GraphData`, `GraphNode`, `GraphLink`) and the existing D3.js graph page already renders nodes and links with relationship type labels. The existing "View Network" button on the person list currently links to `/graph` (the show-all graph) -- Phase 3 will update this to link to the person-centered network view.

There are two viable approaches for the graph traversal query: (A) **PostgreSQL recursive CTE** via native SQL or HQL, or (B) **iterative Java-side BFS** using Panache queries in a loop. Given the data characteristics (relationship table with good indexes, bounded depth of 1-3, dataset of ~62 persons with ~120 relationships in seed data), **approach B (iterative Java-side BFS) is the recommended primary approach**. It is simpler, testable, does not require native SQL, works entirely within the existing Panache repository pattern, and performs well for the expected data volumes. The PostgreSQL recursive CTE approach is documented as an alternative for future scaling but is unnecessary complexity for v1.

For the frontend, the recommended approach is a **server-rendered HTML table/list view** (not D3.js graph) that shows the network as a structured, readable list grouped by degree of separation. This aligns with the HTMX/server-rendered architecture and provides clear, scannable information for investigators. The existing D3.js graph page can remain as a separate "visual graph" feature; the person-centered network view serves a different use case (structured reading vs. visual exploration).

**Primary recommendation:** Implement person-centered network discovery as a new server-rendered page using iterative BFS in Java (not recursive CTE), with depth controlled by a query parameter and rendered as an HTML list/table grouped by degree of separation. Refactor the existing "View Network" links to point to the new person-centered endpoint.

## Standard Stack

### Core

No new libraries or dependencies needed. This phase uses only what is already in the project.

| Library | Version | Purpose | Already Present |
|---------|---------|---------|-----------------|
| Quarkus Hibernate ORM Panache | 3.30.3 | Repository queries for relationship traversal | Yes |
| Quarkus REST | 3.30.3 | New/refactored endpoint for person network | Yes |
| Qute Templates | 3.30.3 | Server-rendered network view with fragments | Yes |
| HTMX | 2.0.8 | Depth control via select dropdown, partial page updates | Yes |
| UIkit | 3.25.4 | Tables, lists, cards, badges for network display | Yes |
| PostgreSQL | 17.7 | Existing `person_relationship` table with indexes | Yes |

### Supporting

None needed. No new dependencies.

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Java-side iterative BFS | PostgreSQL `WITH RECURSIVE` CTE | CTE is more efficient for very large graphs but adds complexity (native SQL or HQL CTE syntax); Java BFS is simpler, testable, and sufficient for expected data volumes |
| Java-side iterative BFS | Hibernate 7 HQL CTE (`WITH` clause) | Quarkus 3.30 ships Hibernate 7.2 which supports HQL CTEs, but the syntax is less familiar and harder to debug; can migrate later if performance requires it |
| Server-rendered HTML list | D3.js force-directed graph | D3 requires JSON API endpoint + client-side JavaScript; HTML list is readable, searchable, printable, and aligns with the HTMX architecture |
| Keep existing `/graph` page | Replace with person-centered view | Keep both; `/graph` serves visual exploration, person network serves structured investigation |

**Installation:** No installation needed.

## Architecture Patterns

### Recommended Project Structure

```
src/main/java/io/archton/scaffold/
├── entity/
│   └── (no changes - Person, PersonRelationship, Relationship already exist)
├── repository/
│   └── PersonRelationshipRepository.java  # ADD: findConnectionsByPersonId methods
├── service/
│   └── NetworkService.java                # NEW: BFS traversal logic, DTO assembly
├── router/
│   └── GraphResource.java                 # MODIFY: Add person-centered network endpoint
└── (DTOs can be inner classes of service or resource)

src/main/resources/templates/
└── GraphResource/
    ├── graph.html                         # EXISTING: Keep as-is (show-all D3 graph)
    └── network.html                       # NEW: Person-centered network HTML view
```

### Pattern 1: Iterative BFS Graph Traversal in Java

**What:** Walk the `person_relationship` table outward from a focal person, collecting connections at each depth level. Use a `Set<Long>` of visited person IDs to avoid cycles. Use Panache queries to fetch relationships for each frontier set.
**When to use:** When traversing a relationship graph at bounded depth (1-3 levels).
**Why this over recursive CTE:** Simpler code, easier to test, works within existing Panache patterns, debuggable with standard Java tooling. The bounded depth means at most 3 query rounds.

```java
// Pseudocode for NetworkService.buildNetwork(personId, maxDepth)
public NetworkResult buildNetwork(Long personId, int maxDepth) {
    Set<Long> visited = new HashSet<>();
    Map<Integer, List<ConnectionInfo>> connectionsByDepth = new LinkedHashMap<>();
    Set<Long> currentFrontier = Set.of(personId);
    visited.add(personId);

    for (int depth = 1; depth <= maxDepth; depth++) {
        // Query: find all PersonRelationship where sourcePerson.id IN currentFrontier
        //        OR relatedPerson.id IN currentFrontier
        //        AND the OTHER person is not in visited
        List<PersonRelationship> relationships = repository.findConnectionsForPersonIds(currentFrontier);

        Set<Long> nextFrontier = new HashSet<>();
        List<ConnectionInfo> depthConnections = new ArrayList<>();

        for (PersonRelationship pr : relationships) {
            Long otherId = pr.sourcePerson.id.equals(/* a frontier id */) ? pr.relatedPerson.id : pr.sourcePerson.id;
            if (!visited.contains(otherId)) {
                visited.add(otherId);
                nextFrontier.add(otherId);
                depthConnections.add(new ConnectionInfo(pr, depth));
            }
        }

        connectionsByDepth.put(depth, depthConnections);
        currentFrontier = nextFrontier;

        if (currentFrontier.isEmpty()) break; // No more connections to explore
    }

    return new NetworkResult(focalPerson, connectionsByDepth);
}
```

Source: Standard BFS algorithm applied to the existing data model.

### Pattern 2: Bidirectional Relationship Query

**What:** The `person_relationship` table stores directional relationships (source -> related). To find ALL connections for a person, query where `sourcePerson.id IN (ids) OR relatedPerson.id IN (ids)`. This captures both directions.
**When to use:** Always when querying connections for a person, since the relationship may be stored in either direction.
**Critical detail:** The seed data stores MOST relationships bidirectionally (Marx COLLAB Engels AND Engels COLLAB Marx), but this is not guaranteed. The query must check both columns to be safe.

```java
// In PersonRelationshipRepository
public List<PersonRelationship> findConnectionsForPersonIds(Set<Long> personIds) {
    if (personIds.isEmpty()) return List.of();
    // Use EntityGraph to eagerly load related entities
    EntityGraph<?> graph = getEntityManager().getEntityGraph("PersonRelationship.withDetails");
    return find("sourcePerson.id IN ?1 OR relatedPerson.id IN ?1", personIds)
        .withHint(FETCH_GRAPH_HINT, graph)
        .list();
}
```

Source: Existing `PersonRelationshipRepository` pattern with EntityGraph hints.

### Pattern 3: Network View as Server-Rendered HTML

**What:** Render the network as a structured HTML page, not a D3.js graph. Group connections by degree of separation. Each connection row shows: the connected person's name, the relationship type, and through whom the connection exists (for depth 2+).
**When to use:** For the person-centered investigation view where readability and scannability matter more than visual layout.

```html
<!-- Depth 1: Direct connections -->
<h3>Direct Connections (1st Degree)</h3>
<table class="uk-table uk-table-hover uk-table-divider">
    <thead>
        <tr>
            <th>Person</th>
            <th>Relationship</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        {#for conn in depth1Connections}
        <tr>
            <td>{conn.person.getDisplayName()}</td>
            <td><span class="uk-badge">{conn.relationshipType}</span></td>
            <td>
                <a href="/graph/network/{conn.person.id}">View Network</a>
            </td>
        </tr>
        {/for}
    </tbody>
</table>

<!-- Depth 2: Second-degree connections -->
<h3>2nd Degree Connections</h3>
<!-- Similar table with additional "Connected Through" column -->
```

Source: Existing UIkit table patterns from `person.html` and `personRelationship.html`.

### Pattern 4: HTMX Depth Control

**What:** A `<select>` dropdown for depth (1, 2, 3) that triggers an HTMX GET request to reload the network table with the new depth. Similar to the pagination page-size selector in Phase 2.
**When to use:** When the user wants to change the network depth without a full page reload.

```html
<select name="depth" class="uk-select uk-form-small"
        hx-get="/graph/network/{person.id}"
        hx-target="#network-container"
        hx-include="this"
        hx-push-url="true">
    <option value="1" {#if depth == 1}selected{/if}>1 Degree</option>
    <option value="2" {#if depth == 2}selected{/if}>2 Degrees</option>
    <option value="3" {#if depth == 3}selected{/if}>3 Degrees</option>
</select>
```

Source: Existing page-size selector pattern from Phase 2 `person.html` lines 69-79; HTMX cascading select pattern from Context7 docs.

### Pattern 5: EntityGraph Enhancement for Network Queries

**What:** The existing `PersonRelationship.withDetails` entity graph loads `relatedPerson` (with title subgraph) and `relationship`. For network traversal, we also need `sourcePerson` loaded (to determine the "other" person when walking both directions). Either enhance the existing graph or create a new one.
**When to use:** When querying PersonRelationship for network traversal where both source and related persons are needed.

```java
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "PersonRelationship.withDetails",
        // existing graph - keep as-is
        attributeNodes = {
            @NamedAttributeNode(value = "relatedPerson", subgraph = "person-title"),
            @NamedAttributeNode("relationship")
        },
        subgraphs = @NamedSubgraph(name = "person-title", attributeNodes = @NamedAttributeNode("title"))
    ),
    @NamedEntityGraph(
        name = "PersonRelationship.withFullDetails",
        // new graph for network traversal
        attributeNodes = {
            @NamedAttributeNode(value = "sourcePerson", subgraph = "person-title"),
            @NamedAttributeNode(value = "relatedPerson", subgraph = "person-title"),
            @NamedAttributeNode("relationship")
        },
        subgraphs = @NamedSubgraph(name = "person-title", attributeNodes = @NamedAttributeNode("title"))
    )
})
```

Source: Existing `PersonRelationship` entity graph pattern, extended for bidirectional loading.

### Anti-Patterns to Avoid

- **Loading all relationships into memory then filtering in Java**: Always query with WHERE clause for the frontier person IDs, not `listAll()` followed by in-memory filtering. The existing `GraphResource.buildGraphData()` does `personRelationshipRepository.listAll()` -- this is the anti-pattern to avoid for person-centered queries.
- **N+1 queries for person details**: Use EntityGraph to eagerly load `sourcePerson`, `relatedPerson`, and `relationship` in a single query per depth level.
- **Unbounded depth**: Always enforce a maximum depth cap (e.g., 5) regardless of user input to prevent runaway queries. Validate and clamp the depth parameter server-side.
- **Replacing the existing graph page**: The D3.js graph page serves a different purpose (visual exploration). Keep it; add a new person-centered network view alongside it.
- **Using D3.js for the person-centered view**: The network view is an investigation tool -- readability, printability, and structured data matter more than visual graph layout. Use server-rendered HTML tables/lists.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Graph traversal | Custom recursive SQL | Java-side iterative BFS with Panache queries | Bounded depth (1-3), small dataset, simpler and more testable |
| Cycle detection | Custom visited-set tracking in SQL | `Set<Long> visited` in Java BFS | Simple, reliable, and debuggable |
| Connection display | Custom D3.js graph for structured data | UIkit tables with `uk-badge` for relationship types | Server-rendered, printable, accessible, consistent with app architecture |
| Depth selector | Custom JavaScript depth control | HTMX `<select>` with `hx-get` | Follows existing Phase 2 page-size selector pattern exactly |
| Eager loading | Manual JOIN FETCH JPQL | EntityGraph with Panache `withHint()` | Established codebase pattern per ARCHITECTURE.md |

**Key insight:** The graph traversal problem at bounded depth (1-3 levels) is fundamentally a simple BFS problem, not a complex graph algorithm. Java-side iteration with 1-3 database queries (one per depth level) is simpler, faster to implement, and easier to debug than any SQL-based recursive approach. SQL recursive CTEs become valuable only when depth is unbounded or the graph is very large -- neither applies to this use case.

## Common Pitfalls

### Pitfall 1: Directional Relationship Blindness

**What goes wrong:** Querying only `sourcePerson.id = ?1` misses relationships where the focal person is stored as `relatedPerson`.
**Why it happens:** The existing `PersonRelationshipRepository.findBySourcePersonId()` method only checks `sourcePerson.id`. If a relationship was entered as "Engels COLLABORATOR Marx" but not as "Marx COLLABORATOR Engels", querying only source would miss it for Marx.
**How to avoid:** Always query both directions: `sourcePerson.id IN ?1 OR relatedPerson.id IN ?1`. Then determine the "other" person programmatically.
**Warning signs:** A person shows fewer connections than expected; connections appear when viewing from the other person's perspective but not from this one.

### Pitfall 2: Duplicate Connections in Results

**What goes wrong:** The same pair appears twice in the connection list because relationships are stored bidirectionally (Marx COLLAB Engels AND Engels COLLAB Marx).
**Why it happens:** Querying both directions naturally returns both records for the same logical connection.
**How to avoid:** When building the connection list, deduplicate by person pair. For depth 1, a person-relationship pair (personId + relatedPersonId + relationshipId) should appear only once. Use the `visited` set to prevent re-adding a person who was already discovered at a shallower depth.
**Warning signs:** The same person appearing multiple times in the same depth group.

### Pitfall 3: N+1 Query for Person Details

**What goes wrong:** Rendering the network template triggers lazy loading of each connected person's `title`, `gender`, and `relationship.description`, generating dozens of additional SQL queries.
**Why it happens:** `PersonRelationship` has lazy-loaded associations by default (`FetchType.LAZY`).
**How to avoid:** Use a `@NamedEntityGraph` that eagerly loads `sourcePerson` (with title), `relatedPerson` (with title), and `relationship`. Apply the graph hint when querying at each depth level.
**Warning signs:** Many SQL queries in server logs when rendering the network page; slow page load for persons with many connections.

### Pitfall 4: Uncapped Depth Leading to Full Graph Scan

**What goes wrong:** A user passes `depth=100` or a manipulated query parameter, causing the BFS to traverse the entire graph.
**Why it happens:** No server-side validation of the depth parameter.
**How to avoid:** Clamp depth to allowed values (1, 2, 3) server-side, similar to how Phase 2 clamps page size. Reject or default any other value.
**Warning signs:** Slow response times for network queries; full graph appearing in the network view.

### Pitfall 5: Forgetting to Update the "View Network" Link in Person List

**What goes wrong:** The "View Network" button in the person list still links to `/graph` (show-all) instead of the new person-centered network view.
**Why it happens:** The link exists in TWO places in `person.html` (the `table` fragment AND the `modal_success_row` fragment) and must be updated in both.
**How to avoid:** Update all occurrences of `href="/graph"` in the person template to point to the new person-centered endpoint with the person ID.
**Warning signs:** Clicking "View Network" on a person row goes to the show-all graph instead of the person-centered view.

### Pitfall 6: Qute Checked Template Arithmetic Limitation

**What goes wrong:** Trying to use `{#if depth + 1 <= maxDepth}` or `{#let nextDepth = depth + 1}` in Qute templates fails at compile time.
**Why it happens:** Qute checked templates do not support arithmetic operators in `{#if}` conditions or `{#let}` bindings (documented in Phase 2 findings).
**How to avoid:** Pre-compute any needed values in Java and pass them as separate template parameters (e.g., `hasDeeper`, `depthLabel`).
**Warning signs:** Compile errors related to template expressions.

## Code Examples

Verified patterns from existing codebase and official sources:

### BFS Traversal Service

```java
@ApplicationScoped
public class NetworkService {

    @Inject
    PersonRepository personRepository;

    @Inject
    PersonRelationshipRepository personRelationshipRepository;

    /**
     * Build a person-centered network at the specified depth.
     * Uses iterative BFS with one Panache query per depth level.
     */
    public NetworkResult buildNetwork(Long focalPersonId, int maxDepth) {
        // Clamp depth
        maxDepth = Math.max(1, Math.min(maxDepth, 5));

        Person focalPerson = personRepository.findById(focalPersonId);
        if (focalPerson == null) return null;

        Set<Long> visited = new HashSet<>();
        visited.add(focalPersonId);

        Map<Integer, List<NetworkConnection>> connectionsByDepth = new LinkedHashMap<>();
        Set<Long> currentFrontier = new HashSet<>(Set.of(focalPersonId));

        for (int depth = 1; depth <= maxDepth; depth++) {
            List<PersonRelationship> rels =
                personRelationshipRepository.findConnectionsForPersonIds(currentFrontier);

            Set<Long> nextFrontier = new HashSet<>();
            List<NetworkConnection> connections = new ArrayList<>();

            for (PersonRelationship pr : rels) {
                // Determine which person is the "other" (not in current frontier context)
                // Both source and related could be frontier IDs; we want the non-visited one
                Long sourceId = pr.sourcePerson.id;
                Long relatedId = pr.relatedPerson.id;

                Long newPersonId = null;
                Person newPerson = null;
                Person throughPerson = null;
                if (!visited.contains(relatedId)) {
                    newPersonId = relatedId;
                    newPerson = pr.relatedPerson;
                    throughPerson = pr.sourcePerson;
                } else if (!visited.contains(sourceId)) {
                    newPersonId = sourceId;
                    newPerson = pr.sourcePerson;
                    throughPerson = pr.relatedPerson;
                }

                if (newPersonId != null) {
                    visited.add(newPersonId);
                    nextFrontier.add(newPersonId);
                    connections.add(new NetworkConnection(
                        newPerson,
                        pr.relationship,
                        throughPerson,
                        depth
                    ));
                }
            }

            connectionsByDepth.put(depth, connections);
            currentFrontier = nextFrontier;

            if (currentFrontier.isEmpty()) break;
        }

        return new NetworkResult(focalPerson, connectionsByDepth, maxDepth);
    }
}
```

Source: Standard BFS applied to existing `PersonRelationship` data model.

### Repository Method for Bidirectional Query

```java
// In PersonRelationshipRepository.java
public List<PersonRelationship> findConnectionsForPersonIds(Set<Long> personIds) {
    if (personIds.isEmpty()) return List.of();
    EntityGraph<?> graph = getEntityManager()
        .getEntityGraph("PersonRelationship.withFullDetails");
    return find("sourcePerson.id IN ?1 OR relatedPerson.id IN ?1",
            List.copyOf(personIds))
        .withHint(FETCH_GRAPH_HINT, graph)
        .list();
}
```

Source: Existing `PersonRelationshipRepository` EntityGraph pattern extended for bidirectional queries.

### Resource Endpoint for Person Network

```java
// In GraphResource.java or a new NetworkResource.java
@GET
@Path("/network/{personId}")
@Produces(MediaType.TEXT_HTML)
public TemplateInstance showPersonNetwork(
        @PathParam("personId") Long personId,
        @QueryParam("depth") @DefaultValue("1") int depth,
        @HeaderParam("HX-Request") String hxRequest) {

    // Clamp depth to allowed values
    if (depth < 1 || depth > 3) depth = 1;

    NetworkResult network = networkService.buildNetwork(personId, depth);
    if (network == null) {
        throw new NotFoundException("Person not found");
    }

    if ("true".equals(hxRequest)) {
        return Templates.network$connections(network, depth);
    }

    return Templates.network(
        "Network: " + network.focalPerson().getDisplayName(),
        "graph",
        getCurrentUserName(),
        network,
        depth
    );
}
```

Source: Existing `PersonResource.list()` pattern with HX-Request detection from Phase 2.

### HTMX Depth Selector

```html
<select name="depth" class="uk-select uk-form-small uk-form-width-small"
        hx-get="/graph/network/{focalPerson.id}"
        hx-target="#network-container"
        hx-push-url="true">
    <option value="1" {#if depth == 1}selected{/if}>1st Degree</option>
    <option value="2" {#if depth == 2}selected{/if}>2nd Degree</option>
    <option value="3" {#if depth == 3}selected{/if}>3rd Degree</option>
</select>
```

Source: Phase 2 page-size selector pattern; HTMX cascading select docs.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Show-all graph (`/graph`) | Person-centered network view | Phase 3 | Investigators can focus on one person's connections instead of seeing the entire graph |
| D3.js client-side rendering | Server-rendered HTML tables | Phase 3 | Aligns with HTMX/HDA architecture; readable, printable, accessible |
| No depth control | Configurable depth (1, 2, 3) | Phase 3 | Different investigations need different scope |

**Deprecated/outdated:**
- The existing `/graph` show-all page remains but is supplemented by the person-centered view. The "View Network" links in the person list will be updated to point to the person-centered view.

## Data Model Analysis

### Existing Schema (No Changes Needed)

The `person_relationship` table already has everything needed:

```
person_relationship
├── id (BIGSERIAL PK)
├── source_person_id (BIGINT FK -> person.id, indexed)
├── related_person_id (BIGINT FK -> person.id, indexed)
├── relationship_id (BIGINT FK -> relationship.id, indexed)
├── UNIQUE (source_person_id, related_person_id, relationship_id)
└── CHECK (source_person_id != related_person_id)
```

**Key observations from seed data analysis:**
- 62 persons, ~120 person_relationship records
- Most relationships are stored bidirectionally (e.g., Marx COLLAB Engels AND Engels COLLAB Marx)
- 15 relationship types: SPOUSE, PARENT, CHILD, SIBLING, COLLEAGUE, FRIEND, MENTOR, MENTEE, ALLY, COLLAB, SUCCESSOR, PREDEC, RIVAL, STUDENT, TEACHER
- Bidirectional indexing on both `source_person_id` and `related_person_id` supports efficient querying in both directions
- Self-referential constraint (`CHECK source_person_id != related_person_id`) prevents self-loops

**No new migrations needed.** The existing schema and indexes fully support the network traversal queries.

### Query Performance Estimate

For depth 3 BFS from a well-connected person (e.g., Marx with ~10 direct connections):
- Depth 1: 1 query, returns ~10-20 rows (direct connections)
- Depth 2: 1 query for ~10 frontier IDs, returns ~30-50 rows
- Depth 3: 1 query for ~20 frontier IDs, returns ~40-60 rows
- Total: 3 SQL queries with indexed lookups, sub-millisecond each

This is well within acceptable performance. No caching needed for v1.

## Alternative Approach: PostgreSQL Recursive CTE

Documented here for reference. NOT recommended for v1, but available if needed for future scaling.

**Quarkus 3.30 ships Hibernate ORM 7.2**, which inherits Hibernate 6.2's CTE support. This means `WITH RECURSIVE` can be expressed in HQL (not just native SQL):

```sql
-- PostgreSQL recursive CTE for graph traversal with depth tracking and cycle detection
WITH RECURSIVE network AS (
    -- Base case: direct connections from focal person
    SELECT
        pr.related_person_id AS person_id,
        pr.relationship_id,
        pr.source_person_id AS connected_through,
        1 AS depth,
        ARRAY[pr.source_person_id, pr.related_person_id] AS path
    FROM person_relationship pr
    WHERE pr.source_person_id = :focalPersonId

    UNION ALL

    -- Recursive case: next degree of separation
    SELECT
        pr.related_person_id AS person_id,
        pr.relationship_id,
        pr.source_person_id AS connected_through,
        n.depth + 1 AS depth,
        n.path || pr.related_person_id
    FROM person_relationship pr
    JOIN network n ON pr.source_person_id = n.person_id
    WHERE n.depth < :maxDepth
      AND NOT (pr.related_person_id = ANY(n.path))  -- cycle detection
)
SELECT DISTINCT ON (person_id) *
FROM network
ORDER BY person_id, depth;
```

This approach is more complex but would be necessary if:
- The dataset grows to thousands of persons with dense connections
- Depth exceeds 3-4 levels
- Performance of iterative Java BFS becomes insufficient

Source: [PostgreSQL Official Docs - WITH Queries](https://www.postgresql.org/docs/current/queries-with.html), [Vlad Mihalcea - Hibernate WITH RECURSIVE](https://vladmihalcea.com/hibernate-with-recursive-query/), [Hibernate ORM 6.2 CTE Support](https://in.relation.to/2023/02/20/hibernate-orm-62-ctes/)

## Open Questions

1. **Endpoint path: `/graph/network/{personId}` vs `/persons/{personId}/network`**
   - What we know: The existing graph is at `/graph`. Person relationships are at `/persons/{personId}/relationships`. The route `/graph/*` is already in the authenticated permissions list.
   - What's unclear: Whether the person-centered network is semantically a "graph" feature or a "person" feature.
   - Recommendation: Use `/graph/network/{personId}` to keep it under the existing `/graph` route (already in auth config, already has a GraphResource). This avoids needing to update `application.properties` permissions. The navigation sidebar "Graph" entry can remain as the entry point.

2. **Should the existing show-all graph page be kept or replaced?**
   - What we know: The D3.js graph serves a different use case (visual exploration of the full dataset). The person-centered view serves structured investigation.
   - What's unclear: Whether users still need the show-all view.
   - Recommendation: Keep both. The navigation sidebar can link to the show-all graph (`/graph`). The "View Network" button on person rows links to the person-centered view (`/graph/network/{personId}`). Phase 3 scope is NET-01/02/03 which focus on person-centered network -- not removing existing functionality.

3. **How to handle the "connected through" display for depth 2+?**
   - What we know: At depth 1, connections are direct. At depth 2, person C is connected through person B. The user needs to see the chain: "Person C -- [Relationship] -- Person B (who is [Relationship] to focal person)."
   - What's unclear: How much chain detail to show vs. keeping the display simple.
   - Recommendation: For depth 2+, add a "Connected Through" column showing the intermediate person's name. For depth 3, show the most recent intermediary. Full path display adds complexity without clear value for v1.

4. **DTO design: separate record classes vs. inner classes?**
   - What we know: The existing `GraphResource` uses inner static classes for DTOs (`GraphData`, `GraphNode`, `GraphLink`). ARCHITECTURE.md does not mandate a specific DTO location.
   - What's unclear: Whether to follow the same pattern or use Java records.
   - Recommendation: Use Java records (available since Java 16, project uses Java 21) for the network DTOs. They are more concise and immutable by default. Place them as inner records of `NetworkService` or as top-level records in a `dto` package if there are more than 2-3.

## Sources

### Primary (HIGH confidence)

- Existing codebase: `GraphResource.java` -- current graph endpoint, DTO patterns, data building logic
- Existing codebase: `PersonRelationshipRepository.java` -- EntityGraph pattern, bidirectional query foundation
- Existing codebase: `PersonRelationship.java` entity -- data model, existing entity graph definition
- Existing codebase: `person.html` -- "View Network" link locations (table fragment + modal_success_row fragment)
- Existing codebase: `V1.6.0__Create_person_relationship_table.sql` -- schema with indexes and constraints
- Existing codebase: `V1.6.1__Insert_person_relationship_data.sql` -- seed data analysis (62 persons, ~120 relationships)
- `docs/ARCHITECTURE.md` -- EntityGraph pattern, service layer guidelines, repository patterns
- [Quarkus Hibernate ORM Panache Guide](https://quarkus.io/guides/hibernate-orm-panache) -- `PanacheQuery`, `find()`, EntityGraph with `withHint()`
- Context7 `/websites/quarkus_io_guides` -- DTO projection, named queries, native query patterns

### Secondary (MEDIUM confidence)

- [PostgreSQL Official Docs - WITH Queries](https://www.postgresql.org/docs/current/queries-with.html) -- Recursive CTE syntax, depth tracking, cycle detection
- [Vlad Mihalcea - Hibernate WITH RECURSIVE](https://vladmihalcea.com/hibernate-with-recursive-query/) -- HQL CTE approach for Hibernate 6+
- [Hibernate ORM 6.2 CTE Support Blog](https://in.relation.to/2023/02/20/hibernate-orm-62-ctes/) -- Confirmed CTE support in HQL
- Context7 `/bigskysoftware/htmx` -- Cascading select patterns, `hx-get` with parameters, `hx-push-url`

### Tertiary (LOW confidence)

- Web search for Quarkus 3.30 Hibernate version (confirmed as Hibernate 7.2 via release notes and migration guides, but exact minor version unverified)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- no new dependencies; everything needed already exists in the project
- Architecture: HIGH -- BFS traversal is a well-understood algorithm; server-rendered HTML follows established project patterns; existing data model supports the queries without modification
- Pitfalls: HIGH -- identified from direct codebase inspection (bidirectional relationships, N+1 queries, template duplication, Qute arithmetic limitation all observed in existing code)
- Data model: HIGH -- direct analysis of schema, indexes, constraints, and seed data confirms suitability

**Research date:** 2026-02-14
**Valid until:** 2026-04-14 (stable; no external dependencies that could change, data model is already in place)
