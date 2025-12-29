# Test Cases for Identity and Access Management

## Prerequisites

- Application running at `http://localhost:9080`
- Fresh database state (or known test data)
- Default admin user: `admin@example.com` / `AdminPassword123`

## Test Data

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | AdminPassword123 | admin |

**Note:** Per NIST SP 800-63B-4, new user passwords must be at least 15 characters.

---

# US-001-01: User Registration

### TC-001-01-001: Signup Page UI Elements
**Parent Use Case:** [UC-001-01-01: Display Signup Page](use-cases.md#uc-001-01-01-display-signup-page)

**Objective:** Verify signup page renders correctly with all required elements.

**Steps:**
1. Navigate to `/signup`
2. Take a snapshot of the page

**Expected:**
- [ ] Page title contains "Sign Up"
- [ ] Email input field exists (id: `email`)
- [ ] Password input field exists (id: `password`)
- [ ] Submit button with text "Sign Up" exists
- [ ] Link to login page exists with text "Login"

---

### TC-001-01-002: Signup Successful Registration
**Parent Use Case:** [UC-001-01-02: Register New User](use-cases.md#uc-001-01-02-register-new-user)

**Objective:** Verify a new user can register successfully.

**Steps:**
1. Navigate to `/signup`
2. Fill form:
   - email: `testuser1@example.com`
   - password: `TestPassword12345` (15+ characters)
3. Click "Sign Up" button
4. Wait for navigation

**Expected:**
- [ ] Redirected to `/login` page
- [ ] No error message displayed

---

### TC-001-01-003: Signup Email Required
**Parent Use Case:** [UC-001-01-02: Register New User](use-cases.md#uc-001-01-02-register-new-user)

**Objective:** Verify email is required for registration.

**Steps:**
1. Navigate to `/signup`
2. Fill form:
   - email: (leave empty)
   - password: `TestPassword12345`
3. Click "Sign Up" button

**Expected:**
- [ ] Error message "Email is required." displayed
- [ ] User remains on signup page

---

### TC-001-01-004: Signup Password Minimum Length
**Parent Use Case:** [UC-001-01-02: Register New User](use-cases.md#uc-001-01-02-register-new-user)

**Objective:** Verify password must be at least 15 characters (NIST SP 800-63B-4).

**Steps:**
1. Navigate to `/signup`
2. Fill form:
   - email: `test@example.com`
   - password: `short` (less than 15 characters)
3. Click "Sign Up" button

**Expected:**
- [ ] Error message "Password must be at least 15 characters." displayed
- [ ] User remains on signup page

---

### TC-001-01-005: Signup Duplicate Email Prevention
**Parent Use Case:** [UC-001-01-02: Register New User](use-cases.md#uc-001-01-02-register-new-user)

**Objective:** Verify system prevents duplicate email addresses.

**Steps:**
1. Navigate to `/signup`
2. Fill form with existing email:
   - email: `admin@example.com`
   - password: `TestPassword12345`
3. Click "Sign Up" button

**Expected:**
- [ ] Error message "Email already registered." displayed
- [ ] User remains on signup page

---

### TC-001-01-006: Signup Email Case Insensitivity
**Parent Use Case:** [UC-001-01-02: Register New User](use-cases.md#uc-001-01-02-register-new-user)

**Objective:** Verify email addresses are matched case-insensitively.

**Steps:**
1. Navigate to `/signup`
2. Fill form:
   - email: `ADMIN@EXAMPLE.COM` (uppercase version of existing email)
   - password: `TestPassword12345`
3. Click "Sign Up" button

**Expected:**
- [ ] Error message "Email already registered." displayed
- [ ] Email matching is case-insensitive

---

### TC-001-01-007: Form Input Sanitization
**Parent Use Case:** [UC-001-01-02: Register New User](use-cases.md#uc-001-01-02-register-new-user)

**Objective:** Verify inputs are properly trimmed and normalized.

**Steps:**
1. Navigate to `/signup`
2. Fill form with whitespace:
   - email: `  TEST2@EXAMPLE.COM  ` (spaces and uppercase)
   - password: `TestPassword12345`
3. Click "Sign Up" button
4. Then login with:
   - j_username: `test2@example.com` (no spaces, lowercase)
   - j_password: `TestPassword12345`

**Expected:**
- [ ] Registration succeeds
- [ ] Email stored as lowercase, trimmed
- [ ] Login works with normalized values

---

### TC-001-01-008: Signup Email Client-Side Normalization
**Parent Use Case:** [UC-001-01-02: Register New User](use-cases.md#uc-001-01-02-register-new-user)

**Objective:** Verify signup form normalizes email (lowercase + trim) before submission via onsubmit handler.

**Steps:**
1. Navigate to `/signup`
2. Take a snapshot to verify form has onsubmit handler
3. Fill form:
   - email: `  TESTCLIENT@EXAMPLE.COM  ` (spaces and uppercase)
   - password: `TestPassword12345`
4. Use evaluate_script to check email field value before and after form validation triggers
5. Click "Sign Up" button

**Expected:**
- [ ] Form element has onsubmit attribute with normalization logic
- [ ] Email value is transformed to `testclient@example.com` (lowercase, trimmed) on submit
- [ ] Registration succeeds with normalized email

---

# US-001-02: User Login

### TC-001-02-001: Login Page UI Elements
**Parent Use Case:** [UC-001-02-01: Display Login Page](use-cases.md#uc-001-02-01-display-login-page)

**Objective:** Verify login page renders correctly with all required elements.

**Steps:**
1. Navigate to `/login`
2. Take a snapshot of the page

**Expected:**
- [ ] Page title contains "Login"
- [ ] Email input field exists (id: `j_username`, label: "Email")
- [ ] Password input field exists (id: `j_password`)
- [ ] Submit button with text "Login" exists
- [ ] Link to signup page exists with text "Sign up"

---

### TC-001-02-002: Login Successful Authentication
**Parent Use Case:** [UC-001-02-02: Authenticate User](use-cases.md#uc-001-02-02-authenticate-user)

**Objective:** Verify valid credentials allow login.

**Steps:**
1. Navigate to `/login`
2. Fill form:
   - j_username: `admin@example.com`
   - j_password: `AdminPassword123`
3. Click "Login" button
4. Wait for navigation

**Expected:**
- [ ] Redirected to home page
- [ ] User is authenticated (navigation shows logged-in state)
- [ ] No error message displayed

---

### TC-001-02-003: Login Invalid Password
**Parent Use Case:** [UC-001-02-02: Authenticate User](use-cases.md#uc-001-02-02-authenticate-user)

**Objective:** Verify invalid password is rejected with generic error.

**Steps:**
1. Navigate to `/login`
2. Fill form:
   - j_username: `admin@example.com`
   - j_password: `WrongPassword123`
3. Click "Login" button

**Expected:**
- [ ] Error message "Invalid email or password." displayed
- [ ] Error does NOT reveal whether email exists

---

### TC-001-02-004: Login Invalid Email
**Parent Use Case:** [UC-001-02-02: Authenticate User](use-cases.md#uc-001-02-02-authenticate-user)

**Objective:** Verify invalid email is rejected with generic error.

**Steps:**
1. Navigate to `/login`
2. Fill form:
   - j_username: `nonexistent@example.com`
   - j_password: `SomePassword123`
3. Click "Login" button

**Expected:**
- [ ] Error message "Invalid email or password." displayed
- [ ] Error does NOT reveal whether email exists

---

### TC-001-02-005: Login Email Case Insensitivity
**Parent Use Case:** [UC-001-02-02: Authenticate User](use-cases.md#uc-001-02-02-authenticate-user)

**Objective:** Verify login works regardless of email case.

**Steps:**
1. Navigate to `/login`
2. Fill form:
   - j_username: `ADMIN@EXAMPLE.COM` (uppercase)
   - j_password: `AdminPassword123`
3. Click "Login" button

**Expected:**
- [ ] Login successful
- [ ] Email matching is case-insensitive

---

### TC-001-02-006: Login Empty Credentials
**Parent Use Case:** [UC-001-02-02: Authenticate User](use-cases.md#uc-001-02-02-authenticate-user)

**Objective:** Verify empty form submission is handled.

**Steps:**
1. Navigate to `/login`
2. Leave form empty
3. Attempt to click "Login" button

**Expected:**
- [ ] Browser validation prevents submission (required fields)
- [ ] OR server returns appropriate error

---

### TC-001-02-007: User Enumeration Prevention
**Parent Use Case:** [UC-001-02-02: Authenticate User](use-cases.md#uc-001-02-02-authenticate-user)

**Objective:** Verify system does not leak user existence information.

**Steps:**
1. Login with existing email, wrong password
2. Note error message
3. Login with non-existing email, any password
4. Note error message
5. Compare messages

**Expected:**
- [ ] Both error messages are identical: "Invalid email or password."
- [ ] No timing difference observable between responses
- [ ] No indication of whether email exists

---

### TC-001-02-008: Protected Route Unauthenticated
**Parent Use Case:** [UC-001-02-03: Access Protected Route](use-cases.md#uc-001-02-03-access-protected-route-unauthenticated)

**Objective:** Verify protected routes require authentication.

**Steps:**
1. Ensure no active session (logout if needed)
2. Navigate to `/persons`

**Expected:**
- [ ] Redirected to `/login` page
- [ ] Cannot access protected content without authentication

---

### TC-001-02-009: Protected Route Authenticated
**Parent Use Case:** [UC-001-02-02: Authenticate User](use-cases.md#uc-001-02-02-authenticate-user)

**Objective:** Verify authenticated users can access protected routes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`

**Expected:**
- [ ] Persons page loads successfully
- [ ] User sees the persons list

---

### TC-001-02-010: Navigation Between Auth Pages
**Parent Use Case:** [UC-001-02-01: Display Login Page](use-cases.md#uc-001-02-01-display-login-page)

**Objective:** Verify navigation links work correctly.

**Steps:**
1. Navigate to `/login`
2. Click "Sign up" link
3. Verify on signup page
4. Click "Login" link
5. Verify on login page

**Expected:**
- [ ] "Sign up" link navigates from login to signup page
- [ ] "Login" link navigates from signup to login page

---

# US-001-03: User Logout

### TC-001-03-001: Logout Flow
**Parent Use Case:** [UC-001-03-01: Logout User](use-cases.md#uc-001-03-01-logout-user)

**Objective:** Verify user can logout successfully.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/logout`
3. Take a snapshot of the page

**Expected:**
- [ ] Logout page is displayed
- [ ] User session is terminated
- [ ] Accessing protected routes redirects to login
