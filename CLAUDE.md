# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a reference application that uses Quarkus and HTMX to build server-rendered web pages.
- **[Java 21 LTS](https://adoptium.net/en-GB/temurin/releases?version=21)** Eclipse Temurin cross-platform, enterprise-ready, open-source Java runtime binaries
- **[Quarkus 3.30.3](https://quarkus.io/)** Kubernetes Native Java stack tailored for OpenJDK HotSpot and GraalVM with REST endpoints and Qute templating
- **[HTMX 2.0.8](https://htmx.org)** Dynamic HTML updates without client-side JavaScript
- **[UIkit 3.25](https://getuikit.com/)** CSS framework for styling
- **[Hibernate ORM](https://hibernate.org/orm/) with [Panache](https://quarkus.io/guides/hibernate-orm-panache)** Simplified Object Relational Mapper using Jakarta Persistence (formerly known as JPA)
- **[PostgreSQL 17.7](https://www.postgresql.org/) with [Flyway](https://flywaydb.org/)** the world's most powerful open source object-relational database system with lightweight database migration tool

## Key Commands

```bash
# Development (live reload at http://localhost:9080)
./mvnw compile quarkus:dev

# Testing
./mvnw test                           # All tests
./mvnw test -Dtest=ClassName          # Single class
./mvnw test -Dtest=ClassName#method   # Single method
./mvnw verify                         # Integration tests

# Building
./mvnw package                                          # Layered JAR
./mvnw package -Dquarkus.package.jar.type=uber-jar      # Uber JAR
./mvnw package -Dnative                                 # Native (GraalVM)
```

## Related Documentation

- `docs/ARCHITECTURE.md` - Patterns and technical decisions
- `docs/SECURITY.md` - Security policies and implementation
- `docs/WORKFLOW.md` - Spec-driven development workflow and naming conventions

## Managing the Backend Server

This project uses the Quarkus dev server which runs all the time, so issue a `curl http://127.0.0.1:9080/q/health` after a code update to trigger a server refresh.

If the curl command fails, check if the server is listening on port 9080 with `ss -tlnp | grep 9080`.

If it is not running, start it in the background with `./mvnw quarkus:dev -Dquarkus.console.enabled=false`.

When the server is started as a background task, its output is written to `/tmp/claude/-home-geraldo-quarkus-dd-mailer/tasks/<task-id>.output`. To check the server logs:

```bash
# View full output
cat /tmp/claude/-home-geraldo-quarkus-dd-mailer/tasks/<task-id>.output

# Follow logs in real-time
tail -f /tmp/claude/-home-geraldo-quarkus-dd-mailer/tasks/<task-id>.output
```

## Web Research Strategy

When doing web research for designing, reviewing or implementing anything, use the following sources and then supplement with Context7 MCP.

### Core Framework & Language

| Technology | Version | Documentation |
|------------|---------|---------------|
| Java | 21 LTS | https://docs.oracle.com/en/java/javase/21/ |
| Eclipse Temurin | 21 | https://adoptium.net/docs/ |
| Quarkus | 3.30.3 | https://quarkus.io/guides/ |
| Quarkus | 3.30.3 | https://quarkus.io/guides/getting-started |
| Maven | 3.x | https://maven.apache.org/guides/ |
| GraalVM (Native) | — | https://www.graalvm.org/latest/docs/ |

### Quarkus Extensions

| Purpose | Extension | Documentation |
|---------|-----------|---------------|
| REST API | `quarkus-rest` | https://quarkus.io/guides/rest |
| Templating | `quarkus-rest-qute` | https://quarkus.io/guides/qute |
| Panache ORM | `quarkus-hibernate-orm-panache` | https://quarkus.io/guides/hibernate-orm-panache |
| Database Driver | `quarkus-jdbc-postgresql` | https://quarkus.io/guides/datasource |
| Migrations | `quarkus-flyway` | https://quarkus.io/guides/flyway |
| Security | `quarkus-security-jpa` | https://quarkus.io/guides/security-jpa |
| Security | `quarkus-security-jpa` | https://quarkus.io/guides/security-form-auth |
| Validation | `quarkus-hibernate-validator` | https://quarkus.io/guides/validation |
| CDI | `quarkus-arc` | https://quarkus.io/guides/cdi |
| Testing | `quarkus-junit5` | https://quarkus.io/guides/getting-started-testing |

### Database

| Technology | Version | Documentation |
|------------|---------|---------------|
| PostgreSQL | 17.7 | https://www.postgresql.org/docs/17/ |
| PostgreSQL | 17.7 | https://www.postgresql.org/docs/17/tutorial.html |
| Flyway | — | https://documentation.red-gate.com/fd |
| Flyway | — | https://documentation.red-gate.com/fd/quickstart-how-flyway-works-184127223.html |
| Hibernate ORM | — | https://hibernate.org/orm/documentation/ |
| Jakarta Persistence (JPA) | — | https://jakarta.ee/specifications/persistence/ |

### Frontend Stack

| Technology | Version | Documentation |
|------------|---------|---------------|
| HTMX | 2.0.8 | https://htmx.org/reference/ |
| HTMX | 2.0.8 | https://htmx.org/examples/ |
| HTMX | 2.0.8 | https://htmx.org/docs/ |
| UIkit | 3.25 | https://getuikit.com/docs/introduction |

### Testing

| Technology | Documentation |
|------------|---------------|
| JUnit 5 | https://junit.org/junit5/docs/current/user-guide/ |
| REST Assured | https://rest-assured.io/ |
| Jsoup | https://jsoup.org/ |
| Jsoup | https://jsoup.org/cookbook/ |
| Quarkus Testing | https://quarkus.io/guides/getting-started-testing |

### Security

| Topic | Documentation |
|-------|---------------|
| Quarkus Security Overview | https://quarkus.io/guides/security-overview |
| Quarkus Form Authentication | https://quarkus.io/guides/security-form-auth |
| BCrypt Password Hashing | https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-bcrypt |
| NIST SP 800-63B-4 (Password Guidelines) | https://pages.nist.gov/800-63-4/sp800-63b.html |

### Quick Lookup by Task

| Task Type | First Consult |
|-----------|---------------|
| REST endpoints | https://quarkus.io/guides/rest |
| Templates/views | https://quarkus.io/guides/qute |
| Database/entities | https://quarkus.io/guides/hibernate-orm-panache |
| Migrations | https://quarkus.io/guides/flyway |
| Authentication | https://quarkus.io/guides/security-form-auth |
| Validation | https://quarkus.io/guides/validation |
| Frontend interactions | https://htmx.org/docs/ |
| UI components | https://getuikit.com/docs/introduction |

### Decision Tree

```
Need to research something?
│
├─ Is it Quarkus-specific?
│  └─ YES → Start at quarkus.io/guides/{topic}
│
├─ Is it database/SQL related?
│  └─ YES → PostgreSQL docs + Flyway docs
│
├─ Is it frontend behavior?
│  └─ YES → HTMX docs → UIkit docs
│
├─ Is it Java language feature?
│  └─ YES → Oracle Java 21 docs
│
└─ Is it testing related?
   └─ YES → Quarkus testing guide → JUnit 5 → REST Assured
```

### Context7 MCP (Supplement)

Use after consulting primary sources for:
- Code examples beyond official docs
- Version-specific API details
- Edge cases and workarounds

### Key Principles

1. **Version awareness**: This project uses specific versions (Quarkus 3.30.3, HTMX 2.0.8, UIkit 3.25, Java 21)
2. **Quarkus-first**: Most integration questions should start with Quarkus guides
3. **HTMX patterns**: Frontend behavior lookups should reference htmx.org/examples/
4. **Security compliance**: Security decisions reference NIST SP 800-63B-4

---
