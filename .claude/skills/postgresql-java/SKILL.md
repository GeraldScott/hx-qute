---
name: postgresql-java
description: PostgreSQL database development with Java including schema design, Flyway migrations, query optimization, indexing strategies, and Panache/JDBC patterns. Use when designing database schemas, writing migrations, optimizing queries, troubleshooting database performance, or integrating PostgreSQL with Quarkus/Java applications.
---

# PostgreSQL with Java

## Flyway Migrations

### Naming Convention
```
V{version}__{description}.sql
V1__create_customers_table.sql
V2__add_email_column.sql
V2.1__create_index_on_email.sql
```

### Migration Template
```sql
-- V1__create_customers_table.sql
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_status ON customers(status) WHERE status = 'ACTIVE';

COMMENT ON TABLE customers IS 'Customer master data';
COMMENT ON COLUMN customers.status IS 'ACTIVE, INACTIVE, SUSPENDED';
```

### Repeatable Migrations (Views, Functions)
```sql
-- R__customer_summary_view.sql
CREATE OR REPLACE VIEW customer_summary AS
SELECT 
    c.id,
    c.name,
    COUNT(o.id) as order_count,
    COALESCE(SUM(o.total), 0) as total_spent
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
GROUP BY c.id, c.name;
```

## Entity Mapping

### Basic Entity
```java
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customers_email", columnList = "email"),
    @Index(name = "idx_customers_status", columnList = "status")
})
public class Customer extends PanacheEntity {
    
    @Column(nullable = false, length = 255)
    public String name;
    
    @Column(unique = true, nullable = false)
    public String email;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    public Status status = Status.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    public Instant updatedAt;
    
    @Version
    public Long version;  // Optimistic locking
}
```

### Relationships
```java
// One-to-Many
@Entity
public class Customer extends PanacheEntity {
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Order> orders = new ArrayList<>();
}

@Entity
public class Order extends PanacheEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;
}

// Many-to-Many
@Entity
public class Product extends PanacheEntity {
    @ManyToMany
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    public Set<Category> categories = new HashSet<>();
}
```

## Query Patterns

### Panache Queries
```java
// Simple queries
Customer.find("email", email).firstResult();
Customer.find("status", Status.ACTIVE).list();
Customer.find("name like ?1", "%" + term + "%").list();

// Sorting and pagination
Customer.find("status", Sort.by("name").ascending(), Status.ACTIVE)
    .page(Page.of(pageNum, pageSize))
    .list();

// Projections
Customer.find("SELECT name, email FROM Customer WHERE status = ?1", Status.ACTIVE)
    .project(CustomerSummary.class)
    .list();
```

### Native Queries
```java
@Repository
public class CustomerRepository implements PanacheRepository<Customer> {
    
    @Inject
    EntityManager em;
    
    public List<CustomerStats> getCustomerStats() {
        return em.createNativeQuery("""
            SELECT 
                c.id,
                c.name,
                COUNT(o.id) as order_count,
                COALESCE(SUM(o.total), 0) as total_spent
            FROM customers c
            LEFT JOIN orders o ON o.customer_id = c.id
            WHERE c.status = 'ACTIVE'
            GROUP BY c.id, c.name
            HAVING COUNT(o.id) > 0
            ORDER BY total_spent DESC
            """, CustomerStats.class)
            .getResultList();
    }
}
```

## Indexing Strategies

### When to Index
```sql
-- Columns in WHERE clauses
CREATE INDEX idx_orders_customer_id ON orders(customer_id);

-- Columns in JOIN conditions
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Columns in ORDER BY
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

-- Composite for common query patterns
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);

-- Partial index for common filters
CREATE INDEX idx_orders_pending ON orders(created_at) 
    WHERE status = 'PENDING';

-- Covering index (includes all needed columns)
CREATE INDEX idx_customers_email_name ON customers(email) INCLUDE (name);
```

### Query Analysis
```sql
-- Always check query plans
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM orders WHERE customer_id = 123 AND status = 'PENDING';

-- Common issues:
-- Seq Scan on large tables = missing index
-- High "Buffers: shared read" = cold cache
-- "Rows Removed by Filter" >> actual rows = poor selectivity
```

## Performance Patterns

### Batch Operations
```java
// ❌ Slow: Individual inserts
for (Customer c : customers) {
    customerRepo.persist(c);
}

// ✅ Fast: Batch insert
@Transactional
public void batchInsert(List<Customer> customers) {
    for (int i = 0; i < customers.size(); i++) {
        em.persist(customers.get(i));
        if (i % 50 == 0) {
            em.flush();
            em.clear();
        }
    }
}
```

### Avoiding N+1
```java
// ❌ N+1 problem
List<Order> orders = Order.listAll();
for (Order o : orders) {
    log.info(o.customer.name);  // Lazy load per iteration
}

// ✅ Eager fetch with JOIN FETCH
@Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.status = ?1")
List<Order> findByStatusWithCustomer(Status status);

// ✅ Entity graph
@EntityGraph(attributePaths = {"customer", "items"})
List<Order> findByStatus(Status status);
```

### Connection Pool (application.properties)
```properties
# Pool sizing
quarkus.datasource.jdbc.min-size=5
quarkus.datasource.jdbc.max-size=20
quarkus.datasource.jdbc.acquisition-timeout=30S

# Statement caching
quarkus.datasource.jdbc.additional-jdbc-properties.preparedStatementCacheQueries=256
quarkus.datasource.jdbc.additional-jdbc-properties.preparedStatementCacheSizeMiB=5

# Connection validation
quarkus.datasource.jdbc.validation-query-sql=SELECT 1
quarkus.datasource.jdbc.idle-removal-interval=2M
```

## Common PostgreSQL Types

```java
// UUID primary key
@Id
@GeneratedValue
@Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
public UUID id;

// JSONB
@Type(JsonBinaryType.class)
@Column(columnDefinition = "jsonb")
public Map<String, Object> metadata;

// Array
@Type(ListArrayType.class)
@Column(columnDefinition = "text[]")
public List<String> tags;

// Enum as PostgreSQL enum
@Enumerated(EnumType.STRING)
@Column(columnDefinition = "order_status")
@Type(PostgreSQLEnumType.class)
public OrderStatus status;
```

## Useful Queries

```sql
-- Find slow queries
SELECT query, calls, mean_time, total_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;

-- Table sizes
SELECT 
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Index usage
SELECT 
    indexrelname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Unused indexes
SELECT indexrelname FROM pg_stat_user_indexes WHERE idx_scan = 0;

-- Lock monitoring
SELECT * FROM pg_locks WHERE NOT granted;
```
