# Technical Specification: Feature 004 - Relationship Management

This document describes the technical implementation requirements for the Relationship Management feature, covering CRUD operations for the Relationship master data entity.

---

## 1. Database Schema

### 1.1 Relationship Table

**Migration**: `V007__Create_relationship_table.sql`

**Key Constraints:**
- `code`: Max 10 characters, unique, uppercase, not null
- `description`: Max 255 characters, unique, not null
- Audit fields track creation and modification metadata

**Important - PostgreSQL ID Generation:**
- Primary key column must use `BIGSERIAL` type (auto-increment)
- This maps to `GenerationType.IDENTITY` in JPA
- Do NOT use sequences - Panache's default sequence strategy conflicts with `BIGSERIAL`

```sql
CREATE TABLE relationship (
    id BIGSERIAL PRIMARY KEY,  -- Must be BIGSERIAL, not BIGINT with sequence
    code VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

-- Seed data
INSERT INTO relationship (code, description) VALUES
    ('SPOUSE', 'Spouse'),
    ('PARENT', 'Parent'),
    ('CHILD', 'Child'),
    ('SIBLING', 'Sibling'),
    ('COLLEAGUE', 'Colleague'),
    ('FRIEND', 'Friend');
```

---

## 2. Entity Design

### 2.1 Relationship Entity

**File**: `src/main/java/io/archton/scaffold/entity/Relationship.java`

**Pattern**: Active Record using PanacheEntityBase (no separate repository class)

**Important - Use PanacheEntityBase, NOT PanacheEntity:**
- `PanacheEntity` assumes sequence-based ID generation (creates `entity_SEQ` sequence)
- `PanacheEntityBase` allows explicit `@Id` with `GenerationType.IDENTITY`
- When using PostgreSQL `BIGSERIAL`, you MUST use `PanacheEntityBase`

```java
package io.archton.scaffold.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "relationship", uniqueConstraints = {
    @UniqueConstraint(columnNames = "code"),
    @UniqueConstraint(columnNames = "description")
})
public class Relationship extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    public String code;

    @Column(name = "description", nullable = false, unique = true, length = 255)
    public String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    // Static finder methods (Active Record pattern)
    public static Relationship findByCode(String code) {
        return find("code", code).firstResult();
    }

    public static Relationship findByDescription(String description) {
        return find("description", description).firstResult();
    }

    public static List<Relationship> listAllOrdered() {
        return list("ORDER BY code ASC");
    }

    // Lifecycle callbacks
    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
```

---

## 3. Resource Layer

### 3.1 RelationshipResource

**File**: `src/main/java/io/archton/scaffold/router/RelationshipResource.java`

**Security**: `@RolesAllowed("admin")` - Admin-only access

**Pattern**: Follows ARCHITECTURE.md Section 4.2 with:
- Single `Templates` class with type-safe fragment methods (`relationship$table`, etc.)
- Qute `{#fragment}` sections for HTMX partial responses (compile-time validated)
- **Modal-based CRUD** using UIkit modals for Create, Edit, and Delete operations
- OOB swaps for table/row updates after modal form submission

### 3.2 Endpoints

| Method | Path | Handler | Description | Status |
|--------|------|---------|-------------|--------|
| GET | `/relationships` | `list()` | List all relationships (full page or table fragment) | ⏳ TODO |
| GET | `/relationships/create` | `createForm()` | Return create form modal content | ⏳ TODO |
| POST | `/relationships` | `create()` | Submit create form from modal | ⏳ TODO |
| GET | `/relationships/{id}/edit` | `editForm()` | Return edit form modal content with entity data | ⏳ TODO |
| PUT | `/relationships/{id}` | `update()` | Submit edit form from modal | ⏳ TODO |
| GET | `/relationships/{id}/delete` | `deleteConfirm()` | Return delete confirmation modal content | ⏳ TODO |
| DELETE | `/relationships/{id}` | `delete()` | Execute deletion after modal confirmation | ⏳ TODO |

### 3.3 Request/Response Patterns

**List (GET /relationships)**:
- Full page: Returns `Templates.relationship(title, currentPage, userName, relationships)` (includes static modal shell)
- HTMX request: Returns `Templates.relationship$table(relationships)` fragment
- Detection: Check `HX-Request` header

**Create Form (GET /relationships/create)**:
- Returns `Templates.relationship$modal_create(new Relationship(), null)` - modal body content for create form
- Modal is shown via UIkit after HTMX swap

**Create Submit (POST /relationships)**:
- Success: Returns `Templates.relationship$modal_success(message, relationships)` with:
  - Script to close modal via `hx-on::load`
  - OOB table container refresh
- Validation error: Returns `Templates.relationship$modal_create(relationship, error)` - re-renders form with error

**Edit Form (GET /relationships/{id}/edit)**:
- Returns `Templates.relationship$modal_edit(relationship, null)` - modal body content with pre-populated form
- Modal is shown via UIkit after HTMX swap

**Edit Submit (PUT /relationships/{id})**:
- Success: Returns `Templates.relationship$modal_success_row(message, relationship)` with:
  - Script to close modal via `hx-on::load`
  - OOB row update for the edited relationship
- Validation error: Returns `Templates.relationship$modal_edit(relationship, error)` - re-renders form with error

**Delete Confirm (GET /relationships/{id}/delete)**:
- Returns `Templates.relationship$modal_delete(relationship, null)` - confirmation modal content

**Delete Execute (DELETE /relationships/{id})**:
- Success: Returns modal close + OOB row removal
- In-use error: Returns `Templates.relationship$modal_delete(relationship, error)` - shows error in modal

---

## 4. Template Structure

### 4.1 Single File with Fragments

**File**: `templates/RelationshipResource/relationship.html`

Uses Qute fragments (`{#fragment}`) for HTMX partial responses. All fragments are defined within the main template file for compile-time validation and maintainability.

**Important - Fragment Rendering Control:**
Modal fragments use `rendered=false` attribute to prevent them from being rendered as part of the main page. They are only rendered when explicitly called as standalone fragments.

**Important - Null-Safe Error Checking:**
Use `{#if error??}` syntax (double question mark) for null-safe checking of optional String parameters. This ensures the condition evaluates correctly when error is null.

### 4.2 Main Page Structure

```html
{@String title}
{@String currentPage}
{@String userName}
{@java.util.List<io.archton.scaffold.entity.Relationship> relationships}
{#include base}
{#title}{title}{/title}

<h2 class="uk-heading-small">Relationship Code Maintenance</h2>

<!-- Add Button - triggers modal AND loads content via HTMX -->
<button
    class="uk-button uk-button-primary uk-margin-bottom"
    type="button"
    hx-get="/relationships/create"
    hx-target="#relationship-modal-body"
    hx-on::after-request="UIkit.modal('#relationship-modal').show()"
>
    <span uk-icon="plus"></span> Add
</button>

<div id="relationship-table-container">{#include $table /}</div>

<!-- Static Modal Shell (always present in DOM) -->
<div id="relationship-modal" uk-modal="bg-close: false">
    <div class="uk-modal-dialog">
        <div id="relationship-modal-body" class="uk-modal-body">
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
{#if relationships.isEmpty()}
<p class="uk-text-muted">No relationships found.</p>
{#else}
<div class="uk-overflow-auto">
    <table id="relationship-table" class="uk-table uk-table-hover uk-table-divider">
        <thead>
            <tr>
                <th class="uk-table-shrink">Code</th>
                <th class="uk-table-expand">Description</th>
                <th class="uk-width-small">Actions</th>
            </tr>
        </thead>
        <tbody id="relationship-table-body">
            {#for r in relationships}
            <tr id="relationship-row-{r.id}">
                <td>{r.code}</td>
                <td>{r.description}</td>
                <td>
                    <div class="uk-button-group">
                        <button
                            class="uk-button uk-button-small uk-button-primary"
                            hx-get="/relationships/{r.id}/edit"
                            hx-target="#relationship-modal-body"
                            hx-on::after-request="UIkit.modal('#relationship-modal').show()"
                        >
                            Edit
                        </button>
                        <button
                            class="uk-button uk-button-small uk-button-danger"
                            hx-get="/relationships/{r.id}/delete"
                            hx-target="#relationship-modal-body"
                            hx-on::after-request="UIkit.modal('#relationship-modal').show()"
                        >
                            Delete
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

{!-- Create Form Modal Content --}
{#fragment id=modal_create rendered=false}
{@io.archton.scaffold.entity.Relationship relationship}
{@String error}
<h2 class="uk-modal-title">Add Relationship</h2>
{#if error??}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}
<form hx-post="/relationships" hx-target="#relationship-modal-body">
    <div class="uk-margin">
        <label class="uk-form-label">Code *</label>
        <input class="uk-input" type="text" name="code" maxlength="10" value="{relationship.code ?: ''}" required />
    </div>
    <div class="uk-margin">
        <label class="uk-form-label">Description *</label>
        <input class="uk-input" type="text" name="description" value="{relationship.description ?: ''}" required />
    </div>
    <div class="uk-margin uk-text-right">
        <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
        <button class="uk-button uk-button-primary" type="submit">Save</button>
    </div>
</form>
{/fragment}

{!-- Edit Form Modal Content --}
{#fragment id=modal_edit rendered=false}
{@io.archton.scaffold.entity.Relationship relationship}
{@String error}
<h2 class="uk-modal-title">Edit Relationship</h2>
{#if error??}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}
<form hx-put="/relationships/{relationship.id}" hx-target="#relationship-modal-body">
    <div class="uk-margin">
        <label class="uk-form-label">Code *</label>
        <input class="uk-input" type="text" name="code" maxlength="10" value="{relationship.code ?: ''}" required />
    </div>
    <div class="uk-margin">
        <label class="uk-form-label">Description *</label>
        <input class="uk-input" type="text" name="description" value="{relationship.description ?: ''}" required />
    </div>
    <details class="uk-margin">
        <summary class="uk-text-muted">Audit Information</summary>
        <div class="uk-text-small uk-text-muted uk-margin-small-top">
            <div>Created: {relationship.createdAt} by {relationship.createdBy}</div>
            <div>Updated: {relationship.updatedAt} by {relationship.updatedBy}</div>
        </div>
    </details>
    <div class="uk-margin uk-text-right">
        <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
        <button class="uk-button uk-button-primary" type="submit">Save</button>
    </div>
</form>
{/fragment}

{!-- Delete Confirmation Modal Content --}
{#fragment id=modal_delete rendered=false}
{@io.archton.scaffold.entity.Relationship relationship}
{@String error}
<h2 class="uk-modal-title">Delete Relationship</h2>
{#if error??}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}
<p>Are you sure you want to delete <strong>{relationship.code} - {relationship.description}</strong>?</p>
<div class="uk-margin uk-text-right">
    <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
    <button class="uk-button uk-button-danger" hx-delete="/relationships/{relationship.id}" hx-target="#relationship-modal-body">Delete</button>
</div>
{/fragment}

{!-- Success Response (closes modal + OOB table container refresh) --}
{#fragment id=modal_success rendered=false}
{@String message}
{@java.util.List<io.archton.scaffold.entity.Relationship> relationships}
<div hx-on::load="UIkit.modal('#relationship-modal').hide()"></div>
<div id="relationship-table-container" hx-swap-oob="innerHTML">
    {#if relationships.isEmpty()}
    <p class="uk-text-muted">No relationships found.</p>
    {#else}
    <div class="uk-overflow-auto">
        <table id="relationship-table" class="uk-table uk-table-hover uk-table-divider">
            <thead>
                <tr>
                    <th class="uk-table-shrink">Code</th>
                    <th class="uk-table-expand">Description</th>
                    <th class="uk-width-small">Actions</th>
                </tr>
            </thead>
            <tbody id="relationship-table-body">
                {#for r in relationships}
                <tr id="relationship-row-{r.id}">
                    <td>{r.code}</td>
                    <td>{r.description}</td>
                    <td>
                        <div class="uk-button-group">
                            <button class="uk-button uk-button-small uk-button-primary"
                                    hx-get="/relationships/{r.id}/edit"
                                    hx-target="#relationship-modal-body"
                                    hx-on::after-request="UIkit.modal('#relationship-modal').show()">
                                Edit
                            </button>
                            <button class="uk-button uk-button-small uk-button-danger"
                                    hx-get="/relationships/{r.id}/delete"
                                    hx-target="#relationship-modal-body"
                                    hx-on::after-request="UIkit.modal('#relationship-modal').show()">
                                Delete
                            </button>
                        </div>
                    </td>
                </tr>
                {/for}
            </tbody>
        </table>
    </div>
    {/if}
</div>
{/fragment}

{!-- Success Response for Edit (closes modal + OOB single row update) --}
{#fragment id=modal_success_row rendered=false}
{@String message}
{@io.archton.scaffold.entity.Relationship relationship}
<div hx-on::load="UIkit.modal('#relationship-modal').hide()"></div>
<tr id="relationship-row-{relationship.id}" hx-swap-oob="outerHTML">
    <td>{relationship.code}</td>
    <td>{relationship.description}</td>
    <td>
        <div class="uk-button-group">
            <button class="uk-button uk-button-small uk-button-primary"
                    hx-get="/relationships/{relationship.id}/edit"
                    hx-target="#relationship-modal-body"
                    hx-on::after-request="UIkit.modal('#relationship-modal').show()">
                Edit
            </button>
            <button class="uk-button uk-button-small uk-button-danger"
                    hx-get="/relationships/{relationship.id}/delete"
                    hx-target="#relationship-modal-body"
                    hx-on::after-request="UIkit.modal('#relationship-modal').show()">
                Delete
            </button>
        </div>
    </td>
</tr>
{/fragment}

{!-- Delete Success Response (closes modal + OOB row removal) --}
{#fragment id=modal_delete_success rendered=false}
{@Long deletedId}
<div hx-on::load="UIkit.modal('#relationship-modal').hide()"></div>
<tr id="relationship-row-{deletedId}" hx-swap-oob="delete"></tr>
{/fragment}
```

### 4.4 Type-Safe Fragment Methods

```java
@CheckedTemplate
public static class Templates {
    // Full page (with base template parameters)
    public static native TemplateInstance relationship(
        String title,
        String currentPage,
        String userName,
        List<Relationship> relationships
    );

    // Table fragment
    public static native TemplateInstance relationship$table(List<Relationship> relationships);

    // Modal content fragments
    public static native TemplateInstance relationship$modal_create(Relationship relationship, String error);
    public static native TemplateInstance relationship$modal_edit(Relationship relationship, String error);
    public static native TemplateInstance relationship$modal_delete(Relationship relationship, String error);

    // Success response fragments (close modal + OOB updates)
    public static native TemplateInstance relationship$modal_success(String message, List<Relationship> relationships);
    public static native TemplateInstance relationship$modal_success_row(String message, Relationship relationship);
    public static native TemplateInstance relationship$modal_delete_success(Long deletedId);
}
```

### 4.5 Fragment Parameters

| Fragment | Parameters | Description | Status |
|----------|------------|-------------|--------|
| `relationship$table` | `relationships: List<Relationship>` | Table with all rows (inlined) | ⏳ TODO |
| `relationship$modal_create` | `relationship: Relationship`, `error: String` | Create form modal content | ⏳ TODO |
| `relationship$modal_edit` | `relationship: Relationship`, `error: String` | Edit form modal content | ⏳ TODO |
| `relationship$modal_delete` | `relationship: Relationship`, `error: String` | Delete confirmation modal content | ⏳ TODO |
| `relationship$modal_success` | `message: String`, `relationships: List<Relationship>` | Success + close modal + OOB table container refresh | ⏳ TODO |
| `relationship$modal_success_row` | `message: String`, `relationship: Relationship` | Success + close modal + OOB single row update | ⏳ TODO |
| `relationship$modal_delete_success` | `deletedId: Long` | Close modal + OOB row removal | ⏳ TODO |

---

## 5. HTMX Patterns

See Feature 002 spec (Section 5) for detailed HTMX patterns documentation. Relationship follows the same modal-based CRUD architecture.

---

## 6. Validation Rules

### 6.1 Create Validation

| Field | Rule | Error Message |
|-------|------|---------------|
| code | Required | "Code is required." |
| code | Max 10 chars | "Code must be at most 10 characters." |
| code | Unique | "Code already exists." |
| description | Required | "Description is required." |
| description | Unique | "Description already exists." |

### 6.2 Edit Validation

Same as create, but uniqueness checks exclude current record.

### 6.3 Delete Validation

| Condition | Error Message |
|-----------|---------------|
| Relationship in use by Person records | "Cannot delete: Relationship is in use by X person(s)." |

---

## 7. Security Configuration

### 7.1 Route Protection

**application.properties**:
```properties
quarkus.http.auth.permission.admin.paths=/admin/*,/genders/*,/titles/*,/relationships/*
quarkus.http.auth.permission.admin.policy=admin
quarkus.http.auth.policy.admin.roles-allowed=admin
```

### 7.2 Resource Annotation

```java
@Path("/relationships")
@RolesAllowed("admin")
public class RelationshipResource { ... }
```

---

## 8. Navigation

### 8.1 Sidebar Menu

**Location**: Under "Maintenance" parent menu item, after Title

```html
<li class="uk-parent">
    <a href="#">Maintenance</a>
    <ul class="uk-nav-sub">
        <li class="{#if currentPage == 'gender'}uk-active{/if}">
            <a href="/genders">Gender</a>
        </li>
        <li class="{#if currentPage == 'title'}uk-active{/if}">
            <a href="/titles">Title</a>
        </li>
        <li class="{#if currentPage == 'relationship'}uk-active{/if}">
            <a href="/relationships">Relationship</a>
        </li>
    </ul>
</li>
```

---

## 9. Traceability

| Use Case | Implementation Component | Status |
|----------|-------------------------|--------|
| UC-004-01-01: View Relationship List | `RelationshipResource.list()`, `relationship.html`, `relationship$table` fragment | ⏳ TODO |
| UC-004-02-01: Display Create Form | `RelationshipResource.createForm()`, `relationship$modal_create` fragment | ⏳ TODO |
| UC-004-02-02: Submit Create Form | `RelationshipResource.create()`, `relationship$modal_success` fragment | ⏳ TODO |
| UC-004-03-01: Display Edit Form | `RelationshipResource.editForm()`, `relationship$modal_edit` fragment | ⏳ TODO |
| UC-004-03-02: Submit Edit Form | `RelationshipResource.update()`, `relationship$modal_success_row` fragment | ⏳ TODO |
| UC-004-03-03: Cancel Edit | UIkit `uk-modal-close` class (no server request) | ⏳ TODO |
| UC-004-04-01: Delete Relationship | `RelationshipResource.delete()`, `relationship$modal_delete_success` fragment | ⏳ TODO |

---

## 10. Sample Data

| Code | Description |
|------|-------------|
| SPOUSE | Spouse |
| PARENT | Parent |
| CHILD | Child |
| SIBLING | Sibling |
| COLLEAGUE | Colleague |
| FRIEND | Friend |

---

## 11. Dependencies

### 11.1 Required Extensions

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

### 11.2 Frontend Dependencies (CDN)

- HTMX 2.0.8
- UIkit 3.25.4

---

*Document Version: 1.0*
*Last Updated: January 2026*
