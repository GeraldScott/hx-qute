# Technical Specification: Feature 003 - Person Management

This document describes the technical implementation requirements for the Person Management feature, providing CRUD operations for managing Person records with filtering and sorting capabilities.

---

## 1. Database Schema

### 1.1 Person Table

**Migration**: `V1.1.0__Create_person_table.sql`

```sql
CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    date_of_birth DATE,
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
CREATE INDEX idx_person_gender ON person(gender_id);
```

**Key Constraints:**
- `email`: Unique, not null, normalized to lowercase
- `first_name`, `last_name`: Optional, max 100 characters
- `gender_id`: Foreign key to gender table (optional)
- Audit fields track creation and modification metadata

---

## 2. Entity Design

### 2.1 Person Entity

**File**: `src/main/java/io/archton/scaffold/entity/Person.java`

```java
package io.archton.scaffold.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "person", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class Person extends PanacheEntity {

    @Column(name = "first_name", length = 100)
    public String firstName;

    @Column(name = "last_name", length = 100)
    public String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @Column(name = "phone", length = 50)
    public String phone;

    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

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
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email;
    }

    // Static finder methods
    public static Person findByEmail(String email) {
        return find("LOWER(email)", email.toLowerCase()).firstResult();
    }

    public static List<Person> listAllOrdered() {
        return list("ORDER BY lastName ASC, firstName ASC");
    }

    public static List<Person> findByNameContaining(String searchText) {
        String pattern = "%" + searchText.toLowerCase() + "%";
        return list("LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1 ORDER BY lastName ASC, firstName ASC", pattern);
    }

    public static List<Person> listOrderedBy(String field, String direction) {
        String order = "ASC".equalsIgnoreCase(direction) ? "ASC" : "DESC";
        String orderBy = switch (field) {
            case "firstName" -> "firstName " + order + ", lastName ASC";
            case "lastName" -> "lastName " + order + ", firstName ASC";
            case "email" -> "email " + order;
            default -> "lastName ASC, firstName ASC";
        };
        return list("ORDER BY " + orderBy);
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

**Pattern**: Follows ARCHITECTURE.md Section 4.3 with:
- Full page `Templates` class for direct navigation
- `Partials` class with `basePath = "partials"` for HTMX fragments
- Inline editing (no modals)
- Filter and sort persistence in session
- OOB swaps for table refresh after create

### 3.2 Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/persons` | `list()` | List persons (with filter/sort) |
| GET | `/persons/create` | `createForm()` | Display inline create form |
| GET | `/persons/create/cancel` | `createFormCancel()` | Return Add button |
| POST | `/persons/create` | `create()` | Submit create form |
| GET | `/persons/{id}` | `getRow()` | Get single row partial |
| GET | `/persons/{id}/edit` | `editForm()` | Display inline edit form |
| POST | `/persons/{id}/update` | `update()` | Submit edit form |
| DELETE | `/persons/{id}` | `delete()` | Delete person |

### 3.3 Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `filter` | String | Search text for firstName/lastName |
| `sortField` | String | Field to sort by (firstName, lastName) |
| `sortDir` | String | Sort direction (asc, desc) |
| `clear` | String | Set to "true" to clear filter |
| `clearSort` | String | Set to "true" to clear sort |

### 3.4 Session Storage Keys

| Key | Description |
|-----|-------------|
| `persons.filter` | Persisted filter text |
| `persons.sortField` | Persisted sort field |
| `persons.sortDir` | Persisted sort direction |

---

## 4. Template Structure

### 4.1 Full Page Template

**File**: `templates/PersonResource/persons.html`

```
{#include base}
- Page title: "Persons"
- Filter form (hx-get="/persons" hx-target="#persons-table-container")
- Sort panel (hx-get="/persons" hx-target="#persons-table-container")
- Create form container: #person-create-container
- Table container: #persons-table-container
{/include}
```

### 4.2 Partial Templates

**Directory**: `templates/partials/`

| File | Description | Target |
|------|-------------|--------|
| `persons_table.html` | Table with all rows | `#persons-table-container` |
| `person_row.html` | Single row (display mode) | `closest tr` |
| `person_row_edit.html` | Single row (edit mode) | `closest tr` |
| `person_create_form.html` | Inline create form | `#person-create-container` |
| `person_create_button.html` | Add button | `#person-create-container` |
| `person_success.html` | Success message + OOB table | `#person-create-container` |
| `person_error.html` | Error message | Context-dependent |

### 4.3 Template Parameters

**persons.html**:
- `title`: String
- `currentPage`: String ("persons")
- `userName`: String (nullable)
- `persons`: List<Person>
- `filterText`: String (nullable)
- `genderChoices`: List<Gender>
- `sortField`: String (nullable)
- `sortDir`: String (nullable)

**persons_table.html**:
- `persons`: List<Person>
- `filterText`: String (nullable)

**person_row.html**:
- `person`: Person

**person_row_edit.html**:
- `person`: Person
- `genderChoices`: List<Gender>
- `error`: String (nullable)

**person_create_form.html**:
- `genderChoices`: List<Gender>
- `error`: String (nullable)

**person_success.html**:
- `message`: String
- `persons`: List<Person>

---

## 5. HTMX Patterns

### 5.1 Filter Form

```html
<form hx-get="/persons"
      hx-target="#persons-table-container"
      hx-push-url="true"
      hx-sync="this:replace">
    <input type="search" name="filter" value="{filterText ?: ''}"
           hx-get="/persons"
           hx-trigger="input changed delay:300ms, search"
           hx-target="#persons-table-container"
           hx-sync="closest form:abort"/>
    <button type="submit">Filter</button>
    <a hx-get="/persons?clear=true"
       hx-target="#persons-table-container">Clear</a>
</form>
```

### 5.2 Sort Panel

```html
<form hx-get="/persons"
      hx-target="#persons-table-container"
      hx-push-url="true">
    <select name="sortField">
        <option value="">Sort by...</option>
        <option value="firstName" {#if sortField == 'firstName'}selected{/if}>First Name</option>
        <option value="lastName" {#if sortField == 'lastName'}selected{/if}>Last Name</option>
    </select>
    <select name="sortDir">
        <option value="asc" {#if sortDir == 'asc'}selected{/if}>Ascending</option>
        <option value="desc" {#if sortDir == 'desc'}selected{/if}>Descending</option>
    </select>
    <button type="submit">Sort</button>
    <a hx-get="/persons?clearSort=true"
       hx-target="#persons-table-container">Clear</a>
</form>
```

### 5.3 Inline Row Editing

```html
<!-- Display Row (person_row.html) -->
<tr>
    <td>{person.firstName ?: ''}</td>
    <td>{person.lastName ?: ''}</td>
    <td>{person.email}</td>
    <td>{person.phone ?: ''}</td>
    <td>{person.dateOfBirth}</td>
    <td>{person.gender.description ?: ''}</td>
    <td>
        <button hx-get="/persons/{person.id}/edit"
                hx-target="closest tr"
                hx-swap="outerHTML">Edit</button>
        <button hx-delete="/persons/{person.id}"
                hx-confirm="Are you sure?"
                hx-target="closest tr"
                hx-swap="delete swap:300ms">Delete</button>
    </td>
</tr>

<!-- Edit Row (person_row_edit.html) -->
<tr class="editing">
    <td><input name="firstName" value="{person.firstName ?: ''}"/></td>
    <td><input name="lastName" value="{person.lastName ?: ''}"/></td>
    <td><input name="email" type="email" value="{person.email}"/></td>
    <td><input name="phone" value="{person.phone ?: ''}"/></td>
    <td><input name="dateOfBirth" type="date" value="{person.dateOfBirth}"/></td>
    <td>
        <select name="genderId">
            <option value="">-- Select --</option>
            {#for gender in genderChoices}
            <option value="{gender.id}" {#if person.gender?? && person.gender.id == gender.id}selected{/if}>
                {gender.description}
            </option>
            {/for}
        </select>
    </td>
    <td>
        <button hx-get="/persons/{person.id}"
                hx-target="closest tr"
                hx-swap="outerHTML">Cancel</button>
        <button hx-post="/persons/{person.id}/update"
                hx-include="closest tr"
                hx-target="closest tr"
                hx-swap="outerHTML">Save</button>
    </td>
</tr>
```

### 5.4 Attribute Inheritance

```html
<!-- Table body with inherited attributes -->
<tbody id="persons-table-body" hx-target="closest tr" hx-swap="outerHTML">
    {#for person in persons}
    {#include partials/person_row person=person /}
    {/for}
</tbody>
```

---

## 6. Validation Rules

### 6.1 Create Validation

| Field | Rule | Error Message |
|-------|------|---------------|
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
        <span>Persons</span>
    </a>
</li>
```

---

## 9. Session-Based Filter/Sort Persistence

### 9.1 Implementation Pattern

```java
@Inject
io.vertx.ext.web.RoutingContext routingContext;

private static final String FILTER_SESSION_KEY = "persons.filter";
private static final String SORT_FIELD_KEY = "persons.sortField";
private static final String SORT_DIR_KEY = "persons.sortDir";

@GET
public TemplateInstance list(
        @QueryParam("filter") String filterText,
        @QueryParam("sortField") String sortField,
        @QueryParam("sortDir") String sortDir,
        @QueryParam("clear") String clear,
        @QueryParam("clearSort") String clearSort) {

    // Handle clear actions
    if ("true".equals(clear)) {
        routingContext.session().remove(FILTER_SESSION_KEY);
        filterText = null;
    }
    if ("true".equals(clearSort)) {
        routingContext.session().remove(SORT_FIELD_KEY);
        routingContext.session().remove(SORT_DIR_KEY);
        sortField = null;
        sortDir = null;
    }

    // Persist or restore from session
    if (filterText == null) {
        filterText = routingContext.session().get(FILTER_SESSION_KEY);
    } else {
        routingContext.session().put(FILTER_SESSION_KEY, filterText.trim());
    }

    // ... similar for sort fields
}
```

---

## 10. Traceability

| Use Case | Implementation Component |
|----------|-------------------------|
| UC-003-01-01: View Persons List | `PersonResource.list()`, `persons.html`, `persons_table.html`, `person_row.html` |
| UC-003-02-01: Display Create Form | `PersonResource.createForm()`, `person_create_form.html` |
| UC-003-02-02: Submit Create Form | `PersonResource.create()`, `person_success.html` |
| UC-003-03-01: Display Edit Form | `PersonResource.editForm()`, `person_row_edit.html` |
| UC-003-03-02: Submit Edit Form | `PersonResource.update()`, `person_row.html` |
| UC-003-03-03: Cancel Edit | `PersonResource.getRow()`, `person_row.html` |
| UC-003-04-01: Delete Person | `PersonResource.delete()` |
| UC-003-05-01: Apply Filter | `PersonResource.list()`, filter form in `persons.html` |
| UC-003-05-02: Clear Filter | `PersonResource.list()` with `clear=true` |
| UC-003-06-01: Apply Sort | `PersonResource.list()`, sort panel in `persons.html` |
| UC-003-06-02: Clear Sort | `PersonResource.list()` with `clearSort=true` |

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

### 11.2 Entity Dependencies

- `Gender` entity must exist (Feature 002)

### 11.3 Frontend Dependencies (CDN)

- HTMX 2.0.8
- UIkit 3.25

---

*Document Version: 1.0*
*Last Updated: December 2025*
