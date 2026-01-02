# Technical Specification for Feature 001: Identity and Access Management

This document describes the technical implementation details for user registration, authentication, and logout.

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

## Architecture Overview

The authentication system uses a **modal-based login** approach rather than separate login pages:

```
┌─────────────────────────────────────────────────────────────────┐
│                         base.html                                │
│              (contains login modal #login-modal)                 │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
              ▼               ▼               ▼
┌─────────────────────┐ ┌─────────────────┐ ┌───────────────────────┐
│   /signup (page)    │ │ /logout (page)  │ │ /j_security_check     │
│   AuthResource      │ │ AuthResource    │ │ (Quarkus form auth)   │
└─────────────────────┘ └─────────────────┘ └───────────────────────┘
              │                                         │
              └─────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────────┐
              │      UserLoginService         │
              │    UserLoginRepository        │
              │       UserLogin entity        │
              └───────────────────────────────┘
```

**Key Design Decisions:**
- Login is handled via a UIkit modal embedded in `base.html`
- No separate `/login` page - modal triggered by `?login=true` query parameter
- Form authentication handled by Quarkus Security JPA
- Signup and logout are separate pages

---

## Configuration Reference

### application.properties additions for Feature 001

```properties
# =============================================================================
# Feature 001: Form-Based Authentication Configuration
# =============================================================================

# --- Form Authentication ---
quarkus.http.auth.form.enabled=true
quarkus.http.auth.form.login-page=/?login=true
quarkus.http.auth.form.landing-page=/
quarkus.http.auth.form.error-page=/?login=true&error=true
quarkus.http.auth.form.timeout=PT30M
quarkus.http.auth.form.cookie-name=quarkus-credential
quarkus.http.auth.form.http-only-cookie=true

# --- Session Security ---
quarkus.http.auth.form.new-cookie-interval=PT1M

# --- Route Protection ---
quarkus.http.auth.permission.authenticated.paths=/dashboard/*,/api/*,/persons,/persons/*,/profile/*,/graph,/graph/*
quarkus.http.auth.permission.authenticated.policy=authenticated

quarkus.http.auth.permission.admin.paths=/admin/*,/genders/*,/titles/*,/relationships/*
quarkus.http.auth.permission.admin.policy=admin
quarkus.http.auth.policy.admin.roles-allowed=admin

quarkus.http.auth.permission.public.paths=/,/login,/signup,/logout,/css/*,/js/*,/images/*,/webjars/*,/img/*,/style.css
quarkus.http.auth.permission.public.policy=permit

# --- Password Policy (NIST SP 800-63B-4) ---
app.security.password.min-length=15
app.security.password.max-length=128
```

---

## Route Protection Summary

| Path Pattern | Policy | Roles | Description |
|--------------|--------|-------|-------------|
| `/`, `/signup`, `/logout` | Public | - | Auth pages and static resources |
| `/dashboard/*`, `/api/*` | Authenticated | Any | Dashboard and API endpoints |
| `/persons`, `/persons/*` | Authenticated | Any | Person management |
| `/profile/*` | Authenticated | Any | User profile |
| `/graph`, `/graph/*` | Authenticated | Any | Network graph visualization |
| `/admin/*` | Admin | admin | Admin panel |
| `/genders/*` | Admin | admin | Gender master data |
| `/titles/*` | Admin | admin | Title master data |
| `/relationships/*` | Admin | admin | Relationship master data |

---

## Endpoints

### AuthResource (`/`)

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/signup` | Display signup page | No |
| POST | `/signup` | Process registration | No |
| GET | `/logout` | Logout and show confirmation | No |

**Note:** There is no `/login` endpoint. Login is handled via modal + Quarkus form auth.

---

## Template Structure

### Login Modal (in base.html)

The login form is a UIkit modal embedded in the base template:

```html
<div id="login-modal" uk-modal>
    <form action="/j_security_check" method="POST">
        <input type="email" id="j_username" name="j_username" />
        <input type="password" id="j_password" name="j_password" />
        <button type="submit">Login</button>
    </form>
</div>
```

**Modal Trigger:** Navigation link with `uk-toggle` attribute:
```html
<a href="#login-modal" uk-toggle>Login</a>
```

**Query Parameter Handling:** JavaScript in base.html opens modal on `?login=true`:
```javascript
if (params.has('login')) {
    UIkit.modal('#login-modal').show();
    if (params.has('error')) {
        // Show error message
    }
}
```

### Signup Page (signup.html)

- Form posts to `/signup`
- Fields: email (id=`email`), password (id=`password`)
- "Login" link opens login modal (`href="#login-modal" uk-toggle`)
- Client-side email normalization via `onsubmit` handler

### Logout Page (logout.html)

- Displays success message: "You have been successfully logged out."
- "Go to Home" link to `/`
- "Login Again" link opens login modal (`href="#login-modal" uk-toggle`)

---

## Authentication Flow

### Login Flow

1. User clicks "Login" in navigation
2. Login modal opens (`#login-modal`)
3. User enters email and password
4. Form submits to `/j_security_check` (Quarkus handles this)
5. Quarkus looks up `UserLogin` by email (`@Username` field)
6. Quarkus verifies password against BCrypt hash (`@Password` field)
7. **Success:** Redirect to `/` (landing page) with session cookie
8. **Failure:** Redirect to `/?login=true&error=true`, modal shows error

### Registration Flow

1. User navigates to `/signup`
2. User fills email and password
3. Form submits to `POST /signup`
4. Server validates:
   - Email required and valid format
   - Password 15-128 characters
   - Email not already registered (case-insensitive)
5. Server creates `UserLogin` via `UserLoginService`
6. **Success:** Redirect to `/?login=true` (opens login modal)
7. **Failure:** Redirect to `/signup?error=<code>` with error message

### Logout Flow

1. User clicks "Logout ({userName})" in navigation
2. Browser navigates to `/logout`
3. Server destroys session and clears auth cookie
4. Logout confirmation page displayed

---

## Validation Rules

### Signup Form Validation

| Field | Rule | Error Code | Error Message |
|-------|------|------------|---------------|
| email | Required | `email_required` | "Email is required." |
| email | Valid format | `email_invalid` | "Invalid email format." |
| email | Unique (case-insensitive) | `email_exists` | "Email already registered." |
| password | Required | `password_required` | "Password is required." |
| password | Min 15 chars | `password_short` | "Password must be at least 15 characters." |
| password | Max 128 chars | `password_long` | "Password must be 128 characters or less." |

### Login Form Validation

| Condition | Error Message |
|-----------|---------------|
| Invalid email or password | "Invalid email or password." |

**Security:** Same error message for both invalid email and invalid password to prevent user enumeration.

---

## Email Normalization

Email addresses are normalized in two places:

1. **Client-side:** `onsubmit` handler transforms to lowercase and trims
2. **Server-side:** `UserLoginService.create()` normalizes before storage

---

## File Inventory

| File | Purpose |
|------|---------|
| `src/main/java/io/archton/scaffold/router/AuthResource.java` | Signup and logout endpoints |
| `src/main/resources/templates/AuthResource/signup.html` | Signup page template |
| `src/main/resources/templates/AuthResource/logout.html` | Logout confirmation template |
| `src/main/resources/templates/base.html` | Contains login modal |
| `src/main/resources/templates/fragments/navigation.html` | Login/logout navigation links |

---

## Security Considerations

### Session Security
- Cookie name: `quarkus-credential`
- HTTP-only cookie: `true` (prevents JavaScript access)
- Session timeout: 30 minutes
- New cookie interval: 1 minute (session fixation protection)

### Password Security
- BCrypt hashing with cost factor 12
- NIST SP 800-63B-4 compliant (15-128 chars, no composition rules)
- No password truncation

### User Enumeration Prevention
- Same error message for invalid email and invalid password
- No timing differences in authentication responses

---
