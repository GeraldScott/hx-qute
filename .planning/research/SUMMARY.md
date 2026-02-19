# Project Research Summary

**Project:** HX-Qute HTMX Navigation Cleanup
**Domain:** HTMX antipattern remediation in a Quarkus + Qute server-rendered application
**Researched:** 2026-02-19
**Confidence:** HIGH

## Executive Summary

This project is a focused refactoring of how the HX-Qute application handles page navigation. The current codebase uses `@HeaderParam("HX-Request")` in six resource endpoints to branch between returning full pages and fragments -- an antipattern that couples server logic to the client transport, creates two code paths per endpoint, and breaks progressive enhancement. The correct HTMX pattern is well-documented and straightforward: use `hx-boost` on the `<body>` tag so plain `<a>` links become AJAX-powered automatically, always return full pages from the server, and let `hx-select` on the client extract only the content area. No new dependencies are required. This is purely an HTMX attribute reconfiguration on the client side and a simplification on the server side.

The recommended approach adds five attributes to the `<body>` tag (`hx-boost`, `hx-target`, `hx-select`, `hx-swap`, `hx-select-oob`), places `hx-disinherit` on the `#main-content` container to prevent attribute inheritance from poisoning modal CRUD operations, and removes the `HX-Request` header branching from all list endpoints. The existing modal CRUD pattern with OOB swaps is correct and remains unchanged. For resources with search/filter/pagination (Person, PersonRelationship), a separate `/table` endpoint should be introduced to cleanly separate page navigation from in-page partial updates.

The primary risks are: (1) the login form (`/j_security_check`) getting boosted and breaking authentication, (2) HTMX attribute inheritance poisoning modal CRUD operations, (3) the D3.js graph page failing under boosted navigation, and (4) UIkit offcanvas state becoming orphaned after body swaps on mobile. All four risks have clear, tested prevention strategies documented in the research. The implementation order is critical -- modal protections (hx-boost="false", hx-disinherit, hx-select="unset") must be in place BEFORE enabling hx-boost on the body, or CRUD operations break immediately.

## Key Findings

### Recommended Stack

No stack changes required. The existing stack (Quarkus 3.30.3, Java 21, HTMX 2.0.8, UIkit 3.25, PostgreSQL 17.7, Flyway) remains exactly as-is. This refactoring is purely an HTMX attribute reconfiguration. Zero new libraries, extensions, or dependencies are needed.

**Core HTMX attributes being configured:**
- `hx-boost="true"` on `<body>`: converts all `<a>` and `<form>` elements to AJAX automatically
- `hx-select="#main-content"` on `<body>`: extracts only the content area from full-page responses
- `hx-target="#main-content"` on `<body>`: swaps extracted content into the content div
- `hx-select-oob="#sidebar-nav,#mobile-sidebar-nav"` on `<body>`: updates navigation active state via OOB swap
- `hx-disinherit="hx-select hx-target hx-swap"` on `#main-content`: prevents inheritance poisoning of modal CRUD

**Optional addition (Phase 3):**
- `head-support` HTMX extension via CDN: enables full `<head>` merging on boosted navigation

### Expected Features

**Must have (table stakes / correctness fixes):**
- **F1: Adopt hx-boost for navigation** -- the HTMX-native mechanism for AJAX page transitions
- **F2: Remove HX-Request header branching** -- eliminates the antipattern from 6 resources
- **F3: Server always returns full page** -- one code path, simpler, correct
- **F4: Title tag handling** -- automatic with hx-boost (zero work)
- **F5: Active navigation state** -- works via hx-select-oob for sidebar updates
- **F6: Browser history support** -- automatic with hx-boost (zero work)
- **F14: refreshOnHistoryMiss config** -- safety net for edge cases, trivial to add

**Should have (reliability/maintainability):**
- **F7: Vary header** -- only needed if any endpoints still branch on headers
- **F8: head-support extension** -- better `<head>` management across pages
- **F9: Preserve partial updates for search/filter** -- Person and PersonRelationship need separate `/table` endpoints

**Defer (v2+):**
- **F10: Extract common template variables** -- DX improvement, not HTMX-related
- **F11: Standardize modal patterns** -- cosmetic consistency
- **F12: Use `<dialog>` element** -- only if removing UIkit
- **F13: HX-Trigger for decoupled updates** -- only if modals become cross-page

### Architecture Approach

The application has three coexisting interaction patterns that must be preserved: (1) sidebar navigation via hx-boost (server returns full page, client extracts content), (2) modal CRUD via explicit hx-get/hx-post/hx-delete (server returns fragments for modal content, OOB swaps update the table), and (3) in-page table refresh via dedicated endpoints (server returns table fragments for filter/sort/pagination). The key architectural decision is using `hx-disinherit` on the `#main-content` container to create a clean boundary between navigation-level attributes (inherited from body) and CRUD-level attributes (explicit on each element).

**Major components and their changes:**
1. **`base.html`** -- receives hx-boost attributes on `<body>`, hx-disinherit on `#main-content`, wrapper IDs on navigation sections, hx-boost="false" on login form
2. **`fragments/navigation.html`** -- wrapped in `<div id="sidebar-nav">` for OOB swap targeting (no link changes needed)
3. **`*Resource.java` list endpoints** -- remove `@HeaderParam("HX-Request")` and if/else branching; always return full page
4. **`PersonResource.java` / `PersonRelationshipResource.java`** -- add separate `/table` endpoint for search/filter/pagination
5. **Page templates** -- add hx-boost="false" on modal shells; add hx-select="unset" on modal trigger buttons

### Critical Pitfalls

1. **Login form gets boosted** -- The `/j_security_check` form is same-domain and will be AJAX-ified by hx-boost. Authentication relies on server-side redirects that break under AJAX. Prevention: add `hx-boost="false"` on the login form. Must be done simultaneously with enabling hx-boost.

2. **Inheritance poisoning breaks modal CRUD** -- Body-level `hx-select="#main-content"` inherits to all descendant elements, including modal trigger buttons. Fragment responses contain no `#main-content` element, so hx-select finds nothing and swaps empty content. Prevention: `hx-disinherit="hx-select hx-target hx-swap"` on `#main-content` container.

3. **D3.js graph page fails under boosted navigation** -- Script tags in swapped content have loading-order issues with D3. Prevention: add `hx-boost="false"` to graph navigation links to force full-page load for that route. Simplest and most pragmatic approach.

4. **UIkit offcanvas orphaned on mobile** -- Offcanvas JavaScript state (overlay, scroll lock) persists after body swap. Prevention: close offcanvas programmatically before swap via `htmx:beforeSwap` event listener; call `UIkit.update()` after swap.

5. **History restore shows fragment instead of full page** -- If HX-Request branching remains alongside hx-boost, back-button navigation re-fetches with `HX-Request: true` and gets a fragment. Prevention: remove ALL HX-Request branching from navigation endpoints (this is the core refactoring goal).

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Prepare Template Infrastructure (Non-Breaking)

**Rationale:** All prerequisite changes must be in place before hx-boost is enabled. These changes are purely additive and do not affect current behavior.
**Delivers:** Navigation wrapper IDs, modal protections, form exclusions -- all the scaffolding that prevents breakage when hx-boost activates.
**Addresses:** F1 prerequisites, inheritance poisoning prevention, login/signup form protection
**Avoids:** Pitfalls 1 (login form), 2 (active state), 3 (offcanvas), 6 (login modal toggle)

Work items:
- Wrap navigation sections in `<div id="sidebar-nav">` and `<div id="mobile-sidebar-nav">`
- Add `hx-boost="false"` on all modal shell `<div>` elements across page templates
- Add `hx-select="unset"` and explicit `hx-swap="innerHTML"` on all modal trigger buttons (Add/Edit/Delete)
- Add `hx-boost="false"` on the login form (`/j_security_check`)
- Add `hx-boost="false"` on the logout link
- Add `hx-boost="false"` on any signup links/forms
- Add `hx-disinherit="hx-select hx-target hx-swap"` on the `#main-content` container
- Add `htmx:beforeSwap` listener to close UIkit offcanvas before body swap
- Configure `refreshOnHistoryMiss: true` (F14)

### Phase 2: Enable hx-boost and Simplify Resources (Breaking Change)

**Rationale:** With all protections from Phase 1 in place, enabling hx-boost on the body is safe. Server-side simplification follows immediately because the HX-Request branching becomes dead code.
**Delivers:** AJAX-powered navigation, simplified server endpoints, browser history support, active navigation state, automatic title updates.
**Addresses:** F1, F2, F3, F4, F5, F6
**Avoids:** Pitfall 4 (D3 graph), Pitfall 5 (history fragment restore)

Work items:
- Add `hx-boost="true" hx-target="#main-content" hx-select="#main-content" hx-swap="innerHTML" hx-select-oob="#sidebar-nav,#mobile-sidebar-nav" hx-push-url="true"` to `<body>` in `base.html`
- Add `hx-boost="false"` to graph page navigation links (pragmatic D3 workaround)
- Remove `@HeaderParam("HX-Request")` and if/else branching from GenderResource, TitleResource, RelationshipResource, PersonResource, PersonRelationshipResource, GraphResource list endpoints
- Add separate `/table` endpoint on PersonResource for search/filter/pagination
- Add separate `/table` endpoint on PersonRelationshipResource if it has filter/pagination
- Update filter/sort/pagination `hx-get` URLs in Person and PersonRelationship templates to use the new `/table` endpoints
- Add `Vary: HX-Request` header if any endpoint still branches on request headers (F7)

### Phase 3: Polish and Hardening

**Rationale:** With core functionality working, add quality-of-life improvements and ensure edge cases are handled.
**Delivers:** head-support extension, DX improvements, documentation updates.
**Addresses:** F8, F10, F11
**Avoids:** Technical debt from undocumented patterns

Work items:
- Install head-support HTMX extension via CDN (F8)
- Extract common template variables (userName, currentPage) into shared CDI bean or base method (F10)
- Standardize modal naming conventions across resources (F11)
- Update `docs/ARCHITECTURE.md` to document the hx-boost pattern and remove HX-Request branching examples
- Comprehensive testing of all interaction patterns (see "Looks Done But Isn't" checklist from PITFALLS.md)

### Phase Ordering Rationale

- **Phase 1 before Phase 2 is non-negotiable.** If hx-boost is enabled without modal protections, every CRUD operation breaks immediately (empty modal content, forms targeting wrong containers). The research is unanimous: protections first, activation second.
- **Phase 2 combines client and server changes** because they are tightly coupled. Enabling hx-boost without removing HX-Request branching creates the history-fragment-restore bug (Pitfall 5). Both must happen together.
- **Phase 3 is independent** and can be deferred without affecting correctness. These are genuine improvements but not blocking.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 1:** The offcanvas/UIkit interaction needs hands-on validation. The `htmx:beforeSwap` approach is documented but the UIkit MutationObserver behavior with HTMX innerHTML swaps has not been verified in this specific codebase. Plan for experimentation time.
- **Phase 2:** The PersonResource search/filter/pagination refactoring needs careful analysis of the current query parameter handling to design the `/table` endpoint correctly. Review the actual template `hx-get` targets and `hx-include` patterns.

Phases with standard patterns (skip research-phase):
- **Phase 2 (hx-boost activation):** The body-level attribute configuration is thoroughly documented with HIGH confidence across all four research files. The exact attributes and values are specified.
- **Phase 2 (HX-Request removal):** Straightforward code deletion. The before/after patterns are precisely documented.
- **Phase 3:** All items are well-documented standard patterns.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | No stack changes needed. All HTMX attributes verified against official docs (htmx.org). |
| Features | HIGH | Feature list derived from codebase analysis + official HTMX patterns. Dependency graph is clear. |
| Architecture | HIGH | Three interaction patterns (boost nav, modal CRUD, table refresh) are well-documented. The hx-disinherit solution is verified via GitHub issue #3243. |
| Pitfalls | HIGH | All critical pitfalls have documented prevention strategies. Login form, inheritance poisoning, and D3 graph issues are well-understood failure modes. |

**Overall confidence:** HIGH

All research was sourced from official HTMX documentation, confirmed GitHub issues from the HTMX repository, the Hypermedia Systems book (by the HTMX creator), and direct codebase analysis. The patterns are mature and well-tested in the community.

### Gaps to Address

- **UIkit offcanvas + HTMX body swap interaction:** Theoretical solution documented but needs hands-on validation in this codebase. UIkit's MutationObserver should handle DOM re-initialization, but edge cases with overlay state and scroll locking need testing. Plan for this during Phase 1 implementation.
- **PersonResource filter/pagination exact behavior:** The current `hx-get`, `hx-include`, and query parameter patterns need detailed template inspection before designing the `/table` endpoint. The research identifies the need but the exact API contract depends on current template markup.
- **Plain forms inside content area:** Research flags that any plain `<form>` elements (without explicit hx-* attributes) inside the content area would be boosted. Need to audit all templates for non-HTMX forms that might be affected. The login form is addressed, but other forms may exist.
- **hx-select-oob behavior with hx-select:** Research confirms these work independently (hx-select filters the main swap, hx-select-oob handles separate OOB extractions), but this combination should be explicitly verified during Phase 2 testing.

## Sources

### Primary (HIGH confidence)
- [htmx.org official attribute docs](https://htmx.org/attributes/) -- hx-boost, hx-select, hx-target, hx-push-url, hx-disinherit, hx-select-oob, hx-swap
- [HTMX reference (request headers)](https://htmx.org/reference/) -- HX-Request, HX-Boosted header behavior
- [HTMX documentation (boosting section)](https://htmx.org/docs/#boosting) -- boost mechanics and defaults
- [HTMX quirks page](https://htmx.org/quirks/) -- known caveats with hx-boost
- [GitHub Issue #983](https://github.com/bigskysoftware/htmx/issues/983) -- canonical discussion of hx-boost + hx-select + hx-target pattern
- [GitHub Issue #3243](https://github.com/bigskysoftware/htmx/issues/3243) -- inheritance poisoning problem and workarounds
- [Hypermedia Systems](https://hypermedia.systems/htmx-patterns/) -- HTMX patterns by the framework creator
- Codebase analysis of 6 resource classes and all templates -- direct code inspection

### Secondary (MEDIUM confidence)
- [GitHub Issue #3037](https://github.com/bigskysoftware/htmx/issues/3037) -- history restore breaks HX-Request behavior
- [GitHub Issue #497](https://github.com/bigskysoftware/htmx/issues/497) -- hx-boost fragment vs full page on history
- [GitHub Discussion #2041](https://github.com/bigskysoftware/htmx/discussions/2041) -- Is hx-boosting the whole body a good practice?
- [Quarkus Discussion #41114](https://github.com/quarkusio/quarkus/discussions/41114) -- Qute fragments vs hx-boost (Quarkus team confirms both valid)
- [Ben Nadel: conditionally preventing hx-boost](https://www.bennadel.com/blog/4784-conditionally-preventing-hx-boost-in-htmx-using-an-extension.htm)

---
*Research completed: 2026-02-19*
*Ready for roadmap: yes*
