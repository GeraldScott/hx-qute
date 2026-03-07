# Structure

## Directory Layout

```
hx-qute/
├── src/
│   ├── main/
│   │   ├── java/io/archton/scaffold/
│   │   │   ├── dto/                          # (empty - reserved)
│   │   │   ├── entity/                       # JPA entities
│   │   │   │   ├── Gender.java
│   │   │   │   ├── Person.java
│   │   │   │   ├── PersonRelationship.java
│   │   │   │   ├── Relationship.java
│   │   │   │   ├── Title.java
│   │   │   │   └── UserLogin.java
│   │   │   ├── error/                        # Exception handling
│   │   │   │   └── GlobalExceptionMapper.java
│   │   │   ├── filter/                       # (empty - reserved)
│   │   │   ├── repository/                   # Panache repositories
│   │   │   │   ├── GenderRepository.java
│   │   │   │   ├── PersonRelationshipRepository.java
│   │   │   │   ├── PersonRepository.java
│   │   │   │   ├── RelationshipRepository.java
│   │   │   │   ├── TitleRepository.java
│   │   │   │   └── UserLoginRepository.java
│   │   │   ├── router/                       # JAX-RS resources
│   │   │   │   ├── AuthResource.java
│   │   │   │   ├── GenderResource.java
│   │   │   │   ├── GraphResource.java
│   │   │   │   ├── IndexResource.java
│   │   │   │   ├── PersonRelationshipResource.java
│   │   │   │   ├── PersonResource.java
│   │   │   │   ├── RelationshipResource.java
│   │   │   │   └── TitleResource.java
│   │   │   └── service/                      # Business logic
│   │   │       ├── exception/                # Custom exceptions
│   │   │       │   ├── EntityNotFoundException.java
│   │   │       │   ├── ReferentialIntegrityException.java
│   │   │       │   └── UniqueConstraintException.java
│   │   │       ├── NetworkService.java
│   │   │       ├── PasswordValidator.java
│   │   │       └── UserLoginService.java
│   │   └── resources/
│   │       ├── application.properties        # App config
│   │       ├── banner.txt                    # Startup banner
│   │       ├── db/migration/                 # Flyway SQL migrations
│   │       │   ├── V1.0.0__Create_gender_table.sql
│   │       │   ├── V1.0.1__Insert_gender_data.sql
│   │       │   ├── V1.2.0__Create_user_login_table.sql
│   │       │   ├── V1.2.1__Insert_admin_user.sql
│   │       │   ├── V1.3.0__Create_title_table.sql
│   │       │   ├── V1.3.1__Insert_title_data.sql
│   │       │   ├── V1.4.0__Create_person_table.sql
│   │       │   ├── V1.4.1__Insert_person_data.sql
│   │       │   ├── V1.5.0__Create_relationship_table.sql
│   │       │   ├── V1.5.1__Insert_relationship_data.sql
│   │       │   ├── V1.6.0__Create_person_relationship_table.sql
│   │       │   └── V1.6.1__Insert_person_relationship_data.sql
│   │       ├── META-INF/resources/           # Static assets
│   │       │   ├── favicon.ico
│   │       │   ├── style.css
│   │       │   ├── img/                      # Images & logos
│   │       │   └── js/graph.js               # D3 graph visualization
│   │       └── templates/                    # Qute templates
│   │           ├── base.html                 # Layout template
│   │           ├── error.html                # Error page
│   │           ├── fragments/navigation.html # Shared navigation
│   │           ├── AuthResource/             # Auth templates
│   │           ├── GenderResource/           # Gender CRUD templates
│   │           ├── GraphResource/            # Graph view templates
│   │           ├── IndexResource/            # Landing page
│   │           ├── PersonRelationshipResource/
│   │           ├── PersonResource/           # Person CRUD templates
│   │           ├── RelationshipResource/
│   │           └── TitleResource/
│   └── test/java/io/archton/scaffold/       # (empty - no tests yet)
├── docs/                                     # Project documentation
│   ├── ARCHITECTURE.md
│   ├── SECURITY.md
│   └── WORKFLOW.md
├── pom.xml                                   # Maven build config
└── CLAUDE.md                                 # AI agent instructions
```

## Naming Conventions

### Java
- **Package**: `io.archton.scaffold` - base package
- **Entities**: Singular noun, public fields (`Person`, `Gender`, `UserLogin`)
- **Repositories**: `{Entity}Repository` implementing `PanacheRepository<Entity>`
- **Resources**: `{Entity}Resource` with `@Path("/{plural}")` - uses "router" package name (not "resource" or "controller")
- **Services**: `{Domain}Service` (`UserLoginService`, `NetworkService`)
- **Exceptions**: `{Descriptive}Exception` in `service/exception/`

### Templates
- **Directory**: `templates/{ResourceClassName}/` matches the Java class name exactly
- **Files**: `{templateMethod}.html` - e.g., `person.html` for `PersonResource.Templates.person()`
- **Fragments**: `{#fragment id='name'}` in template file, accessed via `templateName$fragmentId` in Java
- **Shared**: `templates/base.html` (layout), `templates/fragments/` (reusable includes)

### Database
- **Tables**: snake_case singular (`person`, `user_login`, `person_relationship`)
- **Columns**: snake_case (`first_name`, `date_of_birth`, `created_at`)
- **Constraints**: prefixed (`uk_` for unique, `fk_` for foreign key)
- **Migrations**: `V{major}.{minor}.{patch}__{Description}.sql` - DDL at `.0`, seed data at `.1`

## Key Locations

| What | Where |
|------|-------|
| App config | `src/main/resources/application.properties` |
| Database migrations | `src/main/resources/db/migration/` |
| Layout template | `src/main/resources/templates/base.html` |
| Navigation | `src/main/resources/templates/fragments/navigation.html` |
| Static assets | `src/main/resources/META-INF/resources/` |
| Custom CSS | `src/main/resources/META-INF/resources/style.css` |
| Error page | `src/main/resources/templates/error.html` |
