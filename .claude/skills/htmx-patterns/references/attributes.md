# HTMX Attributes, Swaps, Triggers, Lifecycle & Events

Framework-agnostic HTMX 2.0.8 reference. Load when authoring `hx-*` attributes or debugging the request/swap pipeline.

---

## 1. Core Attributes

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

## 2. Swap Strategies

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

## 3. Trigger Patterns

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

### Active Search Trigger

```html
<input type="search" name="search"
       hx-post="/search"
       hx-trigger="input changed delay:500ms, keyup[key=='Enter'], load"
       hx-target="#search-results"
       hx-indicator=".htmx-indicator">
```

---

## 4. Out-of-Band (OOB) Updates

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

## 5. Request Lifecycle

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

## 6. CSS Transitions

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

## 7. View Transitions API

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

## 8. Morphing

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

## 9. Events & JavaScript API

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

## 10. Configuration

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

## 11. Extensions

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

## 12. Request Indicators

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

## 13. Attribute Inheritance

HTMX attributes are inherited by child elements. Use this to reduce duplication.

**Not inherited:** `hx-trigger`, `hx-on*`, `hx-swap-oob`, `hx-preserve`, `hx-history-elt`, `hx-validate`.

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
