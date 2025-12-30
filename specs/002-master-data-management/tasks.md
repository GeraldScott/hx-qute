# Implementation Plan for Feature 002 - Master Data Management

## Current Status

**Current Use Case:** UC-002-03-03: Cancel Edit
**Status:** 🔲 Not Started
**Blockers:** None

---

## Progress Summary

| Use Case | Status |
|----------|--------|
| UC-002-01-01: View Gender List | ✅ Complete |
| UC-002-02-01: Display Create Form | ✅ Complete |
| UC-002-02-02: Submit Create Form | ✅ Complete |
| UC-002-03-01: Display Edit Form | ✅ Complete |
| UC-002-03-02: Submit Edit Form | ✅ Complete |
| UC-002-03-03: Cancel Edit | 🔲 Not Started |
| UC-002-04-01: Delete Gender | 🔲 Not Started |

---

## UC-002-01-01: View Gender List

**Status:** ✅ Complete
**Parent Story:** US-002-01 - View Gender Master Data

**Description:** Display a list of all gender codes and descriptions with admin-only access.

**Implementation Tasks:**
- [x] Update `entity/Gender.java` to extend PanacheEntity with audit fields
- [x] ~~Create migration `V1.0.2__Add_gender_audit_fields.sql` for audit columns~~ (already exists in V1.0.0)
- [x] Add static finder methods (`findByCode`, `findByDescription`, `listAllOrdered`)
- [x] Add `@PrePersist` and `@PreUpdate` lifecycle callbacks
- [x] Delete `repository/GenderRepository.java` (use Active Record pattern)
- [x] Update `router/GenderResource.java` with `@RolesAllowed("admin")`
- [x] Remove repository injection, use Gender entity static methods
- [x] Add `Templates` class with `gender()` and fragment methods
- [x] ~~Add `Partials` class with `basePath = "partials"`~~ (using fragments instead)
- [x] Implement `GET /genders` endpoint with HTMX detection
- [x] Update `templates/GenderResource/gender.html` with fragments
- [x] ~~Create `templates/partials/gender_table.html`~~ (using $table fragment)
- [x] ~~Create `templates/partials/gender_row.html`~~ (using $row fragment)
- [x] Verify `templates/base.html` sidebar has Maintenance > Gender menu item

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/genders` | List all genders |

**Test Results:**
- Test ID: TC-002-01-001, TC-002-01-002
- Status: ✅ Passed
- Notes: TC-002-01-003 (empty state) and TC-002-01-004 (non-admin access) require specific test data setup - deferred to integration tests

---

## UC-002-02-01: Display Create Form

**Status:** ✅ Complete
**Parent Story:** US-002-02 - Create New Gender

**Description:** Display inline create form when Add button is clicked.

**Implementation Tasks:**
- [x] Add `createForm()` endpoint to GenderResource
- [x] Add `Templates.gender$modal_create()` template method
- [x] Add `modal_create` fragment to `gender.html`
- [x] Update main page with static modal shell and HTMX dynamic loading

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/genders/create` | Display create form |
| GET | `/genders/create/cancel` | Return Add button |

**Test Results:**
- Test ID: TC-002-02-001
- Status: ✅ Passed
- Notes: Modal displays correctly with title "Add Gender", Code and Description input fields, Save and Cancel buttons. Modal backdrop configured with bg-close: false.

---

## UC-002-02-02: Submit Create Form

**Status:** ✅ Complete
**Parent Story:** US-002-02 - Create New Gender

**Description:** Validate and create new gender record.

**Implementation Tasks:**
- [x] Implement `POST /genders` endpoint
- [x] Validate code is not empty
- [x] Validate description is not empty
- [x] Validate code max length (1 char)
- [x] Validate code uniqueness
- [x] Validate description uniqueness
- [x] Coerce code to uppercase
- [x] Set audit fields (createdBy, updatedBy from SecurityIdentity)
- [x] Add `modal_success` fragment with OOB table refresh
- [x] Add `Templates.gender$modal_success()` template method

**Validation Rules:**
| Field | Rule | Error Message |
|-------|------|---------------|
| code | Required | "Code is required." |
| code | Max 1 char | "Code must be 1 character." |
| code | Unique | "Code already exists." |
| description | Required | "Description is required." |
| description | Unique | "Description already exists." |

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/genders/create` | Create new gender |

**Test Results:**
- Test ID: TC-002-02-002, TC-002-02-003, TC-002-02-004, TC-002-02-005, TC-002-02-006
- Status: ✅ All Passed
- Notes: All validation rules working correctly. Uppercase coercion, uniqueness checks, and max length validation all functioning as expected.

---

## UC-002-03-01: Display Edit Form

**Status:** ✅ Complete
**Parent Story:** US-002-03 - Edit Existing Gender

**Description:** Display edit form modal with pre-populated data when Edit button is clicked.

**Implementation Tasks:**
- [x] Implement `GET /genders/{id}/edit` endpoint
- [x] Add `Templates.gender$modal_edit()` template method
- [x] Add `modal_edit` fragment to `gender.html`
- [x] Pre-populate form with current values
- [x] Display audit fields (read-only) in expandable section
- [x] Add Edit and Delete buttons to table rows

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/genders/{id}/edit` | Display edit form modal content |

**Test Results:**
- Test ID: TC-002-03-001
- Status: ✅ Passed
- Notes: Edit modal opens with title "Edit Gender", Code and Description fields pre-populated with current values, Audit Information section (expandable), Save and Cancel buttons visible.

---

## UC-002-03-02: Submit Edit Form

**Status:** ✅ Complete
**Parent Story:** US-002-03 - Edit Existing Gender

**Description:** Validate and update existing gender record.

**Implementation Tasks:**
- [x] Implement `PUT /genders/{id}` endpoint
- [x] Validate code is not empty
- [x] Validate description is not empty
- [x] Validate code uniqueness (excluding current record)
- [x] Validate description uniqueness (excluding current record)
- [x] Coerce code to uppercase
- [x] Update audit fields (updatedBy, updatedAt)
- [x] Add `modal_success_row` fragment with OOB row update
- [x] Add `Templates.gender$modal_success_row()` template method
- [x] Fix template fragments with `rendered=false` for standalone fragments

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| PUT | `/genders/{id}` | Update gender |

**Test Results:**
- Test ID: TC-002-03-002
- Status: ✅ Passed
- Notes: Edit modal opens with pre-populated data, description can be modified, Save button submits form via PUT request, modal closes automatically, data persisted to database correctly.

---

## UC-002-03-03: Cancel Edit

**Status:** 🔲 Not Started
**Parent Story:** US-002-03 - Edit Existing Gender

**Description:** Cancel edit and return to display row.

**Implementation Tasks:**
- [ ] Implement `GET /genders/{id}` endpoint to return row partial
- [ ] Return `gender_row.html` with original values

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/genders/{id}` | Get row partial (cancel) |

**Test Results:**
- Test ID: TC-002-03-003
- Status: 🔲 Not Tested
- Notes:

---

## UC-002-04-01: Delete Gender

**Status:** 🔲 Not Started
**Parent Story:** US-002-04 - Delete Gender

**Description:** Delete gender record with confirmation.

**Implementation Tasks:**
- [ ] Implement `DELETE /genders/{id}` endpoint
- [ ] Check if gender is in use by Person records
- [ ] If in use, return error message in `gender_row_edit.html`
- [ ] If not in use, delete record and return empty response
- [ ] HTMX `hx-confirm` handles browser confirmation
- [ ] HTMX `hx-swap="delete"` removes row from DOM

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| DELETE | `/genders/{id}` | Delete gender |

**Test Results:**
- Test ID: TC-002-04-001, TC-002-04-002, TC-002-04-003
- Status: 🔲 Not Tested
- Notes:

---

## Test Cases Reference

### Feature 002 Test Cases

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-002-01-001 | Gender Page UI Elements | UC-002-01-01 | ✅ |
| TC-002-01-002 | Gender List Display | UC-002-01-01 | ✅ |
| TC-002-01-003 | Gender List Empty State | UC-002-01-01 | ⏸️ |
| TC-002-01-004 | Gender Access Requires Admin Role | UC-002-01-01 | ⏸️ |
| TC-002-02-001 | Gender Create Form Display | UC-002-02-01 | ✅ |
| TC-002-02-002 | Gender Create Success | UC-002-02-02 | ✅ |
| TC-002-02-003 | Gender Create Code Uppercase | UC-002-02-02 | ✅ |
| TC-002-02-004 | Gender Create Duplicate Code Prevention | UC-002-02-02 | ✅ |
| TC-002-02-005 | Gender Create Duplicate Description Prevention | UC-002-02-02 | ✅ |
| TC-002-02-006 | Gender Create Code Max Length | UC-002-02-02 | ✅ |
| TC-002-03-001 | Gender Edit Form Display | UC-002-03-01 | ✅ |
| TC-002-03-002 | Gender Edit Success | UC-002-03-02 | ✅ |
| TC-002-03-003 | Gender Edit Cancel | UC-002-03-03 | 🔲 |
| TC-002-04-001 | Gender Delete Confirmation | UC-002-04-01 | 🔲 |
| TC-002-04-002 | Gender Delete Success | UC-002-04-01 | 🔲 |
| TC-002-04-003 | Gender Delete Cancel | UC-002-04-01 | 🔲 |

---
