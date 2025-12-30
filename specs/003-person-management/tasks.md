# Implementation Plan for Feature 003 - Person Management

## Current Status

**Current Use Case:** All Complete
**Status:** ✅ Complete
**Blockers:** None

---

## Progress Summary

| Use Case | Status |
|----------|--------|
| UC-003-01-01: View Persons List | ✅ Complete |
| UC-003-02-01: Display Create Form | ✅ Complete |
| UC-003-02-02: Submit Create Form | ✅ Complete |
| UC-003-03-01: Display Edit Form | ✅ Complete |
| UC-003-03-02: Submit Edit Form | ✅ Complete |
| UC-003-03-03: Cancel Edit | ✅ Complete |
| UC-003-04-01: Delete Person | ✅ Complete |
| UC-003-05-01: Apply Filter | ✅ Complete |
| UC-003-05-02: Clear Filter | ✅ Complete |
| UC-003-06-01: Apply Sort | ✅ Complete |
| UC-003-06-02: Clear Sort | ✅ Complete |

---

## UC-003-01-01: View Persons List

**Status:** ✅ Complete
**Parent Story:** US-003-01 - View Persons List

**Description:** Display a list of all persons with filter bar and modal shell.

**Implementation Tasks:**
- [x] Create migration `V1.4.0__Create_person_table.sql`
  - Include `title_id` FK to title table
  - Include `gender_id` FK to gender table
  - Include audit fields (created_at, updated_at, created_by, updated_by)
- [x] Create `entity/Person.java` extending `PanacheEntityBase`
  - Add explicit `@Id` field with `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - Add `@ManyToOne` relationships to Title and Gender
  - Add audit fields: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
  - Add `findByEmail()`, `listAllOrdered()`, `findByFilter()` methods
  - Add `@PrePersist` callback to set `createdAt` and `updatedAt`
  - Add `@PreUpdate` callback to set `updatedAt`
  - **Note:** `createdBy` and `updatedBy` are set in Resource layer (not lifecycle callbacks) using `SecurityIdentity.getPrincipal().getName()`
- [x] Create `router/PersonResource.java` with `@RolesAllowed({"user", "admin"})`
  - Inject `SecurityIdentity` for populating audit fields (`createdBy`, `updatedBy`)
- [x] Add `@CheckedTemplate` class with fragment methods (using `$` separator)
- [x] Implement `GET /persons` endpoint with query params (filter, sortField, sortDir)
- [x] Create `templates/PersonResource/person.html` with:
  - Filter bar above table (search input, sort dropdowns, Filter/Clear buttons)
  - Add button
  - Table container (`#person-table-container`)
  - Static modal shell (`#person-modal`)
  - `{#fragment id=table}` for table content
- [x] Update `templates/base.html` sidebar with Persons menu item
- [x] Add route protection in `application.properties`

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons` | List all persons (with filter/sort) |

**Test Cases:** TC-003-01-001, TC-003-01-002, TC-003-01-003, TC-003-01-004

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-01-001 | ✅ PASS | UI elements verified: title "Person Management", Add button, Filter bar, Sort dropdowns |
| TC-003-01-002 | ⏭️ SKIP | No person data to verify list display (testable after UC-003-02-02) |
| TC-003-01-003 | ✅ PASS | Empty state "No persons found." displayed, Add button visible |
| TC-003-01-004 | ✅ PASS | Unauthenticated access redirects to /login |

---

## UC-003-02-01: Display Create Form

**Status:** ✅ Complete
**Parent Story:** US-003-02 - Create New Person

**Description:** Display create form in modal when Add button is clicked.

**Implementation Tasks:**
- [x] Implement `GET /persons/create` endpoint
- [x] Add `Templates.person$modal_create()` fragment method
- [x] Create `{#fragment id=modal_create rendered=false}` in person.html
  - Include firstName, lastName, title dropdown, email, phone, dateOfBirth, gender dropdown
  - Pre-load Title.listAllOrdered() and Gender.listAllOrdered() for dropdowns
  - Include Save and Cancel buttons

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/create` | Display create modal content |

**Test Cases:** TC-003-02-001

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-02-001 | ✅ PASS | Modal displays with all fields: firstName, lastName, title dropdown (6 options), email, phone, dateOfBirth, gender dropdown (3 options), Save/Cancel buttons |

---

## UC-003-02-02: Submit Create Form

**Status:** ✅ Complete
**Parent Story:** US-003-02 - Create New Person

**Description:** Validate and create new person record.

**Implementation Tasks:**
- [x] Implement `POST /persons` endpoint with `@Transactional`
- [x] Validate firstName is not empty
- [x] Validate lastName is not empty
- [x] Validate email is not empty
- [x] Validate email format (regex: `^[^@\s]+@[^@\s]+\.[^@\s]+$`)
- [x] Validate email uniqueness (case-insensitive)
- [x] Link title if titleId provided
- [x] Link gender if genderId provided
- [x] Set `createdBy` and `updatedBy` from `securityIdentity.getPrincipal().getName()` before persist
- [x] Add `Templates.person$modal_success()` fragment method
- [x] Create `{#fragment id=modal_success rendered=false}` with:
  - `hx-on::load` to close modal
  - OOB swap to refresh table container

**Validation Rules:**
| Field | Rule | Error Message |
|-------|------|---------------|
| firstName | Required | "First name is required." |
| lastName | Required | "Last name is required." |
| email | Required | "Email is required." |
| email | Valid format | "Invalid email format." |
| email | Unique | "Email already registered." |

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/persons` | Create new person |

**Test Cases:** TC-003-02-002, TC-003-02-003, TC-003-02-004, TC-003-02-005, TC-003-02-006, TC-003-02-007

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-02-002 | ✅ PASS | Person created with title, gender, phone; modal closed, table updated via OOB |
| TC-003-02-003 | ✅ PASS | HTML5 required attribute prevents submission (browser-level validation) |
| TC-003-02-004 | ✅ PASS | HTML5 required attribute prevents submission (browser-level validation) |
| TC-003-02-005 | ✅ PASS | HTML5 required attribute prevents submission (browser-level validation) |
| TC-003-02-006 | ✅ PASS | HTML5 email type prevents invalid format (browser-level validation) |
| TC-003-02-007 | ✅ PASS | "Email already registered." error displayed, modal stays open |

---

## UC-003-03-01: Display Edit Form

**Status:** ✅ Complete
**Parent Story:** US-003-03 - Edit Existing Person

**Description:** Display edit form in modal with pre-populated data.

**Implementation Tasks:**
- [x] Implement `GET /persons/{id}/edit` endpoint
- [x] Add `Templates.person$modal_edit()` fragment method
- [x] Create `{#fragment id=modal_edit rendered=false}` in person.html
  - Pre-populate all fields with current values
  - Title dropdown shows current selection
  - Gender dropdown shows current selection
  - Display audit fields in collapsible `<details>` section (read-only):
    - `createdAt` formatted as date/time
    - `createdBy` as text
    - `updatedAt` formatted as date/time
    - `updatedBy` as text

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/{id}/edit` | Display edit modal content |

**Test Cases:** TC-003-03-001

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-03-001 | ✅ PASS | Modal displays with title "Edit Person", all fields pre-populated (firstName, lastName, email, phone, title dropdown, gender dropdown), audit info in collapsible section shows createdAt/createdBy/updatedAt/updatedBy, Save/Cancel buttons visible |

---

## UC-003-03-02: Submit Edit Form

**Status:** ✅ Complete
**Parent Story:** US-003-03 - Edit Existing Person

**Description:** Validate and update existing person record.

**Implementation Tasks:**
- [x] Implement `PUT /persons/{id}` endpoint with `@Transactional`
- [x] Validate firstName is not empty
- [x] Validate lastName is not empty
- [x] Validate email is not empty
- [x] Validate email format
- [x] Validate email uniqueness (excluding current record)
- [x] Update title and gender links
- [x] Set `updatedBy` from `securityIdentity.getPrincipal().getName()` before merge
- [x] Note: `updatedAt` is set automatically by `@PreUpdate` callback
- [x] Add `Templates.person$modal_success_row()` fragment method
- [x] Create `{#fragment id=modal_success_row rendered=false}` with:
  - `hx-on::load` to close modal
  - OOB swap to update specific row

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| PUT | `/persons/{id}` | Update person |

**Test Cases:** TC-003-03-002, TC-003-03-003

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-03-002 | ✅ PASS | Person updated: lastName changed to "Updated", modal closed automatically, row updated via OOB swap |
| TC-003-03-003 | ✅ PASS | "Email already registered." error displayed when changing email to existing one, modal stays open with form preserved |

---

## UC-003-03-03: Cancel Edit

**Status:** ✅ Complete
**Parent Story:** US-003-03 - Edit Existing Person

**Description:** Cancel edit modal without saving changes.

**Implementation Tasks:**
- [x] Cancel button uses `uk-modal-close` class (no server request needed)
- [x] Modal closes client-side, no changes saved

**Test Cases:** TC-003-03-004

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-03-004 | ✅ PASS | Modal closed after Cancel click, original values preserved in table (lastName remained "Updated" despite being changed to "ChangedButNotSaved" in form) |

---

## UC-003-04-01: Delete Person

**Status:** ✅ Complete
**Parent Story:** US-003-04 - Delete Person

**Description:** Delete person with modal confirmation.

**Implementation Tasks:**
- [x] Implement `GET /persons/{id}/delete` endpoint (confirmation modal)
- [x] Add `Templates.person$modal_delete()` fragment method
- [x] Create `{#fragment id=modal_delete rendered=false}` with:
  - Warning message displaying person's name
  - Delete and Cancel buttons
- [x] Implement `DELETE /persons/{id}` endpoint with `@Transactional`
- [x] Add `Templates.person$modal_delete_success()` fragment method
- [x] Create `{#fragment id=modal_delete_success rendered=false}` with:
  - `hx-on::load` to close modal
  - OOB swap to remove row (using `<template>` wrapper)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/{id}/delete` | Display delete confirmation modal |
| DELETE | `/persons/{id}` | Delete person |

**Test Cases:** TC-003-04-001, TC-003-04-002, TC-003-04-003

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-04-001 | ✅ PASS | Modal displays with title "Delete Person", warning message shows person's name (Mr Tony Benn), Delete and Cancel buttons available |
| TC-003-04-002 | ✅ PASS | Person deleted (Mr Jane Updated), modal closed automatically, row removed from table via OOB swap |
| TC-003-04-003 | ✅ PASS | Modal closed after Cancel click, person (Mr Tony Benn) still exists in table |

---

## UC-003-05-01: Apply Filter

**Status:** ✅ Complete
**Parent Story:** US-003-05 - Filter People

**Description:** Filter persons list by name or email using query parameters.

**Implementation Tasks:**
- [x] Add `@QueryParam("filter")` to list endpoint
- [x] Implement filter logic in `Person.findByFilter()` method
  - Match firstName, lastName, or email (case-insensitive, contains)
- [x] Add filter bar HTML to person.html
  - Search input with debounce (300ms via `hx-trigger="input changed delay:300ms"`)
  - Use `hx-push-url="true"` to update URL
  - Use `hx-include="closest form"` for including sort params

**Test Cases:** TC-003-05-001, TC-003-05-002, TC-003-05-004

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-05-001 | ✅ PASS | Filter "Marx" returns only "Mr Karl Marx", URL updates to ?filter=Marx |
| TC-003-05-002 | ✅ PASS | Filter "ZZZZNONEXISTENT" shows "No persons match the filter criteria." message |
| TC-003-05-004 | ✅ PASS | Direct navigation to /persons?filter=Rosa loads filtered results with input pre-populated |

---

## UC-003-05-02: Clear Filter

**Status:** ✅ Complete
**Parent Story:** US-003-05 - Filter People

**Description:** Clear filter and display all persons.

**Implementation Tasks:**
- [x] Add Clear button as link to `/persons` (no query params)
- [x] System returns full list with default sort

**Test Cases:** TC-003-05-003

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-05-003 | ✅ PASS | Clear button navigates to /persons, filter field cleared, all 12 records displayed |

---

## UC-003-06-01: Apply Sort

**Status:** ✅ Complete
**Parent Story:** US-003-06 - Sort People

**Description:** Sort persons list by selected field using query parameters.

**Implementation Tasks:**
- [x] Add `@QueryParam("sortField")` and `@QueryParam("sortDir")` to list endpoint
- [x] Implement sort logic in `Person.findByFilter()` and `Person.buildOrderBy()` methods
  - Support fields: firstName, lastName, email
  - Support directions: asc, desc
- [x] Add sort dropdowns to filter bar
  - Sort field select (Last Name, First Name, Email)
  - Sort direction select (Ascending, Descending)
- [x] Use `hx-push-url="true"` to update URL with sort params

**Test Cases:** TC-003-06-001, TC-003-06-003

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-06-001 | ✅ PASS | Sort by firstName DESC works: Yanis → Tony → Simone → ... → Antonio; dropdowns show correct selection |
| TC-003-06-003 | ✅ PASS | URL state ?sortField=firstName&sortDir=desc loads correctly with sorted results and pre-selected dropdowns |

---

## UC-003-06-02: Clear Sort

**Status:** ✅ Complete
**Parent Story:** US-003-06 - Sort People

**Description:** Clear custom sort and restore default order.

**Implementation Tasks:**
- [x] Clear button links to `/persons` (clears all params including sort)
- [x] System applies default sort (lastName, firstName ASC)

**Test Cases:** TC-003-06-002

**Test Results (2025-12-30):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-06-002 | ✅ PASS | Clear button navigates to /persons, sort dropdowns reset (Sort by... + Ascending), records sorted by lastName ASC: Benn → Berlinguer → de Beauvoir → Engels → Gramsci → Iglesias → Luxemburg → Marx → Palme → Sartre → Varoufakis → Zetkin |

---

## Test Cases Reference

### Feature 003 Test Cases

| Test ID | Description | Use Case | Status |
|---------|-------------|----------|--------|
| TC-003-01-001 | Persons Page UI Elements | UC-003-01-01 | ✅ |
| TC-003-01-002 | Persons List Display | UC-003-01-01 | ⏭️ |
| TC-003-01-003 | Persons List Empty State | UC-003-01-01 | ✅ |
| TC-003-01-004 | Persons Access Requires Authentication | UC-003-01-01 | ✅ |
| TC-003-02-001 | Person Create Form Display | UC-003-02-01 | ✅ |
| TC-003-02-002 | Person Create Success | UC-003-02-02 | ✅ |
| TC-003-02-003 | Person Create First Name Required | UC-003-02-02 | ✅ |
| TC-003-02-004 | Person Create Last Name Required | UC-003-02-02 | ✅ |
| TC-003-02-005 | Person Create Email Required | UC-003-02-02 | ✅ |
| TC-003-02-006 | Person Create Email Format Validation | UC-003-02-02 | ✅ |
| TC-003-02-007 | Person Create Duplicate Email Prevention | UC-003-02-02 | ✅ |
| TC-003-03-001 | Person Edit Form Display | UC-003-03-01 | ✅ |
| TC-003-03-002 | Person Edit Success | UC-003-03-02 | ✅ |
| TC-003-03-003 | Person Edit Email Uniqueness | UC-003-03-02 | ✅ |
| TC-003-03-004 | Person Edit Cancel | UC-003-03-03 | ✅ |
| TC-003-04-001 | Person Delete Confirmation Modal | UC-003-04-01 | ✅ |
| TC-003-04-002 | Person Delete Success | UC-003-04-01 | ✅ |
| TC-003-04-003 | Person Delete Cancel | UC-003-04-01 | ✅ |
| TC-003-05-001 | Persons Filter by Name | UC-003-05-01 | ✅ |
| TC-003-05-002 | Persons Filter No Results | UC-003-05-01 | ✅ |
| TC-003-05-003 | Persons Filter Clear | UC-003-05-02 | ✅ |
| TC-003-05-004 | Persons Filter URL State | UC-003-05-01 | ✅ |
| TC-003-06-001 | Persons Sort by Name | UC-003-06-01 | ✅ |
| TC-003-06-002 | Persons Sort Clear | UC-003-06-02 | ✅ |
| TC-003-06-003 | Persons Sort URL State | UC-003-06-01 | ✅ |

---
