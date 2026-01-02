# Test Cases for Feature 000: Foundation

## Prerequisites

- PostgreSQL database is accessible
- Flyway migrations can execute
- Application can start in dev mode

## Test Data

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | AdminPassword123 | admin |

---

# US-000-01: Establish Authentication Infrastructure

### TC-000-01-001: UserLogin Table Schema Verification
**Parent Use Case:** [UC-000-01-01: Initialize User Account Storage](use-cases.md#uc-000-01-01-initialize-user-account-storage)

**Objective:** Verify the user_login table is created with correct schema.

**Type:** Database verification

**Steps:**
1. Start the application (triggers Flyway migrations)
2. Query database schema for user_login table

**Verification Query:**
```sql
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'user_login'
ORDER BY ordinal_position;
```

**Expected:**
- [ ] Column `id` exists as BIGSERIAL (bigint with identity)
- [ ] Column `email` exists as VARCHAR(255), NOT NULL
- [ ] Column `password` exists as VARCHAR(255), NOT NULL
- [ ] Column `role` exists as VARCHAR(50), NOT NULL, DEFAULT 'user'
- [ ] Column `first_name` exists as VARCHAR(100), NULLABLE
- [ ] Column `last_name` exists as VARCHAR(100), NULLABLE
- [ ] Column `created_at` exists as TIMESTAMP WITH TIME ZONE
- [ ] Column `updated_at` exists as TIMESTAMP WITH TIME ZONE
- [ ] Column `active` exists as BOOLEAN, DEFAULT TRUE

---

### TC-000-01-002: UserLogin Table Constraints Verification
**Parent Use Case:** [UC-000-01-01: Initialize User Account Storage](use-cases.md#uc-000-01-01-initialize-user-account-storage)

**Objective:** Verify the user_login table has correct constraints and indexes.

**Type:** Database verification

**Steps:**
1. Query database for constraints on user_login table
2. Query database for indexes on user_login table

**Verification Queries:**
```sql
-- Check unique constraint
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'user_login';

-- Check indexes
SELECT indexname FROM pg_indexes WHERE tablename = 'user_login';
```

**Expected:**
- [ ] Unique constraint exists on email column (`uq_user_login_email`)
- [ ] Index exists on email column (`idx_user_login_email`)
- [ ] Primary key exists on id column

---

### TC-000-01-003: UserLogin Entity Annotations Verification
**Parent Use Case:** [UC-000-01-02: Verify Secure Password Storage](use-cases.md#uc-000-01-02-verify-secure-password-storage)

**Objective:** Verify the UserLogin entity has correct security annotations.

**Type:** Code inspection / Unit test

**Steps:**
1. Inspect UserLogin.java source code
2. Verify annotations are present

**Expected:**
- [ ] Class annotated with `@Entity` and `@Table(name = "user_login")`
- [ ] Class annotated with `@UserDefinition`
- [ ] Field `email` annotated with `@Username`
- [ ] Field `password` annotated with `@Password(PasswordType.MCF)`
- [ ] Field `role` annotated with `@Roles`

---

### TC-000-01-004: UserLogin Email Normalization
**Parent Use Case:** [UC-000-01-02: Verify Secure Password Storage](use-cases.md#uc-000-01-02-verify-secure-password-storage)

**Objective:** Verify email addresses are normalized to lowercase and trimmed.

**Type:** Integration test

**Steps:**
1. Use UserLoginService to create a user with uppercase email: `"  TEST@EXAMPLE.COM  "`
2. Query the database for the persisted user
3. Verify email is normalized

**Expected:**
- [ ] Email is stored as `"test@example.com"` (lowercase, trimmed)

---

### TC-000-01-005: UserLogin BCrypt Password Hashing
**Parent Use Case:** [UC-000-01-02: Verify Secure Password Storage](use-cases.md#uc-000-01-02-verify-secure-password-storage)

**Objective:** Verify passwords are hashed using BCrypt with cost factor 12.

**Type:** Integration test

**Steps:**
1. Inject UserLoginService
2. Call `userLoginService.create("test@example.com", "TestPassword12345", "user")`
3. Inspect the password field of the returned entity

**Expected:**
- [ ] Password starts with `$2a$12$` (BCrypt, cost 12)
- [ ] Password is not stored in plain text
- [ ] Password hash is approximately 60 characters

---

### TC-000-01-006: UserLoginRepository Finder Methods
**Parent Use Case:** [UC-000-01-02: Verify Secure Password Storage](use-cases.md#uc-000-01-02-verify-secure-password-storage)

**Objective:** Verify repository finder methods work correctly with case-insensitive email lookup.

**Type:** Integration test

**Steps:**
1. Ensure admin user exists
2. Inject UserLoginRepository
3. Call `userLoginRepository.findByEmail("ADMIN@EXAMPLE.COM")` (uppercase)
4. Call `userLoginRepository.emailExists("admin@example.com")`

**Expected:**
- [ ] `findByEmail()` returns the admin user despite case difference
- [ ] `emailExists()` returns true for existing email
- [ ] `emailExists()` returns false for non-existing email

---

### TC-000-01-007: PasswordValidator Minimum Length
**Parent Use Case:** [UC-000-01-03: Enforce Password Policy](use-cases.md#uc-000-01-03-enforce-password-policy)

**Objective:** Verify password minimum length validation (NIST SP 800-63B-4).

**Type:** Unit test

**Steps:**
1. Inject PasswordValidator service
2. Validate password with 14 characters (too short)
3. Validate password with 15 characters (minimum)
4. Validate password with 16 characters (valid)

**Expected:**
- [ ] 14 chars returns error: "Password must be at least 15 characters"
- [ ] 15 chars returns no errors
- [ ] 16 chars returns no errors

---

### TC-000-01-008: PasswordValidator Maximum Length
**Parent Use Case:** [UC-000-01-03: Enforce Password Policy](use-cases.md#uc-000-01-03-enforce-password-policy)

**Objective:** Verify password maximum length validation.

**Type:** Unit test

**Steps:**
1. Inject PasswordValidator service
2. Validate password with 128 characters (maximum)
3. Validate password with 129 characters (too long)

**Expected:**
- [ ] 128 chars returns no errors
- [ ] 129 chars returns error: "Password must be 128 characters or less"

---

### TC-000-01-009: PasswordValidator No Composition Rules
**Parent Use Case:** [UC-000-01-03: Enforce Password Policy](use-cases.md#uc-000-01-03-enforce-password-policy)

**Objective:** Verify NIST compliance - no composition rules enforced.

**Type:** Unit test

**Steps:**
1. Inject PasswordValidator service
2. Validate password with only lowercase letters (15+ chars)
3. Validate password with only numbers (15+ chars)

**Expected:**
- [ ] All-lowercase password is valid (no uppercase required)
- [ ] All-numeric password is valid (no special chars required)
- [ ] No composition rules are enforced per NIST SP 800-63B-4

---

### TC-000-01-010: Admin User Seed Verification
**Parent Use Case:** [UC-000-01-04: Access System with Default Administrator](use-cases.md#uc-000-01-04-access-system-with-default-administrator)

**Objective:** Verify admin user is seeded correctly.

**Type:** Database verification

**Steps:**
1. Start the application (triggers Flyway migrations)
2. Query user_login table for admin user

**Verification Query:**
```sql
SELECT email, role, first_name, last_name, active
FROM user_login
WHERE email = 'admin@example.com';
```

**Expected:**
- [ ] Admin user exists with email `admin@example.com`
- [ ] Role is `admin`
- [ ] first_name is `Admin`
- [ ] last_name is `User`
- [ ] active is `true`

---

### TC-000-01-011: Admin User Authentication
**Parent Use Case:** [UC-000-01-04: Access System with Default Administrator](use-cases.md#uc-000-01-04-access-system-with-default-administrator)

**Objective:** Verify admin user can authenticate with seeded credentials.

**Type:** Integration test (browser-based)

**Steps:**
1. Navigate to homepage
2. Click Login link to open modal
3. Enter email: `admin@example.com`
4. Enter password: `AdminPassword123`
5. Click Login button

**Expected:**
- [ ] Login is successful
- [ ] User is redirected to home page
- [ ] Navigation shows authenticated state with "Logout (Admin User)"

---

### TC-000-01-012: Application Startup Verification
**Parent Use Case:** [UC-000-01-01: Initialize User Account Storage](use-cases.md#uc-000-01-01-initialize-user-account-storage)

**Objective:** Verify application starts successfully with all migrations applied.

**Type:** Smoke test

**Steps:**
1. Start application with `./mvnw quarkus:dev`
2. Wait for startup complete message
3. Check health endpoint

**Verification:**
```bash
curl http://127.0.0.1:9080/q/health
```

**Expected:**
- [ ] Application starts without errors
- [ ] Health check returns status "UP"
- [ ] Flyway migrations show as applied in logs

---

## Test Summary

| Test ID | Use Case | Type | Status |
|---------|----------|------|--------|
| TC-000-01-001 | UC-000-01-01 | Database | ✅ Passed |
| TC-000-01-002 | UC-000-01-01 | Database | ✅ Passed |
| TC-000-01-003 | UC-000-01-02 | Code Inspection | ✅ Passed |
| TC-000-01-004 | UC-000-01-02 | Integration | ✅ Passed |
| TC-000-01-005 | UC-000-01-02 | Integration | ✅ Passed |
| TC-000-01-006 | UC-000-01-02 | Integration | ✅ Passed |
| TC-000-01-007 | UC-000-01-03 | Unit Test | ✅ Passed |
| TC-000-01-008 | UC-000-01-03 | Unit Test | ✅ Passed |
| TC-000-01-009 | UC-000-01-03 | Unit Test | ✅ Passed |
| TC-000-01-010 | UC-000-01-04 | Database | ✅ Passed |
| TC-000-01-011 | UC-000-01-04 | Integration | ✅ Passed |
| TC-000-01-012 | UC-000-01-01 | Smoke Test | ✅ Passed |

---

# US-000-02: Create Application Shell and Landing Page

### TC-000-02-001: Base Layout Structure
**Parent Use Case:** [UC-000-02-01: Display Base Layout](use-cases.md#uc-000-02-01-display-base-layout)

**Objective:** Verify base layout renders with all required components.

**Type:** Browser-based

**Steps:**
1. Navigate to homepage `/`
2. Take a snapshot of the page

**Expected:**
- [ ] Sidebar is visible on left (320px width on desktop)
- [ ] Logo "HX-Qute" is visible in sidebar header
- [ ] Navigation menu is visible in sidebar
- [ ] Main content area is visible on right
- [ ] Copyright footer is visible at bottom of sidebar

---

### TC-000-02-002: CDN Resources Loaded
**Parent Use Case:** [UC-000-02-01: Display Base Layout](use-cases.md#uc-000-02-01-display-base-layout)

**Objective:** Verify external CDN resources load successfully.

**Type:** Browser-based (network inspection)

**Steps:**
1. Navigate to homepage `/`
2. Check network requests for CDN resources

**Expected:**
- [ ] UIkit CSS loaded from cdn.jsdelivr.net (status 200)
- [ ] UIkit JS loaded from cdn.jsdelivr.net (status 200)
- [ ] UIkit Icons loaded from cdn.jsdelivr.net (status 200)
- [ ] HTMX loaded from cdn.jsdelivr.net (status 200)
- [ ] Custom style.css loaded (status 200)

---

### TC-000-02-003: Navigation Menu Items
**Parent Use Case:** [UC-000-02-02: Navigate Using Sidebar Menu](use-cases.md#uc-000-02-02-navigate-using-sidebar-menu)

**Objective:** Verify all navigation items are present and functional.

**Type:** Browser-based

**Steps:**
1. Navigate to homepage `/`
2. Inspect navigation menu

**Expected:**
- [ ] "Home" link present with home icon, points to `/`
- [ ] "People" link present with users icon, points to `/persons`
- [ ] "Graph" link present with git-fork icon, points to `/graph`
- [ ] "Maintenance" dropdown present with settings icon
- [ ] Maintenance submenu contains: Gender (`/genders`), Title (`/titles`), Relationship (`/relationships`)
- [ ] "Login" link present when not authenticated

---

### TC-000-02-004: Navigation Active State
**Parent Use Case:** [UC-000-02-02: Navigate Using Sidebar Menu](use-cases.md#uc-000-02-02-navigate-using-sidebar-menu)

**Objective:** Verify correct navigation item is highlighted based on current page.

**Type:** Browser-based

**Steps:**
1. Navigate to homepage `/`
2. Verify "Home" is highlighted
3. Login as admin
4. Navigate to `/persons`
5. Verify "People" is highlighted

**Expected:**
- [ ] Home page: "Home" nav item has `uk-active` class
- [ ] Persons page: "People" nav item has `uk-active` class
- [ ] Only one nav item is active at a time

---

### TC-000-02-005: Landing Page Content
**Parent Use Case:** [UC-000-02-03: Display Landing Page](use-cases.md#uc-000-02-03-display-landing-page)

**Objective:** Verify landing page displays technology showcase cards.

**Type:** Browser-based

**Steps:**
1. Navigate to homepage `/`
2. Inspect main content area

**Expected:**
- [ ] Four technology cards are displayed
- [ ] "Supersonic Quarkus" card with Quarkus logo
- [ ] "Type-Safe Qute" card with Qute logo
- [ ] "High-powered HTMX" card with HTMX logo
- [ ] "Robust PostgreSQL" card with PostgreSQL logo
- [ ] Cards have hover effect (lift and shadow)

---

### TC-000-02-006: Development Mode Alert
**Parent Use Case:** [UC-000-02-03: Display Landing Page](use-cases.md#uc-000-02-03-display-landing-page)

**Objective:** Verify development mode alert displays default credentials.

**Type:** Browser-based (dev mode)

**Steps:**
1. Start application in dev mode (`./mvnw quarkus:dev`)
2. Navigate to homepage `/`

**Expected:**
- [ ] Alert box is visible below technology cards
- [ ] Alert shows "Development Server" heading
- [ ] Alert displays default email: `admin@example.com`
- [ ] Alert displays default password: `AdminPassword123`

---

### TC-000-02-007: Mobile Offcanvas Navigation
**Parent Use Case:** [UC-000-02-04: Display Mobile Navigation](use-cases.md#uc-000-02-04-display-mobile-navigation)

**Objective:** Verify mobile navigation works correctly on small screens.

**Type:** Browser-based (mobile viewport)

**Steps:**
1. Set viewport width to 768px (mobile)
2. Navigate to homepage `/`
3. Verify sidebar is hidden
4. Click hamburger menu icon
5. Verify offcanvas opens

**Expected:**
- [ ] Desktop sidebar is hidden on mobile viewport
- [ ] Hamburger menu icon visible in mobile header
- [ ] Clicking hamburger opens offcanvas sidebar
- [ ] Offcanvas contains same navigation as desktop
- [ ] Clicking nav item closes offcanvas and navigates

---

## Test Summary (US-000-02)

| Test ID | Use Case | Type | Status |
|---------|----------|------|--------|
| TC-000-02-001 | UC-000-02-01 | Browser | ✅ Passed |
| TC-000-02-002 | UC-000-02-01 | Browser | ✅ Passed |
| TC-000-02-003 | UC-000-02-02 | Browser | ✅ Passed |
| TC-000-02-004 | UC-000-02-02 | Browser | ✅ Passed |
| TC-000-02-005 | UC-000-02-03 | Browser | ✅ Passed |
| TC-000-02-006 | UC-000-02-03 | Browser | ✅ Passed |
| TC-000-02-007 | UC-000-02-04 | Browser | ✅ Passed |

---
