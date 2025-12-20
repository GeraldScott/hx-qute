# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Quarkus+HTMX prototype application that demonstrates building modern web applications using:
- Quarkus Java framework with REST endpoints
- Qute template engine for server-side rendering
- HTMX for SPA-like client interactions without complex JavaScript
- UIkit CSS framework for styling
- Hibernate ORM Panache for database access
- PostgreSQL database with Flyway migrations

## Key Commands

### Development
```bash
./mvnw compile quarkus:dev
```
- Application runs at http://localhost:8080
- Dev UI available at http://localhost:8080/q/dev/

### Testing
```bash
# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Run a specific test method
./mvnw test -Dtest=ClassName#methodName

# Run integration tests
./mvnw verify
```

### Building
```bash
./mvnw package                                          # Layered JAR
./mvnw package -Dquarkus.package.jar.type=uber-jar     # Uber JAR
./mvnw package -Dnative                                 # Native (requires GraalVM)
```

## Architecture

### Package Structure
```
io.archton.scaffold
├── entity/          # JPA entities with public fields (Panache style)
├── repository/      # PanacheRepository implementations
├── router/          # REST resources (endpoints)
└── error/           # Global exception handling
```

### Template System
- **Base template**: `templates/base.html` - Layout with sidebar navigation, requires `title`, `currentPage`, `userName` parameters
- **Resource templates**: `templates/{ResourceClass}/{methodName}.html` - Extend base via `{#include base}...{/include}`
- **Type-safe parameters**: Templates declare parameters at top with `{@Type varName}` syntax
- Uses `@CheckedTemplate` inner class with `native` methods for compile-time validation

Example resource pattern:
```java
@CheckedTemplate
public static class Templates {
    public static native TemplateInstance myTemplate(String title, String currentPage, String userName, List<Entity> data);
}
```

### Database Migrations
- Flyway migrations in `src/main/resources/db/migration/`
- Naming: `V{major}.{minor}.{patch}__{Description}.sql`
- Migrations run automatically at startup (`quarkus.flyway.migrate-at-start=true`)
- Schema management disabled for Hibernate (`quarkus.hibernate-orm.schema-management.strategy=none`)

### Entity Pattern
Entities use Panache's public field style (no getters/setters needed):
```java
@Entity
public class MyEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String field;
}
```

## Configuration
- Main config: `src/main/resources/application.properties`
- Database: PostgreSQL with Flyway-managed schema
