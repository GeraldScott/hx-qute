# Technical Specification: Phased Authentication Implementation

## 1. Overview

### 1.1 Purpose

This specification defines a **phased approach** to user authentication in the `hx-qute` application, prioritizing simplicity and maximum leverage of Quarkus extensions out-of-the-box. 

### 1.2 Design Philosophy

| Principle | Application |
|-----------|-------------|
| YAGNI | Build only what's needed now; defer complexity |
| Framework-First | Use Quarkus extensions before writing custom code |
| Incremental Complexity | Each phase adds capability without rewriting |
| Compliance by Default | NIST/OWASP requirements built into Phase 1 |

### 1.3 Phase Summary

| Phase | Focus | Quarkus Extension | Complexity |
|-------|-------|-------------------|------------|
| **Phase 1** | Form-based auth with single entity | `quarkus-security-jpa` | Minimal |
| **Phase 2** | Passkeys/WebAuthn MFA | `quarkus-security-webauthn` | Incremental |
| **Phase 3** | OIDC with external IdP | `quarkus-oidc` | Delegated |

### 1.4 References

| Document | Description |
|----------|-------------|
| [NIST SP 800-63B-4](https://csrc.nist.gov/pubs/sp/800/63/b/4/final) | Digital Identity Guidelines: Authentication (July 2025) |
| [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html) | Authentication best practices |
| [Quarkus Security JPA Guide](https://quarkus.io/guides/security-jpa) | JPA-based identity provider |
| [Quarkus WebAuthn Guide](https://quarkus.io/guides/security-webauthn) | Passkey/FIDO2 authentication |
| [Quarkus OIDC Guide](https://quarkus.io/guides/security-oidc-code-flow-authentication) | OpenID Connect integration |

---

## 2. Phase 1: Simple Form Authentication

### 2.1 Goals

- Functional user registration and login
- NIST SP 800-63B-4 compliant password policy
- Maximum use of Quarkus defaults
- Single entity design (UserLogin only, no Person split)
- ~50 lines of custom code

### 2.2 Dependencies

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security-jpa</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-orm-panache</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>
```

### 2.3 Database Schema

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

**Notes:**
- Single table design - profile fields included directly
- `email` is the login identifier (no separate username)
- `password` stored as BCrypt hash in MCF format
- No MFA fields - deferred to Phase 2
- No lockout fields - handled by service layer

### 2.4 UserLogin Entity

```java
package io.archton.htmx.entity;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.PasswordType;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Entity
@Table(name = "user_login")
@UserDefinition
public class UserLogin extends PanacheEntity {

    @Username
    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    public String email;

    @Password(PasswordType.MCF)
    @NotBlank
    @Column(nullable = false)
    public String password;

    @Roles
    @Column(nullable = false)
    public String role = "user";

    // Optional profile fields
    @Size(max = 100)
    @Column(name = "first_name")
    public String firstName;

    @Size(max = 100)
    @Column(name = "last_name")
    public String lastName;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(nullable = false)
    public boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        normalizeEmail();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        normalizeEmail();
    }

    private void normalizeEmail() {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }

    // --- Factory Methods ---

    /**
     * Create a new user with hashed password.
     * Password is hashed using BCrypt with cost factor 12.
     */
    public static UserLogin create(String email, String plainPassword, String role) {
        UserLogin user = new UserLogin();
        user.email = email.toLowerCase().trim();
        user.password = BcryptUtil.bcryptHash(plainPassword, 12);
        user.role = role;
        return user;
    }

    // --- Finder Methods ---

    public static UserLogin findByEmail(String email) {
        return find("email", email.toLowerCase().trim()).firstResult();
    }

    public static boolean emailExists(String email) {
        return count("email", email.toLowerCase().trim()) > 0;
    }

    // --- Display Methods ---

    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email;
    }
}
```

### 2.5 Password Validation Service

NIST SP 800-63B-4 compliant password validation:

```java
package io.archton.htmx.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PasswordValidator {

    @ConfigProperty(name = "app.security.password.min-length", defaultValue = "15")
    int minLength;

    @ConfigProperty(name = "app.security.password.max-length", defaultValue = "128")
    int maxLength;

    /**
     * Validate password against NIST SP 800-63B-4 requirements.
     *
     * @param password the plain text password to validate
     * @return list of validation errors (empty if valid)
     */
    public List<String> validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password is required");
            return errors;
        }

        // NIST 800-63B-4: Minimum 15 characters when password-only auth
        if (password.length() < minLength) {
            errors.add("Password must be at least " + minLength + " characters");
        }

        // NIST 800-63B-4: Accept at least 64 characters (we allow 128)
        if (password.length() > maxLength) {
            errors.add("Password must be " + maxLength + " characters or less");
        }

        // NIST 800-63B-4: No truncation - ensure full password is used
        // (This is enforced by not truncating in storage)

        // NIST 800-63B-4: No composition rules required
        // (Intentionally NOT checking for special chars, uppercase, etc.)

        return errors;
    }

    /**
     * Check if password is valid.
     */
    public boolean isValid(String password) {
        return validate(password).isEmpty();
    }
}
```

### 2.6 Application Configuration

```properties
# =============================================================================
# Phase 1: Form-Based Authentication Configuration
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

quarkus.http.auth.permission.admin.paths=/admin/*
quarkus.http.auth.permission.admin.policy=admin
quarkus.http.auth.policy.admin.roles-allowed=admin

quarkus.http.auth.permission.public.paths=/,/login,/signup,/logout,/css/*,/js/*,/images/*,/webjars/*
quarkus.http.auth.permission.public.policy=permit

# --- Password Policy (NIST SP 800-63B-4) ---
app.security.password.min-length=15
app.security.password.max-length=128

# --- Session Security ---
quarkus.http.auth.form.new-cookie-interval=PT1M
quarkus.http.same-site-cookie.quarkus-credential=strict
```

### 2.7 Authentication Endpoints

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/signup` | GET | Display registration form | No |
| `/signup` | POST | Create new account | No |
| `/login` | GET | Display login form | No |
| `/j_security_check` | POST | Authenticate (Quarkus form auth) | No |
| `/logout` | GET | Terminate session, show confirmation | Yes |

### 2.8 Security Requirements Summary

| Requirement | NIST 800-63B-4 | Implementation |
|-------------|----------------|----------------|
| Minimum password length | 15 chars (password-only) | ✅ Configurable, default 15 |
| Maximum password length | Accept 64+ chars | ✅ 128 chars |
| Password truncation | Prohibited | ✅ Full password stored |
| Composition rules | Not required | ✅ None enforced |
| Password hashing | Approved algorithm | ✅ BCrypt, cost 12 |
| Session timeout | Risk-based | ✅ 30 min idle |
| Secure cookies | HttpOnly, SameSite | ✅ Configured |

### 2.9 What Phase 1 Does NOT Include

| Feature | Reason | Phase |
|---------|--------|-------|
| Account lockout | Defer to service layer or Phase 3 IdP | 2/3 |
| MFA/2FA | Defer to Phase 2 (WebAuthn) | 2 |
| Password breach checking (HIBP) | Enhancement, not MVP | 2 |
| Audit logging | Enhancement | 2 |
| Password reset | Enhancement | 2 |
| Email verification | Enhancement | 2 |

---

## 3. Phase 2: WebAuthn / Passkeys

### 3.1 Goals

- Add passwordless authentication option
- FIDO2/WebAuthn compliant (AAL3 per NIST)
- Passkey support for cross-device authentication
- Incremental addition to Phase 1

### 3.2 Why WebAuthn Over TOTP

Per NIST SP 800-63B-4 authentication assurance levels:

| Method | AAL Level | Phishing Resistant | Recommendation |
|--------|-----------|-------------------|----------------|
| FIDO2/WebAuthn | AAL3 | ✅ Yes | **Primary choice** |
| Hardware Keys | AAL3 | ✅ Yes | Supported by WebAuthn |
| TOTP Apps | AAL2 | ❌ No | Secondary option |
| SMS OTP | AAL1 | ❌ No | **Discouraged** |

### 3.3 Additional Dependencies

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security-webauthn</artifactId>
</dependency>
```

### 3.4 Database Schema Addition

```sql
-- Add to existing schema
CREATE TABLE webauthn_credential (
    credential_id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    public_key BYTEA NOT NULL,
    public_key_algorithm BIGINT NOT NULL,
    counter BIGINT NOT NULL DEFAULT 0,
    aaguid UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_webauthn_user
        FOREIGN KEY (user_id)
        REFERENCES user_login(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_webauthn_user_id ON webauthn_credential(user_id);
```

### 3.5 WebAuthn Credential Entity

```java
package io.archton.htmx.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord.RequiredPersistedData;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "webauthn_credential")
public class WebAuthnCredential extends PanacheEntityBase {

    @Id
    @Column(name = "credential_id")
    public String credentialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserLogin user;

    @Column(name = "public_key", nullable = false)
    public byte[] publicKey;

    @Column(name = "public_key_algorithm", nullable = false)
    public long publicKeyAlgorithm;

    @Column(nullable = false)
    public long counter;

    public UUID aaguid;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    // --- Factory Methods ---

    public static WebAuthnCredential from(WebAuthnCredentialRecord record, UserLogin user) {
        RequiredPersistedData data = record.getRequiredPersistedData();
        WebAuthnCredential credential = new WebAuthnCredential();
        credential.credentialId = data.credentialId();
        credential.user = user;
        credential.publicKey = data.publicKey();
        credential.publicKeyAlgorithm = data.publicKeyAlgorithm();
        credential.counter = data.counter();
        credential.aaguid = data.aaguid();
        return credential;
    }

    public WebAuthnCredentialRecord toRecord() {
        return WebAuthnCredentialRecord.fromRequiredPersistedData(
            new RequiredPersistedData(
                user.email,
                credentialId,
                aaguid,
                publicKey,
                publicKeyAlgorithm,
                counter
            )
        );
    }

    // --- Finder Methods ---

    public static List<WebAuthnCredential> findByUser(UserLogin user) {
        return list("user", user);
    }

    public static List<WebAuthnCredential> findByUserEmail(String email) {
        return list("user.email", email.toLowerCase().trim());
    }

    public static WebAuthnCredential findByCredentialId(String credentialId) {
        return findById(credentialId);
    }
}
```

### 3.6 WebAuthn User Provider

```java
package io.archton.htmx.security;

import io.archton.htmx.entity.UserLogin;
import io.archton.htmx.entity.WebAuthnCredential;
import io.quarkus.security.webauthn.WebAuthnCredentialRecord;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AppWebAuthnUserProvider implements WebAuthnUserProvider {

    @Override
    public Uni<List<WebAuthnCredentialRecord>> findByUsername(String username) {
        return Uni.createFrom().item(() -> {
            List<WebAuthnCredential> credentials = WebAuthnCredential.findByUserEmail(username);
            return credentials.stream()
                .map(WebAuthnCredential::toRecord)
                .collect(Collectors.toList());
        });
    }

    @Override
    public Uni<WebAuthnCredentialRecord> findByCredentialId(String credentialId) {
        return Uni.createFrom().item(() -> {
            WebAuthnCredential credential = WebAuthnCredential.findByCredentialId(credentialId);
            return credential != null ? credential.toRecord() : null;
        });
    }

    @Override
    @Transactional
    public Uni<Void> store(WebAuthnCredentialRecord record) {
        return Uni.createFrom().item(() -> {
            UserLogin user = UserLogin.findByEmail(record.getUsername());
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + record.getUsername());
            }
            WebAuthnCredential credential = WebAuthnCredential.from(record, user);
            credential.persist();
            return null;
        });
    }

    @Override
    @Transactional
    public Uni<Void> update(String credentialId, long counter) {
        return Uni.createFrom().item(() -> {
            WebAuthnCredential credential = WebAuthnCredential.findByCredentialId(credentialId);
            if (credential != null) {
                credential.counter = counter;
            }
            return null;
        });
    }

    @Override
    public Uni<Set<String>> findUsernames() {
        return Uni.createFrom().item(() -> {
            // Return users who have WebAuthn credentials registered
            return WebAuthnCredential.streamAll()
                .map(c -> ((WebAuthnCredential) c).user.email)
                .collect(Collectors.toSet());
        });
    }
}
```

### 3.7 Phase 2 Configuration Additions

```properties
# =============================================================================
# Phase 2: WebAuthn Configuration
# =============================================================================

# WebAuthn Relying Party settings
quarkus.webauthn.relying-party-name=HX-Qute Application
quarkus.webauthn.relying-party-id=${quarkus.http.host:localhost}

# Challenge timeout
quarkus.webauthn.challenge-timeout=PT5M

# Attestation preference (none = privacy-preserving)
quarkus.webauthn.attestation=none

# Authenticator selection
quarkus.webauthn.authenticator-attachment=platform
quarkus.webauthn.resident-key=preferred
quarkus.webauthn.user-verification=preferred
```

### 3.8 Phase 2 Additional Features

| Feature | Description | Priority |
|---------|-------------|----------|
| Account lockout service | Separate service, not in entity | High |
| Password breach check | HIBP API integration | Medium |
| Audit logging | CDI events for security actions | Medium |
| Password reset flow | Email-based reset tokens | Medium |
| Email verification | Verify email on signup | Low |

---

## 4. Phase 3: OIDC / External Identity Provider

### 4.1 Goals

- Delegate authentication to external IdP
- Eliminate password storage responsibility
- Enterprise SSO support
- MFA handled by IdP

### 4.2 When to Implement Phase 3

| Trigger | Description |
|---------|-------------|
| Enterprise customers | Require SSO integration |
| Compliance requirements | Need certified IdP |
| Multi-application environment | Shared identity across apps |
| Advanced MFA needs | Hardware tokens, biometrics |

### 4.3 Dependencies

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-oidc</artifactId>
</dependency>
```

### 4.4 Simplified UserLogin Entity (Phase 3)

When using OIDC, the UserLogin entity becomes a local profile store:

```java
@Entity
@Table(name = "user_login")
public class UserLogin extends PanacheEntity {

    // OIDC subject identifier (from IdP)
    @Column(name = "oidc_subject", unique = true)
    public String oidcSubject;

    // Local email (synced from IdP claims)
    @Email
    @Column(nullable = false, unique = true)
    public String email;

    // Role can come from IdP claims or be locally assigned
    public String role = "user";

    // Profile fields
    public String firstName;
    public String lastName;

    // --- No password field needed! ---

    public static UserLogin findByOidcSubject(String subject) {
        return find("oidcSubject", subject).firstResult();
    }

    public static UserLogin findByEmail(String email) {
        return find("email", email.toLowerCase().trim()).firstResult();
    }
}
```

### 4.5 OIDC Configuration

```properties
# =============================================================================
# Phase 3: OIDC Configuration
# =============================================================================

# --- Development (Keycloak Dev Services) ---
# Quarkus automatically starts Keycloak in dev mode
quarkus.keycloak.devservices.realm-path=dev-realm.json

# --- Production (External Keycloak) ---
%prod.quarkus.oidc.auth-server-url=https://keycloak.example.com/realms/myapp
%prod.quarkus.oidc.client-id=hx-qute-app
%prod.quarkus.oidc.credentials.secret=${OIDC_CLIENT_SECRET}
%prod.quarkus.oidc.application-type=web-app

# Token/session settings
quarkus.oidc.authentication.session-age-extension=PT30M
quarkus.oidc.authentication.cookie-same-site=strict

# Roles from token claims
quarkus.oidc.roles.source=accesstoken
quarkus.oidc.roles.role-claim-path=realm_access/roles

# --- Alternative: Auth0 ---
# quarkus.oidc.provider=auth0
# quarkus.oidc.auth-server-url=https://your-tenant.auth0.com
# quarkus.oidc.client-id=${AUTH0_CLIENT_ID}
# quarkus.oidc.credentials.secret=${AUTH0_CLIENT_SECRET}

# --- Alternative: Google ---
# quarkus.oidc.provider=google
# quarkus.oidc.client-id=${GOOGLE_CLIENT_ID}
# quarkus.oidc.credentials.secret=${GOOGLE_CLIENT_SECRET}
```

### 4.6 Security Identity Augmentor

Sync OIDC claims to local UserLogin entity:

```java
package io.archton.htmx.security;

import io.archton.htmx.entity.UserLogin;
import io.quarkus.oidc.UserInfo;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class OidcUserSyncAugmentor implements SecurityIdentityAugmentor {

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity,
                                          AuthenticationRequestContext context) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }

        return context.runBlocking(() -> syncUser(identity));
    }

    @ActivateRequestContext
    @Transactional
    SecurityIdentity syncUser(SecurityIdentity identity) {
        String subject = identity.getPrincipal().getName();

        // Find or create local user
        UserLogin user = UserLogin.findByOidcSubject(subject);
        if (user == null) {
            user = new UserLogin();
            user.oidcSubject = subject;

            // Extract claims from IdToken or UserInfo
            JsonWebToken token = identity.getCredential(JsonWebToken.class);
            if (token != null) {
                user.email = token.getClaim("email");
                user.firstName = token.getClaim("given_name");
                user.lastName = token.getClaim("family_name");
            }

            user.persist();
        }

        // Add local user ID to identity attributes
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
        builder.addAttribute("userId", user.id);
        builder.addAttribute("user", user);

        return builder.build();
    }
}
```

### 4.7 Comparison: Form Auth vs OIDC

| Concern | Phase 1 (Form Auth) | Phase 3 (OIDC) |
|---------|---------------------|----------------|
| Password storage | Your responsibility | IdP handles |
| Password policy | You enforce | IdP enforces |
| MFA | Phase 2 (WebAuthn) | IdP provides |
| Account lockout | You implement | IdP provides |
| Password reset | You implement | IdP provides |
| Social login | Not available | Built-in |
| SSO | Not available | Built-in |
| Audit logs | You build | IdP provides |
| Compliance | Your responsibility | IdP certified |

---

## 5. Migration Path

### 5.1 Phase 1 to Phase 2

No breaking changes. Add WebAuthn as additional authentication method:

1. Add `quarkus-security-webauthn` dependency
2. Create `webauthn_credential` table
3. Add `WebAuthnCredential` entity
4. Implement `WebAuthnUserProvider`
5. Add WebAuthn UI components

Users can continue using password auth while optionally registering passkeys.

### 5.2 Phase 2 to Phase 3

Requires migration strategy:

| Strategy | Description | Complexity |
|----------|-------------|------------|
| **Parallel Auth** | Support both form and OIDC temporarily | Medium |
| **Forced Migration** | Require all users to re-register via OIDC | Low |
| **Account Linking** | Link existing accounts by email verification | High |

**Recommended: Parallel Auth**
1. Add `oidc_subject` column to `user_login`
2. Configure OIDC alongside form auth
3. On OIDC login, link by email match
4. Deprecate form auth after transition period

---

## 6. Testing Strategy

### 6.1 Phase 1 Tests

```java
@QuarkusTest
public class AuthenticationTest {

    @Test
    void shouldAccessPublicEndpointAnonymously() {
        given()
            .when().get("/")
            .then().statusCode(200);
    }

    @Test
    void shouldRedirectToLoginForProtectedEndpoint() {
        given()
            .redirects().follow(false)
            .when().get("/dashboard")
            .then().statusCode(302)
            .header("Location", containsString("/login"));
    }

    @Test
    void shouldAuthenticateWithValidCredentials() {
        given()
            .formParam("j_username", "test@example.com")
            .formParam("j_password", "validpassword123")
            .when().post("/j_security_check")
            .then().statusCode(302)
            .header("Location", containsString("/dashboard"));
    }

    @Test
    void shouldRejectInvalidCredentials() {
        given()
            .formParam("j_username", "test@example.com")
            .formParam("j_password", "wrongpassword")
            .when().post("/j_security_check")
            .then().statusCode(302)
            .header("Location", containsString("error"));
    }
}
```

### 6.2 Phase 3 Tests with Dev Services

```java
@QuarkusTest
public class OidcAuthenticationTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @Test
    void shouldAuthenticateWithOidcToken() {
        String token = keycloakClient.getAccessToken("alice");

        given()
            .auth().oauth2(token)
            .when().get("/api/protected")
            .then().statusCode(200);
    }

    @Test
    void shouldRejectInvalidToken() {
        given()
            .auth().oauth2("invalid-token")
            .when().get("/api/protected")
            .then().statusCode(401);
    }
}
```

---

## 7. Implementation Checklist

### Phase 1 Checklist

- [ ] Create `user_login` table
- [ ] Implement `UserLogin` entity with `@UserDefinition`
- [ ] Implement `PasswordValidator` service
- [ ] Configure form authentication in `application.properties`
- [ ] Create signup page and endpoint
- [ ] Create login page
- [ ] Implement logout with session invalidation
- [ ] Configure route protection
- [ ] Write authentication tests

### Phase 2 Checklist

- [ ] Add `quarkus-security-webauthn` dependency
- [ ] Create `webauthn_credential` table
- [ ] Implement `WebAuthnCredential` entity
- [ ] Implement `WebAuthnUserProvider`
- [ ] Add WebAuthn registration UI
- [ ] Add WebAuthn login UI
- [ ] Write WebAuthn tests

### Phase 3 Checklist

- [ ] Add `quarkus-oidc` dependency
- [ ] Configure Keycloak/IdP connection
- [ ] Add `oidc_subject` column to `user_login`
- [ ] Implement `OidcUserSyncAugmentor`
- [ ] Configure Dev Services realm
- [ ] Implement account linking strategy
- [ ] Write OIDC tests
- [ ] Plan form auth deprecation

---

## Appendix A: Glossary

| Term | Definition |
|------|------------|
| AAL | Authenticator Assurance Level (NIST) |
| BCrypt | Password hashing algorithm with cost factor |
| FIDO2 | Fast Identity Online 2 standard |
| IdP | Identity Provider |
| MCF | Modular Crypt Format (password hash format) |
| OIDC | OpenID Connect |
| PKCE | Proof Key for Code Exchange |
| WebAuthn | Web Authentication API (W3C) |

## Appendix B: Reference Links

- [NIST SP 800-63B-4](https://csrc.nist.gov/pubs/sp/800/63/b/4/final)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [Quarkus Security JPA](https://quarkus.io/guides/security-jpa)
- [Quarkus WebAuthn](https://quarkus.io/guides/security-webauthn)
- [Quarkus OIDC Code Flow](https://quarkus.io/guides/security-oidc-code-flow-authentication)
- [Quarkus Keycloak Dev Services](https://quarkus.io/guides/security-openid-connect-dev-services)
