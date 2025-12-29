# Technical Specification: Feature 002 - Master Data Management

This document describes the technical implementation requirements for the Master Data Management feature, focusing on Gender entity CRUD operations.

---

## 1. Database Schema

### 1.1 Gender Table

**Key Constraints:**
- `code`: Max 1 character, unique, uppercase, not null
- `description`: Max 255 characters, unique, not null
- Audit fields track creation and modification metadata

---

## 2. Entity Design

### 2.1 Gender Entity

**File**: `src/main/java/io/archton/scaffold/entity/Gender.java`

**Pattern**: Active Record using PanacheEntity (no separate repository class)

```java
package io.archton.scaffold.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "gender", uniqueConstraints = {
    @UniqueConstraint(columnNames = "code"),
    @UniqueConstraint(columnNames = "description")
})
public class Gender extends PanacheEntity {

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
- Full page `Templates` class for direct navigation
- `Partials` class with `basePath = "partials"` for HTMX fragments
- Inline editing (no modals)
- OOB swaps for table refresh after create

### 3.2 Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/genders` | `list()` | List all genders (full page or table partial) |
| GET | `/genders/create` | `createForm()` | Display inline create form |
| GET | `/genders/create/cancel` | `createFormCancel()` | Return Add button (cancel create) |
| POST | `/genders/create` | `create()` | Submit create form |
| GET | `/genders/{id}` | `getRow()` | Get single row partial (for cancel edit) |
| GET | `/genders/{id}/edit` | `editForm()` | Display inline edit form in row |
| POST | `/genders/{id}/update` | `update()` | Submit edit form |
| DELETE | `/genders/{id}` | `delete()` | Delete gender |

### 3.3 Request/Response Patterns

**List (GET /genders)**:
- Full page: Returns `Templates.gender(...)`
- HTMX request: Returns `Partials.gender_table(...)`
- Detection: Check `HX-Request` header

**Create Submit (POST /genders/create)**:
- Success: Returns `Partials.gender_success(message, genders)` with OOB table refresh
- Validation error: Returns `Partials.gender_create_form(error)`

**Edit Submit (POST /genders/{id}/update)**:
- Success: Returns `Partials.gender_row(gender)`
- Validation error: Returns `Partials.gender_row_edit(gender, error)`

**Delete (DELETE /genders/{id})**:
- Success: Returns empty response (row removed via `hx-swap="delete"`)
- In-use error: Returns `Partials.gender_row_edit(gender, error)`

---

## 4. Template Structure

### 4.1 Full Page Template

**File**: `templates/GenderResource/gender.html`

```
{#include base}
- Page title: "Gender Management"
- Create form container: #gender-create-container
- Table container: #gender-table-container
{/include}
```

### 4.2 Partial Templates

**Directory**: `templates/partials/`

| File | Description | Target |
|------|-------------|--------|
| `gender_table.html` | Table with all rows | `#gender-table-container` |
| `gender_row.html` | Single row (display mode) | `closest tr` |
| `gender_row_edit.html` | Single row (edit mode) | `closest tr` |
| `gender_create_form.html` | Inline create form | `#gender-create-container` |
| `gender_create_button.html` | Add button | `#gender-create-container` |
| `gender_success.html` | Success message + OOB table | `#gender-create-container` |
| `gender_error.html` | Error message | Context-dependent |

### 4.3 Template Parameters

**gender_table.html**:
- `genders`: List<Gender>

**gender_row.html**:
- `gender`: Gender

**gender_row_edit.html**:
- `gender`: Gender
- `error`: String (nullable)

**gender_create_form.html**:
- `error`: String (nullable)

**gender_success.html**:
- `message`: String
- `genders`: List<Gender>

---

## 5. HTMX Patterns

### 5.1 Inline Row Editing

```html
<!-- Display Row (gender_row.html) -->
<tr>
    <td>{gender.code}</td>
    <td>{gender.description}</td>
    <td>
        <button hx-get="/genders/{gender.id}/edit"
                hx-target="closest tr"
                hx-swap="outerHTML">Edit</button>
        <button hx-delete="/genders/{gender.id}"
                hx-confirm="Are you sure?"
                hx-target="closest tr"
                hx-swap="delete swap:300ms">Delete</button>
    </td>
</tr>

<!-- Edit Row (gender_row_edit.html) -->
<tr class="editing">
    <td><input name="code" value="{gender.code}"/></td>
    <td><input name="description" value="{gender.description}"/></td>
    <td>
        <button hx-get="/genders/{gender.id}"
                hx-target="closest tr"
                hx-swap="outerHTML">Cancel</button>
        <button hx-post="/genders/{gender.id}/update"
                hx-include="closest tr"
                hx-target="closest tr"
                hx-swap="outerHTML">Save</button>
    </td>
</tr>
```

### 5.2 Inline Create Form

```html
<!-- Create Button (gender_create_button.html) -->
<button hx-get="/genders/create"
        hx-target="#gender-create-container"
        hx-swap="innerHTML">Add Gender</button>

<!-- Create Form (gender_create_form.html) -->
<div class="uk-card uk-card-body">
    <form hx-post="/genders/create"
          hx-target="#gender-create-container"
          hx-swap="innerHTML">
        <input name="code" placeholder="Code *" maxlength="1"/>
        <input name="description" placeholder="Description *"/>
        <button type="submit">Save</button>
        <button type="button"
                hx-get="/genders/create/cancel"
                hx-target="#gender-create-container">Cancel</button>
    </form>
</div>
```

### 5.3 OOB Table Refresh (Success)

```html
<!-- gender_success.html -->
<!-- Main swap: Replace form with Add button -->
<button hx-get="/genders/create"
        hx-target="#gender-create-container">Add Gender</button>

<div class="uk-alert uk-alert-success">{message}</div>

<!-- OOB swap: Refresh table body independently -->
<tbody id="gender-table-body" hx-swap-oob="true">
    {#for gender in genders}
    {#include partials/gender_row gender=gender /}
    {/for}
</tbody>
```

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
| UC-002-01-01: View Gender List | `GenderResource.list()`, `gender.html`, `gender_table.html`, `gender_row.html` |
| UC-002-02-01: Display Create Form | `GenderResource.createForm()`, `gender_create_form.html` |
| UC-002-02-02: Submit Create Form | `GenderResource.create()`, `gender_success.html` |
| UC-002-03-01: Display Edit Form | `GenderResource.editForm()`, `gender_row_edit.html` |
| UC-002-03-02: Submit Edit Form | `GenderResource.update()`, `gender_row.html` |
| UC-002-03-03: Cancel Edit | `GenderResource.getRow()`, `gender_row.html` |
| UC-002-04-01: Delete Gender | `GenderResource.delete()` |

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

*Document Version: 1.0*
*Last Updated: December 2025*
