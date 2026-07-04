---
name: maven-java
description: Use when changing the build — adding dependencies or Quarkus extensions, upgrading Quarkus, troubleshooting Maven/build failures, or packaging (JAR, uber-JAR, native).
---

# Project Build Conventions

Single-module Maven project. Java **21** (`maven.compiler.release`), Quarkus **3.30.3** via BOM import. Build with the wrapper: `./mvnw`, never a system `mvn`.

## Dependency Policy

- **All Quarkus artifacts are version-managed by the BOM** (`io.quarkus.platform:quarkus-bom:${quarkus.platform.version}`). Never add a `<version>` to a `io.quarkus:*` dependency.
- Add extensions with `./mvnw quarkus:add-extension -Dextensions="name"` rather than hand-editing.
- Non-Quarkus dependencies (e.g. `org.jsoup:jsoup`) get an explicit pinned version in `<properties>`.
- **No Lombok, no MapStruct** — entities use public fields (Panache style), and there is no DTO mapping layer.
- Upgrading Quarkus = bump `quarkus.platform.version` only, then run `./mvnw verify` and check the migration notes at https://github.com/quarkusio/quarkus/wiki/Migration-Guides.

## Current Extensions and Why

| Extension | Why it's here |
|-----------|---------------|
| `quarkus-rest-qute` | REST endpoints returning Qute `TemplateInstance` |
| `quarkus-rest-jsonb` | JSON for the graph data endpoints |
| `quarkus-arc` | CDI |
| `quarkus-hibernate-orm-panache` | Repository-pattern ORM |
| `quarkus-jdbc-postgresql` | Driver |
| `quarkus-flyway` | Migrations (owns all DDL) |
| `quarkus-hibernate-validator` | Bean Validation |
| `quarkus-security-jpa` | Form auth against `user_login` table |
| `quarkus-smallrye-health` | `/q/health` (also used to trigger dev-mode reload) |
| `quarkus-junit5`, `rest-assured`, `jsoup` (test) | `@QuarkusTest` + HTTP assertions + HTML parsing |

Before adding a new extension, check this table — prefer what's already here (e.g. don't add `quarkus-rest-jackson` alongside `quarkus-rest-jsonb`, or OIDC alongside form auth).

## Commands

```bash
./mvnw compile quarkus:dev      # dev mode, live reload, port 9080
./mvnw test                     # unit/@QuarkusTest (surefire)
./mvnw verify                   # + integration tests (failsafe; ITs skipped unless -Dnative)
./mvnw package                  # layered JAR
./mvnw package -Dquarkus.package.jar.type=uber-jar
./mvnw package -Dnative         # GraalVM native (activates the `native` profile)

./mvnw dependency:tree -Dincludes=group:artifact   # trace a transitive dep
./mvnw quarkus:list-extensions                     # discover extensions
```

## Build Layout Notes

- `skipITs=true` by default; the `native` profile flips it and enables `quarkus.native.enabled`
- Surefire/failsafe set `java.util.logging.manager=org.jboss.logmanager.LogManager` — keep this when touching plugin config or Quarkus tests break with logging errors
- `maven-compiler-plugin` sets `<parameters>true</parameters>` — required for JAX-RS parameter-name reflection; do not remove

## Related Skills

- `postgresql-java` — what the Flyway/JDBC extensions do at runtime
- `java-patterns` — where code for each concern lives
