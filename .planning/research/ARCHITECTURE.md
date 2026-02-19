# Architecture Research: HTMX Cleanup Patterns

**Domain:** Quarkus + HTMX server-rendered application (HTMX antipattern cleanup)
**Researched:** 2026-02-19
**Confidence:** HIGH

## Standard Architecture

### System Overview: Current vs Target

```
CURRENT STATE (antipattern):
  Server checks HX-Request header --> branches to full page vs fragment
  Navigation uses plain <a> links --> full page reload on every click
  No hx-boost anywhere

TARGET STATE (correct HTMX):
  Server ALWAYS returns full page --> HTMX extracts content client-side
  Navigation uses hx-boost --> AJAX swap of #main-content only
  HX-Request header check REMOVED from all resources
```

### The Three Interaction Patterns

This application has three distinct interaction patterns that must coexist cleanly:

```
Pattern 1: Sidebar Navigation (hx-boost)
  Sidebar <a> click
    --> HTMX issues GET to href (AJAX, pushes URL)
    --> Server returns full HTML page (same as browser request)
    --> hx-select="#main-content" extracts just the content div
    --> hx-target="#main-content" swaps it in
    --> hx-swap="innerHTML" replaces inner content
    --> Title tag updated automatically by htmx
    --> Browser URL bar updated (hx-push-url from boost)

Pattern 2: Modal CRUD (hx-get/hx-post/hx-put/hx-delete)
  Button click (Add/Edit/Delete)
    --> HTMX issues request to endpoint
    --> Server returns fragment HTML (modal content)
    --> hx-target="#entity-modal-body" swaps into modal
    --> hx-on::after-request shows UIkit modal
    --> On success: OOB swap updates table, hx-on::load hides modal

Pattern 3: In-Page Table Refresh (hx-get with query params)
  Filter input / sort click / pagination click
    --> HTMX issues GET with query parameters
    --> Server returns table fragment
    --> hx-target="#entity-table-container" swaps table
    --> No URL change, no modal, just table update
```

### Component Responsibilities

| Component | Responsibility | Communicates With |
|-----------|----------------|-------------------|
| `base.html` | Layout shell: sidebar, main-content div, modal shells. Hosts `hx-boost`, `hx-select`, `hx-target` on the `<body>` tag | All page templates via `{#include base}` |
| `fragments/navigation.html` | Sidebar nav links with `currentPage` active state highlighting | `base.html` via `{#include fragments/navigation /}` |
| `*Resource.java` | Always returns full page `TemplateInstance`. No HX-Request branching for list endpoints. Fragment endpoints for modals remain unchanged | Templates, Repository/Service |
| `*.html` page templates | Full page content inserted into `base.html` content block. Fragments (`rendered=false`) for modal content and table partials | `base.html`, `*Resource.java` |

## Recommended Architecture

### Pattern 1: Sidebar Navigation with hx-boost

**Confidence:** HIGH (verified via htmx.org official docs + GitHub issue #983)

The key insight: **the server always returns a full page**. HTMX's `hx-select` attribute extracts only the content div client-side. This eliminates the need for `@HeaderParam("HX-Request")` branching in list endpoints.

#### base.html Changes

```html
<body hx-boost="true"
      hx-target="#main-content"
      hx-select="#main-content"
      hx-swap="innerHTML">
```

These four attributes on `<body>` mean:

1. **`hx-boost="true"`** -- All `<a>` tags and `<form>` tags within `<body>` become AJAX requests automatically
2. **`hx-target="#main-content"`** -- Boosted responses swap into `#main-content` instead of `<body>`
3. **`hx-select="#main-content"`** -- From the full-page response, extract only the `#main-content` div's contents
4. **`hx-swap="innerHTML"`** -- Replace the inner content of the target (not the element itself)

Boosted anchor clicks also push the URL to browser history, so the address bar stays in sync.

#### Navigation Links: No Changes Needed

The current `fragments/navigation.html` uses plain `<a href="/genders">` links. With `hx-boost="true"` on `<body>`, these are automatically converted to AJAX requests. No `hx-get`, `hx-target`, or `hx-push-url` attributes needed on individual links.

#### Active State Problem and Solution

The `currentPage` variable drives sidebar active state via `{#if currentPage?? == 'gender'}uk-active{/if}`. With `hx-boost`, the sidebar HTML is NOT re-rendered (only `#main-content` changes). The active state becomes stale.

**Solution: Use `hx-select-oob` to update navigation**

Add a navigation wrapper with an ID in `fragments/navigation.html`:

```html
<div id="sidebar-nav">
    <ul class="uk-nav uk-nav-default uk-padding-small" uk-nav>
        <li class="{#if currentPage?? == 'home'}uk-active{/if}">
            <a href="/">...</a>
        </li>
        ...
    </ul>
</div>
```

Then on `<body>`, add:

```html
<body hx-boost="true"
      hx-target="#main-content"
      hx-select="#main-content"
      hx-swap="innerHTML"
      hx-select-oob="#sidebar-nav">
```

The `hx-select-oob="#sidebar-nav"` tells htmx: "From the full-page response, also extract `#sidebar-nav` and swap it into the existing `#sidebar-nav` in the DOM." This keeps the active navigation state in sync without any JavaScript.

**Important:** The sidebar nav appears twice in `base.html` (desktop and mobile offcanvas). Both need wrapper IDs. Use `#sidebar-nav` for desktop and `#mobile-sidebar-nav` for mobile:

```html
hx-select-oob="#sidebar-nav,#mobile-sidebar-nav"
```

#### Resource Changes: Remove HX-Request Branching

**Before (antipattern):**

```java
@GET
@Produces(MediaType.TEXT_HTML)
public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
    List<Gender> genders = genderRepository.listAllOrdered();
    if ("true".equals(hxRequest)) {
        return Templates.gender$table(genders);  // Fragment
    }
    String userName = getCurrentUsername();
    return Templates.gender("Gender Management", "gender", userName, genders);  // Full page
}
```

**After (correct):**

```java
@GET
@Produces(MediaType.TEXT_HTML)
public TemplateInstance list() {
    List<Gender> genders = genderRepository.listAllOrdered();
    String userName = getCurrentUsername();
    return Templates.gender("Gender Management", "gender", userName, genders);
}
```

The `@HeaderParam("HX-Request")` parameter and the branching logic are removed. The server always returns the full page. HTMX's `hx-select` extracts `#main-content` from the response.

### Pattern 2: Modal CRUD with OOB Swaps

**Confidence:** HIGH (codebase already implements this correctly)

Modal CRUD is the one pattern the current codebase handles well. The flow is:

```
1. User clicks Add/Edit/Delete button
   Button has: hx-get="/genders/create"
               hx-target="#gender-modal-body"
               hx-on::after-request="UIkit.modal('#gender-modal').show()"

2. Server returns fragment (modal_create/modal_edit/modal_delete)
   Fragment is injected into #gender-modal-body
   UIkit modal is shown

3. User submits form inside modal
   Form has: hx-post="/genders"
             hx-target="#gender-modal-body"

4. Server returns success fragment
   Fragment contains:
   - <div hx-on::load="UIkit.modal('#gender-modal').hide()"> (closes modal)
   - <div id="gender-table-container" hx-swap-oob="innerHTML"> (updates table)
```

#### Critical: Preventing hx-boost Inheritance in Modals

When `hx-boost="true"` is set on `<body>`, it is inherited by ALL child elements -- including buttons and forms inside modals. This would break modal CRUD because:

- Modal forms would be "boosted" (treated as navigation) instead of targeting `#gender-modal-body`
- The inherited `hx-select="#main-content"` would try to extract `#main-content` from a fragment response that does not contain it

**Solution: Use `hx-boost="false"` on the modal shell**

```html
<div id="gender-modal" uk-modal="bg-close: false" hx-boost="false">
    <div class="uk-modal-dialog uk-modal-container">
        <div id="gender-modal-body" class="uk-modal-body">
            <!-- Content loaded dynamically via HTMX -->
        </div>
    </div>
</div>
```

Setting `hx-boost="false"` on the modal container prevents boost inheritance for all elements inside it. The explicit `hx-get`, `hx-post`, `hx-put`, `hx-delete` attributes on buttons and forms continue to work normally because they are not affected by the boost toggle -- they are explicit HTMX requests, not "boosted" standard HTML elements.

**Additionally**, buttons that trigger modal opens (outside the modal) need `hx-select="unset"` to prevent the inherited `hx-select="#main-content"` from filtering their fragment responses:

```html
<button hx-get="/genders/create"
        hx-target="#gender-modal-body"
        hx-select="unset"
        hx-swap="innerHTML"
        hx-on::after-request="UIkit.modal('#gender-modal').show()">
    <span uk-icon="plus"></span>
</button>
```

The same applies to Edit/Delete buttons in the table rows. All buttons with `hx-get` that target modal content need `hx-select="unset"` and explicit `hx-target` and `hx-swap` to override the inherited boost defaults.

### Pattern 3: In-Page Table Refresh

**Confidence:** HIGH (verified against htmx docs)

Table refresh (filter, sort, pagination) currently works via HTMX requests that return table fragments. After the cleanup, these continue to work but need the same inheritance overrides as modal buttons.

#### Current Pattern (Person table with filter/pagination)

```html
<input type="text" name="filter"
       hx-get="/persons"
       hx-target="#person-table-container"
       hx-trigger="keyup changed delay:300ms"
       hx-include="[name='size']" />
```

#### After Cleanup: Add hx-select="unset"

```html
<input type="text" name="filter"
       hx-get="/persons"
       hx-target="#person-table-container"
       hx-select="unset"
       hx-swap="innerHTML"
       hx-trigger="keyup changed delay:300ms"
       hx-include="[name='size']" />
```

**However**, there is a subtlety here. The Person resource list endpoint currently branches on `HX-Request` to return either the full page or just the table fragment. After removing HX-Request branching, the server always returns the full page. But the filter/pagination requests need just the table.

**Two approaches to resolve this:**

**Approach A (Recommended): Keep a separate table-only endpoint**

Add a dedicated endpoint that returns only the table fragment, separate from the page endpoint:

```java
@GET
@Path("/table")
@Produces(MediaType.TEXT_HTML)
public TemplateInstance table(
        @QueryParam("filter") String filter,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("25") int size) {
    // Returns table fragment only
    PanacheQuery<Person> query = personRepository.findByFilterPaged(filter, null, null);
    query.page(Page.of(page, size));
    return Templates.person$table(query.list(), filter, page, size, ...);
}
```

Then the filter input targets `/persons/table` instead of `/persons`:

```html
<input hx-get="/persons/table"
       hx-target="#person-table-container"
       hx-swap="innerHTML" />
```

This cleanly separates page navigation (GET `/persons` returns full page) from in-page updates (GET `/persons/table` returns fragment). No `hx-select` override needed on the filter because the response is already a fragment.

**Approach B: Use hx-select on the filter to extract the table from a full page**

```html
<input hx-get="/persons"
       hx-target="#person-table-container"
       hx-select="#person-table-container"
       hx-swap="innerHTML" />
```

This works but wastes bandwidth (full page sent, most discarded) and requires the server to re-query all data needed for the full page on every keystroke. Not recommended for pagination-heavy resources.

**Recommendation: Use Approach A for resources with filter/sort/pagination (Person, PersonRelationship). For simple lookup tables (Gender, Title, Relationship) that refresh via OOB swaps on modal success, no separate table endpoint is needed -- the existing OOB pattern handles it.**

## Data Flow Diagrams

### Flow 1: Sidebar Navigation (hx-boost)

```
User clicks "People" in sidebar
    |
    v
<a href="/persons"> intercepted by hx-boost
    |
    v
HTMX issues GET /persons (AJAX, HX-Request: true header sent)
    |
    v
PersonResource.list() returns full page TemplateInstance
    (title="Person Management", currentPage="persons", ...)
    |
    v
Server renders complete HTML: <html><head>...</head><body>...<div id="main-content">...</div>...</body></html>
    |
    v
HTMX receives full HTML response
    |
    +---> hx-select="#main-content" extracts <div id="main-content">...</div>
    |     hx-target="#main-content" + hx-swap="innerHTML" replaces content
    |
    +---> hx-select-oob="#sidebar-nav" extracts updated sidebar nav
    |     Swaps into existing #sidebar-nav (active state updated)
    |
    +---> hx-select-oob="#mobile-sidebar-nav" extracts mobile nav
    |     Swaps into existing #mobile-sidebar-nav
    |
    +---> <title> tag processed: document.title updated
    |
    +---> URL pushed to browser history: /persons
```

### Flow 2: Modal CRUD (Create Example)

```
User clicks "+" button (Add Gender)
    |
    v
<button hx-get="/genders/create"
        hx-target="#gender-modal-body"
        hx-select="unset"
        hx-swap="innerHTML"
        hx-on::after-request="UIkit.modal('#gender-modal').show()">
    |
    v
HTMX issues GET /genders/create
    |
    v
GenderResource.createForm() returns gender$modal_create fragment
    |
    v
Fragment HTML injected into #gender-modal-body
UIkit.modal('#gender-modal').show() called
    |
    v
User fills form, clicks Save
    |
    v
<form hx-post="/genders" hx-target="#gender-modal-body">
    |
    v
GenderResource.create() validates and persists
    |
    +---> Validation error: returns gender$modal_create with error
    |     Re-renders form in modal with error message
    |
    +---> Success: returns gender$modal_success fragment
          |
          +---> <div hx-on::load="UIkit.modal('#gender-modal').hide()">
          |     Modal closes
          |
          +---> <div id="gender-table-container" hx-swap-oob="innerHTML">
                Table refreshed with current data
```

### Flow 3: In-Page Table Refresh (Person filter)

```
User types in filter input
    |
    v
<input hx-get="/persons/table"
       hx-target="#person-table-container"
       hx-swap="innerHTML"
       hx-trigger="keyup changed delay:300ms"
       hx-include="[name='size']">
    |
    v
HTMX issues GET /persons/table?filter=john&size=25
    |
    v
PersonResource.table() returns person$table fragment
    |
    v
Fragment HTML replaces #person-table-container innerHTML
```

## Architectural Patterns

### Pattern 1: Always-Full-Page Resources

**What:** Every GET endpoint that serves a "page" returns the complete HTML page including base layout. No HX-Request branching.

**When to use:** All page-level GET endpoints (list views, detail views, graph views).

**Trade-offs:**
- Pro: Simpler server logic, no branching, every URL works for direct browser access and bookmarking
- Pro: Progressive enhancement -- works without JavaScript
- Con: Slightly larger response payload for boosted navigation (full page instead of fragment)
- This is the intended htmx pattern; the payload overhead is negligible for server-rendered HTML

### Pattern 2: Fragment-Only Endpoints

**What:** Dedicated endpoints that return only Qute fragments (no base layout wrapper).

**When to use:**
- Modal content endpoints (`/entities/create`, `/entities/{id}/edit`, `/entities/{id}/delete`)
- Table-only endpoints for in-page refresh (`/entities/table` for filter/sort/pagination)
- Form submission endpoints that return success/error fragments

**Trade-offs:**
- Pro: Minimal payload, fast swap
- Pro: Clear separation -- these URLs are not navigable pages
- Con: Cannot be bookmarked or accessed directly in browser

### Pattern 3: OOB Swaps for Multi-Region Updates

**What:** Response HTML contains elements with `hx-swap-oob` that update DOM regions outside the primary target.

**When to use:** After CRUD success -- close modal AND refresh table in one response.

**Trade-offs:**
- Pro: Single request updates multiple DOM regions atomically
- Pro: No client-side JavaScript coordination needed
- Con: Response includes extra HTML for OOB targets

## Anti-Patterns

### Anti-Pattern 1: HX-Request Header Branching

**What people do:** Check `@HeaderParam("HX-Request")` in the list endpoint to branch between full page and fragment rendering.

**Why it's wrong:**
- Creates two code paths for the same URL
- Server must know whether the client used AJAX -- violates hypermedia architecture
- Breaks when hx-boost is introduced (boosted links send HX-Request but need full page treatment via hx-select)
- Makes testing harder (must test both paths)

**Do this instead:** Always return the full page. Let HTMX's `hx-select` extract the needed portion client-side. If you need a fragment-only response for in-page updates, create a separate endpoint (e.g., `/entities/table`).

### Anti-Pattern 2: Using hx-boost Without hx-select

**What people do:** Add `hx-boost="true"` on `<body>` without `hx-target` and `hx-select`, causing the entire body innerHTML to be replaced.

**Why it's wrong:** Replaces the entire page body on every navigation, including sidebar, header, footer -- defeating the purpose of partial updates. Also causes UIkit JavaScript state loss (open dropdowns, scroll position).

**Do this instead:** Always pair `hx-boost` with `hx-target="#main-content"`, `hx-select="#main-content"`, and `hx-swap="innerHTML"`.

### Anti-Pattern 3: Forgetting to Override Inherited hx-select in Modals

**What people do:** Set `hx-select="#main-content"` on `<body>` but forget that modal CRUD buttons inherit this attribute.

**Why it's wrong:** When a button does `hx-get="/genders/create"` and the response is a fragment (no `#main-content` element), `hx-select` finds nothing, and the modal body gets empty content.

**Do this instead:** Add `hx-boost="false"` on modal containers, and `hx-select="unset"` on all buttons/elements that trigger modal content loads.

### Anti-Pattern 4: Using hx-boost on Forms Inside Modals

**What people do:** Allow `hx-boost="true"` to be inherited by forms inside modals.

**Why it's wrong:** Boosted forms target the body by default. A modal form submission would replace the page content instead of updating the modal body. Also, boosted forms do not push the URL by default, creating inconsistent behavior.

**Do this instead:** Place `hx-boost="false"` on the modal shell element so all forms and links inside it use their explicit `hx-post`/`hx-put`/`hx-delete` attributes.

## Change Order (Dependencies)

The changes must be applied in a specific order to avoid breaking the application at any intermediate step.

### Phase 1: Add Navigation Wrapper IDs (non-breaking)

**What changes:** `fragments/navigation.html` -- wrap content in `<div id="sidebar-nav">`. `base.html` -- wrap the mobile copy in `<div id="mobile-sidebar-nav">`.

**Why first:** This is a purely additive change. Existing behavior is unaffected. The IDs are needed for `hx-select-oob` in Phase 3.

**Affected files:**
- `src/main/resources/templates/fragments/navigation.html`
- `src/main/resources/templates/base.html`

### Phase 2: Add hx-boost="false" on Modal Shells (non-breaking)

**What changes:** Every page template's modal `<div>` gets `hx-boost="false"`.

**Why second:** Must be in place before `hx-boost="true"` is added to `<body>` in Phase 3, otherwise modal CRUD breaks immediately.

**Affected files:**
- `src/main/resources/templates/GenderResource/gender.html`
- `src/main/resources/templates/TitleResource/title.html`
- `src/main/resources/templates/RelationshipResource/relationship.html`
- `src/main/resources/templates/PersonResource/person.html`
- `src/main/resources/templates/PersonRelationshipResource/personRelationship.html`
- `src/main/resources/templates/GraphResource/graph.html` (if it has modals)

### Phase 3: Add hx-select="unset" on Modal Trigger Buttons (non-breaking)

**What changes:** All buttons with `hx-get` that target a modal body get `hx-select="unset"` and explicit `hx-swap="innerHTML"`.

**Why third:** Must be in place before `hx-select="#main-content"` is added to `<body>`, otherwise fragment responses would be filtered by the inherited `hx-select` and return empty content.

**Affected files:** Same template files as Phase 2, specifically:
- "Add" buttons at the top of each page
- "Edit" buttons in table rows
- "Delete" buttons in table rows

### Phase 4: Enable hx-boost on body (breaking change for navigation, requires Phase 1-3)

**What changes:** `base.html` `<body>` tag gets `hx-boost="true" hx-target="#main-content" hx-select="#main-content" hx-swap="innerHTML" hx-select-oob="#sidebar-nav,#mobile-sidebar-nav"`.

**Why fourth:** This is the big switch. After this, sidebar navigation becomes AJAX-powered. Requires Phase 1 (nav IDs exist for OOB), Phase 2 (modals are not boosted), Phase 3 (modal buttons override inherited hx-select).

**Affected files:**
- `src/main/resources/templates/base.html`

### Phase 5: Remove HX-Request Branching from Resources

**What changes:** Remove `@HeaderParam("HX-Request")` parameter and the `if ("true".equals(hxRequest))` branching from all resource list endpoints. For resources with filter/pagination, add a separate `/table` endpoint.

**Why fifth:** After Phase 4, boosted navigation requests send `HX-Request: true` but the server's response is the same either way (full page). The branching code is now dead code. Removing it simplifies the resources. For Person and PersonRelationship, the filter/sort/pagination pattern needs a dedicated `/table` endpoint so in-page refresh still returns fragments.

**Affected files:**
- `src/main/java/.../router/GenderResource.java` -- remove HX-Request branch
- `src/main/java/.../router/TitleResource.java` -- remove HX-Request branch
- `src/main/java/.../router/RelationshipResource.java` -- remove HX-Request branch
- `src/main/java/.../router/PersonResource.java` -- remove HX-Request branch, add `/table` endpoint
- `src/main/java/.../router/PersonRelationshipResource.java` -- remove HX-Request branch (already uses query params)
- `src/main/java/.../router/GraphResource.java` -- remove HX-Request branch from `showPersonNetwork`

### Phase 6: Update Filter/Sort/Pagination Targets

**What changes:** Update `hx-get` URLs in Person and PersonRelationship templates to point to the new `/table` endpoint instead of the main list endpoint.

**Why sixth:** After Phase 5 creates the `/table` endpoints, the templates must be updated to use them for in-page refresh.

**Affected files:**
- `src/main/resources/templates/PersonResource/person.html`
- `src/main/resources/templates/PersonRelationshipResource/personRelationship.html`

### Phase 7: Update Architecture Documentation

**What changes:** Update `docs/ARCHITECTURE.md` sections 7 (Resource Layer) and 9 (HTMX Integration) to reflect the new patterns. Remove the HX-Request branching examples. Document the hx-boost pattern.

**Affected files:**
- `docs/ARCHITECTURE.md`

## Integration Points

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| `base.html` body attributes <-> page templates | Attribute inheritance via DOM | `hx-boost`, `hx-select`, `hx-target` inherited by all children; must be overridden in modal contexts |
| Navigation fragment <-> `hx-select-oob` | OOB swap by element ID | `#sidebar-nav` and `#mobile-sidebar-nav` must match exactly between `hx-select-oob` value and actual DOM IDs |
| Resource endpoints <-> template fragments | `@CheckedTemplate` contract | Fragment endpoints remain unchanged; only list endpoints change (remove branching) |
| Modal buttons <-> inherited attributes | `hx-select="unset"` override | Every button with `hx-get` targeting a modal must explicitly unset inherited `hx-select` |

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| UIkit JavaScript | `hx-on::after-request` / `hx-on::load` | UIkit modal show/hide. Must survive content swaps. Modal shell stays in DOM; only inner content changes |
| Browser History API | `hx-boost` push-url | Automatically handled by htmx for boosted links. Back/forward restores from htmx history cache |

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| Current (prototype) | Single deployment, dev mode. Approach A (separate `/table` endpoint) for Person is sufficient |
| 0-1k users | No changes needed. Full-page responses are tiny (< 50KB). hx-boost eliminates redundant asset loads |
| 1k+ users | Consider HTTP caching headers on full-page responses. Consider `hx-history` configuration for cache size |

### Scaling Priorities

1. **First bottleneck:** N+1 query on Person list (already addressed with Entity Graphs). Not affected by HTMX cleanup.
2. **Second bottleneck:** Full-page response size for boosted navigation. Mitigated by HTTP compression (gzip/brotli) which Quarkus supports out of the box.

## Sources

- [htmx hx-boost Documentation](https://htmx.org/attributes/hx-boost/) -- HIGH confidence (official docs)
- [htmx hx-select Documentation](https://htmx.org/attributes/hx-select/) -- HIGH confidence (official docs)
- [htmx hx-select-oob Documentation](https://htmx.org/attributes/hx-select-oob/) -- HIGH confidence (official docs)
- [htmx hx-disinherit Documentation](https://htmx.org/attributes/hx-disinherit/) -- HIGH confidence (official docs)
- [GitHub Issue #983: hx-boost but only replacing parts of the page](https://github.com/bigskysoftware/htmx/issues/983) -- HIGH confidence (official repo, confirmed solution)
- [htmx Quirks Page](https://htmx.org/quirks/) -- HIGH confidence (official docs, documents known caveats)
- [htmx Main Documentation](https://htmx.org/docs/) -- HIGH confidence (official docs)
- Codebase analysis of 6 affected resources and their templates -- HIGH confidence (direct code inspection)

---
*Architecture research for: Quarkus+HTMX cleanup patterns*
*Researched: 2026-02-19*
