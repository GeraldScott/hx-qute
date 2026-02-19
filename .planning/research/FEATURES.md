# Feature Research: HTMX Cleanup Patterns

**Domain:** HTMX navigation and response patterns in a Quarkus+Qute hypermedia-driven application
**Researched:** 2026-02-19
**Confidence:** HIGH (verified against official HTMX docs, Quarkus discussion, and codebase analysis)

## Feature Landscape

### Table Stakes (Must Fix for Correct HTMX Usage)

These are not "features" in the product sense. They are correctness fixes. The current codebase uses a branching antipattern (`HX-Request` header check) for page navigation that breaks the HTMX model and creates maintenance overhead. Fixing these is prerequisite to any future development.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **F1: Adopt `hx-boost` for page navigation** | HTMX's designed mechanism for AJAX-ified page navigation. Replaces manual `hx-get` on nav links. Server returns full page; HTMX extracts `<body>` content automatically. Progressive enhancement (works without JS). | MEDIUM | Add `hx-boost="true"` to nav containers in `base.html` and navigation fragment. Remove all `hx-get`/`hx-target`/`hx-push-url` from plain navigation links. |
| **F2: Remove `HX-Request` header branching from list endpoints** | Current pattern: `if ("true".equals(hxRequest)) return fragment; else return fullPage;`. This conflates two concerns: page-level navigation and in-page partial updates. With `hx-boost`, the server always returns the full page for navigation; HTMX handles extraction. The branching is only correct for the `HX-Boosted` header or for genuine partial-update endpoints. | MEDIUM | Affects 6 resources: GenderResource, TitleResource, RelationshipResource, PersonResource, PersonRelationshipResource, GraphResource. Each `list()` method has this branching. |
| **F3: Server always returns full page for navigation** | With `hx-boost`, boosted navigation requests include `HX-Boosted: true` header. HTMX is "smart enough to pull out only the content of the body tag to swap into the new page." The server does not need to branch. Just return the full HTML page every time. Title tag is automatically extracted and applied. | LOW | Simplification -- remove code rather than add it. Each resource's `list()` method becomes a single code path. |
| **F4: Correct `<title>` tag handling** | HTMX automatically processes `<title>` tags from boosted responses. The `base.html` template already has `<title>{title??}</title>` in `<head>`. With `hx-boost`, page titles update correctly on navigation without any server-side branching. Currently works on full-page load but not on HTMX navigation because fragments skip the `<head>`. | LOW | Works automatically once F1 is in place. No template changes needed -- `base.html` already renders the title correctly. |
| **F5: Active navigation state via server-side rendering** | Since `hx-boost` swaps the entire `<body>` innerHTML, the sidebar navigation re-renders on every page load. The `currentPage` variable in `navigation.html` already drives the `uk-active` class correctly. This "just works" with boosted navigation because the full body (including sidebar) is replaced. | LOW | Already implemented correctly in `navigation.html` via `{#if currentPage?? == 'gender'}uk-active{/if}`. Works automatically once F1+F3 are in place. |
| **F6: Browser history and back-button support** | `hx-boost` automatically pushes URLs to browser history for anchor tags and handles back/forward navigation via DOM snapshots. The current navigation links in `navigation.html` use plain `<a href="...">` which is exactly what `hx-boost` enhances. | LOW | Works automatically with F1. No additional attributes needed on navigation links. |
| **F7: Add `Vary: HX-Request` response header** | If any endpoints still branch on `HX-Request` (e.g., for genuine partial updates like pagination or search), the `Vary` header must be set to prevent HTTP caches from serving fragment responses to direct browser requests. | LOW | Add a JAX-RS `ContainerResponseFilter` or annotate endpoints. Only needed on endpoints that genuinely return different content based on request headers. |

### Nice-to-Have Improvements (Differentiators)

These improve DX and UX but are not required for correctness.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **F8: Install `head-support` extension** | Enables full `<head>` tag merging on boosted navigation: stylesheets, meta tags, and scripts update correctly. Without it, only `<title>` is processed. Low-risk CDN addition. | LOW | Add `<script src="https://cdn.jsdelivr.net/npm/htmx-ext-head-support@2.0.5"></script>` to `base.html` and `hx-ext="head-support"` to `<body>`. Enables proper merge/append behavior for `<head>` content across pages. |
| **F9: Preserve HTMX partial updates for search/filter/pagination** | Person and PersonRelationship resources have genuine partial-update use cases (filter, sort, paginate). These should keep returning fragments for in-page updates. The key distinction: navigation = full page via `hx-boost`; in-page interaction = fragment via `hx-get` with `hx-target`. | MEDIUM | Refactor to check `HX-Boosted` header (not `HX-Request`) for navigation branching if needed. For search/filter, use `hx-get` targeting a content container -- these bypass boost and correctly expect fragments. |
| **F10: Extract common template variables** | Every Resource manually retrieves `userName` and constructs `currentPage`. Could use a Qute `TemplateExtension` or a shared base method. Reduces boilerplate across 8 resources. | MEDIUM | Create a CDI bean that provides common template data (userName, currentPage derived from request path). Not HTMX-specific but reduces friction for the cleanup. |
| **F11: Standardize modal patterns across resources** | Current modal patterns are consistent but use different element IDs per resource (`gender-modal`, `gender-modal-body` vs `crud-modal`, `modal-content` in architecture doc). Standardize to a single pattern. | LOW | Choose one naming convention and apply consistently. Consider a shared Qute fragment for the modal shell. |
| **F12: Use `<dialog>` element for modals (optional, future)** | The HTML `<dialog>` element is well-supported (2025+) and provides built-in accessibility: focus trapping, Escape key to close, `showModal()` API. Reduces UIkit JS dependency for modals. | HIGH | Would require replacing UIkit modal JavaScript integration across all resources. Significant effort for marginal benefit since UIkit modals work correctly today. Defer unless UIkit is being removed. |
| **F13: Use `HX-Trigger` response header for decoupled updates** | Alternative to OOB swaps for modal CRUD success. Server returns `HX-Trigger: entityChanged` header; table container listens with `hx-trigger="entityChanged from:body"` and re-fetches itself. More decoupled than OOB -- modal is not coupled to page structure. | MEDIUM | Would change the CRUD success pattern fundamentally. OOB works well for this app's tightly-coupled pages. Consider only if modals need to be reusable across pages. |
| **F14: Configure `refreshOnHistoryMiss: true`** | When browser history cache misses occur (e.g., navigating many pages then pressing back), HTMX defaults to making an AJAX request which can return incorrect content. Setting `refreshOnHistoryMiss: true` forces a full page reload instead, which is simpler and more reliable. | LOW | Add `htmx.config.refreshOnHistoryMiss = true` in base template or via `meta` tag. Safety net for edge cases. |

### Anti-Features (Deliberately NOT Doing)

Features that seem good but create problems in this context.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| **Branching on `HX-Request` for navigation** | "Optimization" -- return fragment instead of full page for faster response | Breaks progressive enhancement (no JS = broken). Requires `Vary` header management. Makes server-side routing logic complex. Creates caching bugs. Confuses boosted navigation (which expects full page) with in-page updates (which expect fragments). This is the current antipattern. | Use `hx-boost` for navigation (server always returns full page). Use `hx-get` + `hx-target` for in-page partial updates only. |
| **SPA-style client-side routing** | "Make it feel like a React app" -- maintain persistent sidebar, swap only content area | Violates HTMX philosophy. Requires complex client-side state management for active nav, title, etc. Loses progressive enhancement. Makes back-button behavior unpredictable. | `hx-boost` provides SPA-like performance with MPA correctness. Full body swap is fast enough (HTMX optimizes by skipping `<head>` re-parsing). |
| **Using `hx-get` on every navigation link** | Fine-grained control over targets | Boilerplate explosion: every nav link needs `hx-get`, `hx-target`, `hx-push-url`, `hx-swap`. Loses progressive enhancement. `hx-boost="true"` on one parent element does the same thing automatically. | `hx-boost="true"` on navigation container. |
| **Client-side active-state JavaScript** | "Use `htmx:afterSettle` event to toggle CSS classes based on URL" | Unnecessary complexity. The server already knows which page is active (`currentPage` variable). With `hx-boost`, the sidebar re-renders from server on each navigation, so server-side class assignment works perfectly. | Server-rendered `currentPage` variable in `navigation.html` (already implemented). |
| **Eliminating OOB swaps entirely** | "OOB is too complex, use events instead" | The current OOB pattern for modal CRUD is correct and efficient for this app. OOB swaps update the table in the same response as closing the modal -- single round-trip. Event-based refresh requires an extra HTTP request. OOB is fine when modals are page-specific (not reused). | Keep OOB swaps for modal CRUD success patterns. They are idiomatic HTMX for this use case. |
| **Returning JSON from server for any HTMX endpoint** | "Return JSON and render client-side for flexibility" | Defeats the entire purpose of HTMX and hypermedia-driven architecture. Server returns HTML. Period. The one exception is `GraphResource.getGraphData()` which returns JSON for the D3/vis.js graph visualization -- that is a legitimate data API, not a UI endpoint. | Always return HTML from UI endpoints. JSON is only for non-HTMX data APIs (graph visualization). |

## Feature Dependencies

```
F1: Adopt hx-boost
    |
    +--- F3: Server returns full page (requires F1 to be meaningful)
    |       |
    |       +--- F4: Title tag handling (automatic once F1+F3 in place)
    |       |
    |       +--- F5: Active nav state (automatic once F1+F3 in place)
    |       |
    |       +--- F6: Browser history (automatic once F1 in place)
    |
    +--- F2: Remove HX-Request branching (requires F1 to replace the pattern)
            |
            +--- F9: Preserve partial updates for search/filter (refine F2 -- don't remove ALL branching)
            |
            +--- F7: Vary header (only needed if F9 retains any header-based branching)

F8: head-support extension (independent, can be done anytime)

F10: Extract common template variables (independent, reduces friction)

F11: Standardize modal patterns (independent, cosmetic)

F14: refreshOnHistoryMiss config (independent, safety net)
```

### Dependency Notes

- **F1 enables F2-F6:** Adopting `hx-boost` is the foundational change. Once nav links are boosted, the server no longer needs to branch for navigation requests. Title, active state, and history all work automatically.
- **F2 requires nuance from F9:** Not all `HX-Request` checks should be removed. Person search/filter/pagination genuinely returns fragments for in-page updates. The distinction is: navigation endpoints (list pages) stop branching; interaction endpoints (search, paginate) keep returning fragments but target specific containers.
- **F7 depends on F9:** The `Vary` header is only needed if some endpoints still return different content based on request headers. If all endpoints return full pages (simplest path), no `Vary` header is needed.
- **F8 is independent:** The head-support extension can be added at any time and improves `<head>` handling for boosted pages. Low risk.

## Fix Prioritization

### Phase 1: Foundation (Do First)

Must-fix items that establish the correct HTMX navigation pattern.

- [x] **F1: Adopt `hx-boost`** -- The single most impactful change. Add to navigation containers.
- [x] **F3: Server returns full page** -- Simplify resources by removing branching for nav.
- [x] **F14: Configure `refreshOnHistoryMiss`** -- Safety net, trivial to add.

### Phase 2: Cleanup (Do Next)

Remove the old branching pattern and handle edge cases.

- [x] **F2: Remove `HX-Request` branching from list endpoints** -- Clean up 6 resources.
- [x] **F9: Preserve partial updates for search/filter** -- Ensure Person pagination still works as fragment swap.
- [x] **F7: Add `Vary` header** -- If any branching remains.

### Phase 3: Polish (Do After)

Improvements that reduce maintenance burden.

- [x] **F8: Install head-support extension** -- Better `<head>` management.
- [x] **F10: Extract common template variables** -- DX improvement.
- [x] **F11: Standardize modal patterns** -- Consistency.

### Future Consideration (Defer)

- [ ] **F12: Use `<dialog>` element** -- Only if removing UIkit dependency.
- [ ] **F13: `HX-Trigger` for decoupled updates** -- Only if modals become cross-page.

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| F1: Adopt hx-boost | HIGH | LOW | P1 |
| F2: Remove HX-Request branching | HIGH | MEDIUM | P1 |
| F3: Server returns full page | HIGH | LOW | P1 |
| F4: Title tag handling | HIGH | NONE (automatic) | P1 |
| F5: Active nav state | HIGH | NONE (automatic) | P1 |
| F6: Browser history | HIGH | NONE (automatic) | P1 |
| F7: Vary header | MEDIUM | LOW | P2 |
| F8: head-support extension | MEDIUM | LOW | P2 |
| F9: Preserve partial updates | HIGH | MEDIUM | P2 |
| F10: Extract common variables | LOW | MEDIUM | P3 |
| F11: Standardize modal patterns | LOW | LOW | P3 |
| F14: refreshOnHistoryMiss | MEDIUM | LOW | P1 |

**Priority key:**
- P1: Correctness -- must fix for proper HTMX behavior
- P2: Should fix -- improves reliability and maintainability
- P3: Nice to have -- reduces boilerplate

## Current Antipattern Analysis

### What the Codebase Does Today

Every resource with a list endpoint follows this pattern:

```java
@GET
@Produces(MediaType.TEXT_HTML)
public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
    List<Entity> items = repository.listAllOrdered();

    if ("true".equals(hxRequest)) {
        return Templates.entity$table(items);  // Fragment
    }

    String userName = getCurrentUsername();
    return Templates.entity("Title", "page", userName, items);  // Full page
}
```

Navigation links in `navigation.html` are plain `<a href="/path">` tags with no HTMX attributes.

### What This Means

1. **Navigation is traditional full-page reload** -- clicking sidebar links causes complete page reload. No AJAX. No smooth transitions.
2. **The `HX-Request` branching is dead code for navigation** -- since nav links are plain `<a>` tags (not HTMX requests), the `HX-Request` check never triggers during navigation. It only triggers if something else (undefined) makes an HTMX request to the list endpoint.
3. **No progressive enhancement** -- the app is a traditional MPA with HTMX only for modal CRUD.

### What Should Change

1. Add `hx-boost="true"` to navigation containers in `base.html`.
2. Navigation links (`<a href="...">`) automatically become AJAX-powered.
3. HTMX sends full-page request with `HX-Boosted: true` header.
4. Server returns full page (no branching needed).
5. HTMX extracts `<body>` content, updates `<title>`, pushes URL to history.
6. Sidebar re-renders with correct active state from server.
7. Remove the `HX-Request` check from navigation endpoints.
8. Keep `hx-get`/`hx-target` for in-page interactions (modal CRUD, search, pagination).

### The Three-Header Decision Tree

After cleanup, server-side logic should follow this pattern:

| Request Type | Headers Present | Server Response |
|--------------|-----------------|-----------------|
| Direct browser navigation (or JS disabled) | None | Full HTML page |
| Boosted navigation via `hx-boost` | `HX-Request: true`, `HX-Boosted: true` | Full HTML page (HTMX extracts body) |
| In-page partial update (modal, search, pagination) | `HX-Request: true` only | HTML fragment targeting specific container |

For most endpoints, this simplifies to: **always return the full page**. The only endpoints that return fragments are those explicitly designed for in-page updates (modal forms, search results, pagination).

## Sources

- [htmx official docs: hx-boost](https://htmx.org/attributes/hx-boost/) -- HIGH confidence
- [htmx official docs: hx-push-url](https://htmx.org/attributes/hx-push-url/) -- HIGH confidence
- [htmx official docs: hx-swap-oob](https://htmx.org/attributes/hx-swap-oob/) -- HIGH confidence
- [htmx official docs: history support](https://htmx.org/docs/#history) -- HIGH confidence
- [htmx official docs: request/response headers](https://htmx.org/docs/#requests) -- HIGH confidence
- [htmx head-support extension](https://htmx.org/extensions/head-support/) -- HIGH confidence
- [htmx essay: template fragments](https://htmx.org/essays/template-fragments/) -- HIGH confidence
- [Hypermedia Systems book: htmx patterns](https://hypermedia.systems/htmx-patterns/) -- HIGH confidence
- [GitHub Discussion: Is hx-boosting the whole body a good practice?](https://github.com/bigskysoftware/htmx/discussions/2041) -- MEDIUM confidence
- [GitHub Discussion: Qute fragments vs hx-boost](https://github.com/quarkusio/quarkus/discussions/41114) -- MEDIUM confidence
- [GitHub Issue #497: hx-boost fragment vs full page](https://github.com/bigskysoftware/htmx/issues/497) -- MEDIUM confidence
- Codebase analysis of all 8 Resource classes and templates -- HIGH confidence

---
*Feature research for: HTMX cleanup patterns in Quarkus+Qute application*
*Researched: 2026-02-19*
