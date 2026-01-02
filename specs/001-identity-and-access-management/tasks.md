# Implementation Plan for Feature 001: Identity and Access Management

This phase implements user registration, modal-based login, and logout functionality.

## UC-001-01-01: Display Signup Page

**Status:** ✅ Complete
**Parent Story:** US-001-01 - User Registration

**Description:** Display signup form with email and password fields.

**Implementation Tasks:**
- [x] Create `router/AuthResource.java` with `@Path("/")`
- [x] Add `@CheckedTemplate` class with `signup()` template method
- [x] Implement `GET /signup` endpoint returning signup template
- [x] Create `templates/AuthResource/signup.html`
- [x] Include email input (id: `email`)
- [x] Include password input (id: `password`)
- [x] Include "Sign Up" submit button
- [x] Include link to open login modal

**Files Created:**
- `src/main/java/io/archton/scaffold/router/AuthResource.java`
- `src/main/resources/templates/AuthResource/signup.html`

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/signup` | Display signup form |

**Test Results:**
- Test ID: TC-001-01-001
- Status: ✅ Passed

---

## UC-001-01-02: Register New User

**Status:** ✅ Complete
**Parent Story:** US-001-01 - User Registration

**Description:** Process signup form submission with validation and user creation.

**Implementation Tasks:**
- [x] Implement `POST /signup` endpoint in AuthResource
- [x] Inject `PasswordValidator` service
- [x] Inject `UserLoginService` service
- [x] Validate email required
- [x] Validate email format using regex
- [x] Validate password using PasswordValidator (15-128 chars)
- [x] Check for duplicate email via `UserLoginService.emailExists()`
- [x] Create user via `UserLoginService.create()`
- [x] Redirect to `/?login=true` on success (opens login modal)
- [x] Redirect with error codes on failure
- [x] Add client-side email normalization via `onsubmit` handler

**Validation Rules:**
| Field | Rule | Error Code | Error Message |
|-------|------|------------|---------------|
| email | Required | `email_required` | "Email is required." |
| email | Valid format | `email_invalid` | "Invalid email format." |
| email | Unique | `email_exists` | "Email already registered." |
| password | Required | `password_required` | "Password is required." |
| password | Min 15 chars | `password_short` | "Password must be at least 15 characters." |
| password | Max 128 chars | `password_long` | "Password must be 128 characters or less." |

**Test Results:**
- Test ID: TC-001-01-002 through TC-001-01-007
- Status: ✅ Passed

---

## UC-001-02-01: Login Modal

**Status:** ✅ Complete
**Parent Story:** US-001-02 - User Login

**Description:** Implement login modal in base template (not a separate page).

**Implementation Tasks:**
- [x] Add login modal (`#login-modal`) to `templates/base.html`
- [x] Form action: `/j_security_check` (Quarkus form auth)
- [x] Include email input (id: `j_username`, name: `j_username`)
- [x] Include password input (id: `j_password`, name: `j_password`)
- [x] Include "Login" submit button
- [x] Include link to signup page
- [x] Add JavaScript to open modal on `?login=true` query param
- [x] Add JavaScript to show error on `?error=true` query param
- [x] Add client-side email normalization via `onsubmit` handler
- [x] Update navigation to show "Login" link that opens modal

**Note:** There is NO `/login` endpoint. Login is handled via modal + Quarkus form authentication.

**Files Modified:**
- `src/main/resources/templates/base.html` (added login modal)
- `src/main/resources/templates/fragments/navigation.html` (login link opens modal)

**Test Results:**
- Test ID: TC-001-02-001
- Status: ✅ Passed

---

## UC-001-02-02: Authenticate User

**Status:** ✅ Complete
**Parent Story:** US-001-02 - User Login

**Description:** Configure Quarkus form authentication to handle login submission.

**Implementation Tasks:**
- [x] Configure form authentication in application.properties:
  - `quarkus.http.auth.form.enabled=true`
  - `quarkus.http.auth.form.login-page=/?login=true`
  - `quarkus.http.auth.form.landing-page=/`
  - `quarkus.http.auth.form.error-page=/?login=true&error=true`
  - `quarkus.http.auth.form.timeout=PT30M`
  - `quarkus.http.auth.form.cookie-name=quarkus-credential`
  - `quarkus.http.auth.form.http-only-cookie=true`
- [x] Configure session security:
  - `quarkus.http.auth.form.new-cookie-interval=PT1M`

**Authentication Flow:**
1. User opens login modal and submits form to `/j_security_check`
2. Quarkus looks up UserLogin by email (`@Username` field)
3. Quarkus verifies password against BCrypt hash (`@Password` field)
4. On success: redirect to `/` with session cookie
5. On failure: redirect to `/?login=true&error=true`

**Test Results:**
- Test ID: TC-001-02-002 through TC-001-02-007, TC-001-02-009
- Status: ✅ Passed

---

## UC-001-02-03: Route Protection

**Status:** ✅ Complete
**Parent Story:** US-001-02 - User Login

**Description:** Configure route protection to redirect unauthenticated users.

**Implementation Tasks:**
- [x] Configure route protection in application.properties:
  - `quarkus.http.auth.permission.authenticated.paths=/dashboard/*,/api/*,/persons,/persons/*,/profile/*,/graph,/graph/*`
  - `quarkus.http.auth.permission.authenticated.policy=authenticated`
  - `quarkus.http.auth.permission.admin.paths=/admin/*,/genders/*,/titles/*,/relationships/*`
  - `quarkus.http.auth.permission.admin.policy=admin`
  - `quarkus.http.auth.policy.admin.roles-allowed=admin`
  - `quarkus.http.auth.permission.public.paths=/,/login,/signup,/logout,...`
  - `quarkus.http.auth.permission.public.policy=permit`
- [x] Update navigation to show Login/Logout based on authentication state

**Route Protection Summary:**
| Path Pattern | Policy | Roles |
|--------------|--------|-------|
| `/dashboard/*`, `/api/*` | Authenticated | Any |
| `/persons`, `/persons/*` | Authenticated | Any |
| `/profile/*` | Authenticated | Any |
| `/graph`, `/graph/*` | Authenticated | Any |
| `/admin/*` | Admin | admin |
| `/genders/*`, `/titles/*`, `/relationships/*` | Admin | admin |
| `/`, `/signup`, `/logout`, static resources | Public | - |

**Test Results:**
- Test ID: TC-001-02-008, TC-001-02-010
- Status: ✅ Passed

---

## UC-001-03-01: Logout User

**Status:** ✅ Complete
**Parent Story:** US-001-03 - User Logout

**Description:** Implement logout functionality with session termination.

**Implementation Tasks:**
- [x] Add `logout()` template method to AuthResource.Templates
- [x] Implement `GET /logout` endpoint
- [x] Inject `RoutingContext` for session access
- [x] Destroy user session via `routingContext.session().destroy()`
- [x] Clear authentication cookie (`quarkus-credential`) via Response cookie
- [x] Create `templates/AuthResource/logout.html`
- [x] Display success message: "You have been successfully logged out."
- [x] Include "Go to Home" link to `/`
- [x] Include "Login Again" link that opens login modal

**Files Created:**
- `src/main/resources/templates/AuthResource/logout.html`

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/logout` | Logout and show confirmation |

**Test Results:**
- Test ID: TC-001-03-001
- Status: ✅ Passed

---

## Implementation Summary

### Files Created/Modified

| File | Purpose |
|------|---------|
| `src/main/java/io/archton/scaffold/router/AuthResource.java` | Signup and logout endpoints |
| `src/main/resources/templates/AuthResource/signup.html` | Signup page |
| `src/main/resources/templates/AuthResource/logout.html` | Logout confirmation page |
| `src/main/resources/templates/base.html` | Contains login modal |
| `src/main/resources/templates/fragments/navigation.html` | Login/logout navigation |
| `src/main/resources/application.properties` | Form auth configuration |

### Test Cases Reference

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-001-01-001 | Signup Page UI Elements | UC-001-01-01 | ✅ |
| TC-001-01-002 | Signup Successful Registration | UC-001-01-02 | ✅ |
| TC-001-01-003 | Signup Email Required | UC-001-01-02 | ✅ |
| TC-001-01-004 | Signup Password Minimum Length | UC-001-01-02 | ✅ |
| TC-001-01-005 | Signup Duplicate Email Prevention | UC-001-01-02 | ✅ |
| TC-001-01-006 | Signup Email Case Insensitivity | UC-001-01-02 | ✅ |
| TC-001-01-007 | Form Input Sanitization | UC-001-01-02 | ✅ |
| TC-001-02-001 | Login Modal UI Elements | UC-001-02-01 | ✅ |
| TC-001-02-002 | Login Successful Authentication | UC-001-02-02 | ✅ |
| TC-001-02-003 | Login Invalid Password | UC-001-02-02 | ✅ |
| TC-001-02-004 | Login Invalid Email | UC-001-02-02 | ✅ |
| TC-001-02-005 | Login Email Case Insensitivity | UC-001-02-02 | ✅ |
| TC-001-02-006 | Login Empty Credentials | UC-001-02-02 | ✅ |
| TC-001-02-007 | User Enumeration Prevention | UC-001-02-02 | ✅ |
| TC-001-02-008 | Protected Route Unauthenticated | UC-001-02-03 | ✅ |
| TC-001-02-009 | Protected Route Authenticated | UC-001-02-02 | ✅ |
| TC-001-02-010 | Navigation Between Auth Components | UC-001-02-01 | ✅ |
| TC-001-03-001 | Logout Flow | UC-001-03-01 | ✅ |

---
