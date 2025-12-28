# HX Qute - Test Cases

**Product:** HX Qute Reference Application
**Document Version:** 1.0
**Last Updated:** 2025-12-27

---

## Overview

This document defines the test cases for browser-based testing using Chrome DevTools MCP. Each test case is linked to its parent use case and includes specific steps and expected outcomes.

### Document References

| Document | Description |
|----------|-------------|
| USE-CASES.md | Use cases with acceptance criteria |
| USER-STORIES.md | User stories organized by epic |
| LOGIN-PHASED.md | Authentication technical specification (Phase 1 implementation) |

### Prerequisites

- Application running at `http://localhost:9080`
- Fresh database state (or known test data)
- Default admin user: `admin@example.com` / `AdminPassword123`

### Test Data

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | AdminPassword123 | admin |

**Note:** Per NIST SP 800-63B-4, new user passwords must be at least 15 characters. 

---

## TC-1: Authentication Tests

### TC-1.01: Signup Page UI Elements
**Parent Use Case:** [UC-1.1: Display Signup Page](USE-CASES.md#uc-11-display-signup-page)

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

### TC-1.02: Signup Successful Registration
**Parent Use Case:** [UC-1.2: Register New User](USE-CASES.md#uc-12-register-new-user)

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

### TC-1.03: Signup Email Required
**Parent Use Case:** [UC-1.2: Register New User](USE-CASES.md#uc-12-register-new-user)

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

### TC-1.04: Signup Password Minimum Length
**Parent Use Case:** [UC-1.2: Register New User](USE-CASES.md#uc-12-register-new-user)

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

### TC-1.05: Signup Duplicate Email Prevention
**Parent Use Case:** [UC-1.2: Register New User](USE-CASES.md#uc-12-register-new-user)

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

### TC-1.06: Signup Email Case Insensitivity
**Parent Use Case:** [UC-1.2: Register New User](USE-CASES.md#uc-12-register-new-user)

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

### TC-1.07: Login Page UI Elements
**Parent Use Case:** [UC-1.3: Display Login Page](USE-CASES.md#uc-13-display-login-page)

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

### TC-1.08: Login Successful Authentication
**Parent Use Case:** [UC-1.4: Authenticate User](USE-CASES.md#uc-14-authenticate-user)

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

### TC-1.09: Login Invalid Password
**Parent Use Case:** [UC-1.4: Authenticate User](USE-CASES.md#uc-14-authenticate-user)

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

### TC-1.10: Login Invalid Email
**Parent Use Case:** [UC-1.4: Authenticate User](USE-CASES.md#uc-14-authenticate-user)

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

### TC-1.11: Login Email Case Insensitivity
**Parent Use Case:** [UC-1.4: Authenticate User](USE-CASES.md#uc-14-authenticate-user)

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

### TC-1.12: Login Empty Credentials
**Parent Use Case:** [UC-1.4: Authenticate User](USE-CASES.md#uc-14-authenticate-user)

**Objective:** Verify empty form submission is handled.

**Steps:**
1. Navigate to `/login`
2. Leave form empty
3. Attempt to click "Login" button

**Expected:**
- [ ] Browser validation prevents submission (required fields)
- [ ] OR server returns appropriate error

---

### TC-1.13: Logout Flow
**Parent Use Case:** [UC-1.5: Logout User](USE-CASES.md#uc-15-logout-user)

**Objective:** Verify user can logout successfully.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/logout`
3. Take a snapshot of the page

**Expected:**
- [ ] Logout page is displayed
- [ ] User session is terminated
- [ ] Accessing protected routes redirects to login

---

### TC-1.14: Protected Route Unauthenticated
**Parent Use Case:** [UC-1.6: Access Protected Route](USE-CASES.md#uc-16-access-protected-route-unauthenticated)

**Objective:** Verify protected routes require authentication.

**Steps:**
1. Ensure no active session (logout if needed)
2. Navigate to `/persons`

**Expected:**
- [ ] Redirected to `/login` page
- [ ] Cannot access protected content without authentication

---

### TC-1.15: Protected Route Authenticated
**Parent Use Case:** [UC-1.4: Authenticate User](USE-CASES.md#uc-14-authenticate-user)

**Objective:** Verify authenticated users can access protected routes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`

**Expected:**
- [ ] Persons page loads successfully
- [ ] User sees the persons list

---

### TC-1.16: Navigation Between Auth Pages
**Parent Use Case:** [UC-1.1: Display Signup Page](USE-CASES.md#uc-11-display-signup-page), [UC-1.3: Display Login Page](USE-CASES.md#uc-13-display-login-page)

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

### TC-1.17: User Enumeration Prevention
**Parent Use Case:** [UC-1.4: Authenticate User](USE-CASES.md#uc-14-authenticate-user)

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

### TC-1.18: Form Input Sanitization
**Parent Use Case:** [UC-1.2: Register New User](USE-CASES.md#uc-12-register-new-user)

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

## TC-2: Gender Master Data Tests

### TC-2.01: Gender Page UI Elements
**Parent Use Case:** [UC-2.1: View Gender List](USE-CASES.md#uc-21-view-gender-list)

**Objective:** Verify gender list page renders correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders` (via Maintenance menu)
3. Take a snapshot of the page

**Expected:**
- [ ] Page title contains "Gender"
- [ ] Table with columns: Code, Description, Actions
- [ ] Add button is visible
- [ ] Navigation shows Maintenance menu item as active

---

### TC-2.02: Gender List Display
**Parent Use Case:** [UC-2.1: View Gender List](USE-CASES.md#uc-21-view-gender-list)

**Objective:** Verify gender list displays existing records.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Verify table content

**Expected:**
- [ ] Existing gender records displayed in table
- [ ] Records sorted by code (ascending)
- [ ] Each row has Edit and Delete buttons

---

### TC-2.03: Gender List Empty State
**Parent Use Case:** [UC-2.1: View Gender List](USE-CASES.md#uc-21-view-gender-list)

**Objective:** Verify empty state message when no records exist.

**Precondition:** No gender records in database

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`

**Expected:**
- [ ] Message "No gender entries found" displayed
- [ ] Add button still visible

---

### TC-2.04: Gender Create Form Display
**Parent Use Case:** [UC-2.2: Create Gender](USE-CASES.md#uc-22-create-gender)

**Objective:** Verify create form displays correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button

**Expected:**
- [ ] Create form displayed
- [ ] Code input field exists
- [ ] Description input field exists
- [ ] Save and Cancel buttons visible

---

### TC-2.05: Gender Create Success
**Parent Use Case:** [UC-2.2: Create Gender](USE-CASES.md#uc-22-create-gender)

**Objective:** Verify successful gender creation.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `x`
   - description: `Non-binary`
5. Click Save button

**Expected:**
- [ ] Redirected to gender list
- [ ] New entry appears in list
- [ ] Code displayed as uppercase (`X`)

---

### TC-2.06: Gender Create Code Uppercase Coercion
**Parent Use Case:** [UC-2.2: Create Gender](USE-CASES.md#uc-22-create-gender)

**Objective:** Verify code is coerced to uppercase.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `abc` (lowercase)
   - description: `Test Gender`
5. Click Save button

**Expected:**
- [ ] Code stored as `ABC` (uppercase)
- [ ] Entry displays with uppercase code

---

### TC-2.07: Gender Create Duplicate Code Prevention
**Parent Use Case:** [UC-2.2: Create Gender](USE-CASES.md#uc-22-create-gender)

**Objective:** Verify duplicate code is rejected.

**Precondition:** Gender with code `M` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `M`
   - description: `Another Male`
5. Click Save button

**Expected:**
- [ ] Error message "Code already exists" displayed
- [ ] User remains on create form

---

### TC-2.08: Gender Create Duplicate Description Prevention
**Parent Use Case:** [UC-2.2: Create Gender](USE-CASES.md#uc-22-create-gender)

**Objective:** Verify duplicate description is rejected.

**Precondition:** Gender with description `Male` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `Z`
   - description: `Male`
5. Click Save button

**Expected:**
- [ ] Error message "Description already exists" displayed
- [ ] User remains on create form

---

### TC-2.09: Gender Create Code Max Length
**Parent Use Case:** [UC-2.2: Create Gender](USE-CASES.md#uc-22-create-gender)

**Objective:** Verify code maximum length validation.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `ABCDEFGH` (8 characters, exceeds 7)
   - description: `Test`
5. Click Save button

**Expected:**
- [ ] Error message about code length displayed
- [ ] User remains on create form

---

### TC-2.10: Gender Edit Form Display
**Parent Use Case:** [UC-2.3: Edit Gender](USE-CASES.md#uc-23-edit-gender)

**Objective:** Verify edit form displays with pre-populated data.

**Precondition:** Gender record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for an existing entry

**Expected:**
- [ ] Edit form displayed
- [ ] Code field pre-populated with current value
- [ ] Description field pre-populated with current value
- [ ] Audit fields displayed (read-only)
- [ ] Save and Cancel buttons visible

---

### TC-2.11: Gender Edit Success
**Parent Use Case:** [UC-2.3: Edit Gender](USE-CASES.md#uc-23-edit-gender)

**Objective:** Verify successful gender update.

**Precondition:** Gender record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for an existing entry
4. Modify description to `Updated Description`
5. Click Save button

**Expected:**
- [ ] Redirected to gender list
- [ ] Updated entry reflects changes
- [ ] Audit fields updated (updatedBy, updatedAt)

---

### TC-2.12: Gender Edit Cancel
**Parent Use Case:** [UC-2.3: Edit Gender](USE-CASES.md#uc-23-edit-gender)

**Objective:** Verify cancel discards changes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for an existing entry
4. Modify description
5. Click Cancel button

**Expected:**
- [ ] Redirected to gender list
- [ ] Original values unchanged

---

### TC-2.13: Gender Delete Confirmation
**Parent Use Case:** [UC-2.4: Delete Gender](USE-CASES.md#uc-24-delete-gender)

**Objective:** Verify delete confirmation dialog appears.

**Precondition:** Gender record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for an existing entry

**Expected:**
- [ ] Confirmation dialog appears
- [ ] Dialog asks "Are you sure you want to delete this entry?"
- [ ] Confirm and Cancel options available

---

### TC-2.14: Gender Delete Success
**Parent Use Case:** [UC-2.4: Delete Gender](USE-CASES.md#uc-24-delete-gender)

**Objective:** Verify successful gender deletion.

**Precondition:** Gender record exists (not in use)

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for an entry
4. Confirm deletion

**Expected:**
- [ ] Entry removed from list
- [ ] List updated immediately

---

### TC-2.15: Gender Delete Cancel
**Parent Use Case:** [UC-2.4: Delete Gender](USE-CASES.md#uc-24-delete-gender)

**Objective:** Verify cancel preserves record.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for an entry
4. Click Cancel in confirmation dialog

**Expected:**
- [ ] Dialog closes
- [ ] Entry still exists in list

---

### TC-2.16: Gender Access Requires Admin Role
**Parent Use Case:** [UC-2.1: View Gender List](USE-CASES.md#uc-21-view-gender-list)

**Objective:** Verify only admin users can access gender management.

**Precondition:** User with "user" role exists

**Steps:**
1. Login as non-admin user
2. Attempt to navigate to `/genders`

**Expected:**
- [ ] Access denied or redirected
- [ ] Error message indicates insufficient permissions

---

## TC-3: Persons Management Tests

### TC-3.01: Persons Page UI Elements
**Parent Use Case:** [UC-3.1: View Persons List](USE-CASES.md#uc-31-view-persons-list)

**Objective:** Verify persons list page renders correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Take a snapshot of the page

**Expected:**
- [ ] Page title contains "Persons"
- [ ] Table with columns: First Name, Last Name, Email, Phone, Date of Birth, Gender, Actions
- [ ] Add button is visible
- [ ] Filter panel is visible above table

---

### TC-3.02: Persons List Display
**Parent Use Case:** [UC-3.1: View Persons List](USE-CASES.md#uc-31-view-persons-list)

**Objective:** Verify persons list displays existing records.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Verify table content

**Expected:**
- [ ] Existing person records displayed in table
- [ ] Records sorted by lastName, then firstName (ascending)
- [ ] Each row has Edit and Delete buttons

---

### TC-3.03: Persons List Empty State
**Parent Use Case:** [UC-3.1: View Persons List](USE-CASES.md#uc-31-view-persons-list)

**Objective:** Verify empty state message when no records exist.

**Precondition:** No person records in database (except admin)

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`

**Expected:**
- [ ] Message "No persons found" displayed
- [ ] Add button still visible

---

### TC-3.04: Person Create Form Display
**Parent Use Case:** [UC-3.2: Create Person](USE-CASES.md#uc-32-create-person)

**Objective:** Verify create form displays correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button

**Expected:**
- [ ] Create form displayed
- [ ] firstName input field exists
- [ ] lastName input field exists
- [ ] email input field exists
- [ ] phone input field exists
- [ ] dateOfBirth date picker exists
- [ ] gender dropdown exists with available options
- [ ] Save and Cancel buttons visible

---

### TC-3.05: Person Create Success
**Parent Use Case:** [UC-3.2: Create Person](USE-CASES.md#uc-32-create-person)

**Objective:** Verify successful person creation.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form:
   - firstName: `John`
   - lastName: `Doe`
   - email: `john.doe@example.com`
   - phone: `555-1234`
   - dateOfBirth: `1990-01-15`
   - gender: Select an available option
5. Click Save button

**Expected:**
- [ ] Redirected to persons list
- [ ] New entry appears in list
- [ ] Email stored as lowercase
- [ ] Gender selection saved correctly

---

### TC-3.06: Person Create Email Required
**Parent Use Case:** [UC-3.2: Create Person](USE-CASES.md#uc-32-create-person)

**Objective:** Verify email is required.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form without email:
   - firstName: `Jane`
   - lastName: `Doe`
   - email: (leave empty)
5. Click Save button

**Expected:**
- [ ] Error message "Email is required" displayed
- [ ] User remains on create form

---

### TC-3.07: Person Create Email Format Validation
**Parent Use Case:** [UC-3.2: Create Person](USE-CASES.md#uc-32-create-person)

**Objective:** Verify email format validation.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form:
   - firstName: `Jane`
   - lastName: `Doe`
   - email: `invalid-email`
5. Click Save button

**Expected:**
- [ ] Error message about invalid email format displayed
- [ ] User remains on create form

---

### TC-3.08: Person Create Duplicate Email Prevention
**Parent Use Case:** [UC-3.2: Create Person](USE-CASES.md#uc-32-create-person)

**Objective:** Verify duplicate email is rejected.

**Precondition:** Person with email `admin@example.com` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form:
   - firstName: `Another`
   - lastName: `Admin`
   - email: `admin@example.com`
5. Click Save button

**Expected:**
- [ ] Error message "Email already registered" displayed
- [ ] User remains on create form

---

### TC-3.09: Person Edit Form Display
**Parent Use Case:** [UC-3.3: Edit Person](USE-CASES.md#uc-33-edit-person)

**Objective:** Verify edit form displays with pre-populated data.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for an existing entry

**Expected:**
- [ ] Edit form displayed
- [ ] All fields pre-populated with current values
- [ ] Gender dropdown shows current selection
- [ ] Audit fields displayed (read-only)
- [ ] Save and Cancel buttons visible

---

### TC-3.10: Person Edit Success
**Parent Use Case:** [UC-3.3: Edit Person](USE-CASES.md#uc-33-edit-person)

**Objective:** Verify successful person update.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for an existing entry
4. Modify lastName to `Updated`
5. Click Save button

**Expected:**
- [ ] Redirected to persons list
- [ ] Updated entry reflects changes
- [ ] Audit fields updated (updatedBy, updatedAt)

---

### TC-3.11: Person Edit Email Uniqueness
**Parent Use Case:** [UC-3.3: Edit Person](USE-CASES.md#uc-33-edit-person)

**Objective:** Verify email uniqueness on edit.

**Precondition:** Two person records exist with different emails

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for first person
4. Change email to match second person's email
5. Click Save button

**Expected:**
- [ ] Error message "Email already registered" displayed
- [ ] User remains on edit form

---

### TC-3.12: Person Edit Cancel
**Parent Use Case:** [UC-3.3: Edit Person](USE-CASES.md#uc-33-edit-person)

**Objective:** Verify cancel discards changes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for an existing entry
4. Modify fields
5. Click Cancel button

**Expected:**
- [ ] Redirected to persons list
- [ ] Original values unchanged

---

### TC-3.13: Person Delete Confirmation
**Parent Use Case:** [UC-3.4: Delete Person](USE-CASES.md#uc-34-delete-person)

**Objective:** Verify delete confirmation dialog appears.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Delete button for an existing entry

**Expected:**
- [ ] Confirmation dialog appears
- [ ] Dialog asks "Are you sure you want to delete this person?"
- [ ] Confirm and Cancel options available

---

### TC-3.14: Person Delete Success
**Parent Use Case:** [UC-3.4: Delete Person](USE-CASES.md#uc-34-delete-person)

**Objective:** Verify successful person deletion.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Delete button for an entry
4. Confirm deletion

**Expected:**
- [ ] Entry removed from list
- [ ] List updated immediately

---

### TC-3.15: Person Delete Cancel
**Parent Use Case:** [UC-3.4: Delete Person](USE-CASES.md#uc-34-delete-person)

**Objective:** Verify cancel preserves record.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Delete button for an entry
4. Click Cancel in confirmation dialog

**Expected:**
- [ ] Dialog closes
- [ ] Entry still exists in list

---

### TC-3.16: Persons Filter by Name
**Parent Use Case:** [UC-3.5: Filter Persons](USE-CASES.md#uc-35-filter-persons)

**Objective:** Verify filtering by name works.

**Precondition:** Multiple person records exist

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Enter filter text matching a lastName
4. Click Filter button

**Expected:**
- [ ] Only matching records displayed
- [ ] Filter criteria visible in filter field

---

### TC-3.17: Persons Filter No Results
**Parent Use Case:** [UC-3.5: Filter Persons](USE-CASES.md#uc-35-filter-persons)

**Objective:** Verify filter with no matches shows appropriate message.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Enter filter text that matches no records: `ZZZZNONEXISTENT`
4. Click Filter button

**Expected:**
- [ ] Message "No persons match the filter criteria" displayed
- [ ] Filter criteria still visible

---

### TC-3.18: Persons Filter Clear
**Parent Use Case:** [UC-3.5: Filter Persons](USE-CASES.md#uc-35-filter-persons)

**Objective:** Verify clearing filter shows all records.

**Precondition:** Filter is currently applied

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons` with active filter
3. Click Clear button

**Expected:**
- [ ] All records displayed
- [ ] Filter field cleared

---

### TC-3.19: Persons Filter Persistence
**Parent Use Case:** [UC-3.5: Filter Persons](USE-CASES.md#uc-35-filter-persons)

**Objective:** Verify filter persists during session.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Apply filter
4. Navigate away to another page
5. Return to `/persons`

**Expected:**
- [ ] Previous filter still applied
- [ ] Filtered results displayed

---

### TC-3.20: Persons Access Requires Authentication
**Parent Use Case:** [UC-3.1: View Persons List](USE-CASES.md#uc-31-view-persons-list)

**Objective:** Verify unauthenticated users cannot access persons page.

**Steps:**
1. Ensure not logged in
2. Navigate to `/persons`

**Expected:**
- [ ] Redirected to `/login` page
- [ ] Cannot access persons list without authentication

---

## Test Execution Summary

### TC-1: Authentication Tests

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-1.01 | Signup Page UI | UC-1.1 | [ ] | |
| TC-1.02 | Signup Success | UC-1.2 | [ ] | |
| TC-1.03 | Signup Email Required | UC-1.2 | [ ] | |
| TC-1.04 | Signup Password Min Length | UC-1.2 | [ ] | |
| TC-1.05 | Signup Duplicate Email | UC-1.2 | [ ] | |
| TC-1.06 | Signup Email Case Insensitivity | UC-1.2 | [ ] | |
| TC-1.07 | Login Page UI | UC-1.3 | [ ] | |
| TC-1.08 | Login Success | UC-1.4 | [ ] | |
| TC-1.09 | Login Invalid Password | UC-1.4 | [ ] | |
| TC-1.10 | Login Invalid Email | UC-1.4 | [ ] | |
| TC-1.11 | Login Email Case Insensitivity | UC-1.4 | [ ] | |
| TC-1.12 | Login Empty Credentials | UC-1.4 | [ ] | |
| TC-1.13 | Logout Flow | UC-1.5 | [ ] | |
| TC-1.14 | Protected Route Unauth | UC-1.6 | [ ] | |
| TC-1.15 | Protected Route Auth | UC-1.4 | [ ] | |
| TC-1.16 | Navigation Auth Pages | UC-1.1, UC-1.3 | [ ] | |
| TC-1.17 | User Enumeration Prevention | UC-1.4 | [ ] | |
| TC-1.18 | Form Input Sanitization | UC-1.2 | [ ] | |

### TC-2: Gender Tests

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-2.01 | Gender Page UI | UC-2.1 | [ ] | |
| TC-2.02 | Gender List Display | UC-2.1 | [ ] | |
| TC-2.03 | Gender List Empty | UC-2.1 | [ ] | |
| TC-2.04 | Gender Create Form | UC-2.2 | [ ] | |
| TC-2.05 | Gender Create Success | UC-2.2 | [ ] | |
| TC-2.06 | Gender Code Uppercase | UC-2.2 | [ ] | |
| TC-2.07 | Gender Duplicate Code | UC-2.2 | [ ] | |
| TC-2.08 | Gender Duplicate Desc | UC-2.2 | [ ] | |
| TC-2.09 | Gender Code Max Length | UC-2.2 | [ ] | |
| TC-2.10 | Gender Edit Form | UC-2.3 | [ ] | |
| TC-2.11 | Gender Edit Success | UC-2.3 | [ ] | |
| TC-2.12 | Gender Edit Cancel | UC-2.3 | [ ] | |
| TC-2.13 | Gender Delete Confirm | UC-2.4 | [ ] | |
| TC-2.14 | Gender Delete Success | UC-2.4 | [ ] | |
| TC-2.15 | Gender Delete Cancel | UC-2.4 | [ ] | |
| TC-2.16 | Gender Admin Role | UC-2.1 | [ ] | |

### TC-3: Persons Tests

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-3.01 | Persons Page UI | UC-3.1 | [ ] | |
| TC-3.02 | Persons List Display | UC-3.1 | [ ] | |
| TC-3.03 | Persons List Empty | UC-3.1 | [ ] | |
| TC-3.04 | Person Create Form | UC-3.2 | [ ] | |
| TC-3.05 | Person Create Success | UC-3.2 | [ ] | |
| TC-3.06 | Person Email Required | UC-3.2 | [ ] | |
| TC-3.07 | Person Email Format | UC-3.2 | [ ] | |
| TC-3.08 | Person Duplicate Email | UC-3.2 | [ ] | |
| TC-3.09 | Person Edit Form | UC-3.3 | [ ] | |
| TC-3.10 | Person Edit Success | UC-3.3 | [ ] | |
| TC-3.11 | Person Edit Email Unique | UC-3.3 | [ ] | |
| TC-3.12 | Person Edit Cancel | UC-3.3 | [ ] | |
| TC-3.13 | Person Delete Confirm | UC-3.4 | [ ] | |
| TC-3.14 | Person Delete Success | UC-3.4 | [ ] | |
| TC-3.15 | Person Delete Cancel | UC-3.4 | [ ] | |
| TC-3.16 | Persons Filter Name | UC-3.5 | [ ] | |
| TC-3.17 | Persons Filter No Results | UC-3.5 | [ ] | |
| TC-3.18 | Persons Filter Clear | UC-3.5 | [ ] | |
| TC-3.19 | Persons Filter Persist | UC-3.5 | [ ] | |
| TC-3.20 | Persons Auth Required | UC-3.1 | [ ] | |

---

## Traceability Matrix

| Use Case | Test Cases |
|----------|------------|
| UC-1.1: Display Signup Page | TC-1.01, TC-1.16 |
| UC-1.2: Register New User | TC-1.02, TC-1.03, TC-1.04, TC-1.05, TC-1.06, TC-1.18 |
| UC-1.3: Display Login Page | TC-1.07, TC-1.16 |
| UC-1.4: Authenticate User | TC-1.08, TC-1.09, TC-1.10, TC-1.11, TC-1.12, TC-1.15, TC-1.17 |
| UC-1.5: Logout User | TC-1.13 |
| UC-1.6: Access Protected Route | TC-1.14 |
| UC-2.1: View Gender List | TC-2.01, TC-2.02, TC-2.03, TC-2.16 |
| UC-2.2: Create Gender | TC-2.04, TC-2.05, TC-2.06, TC-2.07, TC-2.08, TC-2.09 |
| UC-2.3: Edit Gender | TC-2.10, TC-2.11, TC-2.12 |
| UC-2.4: Delete Gender | TC-2.13, TC-2.14, TC-2.15 |
| UC-3.1: View Persons List | TC-3.01, TC-3.02, TC-3.03, TC-3.20 |
| UC-3.2: Create Person | TC-3.04, TC-3.05, TC-3.06, TC-3.07, TC-3.08 |
| UC-3.3: Edit Person | TC-3.09, TC-3.10, TC-3.11, TC-3.12 |
| UC-3.4: Delete Person | TC-3.13, TC-3.14, TC-3.15 |
| UC-3.5: Filter Persons | TC-3.16, TC-3.17, TC-3.18, TC-3.19 |

---

## Chrome DevTools MCP Commands Reference

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
