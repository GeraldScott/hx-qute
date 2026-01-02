# Removal Plan: Feature 004 - Network Graph Visualization

## Overview

This plan removes the Network Graph Visualization feature (Feature 004) from the codebase. The feature provides an interactive Cytoscape.js-based graph visualization of people and their relationships.

**Created:** 2026-01-02
**Status:** ✅ Complete

---

## Impact Analysis

### Components to Remove

| Component | Type | Path |
|-----------|------|------|
| GraphResource | REST Resource | `src/main/java/io/archton/scaffold/router/GraphResource.java` |
| GraphService | Service | `src/main/java/io/archton/scaffold/service/GraphService.java` |
| GraphNode | DTO | `src/main/java/io/archton/scaffold/dto/GraphNode.java` |
| GraphEdge | DTO | `src/main/java/io/archton/scaffold/dto/GraphEdge.java` |
| GraphData | DTO | `src/main/java/io/archton/scaffold/dto/GraphData.java` |
| graph.html | Template | `src/main/resources/templates/GraphResource/graph.html` |
| Template Dir | Directory | `src/main/resources/templates/GraphResource/` |
| Feature Specs | Directory | `specs/004-network-graph/` |

### Components to Modify

| File | Change Required |
|------|-----------------|
| `base.html` | Remove navigation link to /graph |
| `application.properties` | Remove /graph from authenticated paths |
| `PersonRelationshipRepository.java` | Remove `findAllForGraph()` and `findDistinctRelationshipTypes()` methods |
| `PersonRelationship.java` | Remove `PersonRelationship.forGraph` named entity graph |
| `docs/USER-STORIES.md` | Remove Feature 004 user stories |
| `specs/PROJECT-PLAN.md` | Remove Feature 004 references |

### No Impact

| Component | Reason |
|-----------|--------|
| PersonRepository | `countRelationshipsByPerson()` may be used elsewhere - verify before removal |
| Person entity | No graph-specific code |
| docs/ARCHITECTURE.md | Entity Graphs section is general documentation, not graph-specific |

---

## Removal Steps

### Phase 1: Delete Feature-Specific Files

**Step 1.1: Delete Java source files**

```bash
rm src/main/java/io/archton/scaffold/router/GraphResource.java
rm src/main/java/io/archton/scaffold/service/GraphService.java
rm src/main/java/io/archton/scaffold/dto/GraphNode.java
rm src/main/java/io/archton/scaffold/dto/GraphEdge.java
rm src/main/java/io/archton/scaffold/dto/GraphData.java
```

**Step 1.2: Delete template directory**

```bash
rm -r src/main/resources/templates/GraphResource/
```

**Step 1.3: Delete feature specifications**

```bash
rm -r specs/004-network-graph/
```

---

### Phase 2: Modify Navigation

**Step 2.1: Remove graph link from base.html**

**File:** `src/main/resources/templates/base.html`

Remove lines 186-191:
```html
<li class="{#if currentPage?? == 'graph'}uk-active{/if}">
    <a href="/graph">
        <span uk-icon="icon: git-branch; ratio: 1.2"></span>
        <span class="uk-margin-small-left">Graph</span>
    </a>
</li>
```

---

### Phase 3: Update Security Configuration

**Step 3.1: Remove /graph from authenticated paths**

**File:** `src/main/resources/application.properties`

Change line 34 from:
```properties
quarkus.http.auth.permission.authenticated.paths=/dashboard/*,/api/*,/persons,/persons/*,/profile/*,/graph,/graph/*
```

To:
```properties
quarkus.http.auth.permission.authenticated.paths=/dashboard/*,/api/*,/persons,/persons/*,/profile/*
```

---

### Phase 4: Update Repository Layer

**Step 4.1: Remove graph-specific methods from PersonRelationshipRepository**

**File:** `src/main/java/io/archton/scaffold/repository/PersonRelationshipRepository.java`

Remove `findAllForGraph()` method (lines 79-87):
```java
/**
 * Find all relationships with eager loading for graph visualization.
 * Uses the PersonRelationship.forGraph entity graph.
 */
public List<PersonRelationship> findAllForGraph() {
    EntityGraph<?> graph = getEntityManager().getEntityGraph("PersonRelationship.forGraph");

    return find("SELECT pr FROM PersonRelationship pr")
        .withHint(FETCH_GRAPH_HINT, graph)
        .list();
}
```

Remove `findDistinctRelationshipTypes()` method (lines 89-99):
```java
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

### Phase 5: Update Entity Layer

**Step 5.1: Remove forGraph entity graph from PersonRelationship**

**File:** `src/main/java/io/archton/scaffold/entity/PersonRelationship.java`

Remove the second `@NamedEntityGraph` (lines 20-27):
```java
@NamedEntityGraph(
    name = "PersonRelationship.forGraph",
    attributeNodes = {
        @NamedAttributeNode("sourcePerson"),
        @NamedAttributeNode("relatedPerson"),
        @NamedAttributeNode("relationship")
    }
)
```

Keep the `PersonRelationship.withDetails` entity graph as it's used for relationship management.

---

### Phase 6: Update Documentation

**Step 6.1: Remove Feature 004 from USER-STORIES.md**

**File:** `docs/USER-STORIES.md`

Remove the entire "Feature 004: Network Graph Visualization" section including:
- US-004-01: Display a network diagram
- US-004-02: Export Graph View

**Step 6.2: Update PROJECT-PLAN.md**

**File:** `specs/PROJECT-PLAN.md`

1. Change "Current Phase" to next feature or "Complete"
2. Remove `@specs/004-network-graph/tasks.md` from context list
3. Remove Feature 004 row from Progress Summary table
4. Remove Feature 004 row from Feature Dependency Chain table

---

### Phase 7: Verification

**Step 7.1: Compile and verify**

```bash
./mvnw compile
```

Expected: Build succeeds with no errors

**Step 7.2: Run tests**

```bash
./mvnw test
```

Expected: All tests pass (no graph-related tests exist)

**Step 7.3: Manual verification**

1. Start dev server: `./mvnw quarkus:dev`
2. Navigate to application
3. Verify "Graph" link is removed from navigation
4. Verify `/graph` returns 404
5. Verify other features still work

---

## Rollback Plan

If issues arise, restore from git:

```bash
git checkout HEAD -- \
    src/main/java/io/archton/scaffold/router/GraphResource.java \
    src/main/java/io/archton/scaffold/service/GraphService.java \
    src/main/java/io/archton/scaffold/dto/ \
    src/main/resources/templates/GraphResource/ \
    src/main/resources/templates/base.html \
    src/main/resources/application.properties \
    src/main/java/io/archton/scaffold/repository/PersonRelationshipRepository.java \
    src/main/java/io/archton/scaffold/entity/PersonRelationship.java \
    specs/004-network-graph/ \
    specs/PROJECT-PLAN.md \
    docs/USER-STORIES.md
```

---

## Checklist

### Phase 1: Delete Feature-Specific Files
- [x] Delete `GraphResource.java`
- [x] Delete `GraphService.java`
- [x] Delete `GraphNode.java`
- [x] Delete `GraphEdge.java`
- [x] Delete `GraphData.java`
- [x] Delete `templates/GraphResource/` directory
- [x] Delete `specs/004-network-graph/` directory

### Phase 2: Modify Navigation
- [x] Remove graph navigation link from `base.html`

### Phase 3: Update Security Configuration
- [x] Remove `/graph,/graph/*` from `application.properties`

### Phase 4: Update Repository Layer
- [x] Remove `findAllForGraph()` method
- [x] Remove `findDistinctRelationshipTypes()` method

### Phase 5: Update Entity Layer
- [x] Remove `PersonRelationship.forGraph` entity graph

### Phase 6: Update Documentation
- [x] Remove Feature 004 from `USER-STORIES.md`
- [x] Update `PROJECT-PLAN.md`

### Phase 7: Verification
- [x] Build succeeds
- [x] Tests pass
- [x] Manual verification complete

---

## Notes

- The `PersonRepository.countRelationshipsByPerson()` method should be reviewed. It was added for graph node sizing but may have other uses. Leave it for now unless specifically requested to remove.
- No database migrations are required as the graph feature doesn't have its own tables.
- No test files exist for the graph feature, so no test cleanup is needed.

---

*Plan created: 2026-01-02*
*Awaiting user approval before implementation*
