# Technical Specification: Feature 003 - Person Management

This document describes the technical implementation requirements for the Person Management feature, providing CRUD operations for managing Person records with filtering and sorting capabilities.

---

## 1. Database Schema

### 1.1 Person Table

**Migration**: `V1.3.0__Create_person_table.sql`

```sql
CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    date_of_birth DATE,
    title_id BIGINT REFERENCES title(id),
    gender_id BIGINT REFERENCES gender(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_person_email UNIQUE (email)
);

CREATE INDEX idx_person_email ON person(email);
CREATE INDEX idx_person_last_name ON person(last_name);
CREATE INDEX idx_person_first_name ON person(first_name);
CREATE INDEX idx_person_title ON person(title_id);
CREATE INDEX idx_person_gender ON person(gender_id);
```

**Key Constraints:**
- `first_name`: Required, max 100 characters
- `last_name`: Required, max 100 characters
- `email`: Unique, not null, normalized to lowercase
- `title_id`: Foreign key to title table (optional)
- `gender_id`: Foreign key to gender table (optional)
- Audit fields track creation and modification metadata

---

## 2. Entity Design

### 2.1 Person Entity

**File**: `src/main/java/io/archton/scaffold/entity/Person.java`

```java
package io.archton.scaffold.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "person", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class Person extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    public String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    public String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @Column(name = "phone", length = 50)
    public String phone;

    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id")
    public Title title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id")
    public Gender gender;

    @Column(name = "created_at")
    public OffsetDateTime createdAt;

    @Column(name = "updated_at")
    public OffsetDateTime updatedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    // Display name helper
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (title != null) {
            sb.append(title.code).append(" ");
        }
        sb.append(firstName).append(" ").append(lastName);
        return sb.toString().trim();
    }

    // Static finder methods
    public static Person findByEmail(String email) {
        return find("LOWER(email)", email.toLowerCase().trim()).firstResult();
    }

    public static List<Person> listAllOrdered() {
        return list("ORDER BY lastName ASC, firstName ASC");
    }

    public static List<Person> findByFilter(String filterText, String sortField, String sortDir) {
        StringBuilder query = new StringBuilder();
        String orderBy = buildOrderBy(sortField, sortDir);

        if (filterText != null && !filterText.isBlank()) {
            String pattern = "%" + filterText.toLowerCase().trim() + "%";
            return list(
                "LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1 OR LOWER(email) LIKE ?1 " + orderBy,
                pattern
            );
        }
        return list(orderBy.replace("ORDER BY ", ""));
    }

    private static String buildOrderBy(String sortField, String sortDir) {
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        String orderBy = switch (sortField != null ? sortField : "") {
            case "firstName" -> "firstName " + direction + ", lastName ASC";
            case "lastName" -> "lastName " + direction + ", firstName ASC";
            case "email" -> "email " + direction;
            default -> "lastName ASC, firstName ASC";
        };
        return "ORDER BY " + orderBy;
    }

    // Lifecycle callbacks
    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }
}
```

---

## 3. Resource Layer

### 3.1 PersonResource

**File**: `src/main/java/io/archton/scaffold/router/PersonResource.java`

**Security**: `@RolesAllowed({"user", "admin"})` - All authenticated users

**Pattern**: Follows ARCHITECTURE.md Section 7.5 Modal-Based CRUD with:
- Single `@CheckedTemplate` class with fragment methods using `$` separator
- Modal dialogs for create, edit, and delete operations
- Qute fragments within `person.html` template
- Query parameter-based filtering (no session storage)
- OOB swaps for table refresh after mutations

### 3.2 Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/persons` | `list()` | List persons (with filter/sort via query params) |
| GET | `/persons/create` | `createForm()` | Display create modal content |
| POST | `/persons` | `create()` | Submit create form |
| GET | `/persons/{id}/edit` | `editForm()` | Display edit modal content |
| PUT | `/persons/{id}` | `update()` | Submit edit form |
| GET | `/persons/{id}/delete` | `deleteConfirm()` | Display delete confirmation modal |
| DELETE | `/persons/{id}` | `delete()` | Delete person |

### 3.3 Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `filter` | String | Search text for firstName/lastName/email |
| `sortField` | String | Field to sort by (firstName, lastName, email) |
| `sortDir` | String | Sort direction (asc, desc) |

### 3.4 Template Methods

```java
@CheckedTemplate
public static class Templates {
    // Full page
    public static native TemplateInstance person(
        String title,
        String currentPage,
        String userName,
        List<Person> persons,
        List<Title> titleChoices,
        List<Gender> genderChoices,
        String filterText,
        String sortField,
        String sortDir
    );

    // Fragments (note $ separator matching fragment id)
    public static native TemplateInstance person$table(
        List<Person> persons,
        String filterText
    );
    public static native TemplateInstance person$modal_create(
        Person person,
        List<Title> titleChoices,
        List<Gender> genderChoices,
        String error
    );
    public static native TemplateInstance person$modal_edit(
        Person person,
        List<Title> titleChoices,
        List<Gender> genderChoices,
        String error
    );
    public static native TemplateInstance person$modal_delete(
        Person person,
        String error
    );
    public static native TemplateInstance person$modal_success(
        String message,
        List<Person> persons,
        String filterText
    );
    public static native TemplateInstance person$modal_success_row(
        String message,
        Person person
    );
    public static native TemplateInstance person$modal_delete_success(
        Long deletedId
    );
}
```

---

## 4. Template Structure

### 4.1 Template File

**File**: `templates/PersonResource/person.html`

The template contains:
1. **Main page content** with filter bar, Add button, table container, and modal shell
2. **Fragment: `table`** - Table with all person rows
3. **Fragment: `modal_create`** - Create form modal content
4. **Fragment: `modal_edit`** - Edit form modal content
5. **Fragment: `modal_delete`** - Delete confirmation modal content
6. **Fragment: `modal_success`** - Success response with OOB table refresh
7. **Fragment: `modal_success_row`** - Success response with OOB single row update
8. **Fragment: `modal_delete_success`** - Success response with OOB row removal

### 4.2 Page Layout

```
┌─────────────────────────────────────────────────────────────┐
│ Person Management                                            │
├─────────────────────────────────────────────────────────────┤
│ Filter Bar (above table)                                     │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ [Search Input] [Sort Field ▼] [Sort Dir ▼] [Filter] [Clear] │
│ └─────────────────────────────────────────────────────────┘ │
│                                                [+ Add Button] │
├─────────────────────────────────────────────────────────────┤
│ Table Container (#person-table-container)                    │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Name | Email | Phone | DOB | Title | Gender | Actions   │ │
│ │ Row 1: [Data] [Edit] [Delete]                           │ │
│ │ Row 2: [Data] [Edit] [Delete]                           │ │
│ └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│ Static Modal Shell (#person-modal)                           │
│ ← Content loaded dynamically via HTMX                        │
└─────────────────────────────────────────────────────────────┘
```

### 4.3 Filter Bar HTML

```html
<!-- Filter Bar - Above Table -->
<form class="uk-grid-small uk-flex-middle uk-margin-bottom" uk-grid
      hx-get="/persons"
      hx-target="#person-table-container"
      hx-push-url="true">

    <!-- Search Input -->
    <div class="uk-width-expand@s uk-width-1-1">
        <input class="uk-input" type="search" name="filter"
               value="{filterText ?: ''}"
               placeholder="Search name or email..."
               hx-get="/persons"
               hx-trigger="input changed delay:300ms, search"
               hx-target="#person-table-container"
               hx-include="closest form" />
    </div>

    <!-- Sort Field -->
    <div class="uk-width-auto@s">
        <select class="uk-select" name="sortField">
            <option value="">Sort by...</option>
            <option value="lastName" {#if sortField == 'lastName'}selected{/if}>Last Name</option>
            <option value="firstName" {#if sortField == 'firstName'}selected{/if}>First Name</option>
            <option value="email" {#if sortField == 'email'}selected{/if}>Email</option>
        </select>
    </div>

    <!-- Sort Direction -->
    <div class="uk-width-auto@s">
        <select class="uk-select" name="sortDir">
            <option value="asc" {#if sortDir != 'desc'}selected{/if}>Ascending</option>
            <option value="desc" {#if sortDir == 'desc'}selected{/if}>Descending</option>
        </select>
    </div>

    <!-- Buttons -->
    <div class="uk-width-auto@s">
        <button type="submit" class="uk-button uk-button-primary">Filter</button>
        <a href="/persons" class="uk-button uk-button-default">Clear</a>
    </div>
</form>
```

---

## 5. HTMX Patterns

### 5.1 Add Button (Opens Modal)

```html
<button class="uk-button uk-button-primary uk-margin-bottom"
        hx-get="/persons/create"
        hx-target="#person-modal-body"
        hx-on::after-request="UIkit.modal('#person-modal').show()">
    <span uk-icon="plus"></span> Add
</button>
```

### 5.2 Edit Button (Opens Modal)

```html
<button class="uk-button uk-button-small uk-button-primary"
        hx-get="/persons/{p.id}/edit"
        hx-target="#person-modal-body"
        hx-on::after-request="UIkit.modal('#person-modal').show()">
    Edit
</button>
```

### 5.3 Delete Button (Opens Confirmation Modal)

```html
<button class="uk-button uk-button-small uk-button-danger"
        hx-get="/persons/{p.id}/delete"
        hx-target="#person-modal-body"
        hx-on::after-request="UIkit.modal('#person-modal').show()">
    Delete
</button>
```

### 5.4 Modal Success Response (Close + OOB Update)

```html
{#fragment id=modal_success rendered=false}
{@String message}
{@java.util.List<io.archton.scaffold.entity.Person> persons}
{@String filterText}

<!-- Close modal when loaded -->
<div hx-on::load="UIkit.modal('#person-modal').hide()"></div>

<!-- OOB swap: Replace table container content -->
<div id="person-table-container" hx-swap-oob="innerHTML">
    {#include $table persons=persons filterText=filterText /}
</div>
{/fragment}
```

---

## 6. Validation Rules

### 6.1 Create Validation

| Field | Rule | Error Message |
|-------|------|---------------|
| firstName | Required | "First name is required." |
| lastName | Required | "Last name is required." |
| email | Required | "Email is required." |
| email | Valid format | "Invalid email format." |
| email | Unique | "Email already registered." |

### 6.2 Edit Validation

Same as create, but email uniqueness check excludes current record.

### 6.3 Email Validation Regex

```java
private boolean isValidEmail(String email) {
    return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
}
```

---

## 7. Security Configuration

### 7.1 Route Protection

**application.properties**:
```properties
quarkus.http.auth.permission.authenticated.paths=/persons,/persons/*
quarkus.http.auth.permission.authenticated.policy=authenticated
```

### 7.2 Resource Annotation

```java
@Path("/persons")
@RolesAllowed({"user", "admin"})
public class PersonResource { ... }
```

---

## 8. Navigation

### 8.1 Sidebar Menu

**Location**: Main navigation (not under submenu)

```html
<li class="{#if currentPage == 'persons'}uk-active{/if}">
    <a href="/persons">
        <span uk-icon="icon: users"></span>
        <span class="uk-margin-small-left">Persons</span>
    </a>
</li>
```

---

## 9. Traceability

| Use Case | Implementation Component |
|----------|-------------------------|
| UC-003-01-01: View Persons List | `PersonResource.list()`, `person.html`, `$table` fragment |
| UC-003-02-01: Display Create Form | `PersonResource.createForm()`, `$modal_create` fragment |
| UC-003-02-02: Submit Create Form | `PersonResource.create()`, `$modal_success` fragment |
| UC-003-03-01: Display Edit Form | `PersonResource.editForm()`, `$modal_edit` fragment |
| UC-003-03-02: Submit Edit Form | `PersonResource.update()`, `$modal_success_row` fragment |
| UC-003-03-03: Cancel Edit | Cancel button with `uk-modal-close` class |
| UC-003-04-01: Delete Person | `PersonResource.delete()`, `$modal_delete`, `$modal_delete_success` fragments |
| UC-003-05-01: Apply Filter | `PersonResource.list()` with `filter` query param |
| UC-003-05-02: Clear Filter | Link to `/persons` (clears all params) |
| UC-003-06-01: Apply Sort | `PersonResource.list()` with `sortField`, `sortDir` query params |
| UC-003-06-02: Clear Sort | Link to `/persons` (clears all params) |

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

### 10.2 Entity Dependencies

- `Gender` entity must exist (Feature 002) ✅
- `Title` entity must exist (Feature 002) ✅

### 10.3 Frontend Dependencies (CDN)

- HTMX 2.0.8
- UIkit 3.25

---

*Document Version: 2.0*
*Last Updated: December 2025*
