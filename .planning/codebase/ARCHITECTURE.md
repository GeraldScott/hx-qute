# Architecture

**Analysis Date:** 2026-02-14

## Pattern Overview

**Overall:** Layered Hypermedia-Driven Application (HDA) with server-side rendering

**Key Characteristics:**
- All HTML rendered server-side via Qute templates, never client-side
- REST endpoints return `TemplateInstance` objects directly, not JSON
- HTMX handles partial page updates without full-page reloads
- Fragment-based UI architecture with compile-time template validation
- Type-safe repository pattern with Hibernate Panache ORM
- Form-based authentication with BCrypt password hashing
- Transactional service layer manages business logic and validation

## Layers

**Resource Layer (REST Controllers):**
- Purpose: HTTP request handlers that orchestrate template rendering
- Location: `src/main/java/io/archton/scaffold/router/`
- Contains: REST endpoint classes annotated with `@Path`, methods with `@GET`, `@POST`, `@PUT`, `@DELETE`
- Depends on: Service, Repository, Entity, and Qute Template classes
- Used by: HTTP clients (browsers, HTMX requests)
- Pattern: Each resource uses inner `Templates` class with `@CheckedTemplate` for compile-time validation
- Example: `IndexResource.java`, `PersonResource.java`, `AuthResource.java`

**Service Layer:**
- Purpose: Business logic, validation, transactional coordination
- Location: `src/main/java/io/archton/scaffold/service/`
- Contains: `@ApplicationScoped` CDI beans for domain operations
- Depends on: Repository, Entity, custom exceptions
- Used by: Resource layer only
- Example files: `UserLoginService.java` (user creation with BCrypt hashing), `PasswordValidator.java` (NIST SP 800-63B-4 validation)

**Repository Layer:**
- Purpose: Data access abstraction via Panache ORM
- Location: `src/main/java/io/archton/scaffold/repository/`
- Contains: Classes implementing `PanacheRepository<T>` with custom query methods
- Depends on: Entity classes
- Used by: Service and Resource layers
- Pattern: Standard CRUD via `PanacheRepository` interface; custom methods use HQL with type-safe parameters
- Example methods: `findByFilter()` with case-insensitive search, `existsByEmail()`, `existsByEmailAndIdNot()`
- Key repos: `PersonRepository.java`, `UserLoginRepository.java`, `GenderRepository.java`, `TitleRepository.java`, `RelationshipRepository.java`, `PersonRelationshipRepository.java`

**Entity Layer:**
- Purpose: Plain POJO domain objects with Jakarta Persistence annotations
- Location: `src/main/java/io/archton/scaffold/entity/`
- Contains: Entity classes with JPA annotations, lifecycle callbacks, and helper methods
- Depends on: Only Jakarta Persistence API
- Used by: Repository layer and Resource layer
- Example entities: `Person.java` (core business entity with audit fields), `UserLogin.java` (authentication entity with `@UserDefinition`), `Gender.java`, `Title.java`, `Relationship.java`, `PersonRelationship.java`

**Error Handling Layer:**
- Purpose: Global exception handling and HTTP error response formatting
- Location: `src/main/java/io/archton/scaffold/error/`
- Contains: `GlobalExceptionMapper` implementing `ExceptionMapper<Throwable>`
- Depends on: Service exception classes, Qute templates
- Used by: Quarkus REST framework automatically
- File: `GlobalExceptionMapper.java` - maps domain exceptions (EntityNotFoundException, UniqueConstraintException, ReferentialIntegrityException) to HTTP status codes (404, 409) and renders HTML or JSON error responses based on Accept header

**Template Layer:**
- Purpose: Type-safe HTML generation with Qute
- Location: `src/main/resources/templates/`
- Contains: `.html` files organized by resource class
- Structure: Base template with layout + resource-specific templates + fragment templates
- Pattern: `@CheckedTemplate` declarations ensure compile-time validation
- Key files: `base.html` (main layout), resource directories like `PersonResource/`, `AuthResource/`, `GenderResource/`

## Data Flow

**Full-Page Request Flow:**

1. Browser requests `/persons` (GET)
2. `PersonResource.list()` checks for `HX-Request` header
3. No HTMX header → retrieves full data (persons, titles, genders, filters)
4. Returns `Templates.person(...)` with all context
5. Qute renders `PersonResource/person.html` extending `base.html`
6. Server returns complete HTML page

**HTMX Partial Update Flow:**

1. HTMX sends `hx-get="/persons"` with `HX-Request: true` header
2. `PersonResource.list()` detects header
3. Returns only `Templates.person$table(persons, filter)` fragment
4. Qute renders `PersonResource/person$table.html` (partial, no layout)
5. HTMX replaces target element in browser
6. No page reload, minimal network traffic

**Form Submission with Validation:**

1. User submits form (POST to `/persons`) with `@FormParam` values
2. `PersonResource.create()` validates input manually
3. Validation fails → renders same modal fragment with error message
4. Modal displays in browser via HTMX
5. User corrects and resubmits
6. Validation passes → calls `personRepository.persist()`
7. Returns success fragment `Templates.person$modal_success()` which closes modal and refreshes table via OOB (Out-of-Band) swap

**State Management:**
- Stateless: Each request contains full context needed for rendering
- Security state: Managed by Quarkus form authentication (session cookies)
- Form state: Held by HTML form elements, transmitted with each request
- Database state: Source of truth for persistent data
- Entity state: Managed by Hibernate JPA provider (detached/managed entities)

## Key Abstractions

**TemplateInstance:**
- Purpose: Lazy-evaluated compiled template with data binding
- Pattern: Always returned from Resource methods, never JSON objects
- Usage: `Templates.person(title, page, user, data...)` returns TemplateInstance
- Rendering: Quarkus framework automatically renders to response body

**CheckedTemplate:**
- Purpose: Compile-time validation of template signatures and method calls
- Pattern: Inner `Templates` static class in Resource with `@CheckedTemplate`
- Benefit: Ensures all required template parameters are passed, catches errors at build time
- Example from `PersonResource.java`:
  ```java
  @CheckedTemplate
  public static class Templates {
    public static native TemplateInstance person(...);
    public static native TemplateInstance person$table(...);
    public static native TemplateInstance person$modal_create(...);
  }
  ```

**Fragment Templates (Dollar-sign naming):**
- Purpose: Reusable HTML snippets for modals, tables, forms
- Naming: `resourceName$fragmentType` maps to file `ResourceName/resourceName$fragmentType.html`
- Pattern: Called from Resource methods to render partial page content
- Example: `person$table`, `person$modal_create`, `person$modal_delete_success`
- Integration: HTMX requests target these fragments and replace DOM elements

**Panache Repository:**
- Purpose: Simplified ORM with active record-like methods
- Pattern: Custom methods extend query capabilities
- Example from `PersonRepository.java`:
  ```java
  public List<Person> findByFilter(String filterText, String sortField, String sortDir) {
    // Dynamic HQL with case-insensitive LIKE and ORDER BY
  }
  ```
- Benefit: No verbose SQL, type-safe parameters, automatic entity state management

**Entity Lifecycle Callbacks:**
- Purpose: Automatic field management (timestamps, normalization)
- Pattern: `@PrePersist` and `@PreUpdate` methods
- Example from `Person.java`:
  ```java
  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
    if (email != null) email = email.toLowerCase().trim();
  }
  ```
- Usage: Ensures consistent state on all database operations

**Custom Domain Exceptions:**
- Purpose: Semantic error representation and HTTP status code mapping
- Location: `src/main/java/io/archton/scaffold/service/exception/`
- Classes: `EntityNotFoundException`, `UniqueConstraintException`, `ReferentialIntegrityException`
- Mapping in `GlobalExceptionMapper`: EntityNotFoundException → 404, others → 409

## Entry Points

**IndexResource (/):**
- Location: `src/main/java/io/archton/scaffold/router/IndexResource.java`
- Triggers: GET `/`
- Responsibilities: Render home page with security identity check, pass dev mode flag for error page stack traces

**AuthResource (/signup, /logout):**
- Location: `src/main/java/io/archton/scaffold/router/AuthResource.java`
- Triggers: GET/POST `/signup`, GET `/logout`
- Responsibilities: User registration (email validation, password validation, BCrypt hashing), session destruction, form-based login error handling

**PersonResource (/persons):**
- Location: `src/main/java/io/archton/scaffold/router/PersonResource.java`
- Triggers: GET/POST/PUT/DELETE `/persons`, GET/POST `/persons/create`, `/persons/{id}/edit`, `/persons/{id}/delete`
- Responsibilities: Full CRUD for Person entity with filter/sort, role-based access control (`@RolesAllowed("user", "admin")`)

**GenderResource, TitleResource, RelationshipResource:**
- Location: `src/main/java/io/archton/scaffold/router/` (admin-only resources)
- Triggers: GET/POST/PUT/DELETE `/genders`, `/titles`, `/relationships`
- Responsibilities: Master data management for dropdown lists and entity references

**GraphResource (/graph):**
- Location: `src/main/java/io/archton/scaffold/router/GraphResource.java`
- Triggers: GET `/graph`, GET `/graph/data`
- Responsibilities: Relationship graph visualization (returns data for graph rendering)

**PersonRelationshipResource (/person-relationships):**
- Location: `src/main/java/io/archton/scaffold/router/PersonRelationshipResource.java`
- Triggers: GET/POST/PUT/DELETE `/person-relationships`
- Responsibilities: Link people via relationship types

## Error Handling

**Strategy:** Centralized exception mapping with content negotiation

**Patterns:**

1. **Domain Exception Throwing:** Resource/Service throws semantic exception
   - Example: `throw new UniqueConstraintException("email", email, "Email already exists")`

2. **Global Mapping:** `GlobalExceptionMapper` catches all Throwable instances
   - Maps domain exceptions to HTTP status codes
   - Checks Accept header to decide HTML vs JSON response
   - For HTML: Renders `error.html` template with status, message, reference ID
   - For JSON: Returns `{"error":"...", "message":"...", "referenceId":"..."}`

3. **Development Mode:** Dev-mode errors include full stack trace in rendered page
   - Controlled by `GlobalExceptionMapper` checking `quarkus.profile`
   - Production mode hides implementation details

4. **Form Validation Errors:** Return same modal fragment with error message
   - No exception thrown
   - Validation error string passed to template
   - User sees error in modal without modal closing

## Cross-Cutting Concerns

**Logging:**
- Framework: JBoss Logging (preconfigured in Quarkus)
- Pattern: Injected logger in services/resources, no configuration needed
- Example: `GlobalExceptionMapper` logs errors with reference ID for troubleshooting

**Validation:**
- Custom validation in Resource methods (domain-specific)
- Example: Email format regex, NIST password length requirements
- Service layer: Password validation delegated to `PasswordValidator` service
- Entity level: JPA annotations for basic constraints (`@NotBlank`, `@Email`, `@Size`)

**Authentication:**
- Quarkus Security with JPA identity provider
- Form-based auth with session cookies
- Routes protected via `application.properties` permission rules
- Methods restricted via `@RolesAllowed` annotation
- SecurityIdentity injected into Resources for authorization checks

**Transaction Management:**
- `@Transactional` on Resource methods that modify state (POST, PUT, DELETE)
- Quarkus manages transaction lifecycle automatically
- Lazy-loading with proper entity graphs in Repository methods

---

*Architecture analysis: 2026-02-14*
