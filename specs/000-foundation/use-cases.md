# Use Cases for Feature 000: Foundation

This feature establishes the authentication infrastructure required for Identity and Access Management.

## Actors

| Actor | Description |
|-------|-------------|
| Developer | Senior developer implementing the system |
| System | The application runtime and database |

---

# US-000-01: Establish Authentication Infrastructure

## UC-000-01-01: Create UserLogin Database Table

| Attribute | Value |
|-----------|-------|
| Actor | Developer |
| Precondition | PostgreSQL database is accessible |
| Trigger | Developer runs Flyway migrations |

**Main Flow:**
1. System executes Flyway migration V1.2.0
2. System creates `user_login` table with columns:
   - id (BIGSERIAL PRIMARY KEY)
   - email (VARCHAR(255) NOT NULL)
   - password (VARCHAR(255) NOT NULL)
   - role (VARCHAR(50) NOT NULL DEFAULT 'user')
   - first_name (VARCHAR(100))
   - last_name (VARCHAR(100))
   - created_at (TIMESTAMP WITH TIME ZONE)
   - updated_at (TIMESTAMP WITH TIME ZONE)
   - active (BOOLEAN DEFAULT TRUE)
3. System adds unique constraint on email column
4. System creates index on email column for efficient lookups

**Postcondition:** `user_login` table exists with proper schema and constraints

---

## UC-000-01-02: Create UserLogin Entity

| Attribute | Value |
|-----------|-------|
| Actor | Developer |
| Precondition | UC-000-01-01 complete (database table exists) |
| Trigger | Developer creates entity class |

**Main Flow:**
1. Developer creates `UserLogin.java` extending `PanacheEntityBase`
2. Developer adds `@UserDefinition` annotation for Quarkus Security JPA
3. Developer annotates email field with `@Username`
4. Developer annotates password field with `@Password(PasswordType.MCF)`
5. Developer annotates role field with `@Roles`
6. Developer implements `@PrePersist` hook to:
   - Set createdAt and updatedAt timestamps
   - Normalize email to lowercase and trim whitespace
7. Developer implements `@PreUpdate` hook to:
   - Update updatedAt timestamp
   - Normalize email to lowercase and trim whitespace
8. Developer adds `create()` factory method with BCrypt hashing (cost factor 12)
9. Developer adds `findByEmail()` finder method with case-insensitive lookup
10. Developer adds `emailExists()` helper method
11. Developer adds `getDisplayName()` display method

**Postcondition:** UserLogin entity is available for authentication with all required methods

---

## UC-000-01-03: Create PasswordValidator Service

| Attribute | Value |
|-----------|-------|
| Actor | Developer |
| Precondition | None |
| Trigger | Developer creates validation service |

**Main Flow:**
1. Developer creates `PasswordValidator.java` as `@ApplicationScoped` service
2. Developer injects configurable properties:
   - `app.security.password.min-length` (default: 15)
   - `app.security.password.max-length` (default: 128)
3. Developer implements `validate()` method that returns list of errors:
   - Returns "Password is required" if null or empty
   - Returns "Password must be at least {minLength} characters" if too short
   - Returns "Password must be {maxLength} characters or less" if too long
4. Developer implements `isValid()` convenience method

**Password Policy (NIST SP 800-63B-4):**
- Minimum 15 characters for password-only authentication
- Maximum 128 characters accepted
- No composition rules required (no special chars, uppercase, etc.)
- No password truncation

**Postcondition:** PasswordValidator service is available for password validation

---

## UC-000-01-04: Seed Admin User

| Attribute | Value |
|-----------|-------|
| Actor | Developer |
| Precondition | UC-000-01-01 complete (database table exists) |
| Trigger | Developer runs Flyway migrations |

**Main Flow:**
1. System executes Flyway migration V1.2.1
2. System inserts admin user with:
   - email: `admin@example.com`
   - password: BCrypt hash of `AdminPassword123` (cost factor 12)
   - role: `admin`
   - first_name: `Admin`
   - last_name: `User`
   - active: true

**Postcondition:** Admin user exists for testing and initial system access

---
