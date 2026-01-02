# Use Cases for Feature 000: Foundation

This feature establishes the authentication infrastructure required for Identity and Access Management.

## Actors

| Actor | Description |
|-------|-------------|
| System Administrator | Responsible for system setup and configuration |
| System | The application runtime |

---

# US-000-01: Establish Authentication Infrastructure

## UC-000-01-01: Initialize User Account Storage

| Attribute | Value |
|-----------|-------|
| Actor | System Administrator |
| Precondition | Database server is running and accessible |
| Trigger | System Administrator starts the application for the first time |

**Main Flow:**
1. System Administrator starts the application
2. System executes Flyway migrations to create the `user_login` table
3. System creates unique constraint and index on email column
4. System confirms storage is ready for user registrations

**Postcondition:** System is ready to store user accounts with email, password, and role information

**Related Test Cases:** TC-000-01-001, TC-000-01-002, TC-000-01-012

---

## UC-000-01-02: Verify Secure Password Storage

| Attribute | Value |
|-----------|-------|
| Actor | System Administrator |
| Precondition | User account storage exists |
| Trigger | System Administrator verifies security configuration |

**Main Flow:**
1. System Administrator reviews security configuration
2. System confirms passwords are hashed using BCrypt (cost factor 12) via `UserLoginService`
3. System confirms email addresses are normalized for consistent login
4. System confirms `UserLogin` entity has proper Quarkus Security JPA annotations

**Security Guarantees:**
- Passwords are never stored in plain text
- Email addresses are case-insensitive for login (normalized to lowercase)
- User accounts include audit timestamps (createdAt, updatedAt)

**Implementation Components:**
- `UserLogin` entity with `@UserDefinition`, `@Username`, `@Password(PasswordType.MCF)`, `@Roles` annotations
- `UserLoginService` for user creation with BCrypt hashing
- `UserLoginRepository` for case-insensitive email lookups

**Postcondition:** System Administrator has confidence that user credentials are securely stored

**Related Test Cases:** TC-000-01-003, TC-000-01-004, TC-000-01-005, TC-000-01-006

---

## UC-000-01-03: Enforce Password Policy

| Attribute | Value |
|-----------|-------|
| Actor | System Administrator |
| Precondition | None |
| Trigger | System Administrator reviews password requirements |

**Main Flow:**
1. System Administrator reviews the password policy
2. System enforces minimum password length of 15 characters via `PasswordValidator` service
3. System accepts passwords up to 128 characters
4. System does not require special characters or mixed case (per NIST guidelines)

**Password Policy (NIST SP 800-63B-4 Compliant):**

| Requirement | Value |
|-------------|-------|
| Minimum length | 15 characters |
| Maximum length | 128 characters |
| Special characters | Not required |
| Uppercase/lowercase | Not required |
| Password expiration | Not enforced |

**Implementation:**
- `PasswordValidator` service with configurable min/max length
- Configuration via `app.security.password.min-length` and `app.security.password.max-length` properties

**Postcondition:** Password policy is documented and enforced for all new registrations

**Related Test Cases:** TC-000-01-007, TC-000-01-008, TC-000-01-009

---

## UC-000-01-04: Access System with Default Administrator

| Attribute | Value |
|-----------|-------|
| Actor | System Administrator |
| Precondition | Application has been started for the first time |
| Trigger | System Administrator needs initial system access |

**Main Flow:**
1. System Administrator navigates to the homepage
2. System Administrator clicks Login to open the login modal
3. System Administrator enters default credentials:
   - Email: `admin@example.com`
   - Password: `AdminPassword123`
4. System authenticates the administrator
5. System Administrator gains access to administrative functions

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 4a | First login | System Administrator should change the default password |

**Security Note:** The default administrator account is provided for initial setup only. The password should be changed immediately in a production environment.

**Postcondition:** System Administrator has access to the system and can manage users

**Related Test Cases:** TC-000-01-010, TC-000-01-011

---

# US-000-02: Create Application Shell and Landing Page

## UC-000-02-01: Display Base Layout

| Attribute | Value |
|-----------|-------|
| Actor | System |
| Precondition | Application is running |
| Trigger | User navigates to any page |

**Main Flow:**
1. System loads base.html template
2. System includes CDN resources (UIkit CSS/JS, HTMX)
3. System renders sidebar with logo and navigation
4. System renders main content area with page-specific content
5. System includes login modal markup (hidden by default)

**Responsive Behavior:**
- Large screens (≥960px): Fixed 320px sidebar visible
- Small screens (<960px): Sidebar hidden, hamburger menu in header

**Postcondition:** Page displays with consistent layout shell

**Related Test Cases:** TC-000-02-001, TC-000-02-002

---

## UC-000-02-02: Navigate Using Sidebar Menu

| Attribute | Value |
|-----------|-------|
| Actor | Guest, User, Administrator |
| Precondition | Page is loaded with base layout |
| Trigger | User clicks navigation link |

**Main Flow:**
1. User views navigation menu in sidebar
2. User clicks a menu item (Home, People, Graph, or Maintenance submenu)
3. System navigates to the selected page
4. System highlights the active navigation item

**Maintenance Dropdown:**
1. User clicks "Maintenance" to expand dropdown
2. System shows submenu: Gender, Title, Relationship
3. User clicks submenu item
4. System navigates to selected maintenance page

**Postcondition:** User is on the selected page with correct navigation highlighting

**Related Test Cases:** TC-000-02-003, TC-000-02-004

---

## UC-000-02-03: Display Landing Page

| Attribute | Value |
|-----------|-------|
| Actor | Guest, User, Administrator |
| Precondition | Application is running |
| Trigger | User navigates to homepage `/` |

**Main Flow:**
1. System loads IndexResource endpoint
2. System determines authentication state via SecurityIdentity
3. System determines development mode via LaunchMode
4. System renders landing page with technology showcase cards
5. If in development mode, system displays credentials alert

**Technology Cards Displayed:**
- Supersonic Quarkus
- Type-Safe Qute
- High-powered HTMX
- Robust PostgreSQL

**Development Mode Alert:**
- Visible when `devMode=true`
- Shows default credentials: `admin@example.com` / `AdminPassword123`

**Postcondition:** Landing page is displayed with appropriate content

**Related Test Cases:** TC-000-02-005, TC-000-02-006

---

## UC-000-02-04: Display Mobile Navigation

| Attribute | Value |
|-----------|-------|
| Actor | Guest, User, Administrator |
| Precondition | Page is loaded on mobile device (viewport < 960px) |
| Trigger | User taps hamburger menu icon |

**Main Flow:**
1. User sees hamburger menu icon in mobile header
2. User taps the hamburger icon
3. System opens offcanvas sidebar from left
4. User views same navigation as desktop sidebar
5. User taps a navigation item
6. System closes offcanvas and navigates to selected page

**Postcondition:** User navigates successfully on mobile device

**Related Test Cases:** TC-000-02-007

---
