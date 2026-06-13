---
name: htmx-patterns
description: Use when building or modifying HTMX-driven UI in this project — choosing swap strategies, wiring OOB updates, implementing CRUD modals, click-to-edit, active search, infinite scroll, lazy loading, polling, inline validation, tabs, cascading selects, or file upload; integrating HTMX with Qute fragments and UIkit; debugging HTMX request lifecycle, CSS classes, or events.
---

# HTMX Patterns for Hypermedia-Driven Applications

Reference for building interactive UIs using HTMX 2.0.8 with Quarkus + Qute + UIkit 3.25, following the hypermedia-first philosophy: server returns HTML, HTMX handles DOM updates.

## Reference Files

Content lives in three files under `references/`. Load whichever matches the task — most tasks only need one.

| File | Contents | Load when |
|------|----------|-----------|
| `references/attributes.md` | Core attributes, swap strategies, triggers, OOB, lifecycle, CSS transitions, view transitions, morphing, events & JS API, configuration, extensions, indicators, inheritance | Authoring `hx-*` attributes, picking a swap mode, wiring triggers, or debugging the request/swap pipeline |
| `references/patterns.md` | Modal CRUD, active search, click-to-edit, edit/delete row, bulk update, infinite scroll, lazy loading, validation, tabs, cascading selects, progress bar, file upload, polling, keyboard shortcuts, sortable, multi-region updates | Implementing or modifying a concrete UI flow |
| `references/integration.md` | JS integration, debugging, security (CSRF/CSP/XSS), anti-patterns, Qute fragments + `@CheckedTemplate` | Wiring JS hooks, applying security hardening, structuring resources & templates |

## Core Philosophy

### Hypermedia-Driven Application Principles

| Principle | Description |
|-----------|-------------|
| HTML Over the Wire | Server returns HTML fragments, not JSON |
| Server as Source of Truth | State lives on server; client reflects server state |
| Locality of Behavior | Keep behavior co-located with the element it affects |
| Progressive Enhancement | Works without JS; HTMX enhances the experience |
| Reduced Complexity | No client-side state management needed |

### Critical Rules

- HTMX expects **HTML responses** from the server, not JSON.
- Most attributes **inherit** to children. **Not inherited:** `hx-trigger`, `hx-on*`, `hx-swap-oob`, `hx-preserve`, `hx-history-elt`, `hx-validate`. Use `hx-disinherit` or `unset` to stop inheritance.
- Default swap strategy is `innerHTML`. Always confirm the intended swap method.
- Non-GET requests automatically include the closest enclosing form's values.
- Use `hx-boost="true"` for progressive enhancement — pages must work without JS.
- Escape all user-supplied content server-side to prevent XSS.
- HTMX adds/removes CSS classes (`htmx-request`, `htmx-swapping`, `htmx-added`, `htmx-settling`) during the request lifecycle — use these for transitions and indicators.
- All `hx-*` attributes can also be written as `data-hx-*` for HTML validation compliance.

## When to Use HTMX

| Use Case | HTMX Approach |
|----------|---------------|
| Form submissions | `hx-post` with validation response |
| Dynamic content loading | `hx-get` with `hx-target` |
| Real-time updates | Polling with `hx-trigger="every Ns"` or SSE |
| Partial page updates | Target specific elements with `hx-target` |
| Modal dialogs | Load content into modal container |
| Progressive enhancement | `hx-boost="true"` on links and forms |

## When NOT to Use HTMX

| Scenario | Reason | Use Instead |
|----------|--------|-------------|
| Offline-first or collaborative editing | HTMX assumes server is source of truth and a network round-trip per interaction | A SPA framework (React, Svelte) or CRDT-backed client |
| Heavy DOM manipulation with no server involvement | HTMX needs HTML responses to drive swaps | Vanilla JS or Alpine/Stimulus for purely client work |
| JSON APIs consumed by mobile or third-party clients | HTMX expects HTML; mixing JSON+HTML on the same endpoint complicates contracts | Serve JSON from a separate REST endpoint |
| Animation-only interactions independent of server state | A round-trip per frame is wasteful | CSS transitions, Web Animations API, GSAP |
| Real-time bidirectional streams (chat, multiplayer) | Polling is too coarse; HTMX request lifecycle is request/response shaped | `hx-ext="ws"` (WebSocket extension) or a dedicated client |
| Static content that never changes | HTMX adds no value without dynamic updates | Plain HTML |

## Choosing the Right Update Pattern

| You need to… | Use | Reference |
|--------------|-----|-----------|
| Replace one region with the response | `hx-target` + `hx-swap` | `attributes.md` §1, §2 |
| Update several disjoint regions in one response | OOB swaps (`hx-swap-oob`) | `attributes.md` §4 |
| Pick specific bits of the response for OOB targets the requester knows about | `hx-select-oob` on the requesting element | `attributes.md` §1; `patterns.md` §17 |
| Refresh element X when action on element Y completes | `HX-Trigger` response header + `hx-trigger="evt from:body"` | `attributes.md` §5; `patterns.md` §17 |
| Append rows / log entries without losing existing content | `hx-swap="beforeend"` or OOB `beforeend` | `attributes.md` §2, §4 |
| Replace the row the action originated from | `hx-target="closest tr"` + `hx-swap="outerHTML"` | `patterns.md` §4, §5 |

## Project Conventions

### Entity-Prefixed IDs

Used across all HTMX targets to avoid collisions when the same component appears on multiple pages:

| Element | Pattern | Example |
|---------|---------|---------|
| Modal shell | `#{entity}-modal` | `#item-modal` |
| Modal body | `#{entity}-modal-body` | `#item-modal-body` |
| Table container | `#{entity}-table-container` | `#item-table-container` |
| Table body | `#{entity}-table-body` | `#item-table-body` |
| Table row | `#{entity}-row-{id}` | `#item-row-42` |

### Form Input ID Prefixes

Create and edit fragments render into the **same** modal body (`#{entity}-modal-body`), so their `<input id="…">` attributes would collide if both were ever in the DOM at once (e.g., during a swap). Prefix them by mode:

| Fragment | Input ID pattern | Example |
|----------|------------------|---------|
| `modal_create` | `create-{entity}-{field}` | `create-supplier-code` |
| `modal_edit` | `edit-{entity}-{field}` | `edit-supplier-code` |

The `name` attribute stays unprefixed (`name="code"`) so the server binds the same field name regardless of mode.

### Template File Layout

Qute templates live at `src/main/resources/templates/{ResourceClassName}/{entity}.html` — keyed by the resource class, not the entity alone. For example, `SupplierResource` → `templates/SupplierResource/supplier.html`. Fragments inside the same file are addressed as `supplier$table`, `supplier$modal_create`, etc.

### Page-Level Fragment Parameters

The top-level page (the part outside any `{#fragment}`) wraps `{#include base}` and conventionally takes:

| Parameter | Purpose |
|-----------|---------|
| `String title` | Browser title and page heading via `base.html` |
| `String currentPage` | Nav-highlight key consumed by `base.html` |
| `String userName` | From `SecurityIdentity.getPrincipal().getName()`, displayed in the chrome |
| `List<{Entity}> {entities}` | Data for the initial table render |

### HTMX vs Full-Page Dispatch

Detect HTMX requests server-side via `@HeaderParam("HX-Request")` and return the fragment template (`Templates.item$table(...)`) for HTMX, the full page template (`Templates.item(...)`) otherwise. See `references/integration.md` §5 for the `@CheckedTemplate` setup and `references/patterns.md` §1 for preserving pagination/filter state through OOB table refreshes.

## Common Patterns Checklist

- [ ] Use `hx-target` to specify where response goes
- [ ] Use `hx-swap-oob` for updating multiple elements
- [ ] Use `hx-trigger` modifiers for debouncing/throttling
- [ ] Use `hx-indicator` for loading states
- [ ] Use `hx-confirm` for destructive actions
- [ ] Use `hx-on::` for JavaScript integration (kebab-case in attributes)
- [ ] Use `rendered=false` for Qute page fragments
- [ ] Use `hx-sync` to prevent duplicate requests
- [ ] Use `hx-disabled-elt` to prevent double-clicks
- [ ] Use `hx-boost` for progressive enhancement
- [ ] Use `<template>` wrapper for OOB table rows

## Related Skills

- `java-patterns` — `@CheckedTemplate` and modern Java idioms used in resources
- `maven-java` — Quarkus extensions configured via `pom.xml`
- `postgresql-java` — Panache repository patterns invoked from resources that render these fragments

---

*HTMX 2.0.8 · UIkit 3.25 · Quarkus 3.30.3 · Last Updated: 2026-02-17*
