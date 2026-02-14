---
name: java-testing
description: Comprehensive Java testing with JUnit 5, Mockito, AssertJ, and integration testing patterns. Use when writing unit tests, integration tests, mocking dependencies, testing REST APIs, database testing, or when following TDD/BDD practices in Java projects.
---

# Java Testing

## JUnit 5 Fundamentals

### Test Class Structure
```java
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    private CustomerService service;
    
    @Mock
    private CustomerRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CustomerService(repository);
    }

    @Test
    @DisplayName("should find customer by valid ID")
    void findById_ValidId_ReturnsCustomer() {
        // Given
        var customer = new Customer("John", "john@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        // When
        var result = service.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("should throw exception for invalid input")
    void create_NullName_ThrowsException() {
        assertThatThrownBy(() -> service.create(null, "email@test.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Name cannot be null");
    }
}
```

## Mockito Patterns

### Basic Mocking
```java
// Stub return values
when(repository.findById(1L)).thenReturn(Optional.of(customer));
when(repository.findAll()).thenReturn(List.of(c1, c2, c3));
when(repository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

// Stub exceptions
when(service.process(any())).thenThrow(new ServiceException("Failed"));

// Verify interactions
verify(repository).save(customerCaptor.capture());
verify(repository, times(2)).findById(anyLong());
verify(repository, never()).delete(any());

// Argument matchers
when(repository.findByEmail(eq("test@example.com"))).thenReturn(customer);
when(repository.findByNameLike(contains("John"))).thenReturn(List.of(customer));
```

### Argument Captors
```java
@Captor
ArgumentCaptor<Customer> customerCaptor;

@Test
void shouldSaveWithCorrectData() {
    service.createCustomer("John", "john@example.com");
    
    verify(repository).save(customerCaptor.capture());
    
    Customer saved = customerCaptor.getValue();
    assertThat(saved.getName()).isEqualTo("John");
    assertThat(saved.getEmail()).isEqualTo("john@example.com");
}
```

### Spying
```java
@Spy
private CustomerValidator validator = new CustomerValidator();

@Test
void shouldCallRealValidation() {
    // Calls real method
    service.validate(customer);
    verify(validator).validate(customer);
    
    // Override specific method
    doReturn(true).when(validator).isEmailUnique(anyString());
}
```

## AssertJ Assertions

```java
// Object assertions
assertThat(customer)
    .isNotNull()
    .extracting(Customer::getName, Customer::getEmail)
    .containsExactly("John", "john@example.com");

// Collection assertions
assertThat(customers)
    .hasSize(3)
    .extracting(Customer::getName)
    .containsExactlyInAnyOrder("Alice", "Bob", "Charlie")
    .doesNotContain("Unknown");

// Exception assertions
assertThatThrownBy(() -> service.processInvalid())
    .isInstanceOf(ValidationException.class)
    .hasMessageContaining("Invalid")
    .hasCauseInstanceOf(IllegalStateException.class);

// Soft assertions (collect all failures)
SoftAssertions.assertSoftly(softly -> {
    softly.assertThat(result.getName()).isEqualTo("John");
    softly.assertThat(result.getEmail()).contains("@");
    softly.assertThat(result.getAge()).isPositive();
});
```

## Parameterized Tests

```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "  "})
void shouldRejectBlankNames(String name) {
    assertThatThrownBy(() -> service.create(name, "email@test.com"))
        .isInstanceOf(IllegalArgumentException.class);
}

@ParameterizedTest
@CsvSource({
    "john@example.com, true",
    "invalid-email, false",
    "test@, false",
    "@domain.com, false"
})
void shouldValidateEmail(String email, boolean expected) {
    assertThat(validator.isValidEmail(email)).isEqualTo(expected);
}

@ParameterizedTest
@MethodSource("customerProvider")
void shouldProcessCustomers(Customer customer, Status expectedStatus) {
    var result = service.process(customer);
    assertThat(result.getStatus()).isEqualTo(expectedStatus);
}

static Stream<Arguments> customerProvider() {
    return Stream.of(
        Arguments.of(new Customer("VIP"), Status.PRIORITY),
        Arguments.of(new Customer("Regular"), Status.NORMAL)
    );
}
```

## Quarkus Integration Testing

```java
@QuarkusTest
@TestTransaction
class CustomerRepositoryIT {

    @Inject
    CustomerRepository repository;

    @Test
    void shouldPersistAndFind() {
        var customer = new Customer("Test", "test@example.com");
        repository.persist(customer);
        
        var found = repository.findByEmail("test@example.com");
        assertThat(found).isNotNull();
        assertThat(found.getId()).isNotNull();
    }
}

@QuarkusTest
@TestHTTPEndpoint(CustomerResource.class)
class CustomerResourceIT {

    @Test
    void shouldReturnCustomerList() {
        given()
            .when().get()
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$.size()", greaterThanOrEqualTo(0));
    }

    @Test
    void shouldCreateCustomer() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name": "New User", "email": "new@example.com"}
                """)
            .when().post()
            .then()
            .statusCode(201)
            .body("id", notNullValue());
    }
}
```

## Test Containers

```java
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class DatabaseIT {
    // Tests run against real PostgreSQL
}

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {
    
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb");

    @Override
    public Map<String, String> start() {
        postgres.start();
        return Map.of(
            "quarkus.datasource.jdbc.url", postgres.getJdbcUrl(),
            "quarkus.datasource.username", postgres.getUsername(),
            "quarkus.datasource.password", postgres.getPassword()
        );
    }

    @Override
    public void stop() {
        postgres.stop();
    }
}
```

## Test Utilities

### Custom Assertions
```java
public class CustomerAssert extends AbstractAssert<CustomerAssert, Customer> {
    
    public CustomerAssert(Customer actual) {
        super(actual, CustomerAssert.class);
    }
    
    public static CustomerAssert assertThat(Customer actual) {
        return new CustomerAssert(actual);
    }
    
    public CustomerAssert hasName(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getName(), expected)) {
            failWithMessage("Expected name <%s> but was <%s>", expected, actual.getName());
        }
        return this;
    }
}
```

### Test Data Builders
```java
public class CustomerBuilder {
    private String name = "Default Name";
    private String email = "default@example.com";
    private Status status = Status.ACTIVE;
    
    public static CustomerBuilder aCustomer() {
        return new CustomerBuilder();
    }
    
    public CustomerBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public CustomerBuilder inactive() {
        this.status = Status.INACTIVE;
        return this;
    }
    
    public Customer build() {
        return new Customer(name, email, status);
    }
}

// Usage
var customer = aCustomer().withName("John").inactive().build();
```

## Common Dependencies

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```
