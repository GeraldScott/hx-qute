# Use Cases for Feature 001: Identity and Access Management

This feature provides user registration, authentication via modal, and logout functionality.

## Actors

| Actor | Description |
|-------|-------------|
| Guest | Unauthenticated visitor |
| User | Authenticated user with "user" role |
| Administrator | Authenticated user with "admin" role |

---

# US-001-01: User Registration

## UC-001-01-01: Display Signup Page

| Attribute | Value |
|-----------|-------|
| Actor | Guest |
| Precondition | User is not authenticated |
| Trigger | User navigates to `/signup` |

**Main Flow:**
1. System displays signup page with form fields: email, password
2. System displays link to open login modal

**Postcondition:** Signup form is displayed

**Related Test Cases:** TC-001-01-001

---

## UC-001-01-02: Register New User

| Attribute | Value |
|-----------|-------|
| Actor | Guest |
| Precondition | User is on signup page |
| Trigger | User submits signup form |

**Main Flow:**
1. User enters email (valid format)
2. User enters password (minimum 15 characters per NIST SP 800-63B-4)
3. User submits form
4. System validates all fields
5. System normalizes email to lowercase and trims whitespace
6. System creates user via `UserLoginService.create()` which hashes password using BCrypt (cost 12)
7. System redirects to `/?login=true` (homepage with login modal open)

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Email empty | Display "Email is required." error |
| 1b | Email invalid format | Display "Invalid email format." error |
| 2a | Password empty | Display "Password is required." error |
| 2b | Password < 15 chars | Display "Password must be at least 15 characters." error |
| 2c | Password > 128 chars | Display "Password must be 128 characters or less." error |
| 5a | Email exists (case-insensitive) | Display "Email already registered." error |

**Postcondition:** User account created; user redirected to homepage with login modal open

**Related Test Cases:** TC-001-01-002, TC-001-01-003, TC-001-01-004, TC-001-01-005, TC-001-01-006, TC-001-01-007

---

# US-001-02: User Login

## UC-001-02-01: Open Login Modal

| Attribute | Value |
|-----------|-------|
| Actor | Guest |
| Precondition | User is not authenticated |
| Trigger | User clicks "Login" link in navigation |

**Main Flow:**
1. User clicks "Login" link in navigation bar
2. Login modal (`#login-modal`) opens via UIkit toggle
3. System displays login form with fields: email, password
4. System displays link to signup page

**Note:** There is no separate `/login` page. Login is handled via a modal embedded in `base.html`.

**Postcondition:** Login modal is displayed

**Related Test Cases:** TC-001-02-001

---

## UC-001-02-02: Authenticate User

| Attribute | Value |
|-----------|-------|
| Actor | Guest |
| Precondition | Login modal is open |
| Trigger | User submits login form |

**Main Flow:**
1. User enters email
2. User enters password
3. Client-side JavaScript normalizes email (lowercase, trim)
4. User submits form to `/j_security_check`
5. Quarkus Security looks up `UserLogin` by email (`@Username`)
6. Quarkus Security verifies password against stored BCrypt hash (`@Password`)
7. System creates authenticated session with cookie
8. System redirects to homepage with personalized greeting

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 5a | Email not found | Redirect to `/?login=true&error=true`, display "Invalid email or password." |
| 6a | Password incorrect | Redirect to `/?login=true&error=true`, display "Invalid email or password." |

**Security Note:** Same error message for both cases to prevent user enumeration.

**Postcondition:** User authenticated; session created; redirected to homepage

**Related Test Cases:** TC-001-02-002, TC-001-02-003, TC-001-02-004, TC-001-02-005, TC-001-02-006, TC-001-02-007

---

## UC-001-02-03: Access Protected Route (Unauthenticated)

| Attribute | Value |
|-----------|-------|
| Actor | Guest |
| Precondition | User is not authenticated |
| Trigger | User navigates to protected route (e.g., `/persons`, `/graph`) |

**Main Flow:**
1. System detects unauthenticated request to protected resource
2. System redirects to `/?login=true` (homepage with login modal open)

**Protected Routes:**
- `/dashboard/*`, `/api/*` - requires authentication
- `/persons`, `/persons/*` - requires authentication
- `/profile/*` - requires authentication
- `/graph`, `/graph/*` - requires authentication
- `/admin/*` - requires admin role
- `/genders/*`, `/titles/*`, `/relationships/*` - requires admin role

**Postcondition:** User redirected to homepage with login modal open

**Related Test Cases:** TC-001-02-008

---

# US-001-03: User Logout

## UC-001-03-01: Logout User

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is authenticated |
| Trigger | User clicks "Logout ({userName})" link in navigation |

**Main Flow:**
1. User clicks "Logout ({userName})" link in navigation
2. Browser navigates to `/logout`
3. System destroys user session via `RoutingContext.session().destroy()`
4. System clears authentication cookie (`quarkus-credential`)
5. System displays logout confirmation page with message: "You have been successfully logged out."
6. Page shows "Go to Home" link and "Login Again" link (opens login modal)

**Postcondition:** Session terminated; user is unauthenticated; confirmation page displayed

**Related Test Cases:** TC-001-03-001

---
