# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a reference application — the base template for new projects — that uses Quarkus and HTMX to build server-rendered web pages.

- **[Java 21 LTS](https://adoptium.net/en-GB/temurin/releases?version=21)** Eclipse Temurin runtime
- **[Quarkus 3.30.3](https://quarkus.io/)** with REST endpoints and Qute templating
- **[HTMX 2.0.8](https://htmx.org)** Dynamic HTML updates without client-side JavaScript
- **[UIkit 3.25](https://getuikit.com/)** CSS framework for styling
- **[Hibernate ORM](https://hibernate.org/orm/) with [Panache](https://quarkus.io/guides/hibernate-orm-panache)** repository pattern (not active record)
- **[PostgreSQL 17.7](https://www.postgresql.org/) with [Flyway](https://flywaydb.org/)** migrations

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

## Development Workflow

Work is issue-driven (see `docs/adr/0001-issue-driven-workflow.md`):

1. **Define** — capture work as GitHub Issues via the `triage`, `to-prd`, and `to-issues` skills
2. **Implement** — test-first via the `tdd` skill
3. **Verify** — spawn the `e2e-test-runner` subagent (`.claude/agents/e2e-test-runner.md`) with concrete scenarios to check behavior in a real browser via chrome-devtools MCP
4. **Record** — domain language goes in `CONTEXT.md`, decisions in `docs/adr/` (maintained by `/grill-with-docs`)

## Related Documentation

- `docs/ARCHITECTURE.md` — Patterns and technical decisions
- `docs/HTMX_ALIGNMENT.md` — Audit of HTMX usage against the `htmx-patterns` skill
- `docs/TEST-STRATEGY-CI-CD.md` — Test strategy
- `docs/adr/` — Architecture decision records
- `docs/agents/` — How agent skills consume this repo (issue tracker, triage labels, domain docs)

## Project Skills

Project-specific conventions live in `.claude/skills/` — consult them before writing code in their area:

| Skill | Covers |
|-------|--------|
| `htmx-patterns` | HTMX + Qute + UIkit conventions: swap strategies, OOB updates, modal CRUD, fragment naming |
| `quarkus-patterns` | Quarkus platform idioms: CDI/ArC, events, config, scheduling, test mocking, Spring-leak review |
| `java-patterns` | Resource/service/repository layering, entity conventions, exception handling |
| `java-code-review` | Review checklist calibrated to this codebase's patterns |
| `maven-java` | Build configuration, dependency policy, Quarkus extension management |
| `postgresql-java` | Flyway versioning scheme, seed data, schema and repository conventions |

## Managing the Backend Server

This project uses the Quarkus dev server which runs all the time, so issue a `curl http://127.0.0.1:9080/q/health` after a code update to trigger a server refresh.

If the curl command fails, check if the server is listening on port 9080 with `ss -tlnp | grep 9080`.

If it is not running, start it as a background task with `./mvnw quarkus:dev -Dquarkus.console.enabled=false` and check the task's output (reported when the background task starts) for startup errors.

## Web Research Strategy

Consult official docs first, then supplement with Context7 MCP for code examples, version-specific API details, and edge cases. This project pins specific versions: Quarkus 3.30.3, HTMX 2.0.8, UIkit 3.25, Java 21, PostgreSQL 17.7.

| Topic | First consult |
|-------|---------------|
| REST endpoints | https://quarkus.io/guides/rest |
| Templates/views | https://quarkus.io/guides/qute |
| Database/entities | https://quarkus.io/guides/hibernate-orm-panache |
| Migrations | https://quarkus.io/guides/flyway + https://documentation.red-gate.com/fd |
| Authentication | https://quarkus.io/guides/security-form-auth |
| Security (JPA identity) | https://quarkus.io/guides/security-jpa |
| Validation | https://quarkus.io/guides/validation |
| CDI | https://quarkus.io/guides/cdi |
| Frontend interactions | https://htmx.org/docs/ + https://htmx.org/examples/ |
| UI components | https://getuikit.com/docs/introduction |
| Testing | https://quarkus.io/guides/getting-started-testing + https://rest-assured.io/ + https://jsoup.org/ |
| PostgreSQL | https://www.postgresql.org/docs/17/ |
| Java language | https://docs.oracle.com/en/java/javase/21/ |
| Password policy | NIST SP 800-63B-4: https://pages.nist.gov/800-63-4/sp800-63b.html |

---

## Agent skills

### Issue tracker

GitHub Issues at `GeraldScott/hx-qute` via the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Canonical strings (defaults, unmodified): `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `CONTEXT.md` + `docs/adr/` at the repo root (created lazily by `/grill-with-docs`). See `docs/agents/domain.md`.
