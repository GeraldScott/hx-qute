# Testing

## Current State

**No tests exist.** The `src/test/java/io/archton/scaffold/` directory is empty.

## Available Framework

The project's `pom.xml` includes testing dependencies (from STACK.md analysis):
- `quarkus-junit5` - Quarkus test framework with `@QuarkusTest`
- `rest-assured` - HTTP endpoint testing
- `quarkus-junit5-mockito` - Mocking support (if present)

## Testing Patterns (Recommended for this codebase)

### Resource/Endpoint Tests
Given the server-rendered HTML architecture, tests should:
- Use REST Assured to call endpoints
- Assert HTTP status codes
- Parse HTML responses with Jsoup to verify content
- Test both full page and HTMX fragment responses (with/without `HX-Request` header)

### Repository Tests
- Test custom query methods (`findByFilterPaged`, `existsByEmail`, etc.)
- Use `@QuarkusTest` with test database (DevServices PostgreSQL)

### Service Tests
- `PasswordValidator`: Pure unit tests
- `UserLoginService`: Test BCrypt hashing, duplicate email handling
- `NetworkService`: Test BFS traversal with known graph data

## Dev Server

Quarkus DevServices auto-provisions a PostgreSQL container for testing. Flyway migrations run automatically in test mode.
