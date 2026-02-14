# Codebase Structure

**Analysis Date:** 2026-02-14

## Directory Layout

```
hx-qute/
├── pom.xml                           # Maven project configuration
├── CLAUDE.md                         # Project guidelines and slash commands
├── README.md                         # Project documentation
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── io/archton/scaffold/
│   │   │       ├── router/           # REST endpoints (Resource layer)
│   │   │       ├── service/          # Business logic (Service layer)
│   │   │       ├── repository/       # Data access (Repository layer)
│   │   │       ├── entity/           # Domain objects (Entity layer)
│   │   │       ├── error/            # Exception handling
│   │   │       ├── filter/           # (Empty - reserved for future filters)
│   │   │       └── dto/              # (Empty - reserved for DTOs)
│   │   ├── resources/
│   │   │   ├── application.properties # Configuration
│   │   │   ├── banner.txt            # Startup banner
│   │   │   ├── templates/            # Qute templates organized by resource
│   │   │   ├── db/migration/         # Flyway SQL migrations
│   │   │   └── META-INF/resources/   # Static assets
│   │   └── docker/                  # Docker image definitions
│   └── test/
│       └── java/                     # (Currently empty - no tests)
└── docs/
    ├── ARCHITECTURE.md               # Detailed architecture guide
    ├── SECURITY.md                   # Security policies
    ├── USER-STORIES.md               # Feature specifications
    └── DEVELOPMENT-WORKFLOW.md       # Workflow guide
```

## Directory Purposes

**src/main/java/io/archton/scaffold/router/:**
- Purpose: REST endpoint handlers that receive HTTP requests and coordinate template rendering
- Contains: Classes ending with `Resource` annotated with `@Path`
- Key files:
  - `IndexResource.java` - home page (/)
  - `AuthResource.java` - signup, login, logout (/signup, /logout)
  - `PersonResource.java` - person CRUD (/persons)
  - `GenderResource.java` - gender CRUD (/genders, admin-only)
  - `TitleResource.java` - title CRUD (/titles, admin-only)
  - `RelationshipResource.java` - relationship type CRUD (/relationships, admin-only)
  - `PersonRelationshipResource.java` - person-to-person links (/person-relationships)
  - `GraphResource.java` - relationship visualization (/graph)

**src/main/java/io/archton/scaffold/service/:**
- Purpose: Business logic, validation, transactional coordination
- Contains: `@ApplicationScoped` CDI beans for domain operations
- Key files:
  - `UserLoginService.java` - user creation with BCrypt password hashing
  - `PasswordValidator.java` - NIST SP 800-63B-4 password validation
  - `exception/` subdirectory: Custom domain exceptions
    - `UniqueConstraintException.java` - duplicate key violation
    - `EntityNotFoundException.java` - entity not found
    - `ReferentialIntegrityException.java` - constraint violation

**src/main/java/io/archton/scaffold/repository/:**
- Purpose: Data access layer using Hibernate Panache ORM
- Contains: Classes implementing `PanacheRepository<T>` with custom query methods
- Key files:
  - `PersonRepository.java` - findByFilter(), existsByEmail(), sorting and filtering
  - `UserLoginRepository.java` - user lookup and existence checks
  - `GenderRepository.java` - gender CRUD operations
  - `TitleRepository.java` - title CRUD operations
  - `RelationshipRepository.java` - relationship type CRUD
  - `PersonRelationshipRepository.java` - person relationship links

**src/main/java/io/archton/scaffold/entity/:**
- Purpose: Domain object definitions with Jakarta Persistence annotations
- Contains: JPA `@Entity` classes with lifecycle callbacks
- Key files:
  - `Person.java` - core business entity with relationships to Title/Gender
  - `UserLogin.java` - authentication entity with `@UserDefinition` for form auth
  - `Gender.java` - reference entity for person gender
  - `Title.java` - reference entity for person title (Mr., Ms., Dr., etc.)
  - `Relationship.java` - relationship type (parent, sibling, spouse, etc.)
  - `PersonRelationship.java` - join entity linking two people with a relationship type

**src/main/java/io/archton/scaffold/error/:**
- Purpose: Global exception handling and error response formatting
- Contains: `ExceptionMapper` implementation for converting exceptions to HTTP responses
- Key files:
  - `GlobalExceptionMapper.java` - maps domain exceptions to HTTP status codes, renders error.html or JSON

**src/main/java/io/archton/scaffold/filter/:**
- Purpose: Reserved for servlet filters (currently empty)
- Usage: For future interceptors like request logging, CORS handling

**src/main/java/io/archton/scaffold/dto/:**
- Purpose: Reserved for Data Transfer Objects (currently empty)
- Usage: For future API response wrapping if JSON endpoints added

**src/main/resources/templates/:**
- Purpose: Qute HTML templates organized by REST resource class
- Structure: One directory per `*Resource.java`, containing full page and fragment templates
- Key directories:
  - `PersonResource/` - person.html, person$table.html, person$modal_create.html, person$modal_edit.html, person$modal_delete.html, person$modal_success.html, person$modal_success_row.html, person$modal_delete_success.html
  - `AuthResource/` - signup.html, logout.html
  - `GenderResource/` - gender.html, gender$table.html, gender$modal_create.html, etc.
  - `TitleResource/` - title.html, title$table.html, title$modal_create.html, etc.
  - `RelationshipResource/` - relationship.html and fragments
  - `PersonRelationshipResource/` - personRelationship.html and fragments
  - `IndexResource/` - index.html
  - `GraphResource/` - graph.html, personModal.html
  - `fragments/` - shared includes (navigation.html)
- Key files:
  - `base.html` - main layout template (sidebar, header, modals)
  - `error.html` - error page with status, message, reference ID

**src/main/resources/db/migration/:**
- Purpose: Flyway versioned database migrations
- Naming: `V#.#.#__Description.sql` (version number, double underscore, description)
- Key files:
  - `V1.0.0__Create_gender_table.sql`
  - `V1.0.1__Insert_gender_data.sql`
  - `V1.2.0__Create_user_login_table.sql`
  - `V1.3.0__Create_title_table.sql`
  - `V1.4.0__Create_person_table.sql`
  - `V1.4.1__Insert_person_data.sql`
  - `V1.5.0__Create_relationship_table.sql`
  - `V1.5.1__Insert_relationship_data.sql`
  - `V1.6.0__Create_person_relationship_table.sql`
  - `V1.6.1__Insert_person_relationship_data.sql`

**src/main/resources/META-INF/resources/:**
- Purpose: Static assets (CSS, JavaScript, images)
- Contains:
  - `style.css` - custom stylesheet for layout and UIkit customizations
  - `favicon.ico` - browser tab icon
  - `js/` - JavaScript files
    - `graph.js` - relationship graph visualization logic
  - `img/` - image assets
    - `logo-scaffold.png` - application logo
    - `*.svg` - technology logos (Quarkus, HTMX, PostgreSQL)

**src/main/resources/application.properties:**
- Purpose: Quarkus runtime configuration
- Key sections:
  - Port configuration: `quarkus.http.port=9080`
  - Database: `quarkus.datasource.db-kind=postgresql`, Flyway auto-migration
  - Form auth: enabled with login/error pages, session timeout, cookie settings
  - Route protection: public paths, authenticated paths, admin paths
  - Password policy: min 15 chars, max 128 chars (NIST 800-63B-4)

**docs/:**
- Purpose: Architecture and development documentation
- Key files:
  - `ARCHITECTURE.md` - technical architecture patterns and design decisions
  - `SECURITY.md` - security policies and implementation details
  - `USER-STORIES.md` - feature specifications with acceptance criteria
  - `DEVELOPMENT-WORKFLOW.md` - spec-driven workflow guide

## Key File Locations

**Entry Points:**
- `src/main/java/io/archton/scaffold/router/IndexResource.java`: GET / (home page)
- `src/main/java/io/archton/scaffold/router/AuthResource.java`: GET/POST /signup, GET /logout
- `pom.xml`: Maven build configuration and dependency declarations

**Configuration:**
- `src/main/resources/application.properties`: Runtime configuration (port, database, auth, security)
- `pom.xml`: Java version (17), Quarkus version (3.30.3), dependencies

**Core Logic:**
- `src/main/java/io/archton/scaffold/service/UserLoginService.java`: User creation and password hashing
- `src/main/java/io/archton/scaffold/service/PasswordValidator.java`: Password validation against NIST standards
- `src/main/java/io/archton/scaffold/repository/PersonRepository.java`: Query methods for filtering and sorting

**Templates:**
- `src/main/resources/templates/base.html`: Main layout (sidebar, navigation, modals)
- `src/main/resources/templates/fragments/navigation.html`: Shared navigation menu
- `src/main/resources/templates/PersonResource/person.html`: Person list page
- `src/main/resources/templates/error.html`: Error page template

## Naming Conventions

**Files:**
- `*Resource.java` - REST endpoint classes (must follow class name = file name)
- `*Repository.java` - Repository implementations
- `*Service.java` - Service/business logic classes
- `*Exception.java` - Custom exceptions
- `*Fragment.html` or `*$fragmentType.html` - Template fragments (dollar sign denotes fragment)
- `*.sql` - Database migrations with `V#.#.#__` prefix

**Directories:**
- `router/` - REST endpoint handlers
- `service/` - Business logic and validation
- `repository/` - Data access layer
- `entity/` - Domain objects
- `error/` - Exception handling
- `exception/` - Custom exception classes
- `templates/[ResourceName]/` - Templates named after Resource class (without "Resource" suffix)
- `db/migration/` - Database schema and data migrations

**Java Class Naming:**
- Resource classes: `PersonResource`, `GenderResource` (UpperCamelCase)
- Repository classes: `PersonRepository` (UpperCamelCase)
- Service classes: `UserLoginService`, `PasswordValidator` (UpperCamelCase)
- Entity classes: `Person`, `Gender`, `UserLogin` (UpperCamelCase)
- Exception classes: `EntityNotFoundException` (UpperCamelCase)

**Template Fragment Naming:**
- Full pages: `person.html` (matches resource name lowercase)
- Table fragment: `person$table.html`
- Modal fragments: `person$modal_create.html`, `person$modal_edit.html`, `person$modal_delete.html`
- Success fragments: `person$modal_success.html`, `person$modal_success_row.html`, `person$modal_delete_success.html`
- Pattern: `resourcename$fragmenttype.html` all lowercase with underscores

## Where to Add New Code

**New Feature (e.g., adding Department CRUD):**
1. Create entity: `src/main/java/io/archton/scaffold/entity/Department.java`
2. Create repository: `src/main/java/io/archton/scaffold/repository/DepartmentRepository.java`
3. Create service (if business logic needed): `src/main/java/io/archton/scaffold/service/DepartmentService.java`
4. Create resource: `src/main/java/io/archton/scaffold/router/DepartmentResource.java`
5. Create templates:
   - `src/main/resources/templates/DepartmentResource/department.html` (full page)
   - `src/main/resources/templates/DepartmentResource/department$table.html` (table fragment)
   - `src/main/resources/templates/DepartmentResource/department$modal_create.html` (create form)
   - `src/main/resources/templates/DepartmentResource/department$modal_edit.html` (edit form)
   - etc.
6. Add database migration: `src/main/resources/db/migration/V#.#.#__Create_department_table.sql`
7. Register routes in `src/main/resources/application.properties`

**New Component/Module:**
- Filters: `src/main/java/io/archton/scaffold/filter/[Name]Filter.java`
- DTOs: `src/main/java/io/archton/scaffold/dto/[Name]DTO.java`
- Utilities: `src/main/java/io/archton/scaffold/util/[Name]Utility.java` (create util directory if needed)

**Utilities/Shared Helpers:**
- Location: `src/main/java/io/archton/scaffold/util/` (create if doesn't exist)
- Pattern: Static utility methods or reusable service beans
- Examples: Email sender, date formatter, string utilities

**Tests:**
- Location: `src/test/java/io/archton/scaffold/[package]/[ClassName]Test.java`
- Pattern: Mirror source directory structure
- Framework: JUnit 5 (via `quarkus-junit5`)
- Execution: `./mvnw test` or `./mvnw test -Dtest=ClassName#method`

## Special Directories

**target/:**
- Purpose: Build output directory (compiled classes, JAR artifacts, logs)
- Generated: Yes (automatically created by Maven)
- Committed: No (in .gitignore)
- Cleanup: `./mvnw clean` removes this directory

**docs/:**
- Purpose: Project documentation maintained in version control
- Generated: No (manually created and maintained)
- Committed: Yes
- Contents: Architecture, security, workflow, user stories

**META-INF/resources/:**
- Purpose: Root for static web assets served directly by Quarkus
- Generated: No (manually managed)
- Committed: Yes
- Behavior: Files here accessible at `/style.css`, `/img/logo.png`, etc.

---

*Structure analysis: 2026-02-14*
