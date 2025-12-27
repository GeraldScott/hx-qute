# HX Qute - System Specification

This document provides a comprehensive technical specification for HX Qute, a reference application built with Quarkus, HTMX, and Qute templates. It serves as a blueprint for developers building similar applications or extending this one.

---

## Table of Contents

1. [Technology Stack](#1-technology-stack)
2. [Project Structure](#2-project-structure)
3. [Entity Layer](#3-entity-layer)
4. [Resource Layer (Controllers)](#4-resource-layer-controllers)
5. [Template System](#5-template-system)
6. [HTMX Integration Patterns](#6-htmx-integration-patterns)
7. [Security Architecture](#7-security-architecture)
8. [Query Filtering Pattern](#8-query-filtering-pattern)
9. [Startup and Data Initialization](#9-startup-and-data-initialization)
10. [Configuration](#10-configuration)
11. [Static Resources](#11-static-resources)
12. [Common Patterns Reference](#12-common-patterns-reference)
13. [Use Case Traceability Matrix](#13-use-case-traceability-matrix)

**Related Documents:**
- [USER-STORIES.md](USER-STORIES.md) - Agile user stories
- [USE-CASES.md](USE-CASES.md) - Detailed use case specifications
- [TEST-CASES.md](TEST-CASES.md) - Test case documentation
- [TEST-STRATEGY.md](TEST-STRATEGY.md) - Testing strategy
- [LOGIN.md](LOGIN.md) - Authentication technical specification

---

## 1. Technology Stack

### Core Framework
| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Quarkus | 3.30.3 |
| Language | Java | 21 |
| Build Tool | Maven | 3.x |

### Backend Dependencies
| Purpose | Dependency | Notes |
|---------|------------|-------|
| REST API | `quarkus-rest` | RESTEasy Reactive |
| Templating | `quarkus-rest-qute` | Type-safe templates |
| ORM | `quarkus-hibernate-orm-panache` | Active Record pattern |
| Database | `quarkus-jdbc-postgresql` | PostgreSQL driver |
| Migrations | `quarkus-flyway` | Schema versioning |
| Security | `quarkus-security-jpa` | Form-based auth with BCrypt |
| Validation | `quarkus-hibernate-validator` | Bean validation |
| CDI | `quarkus-arc` | Dependency injection |
| Testing | `quarkus-junit5` | JUnit 5 integration |

### Frontend Stack (CDN-based)
| Purpose | Technology | CDN Source |
|---------|------------|------------|
| Dynamic UI | HTMX 2.0.8 | jsdelivr.net |
| CSS Framework | UIkit 3.25 | jsdelivr.net |
| Custom Styles | Custom CSS | Local `/style.css` |

---

## 2. Project Structure

```
src/main/java/io/archton/scaffold/
├── entity/              # JPA entities (Panache public field style)
│   ├── Gender.java
│   ├── Person.java
│   └── UserLogin.java
├── repository/          # PanacheRepository implementations
│   └── GenderRepository.java
├── router/              # REST resources with @CheckedTemplate
│   ├── IndexResource.java
│   ├── GenderResource.java
│   ├── PersonResource.java
│   └── AuthResource.java
├── security/            # Custom security providers
│   └── CaseInsensitiveIdentityProvider.java
├── service/             # Business logic services
│   └── PasswordService.java
└── error/               # Exception handling
    └── GlobalExceptionMapper.java

src/main/resources/
├── db/migration/        # Flyway migrations
│   ├── V1.0.0__Create_gender_table.sql
│   ├── V1.0.1__Insert_gender_data.sql
│   ├── V1.1.0__Create_person_table.sql
│   └── V1.2.0__Create_user_login_table.sql
├── templates/
│   ├── base.html                    # Base layout
│   ├── error.html                   # Error page
│   ├── IndexResource/
│   │   └── index.html               # Home page
│   ├── GenderResource/
│   │   ├── gender.html              # Gender list
│   │   └── genderForm.html          # Gender create/edit form
│   ├── PersonResource/
│   │   ├── persons.html             # Persons list
│   │   └── personForm.html          # Person create/edit form
│   └── AuthResource/
│       ├── login.html               # Login page
│       ├── signup.html              # Signup page
│       └── logout.html              # Logout confirmation
├── META-INF/resources/
│   ├── style.css                    # Custom styles
│   ├── favicon.ico
│   └── img/                         # Images
└── application.properties
```

---

## 3. Entity Layer

### 3.1 Base Pattern: Panache Active Record

All entities extend `PanacheEntity`, which provides:
- Auto-managed `id` field (Long)
- Built-in CRUD methods (`persist()`, `delete()`, `findById()`, etc.)
- Static finder methods via Active Record pattern

```java
@Entity
@Table(name = "table_name")
public class MyEntity extends PanacheEntity {

    // Public fields (no getters/setters needed)
    @Column(nullable = false)
    public String fieldName;

    // Static finder methods
    public static MyEntity findByFieldName(String value) {
        return find("fieldName", value).firstResult();
    }

    public static List<MyEntity> findByStatus(String status) {
        return list("status", status);
    }
}
```

### 3.2 Entity: Gender (Master Data)

**Purpose**: Reference data for gender classification.

**File**: `entity/Gender.java`

```java
@Entity
@Table(name = "gender")
public class Gender extends PanacheEntity {

    @NotBlank
    @Size(max = 7)
    @Column(nullable = false, unique = true, length = 7)
    public String code;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    public String description;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Size(max = 255)
    @Column(name = "created_by", updatable = false)
    public String createdBy;

    @Size(max = 255)
    @Column(name = "updated_by")
    public String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (code != null) {
            code = code.toUpperCase();  // Coerce to uppercase
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (code != null) {
            code = code.toUpperCase();
        }
    }

    // Finder methods
    public static Gender findByCode(String code) {
        return find("code", code.toUpperCase()).firstResult();
    }

    public static Gender findByDescription(String description) {
        return find("description", description).firstResult();
    }

    public static List<Gender> listAllOrdered() {
        return list("ORDER BY code ASC");
    }
}
```

**Key Patterns**:
- Code coerced to uppercase on persist/update
- Unique constraints on code and description
- Audit fields for tracking changes

### 3.3 Entity: Person (User Profile)

**Purpose**: Stores user profile information, separate from authentication credentials.

**File**: `entity/Person.java`

```java
@Entity
@Table(name = "person")
public class Person extends PanacheEntity {

    @Size(max = 100)
    @Column(name = "first_name", length = 100)
    public String firstName;

    @Size(max = 100)
    @Column(name = "last_name", length = 100)
    public String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    public String email;

    @Size(max = 20)
    @Column(length = 20)
    public String phone;

    @Past
    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id")
    public Gender gender;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Size(max = 255)
    @Column(name = "created_by", updatable = false)
    public String createdBy;

    @Size(max = 255)
    @Column(name = "updated_by")
    public String updatedBy;

    @Column(nullable = false)
    public boolean active = true;

    // Bidirectional relationship with UserLogin
    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL,
              orphanRemoval = true, fetch = FetchType.LAZY)
    public UserLogin userLogin;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (email != null) {
            email = email.toLowerCase().trim();  // Normalize email
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }

    // Finder methods
    public static Person findByEmail(String email) {
        return find("email", email.toLowerCase().trim()).firstResult();
    }

    public static List<Person> listAllOrdered() {
        return list("ORDER BY lastName ASC, firstName ASC");
    }

    public static List<Person> findByNameContaining(String searchText) {
        String pattern = "%" + searchText.toLowerCase() + "%";
        return list("LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1", pattern);
    }

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
}
```

**Key Patterns**:
- Email normalized to lowercase and trimmed on persist/update
- Audit timestamps managed via `@PrePersist` / `@PreUpdate`
- Bidirectional one-to-one with `UserLogin`
- Optional relationship with `Gender`

### 3.4 Entity: UserLogin (Authentication)

**Purpose**: Stores authentication credentials with security annotations for Quarkus Security JPA.

**File**: `entity/UserLogin.java`

```java
@Entity
@Table(name = "user_login")
@UserDefinition  // Marks this as the security entity
public class UserLogin extends PanacheEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    public Person person;

    @Username  // Security annotation: identifies the username field
    @NotBlank
    @Size(min = 3, max = 255)
    @Column(nullable = false, unique = true)
    public String username;

    @Password(value = PasswordType.MCF)  // Security annotation: MCF = modular crypt format (BCrypt)
    @NotBlank
    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Roles  // Security annotation: identifies the roles field
    @Column(nullable = false)
    public String role = "user";

    // Login tracking
    @Column(name = "last_login")
    public Instant lastLogin;

    // Account lockout (Phase 2 - schema ready)
    @Column(name = "failed_attempts", nullable = false)
    public int failedAttempts = 0;

    @Column(name = "locked_until")
    public Instant lockedUntil;

    // MFA fields (Phase 2 - schema ready)
    @Column(name = "mfa_enabled", nullable = false)
    public boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    public String mfaSecret;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Size(max = 255)
    @Column(name = "created_by", updatable = false)
    public String createdBy;

    @Size(max = 255)
    @Column(name = "updated_by")
    public String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (username != null) {
            username = username.toLowerCase().trim();  // Normalize username
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Business methods (Phase 2 - lockout enforcement)
    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    public void recordFailedAttempt(int maxAttempts, int lockoutMinutes) {
        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            int lockoutCount = failedAttempts / maxAttempts;
            int multiplier = (int) Math.pow(2, lockoutCount - 1);
            long lockoutSeconds = (long) lockoutMinutes * 60 * multiplier;
            lockoutSeconds = Math.min(lockoutSeconds, 24 * 60 * 60);  // Cap at 24h
            lockedUntil = Instant.now().plusSeconds(lockoutSeconds);
        }
    }

    public void recordSuccessfulLogin() {
        failedAttempts = 0;
        lockedUntil = null;
        lastLogin = Instant.now();
    }

    // Finder methods
    public static UserLogin findByUsername(String username) {
        return find("username", username.toLowerCase().trim()).firstResult();
    }

    public static UserLogin findByPersonId(Long personId) {
        return find("person.id", personId).firstResult();
    }

    public static UserLogin findByEmail(String email) {
        return find("person.email", email.toLowerCase().trim()).firstResult();
    }
}
```

**Key Patterns**:
- `@UserDefinition` marks this as the security entity for Quarkus Security JPA
- `@Username`, `@Password`, `@Roles` for automatic authentication
- `PasswordType.MCF` indicates BCrypt modular crypt format
- Account lockout and MFA fields ready for Phase 2 implementation

---

## 4. Resource Layer (Controllers)

### 4.1 Base Resource Pattern

Resources follow this structure:
1. Class-level `@Path` annotation
2. Optional `@RolesAllowed` for security
3. Injected dependencies (`SecurityIdentity`, repositories)
4. Nested `@CheckedTemplate` class for type-safe templates
5. Handler methods returning `TemplateInstance`

```java
@Path("/resource-path")
@RolesAllowed({"user", "admin"})  // Optional: protect entire resource
public class MyResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    MyRepository repository;

    // Templates located at templates/MyResource/*.html
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance list(
            String title,
            String currentPage,
            String userName,
            List<Entity> items
        );

        public static native TemplateInstance form(
            String title,
            String currentPage,
            String userName,
            Entity item,
            String error
        );
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list() {
        String username = securityIdentity.isAnonymous()
            ? null
            : securityIdentity.getPrincipal().getName();

        List<Entity> items = repository.listAll();

        return Templates.list("Entities", "entity", username, items);
    }
}
```

### 4.2 GenderResource (Complete CRUD Example - Admin)

**File**: `router/GenderResource.java`

This resource demonstrates the full CRUD pattern with HTMX integration for admin-only master data.

```java
@Path("/genders")
@RolesAllowed("admin")  // Admin only access
public class GenderResource {

    @Inject
    GenderRepository genderRepository;

    @Inject
    SecurityIdentity securityIdentity;

    // Full page templates (located at templates/GenderResource/*.html)
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance gender(
            String title,
            String currentPage,
            String userName,
            List<Gender> genders,
            String filterText
        );
    }

    // Partial templates for HTMX updates (located at templates/partials/*.html)
    @CheckedTemplate(basePath = "partials")
    public static class Partials {
        public static native TemplateInstance gender_table(
            List<Gender> genders,
            String filterText
        );

        public static native TemplateInstance gender_form(
            Gender gender,
            String error
        );

        public static native TemplateInstance gender_success(String message);

        public static native TemplateInstance gender_error(String message);
    }

    // LIST with optional filtering
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(
            @Context HttpHeaders headers,
            @QueryParam("filter") String filterText) {

        String username = securityIdentity.getPrincipal().getName();

        List<Gender> genders;
        if (filterText != null && !filterText.trim().isEmpty()) {
            genders = Gender.findByCodeOrDescriptionContaining(filterText.trim());
        } else {
            genders = Gender.listAllOrdered();
        }

        // Return partial for HTMX requests, full page otherwise
        if (isHtmxRequest(headers)) {
            return Partials.gender_table(genders, filterText);
        }
        return Templates.gender("Gender Management", "gender", username, genders, filterText);
    }

    // CREATE form (GET) - returns partial for modal/inline form
    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        return Partials.gender_form(null, null);
    }

    // CREATE submit (POST)
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance create(
            @FormParam("code") String code,
            @FormParam("description") String description) {

        String username = securityIdentity.getPrincipal().getName();

        // Validation: code required
        if (code == null || code.trim().isEmpty()) {
            return Partials.gender_form(null, "Code is required.");
        }

        // Validation: description required
        if (description == null || description.trim().isEmpty()) {
            return Partials.gender_form(null, "Description is required.");
        }

        // Validation: code max length
        if (code.trim().length() > 7) {
            return Partials.gender_form(null, "Code must be 7 characters or less.");
        }

        // Check for duplicate code
        if (Gender.findByCode(code.trim()) != null) {
            return Partials.gender_form(null, "Code already exists.");
        }

        // Check for duplicate description
        if (Gender.findByDescription(description.trim()) != null) {
            return Partials.gender_form(null, "Description already exists.");
        }

        // Create entity
        Gender gender = new Gender();
        gender.code = code.trim().toUpperCase();
        gender.description = description.trim();
        gender.createdBy = username;
        gender.updatedBy = username;
        gender.persist();

        return Partials.gender_success("Gender '" + gender.code + "' was created successfully!");
    }

    // EDIT form (GET) - returns partial for modal/inline form
    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        Gender gender = Gender.findById(id);

        if (gender == null) {
            throw new NotFoundException("Gender not found");
        }

        return Partials.gender_form(gender, null);
    }

    // UPDATE submit (POST)
    @POST
    @Path("/{id}/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance update(
            @PathParam("id") Long id,
            @FormParam("code") String code,
            @FormParam("description") String description) {

        String username = securityIdentity.getPrincipal().getName();
        Gender gender = Gender.findById(id);

        if (gender == null) {
            throw new NotFoundException("Gender not found");
        }

        // Validation: code required
        if (code == null || code.trim().isEmpty()) {
            return Partials.gender_form(gender, "Code is required.");
        }

        // Validation: description required
        if (description == null || description.trim().isEmpty()) {
            return Partials.gender_form(gender, "Description is required.");
        }

        // Check for duplicate code (excluding current record)
        Gender existingCode = Gender.findByCode(code.trim());
        if (existingCode != null && !existingCode.id.equals(id)) {
            return Partials.gender_form(gender, "Code already exists.");
        }

        // Check for duplicate description (excluding current record)
        Gender existingDesc = Gender.findByDescription(description.trim());
        if (existingDesc != null && !existingDesc.id.equals(id)) {
            return Partials.gender_form(gender, "Description already exists.");
        }

        // Update entity
        gender.code = code.trim().toUpperCase();
        gender.description = description.trim();
        gender.updatedBy = username;
        // Managed entity - no explicit persist needed

        return Partials.gender_success("Gender '" + gender.code + "' was updated successfully!");
    }

    // DELETE
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance delete(@PathParam("id") Long id) {
        Gender gender = Gender.findById(id);

        if (gender == null) {
            throw new NotFoundException("Gender not found");
        }

        // Check if gender is in use by Person records
        long personCount = Person.count("gender.id", id);
        if (personCount > 0) {
            return Partials.gender_error(
                "Cannot delete: Gender is in use by " + personCount + " person(s)."
            );
        }

        String deletedCode = gender.code;
        gender.delete();

        return Partials.gender_success("Gender '" + deletedCode + "' was deleted successfully!");
    }

    // Helper: detect HTMX requests
    private boolean isHtmxRequest(HttpHeaders headers) {
        return headers.getHeaderString("HX-Request") != null;
    }
}
```

**Key Patterns**:
- `Templates` class for full page renders (direct navigation)
- `Partials` class with `basePath = "partials"` for HTMX fragment updates
- `isHtmxRequest()` helper checks for `HX-Request` header
- Returns `TemplateInstance` directly (no `Response.seeOther()`)
- Success/error partials for in-place feedback after mutations

---

### 4.3 PersonResource (Complete CRUD Example)

**File**: `router/PersonResource.java`

This resource demonstrates the full CRUD pattern with filtering and HTMX integration.

```java
@Path("/persons")
@RolesAllowed({"user", "admin"})
public class PersonResource {

    @Inject
    SecurityIdentity securityIdentity;

    // Full page templates (located at templates/PersonResource/*.html)
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance persons(
            String title,
            String currentPage,
            String userName,
            List<Person> persons,
            String filterText,
            List<Gender> genderChoices
        );
    }

    // Partial templates for HTMX updates (located at templates/partials/*.html)
    @CheckedTemplate(basePath = "partials")
    public static class Partials {
        public static native TemplateInstance persons_table(
            List<Person> persons,
            String filterText
        );

        public static native TemplateInstance person_form(
            Person person,
            List<Gender> genderChoices,
            String error
        );

        public static native TemplateInstance person_success(String message);

        public static native TemplateInstance person_error(String message);
    }

    // LIST with filtering
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(
            @Context HttpHeaders headers,
            @QueryParam("filter") String filterText) {

        String username = securityIdentity.getPrincipal().getName();

        List<Person> persons;
        if (filterText != null && !filterText.trim().isEmpty()) {
            persons = Person.findByNameContaining(filterText.trim());
        } else {
            persons = Person.listAllOrdered();
        }

        List<Gender> genderChoices = Gender.listAllOrdered();

        // Return partial for HTMX requests, full page otherwise
        if (isHtmxRequest(headers)) {
            return Partials.persons_table(persons, filterText);
        }
        return Templates.persons(
            "Persons", "persons", username,
            persons, filterText, genderChoices
        );
    }

    // CREATE form (GET) - returns partial for modal/inline form
    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        List<Gender> genderChoices = Gender.listAllOrdered();
        return Partials.person_form(null, genderChoices, null);
    }

    // CREATE submit (POST)
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance create(
            @FormParam("firstName") String firstName,
            @FormParam("lastName") String lastName,
            @FormParam("email") String email,
            @FormParam("phone") String phone,
            @FormParam("dateOfBirth") LocalDate dateOfBirth,
            @FormParam("genderId") Long genderId) {

        String username = securityIdentity.getPrincipal().getName();
        List<Gender> genderChoices = Gender.listAllOrdered();

        // Validation: email required
        if (email == null || email.trim().isEmpty()) {
            return Partials.person_form(null, genderChoices, "Email is required.");
        }

        // Validation: email format
        if (!isValidEmail(email)) {
            return Partials.person_form(null, genderChoices, "Invalid email format.");
        }

        // Check for duplicate email
        if (Person.findByEmail(email.trim()) != null) {
            return Partials.person_form(null, genderChoices, "Email already registered.");
        }

        // Create entity
        Person person = new Person();
        person.firstName = firstName != null ? firstName.trim() : null;
        person.lastName = lastName != null ? lastName.trim() : null;
        person.email = email.trim().toLowerCase();
        person.phone = phone != null ? phone.trim() : null;
        person.dateOfBirth = dateOfBirth;
        person.createdBy = username;
        person.updatedBy = username;

        if (genderId != null) {
            person.gender = Gender.findById(genderId);
        }

        person.persist();

        String displayName = person.getDisplayName();
        return Partials.person_success("Person '" + displayName + "' was created successfully!");
    }

    // EDIT form (GET) - returns partial for modal/inline form
    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        Person person = Person.findById(id);

        if (person == null) {
            throw new NotFoundException("Person not found");
        }

        List<Gender> genderChoices = Gender.listAllOrdered();
        return Partials.person_form(person, genderChoices, null);
    }

    // UPDATE submit (POST)
    @POST
    @Path("/{id}/update")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance update(
            @PathParam("id") Long id,
            @FormParam("firstName") String firstName,
            @FormParam("lastName") String lastName,
            @FormParam("email") String email,
            @FormParam("phone") String phone,
            @FormParam("dateOfBirth") LocalDate dateOfBirth,
            @FormParam("genderId") Long genderId) {

        String username = securityIdentity.getPrincipal().getName();
        Person person = Person.findById(id);
        List<Gender> genderChoices = Gender.listAllOrdered();

        if (person == null) {
            throw new NotFoundException("Person not found");
        }

        // Validation: email required
        if (email == null || email.trim().isEmpty()) {
            return Partials.person_form(person, genderChoices, "Email is required.");
        }

        // Validation: email format
        if (!isValidEmail(email)) {
            return Partials.person_form(person, genderChoices, "Invalid email format.");
        }

        // Check for duplicate email (excluding current record)
        Person existingEmail = Person.findByEmail(email.trim());
        if (existingEmail != null && !existingEmail.id.equals(id)) {
            return Partials.person_form(person, genderChoices, "Email already registered.");
        }

        // Update entity
        person.firstName = firstName != null ? firstName.trim() : null;
        person.lastName = lastName != null ? lastName.trim() : null;
        person.email = email.trim().toLowerCase();
        person.phone = phone != null ? phone.trim() : null;
        person.dateOfBirth = dateOfBirth;
        person.updatedBy = username;

        if (genderId != null) {
            person.gender = Gender.findById(genderId);
        } else {
            person.gender = null;
        }
        // Managed entity - no explicit persist needed

        String displayName = person.getDisplayName();
        return Partials.person_success("Person '" + displayName + "' was updated successfully!");
    }

    // DELETE
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance delete(@PathParam("id") Long id) {
        Person person = Person.findById(id);

        if (person == null) {
            throw new NotFoundException("Person not found");
        }

        String displayName = person.getDisplayName();

        // Cascade deletes UserLogin if exists (via orphanRemoval)
        person.delete();

        return Partials.person_success("Person '" + displayName + "' was deleted successfully!");
    }

    // Helper: detect HTMX requests
    private boolean isHtmxRequest(HttpHeaders headers) {
        return headers.getHeaderString("HX-Request") != null;
    }

    // Helper: validate email format
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }
}
```

**Key Patterns**:
- `Templates` class for full page renders (direct navigation)
- `Partials` class with `basePath = "partials"` for HTMX fragment updates
- `isHtmxRequest()` helper checks for `HX-Request` header
- LIST returns table partial for filter updates, full page for direct access
- CREATE/EDIT forms return partials for modal or inline display
- Returns `TemplateInstance` directly (no `Response.seeOther()`)
- Success/error partials for in-place feedback after mutations

---

### 4.4 AuthResource (Authentication)

**File**: `router/AuthResource.java`

```java
@Path("/")
public class AuthResource {

    @Inject
    PasswordService passwordService;

    @Inject
    io.vertx.ext.web.RoutingContext routingContext;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login(
            String title,
            String currentPage,
            String userName,
            String error
        );

        public static native TemplateInstance signup(
            String title,
            String currentPage,
            String userName,
            String error
        );

        public static native TemplateInstance logout(
            String title,
            String currentPage,
            String userName
        );
    }

    // LOGIN PAGE
    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance loginPage(@QueryParam("error") String error) {
        String errorMessage = null;
        if ("true".equals(error)) {
            errorMessage = "Invalid username or password.";
        }
        return Templates.login("Login", "login", null, errorMessage);
    }

    // SIGNUP PAGE
    @GET
    @Path("/signup")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance signupPage(@QueryParam("error") String error) {
        String errorMessage = mapSignupError(error);
        return Templates.signup("Sign Up", "signup", null, errorMessage);
    }

    // SIGNUP SUBMIT
    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response signup(
            @FormParam("username") String username,
            @FormParam("email") String email,
            @FormParam("password") String password) {

        // Validation: username required
        if (username == null || username.trim().isEmpty()) {
            return Response.seeOther(URI.create("/signup?error=username_required")).build();
        }

        // Validation: username min length
        if (username.trim().length() < 3) {
            return Response.seeOther(URI.create("/signup?error=username_short")).build();
        }

        // Validation: email required
        if (email == null || email.trim().isEmpty()) {
            return Response.seeOther(URI.create("/signup?error=email_required")).build();
        }

        // Validation: password required
        if (password == null || password.isEmpty()) {
            return Response.seeOther(URI.create("/signup?error=password_required")).build();
        }

        // Validation: password min length
        if (password.length() < 8) {
            return Response.seeOther(URI.create("/signup?error=password_short")).build();
        }

        // Validation: password max length (NIST SP 800-63B-4)
        if (password.length() > 64) {
            return Response.seeOther(URI.create("/signup?error=password_long")).build();
        }

        // Check for duplicate username (case-insensitive)
        if (UserLogin.findByUsername(username.trim()) != null) {
            return Response.seeOther(URI.create("/signup?error=username_exists")).build();
        }

        // Check for duplicate email (case-insensitive)
        if (Person.findByEmail(email.trim()) != null) {
            return Response.seeOther(URI.create("/signup?error=email_exists")).build();
        }

        // Create Person
        Person person = new Person();
        person.email = email.trim().toLowerCase();
        person.createdBy = "system";
        person.updatedBy = "system";
        person.persist();

        // Create UserLogin with hashed password
        UserLogin userLogin = new UserLogin();
        userLogin.person = person;
        userLogin.username = username.trim().toLowerCase();
        userLogin.passwordHash = passwordService.hashPassword(password);
        userLogin.role = "user";
        userLogin.createdBy = "system";
        userLogin.updatedBy = "system";
        userLogin.persist();

        // Link back (bidirectional)
        person.userLogin = userLogin;

        return Response.seeOther(URI.create("/login")).build();
    }

    // LOGOUT PAGE
    @GET
    @Path("/logout")
    @Produces(MediaType.TEXT_HTML)
    public Response logoutPage() {
        // Destroy session
        if (routingContext.session() != null) {
            routingContext.session().destroy();
        }

        // Clear authentication cookie
        NewCookie clearCookie = new NewCookie.Builder("quarkus-credential")
                .value("")
                .path("/")
                .maxAge(0)
                .build();

        return Response.ok(Templates.logout("Logged Out", "logout", null))
                .cookie(clearCookie)
                .build();
    }

    private String mapSignupError(String error) {
        if (error == null) return null;
        return switch (error) {
            case "username_required" -> "Username is required.";
            case "username_short" -> "Username must be at least 3 characters.";
            case "email_required" -> "Email is required.";
            case "password_required" -> "Password is required.";
            case "password_short" -> "Password must be at least 8 characters.";
            case "password_long" -> "Password must be 64 characters or less.";
            case "username_exists" -> "Username already exists.";
            case "email_exists" -> "Email already registered.";
            default -> "An error occurred. Please try again.";
        };
    }
}
```

---

## 5. Template System

### 5.1 Template Location Convention

Templates are located based on resource class name:

| Resource Class | Template Location |
|----------------|-------------------|
| `IndexResource` | `templates/IndexResource/` |
| `GenderResource` | `templates/GenderResource/` |
| `PersonResource` | `templates/PersonResource/` |
| `AuthResource` | `templates/AuthResource/` |
| **Partials** | `templates/partials/` |

**Partials Directory**: Templates in `templates/partials/` are used for HTMX fragment updates. They are referenced using `@CheckedTemplate(basePath = "partials")` in resource classes.

```
src/main/resources/templates/
├── base.html                          # Base layout
├── error.html                         # Error page
├── IndexResource/
│   └── index.html                     # Home page (full page)
├── GenderResource/
│   └── gender.html                    # Gender list (full page)
├── PersonResource/
│   └── persons.html                   # Persons list (full page)
├── AuthResource/
│   ├── login.html                     # Login page
│   ├── signup.html                    # Signup page
│   └── logout.html                    # Logout confirmation
└── partials/                          # HTMX fragment templates
    ├── gender_table.html              # Gender table partial
    ├── gender_form.html               # Gender create/edit form partial
    ├── gender_success.html            # Gender success message partial
    ├── gender_error.html              # Gender error message partial
    ├── persons_table.html             # Persons table partial
    ├── person_form.html               # Person create/edit form partial
    ├── person_success.html            # Person success message partial
    └── person_error.html              # Person error message partial
```

### 5.2 Base Template (Layout)

**File**: `templates/base.html`

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
    <link rel="icon" href="/favicon.ico" type="image/x-icon"/>

    <!-- UIkit CSS (CDN) -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/uikit@3.25/dist/css/uikit.min.css"/>

    <!-- UIkit JS (CDN) -->
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.25/dist/js/uikit.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.25/dist/js/uikit-icons.min.js"></script>

    <!-- HTMX 2.0.8 (CDN with SRI) -->
    <script
        src="https://cdn.jsdelivr.net/npm/htmx.org@2.0.8/dist/htmx.min.js"
        integrity="sha384-/TgkGk7p307TH7EXJDuUlgG3Ce1UVolAOFopFekQkkXihi5u/6OCvVKyz1W+idaz"
        crossorigin="anonymous"
    ></script>

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
                        <img src="/img/logo-scaffold.png" width="40" height="40" alt="HX-Qute Logo"/>
                        <span class="uk-margin-small-left uk-text-bold uk-text-large">HX-Qute</span>
                    </a>
                </div>

                <!-- Navigation -->
                <ul class="uk-nav uk-nav-default uk-padding-small" uk-nav>
                    <li class="{#if currentPage == 'home'}uk-active{/if}">
                        <a href="/">
                            <span uk-icon="icon: home; ratio: 1.2"></span>
                            <span class="uk-margin-small-left">Home</span>
                        </a>
                    </li>
                    <li class="{#if currentPage == 'persons'}uk-active{/if}">
                        <a href="/persons">
                            <span uk-icon="icon: users; ratio: 1.2"></span>
                            <span class="uk-margin-small-left">Persons</span>
                        </a>
                    </li>
                    <li class="uk-parent {#if currentPage == 'gender'}uk-open{/if}">
                        <a href="#">
                            <span uk-icon="icon: settings; ratio: 1.2"></span>
                            <span class="uk-margin-small-left">Maintenance</span>
                        </a>
                        <ul class="uk-nav-sub">
                            <li class="{#if currentPage == 'gender'}uk-active{/if}">
                                <a href="/genders">
                                    <span uk-icon="icon: users; ratio: 1"></span>
                                    <span class="uk-margin-small-left">Gender</span>
                                </a>
                            </li>
                        </ul>
                    </li>
                    <li>
                        {#if userName}
                        <a href="/logout">
                            <span uk-icon="icon: sign-out; ratio: 1.2"></span>
                            <span class="uk-margin-small-left">Logout ({userName})</span>
                        </a>
                        {#else}
                        <a href="/login">
                            <span uk-icon="icon: sign-in; ratio: 1.2"></span>
                            <span class="uk-margin-small-left">Login</span>
                        </a>
                        {/if}
                    </li>
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
</div>
</body>
</html>
```

**Key Patterns**:
- `{@String variableName}` - Type declarations for template parameters
- `{#insert}default{/}` - Content insertion point
- `{#if currentPage == 'xyz'}uk-active{/if}` - Active page highlighting
- `{userName??}` - Null-safe access

### 5.3 Page Template (Full Page with Partial Include)

**File**: `templates/PersonResource/persons.html`

Full page templates include the base layout and embed a partial for the dynamic content area. This allows the same table content to be rendered as a full page (direct navigation) or as a partial (HTMX update).

```html
{@String title}
{@String currentPage}
{@String userName}
{@java.util.List<io.archton.scaffold.entity.Person> persons}
{@String filterText}
{@java.util.List<io.archton.scaffold.entity.Gender> genderChoices}

{#include base}

<h1 class="uk-heading-small">Persons</h1>

<!-- Filter Form - targets the table container for partial updates -->
<div class="uk-margin">
    <form class="uk-grid-small" uk-grid
          hx-get="/persons"
          hx-target="#persons-table-container"
          hx-push-url="true">
        <div class="uk-width-1-2@s">
            <input class="uk-input" type="text" name="filter"
                   placeholder="Search by name..." value="{filterText ?: ''}"/>
        </div>
        <div class="uk-width-auto@s">
            <button type="submit" class="uk-button uk-button-primary">Filter</button>
            <a href="/persons" class="uk-button uk-button-default"
               hx-get="/persons" hx-target="#persons-table-container">Clear</a>
        </div>
    </form>
</div>

<!-- Add Button - opens form in modal -->
<div class="uk-margin">
    <button class="uk-button uk-button-primary"
            hx-get="/persons/create"
            hx-target="#modal-container"
            hx-trigger="click">
        Add Person
    </button>
</div>

<!-- Table Container - swapped by HTMX on filter/CRUD operations -->
<div id="persons-table-container">
    {#include partials/persons_table persons=persons filterText=filterText /}
</div>

<!-- Modal Container - for create/edit forms -->
<div id="modal-container"></div>

{/include}
```

**Key Patterns**:
- `hx-target="#persons-table-container"` targets the table for partial updates
- `{#include partials/persons_table ... /}` embeds the partial in full page render
- Modal container receives form partials for create/edit operations

---

### 5.4 Partial Templates

Partial templates are HTML fragments without the base layout. They are returned for HTMX requests to update specific page sections.

#### 5.4.1 Table Partial

**File**: `templates/partials/persons_table.html`

```html
{@java.util.List<io.archton.scaffold.entity.Person> persons}
{@String filterText}

{#if persons.isEmpty}
<p class="uk-text-muted">No persons found.</p>
{#else}
<table class="uk-table uk-table-striped uk-table-hover">
    <thead>
        <tr>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Date of Birth</th>
            <th>Gender</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        {#for person in persons}
        <tr>
            <td>{person.firstName ?: ''}</td>
            <td>{person.lastName ?: ''}</td>
            <td>{person.email}</td>
            <td>{person.phone ?: ''}</td>
            <td>{person.dateOfBirth}</td>
            <td>{person.gender.description ?: ''}</td>
            <td>
                <button class="uk-button uk-button-small uk-button-default"
                        hx-get="/persons/{person.id}/edit"
                        hx-target="#modal-container">
                    Edit
                </button>
                <button class="uk-button uk-button-small uk-button-danger"
                        hx-delete="/persons/{person.id}"
                        hx-confirm="Are you sure you want to delete this person?"
                        hx-target="#persons-table-container">
                    Delete
                </button>
            </td>
        </tr>
        {/for}
    </tbody>
</table>
{/if}
```

#### 5.4.2 Form Partial (Modal)

**File**: `templates/partials/person_form.html`

```html
{@io.archton.scaffold.entity.Person person}
{@java.util.List<io.archton.scaffold.entity.Gender> genderChoices}
{@String error}

<div id="person-modal" uk-modal class="uk-open">
    <div class="uk-modal-dialog uk-modal-body">
        <button class="uk-modal-close-default" type="button" uk-close></button>

        <h2 class="uk-modal-title">{#if person}Edit Person{#else}Create Person{/if}</h2>

        {#if error}
        <div class="uk-alert uk-alert-danger" uk-alert>
            <a class="uk-alert-close" uk-close></a>
            <p>{error}</p>
        </div>
        {/if}

        <form hx-post="{#if person}/persons/{person.id}/update{#else}/persons/create{/if}"
              hx-target="#modal-container"
              class="uk-form-stacked">

            <div class="uk-margin">
                <label class="uk-form-label" for="firstName">First Name</label>
                <input class="uk-input" type="text" id="firstName" name="firstName"
                       value="{person.firstName ?: ''}"/>
            </div>

            <div class="uk-margin">
                <label class="uk-form-label" for="lastName">Last Name</label>
                <input class="uk-input" type="text" id="lastName" name="lastName"
                       value="{person.lastName ?: ''}"/>
            </div>

            <div class="uk-margin">
                <label class="uk-form-label" for="email">Email *</label>
                <input class="uk-input" type="email" id="email" name="email" required
                       value="{person.email ?: ''}"/>
            </div>

            <div class="uk-margin">
                <label class="uk-form-label" for="phone">Phone</label>
                <input class="uk-input" type="tel" id="phone" name="phone"
                       value="{person.phone ?: ''}"/>
            </div>

            <div class="uk-margin">
                <label class="uk-form-label" for="dateOfBirth">Date of Birth</label>
                <input class="uk-input" type="date" id="dateOfBirth" name="dateOfBirth"
                       value="{person.dateOfBirth}"/>
            </div>

            <div class="uk-margin">
                <label class="uk-form-label" for="genderId">Gender</label>
                <select class="uk-select" id="genderId" name="genderId">
                    <option value="">-- Select --</option>
                    {#for gender in genderChoices}
                    <option value="{gender.id}" {#if person?? && person.gender?? && person.gender.id == gender.id}selected{/if}>
                        {gender.description}
                    </option>
                    {/for}
                </select>
            </div>

            <div class="uk-margin uk-text-right">
                <button type="button" class="uk-button uk-button-default uk-modal-close">Cancel</button>
                <button type="submit" class="uk-button uk-button-primary">Save</button>
            </div>
        </form>
    </div>
</div>
```

#### 5.4.3 Success Partial

**File**: `templates/partials/person_success.html`

The success partial triggers a table refresh and displays a notification.

```html
{@String message}

<!-- Close modal and refresh table -->
<div hx-get="/persons"
     hx-target="#persons-table-container"
     hx-trigger="load"
     hx-swap="innerHTML">
</div>

<!-- Display success notification using HX-Trigger header -->
<script>
    // Close the modal
    UIkit.modal('#person-modal')?.hide();
    // Show notification
    UIkit.notification({message: '{message}', status: 'success', pos: 'top-center'});
</script>
```

**Alternative using HX-Trigger header** (set in resource):
```java
return Response.ok(Partials.person_success(message))
    .header("HX-Trigger", "{\"showNotification\": {\"message\": \"" + message + "\", \"type\": \"success\"}}")
    .build();
```

#### 5.4.4 Error Partial

**File**: `templates/partials/person_error.html`

```html
{@String message}

<div class="uk-alert uk-alert-danger" uk-alert>
    <a class="uk-alert-close" uk-close></a>
    <p>{message}</p>
</div>
```

---

### 5.5 Gender Partial Templates

#### 5.5.1 Gender Table Partial

**File**: `templates/partials/gender_table.html`

```html
{@java.util.List<io.archton.scaffold.entity.Gender> genders}
{@String filterText}

{#if genders.isEmpty}
<p class="uk-text-muted">No genders found.</p>
{#else}
<table class="uk-table uk-table-striped uk-table-hover">
    <thead>
        <tr>
            <th>Code</th>
            <th>Description</th>
            <th>Created</th>
            <th>Updated</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        {#for gender in genders}
        <tr>
            <td>{gender.code}</td>
            <td>{gender.description}</td>
            <td>{gender.createdAt}</td>
            <td>{gender.updatedAt}</td>
            <td>
                <button class="uk-button uk-button-small uk-button-default"
                        hx-get="/genders/{gender.id}/edit"
                        hx-target="#modal-container">
                    Edit
                </button>
                <button class="uk-button uk-button-small uk-button-danger"
                        hx-delete="/genders/{gender.id}"
                        hx-confirm="Are you sure you want to delete this gender?"
                        hx-target="#gender-table-container">
                    Delete
                </button>
            </td>
        </tr>
        {/for}
    </tbody>
</table>
{/if}
```

#### 5.5.2 Gender Form Partial

**File**: `templates/partials/gender_form.html`

```html
{@io.archton.scaffold.entity.Gender gender}
{@String error}

<div id="gender-modal" uk-modal class="uk-open">
    <div class="uk-modal-dialog uk-modal-body">
        <button class="uk-modal-close-default" type="button" uk-close></button>

        <h2 class="uk-modal-title">{#if gender}Edit Gender{#else}Create Gender{/if}</h2>

        {#if error}
        <div class="uk-alert uk-alert-danger" uk-alert>
            <a class="uk-alert-close" uk-close></a>
            <p>{error}</p>
        </div>
        {/if}

        <form hx-post="{#if gender}/genders/{gender.id}/update{#else}/genders/create{/if}"
              hx-target="#modal-container"
              class="uk-form-stacked">

            <div class="uk-margin">
                <label class="uk-form-label" for="code">Code *</label>
                <input class="uk-input" type="text" id="code" name="code" required
                       maxlength="7" style="text-transform: uppercase;"
                       value="{gender.code ?: ''}"/>
                <span class="uk-text-muted uk-text-small">Max 7 characters, will be uppercased</span>
            </div>

            <div class="uk-margin">
                <label class="uk-form-label" for="description">Description *</label>
                <input class="uk-input" type="text" id="description" name="description" required
                       value="{gender.description ?: ''}"/>
            </div>

            {#if gender}
            <div class="uk-margin uk-text-muted uk-text-small">
                <p>Created: {gender.createdAt} by {gender.createdBy ?: 'unknown'}</p>
                <p>Updated: {gender.updatedAt} by {gender.updatedBy ?: 'unknown'}</p>
            </div>
            {/if}

            <div class="uk-margin uk-text-right">
                <button type="button" class="uk-button uk-button-default uk-modal-close">Cancel</button>
                <button type="submit" class="uk-button uk-button-primary">Save</button>
            </div>
        </form>
    </div>
</div>
```

#### 5.5.3 Gender Success/Error Partials

**File**: `templates/partials/gender_success.html`

```html
{@String message}

<div hx-get="/genders"
     hx-target="#gender-table-container"
     hx-trigger="load"
     hx-swap="innerHTML">
</div>

<script>
    UIkit.modal('#gender-modal')?.hide();
    UIkit.notification({message: '{message}', status: 'success', pos: 'top-center'});
</script>
```

**File**: `templates/partials/gender_error.html`

```html
{@String message}

<div class="uk-alert uk-alert-danger" uk-alert>
    <a class="uk-alert-close" uk-close></a>
    <p>{message}</p>
</div>
```

---

### 5.6 Login Template

**File**: `templates/AuthResource/login.html`

```html
{@String title}
{@String currentPage}
{@String userName}
{@String error}

{#include base}

<div class="uk-flex uk-flex-center uk-flex-middle" style="min-height: 60vh;">
    <div class="uk-card uk-card-default uk-card-body uk-width-1-2@m">
        <h2 class="uk-card-title">Login</h2>

        {#if error}
        <div class="uk-alert uk-alert-danger" uk-alert>
            <a class="uk-alert-close" uk-close></a>
            <p>{error}</p>
        </div>
        {/if}

        <form action="/j_security_check" method="POST" class="uk-form-stacked">
            <div class="uk-margin">
                <label class="uk-form-label" for="j_username">Username</label>
                <input class="uk-input" type="text" id="j_username" name="j_username"
                       placeholder="Enter username" required/>
            </div>

            <div class="uk-margin">
                <label class="uk-form-label" for="j_password">Password</label>
                <input class="uk-input" type="password" id="j_password" name="j_password"
                       placeholder="Enter password" required/>
            </div>

            <div class="uk-margin">
                <button type="submit" class="uk-button uk-button-primary">Login</button>
            </div>
        </form>

        <p class="uk-text-small">
            Don't have an account? <a href="/signup">Sign up</a>
        </p>
    </div>
</div>

{/include}
```

**Critical**: Form must POST to `/j_security_check` with fields named `j_username` and `j_password`.

### 5.7 Signup Template

**File**: `templates/AuthResource/signup.html`

```html
{@String title}
{@String currentPage}
{@String userName}
{@String error}

{#include base}

<div class="uk-flex uk-flex-center uk-flex-middle" style="min-height: 60vh;">
    <div class="uk-card uk-card-default uk-card-body uk-width-1-2@m">
        <h2 class="uk-card-title">Sign Up</h2>

        {#if error}
        <div class="uk-alert uk-alert-danger" uk-alert>
            <a class="uk-alert-close" uk-close></a>
            <p>{error}</p>
        </div>
        {/if}

        <form action="/signup" method="POST" class="uk-form-stacked">
            <div class="uk-margin">
                <label class="uk-form-label" for="username">Username</label>
                <input class="uk-input" type="text" id="username" name="username"
                       placeholder="Enter username (min 3 characters)" required/>
            </div>

            <div class="uk-margin">
                <label class="uk-form-label" for="email">Email</label>
                <input class="uk-input" type="email" id="email" name="email"
                       placeholder="Enter email" required/>
            </div>

            <div class="uk-margin">
                <label class="uk-form-label" for="password">Password</label>
                <input class="uk-input" type="password" id="password" name="password"
                       placeholder="Enter password (min 8 characters)" required/>
            </div>

            <div class="uk-margin">
                <button type="submit" class="uk-button uk-button-primary">Sign Up</button>
            </div>
        </form>

        <p class="uk-text-small">
            Already have an account? <a href="/login">Login</a>
        </p>
    </div>
</div>

{/include}
```

**Key Elements**:
- Input id: `username`, `email`, `password` (for test case TC-1.01)
- Button text: "Sign Up"
- Link to login page with text "Login"

---

## 6. HTMX Integration Patterns

### 6.1 Core HTMX Attributes

| Attribute | Purpose | Example |
|-----------|---------|---------|
| `hx-get` | GET request | `hx-get="/persons"` |
| `hx-post` | POST request | `hx-post="/persons/create"` |
| `hx-delete` | DELETE request | `hx-delete="/persons/{id}"` |
| `hx-target` | Element to update | `hx-target="#persons-table-container"` |
| `hx-swap` | How to swap content | `hx-swap="outerHTML"`, `hx-swap="innerHTML"` |
| `hx-push-url` | Update browser URL | `hx-push-url="true"` |
| `hx-confirm` | Confirmation dialog | `hx-confirm="Are you sure?"` |
| `hx-trigger` | When to trigger request | `hx-trigger="load"`, `hx-trigger="click"` |
| `hx-indicator` | Loading indicator | `hx-indicator="#spinner"` |
| `hx-include` | Include form values | `hx-include="#filterform"` |

### 6.2 Full Page vs Partial Response Pattern

The server detects HTMX requests via the `HX-Request` header and returns appropriate content:

```java
// In resource class
private boolean isHtmxRequest(HttpHeaders headers) {
    return headers.getHeaderString("HX-Request") != null;
}

@GET
@Produces(MediaType.TEXT_HTML)
public TemplateInstance list(@Context HttpHeaders headers) {
    List<Person> persons = Person.listAllOrdered();

    // Return partial for HTMX requests, full page otherwise
    if (isHtmxRequest(headers)) {
        return Partials.persons_table(persons, filterText);
    }
    return Templates.persons("Persons", "persons", username, persons, filterText, genderChoices);
}
```

### 6.3 Filter Form with Partial Updates

```html
<!-- Filter targets just the table container, not the whole page -->
<form hx-get="/persons"
      hx-target="#persons-table-container"
      hx-push-url="true"
      class="uk-grid-small" uk-grid>
    <div class="uk-width-1-2@s">
        <input class="uk-input" type="text" name="filter"
               placeholder="Search by name..." value="{filterText ?: ''}"/>
    </div>
    <div class="uk-width-auto@s">
        <button type="submit" class="uk-button uk-button-primary">Filter</button>
        <a class="uk-button uk-button-default"
           hx-get="/persons"
           hx-target="#persons-table-container">Clear</a>
    </div>
</form>

<!-- Table container - only this element is swapped on filter -->
<div id="persons-table-container">
    {#include partials/persons_table ... /}
</div>
```

### 6.4 Modal Forms with HTMX

**Opening a form in a modal:**
```html
<button class="uk-button uk-button-primary"
        hx-get="/persons/create"
        hx-target="#modal-container"
        hx-trigger="click">
    Add Person
</button>

<!-- Modal container - receives form partial -->
<div id="modal-container"></div>
```

**Form partial with HTMX submit:**
```html
<div id="person-modal" uk-modal class="uk-open">
    <div class="uk-modal-dialog uk-modal-body">
        <form hx-post="/persons/create"
              hx-target="#modal-container"
              class="uk-form-stacked">
            <!-- form fields -->
            <button type="submit" class="uk-button uk-button-primary">Save</button>
        </form>
    </div>
</div>
```

### 6.5 Success Response with Table Refresh

After a successful create/update/delete, the server returns a success partial that:
1. Triggers a table refresh
2. Closes the modal
3. Shows a notification

```html
{@String message}

<!-- Trigger table refresh on load -->
<div hx-get="/persons"
     hx-target="#persons-table-container"
     hx-trigger="load"
     hx-swap="innerHTML">
</div>

<script>
    // Close the modal
    UIkit.modal('#person-modal')?.hide();
    // Show notification
    UIkit.notification({message: '{message}', status: 'success', pos: 'top-center'});
</script>
```

### 6.6 Delete with Confirmation

```html
<button class="uk-button uk-button-small uk-button-danger"
        hx-delete="/persons/{person.id}"
        hx-confirm="Are you sure you want to delete this person?"
        hx-target="#persons-table-container">
    Delete
</button>
```

**Server returns success partial** which refreshes the table and shows notification.

### 6.7 Loading Indicator

```html
<form hx-post="/persons/create"
      hx-target="#modal-container"
      hx-indicator="#spinner">
    <!-- form fields -->
</form>

<div id="spinner" class="htmx-indicator">
    <div uk-spinner></div>
</div>
```

### 6.8 Out-of-Band (OOB) Swaps

For updating multiple page elements in a single response:

```html
<!-- Main response content -->
<div id="persons-table-container">
    <!-- Updated table content -->
</div>

<!-- OOB swap - also update notification area -->
<div id="notification-area" hx-swap-oob="true">
    <div class="uk-alert uk-alert-success">Person created successfully!</div>
</div>
```

### 6.9 HX-Trigger Header for Events

Server can trigger client-side events via response header:

```java
return Response.ok(templateInstance)
    .header("HX-Trigger", "personCreated")
    .build();
```

Client listens for the event:
```html
<div hx-get="/persons"
     hx-target="#persons-table-container"
     hx-trigger="personCreated from:body">
</div>
```

---

## 7. Security Architecture

### 7.1 Quarkus Security JPA Configuration

**Entity Annotations**:
```java
@Entity
@UserDefinition  // Marks as security entity
public class UserLogin extends PanacheEntity {

    @Username      // Identifies username field
    public String username;

    @Password(value = PasswordType.MCF)  // BCrypt format
    public String passwordHash;

    @Roles         // Identifies roles field
    public String role;
}
```

**Configuration** (`application.properties`):
```properties
# Form-based Authentication
quarkus.http.auth.form.enabled=true
quarkus.http.auth.form.login-page=/login
quarkus.http.auth.form.error-page=/login?error=true
quarkus.http.auth.form.landing-page=/
quarkus.http.auth.form.post-location=/j_security_check
quarkus.http.auth.form.cookie-name=quarkus-credential
quarkus.http.auth.form.timeout=PT30M

# HTTP Permissions
quarkus.http.auth.permission.authenticated.paths=/persons,/persons/*,/genders,/genders/*
quarkus.http.auth.permission.authenticated.policy=authenticated
quarkus.http.auth.permission.public.paths=/,/login,/logout,/signup,/css/*,/images/*,/img/*,/style.css,/j_security_check
quarkus.http.auth.permission.public.policy=permit
```

### 7.2 Custom Identity Provider (Case-Insensitive Login)

**File**: `security/CaseInsensitiveIdentityProvider.java`

```java
@ApplicationScoped
@Priority(1)  // Higher priority than default JPA provider
public class CaseInsensitiveIdentityProvider
        implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(
            UsernamePasswordAuthenticationRequest request,
            AuthenticationRequestContext context) {
        return context.runBlocking(() -> authenticateBlocking(request));
    }

    @Transactional
    protected SecurityIdentity authenticateBlocking(
            UsernamePasswordAuthenticationRequest request) {

        // Normalize username
        String username = request.getUsername().toLowerCase().trim();

        UserLogin userLogin = UserLogin.findByUsername(username);
        if (userLogin == null) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        // Verify password with BCrypt
        String password = new String(request.getPassword().getPassword());
        if (!BcryptUtil.matches(password, userLogin.passwordHash)) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        // Build security identity
        return QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(userLogin.username))
                .addRole(userLogin.role)
                .build();
    }
}
```

### 7.3 Password Hashing Service

**File**: `service/PasswordService.java`

```java
@ApplicationScoped
public class PasswordService {

    private static final int BCRYPT_COST = 12;

    public String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword, BCRYPT_COST);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}
```

### 7.4 Resource-Level Security

```java
@Path("/genders")
@RolesAllowed("admin")  // Admin only
public class GenderResource { }

@Path("/persons")
@RolesAllowed({"user", "admin"})  // All authenticated users
public class PersonResource { }
```

---

## 8. Query Filtering Pattern

### 8.1 Simple Filter in Resource

```java
@GET
@Produces(MediaType.TEXT_HTML)
public TemplateInstance list(@QueryParam("filter") String filterText) {
    List<Person> persons;
    if (filterText != null && !filterText.trim().isEmpty()) {
        persons = Person.findByNameContaining(filterText.trim());
    } else {
        persons = Person.listAllOrdered();
    }
    return Templates.persons(..., persons, filterText, ...);
}
```

### 8.2 Entity Finder Method

```java
public static List<Person> findByNameContaining(String searchText) {
    String pattern = "%" + searchText.toLowerCase() + "%";
    return list("LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1", pattern);
}
```

### 8.3 Session-Based Filter Persistence

To persist filter criteria during the user's session, use Vert.x session storage:

```java
@Path("/persons")
@RolesAllowed({"user", "admin"})
public class PersonResource {

    @Inject
    io.vertx.ext.web.RoutingContext routingContext;

    private static final String FILTER_SESSION_KEY = "persons.filter";

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@QueryParam("filter") String filterText,
                                  @QueryParam("clear") String clear) {
        String username = securityIdentity.getPrincipal().getName();

        // Handle clear action
        if ("true".equals(clear)) {
            routingContext.session().remove(FILTER_SESSION_KEY);
            filterText = null;
        }

        // If no filter provided, check session for persisted filter
        if (filterText == null) {
            filterText = routingContext.session().get(FILTER_SESSION_KEY);
        } else {
            // Persist new filter to session
            routingContext.session().put(FILTER_SESSION_KEY, filterText.trim());
        }

        List<Person> persons;
        if (filterText != null && !filterText.trim().isEmpty()) {
            persons = Person.findByNameContaining(filterText.trim());
        } else {
            persons = Person.listAllOrdered();
        }

        List<Gender> genderChoices = Gender.listAllOrdered();

        return Templates.persons(
            "Persons", "persons", username,
            persons, filterText, genderChoices
        );
    }
}
```

**Template Update** (Clear button with session clear):
```html
<form hx-get="/persons" hx-target="#main-content" hx-push-url="true">
    <input type="text" name="filter" value="{filterText ?: ''}"/>
    <button type="submit">Filter</button>
    <a href="/persons?clear=true" class="uk-button uk-button-default"
       hx-get="/persons?clear=true" hx-target="#main-content">Clear</a>
</form>
```

**Key Points**:
- Filter persists across page navigations within the session
- `?clear=true` parameter removes the session filter
- Session storage is tied to the user's authenticated session
- Filter is URL-shareable when passed as query parameter

---

## 9. Startup and Data Initialization

### 9.1 Flyway Migrations

Database schema is managed via Flyway migrations in `src/main/resources/db/migration/`.

**Naming Convention**: `V{major}.{minor}.{patch}__{Description}.sql`

**Example**: `V1.0.0__Create_gender_table.sql`

```sql
CREATE TABLE gender (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(7) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_gender_code UNIQUE (code),
    CONSTRAINT uq_gender_description UNIQUE (description)
);

CREATE INDEX idx_gender_code ON gender(code);
```

### 9.2 Default Admin User

Create via migration `V1.2.1__Insert_admin_user.sql`:

```sql
-- Insert admin Person
INSERT INTO person (email, first_name, last_name, created_at, updated_at, created_by, updated_by, active)
VALUES ('admin@example.com', 'Admin', 'User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', true);

-- Insert admin UserLogin (password: Admin@01, BCrypt hash)
INSERT INTO user_login (person_id, username, password_hash, role, created_at, updated_at, created_by, updated_by, mfa_enabled, failed_attempts)
SELECT id, 'admin', '$2a$12$[hash]', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', false, 0
FROM person WHERE email = 'admin@example.com';
```

---

## 10. Configuration

### 10.1 application.properties

```properties
# Application
quarkus.http.port=9080

# PostgreSQL
quarkus.datasource.db-kind=postgresql

# Flyway Migrations
quarkus.flyway.migrate-at-start=true
quarkus.hibernate-orm.schema-management.strategy=none

# Form-based Authentication
quarkus.http.auth.form.enabled=true
quarkus.http.auth.form.login-page=/login
quarkus.http.auth.form.error-page=/login?error=true
quarkus.http.auth.form.landing-page=/
quarkus.http.auth.form.post-location=/j_security_check
quarkus.http.auth.form.cookie-name=quarkus-credential
quarkus.http.auth.form.timeout=PT30M

# HTTP Permissions
quarkus.http.auth.permission.authenticated.paths=/persons,/persons/*,/genders,/genders/*
quarkus.http.auth.permission.authenticated.policy=authenticated
quarkus.http.auth.permission.public.paths=/,/login,/logout,/signup,/css/*,/images/*,/img/*,/style.css,/j_security_check
quarkus.http.auth.permission.public.policy=permit

# Console styling
quarkus.log.console.darken=1

# Custom banner
quarkus.banner.enabled=true
quarkus.banner.path=banner.txt
```

---

## 11. Static Resources

### 11.1 Location

Static files go in `src/main/resources/META-INF/resources/` and are served at the root path.

| File Path | URL |
|-----------|-----|
| `META-INF/resources/style.css` | `/style.css` |
| `META-INF/resources/favicon.ico` | `/favicon.ico` |
| `META-INF/resources/img/logo.png` | `/img/logo.png` |

### 11.2 CDN vs Local

| Resource | Approach | Reason |
|----------|----------|--------|
| HTMX | CDN (SRI) | Subresource Integrity provides security |
| UIkit CSS/JS | CDN | Common library, caching benefits |
| Custom CSS | Local | Project-specific styles |

---

## 12. Common Patterns Reference

### 12.1 Getting Current User

```java
@Inject
SecurityIdentity securityIdentity;

private String getCurrentUsername() {
    return securityIdentity.isAnonymous()
        ? null
        : securityIdentity.getPrincipal().getName();
}

private Person getCurrentPerson() {
    String username = getCurrentUsername();
    if (username == null) return null;
    UserLogin userLogin = UserLogin.findByUsername(username);
    return userLogin != null ? userLogin.person : null;
}
```

### 12.2 Validation Error Handling

```java
@POST
@Path("/create")
@Transactional
public Response create(@FormParam("email") String email) {
    if (email == null || email.trim().isEmpty()) {
        return Response.ok(Templates.form(..., "Email is required.")).build();
    }
    // ... create entity
    return Response.seeOther(URI.create("/persons")).build();
}
```

### 12.3 Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Entity class | PascalCase singular | `Person` |
| Table name | snake_case | `person` |
| Entity field | camelCase | `firstName` |
| Column name | snake_case | `first_name` |
| Resource class | PascalCase + Resource | `PersonResource` |
| URL path | lowercase plural | `/persons` |
| Template file | camelCase.html | `personForm.html` |

---

## 13. Use Case Traceability Matrix

This section maps each use case from [USE-CASES.md](USE-CASES.md) to the technical components that implement it.

### 13.1 Authentication Use Cases (UC-1.x)

| Use Case | Components | Templates | Routes |
|----------|------------|-----------|--------|
| **UC-1.1: Display Signup Page** | `AuthResource.signupPage()` | `AuthResource/signup.html` | GET `/signup` |
| **UC-1.2: Register New User** | `AuthResource.signup()`, `Person` entity, `UserLogin` entity, `PasswordService` | `AuthResource/signup.html` | POST `/signup` |
| **UC-1.3: Display Login Page** | `AuthResource.loginPage()` | `AuthResource/login.html` | GET `/login` |
| **UC-1.4: Authenticate User** | `CaseInsensitiveIdentityProvider`, Quarkus form auth | `AuthResource/login.html` | POST `/j_security_check` |
| **UC-1.5: Logout User** | `AuthResource.logoutPage()`, session destruction | `AuthResource/logout.html` | GET `/logout` |
| **UC-1.6: Access Protected Route** | `quarkus.http.auth.permission.*` configuration | N/A | N/A |

### 13.2 Gender Management Use Cases (UC-2.x)

| Use Case | Components | Templates | Routes |
|----------|------------|-----------|--------|
| **UC-2.1: View Gender List** | `GenderResource.list()`, `Gender.listAllOrdered()` | `GenderResource/gender.html` | GET `/genders` |
| **UC-2.2: Create Gender** | `GenderResource.createForm()`, `GenderResource.create()` | `GenderResource/genderForm.html` | GET/POST `/genders/create` |
| **UC-2.3: Edit Gender** | `GenderResource.editForm()`, `GenderResource.update()` | `GenderResource/genderForm.html` | GET `/genders/{id}/edit`, POST `/genders/{id}/update` |
| **UC-2.4: Delete Gender** | `GenderResource.delete()`, HTMX `hx-confirm` | N/A | DELETE `/genders/{id}` |

### 13.3 Persons Management Use Cases (UC-3.x)

| Use Case | Components | Templates | Routes |
|----------|------------|-----------|--------|
| **UC-3.1: View Persons List** | `PersonResource.list()`, `Person.listAllOrdered()` | `PersonResource/persons.html` | GET `/persons` |
| **UC-3.2: Create Person** | `PersonResource.createForm()`, `PersonResource.create()` | `PersonResource/personForm.html` | GET/POST `/persons/create` |
| **UC-3.3: Edit Person** | `PersonResource.editForm()`, `PersonResource.update()` | `PersonResource/personForm.html` | GET `/persons/{id}/edit`, POST `/persons/{id}/update` |
| **UC-3.4: Delete Person** | `PersonResource.delete()`, cascade to UserLogin | N/A | DELETE `/persons/{id}` |
| **UC-3.5: Filter Persons** | `PersonResource.list()` with `@QueryParam`, `Person.findByNameContaining()` | `PersonResource/persons.html` | GET `/persons?filter=text` |

### 13.4 Cross-Cutting Concerns

| Concern | Implementation | Use Cases Affected |
|---------|----------------|-------------------|
| **Authentication** | `@RolesAllowed`, `quarkus.http.auth.permission.*` | UC-2.x, UC-3.x |
| **Authorization** | `@RolesAllowed("admin")` for Gender | UC-2.x |
| **Validation** | Server-side checks, HTML `required` | UC-1.2, UC-2.2, UC-2.3, UC-3.2, UC-3.3 |
| **Audit Fields** | `createdBy`, `updatedBy`, `createdAt`, `updatedAt` | All entities |
| **Password Security** | `PasswordService`, BCrypt cost 12 | UC-1.2, UC-1.4 |

---

## Appendix A: URL Routes Summary

| URL | Method | Handler | Auth Required | Role |
|-----|--------|---------|---------------|------|
| `/` | GET | IndexResource.index() | No | - |
| `/login` | GET | AuthResource.loginPage() | No | - |
| `/j_security_check` | POST | Quarkus form auth | No | - |
| `/signup` | GET | AuthResource.signupPage() | No | - |
| `/signup` | POST | AuthResource.signup() | No | - |
| `/logout` | GET | AuthResource.logoutPage() | No | - |
| `/persons` | GET | PersonResource.list() | Yes | user, admin |
| `/persons/create` | GET | PersonResource.createForm() | Yes | user, admin |
| `/persons/create` | POST | PersonResource.create() | Yes | user, admin |
| `/persons/{id}/edit` | GET | PersonResource.editForm() | Yes | user, admin |
| `/persons/{id}/update` | POST | PersonResource.update() | Yes | user, admin |
| `/persons/{id}` | DELETE | PersonResource.delete() | Yes | user, admin |
| `/genders` | GET | GenderResource.list() | Yes | admin |
| `/genders/create` | GET | GenderResource.createForm() | Yes | admin |
| `/genders/create` | POST | GenderResource.create() | Yes | admin |
| `/genders/{id}/edit` | GET | GenderResource.editForm() | Yes | admin |
| `/genders/{id}/update` | POST | GenderResource.update() | Yes | admin |
| `/genders/{id}` | DELETE | GenderResource.delete() | Yes | admin |

---

*Document Version: 2.1*
*Last Updated: December 2025*

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | December 2024 | Developer | Initial release (from HX Finance App) |
| 2.0 | December 2024 | Senior Developer | Complete rewrite for HX Qute: removed Transaction/Category/Charts, added Gender/Person/Auth aligned with USE-CASES.md, updated package to io.archton.scaffold, CDN-based assets, Phase 1 scope |
| 2.1 | December 2025 | Senior Developer | Added: password max length validation (NIST), session-based filter persistence, signup.html template, UIKit 3.25, standardized error punctuation |
