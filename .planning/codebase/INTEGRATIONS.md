# External Integrations

**Analysis Date:** 2026-02-14

## APIs & External Services

**Frontend Libraries (CDN):**
- UIkit 3.25.4 - CSS framework loaded from CDN: `https://cdn.jsdelivr.net/npm/uikit@3.25.4/dist/css/uikit.min.css`
- HTMX 2.0.8 - AJAX library loaded from CDN: `https://cdn.jsdelivr.net/npm/htmx.org@2.0.8/dist/htmx.min.js`
- Integrity checking enabled on HTMX script for security

**Internal REST APIs:**
- Form-based HTMX endpoints for dynamic UI updates
- No external third-party API integrations detected

## Data Storage

**Databases:**
- PostgreSQL 17.7 (primary)
  - Connection: Via `quarkus.datasource.db-kind=postgresql` and standard datasource properties
  - Client: JDBC driver (`quarkus-jdbc-postgresql`)
  - ORM: Hibernate ORM with Panache
  - Schema management: Flyway migrations (auto-run at startup)

**File Storage:**
- Local filesystem only for static assets
- Static files served from default Quarkus location (`/img/`, `/css/`, `/js/`, `/style.css`)

**Caching:**
- None detected - using default Quarkus request/response handling

## Authentication & Identity

**Auth Provider:**
- Custom/Built-in - Form-based authentication via JPA
  - Implementation: Quarkus Security JPA with UserLogin entity
  - User entity location: `src/main/java/io/archton/scaffold/entity/UserLogin.java`
  - Password hashing: BCrypt with cost factor 12 (via `io.quarkus.elytron.security.common.BcryptUtil`)
  - Service: `src/main/java/io/archton/scaffold/service/UserLoginService.java`

**Authentication Flow:**
- Form authentication enabled via `quarkus.http.auth.form.enabled=true`
- Login page: `/?login=true`
- Session cookie: `quarkus-credential` (HttpOnly, SameSite=Strict)
- Session timeout: PT30M (30 minutes)
- New cookie interval: PT1M (1 minute for session refresh)

**Authorization:**
- Role-based access control via JPA roles
- Authenticated routes: `/dashboard/*,/api/*,/persons,/persons/*,/profile/*,/graph,/graph/*`
- Admin routes: `/admin/*,/genders/*,/titles/*,/relationships/*` (requires admin role)
- Public routes: `/,/login,/signup,/logout,/css/*,/js/*,/images/*,/webjars/*,/img/*,/style.css`

## Monitoring & Observability

**Error Tracking:**
- None configured - relies on application logging

**Logs:**
- JBoss LogManager via `java.util.logging.manager=org.jboss.logmanager.LogManager`
- Console logging enabled with `quarkus.log.console.darken=1`
- Custom banner displayed on startup: `banner.txt`

**Health Checks:**
- SmallRye Health endpoints available at `/q/health`
- Dev UI available at `/q/dev/`

## CI/CD & Deployment

**Hosting:**
- Deployment agnostic - runs on any Java 17 compatible environment
- Containerization ready (Dockerfile templates available)
- Kubernetes-native via Quarkus (GraalVM native images)

**CI Pipeline:**
- None detected in repository
- Maven-based build with Surefire (unit) and Failsafe (integration) test plugins

## Environment Configuration

**Required environment variables (at minimum):**
- Database connection parameters (datasource.jdbc-url, datasource.username, datasource.password)
- Or use default PostgreSQL connection string

**Optional configuration variables:**
- `quarkus.datasource.db-kind` - Database kind (default: postgresql)
- `quarkus.http.port` - HTTP port (default: 9080)
- Custom app-specific properties: `app.security.password.min-length=15`, `app.security.password.max-length=128`

**Secrets location:**
- Not detected - uses standard Quarkus environment variable substitution
- Credentials should be provided via environment variables at runtime

## Webhooks & Callbacks

**Incoming:**
- None detected - application is HTMX server-rendered, no webhook receivers

**Outgoing:**
- None detected - no external service callbacks

## Data Flow Patterns

**Request Flow:**
1. Client sends HTMX request (browser form/button with `hx-*` attributes)
2. Resource class (e.g., `AuthResource`, `PersonResource`) receives request
3. Service layer (e.g., `UserLoginService`) validates and processes data
4. Repository layer (Panache) performs database operations
5. Qute template renders response HTML
6. HTMX swaps response into target element

**Database Access Pattern:**
- Repository classes implement `PanacheRepository<T>` for active record pattern
- Example: `src/main/java/io/archton/scaffold/repository/PersonRepository.java`
- Repositories: `PersonRepository`, `UserLoginRepository`, `TitleRepository`, `GenderRepository`, `RelationshipRepository`, `PersonRelationshipRepository`
- Flyway migrations in: `src/main/resources/db/migration/` (V1.0.0 through V1.6.1)

## Security Implementation

**Password Policy (NIST SP 800-63B-4 Compliant):**
- Minimum length: 15 characters (`app.security.password.min-length=15`)
- Maximum length: 128 characters (`app.security.password.max-length=128`)
- Hash algorithm: BCrypt with cost factor 12 (resistant to brute-force attacks)

**Session Security:**
- HttpOnly cookies prevent JavaScript access
- SameSite=Strict prevents cross-site request forgery (CSRF)
- Session cookie refresh every 1 minute via `quarkus.http.auth.form.new-cookie-interval=PT1M`

---

*Integration audit: 2026-02-14*
