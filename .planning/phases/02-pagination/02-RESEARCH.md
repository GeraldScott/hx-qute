# Phase 2: Pagination - Research

**Researched:** 2026-02-14
**Domain:** Server-side pagination with Panache, HTMX partial updates, UIkit pagination component
**Confidence:** HIGH

## Summary

Phase 2 adds pagination to the person list, replacing the current unbounded `findByFilter()` query with paginated results. The implementation touches three layers: the repository (Panache `PanacheQuery` with `Page`), the resource (passing page/size query parameters and pagination metadata to templates), and the template (rendering UIkit pagination controls with HTMX attributes for partial page updates).

Panache provides first-class pagination support via `PanacheQuery`. The `find()` method on `PanacheRepository` returns a `PanacheQuery` object that supports `.page(Page.of(index, size))`, `.pageCount()`, `.count()`, `.hasNextPage()`, and `.hasPreviousPage()`. This eliminates the need for any custom pagination logic. The current `PersonRepository.findByFilter()` method returns `List<Person>` directly -- it must be refactored to return a `PanacheQuery<Person>` (or the resource must call `find()` and apply pagination before calling `.list()`).

On the frontend, HTMX pagination is straightforward: each page link uses `hx-get="/persons?page=N&size=S&filter=..."` targeting the table container. The server detects `HX-Request` header and returns only the table fragment with embedded pagination controls. UIkit provides a `uk-pagination` CSS component with `uk-active` and `uk-disabled` states. Combined with HTMX's `hx-push-url="true"`, the pagination state remains bookmarkable and shareable.

**Primary recommendation:** Use Panache's built-in `PanacheQuery.page()` API for database-level pagination, render UIkit `uk-pagination` controls in the table fragment, and wire each page link with `hx-get` targeting the table container. Pass pagination metadata (currentPage, totalPages, pageSize, totalCount) to the template as simple scalar values -- no custom DTO needed for the template layer.

## Standard Stack

### Core

No new libraries or dependencies needed. This phase uses only what is already in the project.

| Library | Version | Purpose | Already Present |
|---------|---------|---------|-----------------|
| Quarkus Hibernate ORM Panache | 3.30.3 | `PanacheQuery` with `Page` for database-level pagination | Yes |
| Quarkus REST | 3.30.3 | `@QueryParam` for page/size parameters | Yes |
| Qute Templates | 3.30.3 | Render pagination controls with template logic | Yes |
| HTMX | 2.0.8 | `hx-get` for partial table/pagination updates | Yes |
| UIkit | 3.25.4 | `uk-pagination` CSS component | Yes |

### Supporting

None needed. No new dependencies.

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Panache `PanacheQuery.page()` | Manual SQL `LIMIT/OFFSET` | Panache handles count query, page math, and boundary checks automatically |
| UIkit `uk-pagination` | Custom pagination HTML | UIkit provides accessible, styled pagination out of the box with `uk-active`/`uk-disabled` states |
| Numbered page navigation | Infinite scroll (`hx-trigger="revealed"`) | Numbered pages are bookmarkable, show total count, and match the investigation tool's use case where users need to navigate to specific pages |
| Numbered page navigation | Click-to-load ("Load More" button) | Click-to-load grows the DOM unboundedly; numbered pages keep the table at a fixed size |

**Installation:** No installation needed.

## Architecture Patterns

### Recommended Project Structure

All changes are within existing files. No new files are needed.

```
src/main/java/.../repository/
    PersonRepository.java       # MODIFY: Add paginated query method
src/main/java/.../router/
    PersonResource.java         # MODIFY: Add page/size @QueryParam, pass pagination metadata to templates
src/main/resources/templates/PersonResource/
    person.html                 # MODIFY: Add pagination controls to table fragment, update @CheckedTemplate
```

### Pattern 1: Panache PanacheQuery Pagination

**What:** Use Panache's `PanacheQuery` API to execute paginated database queries with automatic count queries and page metadata.
**When to use:** Whenever listing entities with more rows than fit comfortably on one page.

```java
// Source: https://quarkus.io/guides/hibernate-orm-panache (Paging section)
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;

// In PersonRepository -- paginated variant of findByFilter
public PanacheQuery<Person> findByFilterPaged(String filterText, String sortField, String sortDir) {
    String orderBy = buildOrderBy(sortField, sortDir);

    if (filterText != null && !filterText.isBlank()) {
        String pattern = "%" + filterText.toLowerCase().trim() + "%";
        return find(
            "LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1 OR LOWER(email) LIKE ?1 " + orderBy,
            pattern
        );
    }
    return find("FROM Person " + orderBy);
}
```

```java
// In PersonResource -- using the paginated query
PanacheQuery<Person> query = personRepository.findByFilterPaged(filter, sortField, sortDir);
query.page(Page.of(page, size));  // page is 0-indexed

List<Person> persons = query.list();
int totalPages = query.pageCount();
long totalCount = query.count();
boolean hasNext = query.hasNextPage();
boolean hasPrevious = query.hasPreviousPage();
```

**Key facts about Panache pagination (verified):**
- `Page.of(index, size)` is **0-indexed** -- first page is `Page.of(0, 25)`, not `Page.of(1, 25)`
- `Page.ofSize(size)` creates first page (index 0) with given size
- `PanacheQuery.pageCount()` returns total number of pages
- `PanacheQuery.count()` returns total entity count
- `PanacheQuery.hasNextPage()` and `hasPreviousPage()` return boolean
- `PanacheQuery` is reusable -- you can call `.page()` and then `.list()` multiple times

### Pattern 2: HTMX Pagination with Query Parameters

**What:** Server-rendered pagination links with `hx-get` attributes that include all state (page, size, filter, sort) as query parameters.
**When to use:** For server-side pagination with HTMX partial updates.

```html
<!-- Each pagination link includes all state in the URL -->
<a hx-get="/persons?page=2&size=25&filter=smith&sortField=lastName&sortDir=asc"
   hx-target="#person-table-container"
   hx-push-url="true">
    3
</a>
```

**Key HTMX attributes for pagination:**
- `hx-get` -- URL with all parameters (page, size, filter, sort)
- `hx-target="#person-table-container"` -- replace table + pagination controls
- `hx-push-url="true"` -- keep browser URL in sync for bookmarkability
- Server detects `HX-Request` header and returns only the table fragment (already implemented in `PersonResource.list()`)

### Pattern 3: UIkit Pagination Component

**What:** UIkit's `uk-pagination` CSS component for rendering page navigation controls.
**When to use:** For styled, accessible pagination controls.

```html
<!-- Source: https://getuikit.com/docs/pagination -->
<nav aria-label="Pagination">
    <ul class="uk-pagination uk-flex-center" uk-margin>
        <!-- Previous button -->
        <li class="{#if currentPage == 0}uk-disabled{/if}">
            <a hx-get="/persons?page={currentPage - 1}&size={pageSize}"
               hx-target="#person-table-container"
               hx-push-url="true">
                <span uk-pagination-previous></span>
            </a>
        </li>

        <!-- Page numbers (server-rendered) -->
        {#for i in totalPages}
        <li class="{#if currentPage == i}uk-active{/if}">
            <a hx-get="/persons?page={i}&size={pageSize}"
               hx-target="#person-table-container"
               hx-push-url="true">
                {i + 1}
            </a>
        </li>
        {/for}

        <!-- Next button -->
        <li class="{#if currentPage >= totalPages - 1}uk-disabled{/if}">
            <a hx-get="/persons?page={currentPage + 1}&size={pageSize}"
               hx-target="#person-table-container"
               hx-push-url="true">
                <span uk-pagination-next></span>
            </a>
        </li>
    </ul>
</nav>
```

**UIkit pagination CSS classes:**
- `uk-pagination` -- base class on `<ul>`
- `uk-active` -- highlights current page (use `<span>` not `<a>`)
- `uk-disabled` -- grays out non-clickable items (use `<span>` not `<a>`)
- `uk-flex-center` -- centers the pagination controls
- `uk-pagination-previous` / `uk-pagination-next` -- arrow icons
- `aria-label="Pagination"` and `aria-current="page"` for accessibility

### Pattern 4: Page Size Selector

**What:** A `<select>` dropdown that lets the user choose results per page (10, 25, 50, 100).
**When to use:** When the requirement specifies configurable page sizes.

```html
<select class="uk-select uk-form-small uk-form-width-small"
        name="size"
        hx-get="/persons"
        hx-target="#person-table-container"
        hx-include="closest form"
        hx-push-url="true">
    <option value="10" {#if pageSize == 10}selected{/if}>10</option>
    <option value="25" {#if pageSize == 25}selected{/if}>25</option>
    <option value="50" {#if pageSize == 50}selected{/if}>50</option>
    <option value="100" {#if pageSize == 100}selected{/if}>100</option>
</select>
```

### Pattern 5: URL-Driven State Preservation

**What:** All pagination, filter, and sort state lives in URL query parameters. The URL is the single source of truth.
**When to use:** Always, for server-side rendered applications.

The current filter bar already uses `hx-push-url="true"` and `hx-include="closest form"` to preserve filter/sort state. Pagination must integrate with this pattern:

- Filter form includes hidden `page` input (reset to 0 on new filter/sort change)
- Pagination links include all current filter/sort parameters
- Changing page size resets to page 0
- `hx-push-url="true"` on all pagination and filter interactions

### Anti-Patterns to Avoid

- **Client-side pagination state:** Do not store current page in JavaScript variables. URL parameters are the single source of truth. Server reads them on each request.
- **Separate count query endpoint:** Do not make a separate AJAX call to get total count. Panache's `PanacheQuery.pageCount()` handles this and the count is passed to the template in the same response.
- **Rendering all page numbers for large datasets:** For many pages (e.g., 100+), render an ellipsis pattern (1 ... 5 6 **7** 8 9 ... 100) instead of all page numbers.
- **Mixing `range()` and `page()` on PanacheQuery:** Panache explicitly throws an exception if you mix range-based and page-based access on the same query.
- **Forgetting to reset page to 0 when filter changes:** When the user types a new search term, the page must reset to 0 or they may request a page that does not exist for the filtered result set.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Paginated queries | Manual `LIMIT/OFFSET` with raw JPQL | `PanacheQuery.page(Page.of(index, size))` | Panache handles count, page math, boundary checks, and generates correct SQL |
| Pagination UI component | Custom `<div>` with manual styling | UIkit `uk-pagination` CSS class | Accessible, styled, supports active/disabled states out of the box |
| Page number calculation | Manual `totalCount / pageSize` math | `PanacheQuery.pageCount()` | Handles rounding, edge cases (0 results), and is tested |
| Boundary detection | Manual `page > 0` / `page < max` checks | `PanacheQuery.hasNextPage()` / `hasPreviousPage()` | Panache handles empty result sets and single-page cases |

**Key insight:** Panache's `PanacheQuery` is a complete pagination toolkit. All the math, SQL generation, and boundary detection is built in. The implementation work is primarily in wiring parameters through the resource to the template.

## Common Pitfalls

### Pitfall 1: 0-indexed vs 1-indexed Page Numbers

**What goes wrong:** The UI shows "Page 1" but sends `page=1` to the server, which Panache treats as the second page (0-indexed), skipping the first page of results.
**Why it happens:** Panache `Page.of(index, size)` is 0-indexed, but users expect to see "Page 1" as the first page.
**How to avoid:** Use 0-indexed page numbers in all URL parameters and internal logic. Convert to 1-indexed only for display: `{currentPage + 1}` in templates. The server always receives and sends 0-indexed values.
**Warning signs:** First page shows records starting from row `pageSize + 1` instead of row 1.

### Pitfall 2: Filter Change Without Page Reset

**What goes wrong:** User is on page 5, types a new filter, gets an empty table because the filtered results have fewer than 5 pages.
**Why it happens:** The filter form submits the current page number along with the new filter text.
**How to avoid:** When the filter text or sort order changes, always reset `page=0`. This can be done by: (a) not including the page parameter in the filter form's `hx-include`, or (b) using a hidden input for page that resets to 0 when the form submits, or (c) having the filter input's own `hx-get` not include the page parameter.
**Warning signs:** Empty table after filtering when records clearly exist.

### Pitfall 3: Pagination Controls Inside vs Outside the Table Fragment

**What goes wrong:** Pagination controls render on initial page load but disappear after an HTMX table refresh, or pagination controls are duplicated.
**Why it happens:** If pagination controls are outside the `table` fragment but the HTMX response only replaces the fragment content, the controls become stale or vanish.
**How to avoid:** The pagination controls MUST be inside the `table` fragment (or inside the container that gets replaced by `hx-target`). The `person-table-container` div is the HTMX target, and both the table and pagination controls must be inside it.
**Warning signs:** Pagination works on first load but becomes non-functional or shows stale page numbers after navigating.

### Pitfall 4: Forgetting to Pass Pagination Params in the modal_success Fragment

**What goes wrong:** After creating or editing a person, the OOB table refresh shows all results without pagination, reverting to unpaginated behavior.
**Why it happens:** The `modal_success` fragment does an OOB swap of `person-table-container`. If the success handler queries `findByFilter(null, null, null)` without pagination, the OOB response contains unpaginated results.
**How to avoid:** After create/update success, the OOB table refresh must use the same paginated query. Either: (a) re-query with default pagination (page 0, default size), or (b) pass pagination state through the create/update flow. Option (a) is simpler and sufficient -- after a mutation, show page 1 with default size.
**Warning signs:** Creating a person causes the table to suddenly show all records without pagination.

### Pitfall 5: Page Size Select Not Preserving Other Parameters

**What goes wrong:** Changing the page size dropdown loses the current filter text and sort order.
**Why it happens:** The page size `<select>` triggers its own `hx-get` without including the filter form's other inputs.
**How to avoid:** The page size selector should be inside the filter form and use `hx-include="closest form"` to send all form values. Alternatively, place the page size `<select>` within the existing filter form rather than as a standalone control.
**Warning signs:** Changing from "25 per page" to "50 per page" clears the search filter.

### Pitfall 6: Ellipsis Logic for Many Pages

**What goes wrong:** With 500 records at 10 per page, the pagination renders 50 clickable page numbers, creating an unusable UI.
**Why it happens:** Naively iterating `{#for i in totalPages}` renders every page number.
**How to avoid:** Implement a sliding window: always show first page, last page, and a window of 2-3 pages around the current page. Use `uk-disabled` `<span>...</span>` for gaps. This logic should be computed server-side (in the resource or a helper method) and passed to the template as a list of page descriptors.
**Warning signs:** Pagination bar wraps to multiple lines or extends beyond the viewport.

## Code Examples

Verified patterns from official sources and existing codebase:

### Repository: Paginated findByFilter

```java
// In PersonRepository.java
// Source: Existing findByFilter() + Quarkus Panache pagination guide

/**
 * Returns a PanacheQuery for filtered persons, ready for pagination.
 * Caller applies .page() before .list().
 */
public PanacheQuery<Person> findByFilterPaged(String filterText, String sortField, String sortDir) {
    String orderBy = buildOrderBy(sortField, sortDir);

    if (filterText != null && !filterText.isBlank()) {
        String pattern = "%" + filterText.toLowerCase().trim() + "%";
        return find(
            "LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1 OR LOWER(email) LIKE ?1 " + orderBy,
            pattern
        );
    }
    return find("FROM Person " + orderBy);
}
```

### Resource: Paginated List Endpoint

```java
// In PersonResource.java
// Source: Existing list() endpoint + Panache pagination API

@GET
@Produces(MediaType.TEXT_HTML)
public TemplateInstance list(
        @HeaderParam("HX-Request") String hxRequest,
        @QueryParam("filter") String filter,
        @QueryParam("sortField") String sortField,
        @QueryParam("sortDir") String sortDir,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("25") int size) {

    // Clamp page size to allowed values
    if (size != 10 && size != 25 && size != 50 && size != 100) {
        size = 25;
    }

    PanacheQuery<Person> query = personRepository.findByFilterPaged(filter, sortField, sortDir);
    query.page(Page.of(page, size));

    List<Person> persons = query.list();
    int totalPages = query.pageCount();
    long totalCount = query.count();

    if ("true".equals(hxRequest)) {
        return Templates.person$table(persons, filter, page, size, totalPages, totalCount);
    }

    // Full page request
    String userName = securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();
    List<Title> titleChoices = titleRepository.listAllOrdered();
    List<Gender> genderChoices = genderRepository.listAllOrdered();

    return Templates.person(
        "Person Management", "persons", userName,
        persons, titleChoices, genderChoices,
        filter, sortField, sortDir,
        page, size, totalPages, totalCount
    );
}
```

### Template: Pagination Controls in Table Fragment

```html
{#fragment id='table' rendered=false}
{@java.util.List<io.archton.scaffold.entity.Person> persons}
{@String filterText}
{@int currentPage}
{@int pageSize}
{@int totalPages}
{@long totalCount}

{#if persons.isEmpty()}
    <!-- empty state message (existing) -->
{#else}
    <!-- Record count summary -->
    <div class="uk-flex uk-flex-between uk-flex-middle uk-margin-small-bottom">
        <span class="uk-text-meta">{totalCount} records</span>
    </div>

    <!-- Existing table markup -->
    <div class="uk-overflow-auto">
        <table id="person-table" class="uk-table uk-table-hover uk-table-divider uk-table-responsive">
            <!-- ... existing table content ... -->
        </table>
    </div>

    <!-- Pagination controls (inside the fragment!) -->
    {#if totalPages > 1}
    <nav aria-label="Pagination" class="uk-margin-top">
        <ul class="uk-pagination uk-flex-center" uk-margin>
            <!-- Previous -->
            {#if currentPage > 0}
            <li>
                <a hx-get="/persons?page={currentPage - 1}&size={pageSize}&filter={filterText ?: ''}"
                   hx-target="#person-table-container"
                   hx-push-url="true">
                    <span uk-pagination-previous></span>
                </a>
            </li>
            {#else}
            <li class="uk-disabled"><span><span uk-pagination-previous></span></span></li>
            {/if}

            <!-- Page numbers with ellipsis window -->
            <!-- (logic computed server-side, passed as list or computed in template) -->

            <!-- Next -->
            {#if currentPage < totalPages - 1}
            <li>
                <a hx-get="/persons?page={currentPage + 1}&size={pageSize}&filter={filterText ?: ''}"
                   hx-target="#person-table-container"
                   hx-push-url="true">
                    <span uk-pagination-next></span>
                </a>
            </li>
            {#else}
            <li class="uk-disabled"><span><span uk-pagination-next></span></span></li>
            {/if}
        </ul>
    </nav>
    {/if}
{/if}
{/fragment}
```

### Template: Page Size Selector (Inside Filter Form)

```html
<!-- Add to existing filter form in person.html -->
<div class="uk-width-auto@s">
    <select class="uk-select" name="size"
            hx-get="/persons"
            hx-target="#person-table-container"
            hx-include="closest form"
            hx-push-url="true">
        <option value="10" {#if pageSize == 10}selected{/if}>10</option>
        <option value="25" {#if pageSize == 25}selected{/if}>25</option>
        <option value="50" {#if pageSize == 50}selected{/if}>50</option>
        <option value="100" {#if pageSize == 100}selected{/if}>100</option>
    </select>
</div>
```

### CheckedTemplate Updates

```java
// Updated signatures in PersonResource.Templates
@CheckedTemplate
public static class Templates {
    // Full page -- add pagination params
    public static native TemplateInstance person(
        String title, String currentPage, String userName,
        List<Person> persons,
        List<Title> titleChoices, List<Gender> genderChoices,
        String filterText, String sortField, String sortDir,
        int page, int size, int totalPages, long totalCount
    );

    // Table fragment -- add pagination params
    public static native TemplateInstance person$table(
        List<Person> persons, String filterText,
        int currentPage, int pageSize, int totalPages, long totalCount
    );

    // modal_success -- add pagination params for OOB table refresh
    public static native TemplateInstance person$modal_success(
        String message, List<Person> persons, String filterText,
        int currentPage, int pageSize, int totalPages, long totalCount
    );

    // Other fragments remain unchanged
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `PanacheEntity.findAll().list()` returning unbounded lists | `PanacheQuery.page(Page.of(index, size)).list()` for paginated queries | Available since Panache 1.x | Prevents loading entire table into memory |
| Client-side pagination (load all, paginate in JS) | Server-side pagination (SQL LIMIT/OFFSET) | Standard practice | Scales to large datasets; reduces memory and bandwidth |
| Custom DTO for pagination result | Pass scalars (currentPage, totalPages, etc.) directly to Qute templates | Qute supports any number of parameters | No need for a wrapper class |

**Deprecated/outdated:**
- `PanacheQuery.range()`: Still available but cannot be mixed with `.page()`. The page-based API is the standard approach for numbered pagination.

## Open Questions

1. **Ellipsis window algorithm complexity**
   - What we know: For many pages, we need a sliding window (e.g., 1 ... 5 6 **7** 8 9 ... 50). Qute's template logic can handle simple conditionals.
   - What's unclear: Whether the ellipsis logic should be computed in Java (resource layer) and passed as a list of page descriptors, or computed directly in Qute template logic.
   - Recommendation: Compute in Java. Create a simple helper method that returns a `List<Integer>` of page numbers to display (using -1 as sentinel for ellipsis). This keeps templates simple and testable.

2. **Default page size**
   - What we know: Requirements specify configurable sizes: 10, 25, 50, 100.
   - What's unclear: Which should be the default.
   - Recommendation: Default to 25. It provides a good balance between information density and performance. This should be a constant in the resource, not a config property (YAGNI for a prototype app).

3. **Interaction with modal_success_row OOB swap**
   - What we know: After editing a person, `modal_success_row` does an OOB swap of just the edited `<tr>`. This is more efficient than refreshing the entire table.
   - What's unclear: Whether the row-level OOB swap remains appropriate with pagination, or whether we should always refresh the full table (with pagination) after mutations.
   - Recommendation: Keep `modal_success_row` for edit operations (the row stays in place on the same page). For create and delete operations, refresh the full paginated table (reset to page 0 for create, stay on current page for delete unless page becomes empty).

## Sources

### Primary (HIGH confidence)
- [Quarkus Hibernate ORM Panache Guide - Paging section](https://quarkus.io/guides/hibernate-orm-panache) -- PanacheQuery pagination API, Page.of(), pageCount(), count()
- [Panache Page Javadoc](https://javadoc.io/static/io.quarkus/quarkus-panache-common/1.12.0.CR1/io/quarkus/panache/common/Page.html) -- Confirms 0-indexed Page.of(index, size)
- [PanacheQuery Javadoc](https://javadoc.io/static/io.quarkus/quarkus-hibernate-orm-panache/3.21.3/io/quarkus/hibernate/orm/panache/PanacheQuery.html) -- hasNextPage(), hasPreviousPage(), pageCount(), count()
- [UIkit Pagination docs](https://getuikit.com/docs/pagination) -- uk-pagination, uk-active, uk-disabled, alignment, ARIA
- [HTMX click-to-load example](https://htmx.org/examples/click-to-load/) -- hx-get with page query params, hx-target, hx-swap
- [HTMX hx-get reference](https://htmx.org/attributes/hx-get/) -- Query parameters in URL
- Context7 `/websites/quarkus_io_guides` -- Panache PanacheQuery paging code examples
- Context7 `/bigskysoftware/htmx` -- Click-to-load and infinite scroll pagination patterns
- Context7 `/websites/getuikit` -- UIkit pagination component HTML structure and classes
- Existing codebase: `PersonRepository.java` -- current `findByFilter()` method, `buildOrderBy()` helper
- Existing codebase: `PersonResource.java` -- current list() endpoint with HX-Request detection
- Existing codebase: `person.html` -- current table fragment, filter form, modal patterns

### Secondary (MEDIUM confidence)
- [Table sorting and pagination with HTMX (vladkens.cc)](https://vladkens.cc/htmx-table-sorting/) -- Complete pattern for sorting + pagination with HTMX, verified against HTMX docs
- [URL-driven state in HTMX (lorenstew.art)](https://www.lorenstew.art/blog/bookmarkable-by-design-url-state-htmx/) -- hx-push-url pattern for bookmarkable pagination state

### Tertiary (LOW confidence)
- None. All findings verified with primary sources.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- No new dependencies; Panache pagination is well-documented and verified via Context7 and official docs
- Architecture: HIGH -- Pattern follows existing codebase conventions (HX-Request detection, fragment rendering, UIkit CSS); all three layers (repo/resource/template) have clear, verified approaches
- Pitfalls: HIGH -- 0-indexed pagination confirmed via Javadoc; filter reset, fragment boundary, and OOB swap issues identified from direct codebase analysis

**Research date:** 2026-02-14
**Valid until:** 2026-04-14 (stable; all technologies are at fixed versions in this project)
