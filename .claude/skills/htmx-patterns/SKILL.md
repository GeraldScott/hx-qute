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
6. [Modal CRUD Pattern](#6-modal-crud-pattern)
7. [Active Search Pattern](#7-active-search-pattern)
8. [Click-to-Edit Pattern](#8-click-to-edit-pattern)
9. [Delete Row Pattern](#9-delete-row-pattern)
10. [Infinite Scroll / Click-to-Load](#10-infinite-scroll--click-to-load)
11. [Inline Validation](#11-inline-validation)
12. [Tabs Pattern](#12-tabs-pattern)
13. [Cascading Selects](#13-cascading-selects)
14. [Request Indicators](#14-request-indicators)
15. [Attribute Inheritance](#15-attribute-inheritance)
16. [JavaScript Integration](#16-javascript-integration)
17. [Security Considerations](#17-security-considerations)
18. [Anti-Patterns](#18-anti-patterns)
19. [Qute Template Integration](#19-qute-template-integration)

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

### When to Use HTMX

| Use Case | HTMX Approach |
|----------|---------------|
| Form submissions | `hx-post` with validation response |
| Dynamic content loading | `hx-get` with `hx-target` |
| Real-time updates | Polling with `hx-trigger="every Ns"` or SSE |
| Partial page updates | Target specific elements with `hx-target` |
| Modal dialogs | Load content into modal container |

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

### Response Handling Attributes

| Attribute | Description | Example |
|-----------|-------------|---------|
| `hx-target` | Element to swap content into | `hx-target="#modal-content"` |
| `hx-swap` | How to swap content | `hx-swap="innerHTML"` |
| `hx-select` | CSS selector to pick from response | `hx-select="#main-content"` |
| `hx-select-oob` | Select OOB content from response | `hx-select-oob="#alert"` |

### Behavior Attributes

| Attribute | Description | Example |
|-----------|-------------|---------|
| `hx-trigger` | Event that triggers request | `hx-trigger="click"` |
| `hx-confirm` | Confirmation dialog | `hx-confirm="Delete this?"` |
| `hx-indicator` | Loading indicator element | `hx-indicator="#spinner"` |
| `hx-disabled-elt` | Disable elements during request | `hx-disabled-elt="this"` |
| `hx-push-url` | Push URL to browser history | `hx-push-url="true"` |

### Data Attributes

| Attribute | Description | Example |
|-----------|-------------|---------|
| `hx-vals` | Additional values as JSON | `hx-vals='{"key":"value"}'` |
| `hx-include` | Include other elements' values | `hx-include="[name='email']"` |
| `hx-params` | Filter parameters | `hx-params="*"` |
| `hx-headers` | Add request headers | `hx-headers='{"X-Custom":"value"}'` |

---

## 3. Swap Strategies

### Available Swap Values

| Value | Behavior |
|-------|----------|
| `innerHTML` | Replace inner content (default) |
| `outerHTML` | Replace entire element |
| `beforebegin` | Insert before target element |
| `afterbegin` | Insert as first child |
| `beforeend` | Insert as last child |
| `afterend` | Insert after target element |
| `delete` | Delete target element |
| `none` | No swap (useful for side effects) |

### Swap Modifiers

```html
<!-- Delay swap for animation -->
<button hx-delete="/item/1" hx-swap="outerHTML swap:1s">Delete</button>

<!-- Scroll target into view -->
<button hx-get="/page/2" hx-swap="innerHTML show:top">Next Page</button>

<!-- Focus element after swap -->
<button hx-get="/form" hx-swap="innerHTML focus-scroll:true">Edit</button>

<!-- Settle time for CSS transitions -->
<button hx-get="/content" hx-swap="innerHTML settle:500ms">Load</button>
```

---

## 4. Trigger Patterns

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

### Special Triggers

```html
<!-- On page load -->
<div hx-get="/initial-data" hx-trigger="load">Loading...</div>

<!-- When element becomes visible -->
<div hx-get="/lazy-content" hx-trigger="revealed">Loading...</div>

<!-- When element enters viewport -->
<div hx-get="/content" hx-trigger="intersect once">Loading...</div>

<!-- Polling -->
<div hx-get="/status" hx-trigger="every 5s">Status: checking...</div>

<!-- Conditional triggers with filters -->
<input hx-post="/search" hx-trigger="keyup[key=='Enter']">
<button hx-post="/action" hx-trigger="click[ctrlKey]">Ctrl+Click</button>
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

### OOB for Table Rows

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

## 6. Modal CRUD Pattern

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

## 7. Active Search Pattern

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

---

## 8. Click-to-Edit Pattern

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

## 9. Delete Row Pattern

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
tr.htmx-swapping {
    opacity: 0;
    transition: opacity 1s ease-out;
}
```

---

## 10. Infinite Scroll / Click-to-Load

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

---

## 11. Inline Validation

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

---

## 12. Tabs Pattern

HATEOAS-style tabs with full HTML response.

```html
<div id="tabs" hx-target="#tab-content" hx-swap="innerHTML">
    <div class="uk-tab" role="tablist">
        <button hx-get="/tab1" class="uk-active" role="tab" aria-selected="true">Tab 1</button>
        <button hx-get="/tab2" role="tab" aria-selected="false">Tab 2</button>
        <button hx-get="/tab3" role="tab" aria-selected="false">Tab 3</button>
    </div>
    <div id="tab-content" role="tabpanel" class="uk-margin">
        <!-- Tab content loaded here -->
    </div>
</div>
```

### Tab Response with Active State Update

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

## 13. Cascading Selects

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

---

## 14. Request Indicators

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

### Inherited Indicator with Override

```html
<main hx-indicator="#global-indicator">
    <button hx-post="/example" hx-indicator="inherit, #local-spinner">
        Submit
        <img id="local-spinner" class="htmx-indicator" src="/img/bars.svg">
    </button>
</main>
<img id="global-indicator" class="htmx-indicator" src="/img/spinner.gif">
```

---

## 15. Attribute Inheritance

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
<div hx-target="#content" hx-disinherit="*">
    <!-- Child elements do NOT inherit hx-target -->
    <button hx-get="/page" hx-target="this">Load Here</button>
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

## 16. JavaScript Integration

### When JavaScript is Appropriate

| Use Case | Approach |
|----------|----------|
| UI framework integration (modals, tooltips) | Inline `hx-on::` attributes |
| Simple DOM manipulation | Inline `hx-on::` attributes |
| Complex reusable logic | External file in `js/` |
| Third-party library initialization | `htmx.onLoad()` callback |

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

<!-- Confirm before request -->
<button hx-delete="/item/1"
        hx-on::before-request="return confirm('Delete this item?')">
    Delete
</button>

<!-- Multiple handlers -->
<button hx-get="/data"
        hx-on::before-request="console.log('Starting...')"
        hx-on::after-request="console.log('Done!')">
    Load Data
</button>
```

### HTMX Events Reference

| Event | When Triggered |
|-------|----------------|
| `htmx:beforeRequest` | Before request is made |
| `htmx:afterRequest` | After request completes |
| `htmx:beforeSwap` | Before content is swapped |
| `htmx:afterSwap` | After content is swapped |
| `htmx:load` | When new content loads into DOM |
| `htmx:confirm` | For async confirmation dialogs |

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

### Calling External Functions

```html
<button hx-delete="/items/{item.id}"
        hx-on::before-request="return Arc.confirmDelete('{item.name}')">
    Delete
</button>
```

### Processing Dynamically Inserted HTMX Content

```javascript
// When external code adds HTMX-enabled HTML
const newContent = document.getElementById('new-content');
newContent.innerHTML = '<button hx-get="/data">Load</button>';
htmx.process(newContent);
```

---

## 17. Security Considerations

### CSRF Protection

```html
<!-- Add CSRF token to all requests via inherited header -->
<html hx-headers='{"X-CSRF-TOKEN": "{csrfToken}"}'>
    ...
</html>

<!-- Or on body -->
<body hx-headers='{"X-CSRF-TOKEN": "{csrfToken}"}'>
    ...
</body>
```

### Content Security Policy

```html
<!-- Restrict HTMX requests to same origin -->
<meta name="htmx-config" content='{"selfRequestsOnly": true}'>

<!-- For CSP with nonces -->
<meta name="htmx-config" content='{"inlineStyleNonce": "random-nonce-value"}'>
```

### Disable HTMX on Untrusted Content

```html
<!-- Prevent HTMX processing on user-generated content -->
<div hx-disable>
    {unsafeUserContent}
</div>
```

### Prevent History Caching of Sensitive Data

```html
<div hx-history="false">
    <!-- Sensitive content not cached in localStorage -->
</div>
```

### XSS Prevention

- **Always escape user content** in templates
- Use Qute's automatic escaping (`{value}` escapes by default)
- Only use `{value.raw}` for trusted HTML content

---

## 18. Anti-Patterns

### Do NOT Fetch JSON and Render Client-Side

```html
<!-- WRONG: Defeats HTMX's purpose -->
<script>
fetch('/api/items')
    .then(r => r.json())
    .then(data => {
        // Client-side rendering
        renderItems(data);
    });
</script>

<!-- CORRECT: Return HTML from server -->
<div hx-get="/items" hx-target="this">
    Loading...
</div>
```

### Do NOT Use Heavy Client-Side State

```html
<!-- WRONG: Complex state management -->
<script>
const state = { items: [], filter: '', page: 1 };
// ... state management code
</script>

<!-- CORRECT: Server is source of truth -->
<form hx-get="/items" hx-target="#results">
    <input name="filter" value="{currentFilter}">
    <input type="hidden" name="page" value="{currentPage}">
</form>
```

### Do NOT Use jQuery for DOM Manipulation

```html
<!-- WRONG: Unnecessary overhead -->
<script>
$('#button').click(function() {
    $(this).hide();
});
</script>

<!-- CORRECT: Use hx-on or vanilla JS -->
<button hx-on:click="this.style.display='none'">Hide Me</button>
```

### Do NOT Put More Than 2-3 Statements in Inline Scripts

```html
<!-- WRONG: Obscures template structure -->
<button hx-on:click="
    const x = this.dataset.value;
    const y = document.getElementById('target');
    if (x > 10) {
        y.classList.add('active');
        y.innerHTML = 'High value';
    } else {
        y.classList.remove('active');
        y.innerHTML = 'Low value';
    }
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

## 19. Qute Template Integration

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
- [ ] Use `hx-on::` for JavaScript integration
- [ ] Use `rendered=false` for Qute page fragments

### Response Status Codes

| Status | HTMX Behavior |
|--------|---------------|
| 200 OK | Normal swap into target |
| 204 No Content | No swap, request completed |
| 286 | Stop polling |
| 4xx/5xx | Use `hx-target-error` or response-targets extension |

---

*Last Updated: 2026-01-10*
*HTMX Version: 2.0.8*
