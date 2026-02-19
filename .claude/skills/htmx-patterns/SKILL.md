---
name: htmx-patterns
description: HTMX design patterns, idioms, and best practices for hypermedia-driven applications. Use for building frontend components or refactoring code for user interaction in HTML pages.
---

# HTMX Patterns for Hypermedia-Driven Applications

This skill provides patterns for building interactive web UIs using HTMX with server-side rendering. It follows the hypermedia-first philosophy where the server returns HTML and HTMX handles DOM updates.

---

## Table of Contents

1. [Core Philosophy](#1-core-philosophy)
2. [Core Attributes Reference](#2-core-attributes-reference)
3. [Swap Strategies](#3-swap-strategies)
4. [Trigger Patterns](#4-trigger-patterns)
5. [Out-of-Band (OOB) Updates](#5-out-of-band-oob-updates)
6. [Request Lifecycle](#6-request-lifecycle)
7. [CSS Transitions](#7-css-transitions)
8. [View Transitions API](#8-view-transitions-api)
9. [Morphing](#9-morphing)
10. [Events & API Reference](#10-events--api-reference)
11. [Configuration](#11-configuration)
12. [Extensions](#12-extensions)
13. [Modal CRUD Pattern](#13-modal-crud-pattern)
14. [Active Search Pattern](#14-active-search-pattern)
15. [Click-to-Edit Pattern](#15-click-to-edit-pattern)
16. [Edit Row Pattern](#16-edit-row-pattern)
17. [Delete Row Pattern](#17-delete-row-pattern)
18. [Bulk Update Pattern](#18-bulk-update-pattern)
19. [Infinite Scroll / Click-to-Load](#19-infinite-scroll--click-to-load)
20. [Lazy Loading](#20-lazy-loading)
21. [Inline Validation](#21-inline-validation)
22. [Tabs Pattern](#22-tabs-pattern)
23. [Cascading Selects](#23-cascading-selects)
24. [Progress Bar Pattern](#24-progress-bar-pattern)
25. [File Upload with Progress](#25-file-upload-with-progress)
26. [Polling Patterns](#26-polling-patterns)
27. [Keyboard Shortcuts](#27-keyboard-shortcuts)
28. [Drag & Drop / Sortable](#28-drag--drop--sortable)
29. [Updating Other Content](#29-updating-other-content)
30. [Request Indicators](#30-request-indicators)
31. [Attribute Inheritance](#31-attribute-inheritance)
32. [JavaScript Integration](#32-javascript-integration)
33. [Debugging](#33-debugging)
34. [Security Considerations](#34-security-considerations)
35. [Anti-Patterns](#35-anti-patterns)
36. [Qute Template Integration](#36-qute-template-integration)

---

## 1. Core Philosophy

### Hypermedia-Driven Application (HDA) Principles

| Principle | Description |
|-----------|-------------|
| **HTML Over the Wire** | Server returns HTML fragments, not JSON |
| **Server as Source of Truth** | State lives on server; client reflects server state |
| **Locality of Behavior (LoB)** | Keep behavior co-located with the element it affects |
| **Progressive Enhancement** | Works without JS; HTMX enhances the experience |
| **Reduced Complexity** | No client-side state management needed |

### Critical Rules

- HTMX expects **HTML responses** from the server, not JSON.
- Most attributes **inherit** to child elements. **Not inherited:** `hx-trigger`, `hx-on*`, `hx-swap-oob`, `hx-preserve`, `hx-history-elt`, `hx-validate`. Use `hx-disinherit` or `unset` to stop inheritance.
- Default swap strategy is `innerHTML`. Always confirm the intended swap method.
- Non-GET requests automatically include the closest enclosing form's values.
- Use `hx-boost="true"` for progressive enhancement — pages must work without JS.
- Escape all user-supplied content server-side to prevent XSS.
- HTMX adds/removes CSS classes during the request lifecycle — use these for transitions and indicators.
- All `hx-*` attributes can also be written as `data-hx-*` for HTML validation compliance.

### When to Use HTMX

| Use Case | HTMX Approach |
|----------|---------------|
| Form submissions | `hx-post` with validation response |
| Dynamic content loading | `hx-get` with `hx-target` |
| Real-time updates | Polling with `hx-trigger="every Ns"` or SSE |
| Partial page updates | Target specific elements with `hx-target` |
| Modal dialogs | Load content into modal container |
| Progressive enhancement | `hx-boost="true"` on links and forms |

---

## 2. Core Attributes Reference

### Request Attributes

| Attribute | Description | Example |
|-----------|-------------|---------|
| `hx-get` | Issue GET request | `hx-get="/items"` |
| `hx-post` | Issue POST request | `hx-post="/items"` |
| `hx-put` | Issue PUT request | `hx-put="/items/1"` |
| `hx-patch` | Issue PATCH request | `hx-patch="/items/1"` |
| `hx-delete` | Issue DELETE request | `hx-delete="/items/1"` |

GET appends parameters to query string; POST/PUT/PATCH send in request body.

### Response Handling Attributes

| Attribute | Description | Example |
|-----------|-------------|---------|
| `hx-target` | Element to swap content into | `hx-target="#modal-content"` |
| `hx-swap` | How to swap content | `hx-swap="innerHTML"` |
| `hx-select` | CSS selector to pick from response | `hx-select="#main-content"` |
| `hx-select-oob` | Select OOB content from response | `hx-select-oob="#count:innerHTML"` |

#### Extended CSS Selectors for `hx-target`

| Selector | Description | Example |
|----------|-------------|---------|
| `closest` | Find closest ancestor matching selector | `hx-target="closest tr"` |
| `find` | Find first descendant matching selector | `hx-target="find .content"` |
| `next` | Find next sibling matching selector | `hx-target="next .output"` |
| `previous` | Find previous sibling matching selector | `hx-target="previous .output"` |

#### `hx-select-oob` on Requesting Elements

Pick specific elements from the response for OOB swap, independent of the main target:

```html
<button hx-get="/data"
        hx-target="#results"
        hx-select="#results"
        hx-select-oob="#count:innerHTML, #status:outerHTML">
    Load
</button>
```

Format: `#id:strategy, #id:strategy, ...`

### Behavior Attributes

| Attribute | Description | Example |
|-----------|-------------|---------|
| `hx-trigger` | Event that triggers request | `hx-trigger="click"` |
| `hx-confirm` | Confirmation dialog | `hx-confirm="Delete this?"` |
| `hx-prompt` | Input dialog (value sent via `HX-Prompt` header) | `hx-prompt="Enter name:"` |
| `hx-indicator` | Loading indicator element | `hx-indicator="#spinner"` |
| `hx-disabled-elt` | Disable elements during request | `hx-disabled-elt="this"` |
| `hx-push-url` | Push URL to browser history | `hx-push-url="true"` |
| `hx-replace-url` | Replace current URL in browser history | `hx-replace-url="true"` |

### Data Attributes

| Attribute | Description | Example |
|-----------|-------------|---------|
| `hx-vals` | Additional values as JSON | `hx-vals='{"key":"value"}'` |
| `hx-vals` | Dynamic values with `js:` prefix | `hx-vals="js:{ts: Date.now()}"` |
| `hx-include` | Include other elements' values | `hx-include="[name='email']"` |
| `hx-params` | Filter parameters | `hx-params="*"` or `hx-params="not password"` |
| `hx-headers` | Add request headers | `hx-headers='{"X-Custom":"value"}'` |
| `hx-encoding` | Set request encoding | `hx-encoding="multipart/form-data"` |

### History Management Attributes

| Attribute | Description | Example |
|-----------|-------------|---------|
| `hx-push-url` | Add history entry | `hx-push-url="true"` or `hx-push-url="/custom-url"` |
| `hx-replace-url` | Modify current history entry | `hx-replace-url="true"` |
| `hx-history` | Prevent page from being cached in history | `hx-history="false"` |
| `hx-history-elt` | Specify element to cache in history snapshot | `hx-history-elt` |

### Miscellaneous Attributes

| Attribute | Description | Example |
|-----------|-------------|---------|
| `hx-boost` | Convert links/forms to AJAX requests | `hx-boost="true"` |
| `hx-ext` | Enable HTMX extensions | `hx-ext="sse"` |
| `hx-preserve` | Maintain element across swaps (requires `id`) | `hx-preserve` |
| `hx-sync` | Control concurrent requests | `hx-sync="closest form:abort"` |
| `hx-validate` | Force form validation for non-form elements | `hx-validate="true"` |
| `hx-request` | Configure request behavior | `hx-request="timeout:5000"` |
| `hx-disable` | Block all HTMX processing on element and children | `hx-disable` |

#### `hx-sync` Strategies

| Strategy | Behavior |
|----------|----------|
| `drop` | Drop new request if one is in-flight (default) |
| `abort` | Abort current request for new one |
| `replace` | Abort current, replace with new |
| `queue first` | Queue first request, drop rest |
| `queue last` | Queue last request, drop rest |
| `queue all` | Queue all requests |

```html
<!-- Prevent double-submit on form -->
<form hx-post="/submit" hx-sync="this:abort">...</form>

<!-- Only latest search request matters -->
<input hx-post="/search" hx-sync="closest form:replace">
```

---

## 3. Swap Strategies

### Available Swap Values

| Value | Behavior |
|-------|----------|
| `innerHTML` | Replace inner content (default) |
| `outerHTML` | Replace entire element |
| `textContent` | Replace text content, no HTML parsing |
| `beforebegin` | Insert before target element (sibling) |
| `afterbegin` | Insert as first child |
| `beforeend` | Insert as last child |
| `afterend` | Insert after target element (sibling) |
| `delete` | Delete target element |
| `none` | No swap (useful for side effects; OOB swaps and headers still process) |

Note: `outerHTML` on `<body>` auto-converts to `innerHTML`. `hx-swap` is inherited.

### Swap Modifiers

| Modifier | Effect | Default |
|----------|--------|---------|
| `swap:<time>` | Delay between old content removal and new insertion | `0` |
| `settle:<time>` | Delay between insertion and settle phase | `20ms` |
| `transition:true` | Use View Transitions API | `false` |
| `ignoreTitle:true` | Don't update `document.title` from response | `false` |
| `scroll:top` | Scroll target to top after swap | — |
| `scroll:bottom` | Scroll target to bottom | — |
| `scroll:<selector>:top` | Scroll element to top | — |
| `show:top` | Scroll viewport so target top is visible | — |
| `show:bottom` | Scroll viewport so target bottom is visible | — |
| `show:<selector>:top` | Scroll viewport so element top is visible | — |
| `show:window:top` | Scroll viewport to window top | — |
| `show:none` | Disable scroll-into-view | — |
| `focus-scroll:true` | Auto-scroll to focused element after swap | `false` |

```html
<!-- Delay swap for animation -->
<button hx-delete="/item/1" hx-swap="outerHTML swap:1s">Delete</button>

<!-- Scroll target into view -->
<button hx-get="/page/2" hx-swap="innerHTML show:top">Next Page</button>

<!-- Settle time for CSS transitions -->
<button hx-get="/content" hx-swap="innerHTML settle:500ms">Load</button>

<!-- Combine multiple modifiers -->
<div hx-get="/data" hx-swap="innerHTML swap:300ms settle:100ms scroll:top">
    Content
</div>

<!-- Fire-and-forget (no DOM update) -->
<button hx-post="/track" hx-swap="none">Track Click</button>
```

---

## 4. Trigger Patterns

### Default Triggers by Element Type

| Element | Default Trigger |
|---------|-----------------|
| `<input>` | `change` |
| `<textarea>` | `change` |
| `<select>` | `change` |
| `<form>` | `submit` |
| Everything else | `click` |

**`hx-trigger` is NOT inherited.**

### Basic Triggers

```html
<!-- Default: click for buttons/links, change for inputs, submit for forms -->
<button hx-get="/data">Click triggers GET</button>

<!-- Explicit trigger -->
<input hx-post="/search" hx-trigger="keyup">

<!-- Multiple triggers -->
<input hx-post="/search" hx-trigger="keyup, search">
```

### Trigger Modifiers

| Modifier | Description | Example |
|----------|-------------|---------|
| `changed` | Only if value changed | `hx-trigger="keyup changed"` |
| `delay:Ns` | Debounce for N seconds | `hx-trigger="keyup delay:500ms"` |
| `throttle:Ns` | Throttle to once per N seconds | `hx-trigger="scroll throttle:500ms"` |
| `once` | Trigger only once | `hx-trigger="load once"` |
| `from:selector` | Listen on different element | `hx-trigger="click from:body"` |
| `target:selector` | Filter to events from matching elements | `hx-trigger="click target:.btn"` |
| `consume` | Prevent event from propagating | `hx-trigger="click consume"` |
| `queue:strategy` | Queue strategy (`first`, `last`, `all`, `none`) | `hx-trigger="click queue:last"` |

### Special Triggers

```html
<!-- On page load -->
<div hx-get="/initial-data" hx-trigger="load">Loading...</div>

<!-- When element becomes visible -->
<div hx-get="/lazy-content" hx-trigger="revealed">Loading...</div>

<!-- When element enters viewport (with options) -->
<div hx-get="/content" hx-trigger="intersect once">Loading...</div>
<div hx-get="/content" hx-trigger="intersect once threshold:0.5">Loading...</div>

<!-- Polling -->
<div hx-get="/status" hx-trigger="every 5s">Status: checking...</div>

<!-- Conditional triggers with JavaScript filters -->
<input hx-post="/search" hx-trigger="keyup[key=='Enter']">
<button hx-post="/action" hx-trigger="click[ctrlKey]">Ctrl+Click</button>

<!-- Conditional polling (only when tab is visible) -->
<div hx-get="/status" hx-trigger="every 2s [document.visibilityState === 'visible']">
    Status
</div>
```

### Active Search Pattern Trigger

```html
<input type="search" name="search"
       hx-post="/search"
       hx-trigger="input changed delay:500ms, keyup[key=='Enter'], load"
       hx-target="#search-results"
       hx-indicator=".htmx-indicator">
```

---

## 5. Out-of-Band (OOB) Updates

OOB swaps update multiple elements with a single response. Elements with matching IDs are swapped independently of the main target.

### Server Response with OOB

```html
<!-- Main response (swapped into hx-target) -->
<div id="modal-content" hx-on::load="UIkit.modal('#crud-modal').hide()">
    <div class="uk-alert uk-alert-success">Item saved successfully.</div>
</div>

<!-- OOB update (updates element with matching ID) -->
<div id="table-container" hx-swap-oob="innerHTML">
    <table><!-- updated table content --></table>
</div>
```

### OOB Swap Strategies

| Value | Behavior |
|-------|----------|
| `true` | Replace entire element by matching `id` (uses `outerHTML`) |
| `innerHTML` | Replace inner content |
| `outerHTML` | Replace entire element |
| `beforebegin` | Insert before element |
| `afterbegin` | Prepend inside element |
| `beforeend` | Append inside element |
| `afterend` | Insert after element |
| `delete` | Delete element |
| `none` | Do nothing |

```html
<!-- Replace innerHTML (default when hx-swap-oob="true") -->
<div id="notifications" hx-swap-oob="true">New content</div>

<!-- Replace outerHTML -->
<div id="user-row" hx-swap-oob="outerHTML">Updated row</div>

<!-- Append content -->
<div id="log" hx-swap-oob="beforeend">New log entry</div>

<!-- Delete element -->
<tr id="row-42" hx-swap-oob="delete"></tr>
```

### HTML Element Encapsulation for OOB

Some elements require valid HTML wrapping:

| Element Type | Must Be Wrapped In |
|---|---|
| `<tr>`, `<td>`, `<th>` | `<tbody>`, `<table>`, or `<template>` |
| `<li>` | `<ul>`, `<ol>`, `<div>`, `<span>`, or `<template>` |
| `<p>` | `<div>` or `<span>` |
| SVG elements | `<template>` + `<svg>` |

Use `<template>` to wrap table rows for valid HTML:

```html
<div>Main response content</div>

<template>
    <tr id="row-1" hx-swap-oob="outerHTML">
        <td>Updated data</td>
    </tr>
</template>
```

---

## 6. Request Lifecycle

### 11-Step Request Sequence

1. Element is triggered (e.g., click, change, load)
2. Values are gathered from the element and its `hx-include` targets
3. `htmx:configRequest` event fires — modify parameters/headers here
4. `htmx:beforeRequest` event fires — cancel with `preventDefault()`
5. Request is issued
6. `htmx:beforeSwap` event fires — modify swap behavior here
7. Swap is performed (default: `innerHTML` into `hx-target`)
8. `htmx:afterSwap` event fires
9. Settle phase — attributes are settled
10. `htmx:afterSettle` event fires
11. `htmx:load` event fires on new content

### CSS Class Timeline

| Class | Applied When | Removed When |
|-------|-------------|--------------|
| `htmx-request` | Request starts | Request ends |
| `htmx-swapping` | Before swap | After swap |
| `htmx-added` | After swap (new elements only) | After settle |
| `htmx-settling` | After swap | After settle |

### Request Headers (Sent Automatically)

| Header | Description |
|--------|-------------|
| `HX-Boosted` | `true` if boosted element |
| `HX-Current-URL` | Current URL of the browser |
| `HX-History-Restore-Request` | `true` if history restoration |
| `HX-Prompt` | User response to `hx-prompt` |
| `HX-Request` | Always `true` for HTMX requests |
| `HX-Target` | `id` of the target element |
| `HX-Trigger` | `id` of the triggered element |
| `HX-Trigger-Name` | `name` of the triggered element |

### Response Headers (Server Can Send)

| Header | Description |
|--------|-------------|
| `HX-Location` | Client-side redirect without full page reload |
| `HX-Push-Url` | Push URL into browser history |
| `HX-Redirect` | Full-page redirect |
| `HX-Refresh` | Full page refresh (`true`) |
| `HX-Replace-Url` | Replace current URL in history |
| `HX-Reswap` | Override `hx-swap` on the triggering element |
| `HX-Retarget` | Override `hx-target` on the triggering element |
| `HX-Reselect` | Override `hx-select` on the triggering element |
| `HX-Trigger` | Trigger client-side events |
| `HX-Trigger-After-Settle` | Trigger events after settle phase |
| `HX-Trigger-After-Swap` | Trigger events after swap phase |

### Server-Side HTMX Request Detection (Java/Quarkus)

```java
@GET
@Produces(MediaType.TEXT_HTML)
public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
    List<Item> items = itemRepository.listAllOrdered();
    if ("true".equals(hxRequest)) {
        return Templates.item$table(items);
    }
    return Templates.item(items);
}
```

### Response Status Codes

| Status | HTMX Behavior |
|--------|---------------|
| 200 OK | Normal swap into target |
| 204 No Content | No swap, request completed |
| 286 | Stop polling |
| 4xx/5xx | No swap by default; use `response-targets` extension or `htmx:beforeSwap` to customize |

---

## 7. CSS Transitions

HTMX works with CSS transitions when: element has a stable `id`, new content changes a CSS class/property, and CSS defines the transition.

### Fade-Out on Swap

```html
<style>
    .fade-me-out.htmx-swapping {
        opacity: 0;
        transition: opacity 1s ease-out;
    }
</style>
<button class="fade-me-out"
        hx-delete="/item/1"
        hx-swap="outerHTML swap:1s">
    Fade Me Out
</button>
```

### Fade-In on Addition

```html
<style>
    #fade-me-in.htmx-added {
        opacity: 0;
    }
    #fade-me-in {
        opacity: 1;
        transition: opacity 1s ease-out;
    }
</style>
<button id="fade-me-in"
        hx-post="/fade_in_demo"
        hx-swap="outerHTML settle:1s">
    Fade Me In
</button>
```

### Request In-Flight Animation

```html
<style>
    form.htmx-request {
        opacity: .5;
        transition: opacity 300ms linear;
    }
</style>
<form hx-post="/name" hx-swap="outerHTML">
    <label>Name:</label><input name="name" />
    <button type="submit">Submit</button>
</form>
```

### Delete Row Fade-Out

```css
tr.htmx-swapping td {
    opacity: 0;
    transition: opacity 1s ease-out;
}
```

---

## 8. View Transitions API

The View Transitions API provides native browser animations for DOM changes.

### Per-Element

```html
<div hx-get="/page" hx-swap="innerHTML transition:true">Content</div>
```

### Global

```javascript
htmx.config.globalViewTransitions = true;
```

### CSS Customization

```css
/* Default transition */
::view-transition-old(root) { animation: fade-out 0.2s ease-out; }
::view-transition-new(root) { animation: fade-in 0.2s ease-in; }

/* Named transition for specific element */
#content { view-transition-name: content; }
::view-transition-old(content) { animation: slide-out 0.3s; }
::view-transition-new(content) { animation: slide-in 0.3s; }
```

Cancel via `htmx:beforeTransition` event with `event.preventDefault()`.

---

## 9. Morphing

Morphing merges new HTML into the existing DOM, preserving focus, scroll position, and element state — instead of replacing content wholesale.

### Idiomorph (Recommended)

```html
<head>
    <script src="https://unpkg.com/idiomorph@0.7.3/dist/idiomorph-ext.min.js"></script>
</head>
<body hx-ext="morph">
    <div hx-get="/data" hx-swap="morph">Content</div>
    <div hx-get="/data" hx-swap="morph:innerHTML">Content</div>
    <div hx-get="/data" hx-swap="morph:outerHTML">Content</div>
</body>
```

### When to Use Morphing

| Use Case | Why Morphing Helps |
|----------|--------------------|
| Forms with focus preservation | Input focus and cursor position maintained |
| Lists with animations/reordering | Elements morph in place instead of flashing |
| Complex UIs with third-party widget state | Widget state (charts, players) preserved |
| Cases where full replacement causes flicker | Smooth updates without visible re-render |

---

## 10. Events & API Reference

### Event Naming Convention

Both camelCase and kebab-case work in JavaScript. In `hx-on:` attributes, use **kebab-case** (HTML attributes are case-insensitive):

```html
<!-- Correct -->
<div hx-on:htmx:after-swap="handler()">
<!-- Will NOT work -->
<div hx-on:htmx:afterSwap="handler()">
```

### Request Lifecycle Events

| Event | When Fired | Key `event.detail` Properties |
|-------|------------|-------------------------------|
| `htmx:confirm` | Every trigger (not just `hx-confirm`). `preventDefault()` to halt, `issueRequest()` to resume | `elt`, `path`, `verb`, `target`, `question`, `issueRequest(skipConfirmation)` |
| `htmx:configRequest` | Before request. Modify parameters/headers | `parameters`, `headers`, `elt`, `target`, `verb`, `path` |
| `htmx:beforeRequest` | Before AJAX request | `elt`, `target`, `requestConfig`, `xhr` |
| `htmx:beforeSend` | Just before network send | `elt`, `target`, `requestConfig`, `xhr` |
| `htmx:afterRequest` | After request (success or failure) | `elt`, `target`, `xhr`, `successful`, `failed` |
| `htmx:responseError` | On non-2xx/3xx response | `xhr`, `elt`, `target` |
| `htmx:sendError` | On network failure | `xhr`, `elt`, `target` |
| `htmx:sendAbort` | When request aborted | `elt`, `target` |
| `htmx:timeout` | When request times out | `elt`, `target`, `xhr` |

### Swap & Settle Events

| Event | When Fired | Key Properties |
|-------|------------|----------------|
| `htmx:beforeSwap` | Before swap. Modify `shouldSwap`, `swapOverride`, `selectOverride` | `xhr`, `target`, `shouldSwap`, `serverResponse`, `isError` |
| `htmx:afterSwap` | After content swapped in | `xhr`, `elt`, `target` |
| `htmx:beforeTransition` | Before View Transition API swap | `xhr`, `elt`, `target` |
| `htmx:afterSettle` | After DOM settled | `xhr`, `elt`, `target` |

### Element Lifecycle Events

| Event | When Fired |
|-------|------------|
| `htmx:load` | New content added to DOM |
| `htmx:beforeProcessNode` | Before HTMX initializes attributes on node |
| `htmx:afterProcessNode` | After HTMX initialized a node |
| `htmx:beforeCleanupElement` | Before HTMX removes/disables element |

### OOB Events

| Event | When Fired | Key Properties |
|-------|------------|----------------|
| `htmx:oobBeforeSwap` | Before OOB swap | `fragment`, `target` |
| `htmx:oobAfterSwap` | After OOB swap | `fragment`, `target` |
| `htmx:oobErrorNoTarget` | OOB element has no matching `id` in DOM | `content` |

### History Events

| Event | When Fired |
|-------|------------|
| `htmx:beforeHistorySave` | Before page snapshot cached |
| `htmx:pushedIntoHistory` | After URL pushed into history |
| `htmx:replacedInHistory` | After URL replaced in history |
| `htmx:historyRestore` | During back/forward restoration |
| `htmx:historyCacheHit` | History cache hit |
| `htmx:historyCacheMiss` | History cache miss |

### Validation Events

| Event | When Fired |
|-------|------------|
| `htmx:validation:validate` | Before `checkValidity()` — add custom validation here |
| `htmx:validation:failed` | Validation fails |
| `htmx:validation:halted` | Request blocked by validation |

### XHR Progress Events

| Event | Key Properties |
|-------|----------------|
| `htmx:xhr:loadstart` | — |
| `htmx:xhr:progress` | `loaded`, `total` |
| `htmx:xhr:loadend` | — |

### SSE Events

`htmx:sseOpen`, `htmx:sseError`, `htmx:sseBeforeMessage`, `htmx:sseMessage`, `htmx:sseClose`

### WebSocket Events

`htmx:wsConnecting`, `htmx:wsOpen`, `htmx:wsClose`, `htmx:wsError`, `htmx:wsBeforeMessage`, `htmx:wsAfterMessage`, `htmx:wsConfigSend`, `htmx:wsBeforeSend`, `htmx:wsAfterSend`

### JavaScript API

#### Element Selection

```javascript
htmx.find('#my-element');
htmx.find(parentElt, '.child');
htmx.findAll('.items');
htmx.closest(elt, 'form');
```

#### Programmatic AJAX Requests (Returns Promise)

```javascript
htmx.ajax('GET', '/api/data', '#target');
htmx.ajax('POST', '/api/data', {
    target: '#target',
    swap: 'innerHTML',
    values: { key: 'value' },
    headers: { 'X-Custom': 'header' },
    select: '#content',
    selectOOB: '#sidebar',
    push: true
}).then(() => { console.log('Request complete'); });
```

#### DOM Manipulation

```javascript
htmx.addClass(elt, 'active');
htmx.addClass(elt, 'active', 1000);  // add after 1s delay
htmx.removeClass(elt, 'active');
htmx.toggleClass(elt, 'active');
htmx.takeClass(elt, 'selected');     // only this element gets the class
htmx.remove(elt);
htmx.remove(elt, 2000);              // remove after 2s
```

#### Event Management

```javascript
htmx.on('#my-element', 'htmx:afterSwap', function(event) {
    console.log('Swapped!', event.detail);
});
htmx.off('#my-element', 'htmx:afterSwap', listener);
htmx.trigger(elt, 'myCustomEvent', { key: 'value' });
htmx.trigger(elt, 'htmx:abort');  // abort in-flight request
```

#### Content Initialization

```javascript
htmx.process(newContent);  // initialize HTMX on dynamically-added HTML
htmx.onLoad(function(content) {
    // initialize components in newly loaded content
    var tooltips = content.querySelectorAll('[data-tooltip]');
    tooltips.forEach(initTooltip);
});
```

#### Utilities

```javascript
htmx.values(formElement);            // get form values
htmx.parseInterval('500ms');         // 500
htmx.parseInterval('2s');            // 2000
```

---

## 11. Configuration

Configure via JavaScript or meta tag:

```javascript
htmx.config.defaultSwapStyle = 'outerHTML';
```

```html
<meta name="htmx-config" content='{ "defaultSwapStyle": "outerHTML" }' />
```

### Key Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `historyEnabled` | `true` | Enable history snapshot and navigation |
| `historyCacheSize` | `10` | Max pages in history cache. `0` to disable |
| `refreshOnHistoryMiss` | `false` | Full page reload on cache miss |
| `defaultSwapStyle` | `innerHTML` | Default `hx-swap` strategy |
| `defaultSwapDelay` | `0` | Default swap delay in ms |
| `defaultSettleDelay` | `20` | Default settle delay in ms |
| `includeIndicatorStyles` | `true` | Inject default indicator CSS |
| `indicatorClass` | `htmx-indicator` | Class for request indicators |
| `requestClass` | `htmx-request` | Class during requests |
| `addedClass` | `htmx-added` | Class for new content |
| `settlingClass` | `htmx-settling` | Class during settle |
| `swappingClass` | `htmx-swapping` | Class during swap |
| `allowEval` | `true` | Allow eval-dependent features (`hx-on`, `hx-vals` with `js:`) |
| `allowScriptTags` | `true` | Process `<script>` tags in responses |
| `inlineScriptNonce` | `''` | Nonce for inline scripts (CSP) |
| `inlineStyleNonce` | `''` | Nonce for inline styles (CSP) |
| `useTemplateFragments` | `false` | Use `<template>` for HTML parsing |
| `selfRequestsOnly` | `true` | Only allow same-origin requests |
| `withCredentials` | `false` | Send credentials cross-origin |
| `timeout` | `0` | Request timeout in ms (0 = no timeout) |
| `globalViewTransitions` | `false` | Enable View Transitions globally |
| `scrollBehavior` | `instant` | Scroll behavior (`instant`, `smooth`, `auto`) |
| `defaultFocusScroll` | `false` | Scroll focused element into view |
| `getCacheBusterParam` | `false` | Cache-buster param for GET requests |
| `disableInheritance` | `false` | Disable attribute inheritance globally |
| `allowNestedOobSwaps` | `true` | Process OOB swaps in nested content |

---

## 12. Extensions

### Core Extensions

| Extension | Purpose | Package |
|-----------|---------|---------|
| `head-support` | Merge `<head>` from responses | `htmx-ext-head-support` |
| `idiomorph` | DOM morphing swap | `idiomorph` |
| `preload` | Preload on hover/focus | `htmx-ext-preload` |
| `response-targets` | Target by HTTP status code | `htmx-ext-response-targets` |
| `sse` | Server-Sent Events | `htmx-ext-sse` |
| `ws` | WebSocket support | `htmx-ext-ws` |

### SSE Extension

```html
<div hx-ext="sse" sse-connect="/events">
    <div sse-swap="message">Waiting for messages...</div>
    <div hx-get="/update" hx-trigger="sse:notification">Notifications</div>
</div>
```

### WebSocket Extension

```html
<div hx-ext="ws" ws-connect="/ws">
    <form ws-send>
        <input name="message" />
        <button type="submit">Send</button>
    </form>
    <div id="messages"></div>
</div>
```

### response-targets Extension

Target different elements based on HTTP status:

```html
<div hx-ext="response-targets">
    <form hx-post="/submit"
          hx-target="#success"
          hx-target-422="#form-errors"
          hx-target-5*="#server-error">
        ...
    </form>
</div>
```

### Disabling Extensions

```html
<div hx-ext="ignore:preload">
    <!-- preload disabled in this subtree -->
</div>
```

### Defining Custom Extensions

```javascript
htmx.defineExtension('my-ext', {
    onEvent: function(name, event) { },
    transformResponse: function(text, xhr, elt) { return text; },
    isInlineSwap: function(swapStyle) { return false; },
    handleSwap: function(swapStyle, target, fragment, settleInfo) { return false; },
    encodeParameters: function(xhr, parameters, elt) { return null; }
});
```

---

## 13. Modal CRUD Pattern

Complete pattern for modal-based Create, Read, Update, Delete operations.

### Naming Conventions

Use **entity-prefixed IDs** for all modal and table elements to avoid conflicts:

| Element | Naming Pattern | Example |
|---------|----------------|---------|
| Modal shell | `#{entity}-modal` | `#item-modal` |
| Modal body | `#{entity}-modal-body` | `#item-modal-body` |
| Table container | `#{entity}-table-container` | `#item-table-container` |
| Table body | `#{entity}-table-body` | `#item-table-body` |
| Table row | `#{entity}-row-{id}` | `#item-row-42` |

### Page Structure

```html
{@java.util.List<com.example.entity.Item> items}

{#include base}

<h2 class="uk-heading-small">Item Management</h2>

<!-- Add Button -->
<button class="uk-button uk-button-primary uk-button-small uk-margin-bottom"
        type="button"
        hx-get="/items/create"
        hx-target="#item-modal-body"
        hx-on::after-request="UIkit.modal('#item-modal').show()"
        uk-tooltip="Add Item">
    <span uk-icon="plus"></span>
</button>

<div id="item-table-container">{#include $table /}</div>

<!-- Static Modal Shell (always present in DOM) -->
<div id="item-modal" uk-modal="bg-close: false">
    <div class="uk-modal-dialog uk-modal-container">
        <div id="item-modal-body" class="uk-modal-body">
            <!-- Content loaded dynamically via HTMX -->
        </div>
    </div>
</div>

{/include}
```

**Key points:**
- `uk-modal="bg-close: false"` prevents closing when clicking backdrop
- Modal body is empty initially; content loaded via HTMX
- Use `uk-tooltip` for action button hints

### Table Fragment with Action Buttons

```html
{#fragment id='table' rendered=false}
{@java.util.List<com.example.entity.Item> items}
{#if items.isEmpty()}
<p class="uk-text-muted">No items found.</p>
{#else}
<div class="uk-overflow-auto">
    <table id="item-table" class="uk-table uk-table-hover uk-table-divider">
        <thead>
            <tr>
                <th class="uk-table-shrink">Code</th>
                <th class="uk-table-expand">Name</th>
                <th class="uk-width-small">Actions</th>
            </tr>
        </thead>
        <tbody id="item-table-body">
            {#for item in items}
            <tr id="item-row-{item.id}">
                <td>{item.code}</td>
                <td>{item.name}</td>
                <td>
                    <div class="uk-button-group">
                        <button class="uk-button uk-button-small uk-button-primary"
                                hx-get="/items/{item.id}/edit"
                                hx-target="#item-modal-body"
                                hx-on::after-request="UIkit.modal('#item-modal').show()"
                                uk-tooltip="Edit">
                            <span uk-icon="pencil"></span>
                        </button>
                        <button class="uk-button uk-button-small uk-button-danger"
                                hx-get="/items/{item.id}/delete"
                                hx-target="#item-modal-body"
                                hx-on::after-request="UIkit.modal('#item-modal').show()"
                                uk-tooltip="Delete">
                            <span uk-icon="trash"></span>
                        </button>
                    </div>
                </td>
            </tr>
            {/for}
        </tbody>
    </table>
</div>
{/if}
{/fragment}
```

### Create Form Modal Fragment

```html
{#fragment id='modal_create' rendered=false}
{@com.example.entity.Item item}
{@String error}
<h2 class="uk-modal-title">Add Item</h2>
{#if error??}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}
<form hx-post="/items" hx-target="#item-modal-body" class="uk-form-stacked">
    <div class="uk-margin">
        <label class="uk-form-label" for="create-item-code">Code *</label>
        <input class="uk-input" type="text" id="create-item-code" name="code"
               maxlength="10" value="{item.code ?: ''}" required />
    </div>
    <div class="uk-margin">
        <label class="uk-form-label" for="create-item-name">Name *</label>
        <input class="uk-input" type="text" id="create-item-name" name="name"
               value="{item.name ?: ''}" required />
    </div>
    <div class="uk-margin uk-text-right">
        <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
        <button class="uk-button uk-button-primary" type="submit">Save</button>
    </div>
</form>
{/fragment}
```

**Note:** Use `{#if error??}` (null-safe operator) instead of `{#if error}` for proper null checking in Qute.

### Success Response - Full Table Refresh

Use when you need to refresh the entire table (e.g., after create when sort order may change):

```html
{#fragment id='modal_success' rendered=false}
{@String message}
{@java.util.List<com.example.entity.Item> items}
<div hx-on::load="UIkit.modal('#item-modal').hide()"></div>
<div id="item-table-container" hx-swap-oob="innerHTML">
    {#include $table items=items /}
</div>
{/fragment}
```

**Key pattern:** Empty `<div>` with `hx-on::load` to close modal, followed by OOB update.

### Success Response - Single Row Update

Use for efficient single-row updates (e.g., after edit):

```html
{#fragment id='modal_success_row' rendered=false}
{@String message}
{@com.example.entity.Item item}
<div hx-on::load="UIkit.modal('#item-modal').hide()"></div>
<template>
<tr id="item-row-{item.id}" hx-swap-oob="outerHTML">
    <td>{item.code}</td>
    <td>{item.name}</td>
    <td>
        <div class="uk-button-group">
            <button class="uk-button uk-button-small uk-button-primary"
                    hx-get="/items/{item.id}/edit"
                    hx-target="#item-modal-body"
                    hx-on::after-request="UIkit.modal('#item-modal').show()"
                    uk-tooltip="Edit">
                <span uk-icon="pencil"></span>
            </button>
            <button class="uk-button uk-button-small uk-button-danger"
                    hx-get="/items/{item.id}/delete"
                    hx-target="#item-modal-body"
                    hx-on::after-request="UIkit.modal('#item-modal').show()"
                    uk-tooltip="Delete">
                <span uk-icon="trash"></span>
            </button>
        </div>
    </td>
</tr>
</template>
{/fragment}
```

**Key pattern:** Use `<template>` wrapper for table rows to maintain valid HTML structure.

### Delete Confirmation Modal Fragment

```html
{#fragment id='modal_delete' rendered=false}
{@com.example.entity.Item item}
{@String error}
<h2 class="uk-modal-title">Delete Item</h2>
{#if error??}
<div class="uk-alert uk-alert-danger">{error}</div>
{#else}
<div class="uk-alert uk-alert-warning">
    Are you sure you want to delete <strong>{item.code}</strong> - <strong>{item.name}</strong>?
</div>
<p class="uk-text-muted">This action cannot be undone.</p>
{/if}
<div class="uk-margin uk-text-right">
    <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
    {#if !error??}
    <button class="uk-button uk-button-danger"
            hx-delete="/items/{item.id}"
            hx-target="#item-modal-body">
        Delete
    </button>
    {/if}
</div>
{/fragment}
```

**Key pattern:** Conditionally hide delete button when error is present (`{#if !error??}`).

### Delete Success with OOB Row Removal

```html
{#fragment id='modal_delete_success' rendered=false}
{@Long deletedId}
<div hx-on::load="UIkit.modal('#item-modal').hide()"></div>
<template><tr id="item-row-{deletedId}" hx-swap-oob="delete"></tr></template>
{/fragment}
```

---

## 14. Active Search Pattern

Real-time search with debouncing and visual feedback.

```html
<h3>
    Search Items
    <span class="htmx-indicator">
        <img src="/img/bars.svg" alt=""/> Searching...
    </span>
</h3>

<input class="uk-input" type="search" name="search"
       placeholder="Begin typing to search..."
       hx-post="/items/search"
       hx-trigger="input changed delay:500ms, keyup[key=='Enter'], load"
       hx-target="#search-results"
       hx-indicator=".htmx-indicator">

<table class="uk-table">
    <thead>
        <tr>
            <th>Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody id="search-results">
        <!-- Results populated here -->
    </tbody>
</table>
```

**Server returns table rows:**

```html
{#for item in items}
<tr>
    <td>{item.name}</td>
    <td>{item.description}</td>
</tr>
{#else}
<tr><td colspan="2">No results found</td></tr>
{/for}
```

**Key:** `input changed delay:500ms` debounces, `keyup[key=='Enter']` for immediate search, `load` for initial results.

---

## 15. Click-to-Edit Pattern

Inline editing without modals.

### Display State

```html
<div hx-target="this" hx-swap="outerHTML">
    <div><label>Name</label>: {contact.name}</div>
    <div><label>Email</label>: {contact.email}</div>
    <button hx-get="/contacts/{contact.id}/edit" class="uk-button uk-button-primary">
        Click To Edit
    </button>
</div>
```

### Edit State

```html
<form hx-put="/contacts/{contact.id}" hx-target="this" hx-swap="outerHTML">
    <div class="uk-margin">
        <label>Name</label>
        <input class="uk-input" type="text" name="name" value="{contact.name}" autofocus>
    </div>
    <div class="uk-margin">
        <label>Email</label>
        <input class="uk-input" type="email" name="email" value="{contact.email}">
    </div>
    <button class="uk-button uk-button-primary" type="submit">Save</button>
    <button class="uk-button uk-button-default" hx-get="/contacts/{contact.id}">Cancel</button>
</form>
```

---

## 16. Edit Row Pattern

Inline table editing with mutual exclusion — only one row editable at a time.

### View Mode Row

```html
<tr hx-trigger="edit" hx-get="/contact/{contact.id}/edit">
    <td>{contact.name}</td>
    <td>{contact.email}</td>
    <td>
        <button class="uk-button uk-button-small uk-button-primary"
                hx-on:click="
                    let editing = document.querySelector('.editing');
                    if (editing) {
                        Swal.fire({title:'Already Editing',
                            showCancelButton: true, confirmButtonText: 'Yep'})
                            .then((result) => {
                                if (result.isConfirmed) {
                                    htmx.trigger(editing, 'cancel');
                                    htmx.trigger(this.closest('tr'), 'edit');
                                }
                            });
                    } else {
                        htmx.trigger(this.closest('tr'), 'edit');
                    }">
            Edit
        </button>
    </td>
</tr>
```

### Edit Mode Row

```html
<tr hx-trigger="cancel" class="editing" hx-get="/contact/{contact.id}">
    <td><input autofocus name="name" value="{contact.name}" class="uk-input" /></td>
    <td><input name="email" value="{contact.email}" class="uk-input" /></td>
    <td>
        <button class="uk-button uk-button-small uk-button-default"
                hx-get="/contact/{contact.id}">Cancel</button>
        <button class="uk-button uk-button-small uk-button-primary"
                hx-put="/contact/{contact.id}"
                hx-include="closest tr">Save</button>
    </td>
</tr>
```

**Key:** `hx-include="closest tr"` gathers inputs from the row without needing a `<form>`.

---

## 17. Delete Row Pattern

Delete with confirmation and animated removal.

### Table Setup

```html
<table class="uk-table">
    <thead>
        <tr>
            <th>Name</th>
            <th>Email</th>
            <th></th>
        </tr>
    </thead>
    <tbody hx-confirm="Are you sure?" hx-target="closest tr" hx-swap="outerHTML swap:1s">
        <tr id="contact-1">
            <td>John Doe</td>
            <td>john@example.com</td>
            <td>
                <button class="uk-button uk-button-danger uk-button-small"
                        hx-delete="/contacts/1">
                    Delete
                </button>
            </td>
        </tr>
    </tbody>
</table>
```

### CSS for Fade-Out Animation

```css
tr.htmx-swapping td {
    opacity: 0;
    transition: opacity 1s ease-out;
}
```

Server returns empty 200 response. Inherited `hx-confirm`, `hx-target`, `hx-swap` on `<tbody>` apply to all delete buttons.

---

## 18. Bulk Update Pattern

Update multiple records at once with a toast notification.

```html
<form id="checked-contacts" hx-post="/users"
      hx-swap="innerHTML settle:3s" hx-target="#toast">
    <table class="uk-table uk-table-hover">
        <thead>
            <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Active</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>Joe Smith</td>
                <td>joe@smith.org</td>
                <td><input type="checkbox" name="active:joe@smith.org" checked /></td>
            </tr>
            <!-- more rows -->
        </tbody>
    </table>
    <button class="uk-button uk-button-primary" type="submit">Bulk Update</button>
    <output id="toast"></output>
</form>
```

### Toast CSS

```css
#toast.htmx-settling {
    opacity: 100;
}
#toast {
    opacity: 0;
    transition: opacity 3s ease-out;
}
```

**Key:** `settle:3s` keeps the toast visible for 3 seconds before fading out.

---

## 19. Infinite Scroll / Click-to-Load

### Click to Load Pattern

```html
<tbody>
    {#for contact in contacts}
    <tr>
        <td>{contact.name}</td>
        <td>{contact.email}</td>
    </tr>
    {/for}
    {#if hasMore}
    <tr id="load-more-row">
        <td colspan="2">
            <button class="uk-button uk-button-primary"
                    hx-get="/contacts?page={nextPage}"
                    hx-target="#load-more-row"
                    hx-swap="outerHTML">
                Load More
                <img class="htmx-indicator" src="/img/bars.svg" alt="">
            </button>
        </td>
    </tr>
    {/if}
</tbody>
```

Button replaces itself with new rows + a new button for the next page.

### Infinite Scroll Pattern

```html
<tbody id="contacts-body">
    {#for contact in contacts}
    <tr {#if contact_isLast && hasMore}
        hx-get="/contacts?page={nextPage}"
        hx-trigger="revealed"
        hx-swap="afterend"
        hx-indicator="#loading-spinner"
        {/if}>
        <td>{contact.name}</td>
        <td>{contact.email}</td>
    </tr>
    {/for}
</tbody>
<div id="loading-spinner" class="htmx-indicator">Loading...</div>
```

Use `intersect once threshold:0.5` for custom scrollable containers.

---

## 20. Lazy Loading

Load content on demand — on page load or when scrolled into view.

```html
<!-- Load immediately when page loads -->
<div hx-get="/dashboard/stats" hx-trigger="load">
    <span class="htmx-indicator">Loading stats...</span>
</div>

<!-- Load when scrolled into view -->
<div hx-get="/dashboard/chart" hx-trigger="revealed">
    <span class="htmx-indicator">Loading chart...</span>
</div>

<!-- Load once when intersecting viewport -->
<div hx-get="/dashboard/activity" hx-trigger="intersect once">
    <span class="htmx-indicator">Loading activity feed...</span>
</div>
```

---

## 21. Inline Validation

Validate fields as user types.

```html
<form hx-post="/register">
    <div hx-target="this" hx-swap="outerHTML">
        <label>Email Address</label>
        <input name="email" type="email"
               hx-post="/validate/email"
               hx-trigger="blur, keyup changed delay:500ms"
               hx-indicator="#email-indicator">
        <img id="email-indicator" class="htmx-indicator" src="/img/bars.svg" alt="">
    </div>
    <button class="uk-button uk-button-primary" type="submit">Register</button>
</form>
```

### Validation Response (Error)

```html
<div hx-target="this" hx-swap="outerHTML" class="uk-form-danger">
    <label>Email Address</label>
    <input name="email" type="email" value="{email}" class="uk-form-danger"
           hx-post="/validate/email"
           hx-trigger="blur, keyup changed delay:500ms">
    <span class="uk-text-danger">Email already registered</span>
</div>
```

### Validation Response (Success)

```html
<div hx-target="this" hx-swap="outerHTML" class="uk-form-success">
    <label>Email Address</label>
    <input name="email" type="email" value="{email}" class="uk-form-success"
           hx-post="/validate/email"
           hx-trigger="blur, keyup changed delay:500ms">
    <span class="uk-text-success">Email available</span>
</div>
```

**Key:** `hx-target="this"` on the wrapping `<div>` so the entire field block is replaced with the validation result, preserving the HTMX attributes.

---

## 22. Tabs Pattern

### HATEOAS-Style Tabs (with URL History)

```html
<div id="tabs" hx-target="#tab-content">
    <div class="uk-tab" role="tablist">
        <a hx-get="/tab/details" hx-push-url="true"
           hx-on:htmx:after-on-load="htmx.takeClass(this, 'uk-active')"
           class="uk-active" role="tab" aria-selected="true">Details</a>
        <a hx-get="/tab/settings" hx-push-url="true"
           hx-on:htmx:after-on-load="htmx.takeClass(this, 'uk-active')"
           role="tab" aria-selected="false">Settings</a>
        <a hx-get="/tab/activity" hx-push-url="true"
           hx-on:htmx:after-on-load="htmx.takeClass(this, 'uk-active')"
           role="tab" aria-selected="false">Activity</a>
    </div>
    <div id="tab-content" role="tabpanel" class="uk-margin">
        <!-- Tab content loaded here -->
    </div>
</div>
```

**Key:** `htmx.takeClass(this, 'uk-active')` ensures only the clicked tab gets the active class.

### Tab Response with Active State OOB Update

Alternative approach — server controls active state:

```html
<!-- Tab content -->
<p>This is the content for Tab 2...</p>

<!-- OOB update for tab buttons -->
<div class="uk-tab" role="tablist" hx-swap-oob="outerHTML:.uk-tab">
    <button hx-get="/tab1" role="tab" aria-selected="false">Tab 1</button>
    <button hx-get="/tab2" class="uk-active" role="tab" aria-selected="true">Tab 2</button>
    <button hx-get="/tab3" role="tab" aria-selected="false">Tab 3</button>
</div>
```

---

## 23. Cascading Selects

Dependent dropdowns that update based on parent selection.

```html
<div class="uk-margin">
    <label>Make</label>
    <select name="make"
            hx-get="/models"
            hx-target="#model-select"
            hx-trigger="change"
            hx-indicator="#model-indicator">
        <option value="">Select Make...</option>
        <option value="audi">Audi</option>
        <option value="bmw">BMW</option>
        <option value="toyota">Toyota</option>
    </select>
</div>

<div class="uk-margin">
    <label>Model <img id="model-indicator" class="htmx-indicator" src="/img/bars.svg"></label>
    <select id="model-select" name="model">
        <option value="">Select Model...</option>
    </select>
</div>
```

### Server Response for Models

```html
<select id="model-select" name="model">
    <option value="">Select Model...</option>
    <option value="325i">325i</option>
    <option value="x5">X5</option>
    <option value="m3">M3</option>
</select>
```

Default trigger for `<select>` is `change`. Selected value is auto-included as a query parameter.

---

## 24. Progress Bar Pattern

Long-running server job with polling progress updates.

### Initial Trigger

```html
<div hx-trigger="done" hx-get="/job" hx-swap="outerHTML" hx-target="this">
    <h3 role="status">Running</h3>
    <div hx-get="/job/progress"
         hx-trigger="every 600ms"
         hx-target="this"
         hx-swap="innerHTML">
        <div class="progress" role="progressbar"
             aria-valuemin="0" aria-valuemax="100" aria-valuenow="0">
            <div id="pb" class="progress-bar" style="width:0%"></div>
        </div>
    </div>
</div>
```

### Completion

Server responds with `HX-Trigger: done` header when finished. The outer `<div>` listening for `done` fetches the final state. The complete state uses `hx-trigger="none"` to stop polling.

---

## 25. File Upload with Progress

```html
<form hx-post="/upload"
      hx-encoding="multipart/form-data"
      hx-target="#upload-result"
      hx-indicator="#upload-progress">
    <input type="file" name="file" />
    <button class="uk-button uk-button-primary" type="submit">Upload</button>
    <div id="upload-progress" class="htmx-indicator">
        <progress id="progress-bar" value="0" max="100"></progress>
    </div>
</form>
<div id="upload-result"></div>

<script>
    htmx.on('htmx:xhr:progress', function(event) {
        var percent = (event.detail.loaded / event.detail.total) * 100;
        document.getElementById('progress-bar').value = percent;
    });
</script>
```

**Key:** `hx-encoding="multipart/form-data"` is required for file uploads.

---

## 26. Polling Patterns

### Interval Polling

```html
<div hx-get="/status" hx-trigger="every 2s">Status: checking...</div>
```

Stop with HTTP status `286`.

### Load Polling

Server controls the polling loop by returning content that re-triggers itself:

```html
<!-- Server returns this to continue polling -->
<div hx-get="/status" hx-trigger="load delay:1s" hx-swap="outerHTML">
    Status: processing...
</div>

<!-- Server returns this to stop polling -->
<div>Status: complete!</div>
```

### Conditional Polling (Tab Visibility)

```html
<div hx-get="/status"
     hx-trigger="every 2s [document.visibilityState === 'visible']">
    Status
</div>
```

---

## 27. Keyboard Shortcuts

```html
<!-- Global keyboard shortcut -->
<div hx-get="/search-modal"
     hx-trigger="keyup[ctrlKey&&key=='k'] from:body"
     hx-target="#modal-container">
</div>

<!-- Enter key on input -->
<input hx-get="/search"
       hx-trigger="keyup[key=='Enter']"
       hx-target="#results"
       name="q" />
```

---

## 28. Drag & Drop / Sortable

Integration with Sortable.js for reorderable lists.

```html
<form id="sortable-list" hx-post="/reorder" hx-trigger="end">
    <div class="item" data-id="1">
        <input type="hidden" name="order[]" value="1" />Item 1
    </div>
    <div class="item" data-id="2">
        <input type="hidden" name="order[]" value="2" />Item 2
    </div>
</form>

<script>
    htmx.onLoad(function(content) {
        var el = content.querySelector('#sortable-list');
        if (el) {
            new Sortable(el, {
                animation: 150,
                onEnd: function() {
                    el.querySelectorAll('.item').forEach(function(item, i) {
                        item.querySelector('input').value = i + 1;
                    });
                    htmx.trigger(el, 'end');
                }
            });
        }
    });
</script>
```

**Key:** Custom `end` event triggers the `hx-post`. Hidden inputs track the order.

---

## 29. Updating Other Content

Four strategies for updating elements outside the direct `hx-target`:

### 1. OOB Swaps (Server-Driven)

Server includes extra elements with `hx-swap-oob` in the response:

```html
<!-- Main response -->
<div id="main-content">Updated content</div>

<!-- OOB updates -->
<span id="item-count" hx-swap-oob="innerHTML">42</span>
<div id="notification" hx-swap-oob="true">Item saved!</div>
```

### 2. Server-Triggered Events

Server sends `HX-Trigger` response header; client elements listen:

```java
// Server (Quarkus)
return Response.ok(html).header("HX-Trigger", "itemsUpdated").build();
```

```html
<!-- Client: element refreshes when event fires -->
<div hx-get="/items/count"
     hx-trigger="itemsUpdated from:body">
    Count: 41
</div>
```

### 3. `hx-select-oob` (Client-Driven)

Requesting element picks specific parts of the response for OOB swap:

```html
<button hx-get="/data"
        hx-target="#results"
        hx-select="#results"
        hx-select-oob="#count:innerHTML, #status:outerHTML">
    Load
</button>
```

### 4. Path Dependencies with Custom Events

Client-side event coordination:

```html
<button hx-post="/items"
        hx-on::after-request="htmx.trigger(document.body, 'itemsChanged')">
    Add Item
</button>

<div hx-get="/items/summary"
     hx-trigger="itemsChanged from:body">
    Summary: ...
</div>
```

---

## 30. Request Indicators

### Default CSS for Indicators

```css
.htmx-indicator {
    opacity: 0;
    visibility: hidden;
    transition: opacity 200ms ease-in;
}

.htmx-request .htmx-indicator,
.htmx-request.htmx-indicator {
    opacity: 1;
    visibility: visible;
}
```

### Inline Indicator (Child of Trigger)

```html
<button hx-post="/submit">
    Submit
    <img class="htmx-indicator" src="/img/bars.svg" alt="Loading...">
</button>
```

### External Indicator

```html
<button hx-post="/submit" hx-indicator="#global-spinner">
    Submit
</button>
<img id="global-spinner" class="htmx-indicator" src="/img/bars.svg">
```

---

## 31. Attribute Inheritance

HTMX attributes are inherited by child elements. Use this to reduce duplication.

### Parent Attributes

```html
<div hx-target="#output" hx-swap="innerHTML">
    <button hx-post="/items/1/like">Like</button>
    <button hx-delete="/items/1">Delete</button>
</div>
<output id="output"></output>
```

### Confirmation Inheritance

```html
<div hx-confirm="Are you sure?">
    <button hx-delete="/account">Delete Account</button>
    <button hx-put="/account">Update Account</button>
    <!-- Cancel button skips confirmation -->
    <button hx-confirm="unset" hx-get="/">Cancel</button>
</div>
```

### Disable Inheritance

```html
<!-- Disable all inheritance -->
<div hx-target="#content" hx-disinherit="*">
    <button hx-get="/page" hx-target="this">Load Here</button>
</div>

<!-- Disable specific attribute inheritance -->
<div hx-target="#content" hx-disinherit="hx-swap">
    <button hx-get="/page">Inherits target, not swap</button>
</div>
```

### Selective Inheritance

```html
<div hx-target="#tab-container" hx-inherit="hx-target">
    <a hx-boost="true" href="/tab1">Tab 1</a>
    <a hx-boost="true" href="/tab2">Tab 2</a>
</div>
```

---

## 32. JavaScript Integration

### When JavaScript is Appropriate

| Use Case | Approach |
|----------|----------|
| UI framework integration (modals, tooltips) | Inline `hx-on::` attributes |
| Simple DOM manipulation | Inline `hx-on::` attributes |
| Complex reusable logic | External file in `js/` |
| Third-party library initialization | `htmx.onLoad()` callback |
| Cleanup before element removal | `htmx:beforeCleanupElement` listener |

### Inline Event Handlers with hx-on

```html
<!-- Open modal after request -->
<button hx-get="/items/create"
        hx-target="#modal-content"
        hx-on::after-request="UIkit.modal('#crud-modal').show()">
    Add Item
</button>

<!-- Close modal on load -->
<div hx-on::load="UIkit.modal('#crud-modal').hide()">
    Success message...
</div>

<!-- Multiple handlers -->
<button hx-get="/data"
        hx-on::before-request="console.log('Starting...')"
        hx-on::after-request="console.log('Done!')">
    Load Data
</button>

<!-- Modify request parameters -->
<div hx-get="/data"
     hx-on:htmx:config-request="event.detail.parameters.timestamp = Date.now()">
</div>
```

### Custom Confirmation Dialog via `htmx:confirm` Event

```javascript
document.addEventListener('htmx:confirm', function(event) {
    event.preventDefault();
    Swal.fire({
        title: 'Confirm',
        text: event.detail.question,
        showCancelButton: true
    }).then(function(result) {
        if (result.isConfirmed) {
            event.detail.issueRequest();
        }
    });
});
```

### External JavaScript for Complex Logic

```javascript
// js/arc-utils.js
window.Arc = {
    initOnLoad: function() {
        htmx.onLoad(function(content) {
            // Initialize components in newly loaded content
            UIkit.update(content);
        });
    },

    confirmDelete: function(name) {
        return confirm('Delete ' + name + '?');
    }
};

document.addEventListener('DOMContentLoaded', Arc.initOnLoad);
```

### Third-Party Library Lifecycle

```javascript
// Initialize on new content
htmx.onLoad(function(content) {
    var charts = content.querySelectorAll('.chart');
    charts.forEach(function(el) {
        new Chart(el, JSON.parse(el.dataset.config));
    });
});

// Clean up before removal
document.addEventListener('htmx:beforeCleanupElement', function(event) {
    var chart = Chart.getChart(event.target);
    if (chart) chart.destroy();
});
```

### Processing Dynamically Inserted HTMX Content

```javascript
// When external code adds HTMX-enabled HTML
const newContent = document.getElementById('new-content');
newContent.innerHTML = '<button hx-get="/data">Load</button>';
htmx.process(newContent);
```

---

## 33. Debugging

```javascript
// Log all HTMX events to console
htmx.logAll();

// Disable logging
htmx.logNone();

// Custom logger — filter specific events
htmx.logger = function(elt, event, data) {
    if (event === 'htmx:responseError') {
        console.error('HTMX Error:', data);
    }
};
```

### Demo/Mock Server

Use `https://demo.htmx.org` for prototyping:

```html
<button hx-get="https://demo.htmx.org/hello" hx-swap="outerHTML">
    Test
</button>
```

---

## 34. Security Considerations

### CSRF Protection

```html
<!-- Add CSRF token to all requests via inherited header -->
<body hx-headers='{"X-CSRF-TOKEN": "{csrfToken}"}'>
    ...
</body>
```

Or programmatically:

```javascript
document.addEventListener('htmx:configRequest', function(event) {
    event.detail.headers['X-CSRF-TOKEN'] = getCsrfToken();
});
```

### Content Security Policy

```html
<!-- Restrict HTMX requests to same origin -->
<meta name="htmx-config" content='{"selfRequestsOnly": true}'>

<!-- For CSP with nonces -->
<meta name="htmx-config" content='{"inlineScriptNonce": "random-nonce", "inlineStyleNonce": "random-nonce"}'>
```

### Disable HTMX on Untrusted Content

```html
<!-- Prevent HTMX processing on user-generated content -->
<div hx-disable>
    {unsafeUserContent}
</div>
```

**Note:** `hx-disable` cannot be overridden by child elements, unlike `hx-disinherit`.

### Prevent History Caching of Sensitive Data

```html
<div hx-history="false">
    <!-- Sensitive content not cached in localStorage -->
</div>
```

### Security Hardening Config

```html
<meta name="htmx-config" content='{
    "selfRequestsOnly": true,
    "allowEval": false,
    "allowScriptTags": false,
    "historyCacheSize": 0
}'>
```

### URL Validation

```javascript
document.addEventListener('htmx:validateUrl', function(event) {
    if (!event.detail.sameHost) {
        event.preventDefault();  // block cross-origin requests
    }
});
```

### XSS Prevention

- **Always escape user content** in templates
- Use Qute's automatic escaping (`{value}` escapes by default)
- Only use `{value.raw}` for trusted HTML content

---

## 35. Anti-Patterns

### Do NOT Fetch JSON and Render Client-Side

```html
<!-- WRONG: Defeats HTMX's purpose -->
<script>
fetch('/api/items')
    .then(r => r.json())
    .then(data => { renderItems(data); });
</script>

<!-- CORRECT: Return HTML from server -->
<div hx-get="/items" hx-target="this">Loading...</div>
```

### Do NOT Use Heavy Client-Side State

```html
<!-- WRONG: Complex state management -->
<script>
const state = { items: [], filter: '', page: 1 };
</script>

<!-- CORRECT: Server is source of truth -->
<form hx-get="/items" hx-target="#results">
    <input name="filter" value="{currentFilter}">
    <input type="hidden" name="page" value="{currentPage}">
</form>
```

### Do NOT Put More Than 2-3 Statements in Inline Scripts

```html
<!-- WRONG: Obscures template structure -->
<button hx-on:click="
    const x = this.dataset.value;
    const y = document.getElementById('target');
    if (x > 10) { y.classList.add('active'); y.innerHTML = 'High'; }
    else { y.classList.remove('active'); y.innerHTML = 'Low'; }
">Click</button>

<!-- CORRECT: Extract to external file -->
<button hx-on:click="Arc.handleValueChange(this)">Click</button>
```

### Do NOT Use Polling When SSE/WebSockets Are Better

```html
<!-- ACCEPTABLE for infrequent updates -->
<div hx-get="/status" hx-trigger="every 30s">Status: {status}</div>

<!-- BETTER for real-time: Use SSE extension -->
<div hx-ext="sse" sse-connect="/events" sse-swap="message">
    Real-time updates...
</div>
```

---

## 36. Qute Template Integration

### Fragment Pattern with rendered=false

Page-level fragments must use `rendered=false` to prevent duplicate rendering:

```html
{@java.util.List<com.example.entity.Item> items}

{#include base.html}
{#content}
<div class="uk-container">
    <h1>Items</h1>
    <div id="table-container">{#include $table /}</div>
</div>

<div id="crud-modal" uk-modal>
    <div class="uk-modal-dialog">
        <div id="modal-content"></div>
    </div>
</div>
{/content}
{/include}

{#fragment id=table rendered=false}
<table class="uk-table">
    <tbody>
        {#for item in items}
        <tr id="item-row-{item.id}">
            <td>{item.name}</td>
        </tr>
        {/for}
    </tbody>
</table>
{/fragment}

{#fragment id=modal_create rendered=false}
{@com.example.entity.Item item}
{@java.lang.String error}
<div class="uk-modal-header">
    <h2>Create Item</h2>
</div>
<div class="uk-modal-body">
    {#if error}
    <div class="uk-alert uk-alert-danger">{error}</div>
    {/if}
    <form hx-post="/items" hx-target="#modal-content">
        <input name="name" value="{item.name ?: ''}">
        <button type="submit">Save</button>
    </form>
</div>
{/fragment}

{#fragment id=modal_success rendered=false}
{@java.lang.String message}
{@java.util.List<com.example.entity.Item> items}
<div class="uk-modal-body" hx-on::load="UIkit.modal('#crud-modal').hide()">
    <div class="uk-alert uk-alert-success">{message}</div>
</div>
<div id="table-container" hx-swap-oob="innerHTML">
    {#include $table items=items /}
</div>
{/fragment}
```

### Fragment Naming Conventions

| Fragment ID | Java Method | Purpose |
|-------------|-------------|---------|
| `{#fragment id=table}` | `Templates.item$table(...)` | Data table |
| `{#fragment id=modal_create}` | `Templates.item$modal_create(...)` | Create form |
| `{#fragment id=modal_edit}` | `Templates.item$modal_edit(...)` | Edit form |
| `{#fragment id=modal_delete}` | `Templates.item$modal_delete(...)` | Delete confirmation |
| `{#fragment id=modal_success}` | `Templates.item$modal_success(...)` | Success with OOB update |
| `{#fragment id=modal_delete_success}` | `Templates.item$modal_delete_success(...)` | Delete success with OOB removal |

### Including Fragments

```html
<!-- Same template fragment -->
{#include $table /}

<!-- Fragment with parameters -->
{#include $table items=items /}

<!-- Cross-template fragment -->
{#include item$item_row item=currentItem /}
```

### Resource CheckedTemplate Pattern

```java
@Path("/items")
public class ItemResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance item(List<Item> items);
        public static native TemplateInstance item$table(List<Item> items);
        public static native TemplateInstance item$modal_create(Item item, String error);
        public static native TemplateInstance item$modal_edit(Item item, String error);
        public static native TemplateInstance item$modal_success(String message, List<Item> items);
        public static native TemplateInstance item$modal_delete(Item item, String error);
        public static native TemplateInstance item$modal_delete_success(Long deletedId);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
        List<Item> items = itemRepository.listAllOrdered();

        // Return fragment for HTMX requests, full page otherwise
        if ("true".equals(hxRequest)) {
            return Templates.item$table(items);
        }
        return Templates.item(items);
    }
}
```

---

## Quick Reference Card

### Request Flow

```
User Action → hx-trigger → HTTP Request → Server Response (HTML) → hx-swap into hx-target
                                                    ↓
                                         OOB elements swap by ID
```

### Common Patterns Checklist

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

---

*Last Updated: 2026-02-17*
*HTMX Version: 2.0.8*
