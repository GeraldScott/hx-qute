# Stack Research

**Domain:** Testing and quality stack for Quarkus 3.x with server-rendered HTML (Qute + HTMX)
**Researched:** 2026-03-07
**Confidence:** HIGH

## Recommended Stack

### Core Technologies

All versions verified against the Quarkus 3.30.3 BOM (`quarkus-bom`) effective POM output on 2026-03-07.

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| JUnit 5 (Jupiter) | 5.13.4 | Test framework | BOM-managed; already pulled in by `quarkus-junit5`. Standard for all Quarkus projects. No alternative is viable. |
| REST Assured | 5.5.6 | HTTP endpoint testing | BOM-managed; Quarkus auto-configures base URI and test port. The de facto standard for testing Quarkus REST endpoints. Provides fluent DSL for request/response assertions. |
| Jsoup | 1.20.1 | HTML response parsing and assertion | Parse HTML returned by Qute templates, then assert on DOM structure using CSS selectors. Critical for server-rendered apps where REST Assured's body matchers are insufficient (they treat responses as strings, not DOM trees). |
| Hamcrest | 2.2 | Assertion matchers | BOM-managed; pulled in as REST Assured transitive dependency. Used inside REST Assured `.body()` chains. |
| Mockito | 5.20.0 | Mocking CDI beans in integration tests | BOM-managed via `quarkus-junit5-mockito`. Enables `@InjectMock` for replacing CDI beans with mocks without manual wiring. |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `quarkus-junit5` | 3.30.3 | `@QuarkusTest`, `@QuarkusIntegrationTest`, `@TestTransaction` | Every test class. Already in pom.xml. |
| `quarkus-junit5-mockito` | 3.30.3 | `@InjectMock`, `@InjectSpy` for CDI beans | When service tests need to mock a repository or external dependency. |
| `quarkus-test-security` | 3.30.3 | `@TestSecurity(user, roles, authMechanism)` | When testing protected endpoints. Eliminates need to manage real sessions or cookies in tests. Supports `authMechanism = "form"` matching the project's auth setup. |
| Awaitility | 4.3.0 | Async assertion polling | Only if testing async behavior (e.g., event-driven flows). BOM-managed. Not needed initially. |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| Maven Surefire 3.5.3 | Unit/integration test runner (`mvn test`) | Already configured in pom.xml. Runs `*Test.java` classes. |
| Maven Failsafe 3.5.3 | Integration test runner (`mvn verify`) | Already configured in pom.xml. Runs `*IT.java` classes. |
| Quarkus DevServices | Auto-provisions PostgreSQL testcontainer | Zero-config: Quarkus detects `quarkus-jdbc-postgresql` and starts a Testcontainers PostgreSQL instance for `@QuarkusTest`. No manual `docker-compose` or `@QuarkusTestResource` needed. Flyway migrations run automatically against it. |
| Quarkus Continuous Testing | Re-runs affected tests on save | Built into `quarkus:dev` mode. Press `r` in dev console to trigger. No additional dependency needed. |

## Installation

```xml
<!-- Already present -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
</dependency>

<!-- ADD: HTTP endpoint testing -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>

<!-- ADD: HTML DOM parsing for Qute template assertions -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.20.1</version>
    <scope>test</scope>
</dependency>

<!-- ADD: @InjectMock support -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5-mockito</artifactId>
    <scope>test</scope>
</dependency>

<!-- ADD: @TestSecurity for authenticated endpoint testing -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-test-security</artifactId>
    <scope>test</scope>
</dependency>
```

**Note:** `rest-assured`, `quarkus-junit5-mockito`, and `quarkus-test-security` are BOM-managed -- do NOT specify version numbers. Jsoup is NOT in the Quarkus BOM, so it requires an explicit version.

## Test Taxonomy

This project needs three test layers, mapped to the existing architecture:

### Layer 1: Repository Tests (`*RepositoryTest.java`)

- **Annotation:** `@QuarkusTest` + `@TestTransaction`
- **What they test:** Custom Panache repository queries (e.g., `findByCode`, `existsByCodeAndIdNot`)
- **Database:** DevServices auto-starts PostgreSQL + Flyway runs migrations
- **Transaction:** `@TestTransaction` rolls back after each test, keeping tests isolated
- **Pattern:** Inject repository, persist test data, assert query results

### Layer 2: Service Tests (`*ServiceTest.java`)

- **Annotation:** `@QuarkusTest` + `@TestTransaction`
- **What they test:** Business logic, validation, constraint enforcement (UniqueConstraintException, ReferentialIntegrityException, EntityNotFoundException)
- **Mocking:** Use `@InjectMock` only when testing a service in isolation from its dependencies; prefer real database for most service tests since the business logic in this app is thin
- **Pattern:** Call service methods, assert outcomes and exception types

### Layer 3: Resource/Endpoint Tests (`*ResourceTest.java`)

- **Annotation:** `@QuarkusTest` + `@TestSecurity(user = "testUser", roles = "user")` or `roles = "admin"`
- **What they test:** HTTP status codes, redirects, HTML response structure, HTMX-specific headers
- **Tools:** REST Assured for HTTP, Jsoup for parsing HTML responses
- **Pattern:**
  1. REST Assured sends request
  2. Assert status code and headers (`HX-Trigger`, `HX-Redirect`, `HX-Reswap`, Content-Type)
  3. Parse response body with `Jsoup.parse(body)`, assert DOM elements with CSS selectors
  4. For HTMX fragment responses: verify correct fragment returned (not full page)

### HTMX-Specific Test Patterns

Testing HTMX responses requires checking:
- **Fragment responses** return partial HTML (no `<html>`, `<head>` wrapper)
- **OOB swaps** contain `hx-swap-oob="true"` attributes
- **HX-Trigger headers** fire correct events (for toast notifications, list refreshes)
- **HX-Redirect headers** redirect after form submissions
- **Content-Type** is `text/html` for template responses

Jsoup is essential here because REST Assured alone cannot navigate DOM structure. Example:

```java
String html = given()
    .when().get("/persons")
    .then().statusCode(200)
    .extract().body().asString();

Document doc = Jsoup.parse(html);
assertThat(doc.select("table#person-list tbody tr")).isNotEmpty();
assertThat(doc.select("title").text()).contains("Persons");
```

## Alternatives Considered

| Recommended | Alternative | Why Not |
|-------------|-------------|---------|
| REST Assured 5.5.6 | OkHttp / Java HttpClient | REST Assured is Quarkus's official test HTTP client with auto-configured base URI. Using anything else means manual port/host wiring and losing Quarkus integration. |
| Jsoup 1.20.1 | HtmlUnit, Selenium | Jsoup is lightweight (parse + assert). HtmlUnit executes JavaScript (unnecessary -- this app uses server-rendered HTML with HTMX attributes, not JS-dependent rendering). Selenium requires a browser driver and is E2E territory, not integration test territory. |
| `@TestSecurity` | Manual cookie/session management | `@TestSecurity` injects identity directly into the Quarkus security context. Manual form login in each test is fragile, slow, and couples tests to the login flow. |
| `@TestTransaction` | Manual cleanup / truncate tables | `@TestTransaction` auto-rolls back. Manual cleanup is error-prone and requires knowing all tables touched. |
| Mockito via `@InjectMock` | Manual mock classes | `@InjectMock` integrates with CDI lifecycle. Manual mocks require `@Alternative` beans or producer methods -- unnecessary complexity. |
| Hamcrest 2.2 | AssertJ | Hamcrest is required inside REST Assured `.body()` chains. AssertJ could be added alongside for non-REST-Assured assertions, but adds another assertion library to learn. Hamcrest is sufficient for this project's scope. |
| DevServices (Testcontainers) | H2 in-memory database | H2 has SQL dialect differences from PostgreSQL (e.g., SERIAL vs IDENTITY, PostgreSQL-specific functions). DevServices runs real PostgreSQL, matching production. Flyway migrations run identically. No dialect surprises. |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| H2 database for tests | SQL dialect mismatches with PostgreSQL; Flyway migrations may not run identically; masks production bugs | DevServices + Testcontainers PostgreSQL (automatic with `quarkus-jdbc-postgresql`) |
| Selenium / WebDriver for integration tests | Too heavyweight for asserting server-rendered HTML; requires browser infrastructure; slow | Jsoup for HTML parsing + REST Assured for HTTP; reserve browser-based tests for E2E via chrome-devtools MCP |
| Arquillian | Legacy Java EE test framework; Quarkus has its own test framework that supersedes it | `@QuarkusTest` |
| `@QuarkusTestResource` for PostgreSQL | Unnecessary manual container management when DevServices handles it automatically | DevServices (zero-config) |
| Spring Test / MockMvc | Wrong framework; these are Spring-specific | REST Assured + `@QuarkusTest` |
| TestNG | Quarkus officially supports JUnit 5 only; TestNG integration is not maintained | JUnit 5 Jupiter |
| `quarkus-panache-mock` | Deprecated approach; `@InjectMock` on repository beans is the current pattern | `quarkus-junit5-mockito` with `@InjectMock` |

## Version Compatibility

| Package | Compatible With | Notes |
|---------|-----------------|-------|
| `quarkus-junit5` 3.30.3 | JUnit Jupiter 5.13.4 | BOM-managed; always in sync |
| `rest-assured` 5.5.6 | Hamcrest 2.2 | BOM-managed; REST Assured bundles Hamcrest |
| `quarkus-junit5-mockito` 3.30.3 | Mockito 5.20.0 | BOM-managed; do not add standalone Mockito dependency |
| `quarkus-test-security` 3.30.3 | `quarkus-security-jpa` 3.30.3 | Compatible; `@TestSecurity` works with all Quarkus auth mechanisms including form auth |
| Jsoup 1.20.1 | Java 8+ | No compatibility issues with Java 21; standalone library with no framework dependencies |
| Surefire/Failsafe 3.5.3 | JUnit Platform 1.13.4 | Already configured in pom.xml |

## Test Configuration

### application.properties (test profile)

Quarkus DevServices requires minimal test-specific configuration. The `%test.` profile prefix applies only during `@QuarkusTest` execution:

```properties
# DevServices auto-starts PostgreSQL -- no datasource URL needed for tests
# Flyway runs automatically against the DevServices database

# Test port (Quarkus defaults to 8081 for tests, but can override)
%test.quarkus.http.test-port=0
```

Key behaviors that work automatically:
- DevServices detects `quarkus-jdbc-postgresql` and starts a Testcontainers PostgreSQL container
- Flyway migrations in `db/migration` run against the test database
- `@QuarkusTest` sets REST Assured base URI to the test instance
- `@TestTransaction` uses the same transaction manager

### Test Directory Structure

```
src/test/java/io/archton/scaffold/
  repository/
    GenderRepositoryTest.java
    TitleRepositoryTest.java
    RelationshipRepositoryTest.java
    PersonRepositoryTest.java
    PersonRelationshipRepositoryTest.java
    UserLoginRepositoryTest.java
  service/
    UserLoginServiceTest.java
    PasswordValidatorTest.java
    NetworkServiceTest.java
  router/
    IndexResourceTest.java
    AuthResourceTest.java
    GenderResourceTest.java
    TitleResourceTest.java
    RelationshipResourceTest.java
    PersonResourceTest.java
    PersonRelationshipResourceTest.java
    GraphResourceTest.java
  error/
    GlobalExceptionMapperTest.java
```

## Sources

- Quarkus BOM effective POM for 3.30.3 -- verified all BOM-managed versions via `mvn help:effective-pom` (HIGH confidence)
- Context7 `/quarkusio/quarkusio.github.io` -- `@QuarkusTest`, `@TestSecurity`, `@InjectMock`, `@TestTransaction`, DevServices patterns (HIGH confidence)
- Context7 `/rest-assured/rest-assured` -- REST Assured 5.x API and Maven coordinates (HIGH confidence)
- Context7 `/jhy/jsoup` version jsoup-1.20.1 -- CSS selector API for HTML assertion (HIGH confidence)
- Quarkus Testing Guide https://quarkus.io/guides/getting-started-testing -- test annotations, DevServices integration, continuous testing (HIGH confidence)
- Quarkus Security Testing Guide https://quarkus.io/guides/security-testing -- `@TestSecurity` with `authMechanism` parameter (HIGH confidence)

---
*Stack research for: Testing and Quality -- Quarkus 3.30.3 + HTMX + Qute*
*Researched: 2026-03-07*
