# HTMX UI Patterns

Concrete UI flows for this project's stack — **HTMX 2.0.8 + UIkit 3.25 + Qute** with the `#{entity}-modal` / `#{entity}-row-{id}` naming convention. Load when implementing or modifying a UI flow.

For attribute mechanics referenced below (`hx-target`, `hx-swap`, `hx-swap-oob`, triggers, lifecycle), see `attributes.md`.

---

## 1. Modal CRUD Pattern

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

### Success Response — Full Table Refresh

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

**Preserve page state.** If the page has pagination, filter, sort, or search state, the `modal_success` fragment must accept and re-emit that state so the OOB table refresh renders the same view the user was looking at. Otherwise the create/update appears to "lose" the user's filters. Example signature:

```java
public static native TemplateInstance item$modal_success(
    String message, List<Item> items,
    String search, String sortBy, String sortDir, int page, int pageSize);
```

And inside the fragment:

```html
{#fragment id='modal_success' rendered=false}
{@String message}
{@java.util.List<com.example.entity.Item> items}
{@String search}{@String sortBy}{@String sortDir}{@int page}{@int pageSize}
<div hx-on::load="UIkit.modal('#item-modal').hide()"></div>
<div id="item-table-container" hx-swap-oob="innerHTML">
    {#include $table items=items search=search sortBy=sortBy sortDir=sortDir page=page pageSize=pageSize /}
</div>
{/fragment}
```

After a create that may sort anywhere (e.g., alphabetical by `code`), reset `page=0` so the new row is reachable from the first page.

### Success Response — Single Row Update

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

## 2. Active Search

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

## 3. Click-to-Edit

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

## 4. Edit Row

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

## 5. Delete Row

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

## 6. Bulk Update

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

## 7. Infinite Scroll / Click-to-Load

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

## 8. Lazy Loading

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

## 9. Inline Validation

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

## 10. Tabs

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

## 11. Cascading Selects

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

## 12. Progress Bar

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

## 13. File Upload with Progress

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

## 14. Polling

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

## 15. Keyboard Shortcuts

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

## 16. Drag & Drop / Sortable

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

## 17. Multi-Region Updates

Four strategies for updating elements outside the direct `hx-target`. Pick by who drives the update and how decoupled the producer/consumer are:

| Strategy | Driver | Coupling | Reach for when… |
|----------|--------|----------|-----------------|
| OOB swaps (`hx-swap-oob`) | Server | Server knows IDs | Server response can name every element to update |
| `hx-select-oob` | Requesting element | Client picks from response | Caller knows which IDs in the response are interesting |
| `HX-Trigger` response header | Server → client events | Decoupled by event name | Action on X should refresh subscribers across the page |
| `htmx.trigger` from `hx-on::after-request` | Client → client events | Decoupled by event name | Coordinating purely client-side after a request |

### 1. OOB Swaps (Server-Driven)

Server includes extra elements with `hx-swap-oob` in the response. See `attributes.md` § OOB Updates for swap strategy values and HTML encapsulation rules.

```html
<!-- Main response -->
<div id="main-content">Updated content</div>

<!-- OOB updates -->
<span id="item-count" hx-swap-oob="innerHTML">42</span>
<div id="notification" hx-swap-oob="true">Item saved!</div>
```

### 2. `hx-select-oob` (Client-Driven)

Requesting element picks specific parts of the response for OOB swap:

```html
<button hx-get="/data"
        hx-target="#results"
        hx-select="#results"
        hx-select-oob="#count:innerHTML, #status:outerHTML">
    Load
</button>
```

### 3. Server-Triggered Events

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

### 4. Client-Side Custom Events

Coordinate via `htmx.trigger` from request lifecycle hooks:

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
