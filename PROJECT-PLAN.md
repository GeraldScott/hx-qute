# HX Qute - Project Implementation Plan

**Product:** HX Qute Reference Application
**Current Phase:** Feature 001 - Identity and Access Management
**Last Updated:** 2025-12-29

---

## Current Status

**Current Phase:** Feature 001 - Identity and Access Management ✅ COMPLETE
**Next Use Case:** Feature 002 - Master Data Management (UC-002-01-01)
**Blockers:** None

---

## Progress Summary

| Feature | Use Cases | Completed | Remaining |
|---------|-----------|-----------|-----------|
| Feature 000 - Foundation | 4 | 4 | 0 |
| Feature 001 - Identity and Access Management | 6 | 6 | 0 |
| **Total** | **10** | **10** | **0** |

---

## Feature 000: Foundation

This phase establishes the database schema and entity classes required for authentication.

### Task 000-01: Create UserLogin Database Table

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

### Task 000-02: Create UserLogin Entity

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

### Task 000-03: Create PasswordValidator Service

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

### Task 000-04: Seed Admin User

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

## Feature 001: Identity and Access Management

This feature implements user authentication and account management.

**Reference Documents:**
- [USER-STORIES.md](docs/USER-STORIES.md) - US-001-01, US-001-02, US-001-03
- [use-cases.md](specs/US-001-identity-and-access-management/use-cases.md) - UC-001-01-01 to UC-001-03-01
- [test-cases.md](specs/US-001-identity-and-access-management/test-cases.md) - TC-001-01-001 to TC-001-03-001

---

### UC-001-01-01: Display Signup Page

**Status:** ✅ Complete
**Parent Story:** US-001-01 - User Registration

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
- Test ID: TC-001-01-001
- Status: ✅ Passed
- Notes: All 5 expected elements verified on 2025-12-28. Page title "Sign Up", email input (id=email), password input (id=password), "Sign Up" button, and link to login page all present and correctly configured.

---

### UC-001-01-02: Register New User

**Status:** ✅ Complete
**Parent Story:** US-001-01 - User Registration

**Description:** Process signup form submission with validation and user creation.

**Implementation Tasks:**
- [x] Implement `POST /signup` endpoint in AuthResource
- [x] Inject `PasswordValidator` service
- [x] Validate email required
- [x] Validate password using PasswordValidator
- [x] Check for duplicate email (case-insensitive)
- [x] Create UserLogin using factory method
- [x] Redirect to `/login` on success
- [x] Redirect with error codes on failure

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
- Test ID: TC-001-01-002, TC-001-01-003, TC-001-01-004, TC-001-01-005, TC-001-01-006, TC-001-01-007, TC-001-01-008
- Status: ✅ Passed
- Notes: All tests passed on 2025-12-28. Successful registration with redirect to /login. Email required validation works (both HTML5 and server-side). Password minimum 15 chars enforced. Duplicate email prevention working. Case-insensitive email matching verified. Form input sanitization and client-side normalization confirmed.

---

### UC-001-02-01: Display Login Page

**Status:** ✅ Complete
**Parent Story:** US-001-02 - User Login

**Description:** Display login form with email and password fields.

**Implementation Tasks:**
- [x] Add `login()` template method to AuthResource.Templates
- [x] Implement `GET /login` endpoint
- [x] Handle `?error=true` query parameter for error display
- [x] Create `templates/AuthResource/login.html`
- [x] Form action must be `/j_security_check` (Quarkus form auth)
- [x] Include email input (id: `j_username`, name: `j_username`)
- [x] Include password input (id: `j_password`, name: `j_password`)
- [x] Include "Login" submit button
- [x] Include link to signup page
- [x] Display error message when `?error=true`

**Critical:** Form must POST to `/j_security_check` with `j_username` and `j_password` fields.

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/login` | Display login form |

**Test Results:**
- Test ID: TC-001-02-001
- Status: ✅ Passed
- Notes: All 5 UI elements verified on 2025-12-28. Page title "Login", email input (id=j_username), password input (id=j_password), "Login" button, and link to signup page all present. Form correctly posts to /j_security_check.

---

### UC-001-02-02: Authenticate User

**Status:** ✅ Complete
**Parent Story:** US-001-02 - User Login

**Description:** Configure Quarkus form authentication to handle login submission.

**Implementation Tasks:**
- [x] Add `quarkus-security-jpa` dependency to pom.xml (if not present)
- [x] Configure form authentication in application.properties:
  - `quarkus.http.auth.form.enabled=true`
  - `quarkus.http.auth.form.login-page=/login`
  - `quarkus.http.auth.form.landing-page=/`
  - `quarkus.http.auth.form.error-page=/login?error=true`
  - `quarkus.http.auth.form.timeout=PT30M`
  - `quarkus.http.auth.form.cookie-name=quarkus-credential`
  - `quarkus.http.auth.form.http-only-cookie=true`
- [x] Configure session security:
  - `quarkus.http.auth.form.new-cookie-interval=PT1M`
  - `quarkus.http.same-site-cookie.quarkus-credential=strict`

**Authentication Flow:**
1. User submits form to `/j_security_check`
2. Quarkus looks up UserLogin by email (`@Username`)
3. Quarkus verifies password against BCrypt hash (`@Password`)
4. On success: redirect to landing page with session cookie
5. On failure: redirect to error page

**Test Results:**
- Test ID: TC-001-02-002, TC-001-02-003, TC-001-02-004, TC-001-02-005, TC-001-02-006, TC-001-02-007, TC-001-02-009
- Status: ✅ Passed
- Notes: All tests passed on 2025-12-28. Successful login with admin credentials. Invalid credentials show generic error. Case-insensitive email via client-side JS normalization. HTML5 validation prevents empty submission. User enumeration prevention verified (identical error messages). Protected route access after authentication confirmed.

---

### UC-001-02-03: Access Protected Route (Unauthenticated)

**Status:** ✅ Complete
**Parent Story:** US-001-02 - User Login

**Description:** Configure route protection to redirect unauthenticated users to login.

**Implementation Tasks:**
- [x] Configure route protection in application.properties:
  - `quarkus.http.auth.permission.authenticated.paths=/dashboard/*,/api/*,/persons/*,/profile/*`
  - `quarkus.http.auth.permission.authenticated.policy=authenticated`
  - `quarkus.http.auth.permission.admin.paths=/admin/*,/genders/*`
  - `quarkus.http.auth.permission.admin.policy=admin`
  - `quarkus.http.auth.policy.admin.roles-allowed=admin`
  - `quarkus.http.auth.permission.public.paths=/,/login,/signup,/logout,/css/*,/js/*,/images/*,/webjars/*,/img/*,/style.css`
  - `quarkus.http.auth.permission.public.policy=permit`
- [x] Update base template navigation to show Login/Logout based on authentication state
- [x] Inject `SecurityIdentity` in resources that need user info

**Route Protection Summary:**
| Path Pattern | Policy | Roles |
|--------------|--------|-------|
| `/`, `/login`, `/signup`, `/logout` | Public | - |
| `/persons/*`, `/dashboard/*` | Authenticated | Any |
| `/genders/*`, `/admin/*` | Admin | admin |

**Test Results:**
- Test ID: TC-001-02-008, TC-001-02-010
- Status: ✅ Passed
- Notes: All tests passed on 2025-12-28. Unauthenticated access to /persons and /genders redirects to /login. Navigation links work correctly between login and signup pages.

---

### UC-001-03-01: Logout User

**Status:** ✅ Complete
**Parent Story:** US-001-03 - User Logout

**Description:** Implement logout functionality with session termination.

**Implementation Tasks:**
- [x] Add `logout()` template method to AuthResource.Templates
- [x] Implement `GET /logout` endpoint
- [x] Inject `RoutingContext` for session access
- [x] Destroy user session
- [x] Clear authentication cookie (`quarkus-credential`)
- [x] Create `templates/AuthResource/logout.html`
- [x] Display logout confirmation message
- [x] Include link to home page and login page

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/logout` | Logout and show confirmation |

**Test Results:**
- Test ID: TC-001-03-001
- Status: ✅ Passed
- Notes: All assertions passed on 2025-12-28. Logout page displays "Logged Out" title, confirmation message "You have been successfully logged out.", "GO TO HOME" link to /, and "LOGIN AGAIN" link to /login.

---

## Test Cases Reference

### Feature 001 Test Cases

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-001-01-001 | Signup Page UI Elements | UC-001-01-01 | ✅ |
| TC-001-01-002 | Signup Successful Registration | UC-001-01-02 | ✅ |
| TC-001-01-003 | Signup Email Required | UC-001-01-02 | ✅ |
| TC-001-01-004 | Signup Password Minimum Length | UC-001-01-02 | ✅ |
| TC-001-01-005 | Signup Duplicate Email Prevention | UC-001-01-02 | ✅ |
| TC-001-01-006 | Signup Email Case Insensitivity | UC-001-01-02 | ✅ |
| TC-001-01-007 | Form Input Sanitization | UC-001-01-02 | ✅ |
| TC-001-01-008 | Signup Email Client-Side Normalization | UC-001-01-02 | ✅ |
| TC-001-02-001 | Login Page UI Elements | UC-001-02-01 | ✅ |
| TC-001-02-002 | Login Successful Authentication | UC-001-02-02 | ✅ |
| TC-001-02-003 | Login Invalid Password | UC-001-02-02 | ✅ |
| TC-001-02-004 | Login Invalid Email | UC-001-02-02 | ✅ |
| TC-001-02-005 | Login Email Case Insensitivity | UC-001-02-02 | ✅ |
| TC-001-02-006 | Login Empty Credentials | UC-001-02-02 | ✅ |
| TC-001-02-007 | User Enumeration Prevention | UC-001-02-02 | ✅ |
| TC-001-02-008 | Protected Route Unauthenticated | UC-001-02-03 | ✅ |
| TC-001-02-009 | Protected Route Authenticated | UC-001-02-02 | ✅ |
| TC-001-02-010 | Navigation Between Auth Pages | UC-001-02-01 | ✅ |
| TC-001-03-001 | Logout Flow | UC-001-03-01 | ✅ |

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

<!-- Required for Feature 001 -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security-jpa</artifactId>
</dependency>
```

---

## Configuration Reference

### application.properties additions for Feature 001

```properties
# =============================================================================
# Feature 001: Form-Based Authentication Configuration
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
| 2025-12-28 | 1.0 | Initial plan created for Feature 000 (Foundation) and Feature 001 (Identity and Access Management) |
| 2025-12-29 | 1.1 | Renumbered use cases and test cases to follow UC-FFF-SS-NN and TC-FFF-SS-NNN conventions |
