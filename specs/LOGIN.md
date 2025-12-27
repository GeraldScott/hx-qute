# Technical Specification: Person and UserLogin Entity Design

## 1. Overview

### 1.1 Purpose

This specification defines the data model and security requirements for user identity and authentication in the application. It establishes the separation between personal identity data (`Person`) and authentication credentials (`UserLogin`), following industry best practices and compliance with NIST SP 800-63-4 Digital Identity Guidelines.

### 1.2 Scope

This document covers:

- Entity relationship design for Person and UserLogin
- Database schema definitions
- Username and email handling policies
- Authentication security requirements
- Password storage and management
- Multi-factor authentication considerations

### 1.3 References

| Document | Description |
|----------|-------------|
| NIST SP 800-63-4 | Digital Identity Guidelines (July 2025) |
| NIST SP 800-63B-4 | Authentication and Authenticator Management |
| OWASP Authentication Cheat Sheet | Authentication best practices |
| OWASP ASVS 4.0 | Application Security Verification Standard |

---

## 2. Design Principles

### 2.1 Separation of Concerns

The design separates authentication credentials from personal identity data for the following reasons:

1. **Flexibility**: Allows users to change authentication methods without affecting profile data
2. **Security**: Limits exposure of sensitive credential data
3. **Extensibility**: Supports future integration with third-party identity providers (OAuth, SAML, OIDC)
4. **Data Integrity**: Personal data persists independently of authentication state
5. **Compliance**: Aligns with NIST recommendation to treat digital identity as distinct from authentication

### 2.2 Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| Email stored in `Person` | Email is PII and profile data, not authentication data |
| Username in `UserLogin` | Username is an authentication identifier, may differ from email |
| 1:1 relationship | One person has one login; supports future 1:N for multiple auth methods |
| BCrypt hashing | Industry-standard algorithm with built-in salt; simple integration with Quarkus Security JPA |

---

## 3. Entity Relationship Model

### 3.1 Entity Diagram

```mermaid
erDiagram
    Person ||--o| UserLogin : "has"

    Person {
        BIGINT id PK
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR email UK
        VARCHAR phone
        DATE date_of_birth
        TIMESTAMP created_at
        TIMESTAMP updated_at
        BOOLEAN active
    }

    UserLogin {
        BIGINT id PK
        BIGINT person_id FK,UK
        VARCHAR username UK
        VARCHAR password_hash
        BOOLEAN mfa_enabled
        VARCHAR mfa_secret
        TIMESTAMP last_login
        INT failed_attempts
        TIMESTAMP locked_until
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
```

### 3.2 Relationship Details

| Aspect | Specification |
|--------|---------------|
| Cardinality | One-to-One (1:1) |
| Direction | Bidirectional |
| Owner | `UserLogin` owns the relationship |
| Cascade | `Person` cascades ALL to `UserLogin` |
| Orphan Removal | Enabled |
| Optional | `UserLogin` is optional (Person can exist without login) |

---

## 4. Database Schema

### 4.1 Person Table

```sql
CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100),              -- Optional, populated via profile
    last_name VARCHAR(100),               -- Optional, populated via profile
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),              -- Audit: who created this record
    updated_by VARCHAR(255),              -- Audit: who last updated this record
    active BOOLEAN DEFAULT TRUE,

    CONSTRAINT uq_person_email UNIQUE (email)
);

CREATE INDEX idx_person_email ON person(email);
CREATE INDEX idx_person_last_name ON person(last_name);
```

### 4.2 UserLogin Table

```sql
CREATE TABLE user_login (
    id BIGSERIAL PRIMARY KEY,
    person_id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'user',  -- Single role (user, admin)
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    last_login TIMESTAMP WITH TIME ZONE,
    failed_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),              -- Audit: who created this record
    updated_by VARCHAR(255),              -- Audit: who last updated this record

    CONSTRAINT uq_user_login_username UNIQUE (username),
    CONSTRAINT uq_user_login_person UNIQUE (person_id),
    CONSTRAINT fk_user_login_person
        FOREIGN KEY (person_id)
        REFERENCES person(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_user_login_username ON user_login(username);
CREATE INDEX idx_user_login_person_id ON user_login(person_id);
```

### 4.3 Login Audit Table (Recommended)

```sql
CREATE TABLE login_audit (
    id BIGSERIAL PRIMARY KEY,
    user_login_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),          -- Supports IPv4 and IPv6
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_login_audit_user_login 
        FOREIGN KEY (user_login_id) 
        REFERENCES user_login(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_login_audit_user_login_id ON login_audit(user_login_id);
CREATE INDEX idx_login_audit_created_at ON login_audit(created_at);
CREATE INDEX idx_login_audit_event_type ON login_audit(event_type);
```

---

## 5. Username and Email Policy

### 5.1 Username Requirements

| Requirement | Specification |
|-------------|---------------|
| Format | Alphanumeric, dots, underscores, hyphens, or valid email |
| Length | 3-255 characters |
| Case | Case-insensitive (stored lowercase) |
| Uniqueness | Must be unique across all accounts |
| Email as Username | Permitted if email is verified |

### 5.2 Email as Username Considerations

#### Advantages

- Users are unlikely to forget their email address
- Already globally unique
- Simplifies registration process

#### Disadvantages

- Email addresses can change (job changes, provider switches)
- Exposes email to potential attackers if username is visible
- Recycled email addresses may cause account conflicts

#### Policy Decision

Users MAY use their email address as their username. The system SHALL:

1. Allow users to choose a different username if preferred
2. Validate email format when used as username
3. Require email verification before account activation
4. Support username changes without affecting email address
5. Never display full username in public contexts

### 5.3 Email Storage Location

Email SHALL be stored in the `Person` entity because:

- Email is Personally Identifiable Information (PII)
- Email serves communication purposes beyond authentication
- Person record should persist even if authentication method changes
- Supports future scenarios: multiple auth methods, federated identity

---

## 6. Security Requirements

### 6.1 Password Requirements (NIST SP 800-63B-4)

| Requirement | Specification |
|-------------|---------------|
| Minimum Length | 8 characters (12+ recommended) |
| Maximum Length | 64 characters minimum acceptance |
| Character Types | All ASCII and Unicode characters permitted |
| Composition Rules | None required (no mandatory special characters) |
| Blocklist Check | Required against known compromised passwords |
| Periodic Rotation | Not required unless compromise suspected |

#### Password Blocklist Sources

- Have I Been Pwned (HIBP) API
- NIST Bad Passwords List
- Application-specific weak passwords

### 6.2 Password Storage

| Component | Specification |
|-----------|---------------|
| Algorithm | BCrypt |
| Salt | Unique per-password, automatically generated (128-bit) |
| Cost Factor | 12 (2^12 = 4,096 iterations) |
| Hash Format | Modular Crypt Format (MCF), e.g., `$2a$12$...` |
| Quarkus Integration | `@Password(PasswordType.MCF)` for automatic verification |

```java
import io.quarkus.elytron.security.common.BcryptUtil;

// BCrypt configuration using Quarkus built-in utility
// Cost factor of 12 provides ~250-300ms hash time on modern hardware
// Increase to 13 or 14 as hardware improves
private static final int BCRYPT_COST = 12;

String hash = BcryptUtil.bcryptHash(password, BCRYPT_COST);
boolean valid = BcryptUtil.matches(password, hash);
```

### 6.3 Account Lockout Policy

| Parameter | Value | Configuration Key |
|-----------|-------|-------------------|
| Max Failed Attempts | 5 | `app.security.max-failed-attempts` |
| Lockout Duration | 15 minutes (progressive) | `app.security.lockout-minutes` |
| Progressive Multiplier | 2x per subsequent lockout | Built-in |
| Maximum Lockout | 24 hours | Built-in |
| Reset on Success | Failed attempts counter resets | Built-in |

**Note**: Configuration keys will be added to `application.properties` when lockout enforcement is integrated:
```properties
# Account lockout configuration (to be added)
app.security.max-failed-attempts=5
app.security.lockout-minutes=15
```

**Implementation Status**: ⏳ Schema and business logic methods exist in `UserLogin` entity (`isLocked()`, `recordFailedAttempt()`, `recordSuccessfulLogin()`). Enforcement during authentication is pending integration with `CaseInsensitiveIdentityProvider`.

### 6.4 Multi-Factor Authentication

Per NIST SP 800-63-4, MFA SHALL be supported with the following priority:

| Priority | Method | AAL Level |
|----------|--------|-----------|
| 1 | FIDO2/WebAuthn (Passkeys) | AAL3 |
| 2 | Hardware Security Keys | AAL3 |
| 3 | TOTP Authenticator Apps | AAL2 |
| 4 | Push Notifications | AAL2 |
| 5 | SMS (discouraged) | AAL1 |

**Note**: Email SHALL NOT be used as a second factor as it does not meet out-of-band requirements.

### 6.5 Session Management

| Parameter | Value | Status |
|-----------|-------|--------|
| Session ID Length | Minimum 128 bits entropy | ✅ Quarkus default |
| Session Timeout (Idle) | 30 minutes | ✅ `quarkus.http.auth.form.timeout=PT30M` |
| Session Timeout (Absolute) | 12 hours | ⏳ Future enhancement |
| Concurrent Sessions | Configurable per deployment | ✅ Quarkus default |
| Session Regeneration | Required after authentication | ✅ Quarkus default |

---

## 7. Quarkus Implementation

### 7.1 Person Entity

```java
package io.archton.htmx.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "person")
@EntityListeners(AuditListener.class)
public class Person extends PanacheEntity {

    // Optional - populated via profile settings, not during registration
    @Size(max = 100)
    @Column(name = "first_name", length = 100)
    public String firstName;

    // Optional - populated via profile settings, not during registration
    @Size(max = 100)
    @Column(name = "last_name", length = 100)
    public String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    public String email;

    @Size(max = 20)
    @Column(length = 20)
    public String phone;

    @Past
    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Size(max = 255)
    @Column(name = "created_by", updatable = false)
    public String createdBy;

    @Size(max = 255)
    @Column(name = "updated_by")
    public String updatedBy;

    @Column(nullable = false)
    public boolean active = true;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL,
              orphanRemoval = true, fetch = FetchType.LAZY)
    public UserLogin userLogin;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (email != null) {
            email = email.toLowerCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        if (email != null) {
            email = email.toLowerCase();
        }
    }

    // Finder methods
    public static Person findByEmail(String email) {
        return find("email", email.toLowerCase()).firstResult();
    }

    /**
     * Get display name (firstName lastName, or email if names not set)
     */
    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email;
    }
}
```

### 7.2 UserLogin Entity

```java
package io.archton.htmx.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.PasswordType;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Entity
@Table(name = "user_login")
@EntityListeners(AuditListener.class)
@UserDefinition
public class UserLogin extends PanacheEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    public Person person;

    @Username
    @NotBlank
    @Size(min = 3, max = 255)
    @Column(nullable = false, unique = true)
    public String username;

    @Password(value = PasswordType.MCF)
    @NotBlank
    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Roles
    @Column(nullable = false)
    public String role = "user";

    // MFA fields (deferred implementation)
    @Column(name = "mfa_enabled", nullable = false)
    public boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    public String mfaSecret;

    // Login tracking
    @Column(name = "last_login")
    public Instant lastLogin;

    // Account lockout
    @Column(name = "failed_attempts", nullable = false)
    public int failedAttempts = 0;

    @Column(name = "locked_until")
    public Instant lockedUntil;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Size(max = 255)
    @Column(name = "created_by", updatable = false)
    public String createdBy;

    @Size(max = 255)
    @Column(name = "updated_by")
    public String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (username != null) {
            username = username.toLowerCase();
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Business methods

    /**
     * Check if the account is currently locked.
     */
    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    /**
     * Record a failed login attempt and apply progressive lockout if needed.
     *
     * @param maxAttempts    maximum allowed attempts before lockout
     * @param lockoutMinutes base lockout duration in minutes
     */
    public void recordFailedAttempt(int maxAttempts, int lockoutMinutes) {
        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            // Progressive lockout: 2^(lockout_count - 1) * base_minutes
            int lockoutCount = failedAttempts / maxAttempts;
            int multiplier = (int) Math.pow(2, lockoutCount - 1);
            long lockoutSeconds = (long) lockoutMinutes * 60 * multiplier;
            // Cap at 24 hours
            lockoutSeconds = Math.min(lockoutSeconds, 24 * 60 * 60);
            lockedUntil = Instant.now().plusSeconds(lockoutSeconds);
        }
    }

    /**
     * Record a successful login, resetting failed attempts and lockout.
     */
    public void recordSuccessfulLogin() {
        failedAttempts = 0;
        lockedUntil = null;
        lastLogin = Instant.now();
    }

    // Finder methods

    public static UserLogin findByUsername(String username) {
        return find("username", username.toLowerCase()).firstResult();
    }

    public static UserLogin findByPersonId(Long personId) {
        return find("person.id", personId).firstResult();
    }

    public static UserLogin findByEmail(String email) {
        return find("person.email", email.toLowerCase()).firstResult();
    }
}
```

### 7.3 Password Service

```java
package io.archton.htmx.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service for password hashing and verification using BCrypt.
 * Uses Quarkus built-in BcryptUtil for seamless integration.
 */
@ApplicationScoped
public class PasswordService {

    // BCrypt cost factor (2^12 = 4096 iterations)
    // Increase this value as hardware improves
    private static final int BCRYPT_COST = 12;

    /**
     * Hash a plain text password using BCrypt.
     *
     * @param plainPassword the plain text password to hash
     * @return the BCrypt hashed password in MCF format
     */
    public String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword, BCRYPT_COST);
    }

    /**
     * Verify a plain text password against a BCrypt hashed password.
     *
     * @param plainPassword  the plain text password to verify
     * @param hashedPassword the stored BCrypt hashed password
     * @return true if the password matches, false otherwise
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}
```

### 7.4 Maven Dependencies

```xml
<!-- Quarkus Security JPA (includes BcryptUtil) -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security-jpa</artifactId>
</dependency>

<!-- TOTP for MFA (when implemented) -->
<dependency>
    <groupId>dev.samstevens.totp</groupId>
    <artifactId>totp</artifactId>
    <version>1.7.1</version>
</dependency>

<!-- Bean Validation -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>
```

**Note**: The `quarkus-security-jpa` extension provides `io.quarkus.elytron.security.common.BcryptUtil` for password hashing. No external BCrypt library (e.g., `org.mindrot:jbcrypt`) is required.

---

## 8. API Considerations

### 8.1 Authentication Endpoints

#### Current Implementation (Form-based)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/signup` | GET | Display registration form |
| `/signup` | POST | Create new account |
| `/login` | GET | Display login form |
| `/j_security_check` | POST | Authenticate user (Quarkus form auth) |
| `/logout` | GET | Terminate session and display logout page |

#### Future REST API (Planned)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/register` | POST | Create new account |
| `/auth/login` | POST | Authenticate user |
| `/auth/logout` | POST | Terminate session |
| `/auth/refresh` | POST | Refresh access token |
| `/auth/password/reset` | POST | Request password reset |
| `/auth/password/change` | PUT | Change password (authenticated) |
| `/auth/mfa/enable` | POST | Enable MFA |
| `/auth/mfa/verify` | POST | Verify MFA code |

### 8.2 Response Security

- Never return password hashes in API responses
- Use consistent error messages to prevent user enumeration
- Return appropriate HTTP status codes

```java
// Bad: Reveals whether username exists
"Invalid username"
"Invalid password"

// Good: Generic message
"Invalid username or password"
```

---

## 9. Audit and Compliance

### 9.1 Required Audit Events

| Event | Data Captured | Status |
|-------|---------------|--------|
| Login Success | User ID, timestamp, IP, user agent | ⏳ Entity ready |
| Login Failure | Username attempted, timestamp, IP, reason | ⏳ Entity ready |
| Password Change | User ID, timestamp, IP | ⏳ Pending |
| MFA Enable/Disable | User ID, timestamp, IP | ⏳ Pending (MFA deferred) |
| Account Lock | User ID, timestamp, reason | ⏳ Entity ready |
| Account Unlock | User ID, timestamp, method | ⏳ Pending |

**Implementation Status**: ⏳ The `LoginAudit` entity exists with factory methods (`loginSuccess()`, `loginFailure()`, `accountLocked()`, `logout()`). Integration with the authentication flow is pending.

### 9.2 Data Retention

| Data Type | Retention Period |
|-----------|------------------|
| Audit Logs | 2 years minimum |
| Failed Login Attempts | 90 days |
| Session Data | Session lifetime + 30 days |

---


## Appendix B: Glossary

| Term | Definition |
|------|------------|
| AAL | Authenticator Assurance Level (NIST) |
| BCrypt | Password hashing algorithm with built-in salt (cost factor based) |
| BcryptUtil | Quarkus built-in utility (`io.quarkus.elytron.security.common.BcryptUtil`) for BCrypt hashing |
| MFA | Multi-Factor Authentication |
| MCF | Modular Crypt Format - standard format for password hashes (e.g., `$2a$12$...`) |
| TOTP | Time-based One-Time Password |
| FIDO2 | Fast Identity Online 2 standard |
| PII | Personally Identifiable Information |
| Salt | Random value unique to each password hash (auto-generated by BCrypt) |

---

## Appendix C: Browser Test Plan (Chrome DevTools MCP)

This test plan is designed for execution via the Chrome DevTools MCP. Each test case includes specific actions and expected outcomes that can be verified through browser automation.

### Prerequisites

- Application running at `http://localhost:9080`
- Fresh database state (or known test data)
- Default admin user: `admin` / `Admin@01`

---

### TC-01: Signup Page - UI Elements

**Objective**: Verify signup page renders correctly with all required elements.

**Steps**:
1. Navigate to `/signup`
2. Take a snapshot of the page

**Expected**:
- [ ] Page title contains "Sign Up"
- [ ] Username input field exists (id: `username`)
- [ ] Email input field exists (id: `email`)
- [ ] Password input field exists (id: `password`)
- [ ] Submit button with text "Sign Up" exists
- [ ] Link to login page exists with text "Login"

---

### TC-02: Signup - Successful Registration

**Objective**: Verify a new user can register successfully.

**Steps**:
1. Navigate to `/signup`
2. Fill form:
   - username: `testuser1`
   - email: `testuser1@example.com`
   - password: `TestPass123`
3. Click "Sign Up" button
4. Wait for navigation

**Expected**:
- [ ] Redirected to `/login` page
- [ ] No error message displayed
- [ ] New user can now login with these credentials

---

### TC-03: Signup - Username Required Validation

**Objective**: Verify username is required for registration.

**Steps**:
1. Navigate to `/signup`
2. Fill form:
   - username: (leave empty)
   - email: `test@example.com`
   - password: `TestPass123`
3. Click "Sign Up" button

**Expected**:
- [ ] Redirected to `/signup?error=username_required`
- [ ] Error message "Username is required." displayed

---

### TC-04: Signup - Email Required Validation

**Objective**: Verify email is required for registration.

**Steps**:
1. Navigate to `/signup`
2. Fill form:
   - username: `testuser`
   - email: (leave empty)
   - password: `TestPass123`
3. Click "Sign Up" button

**Expected**:
- [ ] Redirected to `/signup?error=email_required`
- [ ] Error message "Email is required." displayed

---

### TC-05: Signup - Password Minimum Length Validation

**Objective**: Verify password must be at least 8 characters.

**Steps**:
1. Navigate to `/signup`
2. Fill form:
   - username: `testuser`
   - email: `test@example.com`
   - password: `short` (less than 8 chars)
3. Click "Sign Up" button

**Expected**:
- [ ] Redirected to `/signup?error=password_too_short`
- [ ] Error message "Password must be at least 8 characters." displayed

---

### TC-06: Signup - Duplicate Username Prevention

**Objective**: Verify system prevents duplicate usernames.

**Steps**:
1. Navigate to `/signup`
2. Fill form with existing username:
   - username: `admin` (existing user)
   - email: `newadmin@example.com`
   - password: `TestPass123`
3. Click "Sign Up" button

**Expected**:
- [ ] Redirected to `/signup?error=username_exists`
- [ ] Error message "Username already exists." displayed

---

### TC-07: Signup - Duplicate Email Prevention

**Objective**: Verify system prevents duplicate email addresses.

**Steps**:
1. First create a user with a specific email (or use existing)
2. Navigate to `/signup`
3. Fill form with existing email:
   - username: `newusername`
   - email: (email of existing user)
   - password: `TestPass123`
4. Click "Sign Up" button

**Expected**:
- [ ] Redirected to `/signup?error=email_exists`
- [ ] Error message "Email already registered." displayed

---

### TC-08: Signup - Username Case Insensitivity

**Objective**: Verify usernames are stored and matched case-insensitively.

**Steps**:
1. Navigate to `/signup`
2. Fill form:
   - username: `ADMIN` (uppercase version of existing user)
   - email: `admin2@example.com`
   - password: `TestPass123`
3. Click "Sign Up" button

**Expected**:
- [ ] Redirected to `/signup?error=username_exists`
- [ ] Username matching is case-insensitive

---

### TC-09: Login Page - UI Elements

**Objective**: Verify login page renders correctly with all required elements.

**Steps**:
1. Navigate to `/login`
2. Take a snapshot of the page

**Expected**:
- [ ] Page title contains "Login"
- [ ] Username input field exists (id: `j_username`)
- [ ] Password input field exists (id: `j_password`)
- [ ] Submit button with text "Login" exists
- [ ] Link to signup page exists with text "Sign up"

---

### TC-10: Login - Successful Authentication

**Objective**: Verify valid credentials allow login.

**Steps**:
1. Navigate to `/login`
2. Fill form:
   - j_username: `admin`
   - j_password: `Admin@01`
3. Click "Login" button
4. Wait for navigation

**Expected**:
- [ ] Redirected to home page or dashboard
- [ ] User is authenticated (check for logged-in UI elements)
- [ ] No error message displayed

---

### TC-11: Login - Invalid Password

**Objective**: Verify invalid password is rejected with generic error.

**Steps**:
1. Navigate to `/login`
2. Fill form:
   - j_username: `admin`
   - j_password: `WrongPassword123`
3. Click "Login" button

**Expected**:
- [ ] Redirected to `/login?error=true`
- [ ] Error message "Invalid username or password." displayed
- [ ] Error does NOT reveal whether username exists (security requirement)

---

### TC-12: Login - Invalid Username

**Objective**: Verify invalid username is rejected with generic error.

**Steps**:
1. Navigate to `/login`
2. Fill form:
   - j_username: `nonexistentuser`
   - j_password: `SomePassword123`
3. Click "Login" button

**Expected**:
- [ ] Redirected to `/login?error=true`
- [ ] Error message "Invalid username or password." displayed
- [ ] Error does NOT reveal whether username exists (security requirement)

---

### TC-13: Login - Username Case Insensitivity

**Objective**: Verify login works regardless of username case.

**Steps**:
1. Navigate to `/login`
2. Fill form:
   - j_username: `ADMIN` (uppercase)
   - j_password: `Admin@01`
3. Click "Login" button

**Expected**:
- [ ] Login successful
- [ ] Username matching is case-insensitive

---

### TC-14: Login - Empty Credentials

**Objective**: Verify empty form submission is handled.

**Steps**:
1. Navigate to `/login`
2. Leave form empty
3. Attempt to click "Login" button

**Expected**:
- [ ] Browser validation prevents submission (required fields)
- [ ] OR server returns appropriate error

---

### TC-15: Logout Flow

**Objective**: Verify user can logout successfully.

**Steps**:
1. Login as `admin` / `Admin@01`
2. Navigate to `/logout`
3. Take a snapshot of the page

**Expected**:
- [ ] Logout page is displayed
- [ ] User session is terminated
- [ ] Accessing protected routes redirects to login

---

### TC-16: Protected Route Access - Unauthenticated

**Objective**: Verify protected routes require authentication.

**Steps**:
1. Ensure no active session (logout if needed)
2. Navigate to `/persons`

**Expected**:
- [ ] Redirected to `/login` page
- [ ] Cannot access protected content without authentication

---

### TC-17: Protected Route Access - Authenticated

**Objective**: Verify authenticated users can access protected routes.

**Steps**:
1. Login as `admin` / `Admin@01`
2. Navigate to `/persons`

**Expected**:
- [ ] Persons page loads successfully
- [ ] User sees the persons list

---

### TC-18: Navigation Between Auth Pages

**Objective**: Verify navigation links work correctly.

**Steps**:
1. Navigate to `/login`
2. Click "Sign up" link
3. Verify on signup page
4. Click "Login" link
5. Verify on login page

**Expected**:
- [ ] "Sign up" link navigates from login to signup page
- [ ] "Login" link navigates from signup to login page

---

### TC-19: Security - User Enumeration Prevention

**Objective**: Verify system does not leak user existence information.

**Steps**:
1. Login with existing username, wrong password
2. Note error message
3. Login with non-existing username, any password
4. Note error message
5. Compare messages

**Expected**:
- [ ] Both error messages are identical: "Invalid username or password."
- [ ] No timing difference observable between responses
- [ ] No indication of whether username exists

---

### TC-20: Form Input Sanitization

**Objective**: Verify inputs are properly trimmed and normalized.

**Steps**:
1. Navigate to `/signup`
2. Fill form with whitespace:
   - username: `  testuser2  ` (with spaces)
   - email: `  TEST@EXAMPLE.COM  ` (spaces and uppercase)
   - password: `TestPass123`
3. Click "Sign Up" button
4. Then login with:
   - j_username: `testuser2` (no spaces)
   - j_password: `TestPass123`

**Expected**:
- [ ] Registration succeeds
- [ ] Username stored as lowercase, trimmed
- [ ] Email stored as lowercase, trimmed
- [ ] Login works with normalized values

---

### Test Execution Summary

| Test ID | Test Name | Status | Notes |
|---------|-----------|--------|-------|
| TC-01 | Signup Page UI | [ ] | |
| TC-02 | Successful Registration | [ ] | |
| TC-03 | Username Required | [ ] | |
| TC-04 | Email Required | [ ] | |
| TC-05 | Password Min Length | [ ] | |
| TC-06 | Duplicate Username | [ ] | |
| TC-07 | Duplicate Email | [ ] | |
| TC-08 | Username Case Insensitive | [ ] | |
| TC-09 | Login Page UI | [ ] | |
| TC-10 | Successful Login | [ ] | |
| TC-11 | Invalid Password | [ ] | |
| TC-12 | Invalid Username | [ ] | |
| TC-13 | Login Case Insensitive | [ ] | |
| TC-14 | Empty Credentials | [ ] | |
| TC-15 | Logout Flow | [ ] | |
| TC-16 | Protected Route /persons (Unauth) | [ ] | |
| TC-17 | Protected Route /persons (Auth) | [ ] | |
| TC-18 | Navigation Links | [ ] | |
| TC-19 | User Enumeration Prevention | [ ] | |
| TC-20 | Input Sanitization | [ ] | |

---

### Chrome DevTools MCP Commands Reference

For test execution, use these MCP tool patterns:

```
# Navigate to page
mcp__chrome-devtools__navigate_page(url="/signup")

# Take snapshot to see elements
mcp__chrome-devtools__take_snapshot()

# Fill form fields
mcp__chrome-devtools__fill(uid="<element-uid>", value="testuser")

# Click button
mcp__chrome-devtools__click(uid="<element-uid>")

# Wait for text to appear
mcp__chrome-devtools__wait_for(text="Invalid username or password")

# Take screenshot for visual verification
mcp__chrome-devtools__take_screenshot()
```

---

## Appendix D: TC-16 Fix - Session Invalidation and Route Protection

### Issue Summary

**Test Executed**: December 25, 2025 via Chrome DevTools MCP

**TC-16 Result**: ✅ FIXED (December 27, 2025)

**Observed Behavior**:
1. User navigated to `/logout` and saw "You have been logged out" message
2. User navigated to `/persons` (protected route)
3. **Expected**: Redirect to `/login`
4. **Actual**: Full access to persons page with "Welcome, admin" displayed

### Root Cause Analysis

**Two distinct issues identified**:

#### Issue 1: Logout Does Not Invalidate Session

**Location**: `src/main/java/io/archton/htmx/resource/AuthResource.java:93-98`

```java
// CURRENT (BROKEN)
@GET
@Path("/logout")
@Produces(MediaType.TEXT_HTML)
public TemplateInstance logoutPage() {
    return Templates.logout();  // Just renders template, session remains valid!
}
```

The logout endpoint only renders a "logged out" template without actually invalidating the HTTP session or authentication context.

#### Issue 2: `/persons` Route Not Protected

**Location**: `src/main/resources/application.properties:23-26`

```properties
# CURRENT (INCOMPLETE)
quarkus.http.auth.permission.authenticated.paths=/api/*
quarkus.http.auth.permission.public.paths=/,/login,/signup,/css/*,/images/*,/j_security_check
```

The `/persons` path is not included in `authenticated.paths`, so it falls through without requiring authentication.

---

### Fix Implementation

All fixes have been applied. See current implementation in the codebase.

#### Step 1: Session Invalidation in Logout (✅ Implemented)

Modify `AuthResource.logoutPage()` to properly terminate the session:

```java
@Inject
io.vertx.ext.web.RoutingContext routingContext;

@GET
@Path("/logout")
@Produces(MediaType.TEXT_HTML)
public TemplateInstance logoutPage() {
    // Invalidate the session
    if (routingContext.session() != null) {
        routingContext.session().destroy();
    }
    // Clear the authentication credential cookie
    return Templates.logout();
}
```

**Alternative using SecurityIdentity**:

```java
@Inject
SecurityIdentity securityIdentity;

@Inject
HttpServerRequest request;

@GET
@Path("/logout")
@Produces(MediaType.TEXT_HTML)
public Uni<TemplateInstance> logoutPage() {
    return Uni.createFrom().item(() -> {
        // For form auth, clearing the cookie is essential
        // The cookie name is configured as "quarkus-credential"
        return Templates.logout();
    });
}
```

**Recommended Approach** - Use Quarkus built-in form auth logout:

Add to `application.properties`:
```properties
quarkus.http.auth.form.logout-path=/logout
```

Then redirect the logout page to use the form logout mechanism, or configure a custom logout handler.

#### Step 2: Route Protection for `/persons` (✅ Implemented)

Updated `application.properties` to protect the persons route:

```properties
# FIXED - Include /persons in protected paths
quarkus.http.auth.permission.authenticated.paths=/api/*,/persons
quarkus.http.auth.permission.authenticated.policy=authenticated

# Alternatively, use role-based protection matching PersonResource
quarkus.http.auth.permission.user-routes.paths=/persons
quarkus.http.auth.permission.user-routes.policy=authenticated
quarkus.http.auth.permission.user-routes.methods=GET,POST
```

#### Step 3: Cookie Invalidation for Complete Logout (✅ Implemented)

The authentication cookie is cleared in `AuthResource.logoutPage()` (see `src/main/java/io/archton/htmx/resource/AuthResource.java`):

```java
@GET
@Path("/logout")
@Produces(MediaType.TEXT_HTML)
public Response logoutPage() {
    // Clear the quarkus-credential cookie
    NewCookie clearCookie = new NewCookie.Builder("quarkus-credential")
        .value("")
        .path("/")
        .maxAge(0)
        .build();

    // Destroy session if exists
    if (routingContext.session() != null) {
        routingContext.session().destroy();
    }

    return Response.ok(Templates.logout())
        .cookie(clearCookie)
        .build();
}
```

---

### Verification Test Plan

After implementing fixes, re-run TC-16:

1. Login as `admin` / `Admin@01`
2. Verify access to `/persons`
3. Navigate to `/logout`
4. Attempt to access `/persons`
5. **Expected**: Redirect to `/login` page
6. Verify no session cookie remains (check DevTools > Application > Cookies)

### Additional Security Recommendations

1. **Add CSRF protection** for logout to prevent logout attacks
2. **Set Secure and HttpOnly flags** on session cookies in production
3. **Consider POST-based logout** instead of GET for better security
4. **Add session timeout** handling with appropriate redirect

---

### Implementation Status

| Task | Priority | Effort | Status |
|------|----------|--------|--------|
| Add `/persons` to protected paths | HIGH | Low | ✅ Done |
| Implement session invalidation in logout | HIGH | Medium | ✅ Done |
| Add cookie clearing on logout | MEDIUM | Low | ✅ Done |
| Add CSRF protection for logout | LOW | Medium | ⏳ Future |
