# Implementation Plan for Feature 004 - Relationship Management

## Current Status

**Current Use Case:** UC-004-03-01: Display Edit Form
**Status:** 🔲 Not Started
**Blockers:** None

---

## Progress Summary

| Use Case | Status |
|----------|--------|
| UC-004-01-01: View Relationship List | ✅ Complete |
| UC-004-02-01: Display Create Form | ✅ Complete |
| UC-004-02-02: Submit Create Form | ✅ Complete |
| UC-004-03-01: Display Edit Form | 🔲 Not Started |
| UC-004-03-02: Submit Edit Form | 🔲 Not Started |
| UC-004-03-03: Cancel Edit | 🔲 Not Started |
| UC-004-04-01: Delete Relationship | 🔲 Not Started |

---

## UC-004-01-01: View Relationship List

**Status:** ✅ Complete
**Parent Story:** US-004-01 - View Relationship Master Data

**Description:** Display a list of all relationship codes and descriptions with admin-only access.

**Implementation Tasks:**
- [x] Create migration `V1.5.0__Create_relationship_table.sql` with relationship table
- [x] Create migration `V1.5.1__Insert_relationship_data.sql` with seed data
- [x] Create `entity/Relationship.java` extending PanacheEntityBase with audit fields
- [x] Add static finder methods (`findByCode`, `findByDescription`, `listAllOrdered`)
- [x] Add `@PrePersist` and `@PreUpdate` lifecycle callbacks
- [x] Create `router/RelationshipResource.java` with `@RolesAllowed("admin")`
- [x] Add `Templates` class with `relationship()` and fragment methods
- [x] Implement `GET /relationships` endpoint with HTMX detection
- [x] Create `templates/RelationshipResource/relationship.html` with fragments
- [x] Update `templates/base.html` sidebar with Maintenance > Relationship menu item
- [x] Update `application.properties` to add `/relationships/*` to admin routes

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/relationships` | List all relationships |

**Test Results:**

| Test ID | Test Name | Status | Notes |
|---------|-----------|--------|-------|
| TC-004-01-001 | Relationship Page UI Elements | ✅ PASS | Page title, table columns, Add button, navigation all correct |
| TC-004-01-002 | Relationship List Display | ✅ PASS | 6 relationships displayed, sorted by code ASC, Edit/Delete buttons present |

---

## UC-004-02-01: Display Create Form

**Status:** ✅ Complete
**Parent Story:** US-004-02 - Create New Relationship

**Description:** Display modal create form when Add button is clicked.

**Implementation Tasks:**
- [x] Add `createForm()` endpoint to RelationshipResource
- [x] Add `Templates.relationship$modal_create()` template method
- [x] Add `modal_create` fragment to `relationship.html`
- [x] Update main page with static modal shell and HTMX dynamic loading

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/relationships/create` | Display create form modal |

**Test Results:**

| Test ID | Test Name | Status | Notes |
|---------|-----------|--------|-------|
| TC-004-02-001 | Relationship Create Form Modal | ✅ PASS | Modal opens with title, code/description fields, Save/Cancel buttons |

---

## UC-004-02-02: Submit Create Form

**Status:** ✅ Complete
**Parent Story:** US-004-02 - Create New Relationship

**Description:** Validate and create new relationship record.

**Implementation Tasks:**
- [x] Implement `POST /relationships` endpoint
- [x] Validate code is not empty
- [x] Validate description is not empty
- [x] Validate code max length (10 chars)
- [x] Validate code uniqueness
- [x] Validate description uniqueness
- [x] Coerce code to uppercase
- [x] Set audit fields (createdBy, updatedBy from SecurityIdentity)
- [x] Add `modal_success` fragment with OOB table refresh
- [x] Add `Templates.relationship$modal_success()` template method

**Validation Rules:**
| Field | Rule | Error Message |
|-------|------|---------------|
| code | Required | "Code is required." |
| code | Max 10 chars | "Code must be at most 10 characters." |
| code | Unique | "Code already exists." |
| description | Required | "Description is required." |
| description | Unique | "Description already exists." |

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/relationships` | Create new relationship |

**Test Results:**

| Test ID | Test Name | Status | Notes |
|---------|-----------|--------|-------|
| TC-004-02-002 | Relationship Create Success | ✅ PASS | Modal closes, COUSIN appears in list sorted correctly |
| TC-004-02-003 | Relationship Code Uppercase | ✅ PASS | Entered "cousin", displayed as "COUSIN" |
| TC-004-02-004 | Relationship Duplicate Code | ✅ PASS | Error "Code already exists." shown, modal stays open |
| TC-004-02-005 | Relationship Duplicate Desc | ✅ PASS | Error "Description already exists." shown |
| TC-004-02-006 | Relationship Code Max Length | ✅ PASS | HTML maxlength=10 prevents entry, server validates too |

---

## UC-004-03-01: Display Edit Form

**Status:** 🔲 Not Started
**Parent Story:** US-004-03 - Edit Existing Relationship

**Description:** Display edit form modal with pre-populated data when Edit button is clicked.

**Implementation Tasks:**
- [ ] Implement `GET /relationships/{id}/edit` endpoint
- [ ] Add `Templates.relationship$modal_edit()` template method
- [ ] Add `modal_edit` fragment to `relationship.html`
- [ ] Pre-populate form with existing values
- [ ] Display audit fields (read-only) in collapsible details section

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/relationships/{id}/edit` | Display edit form modal |

**Test Results:**
- Test ID: TC-004-03-001
- Status: 🔲 Not Tested
- Notes:

---

## UC-004-03-02: Submit Edit Form

**Status:** 🔲 Not Started
**Parent Story:** US-004-03 - Edit Existing Relationship

**Description:** Validate and update existing relationship record.

**Implementation Tasks:**
- [ ] Implement `PUT /relationships/{id}` endpoint
- [ ] Validate code is not empty
- [ ] Validate description is not empty
- [ ] Validate code uniqueness (excluding current record)
- [ ] Validate description uniqueness (excluding current record)
- [ ] Coerce code to uppercase
- [ ] Update audit fields (updatedBy, updatedAt)
- [ ] Add `modal_success_row` fragment with OOB single row update
- [ ] Add `Templates.relationship$modal_success_row()` template method

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| PUT | `/relationships/{id}` | Update existing relationship |

**Test Results:**
- Test ID: TC-004-03-002
- Status: 🔲 Not Tested
- Notes:

---

## UC-004-03-03: Cancel Edit

**Status:** 🔲 Not Started
**Parent Story:** US-004-03 - Edit Existing Relationship

**Description:** Cancel edit operation and close modal without making changes.

**Implementation Tasks:**
- [ ] Cancel button uses `uk-modal-close` class (no server request needed)
- [ ] Verify original values preserved in table row after cancel

**Test Results:**
- Test ID: TC-004-03-003
- Status: 🔲 Not Tested
- Notes: No server-side implementation required - uses UIkit modal close

---

## UC-004-04-01: Delete Relationship

**Status:** 🔲 Not Started
**Parent Story:** US-004-04 - Delete Relationship

**Description:** Delete relationship record with confirmation dialog.

**Implementation Tasks:**
- [ ] Implement `GET /relationships/{id}/delete` endpoint for confirmation modal
- [ ] Implement `DELETE /relationships/{id}` endpoint
- [ ] Add `modal_delete` fragment for confirmation dialog
- [ ] Add `modal_delete_success` fragment with OOB row removal
- [ ] Add check for relationship in use by Person records
- [ ] Add `Templates.relationship$modal_delete()` template method
- [ ] Add `Templates.relationship$modal_delete_success()` template method

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/relationships/{id}/delete` | Display delete confirmation modal |
| DELETE | `/relationships/{id}` | Execute deletion |

**Test Results:**
- Test ID: TC-004-04-001, TC-004-04-002, TC-004-04-003
- Status: 🔲 Not Tested
- Notes:

---

## Test Cases Reference

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-004-01-001 | Relationship Page UI | UC-004-01-01 | ✅ |
| TC-004-01-002 | Relationship List Display | UC-004-01-01 | ✅ |
| TC-004-01-003 | Relationship List Empty | UC-004-01-01 | 🔲 |
| TC-004-01-004 | Relationship Admin Role | UC-004-01-01 | 🔲 |
| TC-004-02-001 | Relationship Create Form Modal | UC-004-02-01 | ✅ |
| TC-004-02-002 | Relationship Create Success | UC-004-02-02 | ✅ |
| TC-004-02-003 | Relationship Code Uppercase | UC-004-02-02 | ✅ |
| TC-004-02-004 | Relationship Duplicate Code | UC-004-02-02 | ✅ |
| TC-004-02-005 | Relationship Duplicate Desc | UC-004-02-02 | ✅ |
| TC-004-02-006 | Relationship Code Max Length | UC-004-02-02 | ✅ |
| TC-004-03-001 | Relationship Edit Form Modal | UC-004-03-01 | 🔲 |
| TC-004-03-002 | Relationship Edit Success | UC-004-03-02 | 🔲 |
| TC-004-03-003 | Relationship Edit Cancel | UC-004-03-03 | 🔲 |
| TC-004-04-001 | Relationship Delete Confirm Modal | UC-004-04-01 | 🔲 |
| TC-004-04-002 | Relationship Delete Success | UC-004-04-01 | 🔲 |
| TC-004-04-003 | Relationship Delete Cancel | UC-004-04-01 | 🔲 |

---

## Status Legend

| Symbol | Meaning |
|--------|---------|
| 🔲 | Not Started |
| 🔄 | In Progress |
| ✅ | Complete |
| ❌ | Blocked |
