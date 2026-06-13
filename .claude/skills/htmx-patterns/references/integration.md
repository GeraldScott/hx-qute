# HTMX Integration — JS, Debugging, Security, Anti-Patterns, Qute

Load when wiring HTMX into JavaScript libraries, applying CSRF/CSP hardening, or structuring Quarkus resources and Qute templates.

---

## 1. JavaScript Integration

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

## 2. Debugging

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

## 3. Security

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

## 4. Anti-Patterns

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

## 5. Qute Template Integration

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
