# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Quarkus+HTMX application demonstrating server-rendered web apps with SPA-like interactions:
- **Quarkus 3.30** with REST endpoints and Qute templating
- **HTMX 2.0** for dynamic HTML updates without client-side JavaScript
- **UIkit 3** CSS framework for styling
- **Hibernate ORM Panache** with PostgreSQL and Flyway migrations

## Key Commands

```bash
# Development (live reload at http://localhost:8080)
./mvnw compile quarkus:dev

# Testing
./mvnw test                           # All tests
./mvnw test -Dtest=ClassName          # Single class
./mvnw test -Dtest=ClassName#method   # Single method
./mvnw verify                         # Integration tests

# Building
./mvnw package                                          # Layered JAR
./mvnw package -Dquarkus.package.jar.type=uber-jar     # Uber JAR
./mvnw package -Dnative                                 # Native (GraalVM)
```

## Architecture

### Package Structure
```
io.archton.scaffold
├── entity/          # JPA entities (Panache public field style)
├── repository/      # PanacheRepository implementations
├── router/          # REST resources with @CheckedTemplate
└── error/           # GlobalExceptionMapper (HTML/JSON content negotiation)
```

### Template Conventions

**Base template** (`templates/base.html`) requires three parameters:
- `title` - Page title
- `currentPage` - Navigation highlight key (`"home"`, `"gender"`, etc.)
- `userName` - Display name or null

**Resource templates** follow `templates/{ResourceClass}/{methodName}.html` pattern:
```html
{@Type paramName}
{#include base}
    <!-- Content injected into {#insert}{/} slot -->
{/include}
```

**Type-safe templates** use `@CheckedTemplate` with native methods:
```java
@CheckedTemplate
public static class Templates {
    public static native TemplateInstance gender(
        String title, String currentPage, String userName, List<Gender> genders);
}
```

### CRUD Endpoint Pattern (HTMX)

Standard REST endpoints for CRUD with HTML partials:

| Method | Path | Description | Returns |
|--------|------|-------------|---------|
| GET | /items | Main page | Full HTML |
| GET | /items/table | Table partial | Table HTML |
| GET | /items/new | Add form | Form in dialog |
| GET | /items/{id}/edit | Edit form | Form in dialog |
| GET | /items/{id}/delete | Delete confirm | Confirm dialog |
| POST | /items | Create | Form HTML or 204 |
| PUT | /items/{id} | Update | Form HTML or 204 |
| DELETE | /items/{id} | Delete | 204 |

Forms use `<dialog>` elements with htmx attributes for modal interactions.

### Database Migrations
- Location: `src/main/resources/db/migration/`
- Naming: `V{major}.{minor}.{patch}__{Description}.sql`
- Auto-run at startup (`quarkus.flyway.migrate-at-start=true`)
- Hibernate schema management disabled

### Entity Pattern
```java
@Entity
public class MyEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String field;  // Public fields, no getters/setters
}
```

### Adding Navigation Items
Edit sidebar in `templates/base.html` - add `uk-active` class conditionally using `currentPage` variable:
```html
<li class="{#if currentPage?? == 'mypage'}uk-active{/if}">
```

## Configuration
- `src/main/resources/application.properties` - Main config
- Dev UI: http://localhost:8080/q/dev/

## Development Workflow

This project will be implemented in a phased approach, one use case at a time.

Read `PROJECT-PLAN.md` for the status of the current phase and the use case that must be implemented. 

Read `specs/SYSTEM-SPECIFICATION.md` before implementing each use case to understand the technical requirements.

Note that the spec file is too large, so search for the relevant section using the use case number.

Implement the use case as per the technical specification.

Issue a `curl http://127.0.0.1:8080/q/health` after a code update to trigger a server refresh.

After completing each use case, find the corresponding test file in `specs/TEST-CASES.md` and run the test using chrome-devtools MCP in a sub-agent. After each test has completed, the sub-agent must update the relevant use case in `PROJECT-PLAN.md` with the test results.

**IMPORTANT**: After completing the tests for each use case, STOP and ask for user feedback before proceeding.

## Managing the backend server

This project uses the Quarkus dev server which runs all the time, so issue a `curl http://127.0.0.1:8080/q/health` after a code update to trigger a server refresh.

If the curl command fails, check if the server is listening on port 8080 with `ss -tlnp | grep 8080`.

If it is not running, start it in the background with `./mvnw quarkus:dev -Dquarkus.console.enabled=false`.

When the server is started as a background task, its output is written to `/tmp/claude/-home-geraldo-quarkus-dd-mailer/tasks/<task-id>.output`. To check the server logs:

```bash
# View full output
cat /tmp/claude/-home-geraldo-quarkus-dd-mailer/tasks/<task-id>.output

# Follow logs in real-time
tail -f /tmp/claude/-home-geraldo-quarkus-dd-mailer/tasks/<task-id>.output
```
