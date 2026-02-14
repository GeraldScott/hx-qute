# Coding Conventions

**Analysis Date:** 2026-02-14

## Naming Patterns

**Files:**
- Entity files: `[EntityName].java` (PascalCase)
  - Example: `Person.java`, `UserLogin.java`, `Gender.java`
- Repository files: `[EntityName]Repository.java`
  - Example: `PersonRepository.java`, `UserLoginRepository.java`
- Resource/Router files: `[EntityName]Resource.java`
  - Example: `PersonResource.java`, `AuthResource.java`
- Service files: `[ServiceName].java`
  - Example: `PasswordValidator.java`, `UserLoginService.java`
- Exception files: `[ExceptionName].java`
  - Example: `UniqueConstraintException.java`, `EntityNotFoundException.java`

**Packages:**
- Standard layering: `io.archton.scaffold.{entity,repository,router,service,filter,error,dto}`
- No wildcard imports; imports organized alphabetically by group

**Classes:**
- PascalCase for all class names (entities, services, repositories, resources)
- Suffix pattern: `Repository`, `Resource`, `Service`, `Exception`

**Methods:**
- camelCase for method names
- Action-based naming: `find*`, `list*`, `create`, `update`, `delete`, `exists*`
- Boolean method names: `is*`, `exists*`, `has*`
  - Examples: `emailExists()`, `isValid()`, `isAnonymous()`
- Getter patterns: `get*` for retrieving computed values
  - Example: `getDisplayName()`, `getFieldName()`

**Variables:**
- camelCase for local variables and instance fields
- UPPER_SNAKE_CASE for constants
  - Example: `EMAIL_REGEX`, `EMAIL_PATTERN`, `ENTITY_NOT_FOUND_MSG`
- Descriptive names: `firstName`, `lastName`, `dateOfBirth`, `createdAt`, `updatedAt`

**Types:**
- Custom exception classes inherit from `RuntimeException` or `Exception`
- Generic type parameters follow Java convention: `<T>`, `<K, V>`

## Code Style

**Formatting:**
- No formatter configured (Eclipse defaults applied by IDE)
- Indentation: 4 spaces
- Line width: No explicit limit observed
- Braces: Java convention (opening brace on same line)

**Linting:**
- No explicit linting configuration (default Maven compiler)
- Compilation: `maven-compiler-plugin` with release=17
- Warnings: Treated as compilation warnings

**Access Modifiers:**
- Public fields: Used extensively in entities (Jakarta JPA requirement)
  - Example: `public String firstName;` in `Person.java`
- Private methods: Used for internal helpers
  - Example: `private String buildOrderBy()` in `PersonRepository.java`
- Protected methods: Used for JPA lifecycle callbacks
  - Example: `protected void onCreate()` in `UserLogin.java`

## Import Organization

**Order:**
1. `package` declaration
2. Java standard library imports (`java.time.*`, `java.util.*`, etc.)
3. Jakarta EE imports (`jakarta.enterprise.*`, `jakarta.persistence.*`, etc.)
4. Quarkus imports (`io.quarkus.*`)
5. Third-party library imports (`org.jboss.*`, `io.vertx.*`)
6. Application imports (`io.archton.scaffold.*`)

**Path Aliases:**
- Not used; absolute package paths only

## Error Handling

**Custom Exception Strategy:**
- Define specific exception classes for domain-level errors: `UniqueConstraintException`, `EntityNotFoundException`, `ReferentialIntegrityException`
- Exceptions include contextual data (field name, value) via constructor parameters
- Global exception mapping: `GlobalExceptionMapper` implements `ExceptionMapper<Throwable>`
  - Maps custom exceptions to HTTP status codes (404, 409, 500)
  - Generates unique reference IDs for tracking errors
  - Returns HTML error pages for browser requests, JSON for API requests
  - Includes stack traces in dev mode only

**Error Response Pattern:**
- For form-based endpoints: Return error in template context
  - Example: `Templates.person$modal_create(person, titleChoices, genderChoices, "Error message")`
- For HTTP responses: Use Response builder with status code
- Error logging: Use JBoss Logger with formatted error messages including reference IDs

**Validation Approach:**
- Inline validation in Resource classes before persistence
- Jakarta validation annotations on entities: `@NotBlank`, `@Email`, `@Size`
- Regex-based validation for complex patterns (email, URL, etc.)
  - Example: `EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"`
- Return to form with error message on validation failure

## Logging

**Framework:** JBoss Logging via `Logger.getLogger(ClassName.class)`

**Patterns:**
- Error logging: `LOG.errorf(exception, "Error %d [%s]: %s | Path: %s", status, referenceId, message, path)`
- Conditional logging in dev mode: `if ("dev".equals(profile)) { ... }`
- No debug or trace logging observed in codebase
- Logging primarily for error tracking and troubleshooting

## Comments

**When to Comment:**
- Jakarta JPA lifecycle callbacks: Mark with inline comment "Lifecycle callbacks"
- Complex business logic: Comment major steps (see `PersonRepository.findByFilter()`)
- Display helper methods: Comment intent (see `Person.getDisplayName()`)
- Method blocks with multiple logical sections: Use section comments

**JSDoc/JavaDoc:**
- Used for public service methods
  - Example: `PasswordValidator.validate()` has full JSDoc with `@param` and `@return`
  - Example: `PasswordValidator.isValid()` has brief JSDoc
- Used for exception classes: Single-line class-level documentation
  - Example: `/** Thrown when a unique constraint would be violated. */`
- Used for repository methods: Brief documentation for custom queries
  - Example: `/** List all persons ordered, with title eagerly fetched for display name rendering. */`
- Not consistently applied to all methods (getter/setter patterns often omitted)

## Function Design

**Size:** Methods typically 10-100 lines; longer methods (100-200+ lines) exist in Resource classes for complex form handling

**Parameters:**
- Jakarta REST annotations for parameter extraction: `@QueryParam`, `@FormParam`, `@PathParam`, `@HeaderParam`
- Type-safe parameter names matching form/URL structure
- No method overloading observed

**Return Values:**
- Services return specific types or Optional
- Resources return `TemplateInstance` for HTML responses or `Response` for HTTP control
- Repositories return `List<T>`, `Optional<T>`, boolean for existence checks, or raw entity types
- Null checks used explicitly: `if (entity == null) { return errorTemplate(...); }`

## Module Design

**Exports:**
- All classes declared as public for JAR distribution
- Services and Repositories use `@ApplicationScoped` CDI scope

**Barrel Files:**
- Not used; no index or aggregator classes

**Dependency Injection:**
- Constructor injection not used; field injection via `@Inject` annotation
- Example:
  ```java
  @Inject
  PersonRepository personRepository;

  @Inject
  SecurityIdentity securityIdentity;
  ```

**Panache Pattern (Hibernate ORM):**
- Repositories extend `PanacheRepository<Entity>`
- Direct method calls via Panache DSL: `find()`, `list()`, `count()`, `findById()`, `persist()`
- EntityManager access for complex queries: `getEntityManager().createQuery(...)`

## Security Patterns

**Authentication:**
- Form-based auth via `quarkus-security-jpa`
- JPA entity annotated with `@UserDefinition`, `@Username`, `@Password`, `@Roles`
- Security identity injection: `@Inject SecurityIdentity securityIdentity`
- Role-based access control: `@RolesAllowed({"user", "admin"})`

**Password Handling:**
- BCrypt hashing with cost factor 12: `BcryptUtil.bcryptHash(password, 12)`
- Plain text passwords never logged
- Password validation: Configurable via properties (`app.security.password.min-length`, `app.security.password.max-length`)

**Data Normalization:**
- Email normalization: lowercase + trim before persistence
- Applied in lifecycle callbacks (`@PrePersist`, `@PreUpdate`)

## Transactional Patterns

**Transaction Management:**
- `@Transactional` annotation on mutating operations (create, update, delete)
- Applied at Resource method level (HTTP request scope)
- Implicit transaction rollback on exception throwing

**Lifecycle Callbacks:**
- `@PrePersist`: Called before insert (audit timestamps, normalization)
- `@PreUpdate`: Called before update (update timestamp, normalization)
- Methods marked `void` or `protected void`

---

*Convention analysis: 2026-02-14*
