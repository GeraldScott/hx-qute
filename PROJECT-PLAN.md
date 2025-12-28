# HX Qute - Project Implementation Plan

**Product:** HX Qute Reference Application
**Current Phase:** Phase 1 - Authentication
**Last Updated:** 2025-12-28

---

## Current Status

**Current Phase:** Phase 1 - Authentication
**Next Use Case:** UC-1.2 - Register New User
**Blockers:** None

---

## Progress Summary

| Phase | Use Cases | Completed | Remaining |
|-------|-----------|-----------|-----------|
| Phase 0 - Foundation | 4 | 4 | 0 |
| Phase 1 - Authentication | 6 | 1 | 5 |
| **Total** | **10** | **5** | **5** |

---

## Phase 0: Foundation

This phase establishes the database schema and entity classes required for authentication.

### UC-0.1: Create UserLogin Database Table

**Status:** ✅ Complete

**Description:** Create Flyway migration for the `user_login` table with all required columns per LOGIN-PHASED.md specification.

**Implementation Tasks:**
- [x] Create migration file `V1.2.0__Create_user_login_table.sql`
- [x] Define columns: id, email, password, role, first_name, last_name, created_at, updated_at, active
- [x] Add unique constraint on email
- [x] Add index on email column

**Technical Specification:**
```sql
CREATE TABLE user_login (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'user',
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT uq_user_login_email UNIQUE (email)
);
CREATE INDEX idx_user_login_email ON user_login(email);
```

**Test Results:**
- Test ID: N/A (database migration)
- Status: ✅ Verified
- Notes: Migration applied successfully on 2025-12-28. Flyway version v1.2.0 confirmed.

---

### UC-0.2: Create UserLogin Entity

**Status:** ✅ Complete

**Description:** Implement the `UserLogin` entity with Quarkus Security JPA annotations for form-based authentication.

**Implementation Tasks:**
- [x] Create `entity/UserLogin.java`
- [x] Add `@UserDefinition` annotation
- [x] Add `@Username` on email field
- [x] Add `@Password(PasswordType.MCF)` on password field
- [x] Add `@Roles` on role field
- [x] Implement `@PrePersist` and `@PreUpdate` hooks for timestamps and email normalization
- [x] Add `create()` factory method with BCrypt hashing (cost factor 12)
- [x] Add `findByEmail()` finder method
- [x] Add `emailExists()` helper method
- [x] Add `getDisplayName()` display method

**Technical Reference:** See SYSTEM-SPECIFICATION.md Section 3.4

**Test Results:**
- Test ID: N/A (unit test)
- Status: ✅ Verified
- Notes: Entity compiles and server starts without errors. Added quarkus-security-jpa and quarkus-hibernate-validator dependencies. Used PanacheEntityBase with IDENTITY generation to match BIGSERIAL migration.

---

### UC-0.3: Create PasswordValidator Service

**Status:** ✅ Complete

**Description:** Implement NIST SP 800-63B-4 compliant password validation service.

**Implementation Tasks:**
- [x] Create `service/PasswordValidator.java`
- [x] Inject configurable min/max length from application.properties
- [x] Implement `validate()` method returning list of errors
- [x] Implement `isValid()` convenience method
- [x] Add properties: `app.security.password.min-length=15`, `app.security.password.max-length=128`

**Password Policy (NIST SP 800-63B-4):**
- Minimum 15 characters (password-only authentication)
- Maximum 128 characters
- No composition rules (no special chars required)
- No truncation

**Technical Reference:** See LOGIN-PHASED.md Section 2.5

**Test Results:**
- Test ID: N/A (unit test)
- Status: ✅ Verified
- Notes: Service compiles and server starts without errors. Uses ConfigProperty with defaultValues (min=15, max=128) per NIST SP 800-63B-4.

---

### UC-0.4: Seed Admin User

**Status:** ✅ Complete

**Description:** Create Flyway migration to insert default admin user for testing.

**Implementation Tasks:**
- [x] Create migration file `V1.2.1__Insert_admin_user.sql`
- [x] Insert admin user with BCrypt hashed password
- [x] Email: `admin@example.com`
- [x] Password: `AdminPassword123` (BCrypt hashed)
- [x] Role: `admin`

**Test Results:**
- Test ID: N/A (database seed)
- Status: ✅ Verified
- Notes: Migration applied successfully on 2025-12-28. Flyway version v1.2.1 confirmed. Admin user seeded with BCrypt cost 12 hash.

---

## Phase 1: Authentication

This phase implements Epic 1: User Authentication & Account Management.

**Reference Documents:**
- [USER-STORIES.md](specs/USER-STORIES.md) - US-1.1, US-1.2, US-1.3
- [USE-CASES.md](specs/USE-CASES.md) - UC-1.1 to UC-1.6
- [LOGIN-PHASED.md](specs/LOGIN-PHASED.md) - Phase 1 specification
- [SYSTEM-SPECIFICATION.md](specs/SYSTEM-SPECIFICATION.md) - Sections 4.4, 5.6, 5.7

---

### UC-1.1: Display Signup Page

**Status:** ✅ Complete
**Parent Story:** US-1.1 - User Registration

**Description:** Display signup form with email and password fields.

**Implementation Tasks:**
- [x] Create `router/AuthResource.java` with `@Path("/")`
- [x] Add `@CheckedTemplate` class with `signup()` method
- [x] Implement `GET /signup` endpoint returning signup template
- [x] Create `templates/AuthResource/signup.html`
- [x] Include email input (id: `email`)
- [x] Include password input (id: `password`)
- [x] Include "Sign Up" submit button
- [x] Include link to login page

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/signup` | Display signup form |

**Test Results:**
- Test ID: TC-1.01
- Status: ✅ Passed
- Notes: All 5 expected elements verified on 2025-12-28. Page title "Sign Up", email input (id=email), password input (id=password), "Sign Up" button, and link to login page all present and correctly configured.

---

### UC-1.2: Register New User

**Status:** 🔲 Not Started
**Parent Story:** US-1.1 - User Registration

**Description:** Process signup form submission with validation and user creation.

**Implementation Tasks:**
- [ ] Implement `POST /signup` endpoint in AuthResource
- [ ] Inject `PasswordValidator` service
- [ ] Validate email required
- [ ] Validate password using PasswordValidator
- [ ] Check for duplicate email (case-insensitive)
- [ ] Create UserLogin using factory method
- [ ] Redirect to `/login` on success
- [ ] Redirect with error codes on failure

**Validation Rules:**
| Field | Rule | Error Message |
|-------|------|---------------|
| email | Required | "Email is required." |
| email | Valid format | "Invalid email format." |
| email | Unique (case-insensitive) | "Email already registered." |
| password | Required | "Password is required." |
| password | Min 15 chars | "Password must be at least 15 characters." |
| password | Max 128 chars | "Password must be 128 characters or less." |

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/signup` | Process registration |

**Test Results:**
- Test ID: TC-1.02, TC-1.03, TC-1.04, TC-1.05, TC-1.06, TC-1.18
- Status: 🔲 Not Tested
- Notes:

---

### UC-1.3: Display Login Page

**Status:** 🔲 Not Started
**Parent Story:** US-1.2 - User Login

**Description:** Display login form with email and password fields.

**Implementation Tasks:**
- [ ] Add `login()` template method to AuthResource.Templates
- [ ] Implement `GET /login` endpoint
- [ ] Handle `?error=true` query parameter for error display
- [ ] Create `templates/AuthResource/login.html`
- [ ] Form action must be `/j_security_check` (Quarkus form auth)
- [ ] Include email input (id: `j_username`, name: `j_username`)
- [ ] Include password input (id: `j_password`, name: `j_password`)
- [ ] Include "Login" submit button
- [ ] Include link to signup page
- [ ] Display error message when `?error=true`

**Critical:** Form must POST to `/j_security_check` with `j_username` and `j_password` fields.

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/login` | Display login form |

**Test Results:**
- Test ID: TC-1.07
- Status: 🔲 Not Tested
- Notes:

---

### UC-1.4: Authenticate User

**Status:** 🔲 Not Started
**Parent Story:** US-1.2 - User Login

**Description:** Configure Quarkus form authentication to handle login submission.

**Implementation Tasks:**
- [ ] Add `quarkus-security-jpa` dependency to pom.xml (if not present)
- [ ] Configure form authentication in application.properties:
  - `quarkus.http.auth.form.enabled=true`
  - `quarkus.http.auth.form.login-page=/login`
  - `quarkus.http.auth.form.landing-page=/`
  - `quarkus.http.auth.form.error-page=/login?error=true`
  - `quarkus.http.auth.form.timeout=PT30M`
  - `quarkus.http.auth.form.cookie-name=quarkus-credential`
  - `quarkus.http.auth.form.http-only-cookie=true`
- [ ] Configure session security:
  - `quarkus.http.auth.form.new-cookie-interval=PT1M`
  - `quarkus.http.same-site-cookie.quarkus-credential=strict`

**Authentication Flow:**
1. User submits form to `/j_security_check`
2. Quarkus looks up UserLogin by email (`@Username`)
3. Quarkus verifies password against BCrypt hash (`@Password`)
4. On success: redirect to landing page with session cookie
5. On failure: redirect to error page

**Test Results:**
- Test ID: TC-1.08, TC-1.09, TC-1.10, TC-1.11, TC-1.12, TC-1.15, TC-1.17
- Status: 🔲 Not Tested
- Notes:

---

### UC-1.5: Logout User

**Status:** 🔲 Not Started
**Parent Story:** US-1.3 - User Logout

**Description:** Implement logout functionality with session termination.

**Implementation Tasks:**
- [ ] Add `logout()` template method to AuthResource.Templates
- [ ] Implement `GET /logout` endpoint
- [ ] Inject `RoutingContext` for session access
- [ ] Destroy user session
- [ ] Clear authentication cookie (`quarkus-credential`)
- [ ] Create `templates/AuthResource/logout.html`
- [ ] Display logout confirmation message
- [ ] Include link to home page and login page

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/logout` | Logout and show confirmation |

**Test Results:**
- Test ID: TC-1.13
- Status: 🔲 Not Tested
- Notes:

---

### UC-1.6: Access Protected Route (Unauthenticated)

**Status:** 🔲 Not Started
**Parent Story:** US-1.2 - User Login

**Description:** Configure route protection to redirect unauthenticated users to login.

**Implementation Tasks:**
- [ ] Configure route protection in application.properties:
  - `quarkus.http.auth.permission.authenticated.paths=/dashboard/*,/api/*,/persons/*,/profile/*`
  - `quarkus.http.auth.permission.authenticated.policy=authenticated`
  - `quarkus.http.auth.permission.admin.paths=/admin/*,/genders/*`
  - `quarkus.http.auth.permission.admin.policy=admin`
  - `quarkus.http.auth.policy.admin.roles-allowed=admin`
  - `quarkus.http.auth.permission.public.paths=/,/login,/signup,/logout,/css/*,/js/*,/images/*,/webjars/*`
  - `quarkus.http.auth.permission.public.policy=permit`
- [ ] Update base template navigation to show Login/Logout based on authentication state
- [ ] Inject `SecurityIdentity` in resources that need user info

**Route Protection Summary:**
| Path Pattern | Policy | Roles |
|--------------|--------|-------|
| `/`, `/login`, `/signup`, `/logout` | Public | - |
| `/persons/*`, `/dashboard/*` | Authenticated | Any |
| `/genders/*`, `/admin/*` | Admin | admin |

**Test Results:**
- Test ID: TC-1.14, TC-1.16
- Status: 🔲 Not Tested
- Notes:

---

## Test Cases Reference

### Phase 1 Test Cases

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-1.01 | Signup Page UI Elements | UC-1.1 | 🔲 |
| TC-1.02 | Signup Successful Registration | UC-1.2 | 🔲 |
| TC-1.03 | Signup Email Required | UC-1.2 | 🔲 |
| TC-1.04 | Signup Password Minimum Length | UC-1.2 | 🔲 |
| TC-1.05 | Signup Duplicate Email Prevention | UC-1.2 | 🔲 |
| TC-1.06 | Signup Email Case Insensitivity | UC-1.2 | 🔲 |
| TC-1.07 | Login Page UI Elements | UC-1.3 | 🔲 |
| TC-1.08 | Login Successful Authentication | UC-1.4 | 🔲 |
| TC-1.09 | Login Invalid Password | UC-1.4 | 🔲 |
| TC-1.10 | Login Invalid Email | UC-1.4 | 🔲 |
| TC-1.11 | Login Email Case Insensitivity | UC-1.4 | 🔲 |
| TC-1.12 | Login Empty Credentials | UC-1.4 | 🔲 |
| TC-1.13 | Logout Flow | UC-1.5 | 🔲 |
| TC-1.14 | Protected Route Unauthenticated | UC-1.6 | 🔲 |
| TC-1.15 | Protected Route Authenticated | UC-1.4 | 🔲 |
| TC-1.16 | Navigation Between Auth Pages | UC-1.1, UC-1.3 | 🔲 |
| TC-1.17 | User Enumeration Prevention | UC-1.4 | 🔲 |
| TC-1.18 | Form Input Sanitization | UC-1.2 | 🔲 |

---

## Dependencies

### Maven Dependencies (pom.xml)

```xml
<!-- Already included in project -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-orm-panache</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>

<!-- Required for Phase 1 -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security-jpa</artifactId>
</dependency>
```

---

## Configuration Reference

### application.properties additions for Phase 1

```properties
# =============================================================================
# Phase 1: Form-Based Authentication Configuration
# =============================================================================

# --- Form Authentication ---
quarkus.http.auth.form.enabled=true
quarkus.http.auth.form.login-page=/login
quarkus.http.auth.form.landing-page=/
quarkus.http.auth.form.error-page=/login?error=true
quarkus.http.auth.form.timeout=PT30M
quarkus.http.auth.form.cookie-name=quarkus-credential
quarkus.http.auth.form.http-only-cookie=true

# --- Route Protection ---
quarkus.http.auth.permission.authenticated.paths=/dashboard/*,/api/*,/persons/*,/profile/*
quarkus.http.auth.permission.authenticated.policy=authenticated

quarkus.http.auth.permission.admin.paths=/admin/*,/genders/*
quarkus.http.auth.permission.admin.policy=admin
quarkus.http.auth.policy.admin.roles-allowed=admin

quarkus.http.auth.permission.public.paths=/,/login,/signup,/logout,/css/*,/js/*,/images/*,/webjars/*,/img/*
quarkus.http.auth.permission.public.policy=permit

# --- Password Policy (NIST SP 800-63B-4) ---
app.security.password.min-length=15
app.security.password.max-length=128

# --- Session Security ---
quarkus.http.auth.form.new-cookie-interval=PT1M
quarkus.http.same-site-cookie.quarkus-credential=strict
```

---

## Revision History

| Date | Version | Changes |
|------|---------|---------|
| 2025-12-28 | 1.0 | Initial plan created for Phase 0 (Foundation) and Phase 1 (Authentication) |
