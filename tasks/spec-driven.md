# Task: Implement Spec-Driven Workflow Improvements

**Created:** 2025-12-29
**Status:** Complete
**Priority:** High

---

## Problem Summary

The spec-driven workflow in this project has inconsistencies in how User Stories map to Use Cases and Test Cases. The naming convention is defined but not correctly applied.

### Naming Convention (from CLAUDE.md)

| Artifact | Format | Example |
|----------|--------|---------|
| User Story | US-FFF-SS | US-002-01, US-002-02 |
| Use Case | UC-FFF-SS-NN | UC-002-01-01, UC-002-02-01 |
| Test Case | TC-FFF-SS-NNN | TC-002-01-001, TC-002-02-001 |

**Key Rule:** The `FFF-SS` portion of UC and TC must match the parent US.

### Current Problem

In `specs/002-master-data-management/` and `specs/003-person-management/`, all use cases were incorrectly numbered under a single story (e.g., UC-002-01-01 through UC-002-01-04) when they should map to different stories:

**Wrong (current):**
```
US-002-01: View Gender → UC-002-01-01
US-002-02: Create Gender → UC-002-01-02  ← WRONG! Should be UC-002-02-01
US-002-03: Edit Gender → UC-002-01-03    ← WRONG! Should be UC-002-03-01
US-002-04: Delete Gender → UC-002-01-04  ← WRONG! Should be UC-002-04-01
```

**Correct:**
```
US-002-01: View Gender → UC-002-01-01
US-002-02: Create Gender → UC-002-02-01, UC-002-02-02
US-002-03: Edit Gender → UC-002-03-01, UC-002-03-02, UC-002-03-03
US-002-04: Delete Gender → UC-002-04-01
```

---

## Tasks

### Task 1: Fix 002-master-data-management/use-cases.md ✅ COMPLETE

The use-cases.md has been updated with correct numbering:
- UC-002-01-01: View Gender List (under US-002-01)
- UC-002-02-01: Display Create Form (under US-002-02)
- UC-002-02-02: Submit Create Form (under US-002-02)
- UC-002-03-01: Display Edit Form (under US-002-03)
- UC-002-03-02: Submit Edit Form (under US-002-03)
- UC-002-03-03: Cancel Edit (under US-002-03)
- UC-002-04-01: Delete Gender (under US-002-04)

---

### Task 2: Fix 002-master-data-management/test-cases.md 🔲 TODO

**Current file has wrong numbering (TC-002-01-001 through TC-002-01-016).**

Rewrite with correct mapping:

```
# US-002-01: View Gender Master Data
TC-002-01-001: Gender Page UI Elements → UC-002-01-01
TC-002-01-002: Gender List Display → UC-002-01-01
TC-002-01-003: Gender List Empty State → UC-002-01-01
TC-002-01-004: Gender Access Requires Admin Role → UC-002-01-01

# US-002-02: Create New Gender
TC-002-02-001: Gender Create Form Display → UC-002-02-01
TC-002-02-002: Gender Create Success → UC-002-02-02
TC-002-02-003: Gender Create Code Uppercase → UC-002-02-02
TC-002-02-004: Gender Create Duplicate Code Prevention → UC-002-02-02
TC-002-02-005: Gender Create Duplicate Description Prevention → UC-002-02-02
TC-002-02-006: Gender Create Code Max Length → UC-002-02-02

# US-002-03: Edit Existing Gender
TC-002-03-001: Gender Edit Form Display → UC-002-03-01
TC-002-03-002: Gender Edit Success → UC-002-03-02
TC-002-03-003: Gender Edit Cancel → UC-002-03-03

# US-002-04: Delete Gender
TC-002-04-001: Gender Delete Confirmation → UC-002-04-01
TC-002-04-002: Gender Delete Success → UC-002-04-01
TC-002-04-003: Gender Delete Cancel → UC-002-04-01
```

---

### Task 3: Fix 003-person-management/use-cases.md 🔲 TODO

**Current file has wrong numbering. Rewrite with correct mapping based on USER-STORIES.md:**

```
US-003-01: View Persons List
  → UC-003-01-01: View Persons List

US-003-02: Create New Person
  → UC-003-02-01: Display Create Form
  → UC-003-02-02: Submit Create Form

US-003-03: Edit Existing Person
  → UC-003-03-01: Display Edit Form
  → UC-003-03-02: Submit Edit Form
  → UC-003-03-03: Cancel Edit

US-003-04: Delete Person
  → UC-003-04-01: Delete Person

US-003-05: Filter People
  → UC-003-05-01: Apply Filter
  → UC-003-05-02: Clear Filter

US-003-06: Sort People (MISSING - must add)
  → UC-003-06-01: Apply Sort
  → UC-003-06-02: Clear Sort
```

---

### Task 4: Fix 003-person-management/test-cases.md 🔲 TODO

Renumber all test cases to match the corrected use case numbering:

```
# US-003-01: View Persons List
TC-003-01-001: Persons Page UI Elements
TC-003-01-002: Persons List Display
TC-003-01-003: Persons List Empty State
TC-003-01-004: Persons Access Requires Authentication

# US-003-02: Create New Person
TC-003-02-001: Person Create Form Display
TC-003-02-002: Person Create Success
TC-003-02-003: Person Create Email Required
TC-003-02-004: Person Create Email Format Validation
TC-003-02-005: Person Create Duplicate Email Prevention

# US-003-03: Edit Existing Person
TC-003-03-001: Person Edit Form Display
TC-003-03-002: Person Edit Success
TC-003-03-003: Person Edit Email Uniqueness
TC-003-03-004: Person Edit Cancel

# US-003-04: Delete Person
TC-003-04-001: Person Delete Confirmation
TC-003-04-002: Person Delete Success
TC-003-04-003: Person Delete Cancel

# US-003-05: Filter People
TC-003-05-001: Persons Filter by Name
TC-003-05-002: Persons Filter No Results
TC-003-05-003: Persons Filter Clear
TC-003-05-004: Persons Filter Persistence

# US-003-06: Sort People (NEW)
TC-003-06-001: Persons Sort by Name
TC-003-06-002: Persons Sort Clear
TC-003-06-003: Persons Sort Persistence
```

---

### Task 5: Create 002-master-data-management/spec.md 🔲 TODO

Create technical specification including:
- Database schema (gender table)
- Entity design (Gender.java)
- Resource endpoints (GenderResource.java)
- Template structure (partials)
- HTMX patterns used
- Security configuration (@RolesAllowed("admin"))

Reference `docs/ARCHITECTURE.md` Section 4.2 for GenderResource pattern.

---

### Task 6: Create 003-person-management/spec.md 🔲 TODO

Create technical specification including:
- Database schema (person table)
- Entity design (Person.java)
- Resource endpoints (PersonResource.java)
- Template structure (partials)
- HTMX patterns used
- Security configuration (@RolesAllowed({"user", "admin"}))

Reference `docs/ARCHITECTURE.md` Section 4.3 for PersonResource pattern.

---

### Task 7: Create 002-master-data-management/tasks.md 🔲 TODO

Create implementation task breakdown with:
- One section per use case (UC-002-01-01 through UC-002-04-01)
- Implementation tasks with checkboxes
- Test results section
- Status badges (use 🔲 Not Started for all)

Use `specs/001-identity-and-access-management/tasks.md` as template.

---

### Task 8: Create 003-person-management/tasks.md 🔲 TODO

Create implementation task breakdown with:
- One section per use case (UC-003-01-01 through UC-003-06-02)
- Implementation tasks with checkboxes
- Test results section
- Status badges (use 🔲 Not Started for all)

---

### Task 9: Update specs/PROJECT-PLAN.md 🔲 TODO

Add references to new feature task files:

```markdown
## Feature plans

@specs/000-foundation/tasks.md
@specs/001-identity-and-access-management/tasks.md
@specs/002-master-data-management/tasks.md
@specs/003-person-management/tasks.md
```

Update Progress Summary table to include Features 002 and 003.

---

### Task 10: Create specs/TEMPLATE/ folder 🔲 TODO

Create template files for new features:

```
specs/TEMPLATE/
├── use-cases.md.template
├── spec.md.template
├── test-cases.md.template
└── tasks.md.template
```

Each template should include placeholder structure matching the naming conventions.

---

### Task 11: Update CLAUDE.md 🔲 TODO

Add to the "Development Workflow" section:

1. **Workflow Diagram** (ASCII art showing flow from USER-STORIES → use-cases → spec → tasks → test-cases → implementation)

2. **Validation Rules** section:
```markdown
### Validation Rules

Before starting implementation, verify:
1. Every UC-FFF-SS-NN links back to a valid US-FFF-SS in USER-STORIES.md
2. Every TC-FFF-SS-NNN links back to a valid UC-FFF-SS-NN in use-cases.md
3. All 4 spec files exist: use-cases.md, spec.md, tasks.md, test-cases.md
4. PROJECT-PLAN.md references the tasks.md for this feature
```

3. **Update workflow order** to: use-cases → spec → tasks → test-cases → implement

---

### Task 12: Git Commit 🔲 TODO

Stage and commit all changes with message:
```
Implement spec-driven workflow improvements

- Fix UC/TC numbering in 002-master-data-management to match US mapping
- Fix UC/TC numbering in 003-person-management to match US mapping
- Add missing US-003-06 (Sort People) use case and tests
- Create spec.md for features 002 and 003
- Create tasks.md for features 002 and 003
- Update PROJECT-PLAN.md with new feature references
- Create TEMPLATE folder for new feature scaffolding
- Update CLAUDE.md with workflow diagram and validation rules
```

---

## Reference Files

| File | Purpose |
|------|---------|
| `docs/USER-STORIES.md` | Source of truth for user stories |
| `docs/ARCHITECTURE.md` | Technical patterns (Section 4.2, 4.3 for resources) |
| `specs/000-foundation/tasks.md` | Template for tasks.md structure |
| `specs/001-identity-and-access-management/tasks.md` | Template for tasks.md structure |
| `CLAUDE.md` | Workflow documentation to update |

---

## Progress Tracker

| # | Task | Status |
|---|------|--------|
| 1 | Fix 002 use-cases.md | ✅ Complete |
| 2 | Fix 002 test-cases.md | ✅ Complete |
| 3 | Fix 003 use-cases.md | ✅ Complete |
| 4 | Fix 003 test-cases.md | ✅ Complete |
| 5 | Create 002 spec.md | ✅ Complete |
| 6 | Create 003 spec.md | ✅ Complete |
| 7 | Create 002 tasks.md | ✅ Complete |
| 8 | Create 003 tasks.md | ✅ Complete |
| 9 | Update PROJECT-PLAN.md | ✅ Complete |
| 10 | Create TEMPLATE folder | ✅ Complete |
| 11 | Update CLAUDE.md | ✅ Complete |
| 12 | Git commit | ✅ Complete |
