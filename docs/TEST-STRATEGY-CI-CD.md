# Quarkus + HTMX Testing Strategy

This document defines the testing strategy for Quarkus applications that use HTMX for server-side rendered HTML fragments. After evaluating multiple approaches, we've selected **REST Assured + Jsoup** as our primary testing toolkit for validating HTML responses from HTMX endpoints.

This combination provides fast, reliable tests without browser overhead while offering powerful HTML parsing and assertion capabilities through familiar CSS selector syntax.

## CI/CD Optimisation Summary

This strategy prioritises fast feedback loops in CI/CD pipelines through:

| Optimisation | Impact |
|--------------|--------|
| Container reuse | 5-10s saved per run |
| `@QuarkusComponentTest` for unit tests | 20-40s total savings |
| Test execution splitting | Fail-fast on unit test failures |
| `@TestTransaction` auto-rollback | 1-2s saved per test |
| Parallel test execution | 30-50% faster overall |
| **Total potential improvement** | **40-60% faster test suite** |

---

## Why REST Assured + Jsoup?

### The HTMX Testing Challenge

HTMX endpoints return HTML fragments rather than JSON. Traditional REST API testing tools are optimised for JSON assertions, leaving a gap when we need to validate:

- HTML document structure
- Element attributes (especially `hx-*` attributes)
- Text content within specific elements
- Presence/absence of DOM nodes
- Correct nesting and hierarchy

### Tool Comparison

| Approach | Speed | JS Support | Complexity | Best For |
|----------|-------|------------|------------|----------|
| REST Assured + Jsoup | ⚡ Fast | ❌ No | Low | Fragment testing |
| Chrome DevTools MCP | 🔄 Medium | ✅ Yes | Low | E2E acceptance tests |
| HtmlUnit | 🐌 Medium | ⚠️ Partial | Medium | Legacy integration |
| String assertions | ⚡ Fast | ❌ No | Low | Smoke tests only |

### Decision Rationale

1. **Performance** — No browser instantiation means tests run in milliseconds
2. **Simplicity** — Jsoup's CSS selector API mirrors front-end development patterns
3. **Reliability** — No flaky tests from JavaScript timing issues
4. **Maintainability** — Declarative assertions are easy to read and update
5. **CI/CD Friendly** — No browser binaries or display servers required

---

## Project Setup

### Maven Dependencies

Add the following to your `pom.xml`:

```xml
<dependencies>
    <!-- Quarkus Test Framework -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-junit5</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- REST Assured - HTTP client for testing -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Jsoup - HTML parsing and assertions -->
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- AssertJ - Fluent assertions (optional but recommended) -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Mockito for @InjectMock and @InjectSpy -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-junit5-mockito</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Panache mocking support for @QuarkusComponentTest -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-panache-mock</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Testcontainers Configuration

Create `src/test/resources/testcontainers.properties` to enable container reuse:

```properties
# Reuse containers across test runs (significant speed improvement)
testcontainers.reuse.enable=true
```

This prevents PostgreSQL container restarts between test executions, saving 5-10 seconds per run.

---

## Test Architecture

### Test Types Overview

Quarkus provides multiple testing annotations optimised for different scenarios:

| Annotation | Use Case | Speed | Database |
|------------|----------|-------|----------|
| `@QuarkusComponentTest` | Service/logic unit tests | ⚡ Very fast | Mocked |
| `@QuarkusTest` | HTTP endpoint integration tests | 🔄 Medium | Real (Dev Services) |
| `@QuarkusIntegrationTest` | Built artifact tests | 🐢 Slow | Real |

**Recommendation:** Use `@QuarkusComponentTest` for business logic (10-50x faster than `@QuarkusTest`), reserve `@QuarkusTest` for HTTP endpoint testing.

### Package Structure

```
src/test/java/io/archton/htmx/
├── BaseHtmxTest.java               # Abstract base class with utilities
├── assertions/
│   └── HtmlAssertions.java         # Custom assertion helpers
├── component/                       # @QuarkusComponentTest (fast, no HTTP)
│   ├── TransactionServiceTest.java
│   ├── ChartDataServiceTest.java
│   └── TransactionFilterTest.java
├── integration/                     # @QuarkusTest (full HTTP stack)
│   ├── TransactionResourceTest.java
│   ├── AuthResourceTest.java
│   ├── ChartResourceTest.java
│   └── IndexResourceTest.java
├── profile/
│   └── TestProfiles.java           # Test profile definitions
└── fragments/
    └── QuteTemplateTest.java       # Isolated template rendering tests
```

### Base Test Class

Create a reusable base class that provides common utilities:

```java
package com.example;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static io.restassured.RestAssured.given;

@QuarkusTest
public abstract class BaseHtmxTest {

    /**
     * Performs a standard HTMX GET request and returns a parsed Document.
     */
    protected Document htmxGet(String path) {
        String html = given()
            .header("HX-Request", "true")
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .contentType(ContentType.HTML)
            .extract()
            .asString();
        
        return Jsoup.parse(html);
    }

    /**
     * Performs an HTMX POST request with form data.
     */
    protected Document htmxPost(String path, Map<String, String> formData) {
        var request = given()
            .header("HX-Request", "true")
            .contentType(ContentType.URLENC);
        
        formData.forEach(request::formParam);
        
        String html = request
            .when()
            .post(path)
            .then()
            .statusCode(200)
            .extract()
            .asString();
        
        return Jsoup.parse(html);
    }

    /**
     * Performs an HTMX DELETE request.
     */
    protected Response htmxDelete(String path) {
        return given()
            .header("HX-Request", "true")
            .when()
            .delete(path);
    }

    /**
     * Parses an HTML string into a Jsoup Document.
     */
    protected Document parseHtml(String html) {
        return Jsoup.parse(html);
    }

    /**
     * Parses an HTML fragment (for partial responses without full document structure).
     */
    protected Document parseFragment(String html) {
        return Jsoup.parseBodyFragment(html);
    }
}
```

---

## Component Testing with @QuarkusComponentTest

Use `@QuarkusComponentTest` for fast unit-style tests that don't require the full Quarkus application context. This is ideal for testing services, filters, and business logic without HTTP overhead.

### Basic Component Test

```java
package io.archton.scaffold.component;

import io.quarkus.test.InjectMock;
import io.quarkus.test.component.QuarkusComponentTest;
import io.quarkus.test.component.TestConfigProperty;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusComponentTest
@TestConfigProperty(key = "app.feature.enabled", value = "true")
public class TransactionServiceTest {

    @Inject
    TransactionService service;

    @InjectMock
    TransactionRepository repository;

    @Test
    void shouldCalculateMonthlyTotal() {
        // Given
        Mockito.when(repository.findByUserAndMonth(any(), any()))
            .thenReturn(List.of(
                new Transaction(100.0, TransactionType.INCOME),
                new Transaction(50.0, TransactionType.EXPENSE)
            ));

        // When
        TransactionSummary summary = service.calculateMonthlyTotal(user, YearMonth.now());

        // Then
        assertThat(summary.getBalance()).isEqualTo(50.0);
    }
}
```

### Mocking Panache Entities

For services that use Panache's Active Record pattern:

```java
package io.archton.scaffold.component;

import io.quarkus.panache.mock.MockPanacheEntities;
import io.quarkus.test.component.QuarkusComponentTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusComponentTest
@MockPanacheEntities(Transaction.class)
public class TransactionFilterTest {

    @Inject
    TransactionFilter filter;

    @Test
    void shouldFilterByCategory() {
        // Given
        Mockito.when(Transaction.find("category", category))
            .thenReturn(List.of(mockTransaction));

        // When
        List<Transaction> results = filter.byCategory(categoryId).getResults();

        // Then
        assertThat(results).hasSize(1);
    }
}
```

---

## Test Profiles

Test profiles allow configuration overrides and selective test execution based on tags.

### Define Test Profiles

```java
package io.archton.scaffold.profile;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;
import java.util.Set;

public class TestProfiles {

    /**
     * Fast tests that don't require database or external services.
     */
    public static class Fast implements QuarkusTestProfile {
        @Override
        public Set<String> tags() {
            return Set.of("fast");
        }
    }

    /**
     * HTML fragment tests requiring full HTTP stack.
     */
    public static class HtmlIntegration implements QuarkusTestProfile {
        @Override
        public Set<String> tags() {
            return Set.of("html", "integration");
        }
    }

    /**
     * Tests with mock external services.
     */
    public static class MockedServices implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "quarkus.rest-client.external-api.url", "http://localhost:8089"
            );
        }

        @Override
        public String getConfigProfile() {
            return "test-mocked";
        }
    }
}
```

### Apply Profiles to Tests

```java
@QuarkusTest
@TestProfile(TestProfiles.HtmlIntegration.class)
@Tag("html")
class TransactionResourceTest extends BaseHtmxTest {
    // Integration tests for HTML endpoints
}
```

### Run Tests by Profile Tags

```bash
# Run only fast tests (CI fast lane)
./mvnw test -Dquarkus.test.profile.tags=fast

# Run HTML integration tests
./mvnw test -Dquarkus.test.profile.tags=html

# Exclude slow tests
./mvnw test -DexcludedGroups=slow
```

---

## Transaction Handling

### Automatic Rollback with @TestTransaction

Use `@TestTransaction` for automatic rollback after each test, ensuring test isolation without manual cleanup:

```java
@QuarkusTest
class TransactionResourceTest extends BaseHtmxTest {

    @Test
    @TestTransaction  // Changes rolled back automatically
    void shouldCreateTransaction() {
        Document doc = htmxPost("/transactions", Map.of(
            "amount", "100.00",
            "type", "INCOME",
            "category", "1",
            "description", "Test transaction"
        ));

        assertElementExists(doc, ".transaction-item");
        // No cleanup needed - transaction is rolled back
    }
}
```

### Test Data Setup with @BeforeEach

```java
@QuarkusTest
class TransactionResourceTest extends BaseHtmxTest {

    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    void setupTestData() {
        // This runs in its own transaction
        User testUser = new User("testuser", "password");
        em.persist(testUser);
    }

    @Test
    @TestTransaction
    void shouldListTransactions() {
        // Test with fresh data, auto-rolled back
    }
}
```

---

## Testing Patterns

### Pattern 1: Basic Fragment Structure Validation

Verify that an endpoint returns the expected HTML structure:

```java
@Test
void shouldReturnTodoListWithCorrectStructure() {
    Document doc = htmxGet("/todos");
    
    // Verify container exists
    Element container = doc.selectFirst("#todo-list");
    assertThat(container).isNotNull();
    
    // Verify list items
    Elements items = doc.select("#todo-list > li.todo-item");
    assertThat(items).hasSizeGreaterThan(0);
    
    // Each item should have required elements
    for (Element item : items) {
        assertThat(item.selectFirst(".todo-text")).isNotNull();
        assertThat(item.selectFirst(".todo-actions")).isNotNull();
    }
}
```

### Pattern 2: HTMX Attribute Validation

Ensure HTMX attributes are correctly set for dynamic behaviour:

```java
@Test
void shouldHaveCorrectHtmxAttributesOnDeleteButton() {
    Document doc = htmxGet("/todos");
    
    Element deleteBtn = doc.selectFirst("button.delete-btn");
    
    // Verify HTMX attributes
    assertThat(deleteBtn.attr("hx-delete")).isEqualTo("/todos/1");
    assertThat(deleteBtn.attr("hx-target")).isEqualTo("#todo-list");
    assertThat(deleteBtn.attr("hx-swap")).isEqualTo("outerHTML");
    assertThat(deleteBtn.attr("hx-confirm")).isEqualTo("Are you sure?");
}
```

### Pattern 3: Dynamic Content Verification

Test that server-rendered content matches expected data:

```java
@Test
void shouldDisplayTodoDetailsCorrectly() {
    // Given: a known todo exists
    long todoId = createTestTodo("Buy milk", "2025-01-15");
    
    // When: requesting the todo fragment
    Document doc = htmxGet("/todos/" + todoId);
    
    // Then: content matches
    assertThat(doc.selectFirst(".todo-title").text()).isEqualTo("Buy milk");
    assertThat(doc.selectFirst(".todo-due-date").text()).contains("15 Jan 2025");
    assertThat(doc.selectFirst("[data-todo-id]").attr("data-todo-id"))
        .isEqualTo(String.valueOf(todoId));
}
```

### Pattern 4: Form Fragment Testing

Validate form structure and pre-populated values:

```java
@Test
void shouldRenderEditFormWithExistingValues() {
    // Given
    long todoId = createTestTodo("Original title", "2025-02-01");
    
    // When
    Document doc = htmxGet("/todos/" + todoId + "/edit");
    
    // Then: form has correct action
    Element form = doc.selectFirst("form");
    assertThat(form.attr("hx-put")).isEqualTo("/todos/" + todoId);
    assertThat(form.attr("hx-target")).isEqualTo("#todo-" + todoId);
    
    // And: inputs are pre-populated
    Element titleInput = doc.selectFirst("input[name=title]");
    assertThat(titleInput.val()).isEqualTo("Original title");
    
    Element dateInput = doc.selectFirst("input[name=dueDate]");
    assertThat(dateInput.val()).isEqualTo("2025-02-01");
}
```

### Pattern 5: Empty State Testing

Verify correct rendering when no data exists:

```java
@Test
void shouldShowEmptyStateWhenNoTodos() {
    // Given: no todos exist
    clearAllTodos();
    
    // When
    Document doc = htmxGet("/todos");
    
    // Then: empty state is displayed
    assertThat(doc.select(".todo-item")).isEmpty();
    
    Element emptyState = doc.selectFirst(".empty-state");
    assertThat(emptyState).isNotNull();
    assertThat(emptyState.text()).contains("No todos yet");
    
    // And: create button is present
    assertThat(doc.selectFirst("a[href='/todos/new']")).isNotNull();
}
```

### Pattern 6: Error State Testing

Validate error handling and user feedback:

```java
@Test
void shouldDisplayValidationErrors() {
    // When: submitting invalid data
    String html = given()
        .header("HX-Request", "true")
        .contentType(ContentType.URLENC)
        .formParam("title", "")  // Empty title - should fail
        .when()
        .post("/todos")
        .then()
        .statusCode(422)  // Unprocessable Entity
        .extract()
        .asString();
    
    Document doc = parseHtml(html);
    
    // Then: error message is displayed
    Element error = doc.selectFirst(".field-error[data-field=title]");
    assertThat(error).isNotNull();
    assertThat(error.text()).contains("Title is required");
    
    // And: input has error styling class
    Element input = doc.selectFirst("input[name=title]");
    assertThat(input.hasClass("is-invalid")).isTrue();
}
```

### Pattern 7: HTMX Response Header Validation

Test that correct HTMX response headers are set:

```java
@Test
void shouldTriggerClientEventAfterDelete() {
    // Given
    long todoId = createTestTodo("To be deleted", null);
    
    // When
    Response response = htmxDelete("/todos/" + todoId);
    
    // Then: status is correct
    assertThat(response.statusCode()).isEqualTo(200);
    
    // And: HTMX trigger header is set
    assertThat(response.header("HX-Trigger")).isEqualTo("todoDeleted");
    
    // Or for JSON trigger with data:
    // assertThat(response.header("HX-Trigger"))
    //     .contains("\"todoDeleted\":{\"id\":" + todoId + "}");
}

@Test
void shouldRedirectAfterCreate() {
    // When
    Response response = given()
        .header("HX-Request", "true")
        .contentType(ContentType.URLENC)
        .formParam("title", "New todo")
        .when()
        .post("/todos");
    
    // Then: HTMX redirect header is set
    assertThat(response.header("HX-Redirect")).isEqualTo("/todos");
}
```

### Pattern 8: Partial Swap Testing

Test fragments intended for specific swap targets:

```java
@Test
void shouldReturnOnlyUpdatedRow() {
    // Given
    long todoId = createTestTodo("Original", null);
    
    // When: toggling completion
    String html = given()
        .header("HX-Request", "true")
        .when()
        .patch("/todos/" + todoId + "/toggle")
        .then()
        .statusCode(200)
        .extract()
        .asString();
    
    Document doc = parseFragment(html);
    
    // Then: only the single row is returned (not the whole list)
    Elements rows = doc.select("li.todo-item");
    assertThat(rows).hasSize(1);
    
    // And: it has the completed class
    assertThat(rows.first().hasClass("completed")).isTrue();
    
    // And: checkbox is checked
    Element checkbox = rows.first().selectFirst("input[type=checkbox]");
    assertThat(checkbox.hasAttr("checked")).isTrue();
}
```

---

## Authentication Testing

Testing secured endpoints requires simulating authenticated sessions. This section covers patterns for testing form-based authentication with Quarkus Security.

### Pattern 9: Testing Protected Endpoints

For endpoints that require authentication, use REST Assured's session management:

```java
@QuarkusTest
class SecuredResourceTest extends BaseHtmxTest {

    @Test
    void shouldRedirectUnauthenticatedUserToLogin() {
        given()
            .redirects().follow(false)
            .when()
            .get("/persons")
            .then()
            .statusCode(302)
            .header("Location", containsString("/login"));
    }

    @Test
    void shouldAccessProtectedResourceWhenAuthenticated() {
        // Login first to get session cookie
        SessionFilter sessionFilter = new SessionFilter();
        
        given()
            .filter(sessionFilter)
            .contentType(ContentType.URLENC)
            .formParam("j_username", "testuser")
            .formParam("j_password", "password123")
            .when()
            .post("/j_security_check")
            .then()
            .statusCode(anyOf(is(200), is(302)));
        
        // Now access protected resource with session
        Document doc = given()
            .filter(sessionFilter)
            .header("HX-Request", "true")
            .when()
            .get("/persons")
            .then()
            .statusCode(200)
            .extract()
            .body().asString()
            .transform(Jsoup::parse);
        
        assertElementExists(doc, "#persons-table");
    }
}
```

### Pattern 10: Role-Based Access Testing

Test that different roles have appropriate access levels:

```java
@QuarkusTest
class RoleBasedAccessTest extends BaseHtmxTest {

    @Test
    void adminShouldAccessMaintenanceMenu() {
        SessionFilter session = loginAs("admin", "adminpass");
        
        Document doc = given()
            .filter(session)
            .header("HX-Request", "true")
            .when()
            .get("/gender")
            .then()
            .statusCode(200)
            .extract()
            .body().asString()
            .transform(Jsoup::parse);
        
        assertElementExists(doc, "#gender-table");
    }

    @Test
    void regularUserShouldNotAccessMaintenance() {
        SessionFilter session = loginAs("user", "userpass");
        
        given()
            .filter(session)
            .when()
            .get("/gender")
            .then()
            .statusCode(403);
    }

    private SessionFilter loginAs(String username, String password) {
        SessionFilter sessionFilter = new SessionFilter();
        given()
            .filter(sessionFilter)
            .contentType(ContentType.URLENC)
            .formParam("j_username", username)
            .formParam("j_password", password)
            .when()
            .post("/j_security_check");
        return sessionFilter;
    }
}
```

### Pattern 11: Testing Login Flow

Test the complete authentication workflow:

```java
@QuarkusTest
class LoginFlowTest extends BaseHtmxTest {

    @Test
    void shouldDisplayLoginForm() {
        Document doc = htmxGet("/login");
        
        assertElementExists(doc, "form[action='/j_security_check']");
        assertElementExists(doc, "input[name='j_username']");
        assertElementExists(doc, "input[name='j_password']");
        assertElementExists(doc, "button[type='submit']");
    }

    @Test
    void shouldRejectInvalidCredentials() {
        String html = given()
            .contentType(ContentType.URLENC)
            .formParam("j_username", "invalid")
            .formParam("j_password", "wrongpass")
            .when()
            .post("/j_security_check")
            .then()
            .extract()
            .asString();
        
        Document doc = parseHtml(html);
        assertElementContainsText(doc, ".error-message", "Invalid username or password");
    }

    @Test
    void shouldShowPersonalizedGreetingAfterLogin() {
        SessionFilter session = new SessionFilter();
        
        // Login
        given()
            .filter(session)
            .contentType(ContentType.URLENC)
            .formParam("j_username", "testuser")
            .formParam("j_password", "password123")
            .when()
            .post("/j_security_check");
        
        // Check home page greeting
        Document doc = given()
            .filter(session)
            .when()
            .get("/")
            .then()
            .extract()
            .body().asString()
            .transform(Jsoup::parse);
        
        assertElementContainsText(doc, ".user-greeting", "Welcome, testuser");
    }
}
```

---

## Using @TestHTTPEndpoint

Quarkus provides `@TestHTTPEndpoint` to automatically configure REST Assured base path for cleaner tests:

```java
import io.quarkus.test.common.http.TestHTTPEndpoint;

@QuarkusTest
@TestHTTPEndpoint(PersonResource.class)
class PersonResourceTest extends BaseHtmxTest {

    @Test
    void shouldListPersons() {
        // Path is relative to PersonResource's base path
        Document doc = htmxGet("");  // GET /persons
        assertElementExists(doc, "#persons-table");
    }

    @Test
    void shouldGetPersonById() {
        Document doc = htmxGet("/1");  // GET /persons/1
        assertElementExists(doc, "#person-details");
    }

    @Test
    void shouldCreatePerson() {
        Document doc = htmxPost("", Map.of(
            "firstName", "John",
            "lastName", "Doe",
            "email", "john@example.com"
        ));  // POST /persons
        
        assertElementExists(doc, ".success-message");
    }
}
```

### Benefits of @TestHTTPEndpoint

1. **DRY** — No need to repeat resource path in every test
2. **Refactor-safe** — Path changes in resource class are automatically reflected
3. **Type-safe** — Compile-time validation of resource class reference
4. **Clear intent** — Test class explicitly declares which resource it tests

---

## E2E Acceptance Testing with Chrome DevTools MCP

While REST Assured + Jsoup is ideal for fast integration tests, acceptance testing requires browser-based validation. This project uses **Chrome DevTools MCP** (Model Context Protocol) for E2E tests.

### Test Case Specification

All acceptance test cases are documented in:

**See:** [TEST-CASES.md](TEST-CASES.md)

This document contains 56 test cases covering:
- TC-1: Authentication Tests (20 cases)
- TC-2: Gender Master Data Tests (16 cases)
- TC-3: Persons Management Tests (20 cases)

### Chrome DevTools MCP Workflow

E2E tests are executed via Chrome DevTools MCP, which provides:

1. **Real browser context** — Tests run in actual Chrome browser
2. **DOM snapshots** — Accessibility-tree based element selection
3. **Network inspection** — Verify HTTP requests and responses
4. **Console monitoring** — Catch JavaScript errors
5. **Screenshot capture** — Visual verification on failures

### Test Execution Commands

```
# Navigate to page
navigate_page url="http://localhost:9080/login"

# Take DOM snapshot (returns element UIDs)
take_snapshot

# Interact with elements by UID
fill uid="input-username" value="testuser"
fill uid="input-password" value="password123"
click uid="btn-submit"

# Wait for navigation/content
wait_for text="Welcome"

# Verify page content
take_snapshot
# Assert expected elements are present
```

### When to Use Each Approach

| Scenario | Tool | Reason |
|----------|------|--------|
| HTMX fragment structure | REST Assured + Jsoup | Fast, reliable |
| Form validation errors | REST Assured + Jsoup | No JS needed |
| Authentication flows | REST Assured + Jsoup | Session cookies work |
| Full user journeys | Chrome DevTools MCP | Real browser behaviour |
| Visual verification | Chrome DevTools MCP | Screenshots |
| JavaScript interactions | Chrome DevTools MCP | JS execution |
| Cross-browser testing | Chrome DevTools MCP | Real Chrome |

### Example E2E Test Scenario

From TEST-CASES.md, TC-1.07 (Login with Valid Credentials):

```markdown
**Steps:**
1. Navigate to /login
2. Enter valid username in j_username field
3. Enter valid password in j_password field
4. Click Submit button

**Expected Results:**
- User is redirected to home page
- Navigation shows authenticated options
- Welcome message displays with username
```

**Chrome DevTools MCP execution:**
```
navigate_page url="http://localhost:9080/login"
take_snapshot
fill uid="[j_username-input-uid]" value="testuser"
fill uid="[j_password-input-uid]" value="password123"
click uid="[submit-button-uid]"
wait_for text="Welcome"
take_snapshot
# Verify welcome message and nav items
```

---

## Custom Assertion Helpers

Create reusable assertion utilities for common patterns:

```java
package com.example.assertions;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static org.assertj.core.api.Assertions.assertThat;

public final class HtmlAssertions {

    private HtmlAssertions() {}

    /**
     * Asserts that an element exists matching the CSS selector.
     */
    public static Element assertElementExists(Document doc, String selector) {
        Element element = doc.selectFirst(selector);
        assertThat(element)
            .withFailMessage("Expected element matching '%s' to exist", selector)
            .isNotNull();
        return element;
    }

    /**
     * Asserts that no elements match the CSS selector.
     */
    public static void assertElementNotExists(Document doc, String selector) {
        Elements elements = doc.select(selector);
        assertThat(elements)
            .withFailMessage("Expected no elements matching '%s', but found %d", 
                selector, elements.size())
            .isEmpty();
    }

    /**
     * Asserts the text content of an element.
     */
    public static void assertElementText(Document doc, String selector, String expected) {
        Element element = assertElementExists(doc, selector);
        assertThat(element.text()).isEqualTo(expected);
    }

    /**
     * Asserts that an element contains specific text.
     */
    public static void assertElementContainsText(Document doc, String selector, String expected) {
        Element element = assertElementExists(doc, selector);
        assertThat(element.text()).contains(expected);
    }

    /**
     * Asserts the value of an attribute on an element.
     */
    public static void assertAttribute(Document doc, String selector, 
                                        String attribute, String expected) {
        Element element = assertElementExists(doc, selector);
        assertThat(element.attr(attribute))
            .withFailMessage("Expected attribute '%s' on '%s' to be '%s' but was '%s'",
                attribute, selector, expected, element.attr(attribute))
            .isEqualTo(expected);
    }

    /**
     * Asserts that an element has a specific CSS class.
     */
    public static void assertHasClass(Document doc, String selector, String className) {
        Element element = assertElementExists(doc, selector);
        assertThat(element.hasClass(className))
            .withFailMessage("Expected element '%s' to have class '%s'", selector, className)
            .isTrue();
    }

    /**
     * Asserts the number of elements matching a selector.
     */
    public static void assertElementCount(Document doc, String selector, int expected) {
        Elements elements = doc.select(selector);
        assertThat(elements.size())
            .withFailMessage("Expected %d elements matching '%s', but found %d",
                expected, selector, elements.size())
            .isEqualTo(expected);
    }

    /**
     * Asserts HTMX attributes on an element.
     */
    public static void assertHtmxConfig(Document doc, String selector,
                                         String method, String target, String swap) {
        Element element = assertElementExists(doc, selector);
        
        // Check for hx-get, hx-post, hx-put, hx-patch, or hx-delete
        String actualMethod = Stream.of("hx-get", "hx-post", "hx-put", "hx-patch", "hx-delete")
            .filter(element::hasAttr)
            .findFirst()
            .orElse(null);
        
        assertThat(actualMethod)
            .withFailMessage("Expected element to have an hx-* method attribute")
            .isNotNull();
        
        if (method != null) {
            assertThat(element.attr(actualMethod)).isEqualTo(method);
        }
        if (target != null) {
            assertThat(element.attr("hx-target")).isEqualTo(target);
        }
        if (swap != null) {
            assertThat(element.attr("hx-swap")).isEqualTo(swap);
        }
    }
}
```

### Usage Example

```java
import static com.example.assertions.HtmlAssertions.*;

@Test
void shouldRenderTodoItemCorrectly() {
    Document doc = htmxGet("/todos/1");
    
    assertElementExists(doc, "#todo-1");
    assertElementText(doc, "#todo-1 .title", "Buy groceries");
    assertHasClass(doc, "#todo-1", "todo-item");
    assertAttribute(doc, "#todo-1", "data-priority", "high");
    assertElementCount(doc, "#todo-1 .action-btn", 3);
    assertHtmxConfig(doc, "#todo-1 .delete-btn", "/todos/1", "#todo-list", "outerHTML");
}
```

---

## Testing Qute Templates

When using Qute templates with HTMX, test both the endpoint response and template rendering:

```java
@QuarkusTest
class TodoTemplateTest extends BaseHtmxTest {

    @Inject
    Template todoItem;  // Inject the template directly for unit testing

    @Test
    void shouldRenderTodoItemTemplate() {
        // Given
        Todo todo = new Todo(1L, "Test todo", false, LocalDate.now());
        
        // When: render template directly
        String html = todoItem.data("todo", todo).render();
        Document doc = parseFragment(html);
        
        // Then
        assertElementExists(doc, "li.todo-item");
        assertElementText(doc, ".todo-text", "Test todo");
    }

    @Test
    void shouldRenderCompletedTodoWithStrikethrough() {
        // Given
        Todo todo = new Todo(1L, "Done todo", true, LocalDate.now());
        
        // When
        String html = todoItem.data("todo", todo).render();
        Document doc = parseFragment(html);
        
        // Then
        assertHasClass(doc, "li.todo-item", "completed");
        assertElementExists(doc, ".todo-text del");  // strikethrough
    }
}
```

---

## Integration with CI/CD

### Maven Surefire Configuration

Configure Maven Surefire to split test execution for faster feedback:

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>${surefire-plugin.version}</version>
    <executions>
        <!-- Fast unit tests first (fail fast) -->
        <execution>
            <id>unit-tests</id>
            <phase>test</phase>
            <goals><goal>test</goal></goals>
            <configuration>
                <excludedGroups>io.quarkus.test.junit.QuarkusTest</excludedGroups>
            </configuration>
        </execution>
        <!-- Integration tests after unit tests pass -->
        <execution>
            <id>integration-tests</id>
            <phase>test</phase>
            <goals><goal>test</goal></goals>
            <configuration>
                <groups>io.quarkus.test.junit.QuarkusTest</groups>
            </configuration>
        </execution>
    </executions>
    <configuration>
        <!-- Parallel execution for speed -->
        <parallel>classes</parallel>
        <threadCount>4</threadCount>
        <perCoreThreadCount>true</perCoreThreadCount>
        <systemPropertyVariables>
            <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

**Benefits:**
- Unit tests run first and fail fast
- Integration tests only run if unit tests pass
- Parallel execution reduces total test time by 30-50%

### GitHub Actions Example

```yaml
name: Test

on: [push, pull_request]

jobs:
  # Fast feedback: unit tests only
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run unit tests
        run: ./mvnw test -DexcludedGroups=io.quarkus.test.junit.QuarkusTest

  # Full integration tests (runs after unit tests)
  integration-tests:
    runs-on: ubuntu-latest
    needs: unit-tests
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run integration tests
        run: ./mvnw test -Dgroups=io.quarkus.test.junit.QuarkusTest

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: test-results
          path: target/surefire-reports/
```

### Test Categories

Use JUnit tags to categorise tests:

```java
@Tag("fast")
@Tag("html")
@QuarkusTest
class TransactionResourceTest extends BaseHtmxTest {
    // ...
}
```

Run specific categories:

```bash
# Run only fast HTML tests
./mvnw test -Dgroups="fast,html"

# Exclude slow E2E tests
./mvnw test -DexcludedGroups="e2e"

# Run tests matching profile tags
./mvnw test -Dquarkus.test.profile.tags=fast
```

---

## Continuous Testing in Dev Mode

Quarkus Dev Mode provides instant test feedback during development:

### Enable Continuous Testing

Add to `application.properties`:

```properties
# Enable continuous testing (tests run on code changes)
quarkus.test.continuous-testing=enabled

# Only run tests matching these patterns
quarkus.test.include-pattern=.*Test

# Exclude integration tests from continuous mode
quarkus.test.exclude-pattern=.*IT
```

### Dev Mode Commands

```bash
# Start dev mode
./mvnw quarkus:dev

# Keyboard shortcuts in dev mode:
# r - Re-run all tests
# f - Re-run failed tests
# o - Toggle test output
# p - Pause/resume tests
# h - Show help
```

### Benefits for Development

1. **Instant feedback** — Tests run automatically on save
2. **Focused testing** — Only affected tests re-run
3. **No restart** — Hot reload keeps application running
4. **Fast iteration** — Catches issues before commit

---

## Best Practices

### Do

- **Use descriptive selectors** — Prefer `#todo-list .todo-item` over `div > ul > li`
- **Test behaviour, not implementation** — Focus on what users see, not internal structure
- **Keep tests independent** — Each test should set up its own data
- **Use data attributes** — Add `data-testid` or `data-*` attributes for stable selectors
- **Test edge cases** — Empty states, error conditions, boundary values
- **Verify HTMX attributes** — These are critical for correct client-side behaviour

### Don't

- **Don't test styling** — CSS classes for styling should not be asserted unless semantic
- **Don't rely on text content for selection** — Text changes frequently; use IDs or data attributes
- **Don't test Jsoup itself** — Trust that parsing works; test your application logic
- **Don't skip error cases** — Validation errors and error states need testing too
- **Don't hardcode IDs** — Use test data factories that return created IDs

### Selector Strategies

```java
// ✅ Good: Uses semantic ID
doc.selectFirst("#user-profile")

// ✅ Good: Uses data attribute
doc.selectFirst("[data-testid='user-avatar']")

// ✅ Good: Uses role for accessibility
doc.selectFirst("[role='navigation']")

// ⚠️ Acceptable: CSS class with semantic meaning
doc.selectFirst(".error-message")

// ❌ Bad: Relies on structure
doc.selectFirst("body > div:nth-child(2) > section > div")

// ❌ Bad: Uses styling class
doc.selectFirst(".mt-4.text-red-500")
```

---

## Troubleshooting

### Common Issues

**Problem:** Jsoup returns null for an element that exists in the browser.

**Cause:** JavaScript modifies the DOM after load.

**Solution:** HTMX endpoints should return complete fragments. If client-side JS is required, use Chrome DevTools MCP for those specific tests (see "E2E Acceptance Testing" section).

---

**Problem:** Tests pass locally but fail in CI.

**Cause:** Test order dependency or shared state.

**Solution:** Ensure each test uses `@Transactional` rollback or explicit cleanup. Use `@TestTransaction` for automatic rollback.

---

**Problem:** Assertion fails with strange whitespace differences.

**Cause:** Jsoup normalises whitespace differently.

**Solution:** Use `.text()` for normalised content or `.wholeText()` for exact preservation. Consider using `contains()` instead of `equals()`.

---

## Appendix: Jsoup Selector Reference

| Selector | Description | Example |
|----------|-------------|---------|
| `#id` | By ID | `#main-content` |
| `.class` | By class | `.todo-item` |
| `tag` | By tag name | `button` |
| `[attr]` | Has attribute | `[hx-get]` |
| `[attr=value]` | Attribute equals | `[type=submit]` |
| `[attr^=value]` | Attribute starts with | `[hx-get^=/api]` |
| `[attr$=value]` | Attribute ends with | `[href$=.pdf]` |
| `[attr*=value]` | Attribute contains | `[class*=btn]` |
| `parent > child` | Direct child | `ul > li` |
| `ancestor descendant` | Any descendant | `form input` |
| `prev + next` | Adjacent sibling | `label + input` |
| `prev ~ siblings` | General sibling | `h1 ~ p` |
| `:first-child` | First child | `li:first-child` |
| `:last-child` | Last child | `li:last-child` |
| `:nth-child(n)` | Nth child | `tr:nth-child(2)` |
| `:has(selector)` | Contains matching | `div:has(> p)` |
| `:not(selector)` | Negation | `input:not([disabled])` |
| `:contains(text)` | Contains text | `p:contains(error)` |
| `:empty` | No children | `div:empty` |

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | Dec 2025 | Development Team | Initial release |
| 1.1 | Dec 2025 | Development Team | Added Quarkus CI/CD optimisations: `@QuarkusComponentTest`, test profiles, `@TestTransaction`, Maven Surefire split execution, parallel testing, continuous testing in dev mode, container reuse |
| 1.2 | Dec 2025 | Development Team | Added authentication testing patterns (Patterns 9-11), `@TestHTTPEndpoint` usage, E2E acceptance testing with Chrome DevTools MCP, link to TEST-CASES.md |

---

*This document is maintained by the development team. For questions or suggestions, please open an issue in the project repository.*
