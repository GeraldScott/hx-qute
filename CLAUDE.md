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
- @docs/WORKFLOW.md - Spec-driven development workflow and naming conventions

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
