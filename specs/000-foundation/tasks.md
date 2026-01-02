# Implementation Plan for Feature 000: Foundation

This phase establishes the database schema, entity classes, repository, and services required for authentication.

## UC-000-01-01: Initialize User Account Storage

**Status:** ✅ Complete
**Parent Story:** US-000-01 - Establish Authentication Infrastructure

**Description:** Create Flyway migration for the `user_login` table with all required columns.

**Implementation Tasks:**
- [x] Create migration file `V1.2.0__Create_user_login_table.sql`
- [x] Define columns: id, email, password, role, first_name, last_name, created_at, updated_at, active
- [x] Add unique constraint on email (`uq_user_login_email`)
- [x] Add index on email column (`idx_user_login_email`)

**File Created:**
- `src/main/resources/db/migration/V1.2.0__Create_user_login_table.sql`

**Test Results:**
- Test ID: TC-000-01-001, TC-000-01-002, TC-000-01-012
- Status: ✅ Passed
- Notes: Migration applied successfully. Schema and constraints verified.

---

## UC-000-01-02: Implement Secure Password Storage

**Status:** ✅ Complete
**Parent Story:** US-000-01 - Establish Authentication Infrastructure

**Description:** Implement the authentication infrastructure with proper layered architecture.

**Implementation Tasks:**
- [x] Create `entity/UserLogin.java` - JPA entity with Quarkus Security annotations
- [x] Create `repository/UserLoginRepository.java` - Panache repository for database operations
- [x] Create `service/UserLoginService.java` - Service for user creation with BCrypt hashing
- [x] Add `@UserDefinition` annotation to entity
- [x] Add `@Username` on email field
- [x] Add `@Password(PasswordType.MCF)` on password field
- [x] Add `@Roles` on role field
- [x] Implement `@PrePersist` and `@PreUpdate` hooks for timestamps and email normalization
- [x] Implement BCrypt hashing (cost factor 12) in service layer
- [x] Implement case-insensitive email lookup in repository

**Files Created:**
- `src/main/java/io/archton/scaffold/entity/UserLogin.java`
- `src/main/java/io/archton/scaffold/repository/UserLoginRepository.java`
- `src/main/java/io/archton/scaffold/service/UserLoginService.java`

**Test Results:**
- Test ID: TC-000-01-003, TC-000-01-004, TC-000-01-005, TC-000-01-006
- Status: ✅ Passed
- Notes: All components implemented and verified. BCrypt hashing confirmed with cost 12. Email normalization working.

---

## UC-000-01-03: Implement Password Validation Service

**Status:** ✅ Complete
**Parent Story:** US-000-01 - Establish Authentication Infrastructure

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

**Files Created:**
- `src/main/java/io/archton/scaffold/service/PasswordValidator.java`

**Test Results:**
- Test ID: TC-000-01-007, TC-000-01-008, TC-000-01-009
- Status: ✅ Passed
- Notes: Service validates correctly. Min/max length enforcement verified. No composition rules per NIST.

---

## UC-000-01-04: Seed Admin User

**Status:** ✅ Complete
**Parent Story:** US-000-01 - Establish Authentication Infrastructure

**Description:** Create Flyway migration to insert default admin user for testing.

**Implementation Tasks:**
- [x] Create migration file `V1.2.1__Insert_admin_user.sql`
- [x] Insert admin user with BCrypt hashed password
- [x] Email: `admin@example.com`
- [x] Password: `AdminPassword123` (BCrypt hashed, cost 12)
- [x] Role: `admin`

**File Created:**
- `src/main/resources/db/migration/V1.2.1__Insert_admin_user.sql`

**Test Results:**
- Test ID: TC-000-01-010, TC-000-01-011
- Status: ✅ Passed
- Notes: Migration applied successfully. Admin user authentication verified.

---

## Implementation Summary

### Files Created

| File | Purpose |
|------|---------|
| `src/main/java/io/archton/scaffold/entity/UserLogin.java` | JPA entity with Quarkus Security JPA annotations |
| `src/main/java/io/archton/scaffold/repository/UserLoginRepository.java` | Panache repository for user lookups |
| `src/main/java/io/archton/scaffold/service/UserLoginService.java` | User creation service with BCrypt hashing |
| `src/main/java/io/archton/scaffold/service/PasswordValidator.java` | NIST-compliant password validation |
| `src/main/resources/db/migration/V1.2.0__Create_user_login_table.sql` | Database table creation |
| `src/main/resources/db/migration/V1.2.1__Insert_admin_user.sql` | Admin user seed data |

### Test Cases Reference

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-000-01-001 | UserLogin Table Schema | UC-000-01-01 | ✅ |
| TC-000-01-002 | UserLogin Table Constraints | UC-000-01-01 | ✅ |
| TC-000-01-003 | UserLogin Entity Annotations | UC-000-01-02 | ✅ |
| TC-000-01-004 | UserLogin Email Normalization | UC-000-01-02 | ✅ |
| TC-000-01-005 | UserLogin BCrypt Hashing | UC-000-01-02 | ✅ |
| TC-000-01-006 | UserLoginRepository Finder Methods | UC-000-01-02 | ✅ |
| TC-000-01-007 | PasswordValidator Min Length | UC-000-01-03 | ✅ |
| TC-000-01-008 | PasswordValidator Max Length | UC-000-01-03 | ✅ |
| TC-000-01-009 | PasswordValidator No Composition | UC-000-01-03 | ✅ |
| TC-000-01-010 | Admin User Seed Verification | UC-000-01-04 | ✅ |
| TC-000-01-011 | Admin User Authentication | UC-000-01-04 | ✅ |
| TC-000-01-012 | Application Startup | UC-000-01-01 | ✅ |

---

# US-000-02: Create Application Shell and Landing Page

## UC-000-02-01: Create Base Layout Template

**Status:** ✅ Complete
**Parent Story:** US-000-02 - Create Application Shell and Landing Page

**Description:** Create the base.html template with responsive layout, CDN includes, and login modal.

**Implementation Tasks:**
- [x] Create `templates/base.html` with Qute template parameters
- [x] Add CDN links for UIkit CSS/JS (3.25.4)
- [x] Add CDN link for HTMX (2.0.8)
- [x] Create responsive two-column layout (sidebar + main content)
- [x] Add logo with "HX-Qute" text in sidebar header
- [x] Include navigation fragment: `{#include fragments/navigation /}`
- [x] Add copyright footer in sidebar
- [x] Create mobile offcanvas sidebar (`#mobile-sidebar`)
- [x] Add mobile header with hamburger toggle
- [x] Create login modal (`#login-modal`) with form
- [x] Add JavaScript to open modal on `?login=true`
- [x] Add JavaScript to show error on `?error=true`

**Files Created:**
- `src/main/resources/templates/base.html`

**Test Results:**
- Test ID: TC-000-02-001, TC-000-02-002
- Status: ✅ Passed

---

## UC-000-02-02: Create Navigation Fragment

**Status:** ✅ Complete
**Parent Story:** US-000-02 - Create Application Shell and Landing Page

**Description:** Create the navigation.html fragment with menu items and active state.

**Implementation Tasks:**
- [x] Create `templates/fragments/navigation.html`
- [x] Add navigation menu as `<ul class="uk-nav uk-nav-default">`
- [x] Add menu items with UIkit icons: Home, People, Graph
- [x] Add Maintenance dropdown with submenu: Gender, Title, Relationship
- [x] Implement active state using `currentPage` template variable
- [x] Add conditional Login link (when `!userName`)
- [x] Add conditional Logout link showing username (when `userName`)

**Files Created:**
- `src/main/resources/templates/fragments/navigation.html`

**Test Results:**
- Test ID: TC-000-02-003, TC-000-02-004
- Status: ✅ Passed

---

## UC-000-02-03: Create Landing Page

**Status:** ✅ Complete
**Parent Story:** US-000-02 - Create Application Shell and Landing Page

**Description:** Create the IndexResource endpoint and landing page template.

**Implementation Tasks:**
- [x] Create `router/IndexResource.java` with `@Path("/")`
- [x] Inject `SecurityIdentity` for authentication state
- [x] Inject `LaunchMode` for development mode detection
- [x] Create `@CheckedTemplate` class with `index()` method
- [x] Implement `GET /` endpoint returning template instance
- [x] Create `templates/IndexResource/index.html`
- [x] Add four technology showcase cards (Quarkus, Qute, HTMX, PostgreSQL)
- [x] Add development mode alert with default credentials
- [x] Add tech card images to `META-INF/resources/img/`

**Files Created:**
- `src/main/java/io/archton/scaffold/router/IndexResource.java`
- `src/main/resources/templates/IndexResource/index.html`

**Test Results:**
- Test ID: TC-000-02-005, TC-000-02-006
- Status: ✅ Passed

---

## UC-000-02-04: Create Custom Stylesheet

**Status:** ✅ Complete
**Parent Story:** US-000-02 - Create Application Shell and Landing Page

**Description:** Create style.css with brand colors and component styles.

**Implementation Tasks:**
- [x] Create `META-INF/resources/style.css`
- [x] Define CSS variables for brand colors
- [x] Style sidebar (320px width, light gray background)
- [x] Style main content area (dirty-sage background)
- [x] Style tech cards (white bg, blue border, hover effect)
- [x] Style navigation active state
- [x] Override UIkit table header styling (rose color, normal case)
- [x] Style logo link (no underline, hover opacity)
- [x] Style mobile offcanvas

**Files Created:**
- `src/main/resources/META-INF/resources/style.css`

**Test Results:**
- Test ID: TC-000-02-007
- Status: ✅ Passed

---

## Implementation Summary (US-000-02)

### Files Created

| File | Purpose |
|------|---------|
| `src/main/java/io/archton/scaffold/router/IndexResource.java` | Landing page endpoint |
| `src/main/resources/templates/base.html` | Base layout template |
| `src/main/resources/templates/fragments/navigation.html` | Sidebar navigation |
| `src/main/resources/templates/IndexResource/index.html` | Landing page content |
| `src/main/resources/META-INF/resources/style.css` | Custom CSS styles |
| `src/main/resources/META-INF/resources/img/logo-scaffold.png` | Application logo |
| `src/main/resources/META-INF/resources/img/Quarkus.svg` | Tech card icon |
| `src/main/resources/META-INF/resources/img/Quarkus_black.svg` | Tech card icon (Qute) |
| `src/main/resources/META-INF/resources/img/HTMX.svg` | Tech card icon |
| `src/main/resources/META-INF/resources/img/PostgresSQL.svg` | Tech card icon |

### Test Cases Reference (US-000-02)

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-000-02-001 | Base Layout Structure | UC-000-02-01 | ✅ |
| TC-000-02-002 | CDN Resources Loaded | UC-000-02-01 | ✅ |
| TC-000-02-003 | Navigation Menu Items | UC-000-02-02 | ✅ |
| TC-000-02-004 | Navigation Active State | UC-000-02-02 | ✅ |
| TC-000-02-005 | Landing Page Content | UC-000-02-03 | ✅ |
| TC-000-02-006 | Development Mode Alert | UC-000-02-03 | ✅ |
| TC-000-02-007 | Mobile Offcanvas Navigation | UC-000-02-04 | ✅ |

---
