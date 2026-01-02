# Development Workflow

This document describes the spec-driven workflow for implementing features in this project.

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

---

## Feature-based Workflow

The project implementation is controlled by @specs/PROJECT-PLAN.md

It will be implemented in a phased approach, one feature at a time:
- Read `NNN-feature-name/tasks.md` for the status of the current phase and the use cases that must be implemented.
- Read `NNN-feature-name/spec.md` before implementing each use case to understand the technical requirements.
- Implement the use case as per the technical specification and keep track of progress in `NNN-feature-name/tasks.md`
- Issue a `curl http://127.0.0.1:9080/q/health` after a code update to trigger a server refresh.

---

## Creating New Features

For the development and implementation of new features, do the following:

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

---

## Implement new features

Before starting implementation, confirm that the custom command `validate-feature` has been executed.

Use the custom command `/implement-feature`


Before starting implementation, confirm that the custom command `validate-feature` has been executed.

---

## Pre-Implementation Reasoning

Before coding each use case, take time to understand the full context. This deliberate pause prevents wasted effort and ensures alignment with project patterns.

### Before Coding Begins

1. **Re-read the use case** in `use-cases.md` - understand the user's goal
2. **Review relevant sections** of `spec.md` - note entities, endpoints, and templates involved
3. **Consider edge cases** - validation rules, error states, empty data scenarios
4. **Check dependencies** - does this UC depend on prior work? Are those complete?
5. **Confirm approach** aligns with `docs/ARCHITECTURE.md` patterns

### Questions to Answer

- What entities and repositories are involved?
- What new endpoints or templates are needed?
- Are there security considerations (roles, permissions)?
- How does this integrate with existing functionality?
- What could go wrong? How should errors be handled?

---

## E2E Test Runner Subagent

The project includes an `e2e-test-runner` subagent (`.claude/agents/e2e-test-runner.md`) that automates browser-based testing using chrome-devtools MCP.

### When It's Used

The `/implement-feature` skill automatically invokes the `e2e-test-runner` subagent after implementation is complete. The subagent:

1. Parses `test-cases.md` to find tests matching the implemented UC
2. Opens a browser and logs in as admin
3. Executes each test case step using chrome-devtools MCP
4. Verifies expected results
5. Returns structured test results for tasks.md

### Test Case Naming Convention

Test cases are matched to use cases by ID prefix:
- `UC-002-03-02` → Tests starting with `TC-002-03-0XX`
- `UC-001-01-01` → Tests starting with `TC-001-01-0XX`

### Test Credentials

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | AdminPassword123 | admin |

### Manual Invocation

To run tests manually, use the Task tool with:
```
Use the e2e-test-runner subagent:
Feature folder: specs/002-master-data-management
Use case: UC-002-03-02
Application URL: http://localhost:9080
```

---

## Tracking Progress

The `NNN-feature-name/tasks.md` file serves as the single source of truth for implementation progress for each feature. Update it as follows:

### Status Badges
Update the status field for each use case:
- `🔲 Not Started` - Work has not begun
- `🔄 In Progress` - Currently being implemented
- `✅ Complete` - Implementation and tests passed
- `❌ Blocked` - Cannot proceed due to dependency or issue

### Implementation Checkboxes
Mark tasks complete as you finish them:
```markdown
- [x] Create entity class
- [x] Create repository
- [ ] Create resource endpoint  ← currently working on
```

### Test Results Block
After running E2E tests via the `e2e-test-runner` subagent, update with the structured format:
```markdown
**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-001-02-001 | ✅ | All assertions passed |
| TC-001-02-002 | ✅ | Form validation working |
| TC-001-02-003 | ❌ | Expected "X" but found "Y" |

**Run Date:** 2025-12-27
**Summary:** 2/3 tests passed
```

---

## Workflow Checklist

When starting a use case:
1. Set status to `🔄 In Progress`
2. Update **Current Status** section
3. Check off implementation tasks as completed
4. Run tests and update **Test Results**
5. Set status to `✅ Complete`
6. Update **Progress Summary** counts
7. Set **Next Use Case** to the next item
8. **STOP** and ask for user feedback

### Updating Current Status
Always update `Current Status Section` in @specs/PROJECT-PLAN.md to reflect current position after updating the feature task list.

Update counts in `Progress Summary Table` after completing each use case.

---

## Guidelines Maintenance

Project documentation should evolve alongside the codebase. After completing a feature, review and update living documents to capture new patterns and decisions.

### After Feature Completion

Review and update as needed:

| Document | Update When... |
|----------|----------------|
| `CLAUDE.md` | New patterns, commands, or conventions were established |
| `docs/ARCHITECTURE.md` | Architectural decisions were made or patterns refined |
| `docs/SECURITY.md` | Security policies or implementations changed |
| `specs/TEMPLATE/` | Template files need improvements based on lessons learned |

### What to Capture

- **New patterns**: If you solved a problem in a reusable way, document it
- **Gotchas**: Edge cases or pitfalls that future work should avoid
- **Decisions**: Why a particular approach was chosen over alternatives
- **Dependencies**: New libraries or integrations added

### Review Prompt

After marking a feature complete, ask:
> "Did this feature introduce anything that future features should know about?"

If yes, update the relevant documentation before moving on.
