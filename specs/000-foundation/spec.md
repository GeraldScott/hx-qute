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

## Entity Design

### UserLogin Entity

**Package:** `io.archton.scaffold.entity`

**Annotations:**
- `@Entity`, `@Table(name = "user_login")` - JPA entity mapping
- `@UserDefinition` - Quarkus Security JPA integration
- `@Username` on email - Identifies login field
- `@Password(PasswordType.MCF)` on password - BCrypt hash verification
- `@Roles` on role - Authorization roles

**Key Methods:**

| Method | Purpose |
|--------|---------|
| `create(email, password, role)` | Factory method with BCrypt hashing (cost 12) |
| `findByEmail(email)` | Case-insensitive email lookup |
| `emailExists(email)` | Check for duplicate emails |
| `getDisplayName()` | Human-readable name or email fallback |

**Lifecycle Hooks:**
- `@PrePersist`: Sets timestamps, normalizes email
- `@PreUpdate`: Updates timestamp, normalizes email

---

## Service Design

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

### Email Normalization
- Converted to lowercase
- Whitespace trimmed
- Applied on both persist and update

### Admin User Seed
- Email: `admin@example.com`
- Password: `AdminPassword123` (BCrypt hashed)
- Role: `admin`
- Purpose: Initial system access and testing

---
