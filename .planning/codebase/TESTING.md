# Testing Patterns

**Analysis Date:** 2026-02-14

## Test Framework

**Runner:**
- JUnit 5 via `quarkus-junit5` dependency (managed by Quarkus BOM 3.30.3)
- Config: Inherited from Quarkus; no explicit `junit-platform.properties` or `junit5.properties` file

**Assertion Library:**
- Standard JUnit 5 assertions (via `org.junit.jupiter.api.Assertions`)
- No third-party assertion library configured

**Run Commands:**
```bash
./mvnw test                           # Run all tests (unit + integration)
./mvnw test -Dtest=ClassName          # Run single test class
./mvnw test -Dtest=ClassName#method   # Run single test method
./mvnw verify                         # Run integration tests (Failsafe)
./mvnw package                        # Build + run all tests
```

**Build Configuration:**
- `maven-surefire-plugin` (v3.5.3) - Unit tests
- `maven-failsafe-plugin` (v3.5.3) - Integration tests (IT* pattern)
- System properties configured:
  - `java.util.logging.manager=org.jboss.logmanager.LogManager`
  - `maven.home=${maven.home}`

## Test File Organization

**Location:**
- Source: `src/main/java/io/archton/scaffold/`
- Tests: `src/test/java/io/archton/scaffold/` (mirror package structure)

**Current State:**
- Test directory structure exists but is empty (no test files present)
- Package path created: `/io/archton/scaffold/`

**Naming:**
- Convention (not yet implemented): `[ClassName]Test.java` or `[ClassName]IT.java`
- Surefire pattern: `*Test.java` classes
- Failsafe pattern: `*IT.java` classes for integration tests

**Structure:**
```
src/test/java/io/archton/scaffold/
├── [entity]/
│   └── [EntityName]Test.java
├── [repository]/
│   └── [RepositoryName]Test.java
├── [router]/
│   └── [ResourceName]Test.java
└── [service]/
    └── [ServiceName]Test.java
```

## Test Strategy

**Test Types (Recommended):**

### Unit Tests
- **Scope**: Business logic in services and repositories
- **Approach**:
  - Mock external dependencies (database, security context)
  - Test single method behavior
  - Fast execution (no Quarkus container)
- **Classes to test**:
  - `PasswordValidator.validate()` - password policy enforcement
  - `PersonRepository.findByFilter()` - query logic
  - `UserLoginService.create()` - user creation with hashing
  - Exception mappers for error handling

### Integration Tests
- **Scope**: Entity lifecycle, repository operations, HTTP endpoints
- **Approach**:
  - Use `@QuarkusTest` annotation (Quarkus testing framework)
  - Full Quarkus container initialized
  - Database populated with test data
  - REST client for endpoint testing
- **Classes to test**:
  - Resource endpoints (PersonResource, GenderResource, AuthResource)
  - Repository queries with actual database
  - Transaction boundaries

### E2E Tests
- **Status**: Not yet configured; candidates exist via `.claude/agents/e2e-test-runner.md`

## Mocking

**Framework:**
- Not yet implemented
- Recommended: Mockito (compatible with JUnit 5 via `mockito-junit-jupiter`)

**Patterns (When Implemented):**
```java
// Mock repositories
@Mock
PersonRepository mockPersonRepository;

@InjectMocks
PersonResource personResource;

// Mock security context
SecurityIdentity mockSecurityIdentity;

// Setup mock behavior
when(mockPersonRepository.findById(1L)).thenReturn(expectedPerson);
```

**What to Mock:**
- Database repositories (unless testing repository itself)
- External service calls (future email/notification services)
- SecurityIdentity for testing role-based access

**What NOT to Mock:**
- Entities (use real instances)
- JPA lifecycle callbacks (test with actual persistence)
- Validators (test logic directly)
- Exception mappers (test with actual exceptions)

## Fixtures and Factories

**Test Data:**
- Not yet implemented
- Recommended approach: Factory methods in test classes

```java
// Example (to be created)
public class PersonFixtures {
    public static Person createValidPerson() {
        Person person = new Person();
        person.firstName = "John";
        person.lastName = "Doe";
        person.email = "john.doe@example.com";
        return person;
    }
}
```

**Location:**
- Proposed: `src/test/java/io/archton/scaffold/fixtures/`
- Or: Inline factory methods in test classes

## Coverage

**Requirements:** No coverage target enforced

**View Coverage:**
```bash
./mvnw clean test jacoco:report
# Report: target/site/jacoco/index.html
```

**Coverage Tool (to be added):**
- JaCoCo plugin can be added to `pom.xml` for code coverage reporting
- No minimum threshold currently enforced

## Error Handling in Tests

**Pattern (Recommended):**
- Use JUnit 5's `assertThrows()` for exception testing

```java
@Test
void testInvalidPassword_ThrowsException() {
    PasswordValidator validator = new PasswordValidator();
    assertThrows(IllegalArgumentException.class,
        () -> validator.validate("short"));
}
```

**Exception Scenarios to Test:**
- `UniqueConstraintException` - when duplicate email/code exists
- `EntityNotFoundException` - when entity not found by ID
- `ReferentialIntegrityException` - when deleting referenced entity
- Validation errors - field validation failures
- Form validation - server-side form submission errors

## Async Testing

**Pattern (When Needed):**
- Quarkus handles async operations transparently
- For testing async endpoints, use `RestAssured` with timeout handling

```java
@Test
void testAsyncEndpoint() {
    given()
        .get("/async-endpoint")
    .then()
        .statusCode(200)
        .timeout(5000); // 5 second timeout
}
```

## Integration Test Setup

**Quarkus Test Annotations:**
- `@QuarkusTest` - Enable Quarkus container
- `@TestHTTPEndpoint([ResourceClass])` - Inject REST client
- `@TestDatabase` - Test database initialization (via Testcontainers or H2)

**Database for Tests:**
- Recommended: Testcontainers PostgreSQL or H2 in-memory
- Flyway migrations auto-run via `quarkus.flyway.migrate-at-start=true`

**Example Test Class (to be created):**
```java
@QuarkusTest
public class PersonResourceIT {

    @TestHTTPEndpoint(PersonResource.class)
    RestClient client;

    @Inject
    PersonRepository personRepository;

    @BeforeEach
    void setup() {
        // Clear test data
        personRepository.deleteAll();
    }

    @Test
    void testListPersons_ReturnsHTML() {
        TemplateInstance response = client.list(null, null, null, null);
        assertNotNull(response);
    }
}
```

## REST Assured Integration

**Configuration (Recommended):**
- Add `rest-assured` dependency for testing REST endpoints
- Use `given()...when()...then()` BDD-style syntax

```java
@Test
void testCreatePerson_Success() {
    given()
        .formParam("firstName", "John")
        .formParam("lastName", "Doe")
        .formParam("email", "john@example.com")
        .post("/persons")
    .then()
        .statusCode(200)
        .contentType(ContentType.HTML);
}
```

## JSoup for HTML Testing

**Configuration (Recommended):**
- Add `jsoup` dependency for parsing/asserting HTML responses
- Useful for testing Qute template output

```java
@Test
void testPersonTable_ContainsCorrectColumns() {
    String html = response.render(); // From TemplateInstance
    Document doc = Jsoup.parse(html);

    Elements headers = doc.select("th");
    assertEquals(5, headers.size());
    assertEquals("First Name", headers.get(0).text());
}
```

## Test Database Isolation

**Strategy:**
- Use database transactions for isolation
- Roll back after each test via `@Transactional`
- Or use `DELETE FROM` in `@AfterEach` methods

```java
@QuarkusTest
public class PersonRepositoryIT {

    @Inject
    PersonRepository personRepository;

    @AfterEach
    @Transactional
    void cleanup() {
        personRepository.deleteAll();
    }
}
```

## Test Naming Convention

**Method Names:**
- Pattern: `test[Method][Scenario]_[ExpectedResult]()`
  - Example: `testFindByEmail_ValidEmail_ReturnsUser()`
  - Example: `testCreatePerson_DuplicateEmail_ReturnsError()`
  - Example: `testDeletePerson_NonExistentId_ReturnsNotFound()`

## Security Testing

**Authentication Tests:**
- Test role-based access control (`@RolesAllowed`)
- Verify unauthenticated requests are redirected
- Test session management and logout

```java
@Test
void testPersonResource_UnauthenticatedRequest_Redirects() {
    given()
        .get("/persons")
    .then()
        .statusCode(302)
        .header("Location", containsString("login"));
}

@Test
void testAdminResource_NonAdminRole_Forbidden() {
    given()
        .auth().form("user@example.com", "password123456")
        .get("/genders")
    .then()
        .statusCode(403);
}
```

**Password Validation Tests:**
- Test NIST SP 800-63B-4 compliance
- Test minimum/maximum length enforcement
- Test no composition rules (current policy)

```java
@Test
void testPasswordValidator_TooShort_Fails() {
    PasswordValidator validator = new PasswordValidator();
    List<String> errors = validator.validate("short");
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).contains("15 characters"));
}
```

## Test Dependencies

**To Be Added to pom.xml:**
```xml
<!-- Mockito for mocking -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>

<!-- REST Assured for HTTP testing -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>

<!-- JSoup for HTML parsing -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <scope>test</scope>
</dependency>

<!-- Testcontainers for database testing -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

## Current Testing Gap

**Status:** No tests implemented yet

**Priority Candidates:**
1. `PasswordValidator` - Core security component
2. `UserLoginService` - User creation and password hashing
3. `PersonRepository.findByFilter()` - Complex query logic
4. Resource endpoints (HTMX responses) - HTTP layer
5. `GlobalExceptionMapper` - Error handling

**Next Steps:**
1. Add test dependencies to `pom.xml`
2. Create fixtures/factories for test data
3. Implement unit tests for services
4. Implement integration tests for resources
5. Configure code coverage goals

---

*Testing analysis: 2026-02-14*
