# Technical Specification for Feature 000: Foundation

This document describes the technical implementation details for the authentication infrastructure.

---

## Dependencies

### Maven Dependencies (pom.xml)

```xml
<!-- JPA-based security with built-in BCrypt support -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security-jpa</artifactId>
</dependency>

<!-- Bean validation for entity constraints -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>
```

---

## Configuration Reference

### application.properties additions for Feature 000

```properties
# =============================================================================
# Feature 000: Password Policy Configuration (NIST SP 800-63B-4)
# =============================================================================

app.security.password.min-length=15
app.security.password.max-length=128
```

---

## Database Schema

### user_login Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-incrementing identifier |
| email | VARCHAR(255) | NOT NULL, UNIQUE | User's email address (login identifier) |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed password in MCF format |
| role | VARCHAR(50) | NOT NULL, DEFAULT 'user' | User role for authorization |
| first_name | VARCHAR(100) | NULLABLE | User's first name |
| last_name | VARCHAR(100) | NULLABLE | User's last name |
| created_at | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| active | BOOLEAN | DEFAULT TRUE | Account active status |

### Indexes

| Index Name | Column | Purpose |
|------------|--------|---------|
| idx_user_login_email | email | Efficient email lookups for authentication |

### Constraints

| Constraint Name | Type | Column | Purpose |
|----------------|------|--------|---------|
| uq_user_login_email | UNIQUE | email | Prevent duplicate accounts |

---

## Architecture Overview

The authentication infrastructure uses a layered architecture pattern:

```
┌─────────────────────────────────────────────────────────────────┐
│                        AuthResource                              │
│              (REST endpoint for signup/logout)                   │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌─────────────────────┐ ┌─────────────────┐ ┌───────────────────────┐
│  UserLoginService   │ │PasswordValidator│ │   Quarkus Security    │
│ (user creation,     │ │ (NIST password  │ │ (authentication via   │
│  BCrypt hashing)    │ │  validation)    │ │  @UserDefinition)     │
└─────────────────────┘ └─────────────────┘ └───────────────────────┘
              │                                         │
              ▼                                         │
┌─────────────────────┐                                │
│ UserLoginRepository │◄───────────────────────────────┘
│  (Panache queries)  │
└─────────────────────┘
              │
              ▼
┌─────────────────────┐
│   UserLogin Entity  │
│   (JPA mapping)     │
└─────────────────────┘
```

---

## Entity Design

### UserLogin Entity

**Package:** `io.archton.scaffold.entity`

**Annotations:**
- `@Entity`, `@Table(name = "user_login")` - JPA entity mapping
- `@UserDefinition` - Quarkus Security JPA integration
- `@Username` on email - Identifies login field
- `@Password(PasswordType.MCF)` on password - BCrypt hash verification
- `@Roles` on role - Authorization roles

**Fields:**

| Field | Type | Annotations | Description |
|-------|------|-------------|-------------|
| id | Long | `@Id`, `@GeneratedValue` | Auto-generated primary key |
| email | String | `@Username`, `@NotBlank`, `@Email`, `@Size(max=255)` | Login identifier |
| password | String | `@Password(PasswordType.MCF)`, `@NotBlank` | BCrypt hashed password |
| role | String | `@Roles` | User role (default: "user") |
| firstName | String | `@Size(max=100)` | User's first name |
| lastName | String | `@Size(max=100)` | User's last name |
| createdAt | Instant | `@Column(updatable=false)` | Creation timestamp |
| updatedAt | Instant | | Last update timestamp |
| active | boolean | | Account active status (default: true) |

**Methods:**

| Method | Purpose |
|--------|---------|
| `getDisplayName()` | Returns human-readable name (firstName + lastName) or email fallback |

**Lifecycle Hooks:**
- `@PrePersist`: Sets createdAt/updatedAt timestamps, normalizes email
- `@PreUpdate`: Updates timestamp, normalizes email

---

## Repository Design

### UserLoginRepository

**Package:** `io.archton.scaffold.repository`

**Scope:** `@ApplicationScoped`

**Implements:** `PanacheRepository<UserLogin>`

**Methods:**

| Method | Returns | Description |
|--------|---------|-------------|
| `findByEmail(email)` | `Optional<UserLogin>` | Case-insensitive email lookup |
| `emailExists(email)` | `boolean` | Check if email is already registered |
| `existsByEmailAndIdNot(email, id)` | `boolean` | Check email uniqueness excluding specific user (for updates) |

---

## Service Design

### UserLoginService

**Package:** `io.archton.scaffold.service`

**Scope:** `@ApplicationScoped`

**Dependencies:**
- `UserLoginRepository` - for database operations

**Methods:**

| Method | Returns | Description |
|--------|---------|-------------|
| `create(email, plainPassword, role)` | `UserLogin` | Creates user with BCrypt-hashed password (cost 12) |
| `emailExists(email)` | `boolean` | Delegates to repository for email existence check |

**Behavior:**
- Email is normalized (lowercase, trimmed) before storage
- Password is hashed using BCrypt with cost factor 12
- Throws `UniqueConstraintException` if email already exists

### PasswordValidator Service

**Package:** `io.archton.scaffold.service`

**Scope:** `@ApplicationScoped`

**Configuration Properties:**

| Property | Default | Description |
|----------|---------|-------------|
| `app.security.password.min-length` | 15 | Minimum password length (NIST) |
| `app.security.password.max-length` | 128 | Maximum password length |

**Methods:**

| Method | Returns | Description |
|--------|---------|-------------|
| `validate(password)` | `List<String>` | Returns list of validation errors |
| `isValid(password)` | `boolean` | Convenience method for valid check |

**NIST SP 800-63B-4 Compliance:**
- Minimum 15 characters for password-only authentication
- Maximum 128 characters accepted (exceeds NIST 64-char requirement)
- No composition rules enforced
- No password truncation

---

## Security Considerations

### Password Hashing
- Algorithm: BCrypt
- Cost factor: 12 (provides ~300ms hash time)
- Format: MCF (Modular Crypt Format) - `$2a$12$...`
- Implementation: `BcryptUtil.bcryptHash()` from Quarkus Elytron

### Email Normalization
- Converted to lowercase
- Whitespace trimmed
- Applied on both persist and update (entity lifecycle hooks)
- Applied in service layer before database operations

### Admin User Seed
- Email: `admin@example.com`
- Password: `AdminPassword123` (BCrypt hashed, cost 12)
- Role: `admin`
- Purpose: Initial system access and testing

---

## File Inventory

| File | Purpose |
|------|---------|
| `src/main/java/io/archton/scaffold/entity/UserLogin.java` | JPA entity with security annotations |
| `src/main/java/io/archton/scaffold/repository/UserLoginRepository.java` | Panache repository for database operations |
| `src/main/java/io/archton/scaffold/service/UserLoginService.java` | Service for user creation with BCrypt hashing |
| `src/main/java/io/archton/scaffold/service/PasswordValidator.java` | NIST-compliant password validation |
| `src/main/resources/db/migration/V1.2.0__Create_user_login_table.sql` | Flyway migration for table creation |
| `src/main/resources/db/migration/V1.2.1__Insert_admin_user.sql` | Flyway migration for admin user seed |

---
