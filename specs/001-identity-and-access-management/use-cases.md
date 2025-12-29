# Actors

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
1. System displays signup form with fields: email, password
2. System displays link to login page

**Postcondition:** Signup form is displayed

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
5. System normalizes email to lowercase
6. System hashes password using BCrypt (cost factor 12)
7. System creates UserLogin record with email as username
8. System redirects to login page

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Email empty | Display "Email is required." error |
| 1b | Email invalid format | Display "Invalid email format." error |
| 2a | Password empty | Display "Password is required." error |
| 2b | Password < 15 chars | Display "Password must be at least 15 characters." error |
| 2c | Password > 128 chars | Display "Password must be 128 characters or less." error |
| 5a | Email exists (case-insensitive) | Display "Email already registered." error |

**Postcondition:** User account created; user redirected to login page

---

# US-001-02: User Login

## UC-001-02-01: Display Login Page

| Attribute | Value |
|-----------|-------|
| Actor | Guest |
| Precondition | User is not authenticated |
| Trigger | User navigates to `/login` |

**Main Flow:**
1. System displays login form with fields: email, password
2. System displays link to signup page

**Postcondition:** Login form is displayed

---

## UC-001-02-02: Authenticate User

| Attribute | Value |
|-----------|-------|
| Actor | Guest |
| Precondition | User is on login page |
| Trigger | User submits login form |

**Main Flow:**
1. User enters email
2. User enters password
3. User submits form to `/j_security_check`
4. System normalizes email to lowercase
5. System looks up UserLogin by email (email is the @Username)
6. System verifies password against stored BCrypt hash
7. System creates authenticated session
8. System redirects to home page with personalized greeting

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 5a | Email not found | Display "Invalid email or password" (generic) |
| 6a | Password incorrect | Display "Invalid email or password" (generic) |

**Postcondition:** User authenticated; session created; redirected to home page

---

## UC-001-02-03: Access Protected Route (Unauthenticated)

| Attribute | Value |
|-----------|-------|
| Actor | Guest |
| Precondition | User is not authenticated |
| Trigger | User navigates to protected route (e.g., `/persons`, `/gender`) |

**Main Flow:**
1. System detects unauthenticated request to protected resource
2. System redirects to `/login` page

**Postcondition:** User redirected to login page

---

# US-001-03: User Logout

## UC-001-03-01: Logout User

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is authenticated |
| Trigger | User clicks logout link or navigates to `/logout` |

**Main Flow:**
1. System invalidates user session
2. System clears authentication cookie
3. System displays logout confirmation page
4. Navigation updates to show unauthenticated options

**Postcondition:** Session terminated; user is unauthenticated
