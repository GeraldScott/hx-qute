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

- @docs/ARCHITECTURE.md - Patterns and technical decisions
- @docs/SECURITY.md - Security policies and implementation
- @docs/API-CONVENTIONS.md - Routing and response formats

## Naming Conventions

### User Stories (US-FFF-SS)
User stories in `docs/USER-STORIES.md` are grouped by feature and follow the format `US-FFF-SS` (to cater for 999 features, each with 99 user stories. That is enough for a large system):
- `FFF` - 3-digit feature number (001, 002, 003)
- `SS` - 2-digit story number within feature (01, 02, 03)

Examples:
- `US-001-01` - Feature 001, Story 01 (User Registration)
- `US-001-02` - Feature 001, Story 02 (User Login)
- `US-002-01` - Feature 002, Story 01 (View Gender Master Data)

### Use Cases (UC-FFF-SS-NN)
Use cases in `specs/*/use-cases.md` follow the format `UC-FFF-SS-NN` (each user story can have up to 99 use cases, which would be an insane number for one user story):
- `FFF` - 3-digit feature number matching the parent user story
- `SS` - 2-digit story number matching the parent user story
- `NN` - 2-digit use case number within the story (01, 02, 03)

Examples:
- `UC-001-01-01` - Display Signup Page (under US-001-01)
- `UC-001-01-02` - Register New User (under US-001-01)
- `UC-001-02-01` - Display Login Page (under US-001-02)

### Test Cases (TC-FFF-SS-NNN)
Test cases follow the same pattern as use cases: `TC-FFF-SS-NNN` (note the three digits, in case we need a thousand tests for a user story. Surely that's enough.)

## Development Workflow

### Spec-Driven Workflow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                        docs/USER-STORIES.md                         │
│                    (Source of truth for features)                   │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    specs/NNN-feature-name/                          │
├─────────────────────────────────────────────────────────────────────┤
│  1. use-cases.md     ─────────────────────────────────────────────► │
│     (UC-FFF-SS-NN)           Maps to User Stories                   │
│                                     │                               │
│  2. spec.md          ◄──────────────┘                               │
│     (Technical design)       References ARCHITECTURE.md             │
│                                     │                               │
│  3. tasks.md         ◄──────────────┘                               │
│     (Implementation plan)    Tracks progress per UC                 │
│                                     │                               │
│  4. test-cases.md    ◄──────────────┘                               │
│     (TC-FFF-SS-NNN)          Maps to Use Cases                      │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Implementation                                │
│    (src/main/java, src/main/resources/templates, db/migration)      │
└─────────────────────────────────────────────────────────────────────┘
```

### Validation Rules

Before starting implementation, verify:

1. **User Story Exists**: Every `UC-FFF-SS-NN` links back to a valid `US-FFF-SS` in `docs/USER-STORIES.md`
2. **Use Case Numbering**: The `FFF-SS` portion of UC must match the parent US
3. **Test Case Mapping**: Every `TC-FFF-SS-NNN` links back to a valid `UC-FFF-SS-NN` in `use-cases.md`
4. **Complete Spec Files**: All 4 spec files exist: `use-cases.md`, `spec.md`, `tasks.md`, `test-cases.md`
5. **Project Plan Reference**: `specs/PROJECT-PLAN.md` references the `tasks.md` for this feature

### Feature-based Workflow

The project implementation is controlled by @specs/PROJECT-PLAN.md

It will be implemented in a phased approach, one feature at a time:
- Read `NNN-feature-name/tasks.md` for the status of the current phase and the use cases that must be implemented.
- Read `NNN-feature-name/spec.md` before implementing each use case to understand the technical requirements.
- Implement the use case as per the technical specification and keep track of progress in `NNN-feature-name/tasks.md`
- Issue a `curl http://127.0.0.1:9080/q/health` after a code update to trigger a server refresh.

### Creating New Features

For the development and implementation of new features, use the spec workflow in `specs/`:

1. **Create folder**: `NNN-feature-name/` (e.g., `002-master-data-management/`)
   - Use templates from `specs/TEMPLATE/` as starting points

2. **Define use-cases.md**: Describe feature requirements from user perspective
   - Each use case relates back to a User Story in `docs/USER-STORIES.md`
   - Follow `UC-FFF-SS-NN` naming where `FFF-SS` matches the parent `US-FFF-SS`

3. **Create spec.md**: Technical specification including:
   - Database schema and migrations
   - Entity design (PanacheEntity pattern)
   - Resource endpoints and HTMX patterns
   - Template structure
   - Security configuration
   - References `docs/ARCHITECTURE.md` for patterns

4. **Generate tasks.md**: Implementation task breakdown
   - One section per use case
   - Checkboxes for individual tasks
   - Test results placeholder

5. **Generate test-cases.md**: Verification of use cases
   - Follow `TC-FFF-SS-NNN` naming matching the parent UC
   - Guide browser-based testing via chrome-devtools MCP
   - Define CI/CD tests in `src/test`

6. **Update PROJECT-PLAN.md**: Add reference to new feature's `tasks.md`

7. **Implement feature**: Work through tasks, checking each as completed

8. **Run tests**: Use chrome-devtools MCP in a sub-agent. Update `tasks.md` with results.

**IMPORTANT**: After completing the tests for each use case, STOP and ask for user feedback before proceeding.

### Tracking Progress

The `NNN-feature-name/tasks.md` file serves as the single source of truth for implementation progress for each feature. Update it as follows:

#### Status Badges
Update the status field for each use case:
- `🔲 Not Started` - Work has not begun
- `🔄 In Progress` - Currently being implemented
- `✅ Complete` - Implementation and tests passed
- `❌ Blocked` - Cannot proceed due to dependency or issue

#### Implementation Checkboxes
Mark tasks complete as you finish them:
```markdown
- [x] Create entity class
- [x] Create repository
- [ ] Create resource endpoint  ← currently working on
```

#### Test Results Block
After running chrome-devtools tests, update:
```markdown
**Test Results:**
Test ID: TC-001-02-01
Status: ✅ Passed
Notes: All assertions passed on 2025-12-27
```

### Workflow Checklist
When starting a use case:
1. Set status to `🔄 In Progress`
2. Update **Current Status** section
3. Check off implementation tasks as completed
4. Run tests and update **Test Results**
5. Set status to `✅ Complete`
6. Update **Progress Summary** counts
7. Set **Next Use Case** to the next item
8. **STOP** and ask for user feedback

### Current Status
Always update `Current Status Section` in @specs/PROJECT-PLAN.md to reflect current position after update the feature task list.

Update counts in `Progress Summary Table` after completing each use case.

## Managing the backend server

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
