---
name: java-code-review
description: Java code review checklist and patterns for identifying bugs, security vulnerabilities, performance issues, and style violations. Use when reviewing Java code, performing code audits, identifying anti-patterns, or when asked to analyze existing Java codebases for quality issues.
---

# Java Code Review

## Review Checklist

### Critical (Must Fix)
- [ ] Null pointer risks without proper handling
- [ ] Resource leaks (streams, connections not closed)
- [ ] SQL injection vulnerabilities
- [ ] Hardcoded credentials or secrets
- [ ] Missing input validation
- [ ] Unchecked exceptions that could crash application
- [ ] Thread safety issues in shared state
- [ ] Infinite loops or recursion without base case

### Important (Should Fix)
- [ ] Missing or incorrect equals/hashCode
- [ ] Mutable objects exposed from getters
- [ ] Empty catch blocks swallowing exceptions
- [ ] N+1 query patterns in ORM code
- [ ] Missing @Transactional where needed
- [ ] Improper use of Optional
- [ ] String concatenation in loops
- [ ] Blocking calls in reactive streams

### Improvement (Nice to Fix)
- [ ] Magic numbers without constants
- [ ] Long methods (>30 lines)
- [ ] Deep nesting (>3 levels)
- [ ] Missing JavaDoc on public API
- [ ] Inconsistent naming conventions
- [ ] Dead code or unused imports

## Common Anti-Patterns

### Null Handling

❌ Bad:
```java
public String getName() {
    return customer.getAddress().getCity().getName();  // NPE risk
}
```

✅ Good:
```java
public Optional<String> getCityName() {
    return Optional.ofNullable(customer)
        .map(Customer::getAddress)
        .map(Address::getCity)
        .map(City::getName);
}
```

### Resource Management

❌ Bad:
```java
InputStream is = new FileInputStream(file);
String content = new String(is.readAllBytes());  // Never closed!
```

✅ Good:
```java
try (var is = new FileInputStream(file)) {
    return new String(is.readAllBytes());
}
```

### Exception Handling

❌ Bad:
```java
try {
    processData();
} catch (Exception e) {
    // Ignored
}
```

✅ Good:
```java
try {
    processData();
} catch (DataException e) {
    log.error("Failed to process: {}", e.getMessage());
    throw new ServiceException("Processing failed", e);
}
```

### Collections

❌ Bad:
```java
List<String> names = new ArrayList<>();
for (Customer c : customers) {
    names.add(c.getName());  // Use Stream
}
```

✅ Good:
```java
List<String> names = customers.stream()
    .map(Customer::getName)
    .toList();
```

### Optional Usage

❌ Bad:
```java
Optional<User> user = findUser(id);
if (user.isPresent()) {
    return user.get().getName();
}
return "Unknown";
```

✅ Good:
```java
return findUser(id)
    .map(User::getName)
    .orElse("Unknown");
```

### Entity Exposure

❌ Bad:
```java
@GET
public Customer getCustomer(@PathParam("id") Long id) {
    return customerRepository.findById(id);  // Exposes internal entity
}
```

✅ Good:
```java
@GET
public CustomerDTO getCustomer(@PathParam("id") Long id) {
    return customerRepository.findById(id)
        .map(this::toDTO)
        .orElseThrow(NotFoundException::new);
}
```

## Security Review Points

### SQL Injection
```java
// ❌ VULNERABLE
query = "SELECT * FROM users WHERE name = '" + userInput + "'";

// ✅ SAFE - Parameterized
query = "SELECT * FROM users WHERE name = ?";
stmt.setString(1, userInput);

// ✅ SAFE - Panache
User.find("name", userInput).firstResult();
```

### Input Validation
```java
// ✅ Validate all external input
public void updateEmail(@Valid @NotBlank @Email String email) {
    // email is validated
}
```

### Sensitive Data
```java
// ❌ Logging sensitive data
log.info("User login: " + username + " password: " + password);

// ✅ Mask sensitive data
log.info("User login: {}", username);
```

## Performance Review Points

### N+1 Queries
```java
// ❌ N+1 problem
for (Order order : orders) {
    order.getCustomer().getName();  // Query per iteration
}

// ✅ Eager fetch
@Query("SELECT o FROM Order o JOIN FETCH o.customer")
List<Order> findAllWithCustomers();
```

### String Concatenation
```java
// ❌ Creates many String objects
String result = "";
for (String s : items) {
    result += s + ",";
}

// ✅ Use StringBuilder
String result = String.join(",", items);
// or
var sb = new StringBuilder();
items.forEach(s -> sb.append(s).append(","));
```

### Boxing/Unboxing
```java
// ❌ Unnecessary boxing
List<Integer> numbers = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    numbers.add(i);  // Auto-boxing
}

// ✅ Use primitive collections for performance-critical code
IntList numbers = new IntArrayList();
```

## Transactional Patterns

### Missing @Transactional
```java
// ❌ No transaction boundary
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepo.findById(fromId);
    Account to = accountRepo.findById(toId);
    from.debit(amount);
    to.credit(amount);  // If this fails, debit already happened!
}

// ✅ Atomic transaction
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepo.findById(fromId);
    Account to = accountRepo.findById(toId);
    from.debit(amount);
    to.credit(amount);
}
```

### Transactional Scope
```java
// ❌ Transaction too broad (holds DB connection during HTTP call)
@Transactional
public void processOrder(Order order) {
    save(order);
    callExternalPaymentAPI(order);  // Slow HTTP call inside transaction!
    updateStatus(order);
}

// ✅ Minimal transaction scope
public void processOrder(Order order) {
    saveOrder(order);  // @Transactional
    PaymentResult result = callExternalPaymentAPI(order);  // Outside TX
    updateOrderStatus(order, result);  // @Transactional
}
```

## Review Comment Templates

### Critical Issue
```
🔴 **CRITICAL**: [Description of the issue]
This could cause [impact]. 
Fix: [Suggested solution]
```

### Important Issue
```
🟡 **IMPORTANT**: [Description of the issue]
Consider [suggestion] to improve [aspect].
```

### Minor Suggestion
```
💡 **Suggestion**: [Optional improvement]
```

### Question
```
❓ **Question**: [Clarification needed]
Could you explain why [specific concern]?
```
