# Implementation Plan for Feature 003 - Person Management

## Current Status

**Current Use Case:** UC-003-01-01: View Persons List
**Status:** 🔲 Not Started
**Blockers:** Requires Feature 002 (Gender entity) to be complete

---

## Progress Summary

| Use Case | Status |
|----------|--------|
| UC-003-01-01: View Persons List | 🔲 Not Started |
| UC-003-02-01: Display Create Form | 🔲 Not Started |
| UC-003-02-02: Submit Create Form | 🔲 Not Started |
| UC-003-03-01: Display Edit Form | 🔲 Not Started |
| UC-003-03-02: Submit Edit Form | 🔲 Not Started |
| UC-003-03-03: Cancel Edit | 🔲 Not Started |
| UC-003-04-01: Delete Person | 🔲 Not Started |
| UC-003-05-01: Apply Filter | 🔲 Not Started |
| UC-003-05-02: Clear Filter | 🔲 Not Started |
| UC-003-06-01: Apply Sort | 🔲 Not Started |
| UC-003-06-02: Clear Sort | 🔲 Not Started |

---

## UC-003-01-01: View Persons List

**Status:** 🔲 Not Started
**Parent Story:** US-003-01 - View Persons List

**Description:** Display a list of all persons with filter and sort panels.

**Implementation Tasks:**
- [ ] Create `entity/Person.java` extending PanacheEntity
- [ ] Create migration `V1.1.0__Create_person_table.sql`
- [ ] Add `@ManyToOne` relationship to Gender
- [ ] Add static finder methods (`findByEmail`, `listAllOrdered`, `findByNameContaining`)
- [ ] Add `@PrePersist` and `@PreUpdate` lifecycle callbacks for email normalization
- [ ] Create `router/PersonResource.java` with `@RolesAllowed({"user", "admin"})`
- [ ] Add `Templates` class with `persons()` method
- [ ] Add `Partials` class with `basePath = "partials"`
- [ ] Implement `GET /persons` endpoint with HTMX detection
- [ ] Create `templates/PersonResource/persons.html` (full page)
- [ ] Create `templates/partials/persons_table.html` (table partial)
- [ ] Create `templates/partials/person_row.html` (row display mode)
- [ ] Update `templates/base.html` sidebar with Persons menu item

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons` | List all persons |

**Test Results:**
- Test ID: TC-003-01-001, TC-003-01-002, TC-003-01-003, TC-003-01-004
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-02-01: Display Create Form

**Status:** 🔲 Not Started
**Parent Story:** US-003-02 - Create New Person

**Description:** Display inline create form when Add button is clicked.

**Implementation Tasks:**
- [ ] Add `createForm()` endpoint to PersonResource
- [ ] Add `createFormCancel()` endpoint to PersonResource
- [ ] Add `Partials.person_create_form()` template method
- [ ] Add `Partials.person_create_button()` template method
- [ ] Create `templates/partials/person_create_form.html`
- [ ] Create `templates/partials/person_create_button.html`
- [ ] Include gender dropdown populated from Gender.listAllOrdered()
- [ ] Include date picker for dateOfBirth field

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/create` | Display create form |
| GET | `/persons/create/cancel` | Return Add button |

**Test Results:**
- Test ID: TC-003-02-001
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-02-02: Submit Create Form

**Status:** 🔲 Not Started
**Parent Story:** US-003-02 - Create New Person

**Description:** Validate and create new person record.

**Implementation Tasks:**
- [ ] Implement `POST /persons/create` endpoint
- [ ] Validate email is not empty
- [ ] Validate email format (regex: `^[^@\s]+@[^@\s]+\.[^@\s]+$`)
- [ ] Validate email uniqueness (case-insensitive)
- [ ] Normalize email to lowercase
- [ ] Set audit fields (createdBy, updatedBy from SecurityIdentity)
- [ ] Link gender if genderId provided
- [ ] Create `templates/partials/person_success.html` with OOB table refresh
- [ ] Create `templates/partials/person_error.html`

**Validation Rules:**
| Field | Rule | Error Message |
|-------|------|---------------|
| email | Required | "Email is required." |
| email | Valid format | "Invalid email format." |
| email | Unique | "Email already registered." |

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/persons/create` | Create new person |

**Test Results:**
- Test ID: TC-003-02-002, TC-003-02-003, TC-003-02-004, TC-003-02-005
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-03-01: Display Edit Form

**Status:** 🔲 Not Started
**Parent Story:** US-003-03 - Edit Existing Person

**Description:** Display inline edit form in place of row when Edit button is clicked.

**Implementation Tasks:**
- [ ] Implement `GET /persons/{id}/edit` endpoint
- [ ] Add `Partials.person_row_edit()` template method
- [ ] Create `templates/partials/person_row_edit.html`
- [ ] Pre-populate form with current values
- [ ] Include gender dropdown with current selection
- [ ] Display audit fields (read-only)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/{id}/edit` | Display edit form |

**Test Results:**
- Test ID: TC-003-03-001
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-03-02: Submit Edit Form

**Status:** 🔲 Not Started
**Parent Story:** US-003-03 - Edit Existing Person

**Description:** Validate and update existing person record.

**Implementation Tasks:**
- [ ] Implement `POST /persons/{id}/update` endpoint
- [ ] Validate email is not empty
- [ ] Validate email format
- [ ] Validate email uniqueness (excluding current record)
- [ ] Normalize email to lowercase
- [ ] Update audit fields (updatedBy, updatedAt)
- [ ] Return updated `person_row.html` on success
- [ ] Return `person_row_edit.html` with error on validation failure

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/persons/{id}/update` | Update person |

**Test Results:**
- Test ID: TC-003-03-002, TC-003-03-003
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-03-03: Cancel Edit

**Status:** 🔲 Not Started
**Parent Story:** US-003-03 - Edit Existing Person

**Description:** Cancel edit and return to display row.

**Implementation Tasks:**
- [ ] Implement `GET /persons/{id}` endpoint to return row partial
- [ ] Return `person_row.html` with original values

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/{id}` | Get row partial (cancel) |

**Test Results:**
- Test ID: TC-003-03-004
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-04-01: Delete Person

**Status:** 🔲 Not Started
**Parent Story:** US-003-04 - Delete Person

**Description:** Delete person record with confirmation.

**Implementation Tasks:**
- [ ] Implement `DELETE /persons/{id}` endpoint
- [ ] Delete record and return empty response
- [ ] HTMX `hx-confirm` handles browser confirmation
- [ ] HTMX `hx-swap="delete"` removes row from DOM

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| DELETE | `/persons/{id}` | Delete person |

**Test Results:**
- Test ID: TC-003-04-001, TC-003-04-002, TC-003-04-003
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-05-01: Apply Filter

**Status:** 🔲 Not Started
**Parent Story:** US-003-05 - Filter People

**Description:** Filter persons list by firstName or lastName.

**Implementation Tasks:**
- [ ] Add `@QueryParam("filter")` to list endpoint
- [ ] Inject `RoutingContext` for session access
- [ ] Store filter in session (`persons.filter`)
- [ ] Add filter form to `persons.html` with debounced input
- [ ] Use `hx-sync="closest form:abort"` for request synchronization
- [ ] Query using `Person.findByNameContaining()`

**Filter Form Attributes:**
- `hx-get="/persons"`
- `hx-target="#persons-table-container"`
- `hx-push-url="true"`
- `hx-sync="this:replace"`

**Test Results:**
- Test ID: TC-003-05-001, TC-003-05-002, TC-003-05-004
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-05-02: Clear Filter

**Status:** 🔲 Not Started
**Parent Story:** US-003-05 - Filter People

**Description:** Clear filter and display all persons.

**Implementation Tasks:**
- [ ] Add `@QueryParam("clear")` to list endpoint
- [ ] Remove filter from session when `clear=true`
- [ ] Return full list ordered by default

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons?clear=true` | Clear filter |

**Test Results:**
- Test ID: TC-003-05-003
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-06-01: Apply Sort

**Status:** 🔲 Not Started
**Parent Story:** US-003-06 - Sort People

**Description:** Sort persons list by selected field.

**Implementation Tasks:**
- [ ] Add `@QueryParam("sortField")` and `@QueryParam("sortDir")` to list endpoint
- [ ] Store sort criteria in session (`persons.sortField`, `persons.sortDir`)
- [ ] Add sort panel to `persons.html`
- [ ] Implement `Person.listOrderedBy(field, direction)`
- [ ] Support fields: firstName, lastName
- [ ] Support directions: asc, desc

**Test Results:**
- Test ID: TC-003-06-001, TC-003-06-003
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-06-02: Clear Sort

**Status:** 🔲 Not Started
**Parent Story:** US-003-06 - Sort People

**Description:** Clear custom sort and restore default order.

**Implementation Tasks:**
- [ ] Add `@QueryParam("clearSort")` to list endpoint
- [ ] Remove sort criteria from session when `clearSort=true`
- [ ] Return list with default order (lastName, firstName ASC)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons?clearSort=true` | Clear sort |

**Test Results:**
- Test ID: TC-003-06-002
- Status: 🔲 Not Tested
- Notes:

---

## Test Cases Reference

### Feature 003 Test Cases

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-003-01-001 | Persons Page UI Elements | UC-003-01-01 | 🔲 |
| TC-003-01-002 | Persons List Display | UC-003-01-01 | 🔲 |
| TC-003-01-003 | Persons List Empty State | UC-003-01-01 | 🔲 |
| TC-003-01-004 | Persons Access Requires Authentication | UC-003-01-01 | 🔲 |
| TC-003-02-001 | Person Create Form Display | UC-003-02-01 | 🔲 |
| TC-003-02-002 | Person Create Success | UC-003-02-02 | 🔲 |
| TC-003-02-003 | Person Create Email Required | UC-003-02-02 | 🔲 |
| TC-003-02-004 | Person Create Email Format Validation | UC-003-02-02 | 🔲 |
| TC-003-02-005 | Person Create Duplicate Email Prevention | UC-003-02-02 | 🔲 |
| TC-003-03-001 | Person Edit Form Display | UC-003-03-01 | 🔲 |
| TC-003-03-002 | Person Edit Success | UC-003-03-02 | 🔲 |
| TC-003-03-003 | Person Edit Email Uniqueness | UC-003-03-02 | 🔲 |
| TC-003-03-004 | Person Edit Cancel | UC-003-03-03 | 🔲 |
| TC-003-04-001 | Person Delete Confirmation | UC-003-04-01 | 🔲 |
| TC-003-04-002 | Person Delete Success | UC-003-04-01 | 🔲 |
| TC-003-04-003 | Person Delete Cancel | UC-003-04-01 | 🔲 |
| TC-003-05-001 | Persons Filter by Name | UC-003-05-01 | 🔲 |
| TC-003-05-002 | Persons Filter No Results | UC-003-05-01 | 🔲 |
| TC-003-05-003 | Persons Filter Clear | UC-003-05-02 | 🔲 |
| TC-003-05-004 | Persons Filter Persistence | UC-003-05-01 | 🔲 |
| TC-003-06-001 | Persons Sort by Name | UC-003-06-01 | 🔲 |
| TC-003-06-002 | Persons Sort Clear | UC-003-06-02 | 🔲 |
| TC-003-06-003 | Persons Sort Persistence | UC-003-06-01 | 🔲 |

---
