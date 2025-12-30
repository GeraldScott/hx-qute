# HX Qute Architecture Guide

A comprehensive technical reference for developing features in this Quarkus + HTMX + Qute application.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Technology Stack](#2-technology-stack)
3. [Project Structure](#3-project-structure)
4. [Database Layer](#4-database-layer)
5. [Entity Layer](#5-entity-layer)
6. [Resource Layer](#6-resource-layer)
7. [Template System](#7-template-system)
8. [HTMX Integration](#8-htmx-integration)
9. [Security Architecture](#9-security-architecture)
10. [Configuration Reference](#10-configuration-reference)
11. [Testing Patterns](#11-testing-patterns)
12. [Development Workflow](#12-development-workflow)

---

## 1. Overview

HX Qute is a reference implementation demonstrating modern server-side web development using the hypermedia-driven application (HDA) pattern. It combines Quarkus's reactive capabilities with HTMX's HTML-over-the-wire approach, eliminating the need for complex JavaScript frameworks while delivering responsive, interactive user experiences.

### Design Principles

| Principle | Implementation |
|-----------|----------------|
| Server-Side Rendering | All HTML generated on the server via Qute templates |
| Hypermedia-Driven | HTMX handles partial page updates without full reloads |
| Type Safety | `@CheckedTemplate` ensures compile-time template validation |
| Fragment-Based UI | Qute fragments enable reusable, modal-based CRUD patterns |
| Security by Default | Form authentication with BCrypt password hashing |

---

## 2. Technology Stack

### 2.1 Core Framework

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Quarkus | 3.30.3 |
| Language | Java | 21 |
| Build Tool | Maven | 3.x |

### 2.2 Backend Dependencies

| Purpose | Extension | Description |
|---------|-----------|-------------|
| REST API | `quarkus-rest` | RESTEasy Reactive endpoints |
| Templating | `quarkus-rest-qute` | Type-safe Qute template integration |
| ORM | `quarkus-hibernate-orm-panache` | Active Record pattern for entities |
| Database | `quarkus-jdbc-postgresql` | PostgreSQL JDBC driver |
| Migrations | `quarkus-flyway` | Versioned schema migrations |
| Security | `quarkus-security-jpa` | JPA-based identity provider with BCrypt |
| Validation | `quarkus-hibernate-validator` | Bean validation (JSR-380) |
| CDI | `quarkus-arc` | Dependency injection |
| Testing | `quarkus-junit5` | JUnit 5 integration |

### 2.3 Frontend Stack (CDN-Based)

| Purpose | Technology | Version | CDN |
|---------|------------|---------|-----|
| Dynamic UI | HTMX | 2.0.8 | jsdelivr.net |
| CSS Framework | UIkit | 3.25.4 | jsdelivr.net |
| Custom Styles | CSS | - | Local `/style.css` |

### 2.4 Database

| Component | Technology |
|-----------|------------|
| RDBMS | PostgreSQL 17 |
| Migrations | Flyway |
| ORM | Hibernate with Panache |

---

## 3. Project Structure

```
src/main/
├── java/io/archton/scaffold/
│   ├── entity/              # JPA entities with Panache
│   │   ├── Gender.java
│   │   └── UserLogin.java
│   ├── error/               # Exception handling
│   │   └── GlobalExceptionMapper.java
│   ├── router/              # REST resource classes
│   │   ├── AuthResource.java
│   │   ├── GenderResource.java
│   │   └── IndexResource.java
│   └── service/             # Business logic services
│       └── PasswordValidator.java
├── resources/
│   ├── application.properties
│   ├── db/migration/        # Flyway SQL migrations
│   │   ├── V1.0.0__Create_gender_table.sql
│   │   ├── V1.0.1__Insert_gender_data.sql
│   │   ├── V1.2.0__Create_user_login_table.sql
│   │   └── V1.2.1__Insert_admin_user.sql
│   ├── templates/
│   │   ├── base.html        # Master layout template
│   │   ├── error.html       # Error page template
│   │   ├── AuthResource/    # Auth page templates
│   │   ├── GenderResource/  # Gender page templates
│   │   └── IndexResource/   # Index page templates
│   └── META-INF/resources/  # Static assets
│       ├── style.css
│       └── img/
└── test/
    └── java/io/archton/scaffold/
        └── router/          # REST endpoint tests
```

---

## 4. Database Layer

### 4.1 Flyway Migrations

Flyway manages all schema changes through versioned SQL scripts.

**Location**: `src/main/resources/db/migration/`

**Naming Convention**: `V{major}.{minor}.{patch}__{Description}.sql`

| Convention | Example |
|------------|---------|
| Initial schema | `V1.0.0__Create_gender_table.sql` |
| New feature | `V1.1.0__Create_person_table.sql` |
| Data seed | `V1.2.1__Insert_admin_user.sql` |
| Bug fix | `V1.2.2__Fix_constraint_name.sql` |

**Configuration**:

```properties
quarkus.flyway.migrate-at-start=true
quarkus.hibernate-orm.schema-management.strategy=none
```

### 4.2 Migration Best Practices

1. **One change per migration**: Each script should represent a single, atomic schema change
2. **Never modify applied migrations**: Create new migrations for changes
3. **Use descriptive names**: Names should clearly indicate the change
4. **Include rollback comments**: Document how to reverse the change
5. **Test migrations**: Verify against a clean database before committing

### 4.3 PostgreSQL-Specific Patterns

**Identity Columns** (use BIGSERIAL for auto-increment):

```sql
CREATE TABLE entity_name (
    id BIGSERIAL PRIMARY KEY,
    -- columns...
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
```

**Important**: Use `BIGSERIAL` with `GenerationType.IDENTITY` in JPA. Do NOT use sequences with Panache entities.

---

## 5. Entity Layer

### 5.1 PanacheEntityBase Pattern

All entities extend `PanacheEntityBase` (not `PanacheEntity`) to use `BIGSERIAL` primary keys:

```java
@Entity
@Table(name = "entity_name")
public class EntityName extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    // Public fields (Panache convention)
    @Column(nullable = false, length = 100)
    public String name;

    // Audit fields (use Instant for UTC timestamps)
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    // Static finder methods
    public static EntityName findByName(String name) {
        return find("name", name).firstResult();
    }

    public static List<EntityName> listAllOrdered() {
        return list("ORDER BY name");
    }

    // Lifecycle callbacks
    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

### 5.2 UserLogin Entity (Security)

The authentication entity uses Quarkus Security JPA annotations:

```java
@Entity
@Table(name = "user_login")
@UserDefinition
public class UserLogin extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @Username
    @Column(nullable = false, unique = true)
    public String email;
    
    @Password(value = PasswordType.MCF)
    @Column(nullable = false)
    public String password;
    
    @Roles
    @Column(nullable = false)
    public String role;
    
    // Factory method with BCrypt hashing
    public static UserLogin create(String email, String password, String role) {
        UserLogin user = new UserLogin();
        user.email = email.toLowerCase().trim();
        user.password = BcryptUtil.bcryptHash(password, 12);
        user.role = role;
        return user;
    }
    
    public static UserLogin findByEmail(String email) {
        return find("LOWER(email)", email.toLowerCase().trim()).firstResult();
    }
    
    public static boolean emailExists(String email) {
        return count("LOWER(email)", email.toLowerCase().trim()) > 0;
    }
}
```

### 5.3 Entity Relationships

```java
// Many-to-One relationship
@ManyToOne
@JoinColumn(name = "gender_id")
public Gender gender;

// One-to-Many relationship (if needed)
@OneToMany(mappedBy = "gender")
public List<Person> persons;
```

---

## 6. Resource Layer

### 6.1 Resource Pattern

Resources serve as controllers, handling HTTP requests and returning templates. This application uses **Qute fragments** (preferred pattern) rather than separate partial template files:

```java
@Path("/entities")
@RolesAllowed({"user", "admin"})
public class EntityResource {

    @Inject
    SecurityIdentity securityIdentity;

    // Type-safe templates including fragments (uses $ separator)
    @CheckedTemplate
    public static class Templates {
        // Full page template
        public static native TemplateInstance entity(
            String title,
            String currentPage,
            String userName,
            List<Entity> entities
        );

        // Fragment methods (note $ separator matching fragment id)
        public static native TemplateInstance entity$table(List<Entity> entities);
        public static native TemplateInstance entity$modal_create(Entity entity, String error);
        public static native TemplateInstance entity$modal_edit(Entity entity, String error);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
        List<Entity> entities = Entity.listAllOrdered();

        // HTMX request: return only the table fragment
        if ("true".equals(hxRequest)) {
            return Templates.entity$table(entities);
        }

        // Full page request
        String userName = getCurrentUsername();
        return Templates.entity("Entities", "entity", userName, entities);
    }

    private String getCurrentUsername() {
        return securityIdentity.isAnonymous()
            ? null
            : securityIdentity.getPrincipal().getName();
    }
}
```

**Key Points:**
- Single `@CheckedTemplate` class contains both full page and fragment methods
- Fragment methods use `$` separator: `entity$table` accesses `{#fragment id=table}` in `entity.html`
- Use `@HeaderParam("HX-Request")` for cleaner HTMX detection (alternative to `@Context HttpHeaders`)

### 6.2 Standard CRUD Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/entities` | `list()` | List with optional filter |
| GET | `/entities/create` | `createForm()` | Show create form |
| POST | `/entities/create` | `create()` | Submit create form |
| GET | `/entities/create/cancel` | `createCancel()` | Cancel create, show button |
| GET | `/entities/{id}` | `getRow()` | Get single row partial |
| GET | `/entities/{id}/edit` | `editForm()` | Get row in edit mode |
| POST | `/entities/{id}/update` | `update()` | Submit edit form |
| DELETE | `/entities/{id}` | `delete()` | Delete entity |

### 6.3 Template Parameter Conventions

All templates receive these standard parameters:

| Parameter | Type | Description |
|-----------|------|-------------|
| `title` | String | Page title for `<title>` tag |
| `currentPage` | String | Identifier for active navigation |
| `userName` | String | Current user's email (null if anonymous) |

### 6.4 Global Exception Handling

The `GlobalExceptionMapper` provides consistent error handling for all requests:

```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    @Location("error.html")
    Template errorTemplate;

    @ConfigProperty(name = "quarkus.profile")
    String profile;

    @Context
    UriInfo uriInfo;

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        // Determine status code from exception type
        int status;
        String message;

        if (exception instanceof WebApplicationException wae) {
            status = wae.getResponse().getStatus();
            message = wae.getMessage();
        } else {
            status = 500;
            message = "An unexpected error occurred.";
        }

        // Generate unique reference ID for troubleshooting
        String referenceId = UUID.randomUUID().toString();

        // Log with reference ID
        LOG.errorf(exception, "Error %d [%s]: %s", status, referenceId, exception.getMessage());

        // Return HTML for browser, JSON for API
        if (acceptsHtml(headers)) {
            boolean devMode = "dev".equals(profile);
            String html = errorTemplate
                .data("status", status)
                .data("message", message)
                .data("referenceId", referenceId)
                .data("devMode", devMode)
                .data("stackTrace", devMode ? getStackTrace(exception) : null)
                .render();
            return Response.status(status).entity(html).type(MediaType.TEXT_HTML).build();
        }

        return Response.status(status)
            .entity(String.format("{\"error\":\"%s\",\"referenceId\":\"%s\"}", message, referenceId))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
```

**Key Features:**
- **Content negotiation**: Returns HTML for browsers, JSON for API clients
- **Reference IDs**: Each error gets a UUID for log correlation
- **Dev mode stack traces**: Full traces shown only in development
- **Consistent logging**: All errors logged with path and reference ID

---

## 7. Template System

### 7.1 Template Locations

Templates follow a convention based on the resource class name:

| Resource Class | Template Directory |
|----------------|-------------------|
| `IndexResource` | `templates/IndexResource/` |
| `GenderResource` | `templates/GenderResource/` |
| `PersonResource` | `templates/PersonResource/` |
| `AuthResource` | `templates/AuthResource/` |
| Partials (shared) | `templates/partials/` |

### 7.2 Base Template (Layout)

The base template (`templates/base.html`) provides the master layout with a responsive sidebar navigation:

```html
{@String title}
{@String currentPage}
{@String userName}
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>{title??}</title>

    <!-- UIkit CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/uikit@3.25.4/dist/css/uikit.min.css"/>

    <!-- UIkit JS -->
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.25.4/dist/js/uikit.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.25.4/dist/js/uikit-icons.min.js"></script>

    <!-- HTMX -->
    <script src="https://cdn.jsdelivr.net/npm/htmx.org@2.0.8/dist/htmx.min.js"></script>

    <link rel="stylesheet" href="/style.css"/>
</head>
<body>
<div class="uk-offcanvas-content">
    <!-- Mobile Header -->
    <div class="uk-navbar-container uk-hidden@l" uk-navbar>
        <div class="uk-navbar-left">
            <a class="uk-navbar-toggle" uk-navbar-toggle-icon href="#mobile-sidebar" uk-toggle></a>
            <span class="uk-navbar-item uk-logo">HX-Qute</span>
        </div>
    </div>

    <!-- Main Layout Container -->
    <div class="uk-grid-collapse" uk-grid>
        <!-- Sidebar (visible on large screens) -->
        <div class="uk-width-auto@l uk-visible@l sidebar-container">
            <aside class="sidebar uk-height-viewport">
                <!-- Logo -->
                <div class="uk-padding-small">
                    <a href="/" class="uk-flex uk-flex-middle logo-link">
                        <img src="/img/logo-scaffold.png" width="40" height="40" alt="Logo"/>
                        <span class="uk-margin-small-left uk-text-bold uk-text-large">HX-Qute</span>
                    </a>
                </div>

                <!-- Navigation -->
                <ul class="uk-nav uk-nav-default uk-padding-small" uk-nav>
                    <li class="{#if currentPage?? == 'home'}uk-active{/if}">
                        <a href="/">
                            <span uk-icon="icon: home"></span>
                            <span class="uk-margin-small-left">Home</span>
                        </a>
                    </li>
                    <li class="uk-parent {#if currentPage?? == 'gender'}uk-open{/if}">
                        <a href="#">
                            <span uk-icon="icon: settings"></span>
                            <span class="uk-margin-small-left">Maintenance</span>
                        </a>
                        <ul class="uk-nav-sub">
                            <li class="{#if currentPage?? == 'gender'}uk-active{/if}">
                                <a href="/genders">Gender</a>
                            </li>
                        </ul>
                    </li>
                    {#if userName}
                    <li>
                        <a href="/logout">
                            <span uk-icon="icon: sign-out"></span>
                            <span class="uk-margin-small-left">Logout ({userName})</span>
                        </a>
                    </li>
                    {#else}
                    <li>
                        <a href="/login">
                            <span uk-icon="icon: sign-in"></span>
                            <span class="uk-margin-small-left">Login</span>
                        </a>
                    </li>
                    {/if}
                </ul>
            </aside>
        </div>

        <!-- Main Content -->
        <div class="uk-width-expand@l main-content-area">
            <div class="uk-padding">
                <div id="main-content" class="uk-container">
                    {#insert}Default content{/}
                </div>
            </div>
        </div>
    </div>

    <!-- Mobile Sidebar (Offcanvas) -->
    <div id="mobile-sidebar" uk-offcanvas="overlay: true">
        <div class="uk-offcanvas-bar sidebar">
            <button class="uk-offcanvas-close" type="button" uk-close></button>
            <!-- Same navigation as desktop sidebar -->
        </div>
    </div>
</div>
</body>
</html>
```

**Key Features:**
- **Responsive sidebar**: Desktop shows fixed sidebar, mobile uses offcanvas menu
- **Parameter declarations**: `{@String title}` etc. at the top for type safety
- **Null-safe navigation**: Uses `{#if currentPage?? == 'home'}` for safe comparison
- **Content slot**: `{#insert}` receives content from child templates

### 7.3 Qute Syntax Reference

| Syntax | Description | Example |
|--------|-------------|---------|
| `{expression}` | Output value | `{entity.name}` |
| `{#if}{/if}` | Conditional | `{#if userName != null}...{/if}` |
| `{#for}{/for}` | Iteration | `{#for item in items}...{/for}` |
| `{#include}{/include}` | Include template | `{#include base}...{/include}` |
| `{#insert /}` | Content slot | Used in base template |
| `{@Type name}` | Parameter declaration | `{@String title}` |
| `{item ?: 'default'}` | Elvis operator | `{name ?: 'Unknown'}` |
| `{item.raw}` | Unescaped output | `{htmlContent.raw}` |
| `{#fragment id=name}` | Named fragment | `{#fragment id=table}...{/fragment}` |
| `{#include $fragment}` | Include fragment | `{#include $table /}` |

### 7.4 Qute Fragments

Qute fragments allow defining reusable template sections within a single file. This is the preferred pattern for modal-based CRUD.

**Defining fragments:**
```html
{#fragment id=table}
<!-- Table content -->
{/fragment}

{#fragment id=modal_create rendered=false}
{@Entity entity}
{@String error}
<!-- Modal content for create form -->
{/fragment}
```

**Key attributes:**
- `id=name` — Fragment identifier, accessed as `$name` or via `Templates.entity$name()`
- `rendered=false` — Fragment is not rendered in main output; only accessible programmatically

**Accessing fragments from Java:**
```java
// Fragment methods use $ separator
public static native TemplateInstance entity$table(List<Entity> entities);
public static native TemplateInstance entity$modal_create(Entity entity, String error);
```

---

## 7.5 Modal-Based CRUD Pattern

This application uses UIkit modals with HTMX for Create, Edit, and Delete operations. The pattern provides a clean user experience without page reloads.

### 7.5.1 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Main Page                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Add Button                                               │   │
│  │  hx-get="/entities/create"                               │   │
│  │  hx-target="#entity-modal-body"                          │   │
│  │  hx-on::after-request="UIkit.modal(...).show()"          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Table Container (#entity-table-container)               │   │
│  │  ┌─────────────────────────────────────────────────────┐ │   │
│  │  │ Row 1: [Data] [Edit] [Delete]                       │ │   │
│  │  │ Row 2: [Data] [Edit] [Delete]                       │ │   │
│  │  └─────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Static Modal Shell (#entity-modal)                      │   │
│  │  ┌─────────────────────────────────────────────────────┐ │   │
│  │  │ Modal Body (#entity-modal-body)                     │ │   │
│  │  │ ← Content loaded dynamically via HTMX               │ │   │
│  │  └─────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

**Key Principles:**
1. **Single static modal shell** — Always present in DOM, content loaded dynamically
2. **HTMX loads modal content** — Button clicks fetch form HTML into modal body
3. **UIkit shows/hides modal** — Via `hx-on::after-request` inline handlers
4. **OOB swaps update table** — Success responses update table without closing modal first
5. **No `<script>` tags** — All JavaScript is inline via HTMX event handlers

**HTMX Event Handler Syntax:**
The `hx-on::` syntax is a shorthand for `hx-on:htmx:`. Both forms are valid:
- `hx-on::after-request` → shorthand (used in this project)
- `hx-on:htmx:after-request` → explicit form

Common HTMX events: `after-request`, `before-request`, `load`, `after-swap`

### 7.5.2 Page Template Structure

```html
{@String title}
{@String currentPage}
{@String userName}
{@java.util.List<io.archton.scaffold.entity.Entity> entities}

{#include base}
{#title}{title}{/title}

<h2 class="uk-heading-small">Entity Management</h2>

<!-- Add Button: loads create form into modal, then shows modal -->
<button class="uk-button uk-button-primary uk-margin-bottom"
        hx-get="/entities/create"
        hx-target="#entity-modal-body"
        hx-on::after-request="UIkit.modal('#entity-modal').show()">
    <span uk-icon="plus"></span> Add
</button>

<!-- Table Container: target for OOB updates -->
<div id="entity-table-container">
    {#include $table /}
</div>

<!-- Static Modal Shell: always present, content loaded dynamically -->
<div id="entity-modal" uk-modal="bg-close: false">
    <div class="uk-modal-dialog">
        <div id="entity-modal-body" class="uk-modal-body">
            <!-- Content loaded via HTMX -->
        </div>
    </div>
</div>

{/include}

{!-- Table Fragment --}
{#fragment id=table}
<table class="uk-table uk-table-hover uk-table-divider">
    <thead>
        <tr>
            <th>Name</th>
            <th>Description</th>
            <th class="uk-width-small">Actions</th>
        </tr>
    </thead>
    <tbody id="entity-table-body">
        {#for e in entities}
        <tr id="entity-row-{e.id}">
            <td>{e.name}</td>
            <td>{e.description}</td>
            <td>
                <div class="uk-button-group">
                    <button class="uk-button uk-button-small uk-button-primary"
                            hx-get="/entities/{e.id}/edit"
                            hx-target="#entity-modal-body"
                            hx-on::after-request="UIkit.modal('#entity-modal').show()">
                        Edit
                    </button>
                    <button class="uk-button uk-button-small uk-button-danger"
                            hx-get="/entities/{e.id}/delete"
                            hx-target="#entity-modal-body"
                            hx-on::after-request="UIkit.modal('#entity-modal').show()">
                        Delete
                    </button>
                </div>
            </td>
        </tr>
        {/for}
    </tbody>
</table>
{/fragment}
```

### 7.5.3 Modal Content Fragments

**Create Form Fragment:**
```html
{#fragment id=modal_create rendered=false}
{@io.archton.scaffold.entity.Entity entity}
{@String error}

<h2 class="uk-modal-title">Add Entity</h2>

{#if error??}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}

<form hx-post="/entities" hx-target="#entity-modal-body">
    <div class="uk-margin">
        <label class="uk-form-label">Name *</label>
        <input class="uk-input" type="text" name="name" 
               value="{entity.name ?: ''}" required />
    </div>
    <div class="uk-margin">
        <label class="uk-form-label">Description</label>
        <input class="uk-input" type="text" name="description" 
               value="{entity.description ?: ''}" />
    </div>
    <div class="uk-margin uk-text-right">
        <button class="uk-button uk-button-default uk-modal-close" type="button">
            Cancel
        </button>
        <button class="uk-button uk-button-primary" type="submit">
            Save
        </button>
    </div>
</form>
{/fragment}
```

**Edit Form Fragment:**
```html
{#fragment id=modal_edit rendered=false}
{@io.archton.scaffold.entity.Entity entity}
{@String error}

<h2 class="uk-modal-title">Edit Entity</h2>

{#if error??}
<div class="uk-alert uk-alert-danger">{error}</div>
{/if}

<form hx-put="/entities/{entity.id}" hx-target="#entity-modal-body">
    <div class="uk-margin">
        <label class="uk-form-label">Name *</label>
        <input class="uk-input" type="text" name="name" 
               value="{entity.name}" required />
    </div>
    <div class="uk-margin">
        <label class="uk-form-label">Description</label>
        <input class="uk-input" type="text" name="description" 
               value="{entity.description ?: ''}" />
    </div>
    <div class="uk-margin uk-text-right">
        <button class="uk-button uk-button-default uk-modal-close" type="button">
            Cancel
        </button>
        <button class="uk-button uk-button-primary" type="submit">
            Save
        </button>
    </div>
</form>
{/fragment}
```

**Delete Confirmation Fragment:**
```html
{#fragment id=modal_delete rendered=false}
{@io.archton.scaffold.entity.Entity entity}
{@String error}

<h2 class="uk-modal-title">Delete Entity</h2>

{#if error??}
<div class="uk-alert uk-alert-danger">{error}</div>
{#else}
<div class="uk-alert uk-alert-warning">
    Are you sure you want to delete <strong>{entity.name}</strong>?
</div>
<p class="uk-text-muted">This action cannot be undone.</p>
{/if}

<div class="uk-margin uk-text-right">
    <button class="uk-button uk-button-default uk-modal-close" type="button">
        Cancel
    </button>
    {#if !error??}
    <button class="uk-button uk-button-danger"
            hx-delete="/entities/{entity.id}"
            hx-target="#entity-modal-body">
        Delete
    </button>
    {/if}
</div>
{/fragment}
```

### 7.5.4 Success Response Fragments

Success responses close the modal and update the table using Out-of-Band (OOB) swaps.

**Create Success (refresh entire table):**
```html
{#fragment id=modal_success rendered=false}
{@String message}
{@java.util.List<io.archton.scaffold.entity.Entity> entities}

<!-- This div closes the modal when loaded -->
<div hx-on::load="UIkit.modal('#entity-modal').hide()"></div>

<!-- OOB swap: Replace table container content -->
<div id="entity-table-container" hx-swap-oob="innerHTML">
    {#include $table entities=entities /}
</div>
{/fragment}
```

**Edit Success (update single row):**
```html
{#fragment id=modal_success_row rendered=false}
{@String message}
{@io.archton.scaffold.entity.Entity entity}

<!-- Close modal -->
<div hx-on::load="UIkit.modal('#entity-modal').hide()"></div>

<!-- OOB swap: Update specific row -->
<template>
<tr id="entity-row-{entity.id}" hx-swap-oob="outerHTML">
    <td>{entity.name}</td>
    <td>{entity.description}</td>
    <td>
        <div class="uk-button-group">
            <button class="uk-button uk-button-small uk-button-primary"
                    hx-get="/entities/{entity.id}/edit"
                    hx-target="#entity-modal-body"
                    hx-on::after-request="UIkit.modal('#entity-modal').show()">
                Edit
            </button>
            <button class="uk-button uk-button-small uk-button-danger"
                    hx-get="/entities/{entity.id}/delete"
                    hx-target="#entity-modal-body"
                    hx-on::after-request="UIkit.modal('#entity-modal').show()">
                Delete
            </button>
        </div>
    </td>
</tr>
</template>
{/fragment}
```

**Delete Success (remove row):**
```html
{#fragment id=modal_delete_success rendered=false}
{@Long deletedId}

<!-- Close modal -->
<div hx-on::load="UIkit.modal('#entity-modal').hide()"></div>

<!-- OOB swap: Remove row (requires <template> wrapper for <tr>) -->
<template>
<tr id="entity-row-{deletedId}" hx-swap-oob="delete"></tr>
</template>
{/fragment}
```

**Important:** When using OOB swaps with `<tr>` elements, wrap them in `<template>` tags to prevent browser parsing issues.

### 7.5.5 Resource Class Pattern

```java
@Path("/entities")
@RolesAllowed({"user", "admin"})
public class EntityResource {

    @Inject
    SecurityIdentity securityIdentity;

    // Type-safe template methods including fragments
    @CheckedTemplate
    public static class Templates {
        // Full page
        public static native TemplateInstance entity(
            String title, String currentPage, String userName,
            List<Entity> entities);
        
        // Fragments (note $ separator)
        public static native TemplateInstance entity$table(List<Entity> entities);
        public static native TemplateInstance entity$modal_create(Entity entity, String error);
        public static native TemplateInstance entity$modal_edit(Entity entity, String error);
        public static native TemplateInstance entity$modal_delete(Entity entity, String error);
        public static native TemplateInstance entity$modal_success(String message, List<Entity> entities);
        public static native TemplateInstance entity$modal_success_row(String message, Entity entity);
        public static native TemplateInstance entity$modal_delete_success(Long deletedId);
    }

    // LIST - Full page or table fragment
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@Context HttpHeaders headers) {
        List<Entity> entities = Entity.listAllOrdered();
        
        if (isHtmxRequest(headers)) {
            return Templates.entity$table(entities);
        }
        return Templates.entity("Entities", "entities", getCurrentUsername(), entities);
    }

    // CREATE FORM - Return modal content
    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        return Templates.entity$modal_create(new Entity(), null);
    }

    // CREATE SUBMIT - Validate and save
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance create(
            @FormParam("name") String name,
            @FormParam("description") String description) {
        
        Entity entity = new Entity();
        entity.name = name;
        entity.description = description;
        
        // Validation
        if (name == null || name.trim().isEmpty()) {
            return Templates.entity$modal_create(entity, "Name is required.");
        }
        
        // Check uniqueness
        if (Entity.findByName(name.trim()) != null) {
            return Templates.entity$modal_create(entity, "Name already exists.");
        }
        
        // Save
        entity.name = name.trim();
        entity.createdBy = getCurrentUsername();
        entity.persist();
        
        // Return success with OOB table refresh
        return Templates.entity$modal_success("Entity created.", Entity.listAllOrdered());
    }

    // EDIT FORM - Return modal with pre-populated data
    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        Entity entity = Entity.findById(id);
        if (entity == null) {
            throw new NotFoundException("Entity not found");
        }
        return Templates.entity$modal_edit(entity, null);
    }

    // EDIT SUBMIT - Validate and update
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance update(
            @PathParam("id") Long id,
            @FormParam("name") String name,
            @FormParam("description") String description) {
        
        Entity entity = Entity.findById(id);
        if (entity == null) {
            throw new NotFoundException("Entity not found");
        }
        
        // Validation
        if (name == null || name.trim().isEmpty()) {
            return Templates.entity$modal_edit(entity, "Name is required.");
        }
        
        // Check uniqueness (exclude current record)
        Entity existing = Entity.findByName(name.trim());
        if (existing != null && !existing.id.equals(id)) {
            return Templates.entity$modal_edit(entity, "Name already exists.");
        }
        
        // Update
        entity.name = name.trim();
        entity.description = description;
        entity.updatedBy = getCurrentUsername();
        
        // Return success with OOB row update
        return Templates.entity$modal_success_row("Entity updated.", entity);
    }

    // DELETE CONFIRM - Return confirmation modal
    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance deleteConfirm(@PathParam("id") Long id) {
        Entity entity = Entity.findById(id);
        if (entity == null) {
            throw new NotFoundException("Entity not found");
        }
        return Templates.entity$modal_delete(entity, null);
    }

    // DELETE EXECUTE - Validate and remove
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance delete(@PathParam("id") Long id) {
        Entity entity = Entity.findById(id);
        if (entity == null) {
            throw new NotFoundException("Entity not found");
        }
        
        // Check if in use (example: foreign key constraint)
        if (Entity.isInUse(id)) {
            return Templates.entity$modal_delete(entity, 
                "Cannot delete: Entity is in use.");
        }
        
        entity.delete();
        return Templates.entity$modal_delete_success(id);
    }

    private String getCurrentUsername() {
        return securityIdentity.isAnonymous() 
            ? null 
            : securityIdentity.getPrincipal().getName();
    }

    private boolean isHtmxRequest(HttpHeaders headers) {
        return headers.getHeaderString("HX-Request") != null;
    }
}
```

### 7.5.6 CRUD Endpoints Summary

| Method | Path | Handler | Returns | Description |
|--------|------|---------|---------|-------------|
| GET | `/entities` | `list()` | Full page or `$table` | List all entities |
| GET | `/entities/create` | `createForm()` | `$modal_create` | Create form in modal |
| POST | `/entities` | `create()` | `$modal_create` or `$modal_success` | Submit create |
| GET | `/entities/{id}/edit` | `editForm()` | `$modal_edit` | Edit form in modal |
| PUT | `/entities/{id}` | `update()` | `$modal_edit` or `$modal_success_row` | Submit edit |
| GET | `/entities/{id}/delete` | `deleteConfirm()` | `$modal_delete` | Confirm delete in modal |
| DELETE | `/entities/{id}` | `delete()` | `$modal_delete` or `$modal_delete_success` | Execute delete |

### 7.5.7 Key Implementation Notes

**Modal Shell Configuration:**
```html
<div id="entity-modal" uk-modal="bg-close: false">
```
- `bg-close: false` prevents accidental closure when clicking backdrop
- Users must explicitly click Cancel or press Escape

**Cancel Button (No Server Request):**
```html
<button class="uk-button uk-button-default uk-modal-close" type="button">
    Cancel
</button>
```
- Uses UIkit's `uk-modal-close` class
- Closes modal client-side without any HTMX request
- Original data preserved (no modifications until Save)

**Error Handling in Modal:**
- Validation errors re-render the form with error message
- Modal stays open; user can correct and resubmit
- Use `{#if error??}` for null-safe error display

**Button Styling:**
- Group Edit/Delete with `uk-button-group` for visual cohesion
- Edit uses `uk-button-primary` (blue)
- Delete uses `uk-button-danger` (red)
- Save uses `uk-button-primary` (blue)
- Cancel uses `uk-button-default` (gray)

---

## 8. HTMX Integration

### 8.1 Core HTMX Attributes

| Attribute | Purpose | Example |
|-----------|---------|---------|
| `hx-get` | GET request | `hx-get="/entities"` |
| `hx-post` | POST request | `hx-post="/entities/create"` |
| `hx-put` | PUT request | `hx-put="/entities/1"` |
| `hx-delete` | DELETE request | `hx-delete="/entities/1"` |
| `hx-target` | Response destination | `hx-target="#table-body"` |
| `hx-swap` | Swap strategy | `hx-swap="outerHTML"` |
| `hx-swap-oob` | Out-of-band swap | `hx-swap-oob="innerHTML"` |
| `hx-trigger` | Event trigger | `hx-trigger="keyup changed delay:500ms"` |
| `hx-on::event` | Inline event handler | `hx-on::after-request="doSomething()"` |
| `hx-push-url` | Update browser URL | `hx-push-url="true"` |
| `hx-indicator` | Loading indicator | `hx-indicator="#spinner"` |

### 8.2 HTMX Patterns

#### Inline Row Editing

```html
<!-- Display mode -->
<tr id="entity-row-{id}">
    <td>{name}</td>
    <td>
        <button hx-get="/entities/{id}/edit"
                hx-target="closest tr"
                hx-swap="outerHTML">Edit</button>
    </td>
</tr>

<!-- Edit mode (returned from /entities/{id}/edit) -->
<tr id="entity-row-{id}">
    <td>
        <input name="name" value="{name}" />
    </td>
    <td>
        <button hx-post="/entities/{id}/update"
                hx-target="closest tr"
                hx-swap="outerHTML"
                hx-include="closest tr">Save</button>
        <button hx-get="/entities/{id}"
                hx-target="closest tr"
                hx-swap="outerHTML">Cancel</button>
    </td>
</tr>
```

#### Out-of-Band Updates

When creating an entity, update both the form area and the table:

```html
<!-- person_success.html -->
<div id="person-create-container">
    <div class="uk-alert uk-alert-success">{message}</div>
    <button hx-get="/persons/create" 
            hx-target="#person-create-container">
        Add Another
    </button>
</div>

<!-- OOB table refresh -->
<tbody id="persons-table-body" hx-swap-oob="true">
    {#for person in persons}
        {#include partials/person_row person=person /}
    {/for}
</tbody>
```

#### Active Search with Debounce

```html
<input type="search" 
       name="filter"
       hx-get="/entities"
       hx-target="#entity-table-container"
       hx-trigger="keyup changed delay:300ms, search"
       hx-push-url="true"
       placeholder="Search..." />
```

### 8.3 Response Headers

| Header | Purpose | Example |
|--------|---------|---------|
| `HX-Redirect` | Full page redirect | `HX-Redirect: /login` |
| `HX-Trigger` | Trigger client event | `HX-Trigger: entityCreated` |
| `HX-Retarget` | Override target | `HX-Retarget: #error-area` |
| `HX-Reswap` | Override swap method | `HX-Reswap: innerHTML` |

### 8.4 Content Negotiation

Resources check for HTMX requests to return partials vs full pages:

```java
private boolean isHtmxRequest(HttpHeaders headers) {
    return headers.getHeaderString("HX-Request") != null;
}

@GET
public TemplateInstance list(@Context HttpHeaders headers) {
    if (isHtmxRequest(headers)) {
        return Partials.entity_table(entities);
    }
    return Templates.entities(title, currentPage, userName, entities);
}
```

---

## 9. Security Architecture

### 9.1 Authentication Flow

The application uses Quarkus form-based authentication with JPA identity provider:

```
┌─────────────┐     POST /j_security_check     ┌─────────────────┐
│   Browser   │ ─────────────────────────────> │ Quarkus Security│
│  (Login     │     j_username, j_password     │                 │
│   Form)     │                                │ ┌─────────────┐ │
└─────────────┘                                │ │ UserLogin   │ │
       ▲                                       │ │ Entity      │ │
       │        Set-Cookie: quarkus-credential │ │ @UserDefn   │ │
       └─────────────────────────────────────  │ └─────────────┘ │
                                               └─────────────────┘
```

### 9.2 Security Configuration

```properties
# Form Authentication
quarkus.http.auth.form.enabled=true
quarkus.http.auth.form.login-page=/login
quarkus.http.auth.form.landing-page=/
quarkus.http.auth.form.error-page=/login?error=true
quarkus.http.auth.form.timeout=PT30M
quarkus.http.auth.form.cookie-name=quarkus-credential
quarkus.http.auth.form.http-only-cookie=true

# Session Security
quarkus.http.auth.form.new-cookie-interval=PT1M
quarkus.http.same-site-cookie.quarkus-credential.value=strict

# Route Protection
quarkus.http.auth.permission.authenticated.paths=/dashboard/*,/api/*,/persons/*,/profile/*
quarkus.http.auth.permission.authenticated.policy=authenticated

quarkus.http.auth.permission.admin.paths=/admin/*,/genders/*
quarkus.http.auth.permission.admin.policy=admin
quarkus.http.auth.policy.admin.roles-allowed=admin

quarkus.http.auth.permission.public.paths=/,/login,/signup,/logout,/css/*,/js/*,/images/*,/webjars/*,/img/*,/style.css
quarkus.http.auth.permission.public.policy=permit
```

### 9.3 Role-Based Access Control

```java
// Admin-only resource
@Path("/genders")
@RolesAllowed("admin")
public class GenderResource { }

// Authenticated users
@Path("/persons")
@RolesAllowed({"user", "admin"})
public class PersonResource { }

// Method-level security
@GET
@RolesAllowed("admin")
public TemplateInstance adminOnly() { }
```

### 9.4 Password Policy (NIST SP 800-63B-4)

| Requirement | Value |
|-------------|-------|
| Minimum length | 15 characters |
| Maximum length | 128 characters |
| Hashing | BCrypt, cost factor 12 |
| Composition rules | None (per NIST) |
| Storage format | Modular Crypt Format (MCF) |

```java
@ApplicationScoped
public class PasswordValidator {
    
    @ConfigProperty(name = "app.security.password.min-length", defaultValue = "15")
    int minLength;
    
    @ConfigProperty(name = "app.security.password.max-length", defaultValue = "128")
    int maxLength;
    
    public List<String> validate(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.length() < minLength) {
            errors.add("Password must be at least " + minLength + " characters.");
        }
        if (password != null && password.length() > maxLength) {
            errors.add("Password must be " + maxLength + " characters or less.");
        }
        return errors;
    }
}
```

---

## 10. Configuration Reference

### 10.1 application.properties

```properties
# =============================================================================
# Server Configuration
# =============================================================================
quarkus.http.port=9080

# =============================================================================
# Database Configuration
# =============================================================================
quarkus.datasource.db-kind=postgresql
# Connection details from environment or dev services

# =============================================================================
# Flyway Configuration
# =============================================================================
quarkus.flyway.migrate-at-start=true
quarkus.hibernate-orm.schema-management.strategy=none

# =============================================================================
# Form Authentication
# =============================================================================
quarkus.http.auth.form.enabled=true
quarkus.http.auth.form.login-page=/login
quarkus.http.auth.form.landing-page=/
quarkus.http.auth.form.error-page=/login?error=true
quarkus.http.auth.form.timeout=PT30M
quarkus.http.auth.form.cookie-name=quarkus-credential
quarkus.http.auth.form.http-only-cookie=true
quarkus.http.auth.form.new-cookie-interval=PT1M
quarkus.http.same-site-cookie.quarkus-credential.value=strict

# =============================================================================
# Route Protection
# =============================================================================
quarkus.http.auth.permission.authenticated.paths=/dashboard/*,/api/*,/persons/*,/profile/*
quarkus.http.auth.permission.authenticated.policy=authenticated

quarkus.http.auth.permission.admin.paths=/admin/*,/genders/*
quarkus.http.auth.permission.admin.policy=admin
quarkus.http.auth.policy.admin.roles-allowed=admin

quarkus.http.auth.permission.public.paths=/,/login,/signup,/logout,/css/*,/js/*,/images/*,/webjars/*,/img/*,/style.css
quarkus.http.auth.permission.public.policy=permit

# =============================================================================
# Password Policy (NIST SP 800-63B-4)
# =============================================================================
app.security.password.min-length=15
app.security.password.max-length=128

# =============================================================================
# Development Settings
# =============================================================================
quarkus.log.console.darken=1
quarkus.banner.enabled=true
quarkus.banner.path=banner.txt
```

### 10.2 Static Resources

Static files are served from `src/main/resources/META-INF/resources/`:

| File Path | URL |
|-----------|-----|
| `META-INF/resources/style.css` | `/style.css` |
| `META-INF/resources/img/logo.png` | `/img/logo.png` |

---

## 11. Testing Patterns

### 11.1 Base Test Class

```java
@QuarkusTest
public abstract class BaseHtmxTest {

    protected String loginAndGetCookie(String username, String password) {
        return given()
            .formParam("j_username", username)
            .formParam("j_password", password)
            .when()
            .post("/j_security_check")
            .then()
            .extract()
            .cookie("quarkus-credential");
    }

    protected Response get(String path) {
        return given().when().get(path);
    }

    protected Response htmxGet(String path) {
        return given()
            .header("HX-Request", "true")
            .when().get(path);
    }

    protected Response authenticatedGet(String path, String cookie) {
        return given()
            .cookie("quarkus-credential", cookie)
            .when().get(path);
    }

    protected Response authenticatedHtmxPost(String path, String cookie, Map<String, String> formParams) {
        var request = given()
            .cookie("quarkus-credential", cookie)
            .header("HX-Request", "true")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        formParams.forEach(request::formParam);
        return request.when().post(path);
    }

    protected Document parseHtml(String html) {
        return Jsoup.parse(html);
    }
}
```

### 11.2 Test Example

```java
@QuarkusTest
public class PersonResourceTest extends BaseHtmxTest {

    @Test
    public void testPersonListRequiresAuth() {
        get("/persons")
            .then()
            .statusCode(302)
            .header("Location", containsString("/login"));
    }

    @Test
    public void testPersonListAuthenticated() {
        String cookie = loginAndGetCookie("admin@example.com", "AdminPassword123");
        
        authenticatedGet("/persons", cookie)
            .then()
            .statusCode(200)
            .body(containsString("Persons"));
    }

    @Test
    public void testPersonCreateHtmx() {
        String cookie = loginAndGetCookie("admin@example.com", "AdminPassword123");
        
        authenticatedHtmxPost("/persons/create", cookie, Map.of(
            "firstName", "John",
            "lastName", "Doe",
            "email", "john@example.com"
        ))
            .then()
            .statusCode(200)
            .body(containsString("john@example.com"));
    }
}
```

### 11.3 Test Credentials

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | AdminPassword123 | admin |

---

## 12. Development Workflow

### 12.1 Starting Development

```bash
# Start Quarkus in dev mode
./mvnw quarkus:dev

# Application runs at http://localhost:9080
# Dev UI at http://localhost:9080/q/dev/
```

### 12.2 Creating a New Feature

1. **Create migration**: Add `V{version}__Description.sql` in `db/migration/`
2. **Create entity**: Add entity class extending `PanacheEntityBase`
3. **Create resource**: Add resource class with `@CheckedTemplate` classes
4. **Create templates**: Add page and partial templates
5. **Add tests**: Create test class extending `BaseHtmxTest`
6. **Update navigation**: Add link in `base.html` if needed

### 12.3 Adding Navigation Items

Edit the sidebar in `templates/base.html`:

```html
<li class="{#if currentPage == 'mypage'}uk-active{/if}">
    <a href="/mypage">My Page</a>
</li>
```

### 12.4 Template Development Tips

1. Check for `HX-Request` header to return partials vs full pages
2. Use `@CheckedTemplate` for compile-time validation
3. Keep partials focused on single components
4. Use OOB swaps for updating multiple page areas
5. Test with both direct navigation and HTMX requests

---

## Quick Reference

### Common Patterns

| Task | Pattern |
|------|---------|
| Get current user | `securityIdentity.getPrincipal().getName()` |
| Check if authenticated | `!securityIdentity.isAnonymous()` |
| Find entity | `Entity.findById(id)` |
| List all | `Entity.listAll()` |
| Find by field | `Entity.find("field", value).firstResult()` |
| Persist entity | `entity.persist()` |
| Delete entity | `entity.delete()` |
| HTMX check | `headers.getHeaderString("HX-Request") != null` |

### Development URLs

| URL | Description |
|-----|-------------|
| `http://localhost:9080` | Application |
| `http://localhost:9080/q/dev/` | Dev UI |
| `http://localhost:9080/q/health` | Health check |

---

*Document Version: 2.1*
*Last Updated: December 2025*
