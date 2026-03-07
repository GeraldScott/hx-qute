# Technology Stack

**Analysis Date:** 2026-03-07

## Languages

**Primary:**
- Java 21 LTS (Eclipse Temurin) - All backend code
- Compiler target set to Java 17 in `pom.xml` (`maven.compiler.release=17`)

**Secondary:**
- HTML - Qute templates in `src/main/resources/templates/`
- JavaScript (vanilla) - Client-side graph visualization in `src/main/resources/META-INF/resources/js/graph.js`
- CSS - Custom styles in `src/main/resources/META-INF/resources/style.css`
- SQL - Flyway migrations in `src/main/resources/db/migration/`

## Runtime

**Environment:**
- JDK 21 (Eclipse Temurin recommended)
- Docker base image: `registry.access.redhat.com/ubi9/openjdk-21:1.23` (see `src/main/docker/Dockerfile.jvm`)
- GraalVM native compilation supported via `native` Maven profile

**Package Manager:**
- Apache Maven 3.9.9 (via Maven Wrapper)
- Wrapper version: 3.3.2
- Lockfile: Not applicable (Maven uses `pom.xml` dependency resolution)
- Wrapper config: `.mvn/wrapper/maven-wrapper.properties`

## Frameworks

**Core:**
- Quarkus 3.30.3 - Application framework (BOM: `io.quarkus.platform:quarkus-bom:3.30.3`)
- Quarkus REST (formerly RESTEasy Reactive) - HTTP endpoints via `quarkus-rest-qute` and `quarkus-rest-jsonb`
- Qute - Server-side HTML templating engine
- Hibernate ORM with Panache - ORM layer via `quarkus-hibernate-orm-panache`
- Quarkus Security JPA - Authentication via `quarkus-security-jpa`

**Frontend (CDN-loaded, not bundled):**
- HTMX 2.0.8 - Dynamic HTML updates without client-side JS framework
- UIkit 3.25.4 - CSS/JS framework for styling and UI components
- D3.js v7 - Force-directed network graph visualization (loaded only on graph page)

**Testing:**
- JUnit 5 - Test runner via `quarkus-junit5`
- Maven Surefire 3.5.3 - Unit test execution
- Maven Failsafe 3.5.3 - Integration test execution

**Build/Dev:**
- Maven Compiler Plugin 3.14.0 - Java compilation with `-parameters` flag enabled
- Quarkus Maven Plugin 3.30.3 - Build, code generation, native image support
- Quarkus Dev Mode - Live reload via `./mvnw compile quarkus:dev`

## Key Dependencies

**Critical (from `pom.xml`):**
- `quarkus-rest-qute` - REST endpoints with Qute template rendering
- `quarkus-rest-jsonb` - JSON-B serialization for REST API responses (used by graph data endpoint)
- `quarkus-hibernate-orm-panache` - Simplified JPA with Repository pattern
- `quarkus-jdbc-postgresql` - PostgreSQL JDBC driver
- `quarkus-flyway` - Database schema migrations at startup
- `quarkus-security-jpa` - JPA-backed authentication with `@UserDefinition` entity
- `quarkus-hibernate-validator` - Bean Validation (Jakarta Validation)
- `quarkus-arc` - CDI dependency injection (ArC, Quarkus CDI implementation)
- `quarkus-smallrye-health` - Health check endpoints at `/q/health`

**Infrastructure:**
- JBoss LogManager - Logging backend (configured via Surefire system property)
- Flyway - Schema versioning with 12 migration scripts (`V1.0.0` through `V1.6.1`)

## Configuration

**Environment:**
- Application properties: `src/main/resources/application.properties`
- HTTP port: `9080` (non-standard to avoid clashes)
- Custom config properties using `@ConfigProperty`:
  - `app.security.password.min-length` (default: 15) - used in `src/main/java/io/archton/scaffold/service/PasswordValidator.java`
  - `app.security.password.max-length` (default: 128) - used in `src/main/java/io/archton/scaffold/service/PasswordValidator.java`
- Database connection configured via Quarkus datasource properties (`quarkus.datasource.*`)
- Flyway runs migrations at startup (`quarkus.flyway.migrate-at-start=true`)
- Hibernate ORM schema management disabled (`quarkus.hibernate-orm.schema-management.strategy=none`) - Flyway handles DDL

**Build:**
- `pom.xml` - Maven project descriptor with Quarkus BOM
- `.mvn/wrapper/maven-wrapper.properties` - Maven wrapper configuration
- `src/main/docker/Dockerfile.jvm` - Standard JVM container image
- `src/main/docker/Dockerfile.native` - GraalVM native container image
- `src/main/docker/Dockerfile.native-micro` - Minimal native container image
- `src/main/docker/Dockerfile.legacy-jar` - Legacy JAR container image

**Security Configuration (in `application.properties`):**
- Form-based authentication with session cookie (`quarkus-credential`)
- Session timeout: 30 minutes
- Cookie rotation interval: 1 minute
- SameSite: strict
- Route protection: `/dashboard/*`, `/api/*`, `/persons/*`, `/profile/*`, `/graph/*` require authentication
- Admin routes: `/admin/*`, `/genders/*`, `/titles/*`, `/relationships/*` require `admin` role
- Public routes: `/`, `/login`, `/signup`, static assets

## Platform Requirements

**Development:**
- JDK 21+ (Temurin recommended)
- Maven 3.9.9 (provided by wrapper, run `./mvnw`)
- PostgreSQL 17.x (Quarkus Dev Services can auto-provision via Testcontainers)
- Port 9080 available

**Production:**
- JDK 21 runtime or GraalVM native binary
- PostgreSQL 17.x database
- Docker support via provided Dockerfiles in `src/main/docker/`

**Key Commands:**
```bash
./mvnw compile quarkus:dev                              # Dev mode with live reload (port 9080)
./mvnw test                                              # Run unit tests
./mvnw verify                                            # Run integration tests
./mvnw package                                           # Build layered JAR
./mvnw package -Dquarkus.package.jar.type=uber-jar       # Build uber JAR
./mvnw package -Dnative                                  # Build native binary (requires GraalVM)
curl http://127.0.0.1:9080/q/health                      # Trigger dev server reload / health check
```

---

*Stack analysis: 2026-03-07*
