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
- Inline editing (no modals)
- OOB swaps for table refresh after create

### 3.2 Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/genders` | `list()` | List all genders (full page or table fragment) |
| GET | `/genders/create` | `createForm()` | Display inline create form |
| GET | `/genders/create/cancel` | `createFormCancel()` | Return Add button (cancel create) |
| POST | `/genders` | `create()` | Submit create form |
| GET | `/genders/{id}` | `getRow()` | Get single row fragment (for cancel edit) |
| GET | `/genders/{id}/edit` | `editForm()` | Display inline edit form in row |
| PUT | `/genders/{id}` | `update()` | Submit edit form |
| DELETE | `/genders/{id}` | `delete()` | Delete gender |

### 3.3 Request/Response Patterns

**List (GET /genders)**:
- Full page: Returns `Templates.gender(...)`
- HTMX request: Returns `Templates.gender$table(...)` fragment
- Detection: Check `HX-Request` header

**Create Submit (POST /genders)**:
- Success: Returns `Templates.gender$success(message, genders)` with OOB table refresh
- Validation error: Returns `Templates.gender$create_form(error)`

**Edit Submit (PUT /genders/{id})**:
- Success: Returns `Templates.gender$row(gender)`
- Validation error: Returns `Templates.gender$row_edit(gender, error)`

**Delete (DELETE /genders/{id})**:
- Success: Returns empty response (row removed via `hx-swap="delete"`)
- In-use error: Returns `Templates.gender$row_edit(gender, error)`

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

```html
{@java.util.List<io.archton.scaffold.entity.Gender> genders}
{#include base}
{#title}Gender Management{/title}

<div id="gender-create-container">
    {#include $create_button /}
</div>

<div id="gender-table-container">
    {#include $table /}
</div>

{/include}

{!-- Table fragment: rows are INLINED, not included from $row --}
{#fragment id=table}
{#if genders.isEmpty()}
<p class="uk-text-muted">No genders found.</p>
{#else}
<table class="uk-table">
    <thead>...</thead>
    <tbody id="gender-table-body">
        {#for g in genders}
        <tr id="gender-row-{g.id}">
            <td>{g.code}</td>
            <td>{g.description}</td>
        </tr>
        {/for}
    </tbody>
</table>
{/if}
{/fragment}

{!-- Standalone row fragment for direct calls (edit/cancel operations) --}
{!-- Must declare parameter explicitly for type-safe validation --}
{#fragment id=row}
{@io.archton.scaffold.entity.Gender gender}
<tr id="gender-row-{gender.id}">
    <td>{gender.code}</td>
    <td>{gender.description}</td>
</tr>
{/fragment}

{#fragment id=row_edit}
{@io.archton.scaffold.entity.Gender gender}
{@String error}
<tr class="editing">...</tr>
{/fragment}

{#fragment id=create_form}
{@String error}
<div class="uk-card uk-card-body">...</div>
{/fragment}

{#fragment id=create_button}
<button hx-get="/genders/create">Add Gender</button>
{/fragment}

{#fragment id=success}
{@String message}
{@java.util.List<io.archton.scaffold.entity.Gender> genders}
<!-- Primary swap content + OOB table refresh -->
{/fragment}
```

### 4.2 Type-Safe Fragment Methods

```java
@CheckedTemplate
public static class Templates {
    // Full page
    public static native TemplateInstance gender(List<Gender> genders);

    // Fragments (type-safe, compile-time validated)
    public static native TemplateInstance gender$table(List<Gender> genders);
    public static native TemplateInstance gender$row(Gender gender);
    public static native TemplateInstance gender$row_edit(Gender gender, String error);
    public static native TemplateInstance gender$create_form(String error);
    public static native TemplateInstance gender$create_button();
    public static native TemplateInstance gender$success(String message, List<Gender> genders);
}
```

### 4.3 Fragment Parameters

| Fragment | Parameters | Description |
|----------|------------|-------------|
| `gender$table` | `genders: List<Gender>` | Table with all rows |
| `gender$row` | `gender: Gender` | Single row (display mode) |
| `gender$row_edit` | `gender: Gender`, `error: String` | Single row (edit mode) |
| `gender$create_form` | `error: String` | Inline create form |
| `gender$create_button` | (none) | Add button |
| `gender$success` | `message: String`, `genders: List<Gender>` | Success + OOB table |

---

## 5. HTMX Patterns

### 5.1 Inline Row Editing

```html
<!-- Display Row (row fragment) -->
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

<!-- Edit Row (row_edit fragment) -->
<tr class="editing">
    <td><input name="code" value="{gender.code}"/></td>
    <td><input name="description" value="{gender.description}"/></td>
    <td>
        <button hx-get="/genders/{gender.id}"
                hx-target="closest tr"
                hx-swap="outerHTML">Cancel</button>
        <button hx-put="/genders/{gender.id}"
                hx-include="closest tr"
                hx-target="closest tr"
                hx-swap="outerHTML">Save</button>
    </td>
</tr>
```

### 5.2 Inline Create Form

```html
<!-- Create Button (create_button fragment) -->
<button hx-get="/genders/create"
        hx-target="#gender-create-container"
        hx-swap="innerHTML">Add Gender</button>

<!-- Create Form (create_form fragment) -->
<div class="uk-card uk-card-body">
    <form hx-post="/genders"
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
<!-- success fragment -->
<!-- Main swap: Replace form with Add button + success message -->
<button hx-get="/genders/create"
        hx-target="#gender-create-container">Add Gender</button>

<div class="uk-alert uk-alert-success">{message}</div>

<!-- OOB swap: Refresh table body independently -->
<!-- Note: <tbody> wrapped in <template> for HTML spec compliance -->
<!-- Note: Rows are INLINED, not included via $row fragment -->
<template>
    <tbody id="gender-table-body" hx-swap-oob="true">
        {#for g in genders}
        <tr id="gender-row-{g.id}">
            <td>{g.code}</td>
            <td>{g.description}</td>
        </tr>
        {/for}
    </tbody>
</template>
```

**Why `<template>` wrapper?** Per HTMX documentation, table elements like `<tbody>`, `<tr>`, `<td>` cannot stand alone in HTML responses. Wrapping in `<template>` ensures proper parsing while HTMX still processes the OOB swap correctly.

**Why inline rows instead of `{#include $row /}`?** With `@CheckedTemplate`, loop variables cannot be passed to included fragments. The `$row` fragment is only used for standalone calls (e.g., cancel edit returning a single row).

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
| UC-002-02-01: Display Create Form | `GenderResource.createForm()`, `gender$create_form` fragment |
| UC-002-02-02: Submit Create Form | `GenderResource.create()`, `gender$success` fragment |
| UC-002-03-01: Display Edit Form | `GenderResource.editForm()`, `gender$row_edit` fragment |
| UC-002-03-02: Submit Edit Form | `GenderResource.update()`, `gender$row` fragment |
| UC-002-03-03: Cancel Edit | `GenderResource.getRow()`, `gender$row` fragment |
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

*Document Version: 1.1*
*Last Updated: December 2025*
*Changes: Added PostgreSQL BIGSERIAL/IDENTITY requirement, PanacheEntityBase usage, and type-safe template fragment limitations*
