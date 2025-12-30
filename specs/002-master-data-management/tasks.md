# Implementation Plan for Feature 002 - Master Data Management

## Current Status

**Current Use Case:** UC-002-06-02: Submit Create Form
**Status:** 🔲 Not Started
**Blockers:** None

---

## Progress Summary

### Gender Use Cases

| Use Case | Status |
|----------|--------|
| UC-002-01-01: View Gender List | ✅ Complete |
| UC-002-02-01: Display Create Form | ✅ Complete |
| UC-002-02-02: Submit Create Form | ✅ Complete |
| UC-002-03-01: Display Edit Form | ✅ Complete |
| UC-002-03-02: Submit Edit Form | ✅ Complete |
| UC-002-03-03: Cancel Edit | ✅ Complete |
| UC-002-04-01: Delete Gender | ✅ Complete |

### Title Use Cases

| Use Case | Status |
|----------|--------|
| UC-002-05-01: View Title List | ✅ Complete |
| UC-002-06-01: Display Create Form | ✅ Complete |
| UC-002-06-02: Submit Create Form | 🔲 Not Started |
| UC-002-07-01: Display Edit Form | 🔲 Not Started |
| UC-002-07-02: Submit Edit Form | 🔲 Not Started |
| UC-002-07-03: Cancel Edit | 🔲 Not Started |
| UC-002-08-01: Delete Title | 🔲 Not Started |

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

**Status:** ✅ Complete
**Parent Story:** US-002-03 - Edit Existing Gender

**Description:** Cancel edit and close modal, discarding any unsaved changes.

**Implementation Notes:**
With the modal-based architecture, Cancel is handled entirely client-side using UIkit's `uk-modal-close` class. No server endpoint is needed because:
- The original row data is never modified until Save is clicked
- Clicking Cancel simply closes the modal via UIkit
- The table row remains unchanged

**Implementation Tasks:**
- [x] Cancel button uses `uk-modal-close` class in `modal_edit` fragment
- [x] Modal closes without server request
- [x] Original table row data preserved (no modification until Save)

**Endpoints:**
None required - handled client-side via UIkit modal close.

**Test Results:**
- Test ID: TC-002-03-003
- Status: ✅ Passed
- Notes: Modal closes correctly after Cancel. Original value "Female" preserved after modifying to "Female Modified for Test" and clicking Cancel. No notifications displayed.

---

## UC-002-04-01: Delete Gender

**Status:** ✅ Complete
**Parent Story:** US-002-04 - Delete Gender

**Description:** Delete gender record with confirmation modal.

**Implementation Tasks:**
- [x] Implement `GET /genders/{id}/delete` endpoint (confirmation modal)
- [x] Implement `DELETE /genders/{id}` endpoint
- [x] Add `modal_delete` fragment for confirmation dialog
- [x] Add `modal_delete_success` fragment for OOB row removal (uses `<template>` wrapper for proper OOB swap)
- [x] Add `Templates.gender$modal_delete()` and `Templates.gender$modal_delete_success()` methods
- [ ] Check if gender is in use by Person records (deferred - Person entity not yet implemented)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/genders/{id}/delete` | Display delete confirmation modal |
| DELETE | `/genders/{id}` | Execute deletion |

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-002-04-001 | ✅ | Modal displays with title "Delete Gender", warning message with code/description, Delete and Cancel buttons |
| TC-002-04-002 | ✅ | Delete removes row via OOB swap (required `<template>` wrapper for `<tr>` elements) |
| TC-002-04-003 | ✅ | Cancel closes modal, record preserved in table |

**Run Date:** 2025-12-30
**Summary:** 3/3 tests passed

---

# Title Implementation

---

## UC-002-05-01: View Title List

**Status:** ✅ Complete
**Parent Story:** US-002-05 - View Title Master Data

**Description:** Display a list of all title codes and descriptions with admin-only access.

**Implementation Tasks:**
- [x] Create Flyway migration `V1.3.0__Create_title_table.sql`
- [x] Create `entity/Title.java` extending PanacheEntityBase with audit fields
- [x] Add static finder methods (`findByCode`, `findByDescription`, `listAllOrdered`)
- [x] Add `@PrePersist` and `@PreUpdate` lifecycle callbacks
- [x] Create `router/TitleResource.java` with `@RolesAllowed("admin")`
- [x] Add `Templates` class with `title()` and fragment methods
- [x] Implement `GET /titles` endpoint with HTMX detection
- [x] Create `templates/TitleResource/title.html` with fragments
- [x] Add Title to Maintenance menu in `templates/base.html`
- [x] Update security configuration in `application.properties` to include `/titles/*`

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/titles` | List all titles |

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-002-05-001 | ✅ | All UI elements verified |
| TC-002-05-002 | ✅ | Implementation correct, empty state shows "No titles found" |

**Run Date:** 2025-12-30
**Summary:** 2/2 tests passed

---

## UC-002-06-01: Display Create Form

**Status:** ✅ Complete
**Parent Story:** US-002-06 - Create New Title

**Description:** Display modal create form when Add button is clicked.

**Implementation Tasks:**
- [x] Add `createForm()` endpoint to TitleResource
- [x] Add `Templates.title$modal_create()` template method
- [x] Add `modal_create` fragment to `title.html`
- [x] Update main page with static modal shell and HTMX dynamic loading

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/titles/create` | Display create form modal |

**Test Results:**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-002-06-001 | ✅ | Modal displays with title "Add Title", Code input (maxlength=5), Description input, Save and Cancel buttons. Modal backdrop configured with bg-close: false. |

**Run Date:** 2025-12-30
**Summary:** 1/1 tests passed

---

## UC-002-06-02: Submit Create Form

**Status:** 🔲 Not Started
**Parent Story:** US-002-06 - Create New Title

**Description:** Validate and create new title record.

**Implementation Tasks:**
- [ ] Implement `POST /titles` endpoint
- [ ] Validate code is not empty
- [ ] Validate description is not empty
- [ ] Validate code max length (5 chars)
- [ ] Validate code uniqueness
- [ ] Validate description uniqueness
- [ ] Coerce code to uppercase
- [ ] Set audit fields (createdBy, updatedBy from SecurityIdentity)
- [ ] Add `modal_success` fragment with OOB table refresh
- [ ] Add `Templates.title$modal_success()` template method

**Validation Rules:**
| Field | Rule | Error Message |
|-------|------|---------------|
| code | Required | "Code is required." |
| code | Max 5 chars | "Code must be at most 5 characters." |
| code | Unique | "Code already exists." |
| description | Required | "Description is required." |
| description | Unique | "Description already exists." |

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/titles` | Create new title |

**Test Results:**
*(To be completed after implementation)*

---

## UC-002-07-01: Display Edit Form

**Status:** 🔲 Not Started
**Parent Story:** US-002-07 - Edit Existing Title

**Description:** Display edit form modal with pre-populated data when Edit button is clicked.

**Implementation Tasks:**
- [ ] Implement `GET /titles/{id}/edit` endpoint
- [ ] Add `Templates.title$modal_edit()` template method
- [ ] Add `modal_edit` fragment to `title.html`
- [ ] Pre-populate form with current values
- [ ] Display audit fields (read-only) in expandable section
- [ ] Add Edit and Delete buttons to table rows

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/titles/{id}/edit` | Display edit form modal content |

**Test Results:**
*(To be completed after implementation)*

---

## UC-002-07-02: Submit Edit Form

**Status:** 🔲 Not Started
**Parent Story:** US-002-07 - Edit Existing Title

**Description:** Validate and update existing title record.

**Implementation Tasks:**
- [ ] Implement `PUT /titles/{id}` endpoint
- [ ] Validate code is not empty
- [ ] Validate description is not empty
- [ ] Validate code uniqueness (excluding current record)
- [ ] Validate description uniqueness (excluding current record)
- [ ] Coerce code to uppercase
- [ ] Update audit fields (updatedBy, updatedAt)
- [ ] Add `modal_success_row` fragment with OOB row update
- [ ] Add `Templates.title$modal_success_row()` template method

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| PUT | `/titles/{id}` | Update title |

**Test Results:**
*(To be completed after implementation)*

---

## UC-002-07-03: Cancel Edit

**Status:** 🔲 Not Started
**Parent Story:** US-002-07 - Edit Existing Title

**Description:** Cancel edit and close modal, discarding any unsaved changes.

**Implementation Notes:**
With the modal-based architecture, Cancel is handled entirely client-side using UIkit's `uk-modal-close` class. No server endpoint is needed because:
- The original row data is never modified until Save is clicked
- Clicking Cancel simply closes the modal via UIkit
- The table row remains unchanged

**Implementation Tasks:**
- [ ] Cancel button uses `uk-modal-close` class in `modal_edit` fragment
- [ ] Modal closes without server request
- [ ] Original table row data preserved (no modification until Save)

**Endpoints:**
None required - handled client-side via UIkit modal close.

**Test Results:**
*(To be completed after implementation)*

---

## UC-002-08-01: Delete Title

**Status:** 🔲 Not Started
**Parent Story:** US-002-08 - Delete Title

**Description:** Delete title record with confirmation modal.

**Implementation Tasks:**
- [ ] Implement `GET /titles/{id}/delete` endpoint (confirmation modal)
- [ ] Implement `DELETE /titles/{id}` endpoint
- [ ] Add `modal_delete` fragment for confirmation dialog
- [ ] Add `modal_delete_success` fragment for OOB row removal
- [ ] Add `Templates.title$modal_delete()` and `Templates.title$modal_delete_success()` methods
- [ ] Check if title is in use by Person records (deferred - Person entity may not have title field yet)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/titles/{id}/delete` | Display delete confirmation modal |
| DELETE | `/titles/{id}` | Execute deletion |

**Test Results:**
*(To be completed after implementation)*

---

## Test Cases Reference

### Gender Test Cases

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
| TC-002-03-003 | Gender Edit Cancel | UC-002-03-03 | ✅ |
| TC-002-04-001 | Gender Delete Confirmation | UC-002-04-01 | ✅ |
| TC-002-04-002 | Gender Delete Success | UC-002-04-01 | ✅ |
| TC-002-04-003 | Gender Delete Cancel | UC-002-04-01 | ✅ |

### Title Test Cases

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-002-05-001 | Title Page UI Elements | UC-002-05-01 | ✅ |
| TC-002-05-002 | Title List Display | UC-002-05-01 | ✅ |
| TC-002-05-003 | Title List Empty State | UC-002-05-01 | 🔲 |
| TC-002-05-004 | Title Access Requires Admin Role | UC-002-05-01 | 🔲 |
| TC-002-06-001 | Title Create Form Display | UC-002-06-01 | ✅ |
| TC-002-06-002 | Title Create Success | UC-002-06-02 | 🔲 |
| TC-002-06-003 | Title Create Code Uppercase | UC-002-06-02 | 🔲 |
| TC-002-06-004 | Title Create Duplicate Code Prevention | UC-002-06-02 | 🔲 |
| TC-002-06-005 | Title Create Duplicate Description Prevention | UC-002-06-02 | 🔲 |
| TC-002-06-006 | Title Create Code Max Length (5 chars) | UC-002-06-02 | 🔲 |
| TC-002-07-001 | Title Edit Form Display | UC-002-07-01 | 🔲 |
| TC-002-07-002 | Title Edit Success | UC-002-07-02 | 🔲 |
| TC-002-07-003 | Title Edit Cancel | UC-002-07-03 | 🔲 |
| TC-002-08-001 | Title Delete Confirmation | UC-002-08-01 | 🔲 |
| TC-002-08-002 | Title Delete Success | UC-002-08-01 | 🔲 |
| TC-002-08-003 | Title Delete Cancel | UC-002-08-01 | 🔲 |

---
