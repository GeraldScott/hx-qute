# Implementation Plan for Feature 003 - Person Management

## Current Status

**Current Use Case:** UC-003-07-06: Delete Relationship
**Status:** 🔲 Not Started
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
| UC-003-07-01: View Person Relationships | ✅ Complete |
| UC-003-07-02: Display Add Relationship Form | ✅ Complete |
| UC-003-07-03: Submit Add Relationship Form | ✅ Complete |
| UC-003-07-04: Display Edit Relationship Form | ✅ Complete |
| UC-003-07-05: Submit Edit Relationship Form | ✅ Complete |
| UC-003-07-06: Delete Relationship | 🔲 Not Started |
| UC-003-07-07: Apply Relationship Filter | 🔲 Not Started |
| UC-003-07-08: Clear Relationship Filter | 🔲 Not Started |
| UC-003-07-09: Apply Relationship Sort | 🔲 Not Started |
| UC-003-07-10: Clear Relationship Sort | 🔲 Not Started |

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

## UC-003-07-01: View Person Relationships

**Status:** ✅ Complete
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Display a list of all relationships for a selected person, with filter bar and modal shell.

**Implementation Tasks:**
- [x] Create migration `V1.6.0__Create_person_relationship_table.sql`
  - Include `source_person_id` FK to person table
  - Include `related_person_id` FK to person table
  - Include `relationship_id` FK to relationship table
  - Include audit fields (created_at, updated_at, created_by, updated_by)
  - Add unique constraint (source_person_id, related_person_id, relationship_id)
  - Add check constraint (source_person_id != related_person_id)
- [x] Create `entity/PersonRelationship.java`
  - Add explicit `@Id` field with `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - Add `@ManyToOne` relationships to Person (source), Person (related), Relationship
  - Add audit fields: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
  - Add `@PrePersist` callback to set `createdAt` and `updatedAt`
  - Add `@PreUpdate` callback to set `updatedAt`
- [x] Create `repository/PersonRelationshipRepository.java`
  - Implement `findBySourcePersonId()` method
  - Implement `findBySourcePersonWithFilter()` method with filter and sort
  - Implement `exists()` method for duplicate detection
  - Implement `existsExcluding()` method for update validation
- [x] Create `router/PersonRelationshipResource.java` with `@RolesAllowed({"user", "admin"})`
  - Inject `PersonRelationshipRepository`, `PersonRepository`, `RelationshipRepository`, `SecurityIdentity`
- [x] Add `@CheckedTemplate` class with fragment methods
- [x] Implement `GET /persons/{personId}/relationships` endpoint with query params (filter, sortField, sortDir)
- [x] Create `templates/PersonRelationshipResource/personRelationship.html` with:
  - Header showing source person name
  - Back button to persons list
  - Filter bar above table (search input, sort dropdowns, Filter/Clear buttons)
  - Add Relationship button
  - Table container (`#relationship-table-container`)
  - Static modal shell (`#relationship-modal`)
  - `{#fragment id=table}` for table content
- [x] Update `templates/PersonResource/person.html` to add Link button in table row actions
- [x] Add route protection in `application.properties` for `/persons/*/relationships*` (already covered by `/persons/*`)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/{personId}/relationships` | List all relationships for person (with filter/sort) |

**Test Cases:** TC-003-07-001, TC-003-07-002, TC-003-07-003, TC-003-07-004

**Test Results (2026-01-01):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-07-001 | ✅ PASS | UI elements verified: title "Relationships for Mr Karl Marx", Back button, Add Relationship button, Filter bar with sort dropdowns |
| TC-003-07-002 | ⏭️ SKIP | No relationship data to verify list display (testable after UC-003-07-03) |
| TC-003-07-003 | ✅ PASS | Empty state "No relationships found for this person." displayed, Add Relationship button visible |
| TC-003-07-004 | ✅ PASS | Link button in person table navigates to /persons/{id}/relationships, page displays person name in title |

---

## UC-003-07-02: Display Add Relationship Form

**Status:** ✅ Complete
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Display add relationship form in modal when Add Relationship button is clicked.

**Implementation Tasks:**
- [x] Implement `GET /persons/{personId}/relationships/create` endpoint
- [x] Add `Templates.personRelationship$modal_create()` fragment method
- [x] Create `{#fragment id=modal_create rendered=false}` in personRelationship.html
  - Include Related Person dropdown (all persons except source person)
  - Include Relationship Type dropdown (from Relationship master data)
  - Pre-load persons and relationship types for dropdowns
  - Include Save and Cancel buttons

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/{personId}/relationships/create` | Display add relationship modal content |

**Test Cases:** TC-003-07-005

**Test Results (2026-01-01):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-07-005 | ✅ PASS | Modal displays with title "Add Relationship", Related Person dropdown (12 persons excluding Karl Marx), Relationship Type dropdown (6 types: Child, Colleague, Friend, Parent, Sibling, Spouse), Save/Cancel buttons visible |

---

## UC-003-07-03: Submit Add Relationship Form

**Status:** ✅ Complete
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Validate and create new person relationship record.

**Implementation Tasks:**
- [x] Implement `POST /persons/{personId}/relationships` endpoint with `@Transactional`
- [x] Validate relatedPersonId is provided
- [x] Validate relationshipId is provided
- [x] Validate relationship does not already exist (same source, related, type)
- [x] Link source person by personId path param
- [x] Link related person by relatedPersonId form param
- [x] Link relationship type by relationshipId form param
- [x] Set `createdBy` and `updatedBy` from `securityIdentity.getPrincipal().getName()` before persist
- [x] Add `Templates.personRelationship$modal_success()` fragment method
- [x] Create `{#fragment id=modal_success rendered=false}` with:
  - `hx-on::load` to close modal
  - OOB swap to refresh table container

**Validation Rules:**
| Field | Rule | Error Message |
|-------|------|---------------|
| relatedPersonId | Required | "Please select a person." |
| relationshipId | Required | "Please select a relationship type." |
| combination | Unique | "This relationship already exists." |

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | `/persons/{personId}/relationships` | Create new relationship |

**Test Cases:** TC-003-07-006, TC-003-07-007, TC-003-07-008, TC-003-07-009

**Test Results (2026-01-01):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-07-006 | ✅ PASS | Relationship created (Karl Marx → Friedrich Engels, Friend), modal closed automatically, table updated via OOB swap |
| TC-003-07-007 | ✅ PASS | HTML5 required attribute prevents submission when person not selected (browser-level validation) |
| TC-003-07-008 | ✅ PASS | HTML5 required attribute prevents submission when relationship type not selected (browser-level validation) |
| TC-003-07-009 | ✅ PASS | "This relationship already exists." error displayed when attempting duplicate (Friedrich Engels + Friend), modal stays open with form preserved |

---

## UC-003-07-04: Display Edit Relationship Form

**Status:** ✅ Complete
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Display edit form in modal with pre-populated data.

**Implementation Tasks:**
- [x] Implement `GET /persons/{personId}/relationships/{id}/edit` endpoint
- [x] Add `Templates.personRelationship$modal_edit()` fragment method
- [x] Create `{#fragment id=modal_edit rendered=false}` in personRelationship.html
  - Pre-populate all fields with current values
  - Related Person dropdown shows current selection
  - Relationship Type dropdown shows current selection
  - Display audit fields in collapsible `<details>` section (read-only)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/{personId}/relationships/{id}/edit` | Display edit relationship modal content |

**Test Cases:** TC-003-07-010

**Test Results (2026-01-01):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-07-010 | ✅ PASS | Modal displays with title "Edit Relationship", Related Person dropdown pre-populated with "Mr Friedrich Engels", Relationship Type dropdown pre-populated with "Friend", Audit Information section shows created/updated timestamps and user, Save/Cancel buttons visible |

---

## UC-003-07-05: Submit Edit Relationship Form

**Status:** ✅ Complete
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Validate and update existing person relationship record.

**Implementation Tasks:**
- [x] Implement `PUT /persons/{personId}/relationships/{id}` endpoint with `@Transactional`
- [x] Validate relatedPersonId is provided
- [x] Validate relationshipId is provided
- [x] Validate no duplicate relationship (excluding current record)
- [x] Update related person link if changed
- [x] Update relationship type link if changed
- [x] Set `updatedBy` from `securityIdentity.getPrincipal().getName()` before merge
- [x] Note: `updatedAt` is set automatically by `@PreUpdate` callback
- [x] Add `Templates.personRelationship$modal_success_row()` fragment method (already exists)
- [x] Create `{#fragment id=modal_success_row rendered=false}` with:
  - `hx-on::load` to close modal
  - OOB swap to update specific row (already exists)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| PUT | `/persons/{personId}/relationships/{id}` | Update relationship |

**Test Cases:** TC-003-07-011, TC-003-07-012

**Test Results (2026-01-01):**
| Test ID | Status | Notes |
|---------|--------|-------|
| TC-003-07-011 | ✅ PASS | Relationship updated (Friedrich Engels from Friend to Colleague), modal closed automatically, table row updated via OOB swap |
| TC-003-07-012 | ✅ PASS | "This relationship already exists." error displayed when changing Rosa Luxemburg relationship to match existing Friedrich Engels + Colleague, modal stays open with form preserved |

---

## UC-003-07-06: Delete Relationship

**Status:** 🔲 Not Started
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Delete relationship with modal confirmation.

**Implementation Tasks:**
- [ ] Implement `GET /persons/{personId}/relationships/{id}/delete` endpoint (confirmation modal)
- [ ] Add `Templates.personRelationship$modal_delete()` fragment method
- [ ] Create `{#fragment id=modal_delete rendered=false}` with:
  - Warning message displaying related person name and relationship type
  - Delete and Cancel buttons
- [ ] Implement `DELETE /persons/{personId}/relationships/{id}` endpoint with `@Transactional`
- [ ] Add `Templates.personRelationship$modal_delete_success()` fragment method
- [ ] Create `{#fragment id=modal_delete_success rendered=false}` with:
  - `hx-on::load` to close modal
  - OOB swap to remove row (using `<template>` wrapper)

**Endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| GET | `/persons/{personId}/relationships/{id}/delete` | Display delete confirmation modal |
| DELETE | `/persons/{personId}/relationships/{id}` | Delete relationship |

**Test Cases:** TC-003-07-013, TC-003-07-014, TC-003-07-015

**Test Results:**
- Test ID: TC-003-07-013, TC-003-07-014, TC-003-07-015
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-07-07: Apply Relationship Filter

**Status:** 🔲 Not Started
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Filter relationships list by related person name or relationship type.

**Implementation Tasks:**
- [ ] Add `@QueryParam("filter")` to list endpoint
- [ ] Implement filter logic in `PersonRelationshipRepository.findBySourcePersonWithFilter()` method
  - Match relatedPerson.firstName, relatedPerson.lastName, or relationship.description (case-insensitive, contains)
- [ ] Add filter bar HTML to personRelationship.html
  - Search input with debounce (300ms via `hx-trigger="input changed delay:300ms"`)
  - Use `hx-push-url="true"` to update URL
  - Use `hx-include="closest form"` for including sort params

**Test Cases:** TC-003-07-016, TC-003-07-017

**Test Results:**
- Test ID: TC-003-07-016, TC-003-07-017
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-07-08: Clear Relationship Filter

**Status:** 🔲 Not Started
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Clear filter and display all relationships for the person.

**Implementation Tasks:**
- [ ] Add Clear button as link to `/persons/{personId}/relationships` (no filter param, preserves sort if any)
- [ ] System returns full list with current or default sort

**Test Cases:** TC-003-07-018

**Test Results:**
- Test ID: TC-003-07-018
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-07-09: Apply Relationship Sort

**Status:** 🔲 Not Started
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Sort relationships list by selected field.

**Implementation Tasks:**
- [ ] Add `@QueryParam("sortField")` and `@QueryParam("sortDir")` to list endpoint
- [ ] Implement sort logic in `PersonRelationshipRepository.findBySourcePersonWithFilter()` and `buildOrderBy()` methods
  - Support fields: firstName (relatedPerson), lastName (relatedPerson), relationship (description)
  - Support directions: asc, desc
- [ ] Add sort dropdowns to filter bar
  - Sort field select (Last Name, First Name, Relationship)
  - Sort direction select (Ascending, Descending)
- [ ] Use `hx-push-url="true"` to update URL with sort params

**Test Cases:** TC-003-07-019

**Test Results:**
- Test ID: TC-003-07-019
- Status: 🔲 Not Tested
- Notes:

---

## UC-003-07-10: Clear Relationship Sort

**Status:** 🔲 Not Started
**Parent Story:** US-003-07 - Build Relationships Between People

**Description:** Clear custom sort and restore default order.

**Implementation Tasks:**
- [ ] Clear button links to `/persons/{personId}/relationships` (clears sort params, preserves filter if any)
- [ ] System applies default sort (relatedPerson.lastName, relatedPerson.firstName ASC)

**Test Cases:** TC-003-07-020

**Test Results:**
- Test ID: TC-003-07-020
- Status: 🔲 Not Tested
- Notes:

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
| TC-003-07-001 | Relationships Page UI Elements | UC-003-07-01 | ✅ |
| TC-003-07-002 | Relationships List Display | UC-003-07-01 | ⏭️ |
| TC-003-07-003 | Relationships List Empty State | UC-003-07-01 | ✅ |
| TC-003-07-004 | Relationships Access From Person Table | UC-003-07-01 | ✅ |
| TC-003-07-005 | Relationship Add Form Display | UC-003-07-02 | 🔲 |
| TC-003-07-006 | Relationship Add Success | UC-003-07-03 | 🔲 |
| TC-003-07-007 | Relationship Add Person Required | UC-003-07-03 | 🔲 |
| TC-003-07-008 | Relationship Add Type Required | UC-003-07-03 | 🔲 |
| TC-003-07-009 | Relationship Add Duplicate Prevention | UC-003-07-03 | 🔲 |
| TC-003-07-010 | Relationship Edit Form Display | UC-003-07-04 | 🔲 |
| TC-003-07-011 | Relationship Edit Success | UC-003-07-05 | 🔲 |
| TC-003-07-012 | Relationship Edit Duplicate Prevention | UC-003-07-05 | 🔲 |
| TC-003-07-013 | Relationship Delete Confirmation Modal | UC-003-07-06 | 🔲 |
| TC-003-07-014 | Relationship Delete Success | UC-003-07-06 | 🔲 |
| TC-003-07-015 | Relationship Delete Cancel | UC-003-07-06 | 🔲 |
| TC-003-07-016 | Relationships Filter by Name | UC-003-07-07 | 🔲 |
| TC-003-07-017 | Relationships Filter No Results | UC-003-07-07 | 🔲 |
| TC-003-07-018 | Relationships Filter Clear | UC-003-07-08 | 🔲 |
| TC-003-07-019 | Relationships Sort | UC-003-07-09 | 🔲 |
| TC-003-07-020 | Relationships Sort Clear | UC-003-07-10 | 🔲 |

---

## Status Legend

| Symbol | Meaning |
|--------|---------|
| 🔲 | Not Started |
| 🔄 | In Progress |
| ✅ | Complete |
| ❌ | Blocked |

---
