# PROJECT-PLAN.md

**Project:** HX Qute Reference Application
**Document Version:** 1.0
**Last Updated:** 2025-12-27

---

## Overview

This document defines the phased implementation plan for the HX Qute application. Each phase builds upon the previous, establishing technical foundations before implementing business features. Use cases are ordered by technical dependency, not business priority.

### Implementation Workflow

For each use case:
1. Read the technical specification in `specs/SYSTEM-SPECIFICATION.md`
2. Implement the required components (entities, repositories, resources, templates)
3. Trigger server refresh with `curl http://127.0.0.1:9080/q/health`
4. Execute test cases from `specs/TEST-CASES.md` using chrome-devtools
5. Update this document with test results
6. **STOP** - Await user feedback before proceeding

---

## Phase 0: Foundation

Establishes the core framework before any use case implementation.

### P0.1: Project Scaffolding
| Attribute | Value |
|-----------|-------|
| Status | ✅ Complete |
| Dependencies | None |

**Deliverables:**
- [x] Quarkus project with Maven wrapper
- [x] HTMX 2.0.8 integration
- [x] UIkit 3.25 CSS framework
- [x] PostgreSQL + Hibernate ORM Panache configuration
- [x] Flyway migration infrastructure
- [x] Base template (`templates/base.html`)
- [x] Home page with navigation structure
- [x] GlobalExceptionMapper for error handling

---

### P0.2: Database Schema Foundation
| Attribute | Value |
|-----------|-------|
| Status | ✅ Complete |
| Dependencies | P0.1 |

**Deliverables:**
- [x] Flyway migration: `V1.0.0__Initial_schema.sql`
- [x] Gender table with audit fields
- [x] Person table with Gender FK
- [x] UserLogin table with Person FK
- [x] Role and UserRole tables for RBAC
- [x] Seed data for admin user and roles

---

## Phase 1: Authentication

Authentication must be implemented first as all other features depend on secured access.

### UC-1.3: Display Login Page
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | P0.2 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.1.3 |

**Implementation Tasks:**
- [ ] Create `AuthResource.java` with login endpoint
- [ ] Create `templates/AuthResource/login.html`
- [ ] Configure form-based authentication in `application.properties`
- [ ] Add login/signup links to base template navigation

**Test Results:**
```
Test ID: TC-1.3
Status: ⬜ Not Tested
Notes:
```

---

### UC-1.1: Display Signup Page
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-1.3 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.1.1 |

**Implementation Tasks:**
- [ ] Add signup endpoint to `AuthResource.java`
- [ ] Create `templates/AuthResource/signup.html`
- [ ] Add navigation link from login page

**Test Results:**
```
Test ID: TC-1.1
Status: ⬜ Not Tested
Notes:
```

---

### UC-1.2: Register New User
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-1.1 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.1.2 |

**Implementation Tasks:**
- [ ] Create `UserLogin` entity
- [ ] Create `UserLoginRepository` with username/email lookups
- [ ] Implement registration endpoint with BCrypt hashing
- [ ] Add field validation (username 3-255 chars, email format, password 8-64 chars)
- [ ] Implement case-insensitive duplicate checking
- [ ] Create Person record linked to UserLogin

**Test Results:**
```
Test ID: TC-1.2
Status: ⬜ Not Tested
Notes:
```

---

### UC-1.4: Authenticate User
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-1.2 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.1.4 |

**Implementation Tasks:**
- [ ] Implement `IdentityProvider` for form authentication
- [ ] Configure Quarkus Security with `/j_security_check`
- [ ] Implement failed login tracking (5 attempts → 15 min lockout)
- [ ] Add progressive lockout duration
- [ ] Redirect to home page on success with personalized greeting

**Test Results:**
```
Test ID: TC-1.4
Status: ⬜ Not Tested
Notes:
```

---

### UC-1.5: Logout User
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-1.4 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.1.5 |

**Implementation Tasks:**
- [ ] Implement logout endpoint
- [ ] Invalidate session and clear cookies
- [ ] Create logout confirmation page
- [ ] Update navigation to show authenticated/unauthenticated state

**Test Results:**
```
Test ID: TC-1.5
Status: ⬜ Not Tested
Notes:
```

---

### UC-1.6: Access Protected Route (Unauthenticated)
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-1.4 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.1.6 |

**Implementation Tasks:**
- [ ] Configure `@RolesAllowed` annotations on protected resources
- [ ] Configure redirect to login for unauthenticated access
- [ ] Test protection on `/persons` and `/gender` routes

**Test Results:**
```
Test ID: TC-1.6
Status: ⬜ Not Tested
Notes:
```

---

## Phase 2: Gender Master Data

Gender is a reference table required by Person. Must be implemented before Person management.

### UC-2.1: View Gender List
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-1.6 (requires admin role protection) |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.2.1 |

**Implementation Tasks:**
- [ ] Create `Gender` entity with audit fields
- [ ] Create `GenderRepository` extending `PanacheRepository`
- [ ] Create `GenderResource` with `@RolesAllowed("admin")`
- [ ] Create `templates/GenderResource/gender.html` with table layout
- [ ] Add Gender link to Maintenance menu (admin only)
- [ ] Display "No gender entries found" when empty

**Test Results:**
```
Test ID: TC-2.1
Status: ⬜ Not Tested
Notes:
```

---

### UC-2.2: Create Gender
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-2.1 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.2.2 |

**Implementation Tasks:**
- [ ] Add create form endpoint (GET `/gender/new`)
- [ ] Create inline form template partial
- [ ] Implement POST endpoint with validation
- [ ] Code: max 7 chars, coerce to uppercase
- [ ] Validate uniqueness of code and description
- [ ] Set audit fields (createdBy, createdAt)
- [ ] HTMX swap to update table on success

**Test Results:**
```
Test ID: TC-2.2
Status: ⬜ Not Tested
Notes:
```

---

### UC-2.3: Edit Gender
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-2.2 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.2.3 |

**Implementation Tasks:**
- [ ] Add edit form endpoint (GET `/gender/{id}/edit`)
- [ ] Create inline edit form template partial
- [ ] Pre-populate form with current values
- [ ] Display audit fields (read-only)
- [ ] Implement PUT endpoint with validation
- [ ] Validate uniqueness excluding current record
- [ ] Update audit fields (updatedBy, updatedAt)

**Test Results:**
```
Test ID: TC-2.3
Status: ⬜ Not Tested
Notes:
```

---

### UC-2.4: Delete Gender
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-2.3 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.2.4 |

**Implementation Tasks:**
- [ ] Implement DELETE endpoint
- [ ] Add confirmation dialog (UIkit modal or HTMX confirm)
- [ ] Check for Person references before deletion
- [ ] Display "Cannot delete: Gender is in use" if referenced
- [ ] HTMX swap to remove row on success

**Test Results:**
```
Test ID: TC-2.4
Status: ⬜ Not Tested
Notes:
```

---

## Phase 3: Persons Management

Persons management depends on Gender for the dropdown selection.

### UC-3.1: View Persons List
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-2.1 (Gender must exist for display) |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.3.1 |

**Implementation Tasks:**
- [ ] Create `Person` entity with Gender FK
- [ ] Create `PersonRepository` with sort by lastName, firstName
- [ ] Create `PersonResource` with `@RolesAllowed({"user", "admin"})`
- [ ] Create `templates/PersonResource/persons.html`
- [ ] Display table: firstName, lastName, email, phone, dateOfBirth, gender
- [ ] Add Persons link to main navigation
- [ ] Display "No persons found" when empty

**Test Results:**
```
Test ID: TC-3.1
Status: ⬜ Not Tested
Notes:
```

---

### UC-3.2: Create Person
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-3.1, UC-2.1 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.3.2 |

**Implementation Tasks:**
- [ ] Add create form endpoint (GET `/persons/new`)
- [ ] Create inline form template partial
- [ ] Populate Gender dropdown from repository
- [ ] Implement POST endpoint with validation
- [ ] Required: firstName, lastName, email
- [ ] Optional: phone, dateOfBirth, gender
- [ ] Normalize email to lowercase
- [ ] Validate email uniqueness
- [ ] Set audit fields

**Test Results:**
```
Test ID: TC-3.2
Status: ⬜ Not Tested
Notes:
```

---

### UC-3.3: Edit Person
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-3.2 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.3.3 |

**Implementation Tasks:**
- [ ] Add edit form endpoint (GET `/persons/{id}/edit`)
- [ ] Create inline edit form template partial
- [ ] Pre-populate form with current values
- [ ] Display audit fields (read-only)
- [ ] Implement PUT endpoint with validation
- [ ] Validate email uniqueness excluding current record
- [ ] Update audit fields

**Test Results:**
```
Test ID: TC-3.3
Status: ⬜ Not Tested
Notes:
```

---

### UC-3.4: Delete Person
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-3.3 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.3.4 |

**Implementation Tasks:**
- [ ] Implement DELETE endpoint
- [ ] Add confirmation dialog
- [ ] Cascade delete to UserLogin if exists
- [ ] HTMX swap to remove row on success

**Test Results:**
```
Test ID: TC-3.4
Status: ⬜ Not Tested
Notes:
```

---

### UC-3.5: Filter Persons
| Attribute | Value |
|-----------|-------|
| Status | 🔲 Not Started |
| Dependencies | UC-3.1 |
| Spec Reference | `SYSTEM-SPECIFICATION.md` Section 4.3.5 |

**Implementation Tasks:**
- [ ] Add filter panel to persons list template
- [ ] Implement filter endpoint with search parameter
- [ ] Match against firstName OR lastName (case-insensitive)
- [ ] Persist filter in session
- [ ] Add Clear button to reset filter
- [ ] Display "No persons match the filter criteria" when empty

**Test Results:**
```
Test ID: TC-3.5
Status: ⬜ Not Tested
Notes:
```

---

## Progress Summary

| Phase | Use Cases | Completed | Remaining |
|-------|-----------|-----------|-----------|
| Phase 0: Foundation | 2 | 2 | 0 |
| Phase 1: Authentication | 6 | 0 | 6 |
| Phase 2: Gender | 4 | 0 | 4 |
| Phase 3: Persons | 5 | 0 | 5 |
| **Total** | **17** | **2** | **15** |

---

## Current Status

**Current Phase:** Phase 1 - Authentication
**Next Use Case:** UC-1.3 - Display Login Page
**Blockers:** None

---

## Dependency Graph

```
P0.1 Project Scaffolding
  └── P0.2 Database Schema
        └── UC-1.3 Display Login Page
              └── UC-1.1 Display Signup Page
                    └── UC-1.2 Register New User
                          └── UC-1.4 Authenticate User
                                ├── UC-1.5 Logout User
                                └── UC-1.6 Access Protected Route
                                      └── UC-2.1 View Gender List
                                            ├── UC-2.2 Create Gender
                                            │     └── UC-2.3 Edit Gender
                                            │           └── UC-2.4 Delete Gender
                                            └── UC-3.1 View Persons List
                                                  ├── UC-3.2 Create Person
                                                  │     └── UC-3.3 Edit Person
                                                  │           └── UC-3.4 Delete Person
                                                  └── UC-3.5 Filter Persons
```

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-27 | Senior Developer | Initial plan created |
