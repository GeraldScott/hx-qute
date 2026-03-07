# External Integrations

**Analysis Date:** 2026-03-07

## APIs & External Services

**CDN Dependencies (loaded in browser via `src/main/resources/templates/base.html`):**
- HTMX 2.0.8 - `https://cdn.jsdelivr.net/npm/htmx.org@2.0.8/dist/htmx.min.js` (with SRI hash)
- UIkit 3.25.4 CSS - `https://cdn.jsdelivr.net/npm/uikit@3.25.4/dist/css/uikit.min.css`
- UIkit 3.25.4 JS - `https://cdn.jsdelivr.net/npm/uikit@3.25.4/dist/js/uikit.min.js`
- UIkit Icons 3.25.4 - `https://cdn.jsdelivr.net/npm/uikit@3.25.4/dist/js/uikit-icons.min.js`
- D3.js v7 - `https://cdn.jsdelivr.net/npm/d3@7` (loaded only on graph page via `src/main/resources/templates/GraphResource/graph.html`)

**Internal REST APIs (JSON):**
- `/graph/data` - Returns JSON graph data for D3.js visualization (consumed by `src/main/resources/META-INF/resources/js/graph.js`)
- `/graph/person/{id}` - Returns HTML fragment for person modal (consumed by `src/main/resources/META-INF/resources/js/graph.js`)

No external third-party API integrations (no Stripe, AWS, email services, etc.).

## Data Storage

**Databases:**
- PostgreSQL 17.x
  - Connection: Configured via `quarkus.datasource.*` properties in `src/main/resources/application.properties`
  - Client: Hibernate ORM with Panache (Repository pattern)
  - Schema management: Flyway migrations in `src/main/resources/db/migration/`
  - Tables (from migrations): `gender`, `user_login`, `title`, `person`, `relationship`, `person_relationship`

**Repositories (Panache):**
- `src/main/java/io/archton/scaffold/repository/GenderRepository.java`
- `src/main/java/io/archton/scaffold/repository/TitleRepository.java`
- `src/main/java/io/archton/scaffold/repository/PersonRepository.java`
- `src/main/java/io/archton/scaffold/repository/RelationshipRepository.java`
- `src/main/java/io/archton/scaffold/repository/PersonRelationshipRepository.java`
- `src/main/java/io/archton/scaffold/repository/UserLoginRepository.java`

**File Storage:**
- Local filesystem only (static assets served from `src/main/resources/META-INF/resources/`)

**Caching:**
- None detected (no explicit caching configuration or dependencies)

## Authentication & Identity

**Auth Provider:**
- Custom, built-in via Quarkus Security JPA
  - Implementation: Form-based authentication using `j_security_check` servlet standard
  - User entity: `src/main/java/io/archton/scaffold/entity/UserLogin.java` with `@UserDefinition` annotation
  - Password storage: BCrypt (MCF format) via `@Password(PasswordType.MCF)`
  - Password validation: NIST SP 800-63B-4 compliant via `src/main/java/io/archton/scaffold/service/PasswordValidator.java`
  - Roles: Single string field (`user` default, `admin` for elevated access)
  - Session: Cookie-based (`quarkus-credential`), 30-minute timeout, HttpOnly, SameSite=strict

**Route Protection (configured in `src/main/resources/application.properties`):**
- Authenticated routes: `/dashboard/*`, `/api/*`, `/persons`, `/persons/*`, `/profile/*`, `/graph`, `/graph/*`
- Admin-only routes: `/admin/*`, `/genders/*`, `/titles/*`, `/relationships/*`
- Public routes: `/`, `/login`, `/signup`, `/logout`, static assets

## Monitoring & Observability

**Health Checks:**
- SmallRye Health via `quarkus-smallrye-health`
- Endpoint: `/q/health` (also used to trigger dev mode reload)

**Error Tracking:**
- None (no Sentry, Bugsnag, or similar)

**Logs:**
- JBoss LogManager (Quarkus default logging)
- Console darkening enabled: `quarkus.log.console.darken=1`
- No external log aggregation configured

## CI/CD & Deployment

**Hosting:**
- Not configured (no deployment target specified)

**CI Pipeline:**
- None detected (no `.github/workflows/`, no Jenkinsfile, no `.gitlab-ci.yml`)

**Container Support:**
- Four Dockerfiles provided in `src/main/docker/`:
  - `Dockerfile.jvm` - Standard JVM mode (UBI9 OpenJDK 21 base)
  - `Dockerfile.native` - GraalVM native binary
  - `Dockerfile.native-micro` - Minimal native image
  - `Dockerfile.legacy-jar` - Single legacy JAR
- No `docker-compose.yml` present

## Environment Configuration

**Required env vars / properties:**
- `quarkus.datasource.jdbc.url` - PostgreSQL connection URL
- `quarkus.datasource.username` - Database username
- `quarkus.datasource.password` - Database password
- Note: In dev mode, Quarkus Dev Services can auto-provision a PostgreSQL container

**Custom app properties:**
- `app.security.password.min-length` - Minimum password length (default: 15)
- `app.security.password.max-length` - Maximum password length (default: 128)

**Secrets location:**
- No `.env` files detected
- Database credentials expected via environment variables or `application.properties` profile overrides
- Default admin user password seeded via Flyway migration `V1.2.1__Insert_admin_user.sql`

## Webhooks & Callbacks

**Incoming:**
- None

**Outgoing:**
- None

---

*Integration audit: 2026-03-07*
