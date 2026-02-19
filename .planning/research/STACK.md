# Technology Stack: HTMX Navigation Cleanup

**Project:** HX-Qute HTMX Navigation Refactor
**Researched:** 2026-02-19
**Focus:** HTMX 2.0.8 attribute configuration for server-side navigation with hx-boost

---

## Executive Summary

The current codebase uses `@HeaderParam("HX-Request")` in 5 resource endpoints (GenderResource, TitleResource, RelationshipResource, PersonResource, GraphResource) to branch between returning full pages vs. fragments. This is an antipattern because it couples server logic to HTMX's transport mechanism and creates two code paths per endpoint.

The correct approach is: **always return full pages from navigation endpoints, use `hx-boost` + `hx-select` on the client to extract only the content area.** The server becomes simpler (one code path), and progressive enhancement is preserved (pages work without JavaScript).

---

## Recommended HTMX Attribute Configuration

### The Core Pattern

**Confidence:** HIGH -- verified against htmx.org official docs, GitHub issues #983 and #3243, and Hypermedia Systems patterns.

Place these attributes on the `<body>` tag in `base.html`:

```html
<body hx-boost="true"
      hx-target="#main-content"
      hx-select="#main-content"
      hx-swap="innerHTML"
      hx-push-url="true">
```

| Attribute | Value | Purpose | Why This Value |
|-----------|-------|---------|----------------|
| `hx-boost` | `"true"` | Converts all `<a>` and `<form>` elements to AJAX requests | Progressive enhancement: links work without JS, feel faster with JS |
| `hx-target` | `"#main-content"` | Tells HTMX where to put the response | Only the content area changes; sidebar, header stay intact |
| `hx-select` | `"#main-content"` | Extracts only `#main-content` from the full-page response | Server returns full HTML pages; client picks out what it needs |
| `hx-swap` | `"innerHTML"` | Replaces inner content of the target | The `#main-content` div itself stays; its children get replaced |
| `hx-push-url` | `"true"` | Updates browser URL bar and history | Boosted links do this by default, but explicit is clearer |

### How It Works (Data Flow)

```
1. User clicks sidebar link: <a href="/genders">Gender</a>
2. hx-boost intercepts, sends AJAX GET /genders
3. Server returns FULL page (base.html + gender content)
4. hx-select extracts #main-content from the response
5. hx-target swaps extracted content into current page's #main-content
6. hx-push-url updates browser URL to /genders
7. Browser back button works (HTMX snapshots DOM)
```

### What This Eliminates on the Server

**Before (antipattern):**
```java
@GET
public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
    List<Gender> genders = genderRepository.listAllOrdered();
    if ("true".equals(hxRequest)) {
        return Templates.gender$table(genders);  // Fragment path
    }
    return Templates.gender("Gender Management", "gender", userName, genders);  // Full page path
}
```

**After (correct):**
```java
@GET
public TemplateInstance list() {
    List<Gender> genders = genderRepository.listAllOrdered();
    String userName = getCurrentUsername();
    return Templates.gender("Gender Management", "gender", userName, genders);  // Always full page
}
```

The server no longer needs to know whether the request came from HTMX. One code path. Simpler. Correct.

---

## Attribute Reference (HTMX 2.0.8)

### hx-boost

**Confidence:** HIGH -- official docs at htmx.org/attributes/hx-boost/

| Property | Detail |
|----------|--------|
| Values | `"true"` or `"false"` |
| Inheritance | YES -- all child `<a>` and `<form>` elements are boosted |
| Default target | `<body>` with `innerHTML` swap |
| Default behavior for links | GET to `href`, pushes URL to history |
| Default behavior for forms | GET/POST based on `method`, does NOT push URL |
| Request header sent | `HX-Boosted: true` (in addition to `HX-Request: true`) |
| Scope | Only same-domain links; non-local anchors (no `#fragment` links) |
| Element scope | Only `<a>` and `<form>` elements are boosted; `<button>` elements are not boosted |

**Key behavior:** HTMX extracts the `<body>` content from the response and swaps it. The `<head>` is mostly ignored -- only the `<title>` tag is processed and updated. This means the server can return a complete HTML page and HTMX handles it correctly.

**Critical distinction -- boost vs. inheritance:** `hx-boost` only converts anchors and forms to AJAX. It does NOT make `<button>` elements AJAX-powered. However, the companion attributes (`hx-target`, `hx-select`, `hx-swap`) set alongside `hx-boost` DO inherit to all descendant elements -- including buttons that have their own `hx-get`/`hx-post` attributes. This inheritance poisoning is the core problem that must be solved (see Inheritance Poisoning section below).

### hx-select

**Confidence:** HIGH -- official docs at htmx.org/attributes/hx-select/

| Property | Detail |
|----------|--------|
| Values | Any CSS selector, or `"unset"` to clear inherited value |
| Inheritance | YES -- children inherit the selector |
| What it does | Picks element(s) matching the CSS selector from the response HTML BEFORE swapping |
| Reset value | `hx-select="unset"` clears the inherited selector |

**Critical interaction with hx-boost:** Without `hx-select`, boosted links swap the entire `<body>` of the response into the current page. With `hx-select="#main-content"`, only the `#main-content` element is extracted from the full-page response and swapped into the target.

### hx-target

**Confidence:** HIGH -- official docs at htmx.org/attributes/hx-target/

| Property | Detail |
|----------|--------|
| Values | CSS selector, `"this"`, `"closest <selector>"`, `"find <selector>"`, `"next"`, `"previous"` |
| Inheritance | YES -- children inherit the target |
| Default (no hx-target) | The element that triggered the request |
| Default with hx-boost | `<body>` |
| Override | Explicit `hx-target` on a child element overrides inherited value |

### hx-push-url

**Confidence:** HIGH -- official docs at htmx.org/attributes/hx-push-url/

| Property | Detail |
|----------|--------|
| Values | `"true"`, `"false"`, or a custom URL string |
| Inheritance | YES |
| What it does | Pushes the URL into browser history, enabling back/forward navigation |
| With hx-boost links | Already defaults to true for links; explicit value is clearer |
| Server override | Server can send `HX-Push-Url` response header to override |

### hx-disinherit

**Confidence:** HIGH -- official docs at htmx.org/attributes/hx-disinherit/

| Property | Detail |
|----------|--------|
| Values | `"*"` (all attributes) or space-separated list: `"hx-select hx-target"` |
| What it does | Prevents specified attributes from being inherited by child elements |
| Critical for | Preventing boost-level `hx-target` and `hx-select` from poisoning CRUD buttons |

### hx-select-oob

**Confidence:** HIGH -- official docs at htmx.org/attributes/hx-select-oob/

| Property | Detail |
|----------|--------|
| Values | Comma-separated CSS selectors, optionally with swap strategy (`#el:afterbegin`) |
| Inheritance | YES |
| What it does | Selects elements from response for out-of-band swaps (parallel to the primary swap) |
| Default swap | `outerHTML` when no strategy specified |

---

## The Inheritance Poisoning Problem and Solution

**Confidence:** HIGH -- verified via GitHub issue #3243 and tested patterns.

### The Problem

When `hx-boost`, `hx-target`, and `hx-select` are set on `<body>`, ALL descendant HTMX-powered elements inherit these values via HTMX's attribute inheritance. This means:

- A `<button hx-get="/genders/create" hx-target="#gender-modal-body">` inside the content area inherits `hx-select="#main-content"` from the body.
- Even though the button has its own explicit `hx-target`, it still inherits `hx-select`.
- HTMX will try to extract `#main-content` from the fragment response (which is modal content, not a full page). Since there is no `#main-content` in the fragment, the swap results in empty content.

### The Solution: hx-disinherit on the Content Container

**Approach A: hx-disinherit on #main-content (RECOMMENDED)**

Place `hx-disinherit="hx-select hx-target hx-swap"` on the `#main-content` div in base.html:

```html
<div id="main-content" class="uk-container"
     hx-disinherit="hx-select hx-target hx-swap">
    {#insert}Default content{/}
</div>
```

This means:
- Navigation links (in sidebar, OUTSIDE `#main-content`) inherit `hx-select="#main-content"` and `hx-target="#main-content"` from `<body>` -- correct for page navigation.
- CRUD buttons (INSIDE `#main-content`) do NOT inherit `hx-select` or `hx-target` from `<body>` -- they use their own explicit values.
- Forms inside CRUD modals with `hx-post`/`hx-put`/`hx-delete` also do NOT inherit the body-level attributes -- they use their own explicit `hx-target="#gender-modal-body"` etc.

**Approach B: hx-select="unset" on each CRUD element**

```html
<button hx-get="/genders/create"
        hx-target="#gender-modal-body"
        hx-select="unset"
        hx-on::after-request="UIkit.modal('#gender-modal').show()">
```

This works but requires adding `hx-select="unset"` to EVERY HTMX-powered element inside the content area -- including every button, every form, every element with an `hx-*` request attribute. Tedious and error-prone.

**Use Approach A.** One attribute on one element solves it for all operations inside the content area.

### Why This Is Sufficient

| Element Location | Inherits from body? | Behavior |
|------------------|---------------------|----------|
| Sidebar `<a href="/genders">` | YES -- `hx-select`, `hx-target` | Boosted navigation, selects `#main-content` from response |
| Content `<button hx-get="/genders/create">` | NO -- blocked by `hx-disinherit` | Uses own explicit `hx-target="#gender-modal-body"`, no `hx-select` |
| Content `<form hx-post="/genders">` | NO -- blocked by `hx-disinherit` | Uses own explicit `hx-target="#gender-modal-body"`, no `hx-select` |
| Modal `<button hx-delete="/genders/1">` | NO -- blocked by `hx-disinherit` | Uses own explicit `hx-target="#gender-modal-body"`, no `hx-select` |

---

## Navigation Element Configuration

### Sidebar Links (in fragments/navigation.html)

**No changes needed to the links themselves.** The existing plain `<a>` links are correct for hx-boost:

```html
<a href="/genders">
    <span uk-icon="icon: users; ratio: 1"></span>
    <span class="uk-margin-small-left">Gender</span>
</a>
```

`hx-boost` automatically intercepts these because:
1. They are `<a>` elements
2. They are descendants of the `<body>` element with `hx-boost="true"`
3. They have same-domain `href` values
4. They do NOT have any `hx-get`/`hx-post` attributes (they are "plain" links)

### Links That Must NOT Be Boosted

| Link | Reason | Solution |
|------|--------|----------|
| `<a href="#login-modal" uk-toggle>` | Hash-only link, not navigation | HTMX already ignores local anchor (hash-only) links automatically |
| `<a href="https://archton.io">` | External link | HTMX already ignores cross-origin links automatically |
| `<a href="/logout">` | Session termination needs full page load | Add `hx-boost="false"` explicitly |
| `<a href="/signup">` inside login modal | Registration page, full page load preferred | Add `hx-boost="false"` explicitly |

**The logout link must be excluded from boosting:**

```html
<a href="/logout" hx-boost="false">
    <span uk-icon="icon: sign-out; ratio: 1.2"></span>
    <span class="uk-margin-small-left">Logout ({userName})</span>
</a>
```

**Rationale:** Logout performs session invalidation and redirect. An AJAX request would receive a redirect response that HTMX would try to follow and swap, which is incorrect behavior. A full page navigation ensures the session cookie is properly cleared and the user sees the landing page.

---

## Active Navigation State

### The Problem

The current `currentPage` variable drives active state in the sidebar:

```html
<li class="{#if currentPage?? == 'gender'}uk-active{/if}">
```

Because `hx-select="#main-content"` only swaps the content area, the navigation in the sidebar is NOT re-rendered during boosted navigation. The active state will NOT update when navigating between pages.

### Solution: hx-select-oob for Navigation Update (RECOMMENDED)

**Confidence:** HIGH -- verified against hx-select-oob official docs.

Use `hx-select-oob` on the body to also extract and swap the navigation from the full-page response:

```html
<body hx-boost="true"
      hx-target="#main-content"
      hx-select="#main-content"
      hx-select-oob="#sidebar-nav,#mobile-sidebar-nav"
      hx-swap="innerHTML"
      hx-push-url="true">
```

Then wrap the navigation sections in base.html with identifiable elements:

```html
<!-- Desktop sidebar navigation -->
<div id="sidebar-nav">
    {#include fragments/navigation /}
</div>

<!-- Mobile offcanvas navigation -->
<div id="mobile-sidebar-nav">
    {#include fragments/navigation /}
</div>
```

When a boosted link response comes back (full page), HTMX will:
1. Extract `#main-content` and swap it into the current page's `#main-content` (primary swap)
2. Extract `#sidebar-nav` from the response and swap it into the existing `#sidebar-nav` (OOB swap via `hx-select-oob`)
3. Extract `#mobile-sidebar-nav` from the response and swap it into the existing `#mobile-sidebar-nav` (OOB swap)

This updates the active navigation state correctly because the full-page response contains the navigation rendered with the new `currentPage` value.

### Alternative Considered: JavaScript-Based Active State

Could use JavaScript to set the active class based on the URL after swap. Rejected because:
- Adds client-side state management (contradicts HTMX philosophy)
- More fragile (URL parsing, class manipulation)
- The `hx-select-oob` approach is cleaner and uses HTMX's own mechanisms

---

## Server-Side Changes (Quarkus Resources)

### What Changes

| Resource | Current Pattern | New Pattern |
|----------|----------------|-------------|
| `GenderResource.list()` | `@HeaderParam("HX-Request")` with if/else | Remove parameter, always return full page |
| `TitleResource.list()` | `@HeaderParam("HX-Request")` with if/else | Remove parameter, always return full page |
| `RelationshipResource.list()` | `@HeaderParam("HX-Request")` with if/else | Remove parameter, always return full page |
| `PersonResource.list()` | `@HeaderParam("HX-Request")` with if/else | Remove parameter, always return full page |
| `GraphResource.showPersonNetwork()` | `@HeaderParam("HX-Request")` with if/else | Remove parameter, always return full page |

### What Does NOT Change

- `IndexResource.get()` -- already returns full page, no header checking
- `GraphResource.showGraph()` -- already returns full page, no header checking
- All CRUD endpoints (`createForm`, `create`, `editForm`, `update`, `deleteConfirm`, `delete`) -- these return fragments for modal operations, which is correct
- All `$table`, `$modal_*` fragments -- still needed for CRUD OOB updates
- All `{#fragment}` definitions in templates -- still needed for CRUD operations
- OOB swap patterns in success/delete fragments -- still needed

### Template Adjustments

The `{#fragment id='table' rendered=false}` fragments remain valuable for two reasons:
1. They are used by `$modal_success` fragments to do OOB table refreshes after CRUD operations
2. They provide the `{#include $table /}` inclusion within the full page template

No fragment definitions need to be removed.

---

## Existing Stack (Unchanged)

These technologies remain exactly as-is. This refactor only changes HTMX attribute usage.

### Core Framework
| Technology | Version | Purpose |
|------------|---------|---------|
| Quarkus | 3.30.3 | Java framework with REST + Qute |
| Java | 21 LTS | Runtime |
| HTMX | 2.0.8 | Hypermedia-driven dynamic UI |
| UIkit | 3.25.4 | CSS framework |
| PostgreSQL | 17.7 | Database |
| Flyway | (bundled) | Schema migrations |

### No New Dependencies Required

This refactor requires zero new libraries, extensions, or dependencies. It is purely an HTMX attribute reconfiguration on the client side and simplification on the server side.

---

## Antipatterns to Remove

### Antipattern 1: Server-Side HX-Request Header Checking for Navigation

**Confidence:** HIGH

**What:** Checking `@HeaderParam("HX-Request")` in list/navigation endpoints to decide between full page vs. fragment response.

**Why it is wrong:**
1. Creates two code paths per endpoint -- more to test, more to break
2. Couples server logic to the client's transport mechanism
3. If someone bookmarks a URL and navigates to it directly, they get the full page. If they click a sidebar link, they get a fragment. These are different responses for the same URL, which violates HTTP semantics.
4. Fragments returned for boosted navigation break if the user refreshes the page (the fragment gets rendered standalone without the base layout)

**Replace with:** Always return full page. Let `hx-select` on the client extract what is needed.

### Antipattern 2: Fragment-Only Responses for Navigation Endpoints

**Confidence:** HIGH

**What:** Returning `Templates.gender$table(genders)` for HTMX navigation requests.

**Why it is wrong:** A table fragment is not what should be swapped during navigation. Navigation should swap the entire page content area (heading, buttons, table, modal shell), not just the table.

**Replace with:** Always return the full page template. The `hx-select="#main-content"` on the client extracts the right content.

### Antipattern 3: Using hx-boost Without hx-select (hypothetical, to prevent)

**Confidence:** HIGH

**What:** Adding `hx-boost="true"` without `hx-select` when using `hx-target` on a content div.

**Why it is wrong:** Without `hx-select`, HTMX swaps the entire `<body>` content from the response into the target div, causing the "double render" problem (navigation, header, footer all duplicated inside the content area).

**Prevention:** Always pair `hx-target` with a matching `hx-select` when using `hx-boost` with a non-body target.

### Antipattern 4: Not Handling Inheritance Poisoning

**Confidence:** HIGH

**What:** Setting `hx-boost`, `hx-target`, and `hx-select` on `<body>` without `hx-disinherit` on the content container.

**Why it is wrong:** Every HTMX-powered element inside the content area inherits `hx-select` and `hx-target` from `<body>`, causing CRUD fragment swaps to silently break (empty content or wrong target).

**Prevention:** Place `hx-disinherit="hx-select hx-target hx-swap"` on the `#main-content` container.

---

## HTMX Request Headers Reference

For debugging and understanding, here are the headers HTMX sends:

| Header | Sent When | Value | Server Use |
|--------|-----------|-------|------------|
| `HX-Request` | Every HTMX request | `"true"` | Identify HTMX traffic (but do not branch on it for navigation) |
| `HX-Boosted` | Only boosted requests | `"true"` | Identify boosted navigation vs. inline HTMX requests |
| `HX-Current-URL` | Every HTMX request | Current browser URL | Context for server |
| `HX-Target` | When target exists | ID of target element | Server can optionally use this |
| `HX-Trigger` | When trigger has ID | ID of triggering element | Server can optionally use this |

**After this refactor, the server does NOT need to read any of these headers for navigation endpoints.** The headers are still sent and can be useful for debugging or logging, but no server logic should branch on them for deciding between full-page vs. fragment responses.

---

## Edge Cases and Special Handling

### Direct URL Access (Bookmarks, Refresh)

When a user directly navigates to `/genders` (bookmark, refresh, typed URL), there is no HTMX request. The browser makes a standard GET request, and the server returns the full page. This works correctly because the server always returns full pages now.

### Browser Back/Forward

HTMX snapshots the DOM when navigating. When the user presses back, HTMX restores the snapshot. The URL updates correctly because `hx-push-url="true"` creates history entries.

### Boosted Forms Inside Content Area

**Confidence:** MEDIUM -- needs validation during implementation.

`hx-boost` converts ALL descendant `<a>` and `<form>` elements. Forms inside the content area that should submit and update a specific target (not navigate) may be affected. There are two scenarios:

1. **Forms with explicit `hx-post`/`hx-put`/`hx-delete`:** These are already HTMX-powered and their behavior is determined by their own attributes plus whatever they inherit. With `hx-disinherit` on `#main-content`, they will NOT inherit `hx-select` or `hx-target` from the body -- they will use their own explicit values. This is correct.

2. **Plain `<form>` elements without `hx-*` attributes:** These WILL be boosted by `hx-boost`. If the content area contains any plain forms (search forms, filter forms), they would be converted to AJAX submissions targeting `<body>` by default. However, with `hx-disinherit` on `#main-content`, they would not inherit `hx-target` or `hx-select` from body, so they would use hx-boost's defaults (`<body>`, `innerHTML`). This could cause full-page swap behavior inside a form submission. **Check all plain forms in templates and add explicit `hx-boost="false"` if needed, or convert them to use explicit HTMX attributes.**

### The PersonResource Search/Filter

The PersonResource has search/filter functionality. Review its form elements to ensure they either:
- Have explicit `hx-get` + `hx-target` (already HTMX-powered, unaffected by boost), OR
- Have `hx-boost="false"` if they should not be boosted

### Login Form (in base.html)

The login form `<form action="/j_security_check" method="POST">` in the login modal:
- This is inside the `<body>` with `hx-boost="true"`
- It is a plain `<form>` element, so it WILL be boosted
- Login form submission should NOT be boosted (it needs server-side redirect handling for authentication)
- **Add `hx-boost="false"` to the login form explicitly**

```html
<form action="/j_security_check" method="POST" class="uk-form-stacked"
      hx-boost="false"
      onsubmit="...">
```

---

## Complete base.html Changes Summary

```html
<!-- BEFORE -->
<body>
    ...
    <div id="main-content" class="uk-container">
        {#insert}Default content{/}
    </div>
    ...
</body>

<!-- AFTER -->
<body hx-boost="true"
      hx-target="#main-content"
      hx-select="#main-content"
      hx-select-oob="#sidebar-nav,#mobile-sidebar-nav"
      hx-swap="innerHTML"
      hx-push-url="true">
    ...
    <div id="sidebar-nav">
        {#include fragments/navigation /}
    </div>
    ...
    <div id="main-content" class="uk-container"
         hx-disinherit="hx-select hx-target hx-swap">
        {#insert}Default content{/}
    </div>
    ...
    <div id="mobile-sidebar-nav">
        {#include fragments/navigation /}
    </div>
    ...
    <form action="/j_security_check" method="POST" hx-boost="false" ...>
    ...
</body>
```

---

## Sources

### Official Documentation (HIGH confidence)
- [hx-boost attribute](https://htmx.org/attributes/hx-boost/) -- HTMX official docs
- [hx-select attribute](https://htmx.org/attributes/hx-select/) -- HTMX official docs
- [hx-target attribute](https://htmx.org/attributes/hx-target/) -- HTMX official docs
- [hx-push-url attribute](https://htmx.org/attributes/hx-push-url/) -- HTMX official docs
- [hx-disinherit attribute](https://htmx.org/attributes/hx-disinherit/) -- HTMX official docs
- [hx-select-oob attribute](https://htmx.org/attributes/hx-select-oob/) -- HTMX official docs
- [hx-swap attribute](https://htmx.org/attributes/hx-swap/) -- HTMX official docs
- [HTMX Reference (request headers)](https://htmx.org/reference/) -- HTMX official docs
- [HTMX Quirks (hx-boost tradeoffs)](https://htmx.org/quirks/) -- HTMX official docs
- [HTMX Documentation (boosting section)](https://htmx.org/docs/#boosting) -- HTMX official docs

### Community Patterns (MEDIUM confidence)
- [GitHub Issue #983: hx-boost replacing parts of page](https://github.com/bigskysoftware/htmx/issues/983) -- Canonical discussion of hx-boost + hx-select + hx-target pattern
- [GitHub Issue #3243: hx-boost inheritance poisoning](https://github.com/bigskysoftware/htmx/issues/3243) -- Documents the inheritance problem and workarounds
- [GitHub Issue #342: hx-boost behavior with parent attributes](https://github.com/bigskysoftware/htmx/issues/342) -- Confirms inheritance affects boosted elements
- [Quarkus Discussion #41114: Fragments vs hx-boost](https://github.com/quarkusio/quarkus/discussions/41114) -- Quarkus team confirming both patterns are valid
- [Hypermedia Systems: HTMX Patterns](https://hypermedia.systems/htmx-patterns/) -- Book by HTMX creator on patterns

### Caveats Acknowledged
- [HTMX Quirks page](https://htmx.org/quirks/) notes some core team members suggest avoiding hx-boost due to head content being discarded and global JS scope not being refreshed. For this application, these are non-issues: all CSS/JS is loaded via CDN in base.html (never changes between pages), and there is minimal client-side JavaScript (just UIkit integration via `hx-on::` attributes).
