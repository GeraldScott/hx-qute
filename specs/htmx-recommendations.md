# HTMX Best Practices Review: SYSTEM-SPECIFICATION.md

**Document:** Review and Recommendations  
**Target:** `specs/SYSTEM-SPECIFICATION.md`  
**Date:** December 2025  
**Status:** Recommendations for Implementation

---

## Executive Summary

The current specification demonstrates solid foundational HTMX patterns including inline row editing, OOB swaps, and content negotiation. However, several HTMX-native patterns are underutilized or missing. This document provides specific recommendations to make the application more "HTMX-native."

---

## Table of Contents

1. [What the Specification Does Well](#1-what-the-specification-does-well)
2. [Recommendations for Improvement](#2-recommendations-for-improvement)
   - [2.1 Add hx-boost for Progressive Enhancement](#21-add-hx-boost-for-progressive-enhancement)
   - [2.2 Leverage Attribute Inheritance](#22-leverage-attribute-inheritance)
   - [2.3 Add hx-indicator Consistently](#23-add-hx-indicator-consistently)
   - [2.4 Add hx-sync for Request Management](#24-add-hx-sync-for-request-management)
   - [2.5 Implement Active Search Pattern](#25-implement-active-search-pattern)
   - [2.6 Add Inline Validation Pattern](#26-add-inline-validation-pattern)
   - [2.7 Use hx-vals for Additional Context](#27-use-hx-vals-for-additional-context)
   - [2.8 Expand HX-Trigger Response Headers](#28-expand-hx-trigger-response-headers)
   - [2.9 Add Error Handling with HX-Retarget](#29-add-error-handling-with-hx-retarget)
   - [2.10 Add hx-preserve for Input Focus](#210-add-hx-preserve-for-input-focus)
   - [2.11 Standardize Response Codes](#211-standardize-response-codes)
   - [2.12 Add Confirm Dialog Customization](#212-add-confirm-dialog-customization)
   - [2.13 Update Template Partials Structure](#213-update-template-partials-structure)
3. [Summary and Priority Matrix](#3-summary-and-priority-matrix)
4. [Proposed New Sections for Specification](#4-proposed-new-sections-for-specification)

---

## 1. What the Specification Does Well

### 1.1 Inline Row Editing Pattern

The click-to-edit pattern with `hx-target="closest tr"` and `hx-swap="outerHTML"` is textbook HTMX:

```html
<button hx-get="/persons/{person.id}/edit"
        hx-target="closest tr"
        hx-swap="outerHTML">Edit</button>
```

**Why this works:**
- Targets the nearest table row ancestor
- Replaces the entire row with the edit form
- No JavaScript required for state management

### 1.2 Out-of-Band Swaps for Create

Using `hx-swap-oob="true"` to refresh the table body after create operations is excellent:

```html
<tbody id="persons-table-body" hx-swap-oob="true">
    {#for person in persons}...{/for}
</tbody>
```

**Why this works:**
- Single response updates multiple page areas
- Table refresh happens automatically after form submission
- No need for client-side event coordination

### 1.3 Delete with Row Removal

The `hx-swap="delete"` pattern with timing modifier is well-implemented:

```html
<button hx-delete="/persons/{id}"
        hx-confirm="Are you sure?"
        hx-target="closest tr"
        hx-swap="delete swap:300ms">Delete</button>
```

**Why this works:**
- Native browser confirmation dialog
- Row removal with animation timing
- Server returns empty response

### 1.4 Content Negotiation

The `isHtmxRequest()` helper for returning partials vs full pages is correct:

```java
private boolean isHtmxRequest(HttpHeaders headers) {
    return headers.getHeaderString("HX-Request") != null;
}
```

**Why this works:**
- Direct navigation gets full page with layout
- HTMX requests get minimal partials
- Same endpoint serves both use cases

---

## 2. Recommendations for Improvement

### 2.1 Add hx-boost for Progressive Enhancement

#### Current Gap

The spec doesn't mention `hx-boost`, a fundamental HTMX pattern for progressively enhancing standard links and forms.

#### Recommendation

Add `hx-boost="true"` to the base template's body or main container to automatically convert all anchor tags and forms to AJAX requests.

#### Implementation

**Update `templates/base.html`:**

```html
<body hx-boost="true">
    <!-- All links and forms automatically use HTMX -->
    <div class="uk-offcanvas-content">
        ...
    </div>
</body>
```

**Opt-out specific links that require full reload:**

```html
<a href="/download/report.pdf" hx-boost="false">Download PDF</a>
<a href="https://external-site.com" hx-boost="false">External Link</a>
```

#### Benefits

| Benefit | Description |
|---------|-------------|
| Faster navigation | No full page reload for internal links |
| Automatic URL updates | Browser history works correctly |
| Graceful degradation | Works without JavaScript enabled |
| Less boilerplate | No need to add `hx-get` to every link |

#### Documentation Addition

Add to Section 6 of SYSTEM-SPECIFICATION.md:

```markdown
### 6.10 Progressive Enhancement with hx-boost

Enable `hx-boost="true"` on the body element to automatically convert standard 
navigation to HTMX requests. This provides:

- Faster navigation (no full page reload)
- Automatic `hx-push-url="true"` behavior  
- Graceful degradation when JavaScript is disabled

**Base template:**
```html
<body hx-boost="true">
```

**Opt-out for specific elements:**
```html
<a href="/download/file.pdf" hx-boost="false">Download</a>
```

**Opt-out for entire sections:**
```html
<nav hx-boost="false">
    <!-- External links here -->
</nav>
```
```

---

### 2.2 Leverage Attribute Inheritance

#### Current Gap

The spec repeats `hx-target` and `hx-swap` on every button. HTMX supports attribute inheritance from parent elements, reducing repetition.

#### Current (Repetitive)

```html
<tr>
    <td>...</td>
    <td>
        <button hx-get="/persons/{id}/edit" 
                hx-target="closest tr" 
                hx-swap="outerHTML">Edit</button>
        <button hx-delete="/persons/{id}" 
                hx-target="closest tr" 
                hx-swap="delete">Delete</button>
    </td>
</tr>
```

#### Improved (With Inheritance)

```html
<tbody hx-target="closest tr" hx-swap="outerHTML">
    <tr>
        <td>...</td>
        <td>
            <button hx-get="/persons/{id}/edit">Edit</button>
            <button hx-delete="/persons/{id}" hx-swap="delete swap:300ms">Delete</button>
        </td>
    </tr>
</tbody>
```

#### Inheritable Attributes

| Attribute | Inherits | Notes |
|-----------|----------|-------|
| `hx-target` | ✅ Yes | Child elements use parent's target |
| `hx-swap` | ✅ Yes | Can be overridden per-element |
| `hx-boost` | ✅ Yes | Applies to all descendant links/forms |
| `hx-confirm` | ❌ No | Must be specified per-element |
| `hx-indicator` | ✅ Yes | Useful for section-wide spinners |
| `hx-headers` | ✅ Yes | Merged with child headers |

#### Documentation Addition

Update Section 6.1 table to include inheritance column.

---

### 2.3 Add hx-indicator Consistently

#### Current Gap

Section 6.7 mentions loading indicators but doesn't standardize their use across the application.

#### Recommendation

Define a global loading indicator and per-element patterns.

#### Global Indicator Pattern

**Add to `templates/base.html`:**

```html
<body hx-boost="true">
    <!-- Global loading indicator -->
    <div id="global-spinner" class="htmx-indicator">
        <div uk-spinner></div>
        <span class="uk-margin-small-left">Loading...</span>
    </div>
    
    <div class="uk-offcanvas-content">
        ...
    </div>
</body>
```

**Add to `style.css`:**

```css
/* HTMX Loading Indicators */
.htmx-indicator {
    display: none;
    position: fixed;
    top: 1rem;
    right: 1rem;
    z-index: 1000;
    background: var(--sidebar-bg);
    padding: 0.5rem 1rem;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* Show indicator when HTMX request is in flight */
.htmx-request .htmx-indicator,
.htmx-request.htmx-indicator {
    display: flex;
    align-items: center;
}

/* Disable interactive elements during request */
.htmx-request button[hx-get],
.htmx-request button[hx-post],
.htmx-request button[hx-put],
.htmx-request button[hx-delete],
.htmx-request a[hx-get] {
    opacity: 0.6;
    pointer-events: none;
    cursor: wait;
}

/* Inline button spinner */
.btn-spinner {
    display: none;
}

.htmx-request .btn-spinner {
    display: inline-block;
}

.htmx-request .btn-text {
    display: none;
}
```

#### Per-Element Indicator Pattern

```html
<button hx-post="/persons/create"
        hx-target="#person-create-container"
        hx-indicator="#create-spinner"
        class="uk-button uk-button-primary">
    <span class="btn-spinner" uk-spinner="ratio: 0.5"></span>
    <span class="btn-text">Save</span>
</button>
```

#### Table Row Loading State

```html
<tr hx-indicator="this">
    <td>
        <button hx-get="/persons/{id}/edit">Edit</button>
    </td>
</tr>
```

```css
tr.htmx-request {
    opacity: 0.6;
    background: #f5f5f5;
}
```

---

### 2.4 Add hx-sync for Request Management

#### Current Gap

No mention of request synchronization, which is critical for filter/search operations where rapid user input can cause race conditions.

#### Problem Scenario

User types quickly in search box:
1. Types "Jo" → Request 1 sent
2. Types "John" → Request 2 sent  
3. Request 2 returns first (faster query)
4. Request 1 returns last → **Overwrites correct results!**

#### Solution

Use `hx-sync` to manage concurrent requests.

#### Implementation

**Filter Form with Sync:**

```html
<form hx-get="/persons"
      hx-target="#persons-table-container"
      hx-sync="this:replace"
      class="uk-grid-small" uk-grid>
    <input type="text" name="filter" 
           hx-get="/persons"
           hx-trigger="input changed delay:300ms"
           hx-target="#persons-table-container"
           hx-sync="closest form:abort"/>
</form>
```

#### Sync Strategies

| Strategy | Behavior | Use Case |
|----------|----------|----------|
| `drop` | Drop new request if one in flight | Prevent duplicate submissions |
| `abort` | Abort current, start new | Search/filter (latest wins) |
| `replace` | Abort current, replace with new | Default for forms |
| `queue` | Queue requests, execute in order | Sequential operations |
| `queue first` | Queue, keep only first | Batch with priority |
| `queue last` | Queue, keep only last | Latest state wins |

#### Documentation Addition

```markdown
### 6.11 Request Synchronization with hx-sync

Use `hx-sync` to manage concurrent requests and prevent race conditions.

**Filter Form (abort previous on new input):**
```html
<input type="search" name="filter"
       hx-get="/persons"
       hx-trigger="input changed delay:300ms"
       hx-sync="closest form:abort"/>
```

**Submit Button (prevent double-click):**
```html
<button hx-post="/persons/create"
        hx-sync="this:drop">
    Save
</button>
```

**Form-wide sync:**
```html
<form hx-post="/persons/create"
      hx-sync="this:abort">
```
```

---

### 2.5 Implement Active Search Pattern

#### Current Gap

Filter requires explicit button click. HTMX supports live search with debouncing for better UX.

#### Current Pattern

```html
<form hx-get="/persons" hx-target="#persons-table-container">
    <input type="text" name="filter"/>
    <button type="submit">Filter</button>  <!-- Required click -->
</form>
```

#### Improved Active Search

```html
<form hx-get="/persons"
      hx-target="#persons-table-container"
      hx-trigger="submit"
      hx-sync="this:replace"
      hx-push-url="true"
      class="uk-grid-small" uk-grid>
    <div class="uk-width-1-2@s uk-position-relative">
        <input class="uk-input" 
               type="search" 
               name="filter"
               placeholder="Search by name..."
               value="{filterText ?: ''}"
               hx-get="/persons"
               hx-trigger="input changed delay:300ms, search"
               hx-target="#persons-table-container"
               hx-sync="closest form:abort"
               hx-indicator="#search-spinner"/>
        <span id="search-spinner" 
              class="htmx-indicator uk-position-center-right uk-margin-small-right">
            <span uk-spinner="ratio: 0.5"></span>
        </span>
    </div>
    <div class="uk-width-auto@s">
        <button type="submit" class="uk-button uk-button-primary">Filter</button>
        <a class="uk-button uk-button-default"
           hx-get="/persons"
           hx-target="#persons-table-container"
           hx-push-url="/persons">Clear</a>
    </div>
</form>
```

#### Trigger Modifiers Explained

| Modifier | Purpose |
|----------|---------|
| `input` | Fires on every keystroke |
| `changed` | Only if value actually changed |
| `delay:300ms` | Wait 300ms after last keystroke |
| `search` | Also fires on search input clear (×) button |

#### Server-Side (No Changes Needed)

The existing `list()` method already handles the `filter` query parameter.

---

### 2.6 Add Inline Validation Pattern

#### Current Gap

Validation only occurs on form submit. HTMX supports field-level inline validation for immediate feedback.

#### Implementation

**Template (Email Field with Validation):**

```html
<div class="uk-margin">
    <label class="uk-form-label" for="email">Email *</label>
    <div class="uk-form-controls">
        <input class="uk-input" 
               type="email" 
               id="email"
               name="email" 
               value="{person.email ?: ''}"
               required
               hx-post="/persons/validate/email"
               hx-trigger="blur changed"
               hx-target="next .validation-message"
               hx-swap="innerHTML"
               hx-include="this"
               hx-vals='{"excludeId": "{person.id ?: ""}"}' />
        <div class="validation-message uk-text-danger uk-text-small uk-margin-small-top"></div>
    </div>
</div>
```

**Server Endpoint:**

```java
@POST
@Path("/validate/email")
@Produces(MediaType.TEXT_HTML)
public String validateEmail(
        @FormParam("email") String email,
        @FormParam("excludeId") String excludeIdStr) {
    
    Long excludeId = null;
    if (excludeIdStr != null && !excludeIdStr.isBlank()) {
        try {
            excludeId = Long.parseLong(excludeIdStr);
        } catch (NumberFormatException e) {
            // Ignore invalid ID
        }
    }
    
    // Validation: required
    if (email == null || email.isBlank()) {
        return "<span uk-icon='icon: warning'></span> Email is required.";
    }
    
    // Validation: format
    if (!isValidEmail(email)) {
        return "<span uk-icon='icon: warning'></span> Invalid email format.";
    }
    
    // Validation: uniqueness
    Person existing = Person.findByEmail(email.trim());
    if (existing != null && !existing.id.equals(excludeId)) {
        return "<span uk-icon='icon: warning'></span> Email already registered.";
    }
    
    // Valid - return success indicator
    return "<span uk-icon='icon: check' class='uk-text-success'></span>";
}
```

**CSS Enhancement:**

```css
/* Validation states */
.validation-message:empty {
    display: none;
}

.validation-message {
    min-height: 1.5em;
}

input:user-invalid {
    border-color: #f0506e;
}

input:user-valid {
    border-color: #32d296;
}
```

#### Trigger Options for Validation

| Trigger | Behavior | Best For |
|---------|----------|----------|
| `blur changed` | Validate when leaving field, only if changed | Most fields |
| `input changed delay:500ms` | Validate while typing with debounce | Username availability |
| `change` | Validate on change (select, checkbox) | Dropdowns |

---

### 2.7 Use hx-vals for Additional Context

#### Current Gap

Hidden fields are used for passing context. `hx-vals` is cleaner and more declarative.

#### Current Pattern (Hidden Fields)

```html
<form hx-post="/persons/{id}/update">
    <input type="hidden" name="id" value="{person.id}"/>
    <input type="hidden" name="_method" value="PUT"/>
    <input name="firstName" value="{person.firstName}"/>
</form>
```

#### Improved Pattern (hx-vals)

```html
<form hx-post="/persons/{id}/update"
      hx-vals='{"id": {person.id}}'>
    <input name="firstName" value="{person.firstName}"/>
</form>
```

#### Dynamic Values with JavaScript

```html
<button hx-post="/persons/bulk-delete"
        hx-vals='js:{
            ids: Array.from(document.querySelectorAll("input.row-select:checked"))
                      .map(el => el.value)
        }'>
    Delete Selected
</button>
```

#### Merging with Form Values

`hx-vals` values are merged with form inputs. Form inputs take precedence if names conflict.

```html
<form hx-post="/persons/create"
      hx-vals='{"source": "web", "version": "1.0"}'>
    <!-- source and version added to form data -->
    <input name="firstName"/>
    <input name="email"/>
</form>
```

---

### 2.8 Expand HX-Trigger Response Headers

#### Current Gap

Section 6.9 briefly mentions HX-Trigger but doesn't show full integration patterns.

#### Server-Side Event Emission

```java
@POST
@Path("/create")
@Transactional
public Response create(...) {
    // ... create person ...
    
    // Simple event
    return Response.ok(Partials.person_create_button())
        .header("HX-Trigger", "personCreated")
        .build();
    
    // Event with data
    return Response.ok(Partials.person_create_button())
        .header("HX-Trigger", """
            {"personCreated": {"id": %d, "name": "%s"}}
            """.formatted(person.id, person.getDisplayName()))
        .build();
    
    // Multiple events
    return Response.ok(Partials.person_create_button())
        .header("HX-Trigger", "personCreated, refreshStats, showToast")
        .build();
}
```

#### Client-Side Event Listeners

```html
<!-- Refresh table when person created -->
<tbody id="persons-table-body"
       hx-get="/persons/table-body"
       hx-trigger="personCreated from:body"
       hx-swap="innerHTML">
</tbody>

<!-- Update statistics widget -->
<div id="stats-widget"
     hx-get="/dashboard/stats"
     hx-trigger="personCreated from:body, personDeleted from:body">
</div>

<!-- Show toast notification with event data -->
<div id="toast-container"
     hx-on:personCreated="showToast(event.detail)">
</div>

<script>
function showToast(detail) {
    UIkit.notification({
        message: `${detail.name} was created successfully!`,
        status: 'success',
        pos: 'top-right'
    });
}
</script>
```

#### Timing Variants

| Header | When Triggered |
|--------|----------------|
| `HX-Trigger` | Immediately after response received |
| `HX-Trigger-After-Settle` | After DOM has settled (animations complete) |
| `HX-Trigger-After-Swap` | After content has been swapped |

```java
// Highlight new row after it's inserted
return Response.ok(html)
    .header("HX-Trigger-After-Settle", "highlightNewRow")
    .build();
```

```html
<tbody hx-on:highlightNewRow="this.querySelector('tr:first-child').classList.add('highlight')">
```

---

### 2.9 Add Error Handling with HX-Retarget

#### Current Gap

Errors are returned to the same target. Sometimes errors need different placement (e.g., toast notification area instead of form).

#### Dynamic Error Targeting

**Server Response:**

```java
@POST
@Path("/create")
public Response create(...) {
    try {
        // ... validation and creation ...
        return Response.ok(Partials.person_success(...))
            .header("HX-Trigger", "personCreated")
            .build();
            
    } catch (ValidationException e) {
        // Redirect error to notification area
        return Response.ok(Partials.error_toast(e.getMessage()))
            .header("HX-Retarget", "#notification-area")
            .header("HX-Reswap", "beforeend")
            .build();
            
    } catch (Exception e) {
        // Server error handling
        return Response.status(500)
            .entity(Partials.error_toast("An unexpected error occurred"))
            .header("HX-Retarget", "#notification-area")
            .header("HX-Reswap", "beforeend")
            .build();
    }
}
```

**Template Structure:**

```html
<!-- Notification area always present in base template -->
<div id="notification-area" 
     class="uk-position-top-right uk-position-fixed"
     style="z-index: 1000; margin: 1rem;">
</div>

<!-- Form with normal target -->
<form hx-post="/persons/create"
      hx-target="#person-create-container">
    ...
</form>
```

#### Error Toast Partial

**File:** `templates/partials/error_toast.html`

```html
{@String message}

<div class="uk-alert uk-alert-danger" uk-alert>
    <a class="uk-alert-close" uk-close></a>
    <p><span uk-icon="icon: warning"></span> {message}</p>
</div>
```

---

### 2.10 Add hx-preserve for Input Focus

#### Current Gap

When swapping content, focused inputs lose focus, which is frustrating during search/filter operations.

#### Solution

Use `hx-preserve` for elements that should survive swaps.

```html
<!-- Preserve search input during table refresh -->
<input id="filter-input" 
       name="filter" 
       hx-preserve="true"
       hx-get="/persons"
       hx-trigger="input changed delay:300ms"
       hx-target="#persons-table-container"/>
```

#### How It Works

1. Element with `hx-preserve` is identified by `id`
2. Before swap, HTMX saves the element
3. After swap, if new content has same `id`, old element is restored
4. Focus, selection, and scroll position are maintained

#### Important Notes

- Requires stable `id` attribute
- Only works for elements with matching `id` in new content
- Useful for: search inputs, filter dropdowns, open accordions

---

### 2.11 Standardize Response Codes

#### Current Gap

The spec doesn't specify HTTP status codes for HTMX responses, which can cause confusion.

#### HTMX Response Code Behavior

| Status | HTMX Behavior |
|--------|---------------|
| 2xx | Normal swap |
| 204 No Content | No swap, good for deletes |
| 3xx | **Not followed by default!** |
| 4xx | Swap if `htmx.config.ignoreStatusCode` includes it |
| 5xx | Error event triggered, no swap by default |

#### Recommended Status Codes

| Scenario | Status | Response Body | Headers |
|----------|--------|---------------|---------|
| Success with content | 200 | HTML partial | - |
| Success, no content | 204 | Empty | - |
| Validation error | 200 or 422 | Form with errors | - |
| Not found | 404 | Error partial | - |
| Redirect needed | 200 | Empty | `HX-Redirect: /path` |
| Server error | 500 | Error partial | `HX-Retarget: #errors` |

#### Important: Redirects

HTMX does **not** follow HTTP 3xx redirects by default. Use response headers instead:

```java
// DON'T DO THIS - Won't work as expected
return Response.seeOther(URI.create("/persons")).build();

// DO THIS INSTEAD
return Response.ok()
    .header("HX-Redirect", "/persons")
    .build();

// Or for in-page navigation
return Response.ok()
    .header("HX-Location", "/persons")
    .build();
```

#### Client-Side Error Handling

```html
<body hx-boost="true"
      hx-on::response-error="handleHtmxError(event)">
```

```javascript
function handleHtmxError(event) {
    const xhr = event.detail.xhr;
    if (xhr.status >= 500) {
        UIkit.notification({
            message: 'Server error. Please try again.',
            status: 'danger'
        });
    }
}
```

---

### 2.12 Add Confirm Dialog Customization

#### Current Gap

Only native `hx-confirm` is shown. Custom styled confirmation dialogs are not addressed.

#### Native Confirm (Current)

```html
<button hx-delete="/persons/{id}"
        hx-confirm="Are you sure you want to delete this person?">
    Delete
</button>
```

#### Custom UIkit Modal Confirm

**Using htmx:confirm Event:**

```html
<button hx-delete="/persons/{id}"
        hx-target="closest tr"
        hx-swap="delete swap:300ms"
        data-confirm="Are you sure you want to delete this person?"
        data-confirm-title="Delete Person">
    Delete
</button>
```

```javascript
// Add to base.html or main.js
document.body.addEventListener('htmx:confirm', function(event) {
    const trigger = event.target;
    const message = trigger.dataset.confirm;
    
    if (!message) return; // No custom confirm, use default
    
    event.preventDefault(); // Stop HTMX from proceeding
    
    UIkit.modal.confirm(message, {
        labels: { ok: 'Delete', cancel: 'Cancel' }
    }).then(function() {
        // User confirmed - issue the request
        event.detail.issueRequest();
    }, function() {
        // User cancelled - do nothing
    });
});
```

#### Custom Confirm with Title

```javascript
document.body.addEventListener('htmx:confirm', function(event) {
    const trigger = event.target;
    const message = trigger.dataset.confirm;
    const title = trigger.dataset.confirmTitle || 'Confirm';
    
    if (!message) return;
    
    event.preventDefault();
    
    // Create custom modal
    const modal = UIkit.modal.dialog(`
        <div class="uk-modal-header">
            <h2 class="uk-modal-title">${title}</h2>
        </div>
        <div class="uk-modal-body">
            <p>${message}</p>
        </div>
        <div class="uk-modal-footer uk-text-right">
            <button class="uk-button uk-button-default uk-modal-close">Cancel</button>
            <button class="uk-button uk-button-danger confirm-btn">Delete</button>
        </div>
    `);
    
    modal.$el.querySelector('.confirm-btn').addEventListener('click', function() {
        modal.hide();
        event.detail.issueRequest();
    });
});
```

---

### 2.13 Update Template Partials Structure

#### Current Gap

Partials are well-structured but could be more granular for maximum reusability.

#### Proposed Enhanced Structure

```
templates/partials/
├── gender/
│   ├── table.html              # Full table with thead and tbody
│   ├── table_body.html         # Just tbody content (for OOB refresh)
│   ├── row.html                # Single row (display mode)
│   ├── row_edit.html           # Single row (edit mode)
│   ├── create_form.html        # Inline create form
│   ├── create_button.html      # Add button
│   ├── success.html            # Success with OOB table refresh
│   └── field_code.html         # Reusable code input field
│
├── person/
│   ├── table.html              # Full table
│   ├── table_body.html         # Just tbody content
│   ├── row.html                # Single row (display)
│   ├── row_edit.html           # Single row (edit mode)
│   ├── create_form.html        # Inline create form
│   ├── create_button.html      # Add button
│   ├── success.html            # Success with OOB refresh
│   ├── filter_form.html        # Standalone filter form
│   ├── field_email.html        # Email field with validation
│   └── field_gender.html       # Gender dropdown
│
├── shared/
│   ├── toast_success.html      # Reusable success notification
│   ├── toast_error.html        # Reusable error notification
│   ├── toast_info.html         # Reusable info notification
│   ├── empty_state.html        # No data found message
│   ├── loading.html            # Loading spinner
│   └── confirm_modal.html      # Confirmation dialog template
│
└── form/
    ├── input_text.html         # Reusable text input
    ├── input_email.html        # Reusable email input
    ├── input_date.html         # Reusable date picker
    ├── select.html             # Reusable dropdown
    └── validation_error.html   # Field error message
```

#### Reusable Field Partial Example

**File:** `templates/partials/form/input_email.html`

```html
{@String name}
{@String value}
{@String label}
{@Boolean required}
{@String placeholder}
{@String validationUrl}
{@String excludeId}

<div class="uk-margin">
    <label class="uk-form-label" for="{name}">{label}{#if required} *{/if}</label>
    <div class="uk-form-controls uk-position-relative">
        <input class="uk-input" 
               type="email" 
               id="{name}"
               name="{name}" 
               value="{value ?: ''}"
               placeholder="{placeholder ?: ''}"
               {#if required}required{/if}
               {#if validationUrl}
               hx-post="{validationUrl}"
               hx-trigger="blur changed"
               hx-target="next .validation-message"
               hx-swap="innerHTML"
               hx-include="this"
               hx-vals='{"excludeId": "{excludeId ?: ""}"}'
               {/if} />
        <div class="validation-message uk-text-danger uk-text-small uk-margin-small-top"></div>
    </div>
</div>
```

**Usage:**

```html
{#include partials/form/input_email 
    name="email" 
    value=person.email 
    label="Email Address"
    required=true
    placeholder="user@example.com"
    validationUrl="/persons/validate/email"
    excludeId=person.id /}
```

---

## 3. Summary and Priority Matrix

### Priority Ranking

| Priority | Recommendation | Impact | Effort |
|----------|---------------|--------|--------|
| **High** | Add `hx-boost="true"` to body | Progressive enhancement, faster nav | Low |
| **High** | Add `hx-sync` to filter/search | Prevent race conditions | Low |
| **High** | Standardize `hx-indicator` | Better UX feedback | Medium |
| **Medium** | Leverage attribute inheritance | Cleaner templates | Low |
| **Medium** | Add active search with debounce | Better search UX | Low |
| **Medium** | Add inline validation | Field-level feedback | Medium |
| **Medium** | Expand HX-Trigger patterns | Event-driven updates | Medium |
| **Low** | Use `hx-vals` instead of hidden fields | Cleaner markup | Low |
| **Low** | Add `hx-preserve` for focus | Better UX during updates | Low |
| **Low** | Document response status codes | Consistent error handling | Low |
| **Low** | Custom confirmation dialogs | Better aesthetics | Medium |
| **Low** | Enhanced partials structure | Better maintainability | High |

### Quick Wins (Implement First)

1. Add `hx-boost="true"` to `<body>` in base.html
2. Add `hx-sync="this:abort"` to filter forms
3. Move common `hx-target` and `hx-swap` to parent `<tbody>` elements
4. Add global loading indicator CSS

### Medium-Term Improvements

1. Implement active search with debouncing
2. Add inline field validation endpoints
3. Use HX-Trigger headers for event-driven updates
4. Standardize error handling with HX-Retarget

### Long-Term Enhancements

1. Refactor partials into more granular, reusable components
2. Create reusable form field partials with built-in validation
3. Implement custom UIkit confirmation dialogs

---

## 4. Proposed New Sections for Specification

Add these sections to SYSTEM-SPECIFICATION.md Section 6:

- **6.10 Progressive Enhancement with hx-boost**
- **6.11 Request Synchronization with hx-sync**
- **6.12 Inline Field Validation**
- **6.13 Dynamic Error Handling with HX-Retarget**
- **6.14 Custom Confirmation Dialogs**
- **6.15 Response Status Code Standards**
- **6.16 Attribute Inheritance Best Practices**
- **6.17 Loading Indicators and Disabled States**

---

## Appendix A: HTMX Configuration Reference

Add to base.html for optimal HTMX behavior:

```html
<script>
    htmx.config.defaultSwapStyle = 'innerHTML';
    htmx.config.defaultSettleDelay = 20;
    htmx.config.includeIndicatorStyles = false;  // We use custom CSS
    htmx.config.historyCacheSize = 10;
    htmx.config.refreshOnHistoryMiss = true;
    htmx.config.scrollBehavior = 'smooth';
</script>
```

---

## Appendix B: HTMX Debugging

Add during development:

```html
<script>
    htmx.logAll();  // Log all HTMX events to console
</script>
```

Or selectively:

```html
<script>
    document.body.addEventListener('htmx:beforeRequest', function(evt) {
        console.log('Request:', evt.detail.pathInfo.requestPath);
    });
    
    document.body.addEventListener('htmx:afterSwap', function(evt) {
        console.log('Swapped:', evt.detail.target.id);
    });
</script>
```

---

*Document generated from HTMX documentation review*  
*Reference: https://htmx.org/reference/*
