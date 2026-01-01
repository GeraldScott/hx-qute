# HX Qute Architecture Guide

A comprehensive technical reference for developing features in this Quarkus + HTMX + Qute application.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Technology Stack](#2-technology-stack)
3. [Project Structure](#3-project-structure)
4. [Database Layer](#4-database-layer)
5. [Entity Layer](#5-entity-layer)
6. [Repository Layer](#6-repository-layer)
7. [Service Layer](#7-service-layer)
8. [Resource Layer](#8-resource-layer)
9. [Template System](#9-template-system)
10. [HTMX Integration](#10-htmx-integration)
11. [Security Architecture](#11-security-architecture)
12. [Configuration Reference](#12-configuration-reference)
13. [Testing Patterns](#13-testing-patterns)
14. [Development Workflow](#14-development-workflow)

---

## 1. Overview

HX Qute is a reference implementation demonstrating modern server-side web development using the hypermedia-driven application (HDA) pattern. It combines Quarkus's reactive capabilities with HTMX's HTML-over-the-wire approach, eliminating the need for complex JavaScript frameworks while delivering responsive, interactive user experiences.

### Design Principles

| Principle | Implementation |
|-----------|----------------|
| Server-Side Rendering | All HTML generated on the server via Qute templates |
| Hypermedia-Driven | HTMX handles partial page updates without full reloads |
| Type Safety | `@CheckedTemplate` ensures compile-time template validation |
| Fragment-Based UI | Qute fragments enable reusable, modal-based CRUD patterns |
| Security by Default | Form authentication with BCrypt password hashing |
| Separation of Concerns | Repository pattern separates data access from business logic |

### Architectural Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Resource Layer (REST)                        │
│            Handles HTTP requests, returns TemplateInstance          │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Service Layer (CDI)                          │
│         Business logic, validation, constraint checking             │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Repository Layer (Panache)                     │
│             Data access, queries, CRUD operations                   │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Entity Layer (JPA)                           │
│              Plain POJOs with JPA annotations                       │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       Database (PostgreSQL)                         │
│              Tables, constraints, indexes                           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

### 2.1 Core Framework

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Quarkus | 3.30.3 |
| Language | Java | 21 |
| Build Tool | Maven | 3.x |

### 2.2 Backend Dependencies

| Purpose | Extension | Description |
|---------|-----------|-------------|
| REST API | `quarkus-rest` | RESTEasy Reactive endpoints |
| Templating | `quarkus-rest-qute` | Type-safe Qute template integration |
| ORM | `quarkus-hibernate-orm-panache` | Repository pattern for entities |
| Database | `quarkus-jdbc-postgresql` | PostgreSQL JDBC driver |
| Migrations | `quarkus-flyway` | Versioned schema migrations |
| Security | `quarkus-security-jpa` | JPA-based identity provider with BCrypt |
| Validation | `quarkus-hibernate-validator` | Bean validation (JSR-380) |
| CDI | `quarkus-arc` | Dependency injection |
| Testing | `quarkus-junit5` | JUnit 5 integration |

### 2.3 Frontend Stack (CDN-Based)

| Purpose | Technology | Version | CDN |
|---------|------------|---------|-----|
| Dynamic UI | HTMX | 2.0.8 | jsdelivr.net |
| CSS Framework | UIkit | 3.25.4 | jsdelivr.net |
| Custom Styles | CSS | - | Local `/style.css` |

### 2.4 Database

| Component | Technology |
|-----------|------------|
| RDBMS | PostgreSQL 17 |
| Migrations | Flyway |
| ORM | Hibernate with Panache |

---

## 3. Project Structure

```
src/main/java/io/archton/scaffold/
├── entity/                    # JPA Entities (POJOs)
│   ├── Gender.java
│   ├── Title.java
│   ├── Person.java
│   └── UserLogin.java
├── repository/                # PanacheRepository implementations
│   ├── GenderRepository.java
│   ├── TitleRepository.java
│   ├── PersonRepository.java
│   └── UserLoginRepository.java
├── service/                   # Business logic and validation
│   ├── GenderService.java
│   ├── TitleService.java
│   ├── PersonService.java
│   └── exception/
│       ├── ValidationException.java
│       ├── UniqueConstraintException.java
│       └── ReferentialIntegrityException.java
├── router/                    # JAX-RS Resources (Controllers)
│   ├── GenderResource.java
│   ├── TitleResource.java
│   ├── PersonResource.java
│   └── AuthResource.java
└── filter/                    # HTTP Filters
    └── SecurityFilter.java

src/main/resources/
├── templates/
│   ├── base.html              # Base layout
│   ├── GenderResource/
│   │   └── gender.html        # Full page + fragments
│   ├── TitleResource/
│   │   └── title.html
│   └── PersonResource/
│       └── person.html
├── db/migration/              # Flyway migrations
│   ├── V001__Create_user_login_table.sql
│   ├── V002__Create_gender_table.sql
│   └── V003__Create_title_table.sql
└── application.properties
```

---

## 4. Database Layer

### 4.1 Migration Strategy

Flyway manages schema evolution with versioned SQL scripts:

**Naming Convention**: `V{version}__{description}.sql`

**Example**: `V002__Create_gender_table.sql`

**Best Practices**:
- Never modify existing migrations in production
- Test migrations against a clean database before committing
- Use `BIGSERIAL` for primary keys with `GenerationType.IDENTITY`

### 4.2 PostgreSQL-Specific Patterns

**Identity Columns** (use BIGSERIAL for auto-increment):

```sql
CREATE TABLE entity_name (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    -- Unique constraints
    CONSTRAINT uk_entity_name_code UNIQUE (code),
    CONSTRAINT uk_entity_name_description UNIQUE (description)
);

-- Foreign key example
ALTER TABLE person 
    ADD CONSTRAINT fk_person_gender 
    FOREIGN KEY (gender_id) REFERENCES gender(id);
```

**Important**: Use `BIGSERIAL` with `GenerationType.IDENTITY` in JPA. Do NOT use sequences with Panache entities.

---

## 5. Entity Layer

### 5.1 Entity Pattern (Plain JPA)

Entities are plain POJOs with JPA annotations. They contain NO business logic or data access methods. This follows the Repository pattern where entities are simple data containers.

```java
package io.archton.scaffold.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "gender", uniqueConstraints = {
    @UniqueConstraint(name = "uk_gender_code", columnNames = "code"),
    @UniqueConstraint(name = "uk_gender_description", columnNames = "description")
})
public class Gender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 1)
    private String code;

    @Column(name = "description", nullable = false, unique = true, length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Default constructor required by JPA
    public Gender() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    // Lifecycle callbacks for audit timestamps
    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

### 5.2 Entity with Foreign Key Relationships

```java
@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    // Many-to-One: Person has one Gender
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_person_gender"))
    private Gender gender;

    // Many-to-One: Person has one Title
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id",
        foreignKey = @ForeignKey(name = "fk_person_title"))
    private Title title;

    // Audit fields and lifecycle callbacks...
}
```

### 5.3 UserLogin Entity (Security)

```java
@Entity
@Table(name = "user_login")
@UserDefinition
public class UserLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Username
    @Column(nullable = false, unique = true)
    private String email;

    @Password(value = PasswordType.MCF)
    @Column(nullable = false)
    private String password;

    @Roles
    @Column(nullable = false)
    private String role;

    // Getters and setters...
}
```

---

## 6. Repository Layer

### 6.1 PanacheRepository Pattern

Repositories implement `PanacheRepository<Entity, ID>` and contain all data access logic. This separates data access concerns from entities, enabling better testability and clearer code organization.

```java
package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Gender;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GenderRepository implements PanacheRepository<Gender> {

    /**
     * Find gender by unique code.
     */
    public Optional<Gender> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    /**
     * Find gender by unique description.
     */
    public Optional<Gender> findByDescription(String description) {
        return find("description", description).firstResultOptional();
    }

    /**
     * Check if a code exists (for unique constraint validation).
     */
    public boolean existsByCode(String code) {
        return count("code", code) > 0;
    }

    /**
     * Check if a code exists for a different entity (for update validation).
     */
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return count("code = ?1 AND id != ?2", code, id) > 0;
    }

    /**
     * Check if a description exists (for unique constraint validation).
     */
    public boolean existsByDescription(String description) {
        return count("description", description) > 0;
    }

    /**
     * Check if a description exists for a different entity (for update validation).
     */
    public boolean existsByDescriptionAndIdNot(String description, Long id) {
        return count("description = ?1 AND id != ?2", description, id) > 0;
    }

    /**
     * List all genders ordered by code.
     */
    public List<Gender> listAllOrdered() {
        return list("ORDER BY code ASC");
    }
}
```

### 6.2 Repository with Referential Integrity Queries

For entities that are referenced by other entities, the repository should include methods to check if the entity is in use:

```java
@ApplicationScoped
public class GenderRepository implements PanacheRepository<Gender> {

    @Inject
    EntityManager em;

    // ... finder methods from above ...

    /**
     * Check if gender is referenced by any Person records.
     * Used to prevent deletion when referential integrity would be violated.
     */
    public boolean isReferencedByPerson(Long genderId) {
        Long count = em.createQuery(
            "SELECT COUNT(p) FROM Person p WHERE p.gender.id = :genderId", Long.class)
            .setParameter("genderId", genderId)
            .getSingleResult();
        return count > 0;
    }

    /**
     * Count how many Person records reference this gender.
     */
    public long countPersonReferences(Long genderId) {
        return em.createQuery(
            "SELECT COUNT(p) FROM Person p WHERE p.gender.id = :genderId", Long.class)
            .setParameter("genderId", genderId)
            .getSingleResult();
    }
}
```

### 6.3 Repository with Complex Queries

```java
@ApplicationScoped
public class PersonRepository implements PanacheRepository<Person> {

    /**
     * Find person by unique email (case-insensitive).
     */
    public Optional<Person> findByEmail(String email) {
        return find("LOWER(email) = LOWER(?1)", email).firstResultOptional();
    }

    /**
     * Check if email exists (case-insensitive).
     */
    public boolean existsByEmail(String email) {
        return count("LOWER(email) = LOWER(?1)", email) > 0;
    }

    /**
     * Check if email exists for a different person (for update validation).
     */
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return count("LOWER(email) = LOWER(?1) AND id != ?2", email, id) > 0;
    }

    /**
     * Search persons by filter text (firstName, lastName, or email).
     */
    public List<Person> search(String filterText, String sortField, String sortDir) {
        String query = buildSearchQuery(filterText, sortField, sortDir);
        if (filterText == null || filterText.isBlank()) {
            return list(query);
        }
        String pattern = "%" + filterText.toLowerCase() + "%";
        return list(query, pattern, pattern, pattern);
    }

    private String buildSearchQuery(String filterText, String sortField, String sortDir) {
        StringBuilder query = new StringBuilder();
        
        if (filterText != null && !filterText.isBlank()) {
            query.append("LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?2 OR LOWER(email) LIKE ?3 ");
        }
        
        String field = validateSortField(sortField);
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        query.append("ORDER BY ").append(field).append(" ").append(direction);
        
        return query.toString();
    }

    private String validateSortField(String sortField) {
        return switch (sortField) {
            case "lastName" -> "lastName";
            case "email" -> "email";
            default -> "firstName";
        };
    }

    /**
     * Validate that a foreign key reference exists.
     */
    public boolean genderExists(Long genderId) {
        return getEntityManager()
            .createQuery("SELECT COUNT(g) FROM Gender g WHERE g.id = :id", Long.class)
            .setParameter("id", genderId)
            .getSingleResult() > 0;
    }

    /**
     * Validate that a foreign key reference exists (optional field).
     */
    public boolean titleExists(Long titleId) {
        if (titleId == null) return true; // Optional field
        return getEntityManager()
            .createQuery("SELECT COUNT(t) FROM Title t WHERE t.id = :id", Long.class)
            .setParameter("id", titleId)
            .getSingleResult() > 0;
    }
}
```

### 6.4 Standard Repository Methods (Inherited from PanacheRepository)

`PanacheRepository` provides these methods automatically:

| Method | Description |
|--------|-------------|
| `persist(entity)` | Save new entity |
| `persistAndFlush(entity)` | Save and immediately flush |
| `delete(entity)` | Delete entity |
| `deleteById(id)` | Delete by primary key |
| `findById(id)` | Find by primary key |
| `findByIdOptional(id)` | Find by primary key (Optional) |
| `listAll()` | List all entities |
| `count()` | Count all entities |
| `find(query, params)` | Query with HQL/JPQL |
| `list(query, params)` | Query returning list |
| `stream(query, params)` | Query returning stream |
| `getEntityManager()` | Access underlying EntityManager |

---

## 7. Service Layer

### 7.1 Service Pattern

Services contain business logic and validation. They orchestrate repository calls and enforce constraints before data is persisted.

```java
package io.archton.scaffold.service;

import io.archton.scaffold.entity.Gender;
import io.archton.scaffold.repository.GenderRepository;
import io.archton.scaffold.service.exception.UniqueConstraintException;
import io.archton.scaffold.service.exception.ReferentialIntegrityException;
import io.archton.scaffold.service.exception.EntityNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GenderService {

    @Inject
    GenderRepository genderRepository;

    /**
     * List all genders ordered by code.
     */
    public List<Gender> listAll() {
        return genderRepository.listAllOrdered();
    }

    /**
     * Find gender by ID.
     */
    public Optional<Gender> findById(Long id) {
        return genderRepository.findByIdOptional(id);
    }

    /**
     * Create a new gender with constraint validation.
     * 
     * @throws UniqueConstraintException if code or description already exists
     */
    @Transactional
    public Gender create(String code, String description, String createdBy) {
        // Validate unique constraints BEFORE attempting to persist
        validateUniqueConstraintsForCreate(code, description);

        Gender gender = new Gender();
        gender.setCode(code.toUpperCase().trim());
        gender.setDescription(description.trim());
        gender.setCreatedBy(createdBy);
        gender.setUpdatedBy(createdBy);

        genderRepository.persist(gender);
        return gender;
    }

    /**
     * Update an existing gender with constraint validation.
     * 
     * @throws EntityNotFoundException if gender not found
     * @throws UniqueConstraintException if code or description conflicts with another record
     */
    @Transactional
    public Gender update(Long id, String code, String description, String updatedBy) {
        Gender gender = genderRepository.findByIdOptional(id)
            .orElseThrow(() -> new EntityNotFoundException("Gender", id));

        // Validate unique constraints, excluding current entity
        validateUniqueConstraintsForUpdate(id, code, description);

        gender.setCode(code.toUpperCase().trim());
        gender.setDescription(description.trim());
        gender.setUpdatedBy(updatedBy);

        // No explicit persist needed - entity is managed
        return gender;
    }

    /**
     * Delete a gender with referential integrity check.
     * 
     * @throws EntityNotFoundException if gender not found
     * @throws ReferentialIntegrityException if gender is referenced by Person records
     */
    @Transactional
    public void delete(Long id) {
        Gender gender = genderRepository.findByIdOptional(id)
            .orElseThrow(() -> new EntityNotFoundException("Gender", id));

        // Check referential integrity BEFORE attempting to delete
        validateReferentialIntegrityForDelete(id);

        genderRepository.delete(gender);
    }

    // ========== Constraint Validation Methods ==========

    /**
     * Validate unique constraints for CREATE operation.
     * Checks that neither code nor description exist in the database.
     */
    private void validateUniqueConstraintsForCreate(String code, String description) {
        if (genderRepository.existsByCode(code.toUpperCase().trim())) {
            throw new UniqueConstraintException("code", code, 
                "A gender with code '" + code + "' already exists.");
        }

        if (genderRepository.existsByDescription(description.trim())) {
            throw new UniqueConstraintException("description", description,
                "A gender with description '" + description + "' already exists.");
        }
    }

    /**
     * Validate unique constraints for UPDATE operation.
     * Checks that code/description don't conflict with OTHER records.
     */
    private void validateUniqueConstraintsForUpdate(Long id, String code, String description) {
        if (genderRepository.existsByCodeAndIdNot(code.toUpperCase().trim(), id)) {
            throw new UniqueConstraintException("code", code,
                "A gender with code '" + code + "' already exists.");
        }

        if (genderRepository.existsByDescriptionAndIdNot(description.trim(), id)) {
            throw new UniqueConstraintException("description", description,
                "A gender with description '" + description + "' already exists.");
        }
    }

    /**
     * Validate referential integrity for DELETE operation.
     * Checks that no dependent records exist.
     */
    private void validateReferentialIntegrityForDelete(Long id) {
        if (genderRepository.isReferencedByPerson(id)) {
            long count = genderRepository.countPersonReferences(id);
            throw new ReferentialIntegrityException("Gender", id, "Person", count,
                "Cannot delete gender: It is referenced by " + count + " person record(s).");
        }
    }
}
```

### 7.2 Service with Foreign Key Validation

For services that create/update entities with foreign key relationships:

```java
@ApplicationScoped
public class PersonService {

    @Inject
    PersonRepository personRepository;

    @Inject
    GenderRepository genderRepository;

    @Inject
    TitleRepository titleRepository;

    /**
     * Create a new person with constraint validation.
     * 
     * @throws UniqueConstraintException if email already exists
     * @throws ReferentialIntegrityException if gender or title doesn't exist
     */
    @Transactional
    public Person create(PersonCreateRequest request, String createdBy) {
        // 1. Validate unique constraints
        validateUniqueConstraintsForCreate(request.getEmail());

        // 2. Validate foreign key references exist
        Gender gender = validateAndFetchGender(request.getGenderId());
        Title title = validateAndFetchTitle(request.getTitleId()); // May be null

        // 3. Create entity
        Person person = new Person();
        person.setFirstName(request.getFirstName().trim());
        person.setLastName(request.getLastName().trim());
        person.setEmail(request.getEmail().toLowerCase().trim());
        person.setGender(gender);
        person.setTitle(title);
        person.setCreatedBy(createdBy);
        person.setUpdatedBy(createdBy);

        personRepository.persist(person);
        return person;
    }

    /**
     * Update an existing person with constraint validation.
     */
    @Transactional
    public Person update(Long id, PersonUpdateRequest request, String updatedBy) {
        Person person = personRepository.findByIdOptional(id)
            .orElseThrow(() -> new EntityNotFoundException("Person", id));

        // 1. Validate unique constraints (excluding current entity)
        validateUniqueConstraintsForUpdate(id, request.getEmail());

        // 2. Validate foreign key references if changed
        if (!person.getGender().getId().equals(request.getGenderId())) {
            person.setGender(validateAndFetchGender(request.getGenderId()));
        }
        if (titleChanged(person.getTitle(), request.getTitleId())) {
            person.setTitle(validateAndFetchTitle(request.getTitleId()));
        }

        // 3. Update fields
        person.setFirstName(request.getFirstName().trim());
        person.setLastName(request.getLastName().trim());
        person.setEmail(request.getEmail().toLowerCase().trim());
        person.setUpdatedBy(updatedBy);

        return person;
    }

    // ========== Validation Helper Methods ==========

    private void validateUniqueConstraintsForCreate(String email) {
        if (personRepository.existsByEmail(email)) {
            throw new UniqueConstraintException("email", email,
                "A person with email '" + email + "' already exists.");
        }
    }

    private void validateUniqueConstraintsForUpdate(Long id, String email) {
        if (personRepository.existsByEmailAndIdNot(email, id)) {
            throw new UniqueConstraintException("email", email,
                "A person with email '" + email + "' already exists.");
        }
    }

    private Gender validateAndFetchGender(Long genderId) {
        return genderRepository.findByIdOptional(genderId)
            .orElseThrow(() -> new ReferentialIntegrityException(
                "Person", null, "Gender", genderId,
                "Invalid gender selection. The selected gender does not exist."));
    }

    private Title validateAndFetchTitle(Long titleId) {
        if (titleId == null) return null; // Title is optional
        return titleRepository.findByIdOptional(titleId)
            .orElseThrow(() -> new ReferentialIntegrityException(
                "Person", null, "Title", titleId,
                "Invalid title selection. The selected title does not exist."));
    }

    private boolean titleChanged(Title current, Long newTitleId) {
        if (current == null && newTitleId == null) return false;
        if (current == null || newTitleId == null) return true;
        return !current.getId().equals(newTitleId);
    }
}
```

### 7.3 Custom Exception Classes

```java
package io.archton.scaffold.service.exception;

/**
 * Thrown when a unique constraint would be violated.
 */
public class UniqueConstraintException extends RuntimeException {
    private final String fieldName;
    private final Object fieldValue;

    public UniqueConstraintException(String fieldName, Object fieldValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() { return fieldName; }
    public Object getFieldValue() { return fieldValue; }
}

/**
 * Thrown when referential integrity would be violated.
 */
public class ReferentialIntegrityException extends RuntimeException {
    private final String entityType;
    private final Long entityId;
    private final String referencedType;
    private final Object referenceInfo;

    public ReferentialIntegrityException(String entityType, Long entityId, 
            String referencedType, Object referenceInfo, String message) {
        super(message);
        this.entityType = entityType;
        this.entityId = entityId;
        this.referencedType = referencedType;
        this.referenceInfo = referenceInfo;
    }

    // Getters...
}

/**
 * Thrown when an entity is not found.
 */
public class EntityNotFoundException extends RuntimeException {
    private final String entityType;
    private final Long entityId;

    public EntityNotFoundException(String entityType, Long entityId) {
        super(entityType + " with ID " + entityId + " not found.");
        this.entityType = entityType;
        this.entityId = entityId;
    }

    // Getters...
}
```

### 7.4 Constraint Validation Summary

| Operation | Unique Constraints | Referential Integrity |
|-----------|-------------------|----------------------|
| **CREATE** | Check field doesn't exist | Verify FK references exist |
| **READ** | N/A | N/A |
| **UPDATE** | Check field doesn't exist for OTHER records | Verify FK references exist (if changed) |
| **DELETE** | N/A | Check no dependent records exist |

### 7.5 Validation Flow Diagram

```
CREATE Operation:
┌─────────────────┐     ┌────────────────────────┐     ┌───────────────────────┐
│ Resource Layer  │────▶│    Service Layer       │────▶│   Repository Layer    │
│ (HTTP Request)  │     │ 1. Unique constraints  │     │   persist(entity)     │
└─────────────────┘     │ 2. FK reference exists │     └───────────────────────┘
                        └────────────────────────┘
                               │ Error?
                               ▼
                        ┌─────────────────────────┐
                        │ UniqueConstraintException│
                        │ or ReferentialIntegrity │
                        │ Exception               │
                        └─────────────────────────┘

UPDATE Operation:
┌─────────────────┐     ┌───────────────────────────┐     ┌───────────────────────┐
│ Resource Layer  │────▶│      Service Layer        │────▶│   Repository Layer    │
│ (HTTP Request)  │     │ 1. Entity exists?         │     │   (managed entity)    │
└─────────────────┘     │ 2. Unique (excl. self)    │     └───────────────────────┘
                        │ 3. FK references exist    │
                        └───────────────────────────┘

DELETE Operation:
┌─────────────────┐     ┌───────────────────────────┐     ┌───────────────────────┐
│ Resource Layer  │────▶│      Service Layer        │────▶│   Repository Layer    │
│ (HTTP Request)  │     │ 1. Entity exists?         │     │   delete(entity)      │
└─────────────────┘     │ 2. No dependent records?  │     └───────────────────────┘
                        └───────────────────────────┘
```

---

## 8. Resource Layer

### 8.1 Resource Pattern

Resources serve as controllers, handling HTTP requests and delegating to services. They return TemplateInstance for HTML responses.

```java
@Path("/genders")
@RolesAllowed("admin")
public class GenderResource {

    @Inject
    GenderService genderService;

    @Inject
    SecurityIdentity securityIdentity;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance gender(
            String title, String currentPage, String userName, List<Gender> genders);
        public static native TemplateInstance gender$table(List<Gender> genders);
        public static native TemplateInstance gender$modal_create(Gender gender, String error);
        public static native TemplateInstance gender$modal_edit(Gender gender, String error);
        public static native TemplateInstance gender$modal_success(String message, List<Gender> genders);
        public static native TemplateInstance gender$modal_success_row(String message, Gender gender);
        public static native TemplateInstance gender$modal_delete(Gender gender, String error);
        public static native TemplateInstance gender$modal_delete_success(Long deletedId);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
        List<Gender> genders = genderService.listAll();

        if ("true".equals(hxRequest)) {
            return Templates.gender$table(genders);
        }

        String userName = getCurrentUsername();
        return Templates.gender("Genders", "gender", userName, genders);
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        return Templates.gender$modal_create(new Gender(), null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance create(
            @FormParam("code") String code,
            @FormParam("description") String description) {
        
        // Basic input validation
        String error = validateInput(code, description);
        if (error != null) {
            Gender gender = new Gender();
            gender.setCode(code);
            gender.setDescription(description);
            return Templates.gender$modal_create(gender, error);
        }

        try {
            genderService.create(code, description, getCurrentUsername());
            List<Gender> genders = genderService.listAll();
            return Templates.gender$modal_success("Gender created successfully.", genders);
            
        } catch (UniqueConstraintException e) {
            Gender gender = new Gender();
            gender.setCode(code);
            gender.setDescription(description);
            return Templates.gender$modal_create(gender, e.getMessage());
        }
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        return genderService.findById(id)
            .map(gender -> Templates.gender$modal_edit(gender, null))
            .orElseThrow(() -> new NotFoundException("Gender not found"));
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance update(
            @PathParam("id") Long id,
            @FormParam("code") String code,
            @FormParam("description") String description) {
        
        String error = validateInput(code, description);
        if (error != null) {
            Gender gender = genderService.findById(id).orElseThrow();
            gender.setCode(code);
            gender.setDescription(description);
            return Templates.gender$modal_edit(gender, error);
        }

        try {
            Gender updated = genderService.update(id, code, description, getCurrentUsername());
            return Templates.gender$modal_success_row("Gender updated successfully.", updated);
            
        } catch (UniqueConstraintException e) {
            Gender gender = genderService.findById(id).orElseThrow();
            gender.setCode(code);
            gender.setDescription(description);
            return Templates.gender$modal_edit(gender, e.getMessage());
            
        } catch (EntityNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance deleteConfirm(@PathParam("id") Long id) {
        return genderService.findById(id)
            .map(gender -> Templates.gender$modal_delete(gender, null))
            .orElseThrow(() -> new NotFoundException("Gender not found"));
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance delete(@PathParam("id") Long id) {
        try {
            genderService.delete(id);
            return Templates.gender$modal_delete_success(id);
            
        } catch (ReferentialIntegrityException e) {
            Gender gender = genderService.findById(id).orElseThrow();
            return Templates.gender$modal_delete(gender, e.getMessage());
            
        } catch (EntityNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    private String validateInput(String code, String description) {
        if (code == null || code.trim().isEmpty()) {
            return "Code is required.";
        }
        if (description == null || description.trim().isEmpty()) {
            return "Description is required.";
        }
        if (code.trim().length() > 1) {
            return "Code must be 1 character or less.";
        }
        return null;
    }

    private String getCurrentUsername() {
        return securityIdentity.isAnonymous() ? null 
            : securityIdentity.getPrincipal().getName();
    }
}
```

### 8.2 Standard CRUD Endpoints

| Method | Path | Handler | Description |
|--------|------|---------|-------------|
| GET | `/entities` | `list()` | List all (full page or table fragment) |
| GET | `/entities/create` | `createForm()` | Return create form modal content |
| POST | `/entities` | `create()` | Submit create form |
| GET | `/entities/{id}/edit` | `editForm()` | Return edit form modal content |
| PUT | `/entities/{id}` | `update()` | Submit edit form |
| GET | `/entities/{id}/delete` | `deleteConfirm()` | Return delete confirmation modal |
| DELETE | `/entities/{id}` | `delete()` | Execute deletion |

### 8.3 Exception Handling in Resources

```java
// Map service exceptions to HTTP responses

// Option 1: Explicit try-catch (shown above)

// Option 2: Global ExceptionMapper
@Provider
public class ServiceExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof EntityNotFoundException) {
            return Response.status(Status.NOT_FOUND)
                .entity(exception.getMessage())
                .build();
        }
        if (exception instanceof UniqueConstraintException) {
            return Response.status(Status.CONFLICT)
                .entity(exception.getMessage())
                .build();
        }
        if (exception instanceof ReferentialIntegrityException) {
            return Response.status(Status.CONFLICT)
                .entity(exception.getMessage())
                .build();
        }
        // Re-throw unexpected exceptions
        throw exception;
    }
}
```

---

## 9. Template System

### 9.1 Qute Fragments for HTMX

Templates use `{#fragment}` sections for partial responses:

```html
{@java.util.List<io.archton.scaffold.entity.Gender> genders}
{@java.lang.String title}
{@java.lang.String currentPage}
{@java.lang.String userName}

{#include base.html}
{#title}{title}{/title}

{#content}
<div class="uk-container">
    <h1>Gender Management</h1>
    
    <button class="uk-button uk-button-primary"
            hx-get="/genders/create"
            hx-target="#modal-content"
            hx-on::after-request="UIkit.modal('#crud-modal').show()">
        Add Gender
    </button>

    <div id="table-container">
        {#include gender$table genders=genders /}
    </div>
</div>

<!-- Modal Shell -->
<div id="crud-modal" uk-modal>
    <div class="uk-modal-dialog">
        <div id="modal-content"></div>
    </div>
</div>
{/content}
{/include}

{#fragment id=table}
<table class="uk-table uk-table-striped">
    <thead>
        <tr>
            <th>Code</th>
            <th>Description</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody id="genders-table-body">
        {#for gender in genders}
        <tr id="gender-row-{gender.id}">
            <td>{gender.code}</td>
            <td>{gender.description}</td>
            <td>
                <div class="uk-button-group">
                    <button class="uk-button uk-button-small uk-button-primary"
                            hx-get="/genders/{gender.id}/edit"
                            hx-target="#modal-content"
                            hx-on::after-request="UIkit.modal('#crud-modal').show()">
                        Edit
                    </button>
                    <button class="uk-button uk-button-small uk-button-danger"
                            hx-get="/genders/{gender.id}/delete"
                            hx-target="#modal-content"
                            hx-on::after-request="UIkit.modal('#crud-modal').show()">
                        Delete
                    </button>
                </div>
            </td>
        </tr>
        {/for}
    </tbody>
</table>
{/fragment}

{#fragment id=modal_create}
{@io.archton.scaffold.entity.Gender gender}
{@java.lang.String error}
<div class="uk-modal-header">
    <h2 class="uk-modal-title">Create Gender</h2>
</div>
<div class="uk-modal-body">
    {#if error}
    <div class="uk-alert uk-alert-danger">{error}</div>
    {/if}
    <form hx-post="/genders" hx-target="#modal-content">
        <div class="uk-margin">
            <label class="uk-form-label">Code</label>
            <input class="uk-input" type="text" name="code" 
                   value="{gender.code ?: ''}" maxlength="1" required>
        </div>
        <div class="uk-margin">
            <label class="uk-form-label">Description</label>
            <input class="uk-input" type="text" name="description"
                   value="{gender.description ?: ''}" maxlength="255" required>
        </div>
        <div class="uk-margin">
            <button class="uk-button uk-button-primary" type="submit">Save</button>
            <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
        </div>
    </form>
</div>
{/fragment}

{#fragment id=modal_success}
{@java.lang.String message}
{@java.util.List<io.archton.scaffold.entity.Gender> genders}
<div class="uk-modal-body" hx-on::load="UIkit.modal('#crud-modal').hide()">
    <div class="uk-alert uk-alert-success">{message}</div>
</div>
<!-- OOB update for table -->
<div id="table-container" hx-swap-oob="innerHTML">
    {#include gender$table genders=genders /}
</div>
{/fragment}

{#fragment id=modal_delete}
{@io.archton.scaffold.entity.Gender gender}
{@java.lang.String error}
<div class="uk-modal-header">
    <h2 class="uk-modal-title">Delete Gender</h2>
</div>
<div class="uk-modal-body">
    {#if error}
    <div class="uk-alert uk-alert-danger">{error}</div>
    {/if}
    <p>Are you sure you want to delete gender "{gender.description}"?</p>
    <div class="uk-margin">
        <button class="uk-button uk-button-danger"
                hx-delete="/genders/{gender.id}"
                hx-target="#modal-content">
            Delete
        </button>
        <button class="uk-button uk-button-default uk-modal-close" type="button">Cancel</button>
    </div>
</div>
{/fragment}

{#fragment id=modal_delete_success}
{@java.lang.Long deletedId}
<div class="uk-modal-body" hx-on::load="UIkit.modal('#crud-modal').hide()">
    <div class="uk-alert uk-alert-success">Gender deleted successfully.</div>
</div>
<!-- OOB remove the deleted row -->
<tr id="gender-row-{deletedId}" hx-swap-oob="delete"></tr>
{/fragment}
```

---

## 10. HTMX Integration

### 10.1 Core HTMX Attributes

| Attribute | Purpose | Example |
|-----------|---------|---------|
| `hx-get` | GET request | `hx-get="/entities"` |
| `hx-post` | POST request | `hx-post="/entities"` |
| `hx-put` | PUT request | `hx-put="/entities/1"` |
| `hx-delete` | DELETE request | `hx-delete="/entities/1"` |
| `hx-target` | Response destination | `hx-target="#modal-content"` |
| `hx-swap` | Swap strategy | `hx-swap="outerHTML"` |
| `hx-swap-oob` | Out-of-band swap | `hx-swap-oob="innerHTML"` |
| `hx-trigger` | Event trigger | `hx-trigger="click"` |
| `hx-on::event` | Inline handler | `hx-on::load="closeModal()"` |

### 10.2 Modal-Based CRUD Pattern

```
User clicks "Add" ──▶ GET /entities/create ──▶ Modal opens with form
                                                      │
User submits form ──▶ POST /entities ────────────────┘
                           │
              ┌────────────┴────────────┐
              │                         │
        Validation Error          Success
              │                         │
              ▼                         ▼
    Re-render form with         Close modal +
    error message in modal      OOB table refresh
```

---

## 11. Creating New Entities - Complete Checklist

When creating a new data entity, follow this checklist:

### 11.1 Database Layer

- [ ] Create Flyway migration: `VXXX__Create_entity_table.sql`
- [ ] Define primary key with `BIGSERIAL`
- [ ] Add unique constraints with named constraints
- [ ] Add foreign key constraints with named constraints
- [ ] Add audit columns (created_at, updated_at, created_by, updated_by)

### 11.2 Entity Layer

- [ ] Create entity class in `entity/` package
- [ ] Add JPA annotations (@Entity, @Table, @Column)
- [ ] Define @Id with GenerationType.IDENTITY
- [ ] Add @UniqueConstraint annotations to @Table
- [ ] Define relationships with @ManyToOne, @OneToMany
- [ ] Add @PrePersist and @PreUpdate for audit timestamps
- [ ] Create getters and setters for all fields

### 11.3 Repository Layer

- [ ] Create repository class implementing `PanacheRepository<Entity, Long>`
- [ ] Add @ApplicationScoped annotation
- [ ] Implement `findByXxx` methods for unique fields
- [ ] Implement `existsByXxx` methods for unique constraint validation
- [ ] Implement `existsByXxxAndIdNot` methods for update validation
- [ ] Implement `isReferencedByXxx` methods if entity can be referenced
- [ ] Implement `listAllOrdered` for default listing

### 11.4 Service Layer

- [ ] Create service class with @ApplicationScoped
- [ ] Inject repository and related repositories
- [ ] Implement `listAll()` method
- [ ] Implement `findById()` method
- [ ] Implement `create()` with unique constraint validation
- [ ] Implement `update()` with unique constraint validation (excluding self)
- [ ] Implement `delete()` with referential integrity validation
- [ ] Add @Transactional to write methods

### 11.5 Resource Layer

- [ ] Create resource class with @Path
- [ ] Add security annotations (@RolesAllowed)
- [ ] Define @CheckedTemplate inner class with all fragment methods
- [ ] Implement list() endpoint (full page + HTMX fragment)
- [ ] Implement createForm() endpoint
- [ ] Implement create() endpoint with exception handling
- [ ] Implement editForm() endpoint
- [ ] Implement update() endpoint with exception handling
- [ ] Implement deleteConfirm() endpoint
- [ ] Implement delete() endpoint with exception handling

### 11.6 Template Layer

- [ ] Create template file: `templates/EntityResource/entity.html`
- [ ] Define type declarations for all variables
- [ ] Create table fragment (`{#fragment id=table}`)
- [ ] Create modal_create fragment
- [ ] Create modal_edit fragment
- [ ] Create modal_success fragment with OOB updates
- [ ] Create modal_delete fragment
- [ ] Create modal_delete_success fragment with OOB row removal

---

## 12. Testing Patterns

### 12.1 Repository Tests

```java
@QuarkusTest
@TestTransaction
class GenderRepositoryTest {

    @Inject
    GenderRepository genderRepository;

    @Test
    void shouldFindByCode() {
        Gender gender = createTestGender("M", "Male");
        genderRepository.persist(gender);

        Optional<Gender> found = genderRepository.findByCode("M");
        
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Male");
    }

    @Test
    void shouldCheckExistsByCodeAndIdNot() {
        Gender gender1 = createTestGender("M", "Male");
        Gender gender2 = createTestGender("F", "Female");
        genderRepository.persist(gender1);
        genderRepository.persist(gender2);

        // Should return true when code exists for different ID
        assertThat(genderRepository.existsByCodeAndIdNot("M", gender2.getId())).isTrue();
        
        // Should return false when checking same ID
        assertThat(genderRepository.existsByCodeAndIdNot("M", gender1.getId())).isFalse();
    }
}
```

### 12.2 Service Tests

```java
@QuarkusTest
class GenderServiceTest {

    @Inject
    GenderService genderService;

    @Test
    @TestTransaction
    void shouldThrowUniqueConstraintExceptionOnDuplicateCode() {
        genderService.create("M", "Male", "test");

        assertThatThrownBy(() -> genderService.create("M", "Another", "test"))
            .isInstanceOf(UniqueConstraintException.class)
            .hasMessageContaining("code")
            .hasMessageContaining("M");
    }

    @Test
    @TestTransaction
    void shouldThrowReferentialIntegrityExceptionWhenInUse() {
        // Setup: Create gender and person using it
        Gender gender = genderService.create("M", "Male", "test");
        // ... create person with this gender ...

        assertThatThrownBy(() -> genderService.delete(gender.getId()))
            .isInstanceOf(ReferentialIntegrityException.class)
            .hasMessageContaining("referenced");
    }
}
```

---

## 13. Configuration Reference

### 13.1 Database Configuration

```properties
# PostgreSQL connection
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USER:scaffold}
quarkus.datasource.password=${DB_PASS:scaffold}
quarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:scaffold}

# Hibernate settings
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.log.sql=false

# Flyway migrations
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
```

### 13.2 Security Configuration

```properties
# Form authentication
quarkus.http.auth.form.enabled=true
quarkus.http.auth.form.login-page=/login
quarkus.http.auth.form.landing-page=/
quarkus.http.auth.form.error-page=/login?error=true

# Route permissions
quarkus.http.auth.permission.admin.paths=/genders,/genders/*,/titles,/titles/*
quarkus.http.auth.permission.admin.policy=admin
quarkus.http.auth.permission.authenticated.paths=/persons,/persons/*
quarkus.http.auth.permission.authenticated.policy=authenticated
```

---

*Document Version: 2.0*
*Pattern: PanacheRepository with Service Layer*
*Last Updated: 2026-01-01*
