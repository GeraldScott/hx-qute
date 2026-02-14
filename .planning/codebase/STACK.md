# Technology Stack

**Analysis Date:** 2026-02-14

## Languages

**Primary:**
- Java 17 LTS - Core application language, configured via `maven.compiler.release` property in `pom.xml`

## Runtime

**Environment:**
- Java 17 (minimum) via Eclipse Temurin or compatible JRE
- Quarkus 3.30.3 - Kubernetes-native Java framework for server-side rendering

**Package Manager:**
- Apache Maven 3.9.9 - Managed via Maven wrapper (`mvnw`)
- Lockfile: `pom.xml` (with dependency management via Quarkus BOM)

## Frameworks

**Core:**
- Quarkus 3.30.3 - REST and web framework
- Qute - Quarkus type-safe template engine for server-rendered HTML

**ORM/Database:**
- Hibernate ORM with Panache - Simplified ORM for data access
- Flyway - Database schema migration tool
- PostgreSQL JDBC Driver - PostgreSQL connectivity

**Validation:**
- Hibernate Validator - Bean validation (Jakarta Validation API)

**Security:**
- Quarkus Security JPA - Form-based authentication using database users

**Server:**
- Vert.x - Underlying async HTTP server (managed by Quarkus)

**Health/Monitoring:**
- Quarkus SmallRye Health - Health check endpoints (`/q/health`)

## Key Dependencies

**Critical:**
- `quarkus-rest-qute` - REST endpoints with Qute template integration
- `quarkus-rest-jsonb` - JSON serialization for REST responses
- `quarkus-hibernate-orm-panache` - Active Record pattern ORM
- `quarkus-security-jpa` - Authentication via JPA entities
- `quarkus-jdbc-postgresql` - PostgreSQL database driver
- `quarkus-flyway` - Database migrations on application startup
- `quarkus-junit5` - JUnit 5 testing support (test scope)

**Infrastructure:**
- `quarkus-arc` - CDI container for dependency injection

## Configuration

**Environment:**
- Configuration file: `src/main/resources/application.properties`
- Database connection via environment variables or configuration properties (datasource.db-kind, datasource.jdbc-url, etc.)
- Runtime port: 9080 (configured to avoid conflicts)

**Build:**
- Build configuration: `pom.xml`
- Maven compiler plugin: 3.14.0 (with parameters enabled for reflection)
- Surefire plugin: 3.5.3 (unit tests)
- Failsafe plugin: 3.5.3 (integration tests)

**Key Configurations:**
- `quarkus.http.port=9080` - HTTP server port
- `quarkus.datasource.db-kind=postgresql` - Database type
- `quarkus.flyway.migrate-at-start=true` - Auto-run migrations
- `quarkus.hibernate-orm.schema-management.strategy=none` - Schema managed by Flyway
- Form authentication enabled with cookie-based sessions
- SameSite=Strict cookies for CSRF protection

## Platform Requirements

**Development:**
- Java 17 LTS or higher
- Maven 3.9.9 (via wrapper)
- PostgreSQL 17.7 (recommended, can use Testcontainers with Podman)
- Podman (for containerized PostgreSQL in development)

**Production:**
- Java 17 runtime
- PostgreSQL 17.7 database
- Deployment targets: Docker/Podman containers, Kubernetes, bare JVM
- Native compilation optional via GraalVM (native profile in pom.xml)

## Build Modes

**Modes Available:**
- **JVM mode (default)**: `./mvnw package` produces layered JAR in `target/quarkus-app/`
- **Uber JAR**: `./mvnw package -Dquarkus.package.jar.type=uber-jar` produces single runnable JAR
- **Native mode**: `./mvnw package -Dnative` produces native executable (requires GraalVM)
- **Dev mode**: `./mvnw quarkus:dev` with live reload at `http://localhost:9080`

---

*Stack analysis: 2026-02-14*
