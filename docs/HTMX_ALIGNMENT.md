# HTMX Alignment Analysis

Comparison of the project's HTMX usage against the patterns and guidelines in `.claude/skills/htmx-patterns/SKILL.md`.

**Scope:** All 14 template files and 8 resource classes.

---

## Well-Aligned Areas

### 1. Modal CRUD Pattern (SKILL.md Section 13)

The Gender, Title, Relationship, Person, and PersonRelationship pages all follow the exact pattern from Section 13:

- Entity-prefixed IDs (`#gender-modal`, `#gender-modal-body`, `#gender-table-container`, `#gender-row-{id}`) match the naming conventions table exactly
- Static modal shell with `uk-modal="bg-close: false"`
- Add button with `hx-get` + `hx-target="#entity-modal-body"` + `hx-on::after-request="UIkit.modal(...).show()"`
- Table action buttons (edit/delete) follow the same pattern
- `{#fragment id='table' rendered=false}` for data tables
- Empty state handling (`{#if items.isEmpty()}`) present consistently

### 2. OOB Updates (Section 5)

- Success fragments use `hx-swap-oob="innerHTML"` for full-table refresh (`modal_success` fragments)
- Single-row updates use `<template>` wrapper with `hx-swap-oob="outerHTML"` on `<tr>` elements (`modal_success_row` fragments), correctly following the HTML encapsulation rules
- Delete success uses `hx-swap-oob="delete"` inside `<template>`

### 3. Modal Close via `hx-on::load` (Section 32)

All success fragments use `<div hx-on::load="UIkit.modal('#entity-modal').hide()"></div>`, matching the pattern from Section 13 exactly.

### 4. Delete Confirmation Pattern (Section 13)

- Uses `{#if error??}` for null-safe error checking
- Conditionally hides delete button on error (`{#if !error??}`)
- Confirmation shows entity details before deletion

### 5. Qute Fragment Integration (Section 36)

- All fragments use `rendered=false`
- Fragment naming follows the conventions table (`table`, `modal_create`, `modal_edit`, `modal_delete`, `modal_success`, `modal_delete_success`)
- `{#include $table /}` for same-template includes
- Type declarations at fragment top

### 6. Active Search Pattern (Section 14)

`person.html` and `personRelationship.html` use:

```html
hx-trigger="input changed delay:300ms, search"
hx-target="#person-table-container"
hx-include="closest form"
```

This matches Section 14's debounced search pattern. The 300ms delay is a reasonable deviation from the example's 500ms.

### 7. Form Submissions

All create forms use `hx-post`, edit forms use `hx-put`, delete buttons use `hx-delete` with appropriate `hx-target` pointing to the modal body for in-modal response handling.

### 8. Pagination with `hx-push-url` (Sections 2 and 6)

`person.html` uses `hx-get` with `hx-push-url="true"` on pagination links, enabling history management. The `network.html` depth selector also uses `hx-push-url`.

---

## Deviations and Areas for Improvement

### 1. `HX-Request` Header Checking -- Debatable Pattern

All 6 list endpoints check `@HeaderParam("HX-Request")` to return either a fragment or full page:

| Resource | Line |
|---|---|
| `GenderResource.java` | 62 |
| `TitleResource.java` | 62 |
| `RelationshipResource.java` | 62 |
| `PersonResource.java` | 127 |
| `PersonRelationshipResource.java` | 414 |
| `GraphResource.java` | 121 |

SKILL.md Section 6 documents this header and shows a code example using this exact pattern, so it is sanctioned by the guide. However, the alternative (`hx-select` on the client side to extract content from a full-page response) would eliminate server-side branching. The current approach avoids sending unnecessary HTML over the wire, at the cost of maintaining two return paths per endpoint.

### 2. No `hx-boost` Usage

SKILL.md Section 2 recommends `hx-boost="true"` for progressive enhancement. The current `fragments/navigation.html` has plain `<a href="...">` links with no `hx-boost`, `hx-target`, or `hx-push-url` attributes. Every sidebar navigation click triggers a full page reload. HTMX-powered partial updates only happen within CRUD modals and filter/pagination controls.

**Impact:** Not wrong (pages work without JS as progressive enhancement), but navigation between sections (Home, People, Graph, Maintenance) does not benefit from HTMX's partial page updates.

### 3. No `hx-indicator` Usage (Section 30)

None of the templates use `hx-indicator` or the `.htmx-indicator` CSS class for loading states. The search inputs, form submissions, and modal content loads have no loading indicators. On slow connections, users get no visual feedback that a request is in progress.

### 4. No `hx-sync` for Double-Submit Prevention (Section 2)

SKILL.md Section 2 documents `hx-sync` (e.g., `hx-sync="this:abort"` on forms). None of the create/edit forms use `hx-sync` to prevent double submissions.

### 5. No `hx-disabled-elt` for Button States (Section 2)

SKILL.md Section 2 lists `hx-disabled-elt="this"` for disabling elements during requests. No form submit buttons use this attribute, meaning users can click "Save" or "Delete" multiple times during a slow request.

### 6. No `hx-confirm` for Delete Actions (Section 17)

SKILL.md Section 17 (Delete Row Pattern) uses `hx-confirm="Are you sure?"` for inline delete confirmations. The project instead uses a modal-based confirmation flow (load delete form into modal, then confirm). This is a valid alternative but more complex.

### 7. Login Form Not HTMX-Enhanced

`base.html` line 128 has a traditional `<form action="/j_security_check" method="POST">`. The signup form is also traditional. These don't use HTMX, which is acceptable for security forms (Quarkus form authentication requires a standard POST), but login errors cause full page reloads.

### 8. Missing HTMX Meta Configuration (Section 11)

The current `base.html` has no `<meta name="htmx-config">` tag. SKILL.md Section 11 recommends configuring key options. Notably missing:

- `selfRequestsOnly: true` (Section 34 security hardening)
- `globalViewTransitions: true` (Section 8)

### 9. Graph Page Mixes HTMX and Heavy JavaScript

`graph.html` loads D3.js and uses a custom `graph.js` script for the force-directed graph visualization. The node context menu and interactions are pure JavaScript, not HTMX. SKILL.md Section 35 warns against mixing heavy client-side state with HTMX. However, this is a legitimate use case since D3 force graphs cannot be server-rendered. The `network.html` page properly uses HTMX for the depth selector dropdown.

### 10. Row Duplication Between Fragments

The table row markup in `modal_success_row` fragments duplicates the row markup from the `table` fragment. For example, in `gender.html` the row at lines 50-75 is nearly identical to the row at lines 150-172. If a column is added, both places must be updated. SKILL.md does not address this directly, but it is a DRY concern.

---

## Summary

| SKILL.md Area | Status | Notes |
|---|---|---|
| Modal CRUD (S13) | Aligned | All 5 CRUD pages follow the exact pattern |
| OOB Updates (S5) | Aligned | Correct use of `innerHTML`, `outerHTML`, `delete`, `<template>` wrapping |
| Qute Fragments (S36) | Aligned | `rendered=false`, naming conventions, `$table` includes |
| Active Search (S14) | Aligned | Debounced input with `delay:300ms` |
| Swap Strategies (S3) | Aligned | Appropriate use of `innerHTML` and `outerHTML` |
| JS Integration (S32) | Aligned | `hx-on::` used correctly, graph page is a valid exception |
| Anti-Patterns (S35) | Clean | No JSON-over-wire, no heavy client state (except graph) |
| CSS Transitions (S7) | Not used | No transition animations on swaps |
| `hx-boost` (S2) | Not used | Navigation is traditional full-page |
| `hx-indicator` (S30) | Not used | No loading feedback |
| `hx-sync` (S2) | Not used | No double-submit prevention |
| `hx-disabled-elt` (S2) | Not used | Buttons not disabled during requests |
| `hx-confirm` (S17) | Alternative | Uses modal confirmation instead |
| HTMX Config (S11) | Missing | No `<meta name="htmx-config">` |
| Security (S34) | Partial | No `selfRequestsOnly`, no `hx-disable` on user content |
| HX-Request header (S6) | Used | Documented in SKILL.md but debatable |

---

*Analysis date: 2026-02-18*
