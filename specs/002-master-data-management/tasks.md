# Implementation Plan for Feature 002 - Master Data Management

## Current Status

**Current Use Case:** UC-002-01-01: View Gender List
**Status:** 🔲 Not Started
**Blockers:** None

---

## Progress Summary

| Use Case | Status |
|----------|--------|
| UC-002-01-01: View Gender List | 🔲 Not Started |
| UC-002-02-01: Display Create Form | 🔲 Not Started |
| UC-002-02-02: Submit Create Form | 🔲 Not Started |
| UC-002-03-01: Display Edit Form | 🔲 Not Started |
| UC-002-03-02: Submit Edit Form | 🔲 Not Started |
| UC-002-03-03: Cancel Edit | 🔲 Not Started |
| UC-002-04-01: Delete Gender | 🔲 Not Started |

---

## UC-002-01-01: View Gender List

**Status:** 🔲 Not Started
**Parent Story:** US-002-01 - View Gender Master Data

**Description:** Display a list of all gender codes and descriptions with admin-only access.

**Implementation Tasks:**
- [ ] Update `entity/Gender.java` to extend PanacheEntity with audit fields
- [ ] Create/update migration `V1.0.0__Create_gender_table.sql` with audit columns
- [ ] Add static finder methods (`findByCode`, `findByDescription`, `listAllOrdered`)
- [ ] Add `@PrePersist` and `@PreUpdate` lifecycle callbacks
- [ ] Update `router/GenderResource.java` with `@RolesAllowed("admin")`
- [ ] Add `Templates` class with `gender()` method
- [ ] Add `Partials` class with `basePath = "partials"`
- [ ] Implement `GET /genders` endpoint with HTMX detection
- [ ] Create `templates/GenderResource/gender.html` (full page)
- [ ] Create `templates/partials/gender_table.html` (table partial)
- [ ] Create `templates/partials/gender_row.html` (row display mode)
- [ ] Update `templates/base.html` sidebar with Maintenance > Gender menu item

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/genders` | List all genders |

**Test Results:**
- Test ID: TC-002-01-001, TC-002-01-002, TC-002-01-003, TC-002-01-004
- Status: 🔲 Not Tested
- Notes:

---

## UC-002-02-01: Display Create Form

**Status:** 🔲 Not Started
**Parent Story:** US-002-02 - Create New Gender

**Description:** Display inline create form when Add button is clicked.

**Implementation Tasks:**
- [ ] Add `createForm()` endpoint to GenderResource
- [ ] Add `createFormCancel()` endpoint to GenderResource
- [ ] Add `Partials.gender_create_form()` template method
- [ ] Add `Partials.gender_create_button()` template method
- [ ] Create `templates/partials/gender_create_form.html`
- [ ] Create `templates/partials/gender_create_button.html`
- [ ] Add `#gender-create-container` to `gender.html`

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/genders/create` | Display create form |
| GET | `/genders/create/cancel` | Return Add button |

**Test Results:**
- Test ID: TC-002-02-001
- Status: 🔲 Not Tested
- Notes:

---

## UC-002-02-02: Submit Create Form

**Status:** 🔲 Not Started
**Parent Story:** US-002-02 - Create New Gender

**Description:** Validate and create new gender record.

**Implementation Tasks:**
- [ ] Implement `POST /genders/create` endpoint
- [ ] Validate code is not empty
- [ ] Validate description is not empty
- [ ] Validate code max length (7 chars)
- [ ] Validate code uniqueness
- [ ] Validate description uniqueness
- [ ] Coerce code to uppercase
- [ ] Set audit fields (createdBy, updatedBy from SecurityIdentity)
- [ ] Create `templates/partials/gender_success.html` with OOB table refresh
- [ ] Create `templates/partials/gender_error.html`

**Validation Rules:**
| Field | Rule | Error Message |
|-------|------|---------------|
| code | Required | "Code is required." |
| code | Max 7 chars | "Code must be 7 characters or less." |
| code | Unique | "Code already exists." |
| description | Required | "Description is required." |
| description | Unique | "Description already exists." |

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/genders/create` | Create new gender |

**Test Results:**
- Test ID: TC-002-02-002, TC-002-02-003, TC-002-02-004, TC-002-02-005, TC-002-02-006
- Status: 🔲 Not Tested
- Notes:

---

## UC-002-03-01: Display Edit Form

**Status:** 🔲 Not Started
**Parent Story:** US-002-03 - Edit Existing Gender

**Description:** Display inline edit form in place of row when Edit button is clicked.

**Implementation Tasks:**
- [ ] Implement `GET /genders/{id}/edit` endpoint
- [ ] Add `Partials.gender_row_edit()` template method
- [ ] Create `templates/partials/gender_row_edit.html`
- [ ] Pre-populate form with current values
- [ ] Display audit fields (read-only)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/genders/{id}/edit` | Display edit form |

**Test Results:**
- Test ID: TC-002-03-001
- Status: 🔲 Not Tested
- Notes:

---

## UC-002-03-02: Submit Edit Form

**Status:** 🔲 Not Started
**Parent Story:** US-002-03 - Edit Existing Gender

**Description:** Validate and update existing gender record.

**Implementation Tasks:**
- [ ] Implement `POST /genders/{id}/update` endpoint
- [ ] Validate code is not empty
- [ ] Validate description is not empty
- [ ] Validate code uniqueness (excluding current record)
- [ ] Validate description uniqueness (excluding current record)
- [ ] Coerce code to uppercase
- [ ] Update audit fields (updatedBy, updatedAt)
- [ ] Return updated `gender_row.html` on success
- [ ] Return `gender_row_edit.html` with error on validation failure

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/genders/{id}/update` | Update gender |

**Test Results:**
- Test ID: TC-002-03-002
- Status: 🔲 Not Tested
- Notes:

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
| TC-002-01-001 | Gender Page UI Elements | UC-002-01-01 | 🔲 |
| TC-002-01-002 | Gender List Display | UC-002-01-01 | 🔲 |
| TC-002-01-003 | Gender List Empty State | UC-002-01-01 | 🔲 |
| TC-002-01-004 | Gender Access Requires Admin Role | UC-002-01-01 | 🔲 |
| TC-002-02-001 | Gender Create Form Display | UC-002-02-01 | 🔲 |
| TC-002-02-002 | Gender Create Success | UC-002-02-02 | 🔲 |
| TC-002-02-003 | Gender Create Code Uppercase | UC-002-02-02 | 🔲 |
| TC-002-02-004 | Gender Create Duplicate Code Prevention | UC-002-02-02 | 🔲 |
| TC-002-02-005 | Gender Create Duplicate Description Prevention | UC-002-02-02 | 🔲 |
| TC-002-02-006 | Gender Create Code Max Length | UC-002-02-02 | 🔲 |
| TC-002-03-001 | Gender Edit Form Display | UC-002-03-01 | 🔲 |
| TC-002-03-002 | Gender Edit Success | UC-002-03-02 | 🔲 |
| TC-002-03-003 | Gender Edit Cancel | UC-002-03-03 | 🔲 |
| TC-002-04-001 | Gender Delete Confirmation | UC-002-04-01 | 🔲 |
| TC-002-04-002 | Gender Delete Success | UC-002-04-01 | 🔲 |
| TC-002-04-003 | Gender Delete Cancel | UC-002-04-01 | 🔲 |

---
