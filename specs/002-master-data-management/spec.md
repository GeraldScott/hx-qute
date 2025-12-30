# Technical Specification: Feature 002 - Master Data Management

This document describes the technical implementation requirements for the Master Data Management feature, focusing on Gender entity CRUD operations.

---

## 1. Database Schema

### 1.1 Gender Table

**Key Constraints:**
- `code`: Max 1 character, unique, uppercase, not null
- `description`: Max 255 characters, unique, not null
- Audit fields track creation and modification metadata

**Important - PostgreSQL ID Generation:**
- Primary key column must use `BIGSERIAL` type (auto-increment)
- This maps to `GenerationType.IDENTITY` in JPA
- Do NOT use sequences - Panache's default sequence strategy conflicts with `BIGSERIAL`

```sql
CREATE TABLE gender (
    id BIGSERIAL PRIMARY KEY,  -- Must be BIGSERIAL, not BIGINT with sequence
    code VARCHAR(1) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);
```

---

## 2. Entity Design

### 2.1 Gender Entity

**File**: `src/main/java/io/archton/scaffold/entity/Gender.java`

**Pattern**: Active Record using PanacheEntityBase (no separate repository class)

**Important - Use PanacheEntityBase, NOT PanacheEntity:**
- `PanacheEntity` assumes sequence-based ID generation (creates `entity_SEQ` sequence)
- `PanacheEntityBase` allows explicit `@Id` with `GenerationType.IDENTITY`
- When using PostgreSQL `BIGSERIAL`, you MUST use `PanacheEntityBase`

```java
package io.archton.scaffold.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "gender", uniqueConstraints = {
    @UniqueConstraint(columnNames = "code"),
    @UniqueConstraint(columnNames = "description")
})
public class Gender extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", nullable = false, unique = true, length = 1)
    public String code;

    @Column(name = "description", nullable = false, unique = true, length = 255)
    public String description;

    @Column(name = "created_at")
    public OffsetDateTime createdAt;

    @Column(name = "updated_at")
    public OffsetDateTime updatedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    // Static finder methods (Active Record pattern)
    public static Gender findByCode(String code) {
        return find("code", code).firstResult();
    }

    public static Gender findByDescription(String description) {
        return find("description", description).firstResult();
    }

    public static List<Gender> listAllOrdered() {
        return list("ORDER BY code ASC");
    }

    // Lifecycle callbacks
    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
```

**Note**: Delete `repository/GenderRepository.java` if it exists - use Active Record pattern instead.

---

## 3. Resource Layer

### 3.1 GenderResource

**File**: `src/main/java/io/archton/scaffold/router/GenderResource.java`

**Security**: `@RolesAllowed("admin")` - Admin-only access

**Pattern**: Follows ARCHITECTURE.md Section 4.2 with:
- Single `Templates` class with type-safe fragment methods (`gender$row`, `gender$table`, etc.)
- Qute `{#fragment}` sections for HTMX partial responses (compile-time validated)
- **Modal-based CRUD** using UIkit modals for Create, Edit, and Delete operations
- OOB swaps for table/row updates after modal form submission

### 3.2 Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/genders` | `list()` | List all genders (full page or table fragment) |
| GET | `/genders/create` | `createForm()` | Return create form modal content |
| POST | `/genders` | `create()` | Submit create form from modal |
| GET | `/genders/{id}/edit` | `editForm()` | Return edit form modal content with entity data |
| PUT | `/genders/{id}` | `update()` | Submit edit form from modal |
| GET | `/genders/{id}/delete` | `deleteConfirm()` | Return delete confirmation modal content |
| DELETE | `/genders/{id}` | `delete()` | Execute deletion after modal confirmation |

### 3.3 Request/Response Patterns

**List (GET /genders)**:
- Full page: Returns `Templates.gender(...)` (includes static modal shell)
- HTMX request: Returns `Templates.gender$table(...)` fragment
- Detection: Check `HX-Request` header

**Create Form (GET /genders/create)**:
- Returns `Templates.gender$modal_create()` - modal body content for create form
- Modal is shown via UIkit after HTMX swap

**Create Submit (POST /genders)**:
- Success: Returns `Templates.gender$modal_success(message, genders)` with:
  - Script to close modal
  - OOB table refresh
- Validation error: Returns `Templates.gender$modal_create(gender, error)` - re-renders form with error

**Edit Form (GET /genders/{id}/edit)**:
- Returns `Templates.gender$modal_edit(gender)` - modal body content with pre-populated form
- Modal is shown via UIkit after HTMX swap

**Edit Submit (PUT /genders/{id})**:
- Success: Returns `Templates.gender$modal_success_row(message, gender)` with:
  - Script to close modal
  - OOB row update for the edited gender
- Validation error: Returns `Templates.gender$modal_edit(gender, error)` - re-renders form with error

**Delete Confirm (GET /genders/{id}/delete)**:
- Returns `Templates.gender$modal_delete(gender)` - confirmation modal content

**Delete Execute (DELETE /genders/{id})**:
- Success: Returns empty body with `HX-Trigger: closeModal` header + OOB row removal
- In-use error: Returns `Templates.gender$modal_delete(gender, error)` - shows error in modal

---

## 4. Template Structure

### 4.1 Single File with Fragments

**File**: `templates/GenderResource/gender.html`

Uses Qute fragments (`{#fragment}`) for HTMX partial responses. All fragments are defined within the main template file for compile-time validation and maintainability.

**Important - Type-Safe Template Limitations:**
With `@CheckedTemplate`, Qute validates ALL expressions at compile time against the template's parameter list. This means:
- Loop variables (e.g., `gender` from `{#for gender in genders}`) cannot be passed to included fragments
- `{#include $row gender=gender /}` does NOT work - the fragment's parameter is not in the main template's parameter list
- **Solution**: Inline row content directly in the for loop within `$table` fragment
- Standalone fragments (like `$row` for edit/cancel) must declare their own parameters with `{@...}` syntax

### 4.2 Main Page Structure

```html
{@java.util.List<io.archton.scaffold.entity.Gender> genders}
{#include base}
{#title}Gender Management{/title}

<h2 class="uk-heading-small">Gender Code Maintenance</h2>

<!-- Add Button - triggers modal AND loads content -->
<button class="uk-button uk-button-primary uk-margin-bottom"
        type="button"
        hx-get="/genders/create"
        hx-target="#gender-modal-body"
        hx-on::after-request="UIkit.modal('#gender-modal').show()">
    <span uk-icon="plus"></span> Add Gender
</button>

<div id="gender-table-container">
    {#include $table /}
</div>

<!-- Static Modal Shell (always present in DOM) -->
<div id="gender-modal" uk-modal="bg-close: false">
    <div class="uk-modal-dialog">
        <div id="gender-modal-body" class="uk-modal-body">
            <!-- Content loaded dynamically via HTMX -->
        </div>
    </div>
</div>

{/include}
```

### 4.3 Fragment Definitions

```html
{!-- Table fragment: rows are INLINED, not included from $row --}
{#fragment id=table}
{#if genders.isEmpty()}
<p class="uk-text-muted">No genders found.</p>
{#else}
<div class="uk-overflow-auto">
    <table class="uk-table uk-table-hover uk-table-divider">
        <thead>
            <tr>
                <th class="uk-table-shrink">Code</th>
                <th class="uk-table-expand">Description</th>
                <th class="uk-width-small">Actions</th>
            </tr>
        </thead>
        <tbody id="gender-table-body">
            {#for g in genders}
            <tr id="gender-row-{g.id}">
                <td>{g.code}</td>
                <td>{g.description}</td>
                <td>
                    <button class="uk-button uk-button-small uk-button-default"
                            hx-get="/genders/{g.id}/edit"
                            hx-target="#gender-modal-body"
                            hx-on::after-request="UIkit.modal('#gender-modal').show()">
                        Edit
                    </button>
                    <button class="uk-button uk-button-small uk-button-danger"
                            hx-get="/genders/{g.id}/delete"
                            hx-target="#gender-modal-body"
                            hx-on::after-request="UIkit.modal('#gender-modal').show()">
                        Delete
                    </button>
                </td>
            </tr>
            {/for}
        </tbody>
    </table>
</div>
{/if}
{/fragment}

{!-- Standalone row fragment for OOB updates after edit --}
{#fragment id=row}
{@io.archton.scaffold.entity.Gender gender}
<tr id="gender-row-{gender.id}">
    <td>{gender.code}</td>
    <td>{gender.description}</td>
    <td>
        <button class="uk-button uk-button-small uk-button-default"
                hx-get="/genders/{gender.id}/edit"
                hx-target="#gender-modal-body"
                hx-on::after-request="UIkit.modal('#gender-modal').show()">
            Edit
        </button>
        <button class="uk-button uk-button-small uk-button-danger"
                hx-get="/genders/{gender.id}/delete"
                hx-target="#gender-modal-body"
                hx-on::after-request="UIkit.modal('#gender-modal').show()">
            Delete
        </button>
    </td>
</tr>
{/fragment}

{!-- Create Form Modal Content --}
{#fragment id=modal_create}
{@io.archton.scaffold.entity.Gender gender}
{@String error}
<h2 class="uk-modal-title">Add Gender</h2>
{#if error}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}
<form hx-post="/genders" hx-target="#gender-modal-body">
    <div class="uk-margin">
        <label class="uk-form-label">Code *</label>
        <input class="uk-input" type="text" name="code" maxlength="1"
               value="{gender.code ?: ''}" required />
    </div>
    <div class="uk-margin">
        <label class="uk-form-label">Description *</label>
        <input class="uk-input" type="text" name="description"
               value="{gender.description ?: ''}" required />
    </div>
    <div class="uk-margin uk-text-right">
        <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
        <button class="uk-button uk-button-primary" type="submit">Save</button>
    </div>
</form>
{/fragment}

{!-- Edit Form Modal Content --}
{#fragment id=modal_edit}
{@io.archton.scaffold.entity.Gender gender}
{@String error}
<h2 class="uk-modal-title">Edit Gender</h2>
{#if error}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}
<form hx-put="/genders/{gender.id}" hx-target="#gender-modal-body">
    <div class="uk-margin">
        <label class="uk-form-label">Code *</label>
        <input class="uk-input" type="text" name="code" maxlength="1"
               value="{gender.code}" required />
    </div>
    <div class="uk-margin">
        <label class="uk-form-label">Description *</label>
        <input class="uk-input" type="text" name="description"
               value="{gender.description}" required />
    </div>
    <details class="uk-margin">
        <summary class="uk-text-muted">Audit Information</summary>
        <div class="uk-text-small uk-text-muted uk-margin-small-top">
            <div>Created: {gender.createdAt} by {gender.createdBy}</div>
            <div>Updated: {gender.updatedAt} by {gender.updatedBy}</div>
        </div>
    </details>
    <div class="uk-margin uk-text-right">
        <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
        <button class="uk-button uk-button-primary" type="submit">Save</button>
    </div>
</form>
{/fragment}

{!-- Delete Confirmation Modal Content --}
{#fragment id=modal_delete}
{@io.archton.scaffold.entity.Gender gender}
{@String error}
<h2 class="uk-modal-title">Delete Gender</h2>
{#if error}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}
<p>Are you sure you want to delete <strong>{gender.code} - {gender.description}</strong>?</p>
<p class="uk-text-muted">This action cannot be undone.</p>
<div class="uk-margin uk-text-right">
    <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
    <button class="uk-button uk-button-danger"
            hx-delete="/genders/{gender.id}"
            hx-target="#gender-modal-body">Delete</button>
</div>
{/fragment}

{!-- Success Response (closes modal + OOB table refresh) --}
{#fragment id=modal_success}
{@String message}
{@java.util.List<io.archton.scaffold.entity.Gender> genders}
<!-- Empty modal body (will be hidden) -->
<div hx-on::load="UIkit.modal('#gender-modal').hide()"></div>

<!-- OOB: Refresh entire table body -->
<tbody id="gender-table-body" hx-swap-oob="innerHTML">
    {#for g in genders}
    <tr id="gender-row-{g.id}">
        <td>{g.code}</td>
        <td>{g.description}</td>
        <td>
            <button class="uk-button uk-button-small uk-button-default"
                    hx-get="/genders/{g.id}/edit"
                    hx-target="#gender-modal-body"
                    hx-on::after-request="UIkit.modal('#gender-modal').show()">
                Edit
            </button>
            <button class="uk-button uk-button-small uk-button-danger"
                    hx-get="/genders/{g.id}/delete"
                    hx-target="#gender-modal-body"
                    hx-on::after-request="UIkit.modal('#gender-modal').show()">
                Delete
            </button>
        </td>
    </tr>
    {/for}
</tbody>
{/fragment}

{!-- Success Response for Edit (closes modal + OOB single row update) --}
{#fragment id=modal_success_row}
{@String message}
{@io.archton.scaffold.entity.Gender gender}
<!-- Empty modal body (will be hidden) -->
<div hx-on::load="UIkit.modal('#gender-modal').hide()"></div>

<!-- OOB: Update single row -->
<tr id="gender-row-{gender.id}" hx-swap-oob="outerHTML">
    <td>{gender.code}</td>
    <td>{gender.description}</td>
    <td>
        <button class="uk-button uk-button-small uk-button-default"
                hx-get="/genders/{gender.id}/edit"
                hx-target="#gender-modal-body"
                hx-on::after-request="UIkit.modal('#gender-modal').show()">
            Edit
        </button>
        <button class="uk-button uk-button-small uk-button-danger"
                hx-get="/genders/{gender.id}/delete"
                hx-target="#gender-modal-body"
                hx-on::after-request="UIkit.modal('#gender-modal').show()">
            Delete
        </button>
    </td>
</tr>
{/fragment}

{!-- Delete Success Response (closes modal + OOB row removal) --}
{#fragment id=modal_delete_success}
{@Long deletedId}
<!-- Empty modal body (will be hidden) -->
<div hx-on::load="UIkit.modal('#gender-modal').hide()"></div>

<!-- OOB: Remove the deleted row -->
<tr id="gender-row-{deletedId}" hx-swap-oob="delete"></tr>
{/fragment}
```

### 4.4 Type-Safe Fragment Methods

```java
@CheckedTemplate
public static class Templates {
    // Full page
    public static native TemplateInstance gender(List<Gender> genders);

    // Table fragments
    public static native TemplateInstance gender$table(List<Gender> genders);
    public static native TemplateInstance gender$row(Gender gender);

    // Modal content fragments
    public static native TemplateInstance gender$modal_create(Gender gender, String error);
    public static native TemplateInstance gender$modal_edit(Gender gender, String error);
    public static native TemplateInstance gender$modal_delete(Gender gender, String error);

    // Success response fragments (close modal + OOB updates)
    public static native TemplateInstance gender$modal_success(String message, List<Gender> genders);
    public static native TemplateInstance gender$modal_success_row(String message, Gender gender);
    public static native TemplateInstance gender$modal_delete_success(Long deletedId);
}
```

### 4.5 Fragment Parameters

| Fragment | Parameters | Description |
|----------|------------|-------------|
| `gender$table` | `genders: List<Gender>` | Table with all rows (inlined) |
| `gender$row` | `gender: Gender` | Single row for OOB updates |
| `gender$modal_create` | `gender: Gender`, `error: String` | Create form modal content |
| `gender$modal_edit` | `gender: Gender`, `error: String` | Edit form modal content |
| `gender$modal_delete` | `gender: Gender`, `error: String` | Delete confirmation modal content |
| `gender$modal_success` | `message: String`, `genders: List<Gender>` | Success + close modal + OOB table refresh |
| `gender$modal_success_row` | `message: String`, `gender: Gender` | Success + close modal + OOB single row update |
| `gender$modal_delete_success` | `deletedId: Long` | Close modal + OOB row removal |

---

## 5. HTMX Patterns

### 5.1 Modal-Based CRUD Architecture

The application uses a **single static modal shell** that is always present in the DOM. Modal content is loaded dynamically via HTMX, and the modal is shown/hidden using UIkit's JavaScript API through inline event handlers.

**Key Pattern:**
1. Button triggers HTMX request to load modal content
2. Content is swapped into the modal body (`#gender-modal-body`)
3. After swap, inline `hx-on::after-request` handler shows the modal
4. Form submissions target the same modal body
5. Success responses include OOB swaps to update the table + hide the modal

### 5.2 Opening Modals (Load + Show)

```html
<!-- Add Button: Load create form, then show modal -->
<button class="uk-button uk-button-primary"
        hx-get="/genders/create"
        hx-target="#gender-modal-body"
        hx-on::after-request="UIkit.modal('#gender-modal').show()">
    <span uk-icon="plus"></span> Add Gender
</button>

<!-- Edit Button: Load edit form with entity data, then show modal -->
<button class="uk-button uk-button-small uk-button-default"
        hx-get="/genders/{gender.id}/edit"
        hx-target="#gender-modal-body"
        hx-on::after-request="UIkit.modal('#gender-modal').show()">
    Edit
</button>

<!-- Delete Button: Load confirmation, then show modal -->
<button class="uk-button uk-button-small uk-button-danger"
        hx-get="/genders/{gender.id}/delete"
        hx-target="#gender-modal-body"
        hx-on::after-request="UIkit.modal('#gender-modal').show()">
    Delete
</button>
```

**Why `hx-on::after-request`?** This inline JavaScript handler (allowed by HTMX) executes after the HTMX request completes, ensuring the modal content is loaded before showing. This avoids the need for `<script>` tags.

### 5.3 Form Submissions in Modal

```html
<!-- Create form submits to modal body -->
<form hx-post="/genders" hx-target="#gender-modal-body">
    <input name="code" ... />
    <input name="description" ... />
    <button type="submit">Save</button>
</form>

<!-- Edit form submits to modal body -->
<form hx-put="/genders/{gender.id}" hx-target="#gender-modal-body">
    <input name="code" ... />
    <input name="description" ... />
    <button type="submit">Save</button>
</form>

<!-- Delete confirmation button -->
<button hx-delete="/genders/{gender.id}" hx-target="#gender-modal-body">
    Delete
</button>
```

### 5.4 Closing Modal + OOB Updates (Success)

On successful form submission, the server returns a response that:
1. Closes the modal via `hx-on::load` inline handler
2. Updates the table via OOB swap

```html
<!-- Success response fragment -->
<!-- Empty div that closes modal when loaded -->
<div hx-on::load="UIkit.modal('#gender-modal').hide()"></div>

<!-- OOB swap: Update table body -->
<tbody id="gender-table-body" hx-swap-oob="innerHTML">
    {#for g in genders}
    <tr id="gender-row-{g.id}">...</tr>
    {/for}
</tbody>
```

**For Edit Success (single row update):**
```html
<div hx-on::load="UIkit.modal('#gender-modal').hide()"></div>

<!-- OOB swap: Update single row -->
<tr id="gender-row-{gender.id}" hx-swap-oob="outerHTML">
    <td>{gender.code}</td>
    <td>{gender.description}</td>
    <td>...</td>
</tr>
```

**For Delete Success (row removal):**
```html
<div hx-on::load="UIkit.modal('#gender-modal').hide()"></div>

<!-- OOB swap: Remove row -->
<tr id="gender-row-{deletedId}" hx-swap-oob="delete"></tr>
```

### 5.5 Cancel Button (UIkit Modal Close)

The Cancel button uses UIkit's `uk-modal-close` class which automatically closes the modal without any HTMX request:

```html
<button class="uk-button uk-button-default uk-modal-close" type="button">
    Cancel
</button>
```

### 5.6 Error Handling in Modal

On validation errors, the server re-renders the form with error message. The modal stays open:

```html
{#if error}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}
<form ...>
    <!-- Form fields with preserved values -->
</form>
```

### 5.7 Important Notes

**No `<script>` tags needed:** All JavaScript is inline via:
- `hx-on::after-request` - Execute JS after HTMX request completes
- `hx-on::load` - Execute JS when element is loaded into DOM
- `uk-modal-close` class - UIkit's declarative modal close

**Modal shell configuration:**
```html
<div id="gender-modal" uk-modal="bg-close: false">
```
- `bg-close: false` prevents accidental closure when clicking backdrop
- Users must explicitly click Cancel or press Escape to close

---

## 6. Validation Rules

### 6.1 Create Validation

| Field | Rule | Error Message |
|-------|------|---------------|
| code | Required | "Code is required." |
| code | Max 1 char | "Code must be 1 character." |
| code | Unique | "Code already exists." |
| description | Required | "Description is required." |
| description | Unique | "Description already exists." |

### 6.2 Edit Validation

Same as create, but uniqueness checks exclude current record.

### 6.3 Delete Validation

| Condition | Error Message |
|-----------|---------------|
| Gender in use by Person | "Cannot delete: Gender is in use by X person(s)." |

---

## 7. Security Configuration

### 7.1 Route Protection

**application.properties**:
```properties
quarkus.http.auth.permission.admin.paths=/genders,/genders/*
quarkus.http.auth.permission.admin.policy=admin
quarkus.http.auth.policy.admin.roles-allowed=admin
```

### 7.2 Resource Annotation

```java
@Path("/genders")
@RolesAllowed("admin")
public class GenderResource { ... }
```

---

## 8. Navigation

### 8.1 Sidebar Menu

**Location**: Under "Maintenance" parent menu item

```html
<li class="uk-parent">
    <a href="#">Maintenance</a>
    <ul class="uk-nav-sub">
        <li class="{#if currentPage == 'gender'}uk-active{/if}">
            <a href="/genders">Gender</a>
        </li>
    </ul>
</li>
```

---

## 9. Traceability

| Use Case | Implementation Component |
|----------|-------------------------|
| UC-002-01-01: View Gender List | `GenderResource.list()`, `gender.html`, `gender$table` fragment (rows inlined) |
| UC-002-02-01: Display Create Form | `GenderResource.createForm()`, `gender$modal_create` fragment |
| UC-002-02-02: Submit Create Form | `GenderResource.create()`, `gender$modal_success` fragment |
| UC-002-03-01: Display Edit Form | `GenderResource.editForm()`, `gender$modal_edit` fragment |
| UC-002-03-02: Submit Edit Form | `GenderResource.update()`, `gender$modal_success_row` fragment |
| UC-002-03-03: Cancel Edit | UIkit `uk-modal-close` class (no server request) |
| UC-002-04-01: Delete Gender | `GenderResource.deleteConfirm()`, `GenderResource.delete()`, `gender$modal_delete`, `gender$modal_delete_success` fragments |

---

## 10. Dependencies

### 10.1 Required Extensions

```xml
<!-- Already included in project -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-orm-panache</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-qute</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security-jpa</artifactId>
</dependency>
```

### 10.2 Frontend Dependencies (CDN)

- HTMX 2.0.8
- UIkit 3.25

---

*Document Version: 2.0*
*Last Updated: December 2025*
*Changes: Redesigned from inline editing to modal-based CRUD using UIkit modals with HTMX integration. Added modal fragments, OOB swap patterns for table/row updates, and inline JavaScript via hx-on attributes.*
