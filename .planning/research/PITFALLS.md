# Pitfalls Research

**Domain:** HTMX antipattern cleanup -- migrating from HX-Request header checking to hx-boost + hx-select navigation
**Researched:** 2026-02-19
**Confidence:** HIGH (verified against official HTMX docs, GitHub issues, and codebase analysis)

## Critical Pitfalls

### Pitfall 1: Login Form (`/j_security_check`) Gets Boosted and Breaks Authentication

**What goes wrong:**
The login form in `base.html` POSTs to `/j_security_check` (Jakarta EE form auth). If `hx-boost="true"` is placed on a parent element (body or offcanvas-content), this form gets AJAX-ified. The server-side authentication handler relies on a traditional form POST followed by a redirect (302 to landing page). HTMX intercepts the redirect as an AJAX response, and the authentication flow breaks silently -- the user appears stuck on the login modal, or gets a blank/partial response.

**Why it happens:**
`hx-boost` inherits to all descendant forms on the same domain. The login form action `/j_security_check` is same-domain, so it gets boosted. Jakarta EE form authentication uses server-side redirects that do not work correctly as AJAX responses. Quarkus config `quarkus.http.auth.form.landing-page=/` and `quarkus.http.auth.form.error-page=/?login=true&error=true` rely on full-page redirects.

**How to avoid:**
Add `hx-boost="false"` directly on the login form element in `base.html`:
```html
<form hx-boost="false" action="/j_security_check" method="POST" class="uk-form-stacked" ...>
```

**Warning signs:**
- Login appears to succeed (no error) but user stays on the same page
- Login redirects show up as partial HTML or blank content
- Browser URL does not change after login

**Phase to address:**
First phase -- this must be done simultaneously with adding `hx-boost="true"` to the layout. It is the single most likely thing to break.

---

### Pitfall 2: Sidebar Active State Becomes Stale After Navigation

**What goes wrong:**
The navigation sidebar (in `fragments/navigation.html`) uses Qute conditionals like `{#if currentPage?? == 'persons'}uk-active{/if}` to highlight the current page. With `hx-boost` targeting `body` innerHTML (the default), the entire body is replaced including the sidebar, so the active state updates correctly. But if the refactoring uses `hx-select="#main-content"` to only swap the main content area and keep the sidebar intact, the sidebar active state will never update -- the user navigates to "People" but "Home" stays highlighted.

**Why it happens:**
The `hx-select` attribute filters the response to only extract the `#main-content` div. The navigation sidebar is outside that selection, so its server-rendered `uk-active` class is discarded. The existing sidebar DOM with the old `uk-active` persists.

**How to avoid:**
Two options, choose one:

**Option A (recommended): Use `hx-boost="true"` with body-level swap (the default).** Let HTMX replace the entire body content. The server already renders the correct active state in every response. The sidebar, offcanvas, login modal, and all structural elements are re-rendered each time. This is simpler and correct by default.

**Option B: Use `hx-select` but add OOB swap for navigation.** If targeting only `#main-content`, add an out-of-band swap for the navigation. This requires server responses to include a navigation fragment with `hx-swap-oob`. This adds complexity to every page endpoint.

Option A is strongly preferred because it eliminates the active-state problem entirely, and the overhead of re-rendering the sidebar HTML is negligible.

**Warning signs:**
- Sidebar highlighting does not change when clicking navigation links
- "Home" stays active even when viewing other pages
- Mobile offcanvas shows wrong active state

**Phase to address:**
Architecture decision needed before implementation begins. This determines the entire hx-boost + hx-select strategy.

---

### Pitfall 3: UIkit Offcanvas Mobile Sidebar Left in Broken State After Body Swap

**What goes wrong:**
The mobile sidebar uses `<div id="mobile-sidebar" uk-offcanvas="overlay: true">` in `base.html`. If the user opens the mobile sidebar, taps a navigation link, and `hx-boost` replaces the body innerHTML, UIkit's offcanvas JavaScript state becomes orphaned. The overlay backdrop may stay visible, scroll locking may persist, or the new offcanvas element may not be initialized by UIkit, leaving mobile navigation broken.

**Why it happens:**
UIkit registers JavaScript event handlers and internal state on DOM elements. When `hx-boost` replaces the body innerHTML, those DOM elements are destroyed and recreated, but UIkit does not automatically re-initialize on the new elements. UIkit's offcanvas component manages body classes (like `uk-offcanvas-page`) and overlay elements that may persist across the swap.

**How to avoid:**
1. Close the offcanvas before HTMX processes a boosted navigation. Use an event listener on `htmx:beforeSwap` to call `UIkit.offcanvas('#mobile-sidebar').hide()` if the offcanvas is currently open.
2. Alternatively, add `hx-on::click` handlers on navigation links inside the offcanvas to close it before the HTMX request fires.
3. After swap, ensure UIkit re-processes the new DOM. Listen for `htmx:afterSettle` and call `UIkit.update()` on the new content.

```javascript
document.body.addEventListener('htmx:beforeSwap', function(evt) {
    var offcanvas = document.getElementById('mobile-sidebar');
    if (offcanvas && UIkit.offcanvas(offcanvas).$props.overlay) {
        UIkit.offcanvas(offcanvas).hide();
    }
});
```

**Warning signs:**
- Dark overlay persists after clicking a link in mobile sidebar
- Body scroll is locked after navigation on mobile
- Mobile sidebar button stops working after first navigation
- `uk-offcanvas-page` class left on HTML element

**Phase to address:**
Must be addressed in the same phase as hx-boost implementation. Cannot be deferred.

---

### Pitfall 4: Graph Page D3.js Visualization Breaks on Navigation

**What goes wrong:**
The graph page (`graph.html`) loads D3.js via a CDN `<script>` tag in the template body and initializes a force-directed graph in `graph.js`. With `hx-boost`, navigating TO the graph page via a boosted link means: (1) the D3.js `<script>` tag in the response body may not re-execute because HTMX's innerHTML swap does not re-evaluate scripts by default, and (2) `graph.js` depends on D3 being loaded, so if D3 is not available, the graph initialization fails silently or throws errors. Navigating AWAY from the graph page and then BACK via browser history can also restore stale DOM without reinitializing D3.

**Why it happens:**
HTMX's `hx-boost` uses innerHTML swap on the body. Script tags in the swapped content are processed by HTMX (it does evaluate inline and external scripts), but the D3 library may already be in the global scope from a prior visit, or may not be loaded on the first boosted navigation. The `graph.js` IIFE pattern runs immediately on script evaluation but expects `d3` to be globally available. The graph page also loads D3 in the `<head>` equivalent position (actually in the body before the graph content), which behaves differently under boosted vs. non-boosted navigation.

**How to avoid:**
1. Move the D3.js CDN `<script>` to `base.html` `<head>` so it is always available. This wastes bandwidth on non-graph pages but eliminates the loading-order problem entirely.
2. Or use the `head-support` extension (`hx-ext="head-support"`) with D3 in a proper `<head>` tag, and mark graph.js with `hx-head="re-eval"` so it reinitializes on each visit.
3. Or add `hx-boost="false"` to navigation links pointing to `/graph` to force a full-page load for that specific route. This is the simplest approach and preserves the D3 loading behavior exactly as it works today.

Option 3 is recommended as the pragmatic choice for a cleanup refactoring. The graph page is a special case with heavy JavaScript dependencies.

**Warning signs:**
- Blank graph container (SVG exists but nothing rendered)
- Console errors: "d3 is not defined" or "Cannot read property of null"
- Graph works on direct URL load but not via sidebar navigation
- Graph data appears after manual browser refresh

**Phase to address:**
Must be planned before hx-boost implementation. Decision on how to handle JS-heavy pages is architectural.

---

### Pitfall 5: History Restore Shows Fragment Instead of Full Page (The HX-Request Header Trap)

**What goes wrong:**
Currently, the server checks `HX-Request` header to decide whether to return a fragment (table only) or a full page (with layout). When using `hx-boost`, HTMX sets `HX-Request: true` on all requests including history restoration requests. If a user navigates with hx-boost, then hits the browser back button, and the history cache is disabled or missed, HTMX re-fetches the URL with `HX-Request: true`. The server returns a fragment (just the table), and HTMX swaps it into the body, resulting in a page showing only a data table with no layout, no sidebar, no navigation.

**Why it happens:**
The `if ("true".equals(hxRequest))` pattern in all list endpoints (GenderResource, TitleResource, PersonResource, RelationshipResource, GraphResource, PersonRelationshipResource) was designed for in-page HTMX requests (search, filter, pagination) -- not for boosted navigation. But HTMX cannot distinguish between "this is a boosted navigation request" and "this is a table refresh request" using only the HX-Request header.

**How to avoid:**
This is the core antipattern being eliminated. The refactoring should:

1. **Remove all `@HeaderParam("HX-Request")` checks from list endpoints.** Always return the full page.
2. **Use `hx-select="#main-content"` on boosted links** to let HTMX extract only the content area from the full-page response. The server does not need to know whether the request is boosted or not -- it always returns a complete page.
3. **For in-page HTMX requests** (search, filter, pagination), these already use explicit `hx-target="#person-table-container"` and `hx-get`, so they will NOT go through the boost path. These should keep returning fragments via their own dedicated endpoints or via a different mechanism (such as a query parameter like `?partial=true`).

Wait -- this creates a new problem. The search/filter/pagination requests on PersonResource (`hx-get="/persons"` with `hx-target="#person-table-container"`) hit the SAME endpoint as a boosted navigation to `/persons`. If we remove the HX-Request check and always return the full page, these in-page requests will also get the full page and try to swap a full HTML document into the table container.

**The real solution:** Use `hx-select` on the in-page requests too, or separate the endpoints:
- Navigation: `GET /persons` returns full page. Boosted links use `hx-select="#main-content"`.
- Table refresh: `GET /persons` with `hx-select="#person-table-container"` on the search/filter form. OR use a dedicated `GET /persons/table` endpoint for partial responses.

Alternatively, use the `HX-Boosted` header (not `HX-Request`) to distinguish boosted navigation from in-page HTMX requests. `hx-boost` sets `HX-Boosted: true` in addition to `HX-Request: true`, while explicit `hx-get` requests only set `HX-Request: true`.

**Warning signs:**
- Back button shows raw table HTML with no layout
- Browser session restore shows a fragment
- Search/filter returns full page HTML crammed into the table container
- Page content duplicated (layout inside layout)

**Phase to address:**
This is the central architectural decision of the refactoring. Must be resolved FIRST before any code changes.

---

### Pitfall 6: Login Modal Toggle Link (`#login-modal`) Intercepted by hx-boost

**What goes wrong:**
The navigation sidebar has a login link: `<a href="#login-modal" uk-toggle>`. According to HTMX docs, local anchor links (href starting with `#`) are NOT boosted by hx-boost. However, UIkit's `uk-toggle` attribute on this element may interact unexpectedly if htmx processes the element before UIkit does. Additionally, if the login link markup changes during the refactoring (e.g., someone accidentally removes the `#` prefix), it would get boosted and navigate instead of toggling the modal.

**Why it happens:**
The `#login-modal` link is a local anchor, which HTMX correctly skips for boosting. But there is a subtlety: after a boosted body swap, UIkit may need to re-bind its toggle handler to the new DOM element. If UIkit has not re-initialized the `uk-toggle` attribute on the newly swapped navigation, clicking "Login" does nothing.

**How to avoid:**
1. After each boosted swap, verify that UIkit initializes components on the new DOM. UIkit auto-detects new elements via MutationObserver, but verify this works with HTMX's innerHTML swap.
2. As a safety net, add `hx-boost="false"` on the login toggle link explicitly.
3. Test this specific interaction after implementing hx-boost: click Login in sidebar after navigating to a different page.

**Warning signs:**
- Login link does nothing after navigating via hx-boost
- Login modal appears on first page load but not after navigating to another page and back
- No JavaScript errors in console (silent failure)

**Phase to address:**
Must be tested during hx-boost implementation. Add to the test checklist.

---

## Technical Debt Patterns

Shortcuts that seem reasonable but create long-term problems.

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Keep HX-Request checks alongside hx-boost | Partial migration, less code to change | Dual response logic creates confusion; history restore bugs surface randomly | Never -- remove the checks or do not add hx-boost |
| Use `hx-boost` body-wide without head-support extension | Simpler setup | Page-specific scripts (D3) fail silently; styles lost on navigation | Only if ALL pages have identical head content |
| Disable history cache entirely (`historyCacheSize: 0`) | Avoids stale content and fragment-restore bugs | Back/forward navigation triggers server request every time; slower UX | Acceptable for this app's scale; recommended as starting point |
| Force full-page reload on graph page via `hx-boost="false"` | Graph page keeps working exactly as-is | Inconsistent navigation feel; graph page flashes during load | Acceptable -- graph is a special case with heavy JS |

## Integration Gotchas

Common mistakes when connecting these technologies together.

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| hx-boost + UIkit modals | Assuming UIkit auto-reinitializes after body swap | Call `UIkit.update()` after htmx:afterSettle, or verify UIkit's MutationObserver catches the swap |
| hx-boost + Quarkus form auth | Letting `/j_security_check` get boosted | Add `hx-boost="false"` on the login form |
| hx-boost + hx-push-url (search/filter) | Both hx-boost and search form push URLs to the same endpoint | Distinguish via HX-Boosted header, or use hx-select on search forms, or separate endpoints |
| hx-boost + signup form | `/signup` POST gets AJAX-ified; redirect after signup fails | Add `hx-boost="false"` on the signup form, or handle HTMX redirect response headers |
| hx-boost + logout | `/logout` GET gets boosted; server invalidates session but HTMX shows response as partial content | Verify logout works as boosted request; may need `hx-boost="false"` or redirect handling |
| hx-select + OOB swaps | Assuming hx-select filters OOB elements too | hx-select only filters the main response; OOB swaps are processed separately. Existing OOB patterns (modal_success, modal_delete_success) should still work |

## Performance Traps

Patterns that work at small scale but fail as usage grows.

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Always returning full page (no fragments) | Every search keystroke (300ms debounce) returns a full HTML document | Use hx-select on search/filter to extract just the table, or keep a fragment endpoint for search | Noticeable with 50+ entities and complex templates |
| History cache storing full pages | Browser localStorage fills up with full HTML snapshots | Set `historyCacheSize` to a small number (e.g., 10) or disable | After browsing many pages in one session |

## Security Mistakes

Domain-specific security issues beyond general web security.

| Mistake | Risk | Prevention |
|---------|------|------------|
| Boosted login form exposes auth flow to HTMX processing | Authentication bypass or session confusion if AJAX redirect fails | Always `hx-boost="false"` on auth forms |
| History cache stores authenticated page content | Sensitive data visible via back button after logout | Use `hx-history="false"` on pages with sensitive data, or disable history cache |
| Boosted requests bypass CSRF protection | If Quarkus form auth expects specific request characteristics | Verify HTMX boosted requests include CSRF tokens if configured |

## UX Pitfalls

Common user experience mistakes in this domain.

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Removing HX-Request checks without adding hx-select | Full pages swap into targeted containers, creating layout-in-layout | Always pair "always return full page" with hx-select on the consumer side |
| Not closing offcanvas before boosted navigation on mobile | User sees broken overlay, locked scroll | Close offcanvas programmatically before swap |
| D3 graph disappearing after boosted navigation | User clicks Graph, sees empty page, must refresh | Either exclude graph from boosting or ensure D3 reinitializes |
| Back button shows stale table data after CRUD operation | User edits a person, goes back, sees old data | Disable history cache or use `refreshOnHistoryMiss: true` |
| Navigation feels "stuck" because active state does not update | Confuses user about current location | Use body-level swap (not hx-select on main-content alone) |

## "Looks Done But Isn't" Checklist

Things that appear complete but are missing critical pieces.

- [ ] **hx-boost on body:** Login form (`/j_security_check`) has `hx-boost="false"` -- verify login works
- [ ] **hx-boost on body:** Signup form (`/signup` POST) has `hx-boost="false"` -- verify signup + redirect works
- [ ] **hx-boost on body:** Login toggle (`#login-modal uk-toggle`) still opens modal after boosted navigation
- [ ] **hx-boost on body:** Logout link (`/logout`) works correctly -- session destroyed, page displays properly
- [ ] **Mobile navigation:** Offcanvas closes before boosted swap; reinitializes after swap
- [ ] **Graph page:** D3 visualization initializes on boosted navigation (or page is excluded from boost)
- [ ] **Search/filter:** PersonResource search returns correct content (not full layout in table container)
- [ ] **Pagination:** hx-push-url on pagination still works alongside hx-boost
- [ ] **Back button:** Browser back navigation shows correct full page, not a fragment
- [ ] **Browser refresh:** Direct URL access to any page still returns a complete page (no regression)
- [ ] **Session restore:** Browser reopening tabs shows full page, not a fragment
- [ ] **UIkit modals:** All CRUD modals (gender, title, relationship, person) still open/close after boosted navigation
- [ ] **OOB swaps:** modal_success and modal_delete_success OOB table updates still work after refactoring

## Recovery Strategies

When pitfalls occur despite prevention, how to recover.

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Login form boosted | LOW | Add `hx-boost="false"` to the form; no server changes needed |
| Active state stale | MEDIUM | Switch from hx-select approach to full body swap approach; or add OOB nav updates |
| D3 graph broken | LOW | Add `hx-boost="false"` to graph navigation links; or move D3 to head |
| History shows fragments | HIGH | Requires removing HX-Request header checks from all endpoints AND updating all HTMX attributes on search/filter/pagination forms |
| UIkit offcanvas broken on mobile | LOW | Add htmx:beforeSwap event listener to close offcanvas; or call UIkit.update() after swap |
| Search returns full page in table | MEDIUM | Add hx-select to search form, or use HX-Boosted header check instead of HX-Request |

## Pitfall-to-Phase Mapping

How roadmap phases should address these pitfalls.

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| Login form boosted | Phase 1: Add hx-boost infrastructure | Login, see dashboard; logout, login again |
| Sidebar active state | Phase 1: Architecture decision | Navigate to each page; verify sidebar highlights correctly |
| Offcanvas broken on mobile | Phase 1: Add hx-boost infrastructure | Open mobile sidebar, click link, verify sidebar closes and page loads |
| D3 graph broken | Phase 1 or 2: Handle JS-heavy pages | Navigate to Graph via sidebar; verify D3 renders; navigate away and back |
| History fragment restore | Phase 2: Remove HX-Request checks | Navigate via boost, hit back button; refresh page; restore browser tabs |
| Login modal toggle | Phase 1: Add hx-boost infrastructure | Navigate to any page via boost; click Login in sidebar; verify modal appears |
| Search/filter endpoint conflict | Phase 2: Remove HX-Request checks | Use search on persons page; verify table updates (not full page in table) |
| Signup form boosted | Phase 1: Add hx-boost infrastructure | Complete signup flow; verify redirect to login |
| OOB swaps after refactoring | Phase 3: Cleanup and verification | Create/edit/delete entity via modal; verify table updates and modal closes |

## Sources

- [HTMX hx-boost attribute documentation](https://htmx.org/attributes/hx-boost/) -- HIGH confidence
- [HTMX hx-select attribute documentation](https://htmx.org/attributes/hx-select/) -- HIGH confidence
- [HTMX quirks page](https://htmx.org/quirks/) -- HIGH confidence
- [HTMX head-support extension](https://htmx.org/extensions/head-support/) -- HIGH confidence
- [GitHub issue #983: hx-boost replacing only specific parts](https://github.com/bigskysoftware/htmx/issues/983) -- HIGH confidence
- [GitHub issue #497: hx-boost/hx-push-url fragment restore](https://github.com/bigskysoftware/htmx/issues/497) -- HIGH confidence
- [GitHub issue #3037: restore from history breaks HX-Request behavior](https://github.com/bigskysoftware/htmx/issues/3037) -- HIGH confidence
- [GitHub issue #3447: links with hash and hx-boost](https://github.com/bigskysoftware/htmx/issues/3447) -- MEDIUM confidence
- [Ben Nadel: conditionally preventing hx-boost](https://www.bennadel.com/blog/4784-conditionally-preventing-hx-boost-in-htmx-using-an-extension.htm) -- MEDIUM confidence
- [HTMX UIkit modal example](https://htmx.org/examples/modal-uikit/) -- HIGH confidence
- Codebase analysis of `src/main/java/io/archton/scaffold/router/` and `src/main/resources/templates/` -- HIGH confidence

---
*Pitfalls research for: HTMX antipattern cleanup (HX-Request to hx-boost + hx-select)*
*Researched: 2026-02-19*
