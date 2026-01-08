---
name: java-patterns
description: Modern Java design patterns, idioms, and best practices including records, sealed classes, pattern matching, Stream API, and functional programming. Use when designing Java applications, implementing common patterns, refactoring code, or when following modern Java 17+ idioms and practices.
---

# Modern Java Patterns

## Records (Java 16+)

### DTOs and Value Objects
```java
// Immutable by design
public record CustomerDTO(
    Long id,
    String name,
    String email,
    Instant createdAt
) {
    // Compact constructor for validation
    public CustomerDTO {
        Objects.requireNonNull(name, "name required");
        Objects.requireNonNull(email, "email required");
    }
    
    // Additional methods
    public String displayName() {
        return name + " <" + email + ">";
    }
}

// From entity
public static CustomerDTO from(Customer entity) {
    return new CustomerDTO(
        entity.getId(),
        entity.getName(),
        entity.getEmail(),
        entity.getCreatedAt()
    );
}
```

### Builder Pattern with Records
```java
public record SearchCriteria(
    String query,
    int page,
    int size,
    SortOrder sort
) {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String query = "";
        private int page = 0;
        private int size = 20;
        private SortOrder sort = SortOrder.DESC;
        
        public Builder query(String q) { this.query = q; return this; }
        public Builder page(int p) { this.page = p; return this; }
        public Builder size(int s) { this.size = s; return this; }
        public Builder sort(SortOrder s) { this.sort = s; return this; }
        
        public SearchCriteria build() {
            return new SearchCriteria(query, page, size, sort);
        }
    }
}
```

## Sealed Classes (Java 17+)

```java
// Restricted hierarchy
public sealed interface PaymentResult 
    permits PaymentSuccess, PaymentFailure, PaymentPending {
}

public record PaymentSuccess(String transactionId, Instant timestamp) 
    implements PaymentResult {}

public record PaymentFailure(String errorCode, String message) 
    implements PaymentResult {}

public record PaymentPending(String checkoutUrl) 
    implements PaymentResult {}

// Exhaustive pattern matching
public String handleResult(PaymentResult result) {
    return switch (result) {
        case PaymentSuccess s -> "Paid: " + s.transactionId();
        case PaymentFailure f -> "Failed: " + f.message();
        case PaymentPending p -> "Pending: " + p.checkoutUrl();
    };
}
```

## Pattern Matching

### Switch Expressions
```java
// Type patterns
public String describe(Object obj) {
    return switch (obj) {
        case String s -> "String of length " + s.length();
        case Integer i when i > 0 -> "Positive: " + i;
        case Integer i -> "Non-positive: " + i;
        case List<?> list -> "List with " + list.size() + " elements";
        case null -> "null value";
        default -> "Unknown: " + obj.getClass();
    };
}

// Record patterns (Java 21+)
public double calculateArea(Shape shape) {
    return switch (shape) {
        case Circle(double radius) -> Math.PI * radius * radius;
        case Rectangle(double w, double h) -> w * h;
        case Triangle(double base, double height) -> 0.5 * base * height;
    };
}
```

### instanceof Patterns
```java
// Before
if (obj instanceof String) {
    String s = (String) obj;
    return s.toLowerCase();
}

// After
if (obj instanceof String s) {
    return s.toLowerCase();
}

// With conditions
if (obj instanceof String s && s.length() > 5) {
    return s.substring(0, 5);
}
```

## Optional Patterns

```java
// Chain operations
Optional<String> cityName = findCustomer(id)
    .map(Customer::getAddress)
    .map(Address::getCity)
    .map(City::getName);

// OrElse variants
String name = optional.orElse("default");
String name = optional.orElseGet(() -> computeDefault());
String name = optional.orElseThrow(() -> new NotFoundException("Not found"));

// Conditional execution
optional.ifPresent(customer -> sendEmail(customer));
optional.ifPresentOrElse(
    customer -> sendEmail(customer),
    () -> log.warn("Customer not found")
);

// Filter and transform
Optional<Customer> activeCustomer = findCustomer(id)
    .filter(c -> c.getStatus() == Status.ACTIVE)
    .map(this::enrichWithDetails);

// Combine optionals
Optional<Order> order = customerId
    .flatMap(this::findCustomer)
    .flatMap(this::findLatestOrder);
```

## Stream Patterns

### Collection Processing
```java
// Grouping
Map<Status, List<Order>> byStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::getStatus));

// Grouping with counting
Map<Status, Long> countByStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

// Partitioning
Map<Boolean, List<Order>> partitioned = orders.stream()
    .collect(Collectors.partitioningBy(o -> o.getTotal().compareTo(threshold) > 0));

// To Map
Map<Long, Customer> byId = customers.stream()
    .collect(Collectors.toMap(Customer::getId, Function.identity()));

// Joining
String names = customers.stream()
    .map(Customer::getName)
    .collect(Collectors.joining(", "));
```

### Advanced Collectors
```java
// Custom collector for statistics
record Stats(int count, double sum, double avg) {}

Stats stats = orders.stream()
    .collect(Collectors.teeing(
        Collectors.counting(),
        Collectors.summingDouble(o -> o.getTotal().doubleValue()),
        (count, sum) -> new Stats(count.intValue(), sum, sum / count)
    ));

// Nested grouping
Map<String, Map<Status, List<Order>>> byRegionAndStatus = orders.stream()
    .collect(Collectors.groupingBy(
        Order::getRegion,
        Collectors.groupingBy(Order::getStatus)
    ));
```

## Functional Interfaces

```java
// Custom functional interface
@FunctionalInterface
public interface Validator<T> {
    ValidationResult validate(T input);
    
    default Validator<T> and(Validator<T> other) {
        return input -> {
            var result = this.validate(input);
            return result.isValid() ? other.validate(input) : result;
        };
    }
}

// Usage
Validator<String> notEmpty = s -> s.isEmpty() 
    ? ValidationResult.invalid("Empty") 
    : ValidationResult.valid();

Validator<String> maxLength = s -> s.length() > 100 
    ? ValidationResult.invalid("Too long") 
    : ValidationResult.valid();

Validator<String> combined = notEmpty.and(maxLength);
```

## Result Pattern (Alternative to Exceptions)

```java
public sealed interface Result<T> permits Success, Failure {
    
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }
    
    static <T> Result<T> failure(String error) {
        return new Failure<>(error);
    }
    
    <U> Result<U> map(Function<T, U> mapper);
    <U> Result<U> flatMap(Function<T, Result<U>> mapper);
    T orElse(T defaultValue);
    T orElseThrow();
}

record Success<T>(T value) implements Result<T> {
    public <U> Result<U> map(Function<T, U> mapper) {
        return Result.success(mapper.apply(value));
    }
    // ... other implementations
}

record Failure<T>(String error) implements Result<T> {
    public <U> Result<U> map(Function<T, U> mapper) {
        return new Failure<>(error);
    }
    // ... other implementations
}

// Usage
public Result<Customer> createCustomer(CreateRequest request) {
    return validateRequest(request)
        .flatMap(this::checkDuplicateEmail)
        .map(this::saveCustomer);
}
```

## Builder Pattern

```java
public class Customer {
    private final Long id;
    private final String name;
    private final String email;
    private final Address address;
    
    private Customer(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name);
        this.email = Objects.requireNonNull(builder.email);
        this.address = builder.address;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public Builder toBuilder() {
        return new Builder()
            .id(this.id)
            .name(this.name)
            .email(this.email)
            .address(this.address);
    }
    
    public static class Builder {
        private Long id;
        private String name;
        private String email;
        private Address address;
        
        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder address(Address address) { this.address = address; return this; }
        
        public Customer build() {
            return new Customer(this);
        }
    }
}
```

## Service Layer Pattern

```java
@ApplicationScoped
public class OrderService {
    
    @Inject
    OrderRepository repository;
    
    @Inject
    PaymentService paymentService;
    
    @Inject
    Event<OrderCreated> orderCreatedEvent;
    
    @Transactional
    public Result<Order> createOrder(CreateOrderRequest request) {
        return validateOrder(request)
            .flatMap(this::checkInventory)
            .flatMap(this::processPayment)
            .map(this::saveOrder)
            .peek(order -> orderCreatedEvent.fire(new OrderCreated(order.getId())));
    }
    
    private Result<Order> validateOrder(CreateOrderRequest request) {
        if (request.getItems().isEmpty()) {
            return Result.failure("Order must have items");
        }
        return Result.success(Order.from(request));
    }
}
```
