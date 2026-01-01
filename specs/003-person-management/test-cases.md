# Test Cases for Feature 003: Person Management

## Prerequisites

- Application running at `http://localhost:9080`
- Fresh database state (or known test data)
- Default admin user: `admin@example.com` / `AdminPassword123`
- Gender master data exists

## Test Data

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | AdminPassword123 | admin |

---

# US-003-01: View Persons List

### TC-003-01-001: Persons Page UI Elements
**Parent Use Case:** [UC-003-01-01: View Persons List](use-cases.md#uc-003-01-01-view-persons-list)

**Objective:** Verify persons list page renders correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Take a snapshot of the page

**Expected:**
- [ ] Page title contains "Persons"
- [ ] Table with columns: First Name, Last Name, Email, Phone, Date of Birth, Gender, Actions
- [ ] Add button is visible
- [ ] Filter panel is visible above table

---

### TC-003-01-002: Persons List Display
**Parent Use Case:** [UC-003-01-01: View Persons List](use-cases.md#uc-003-01-01-view-persons-list)

**Objective:** Verify persons list displays existing records.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Verify table content

**Expected:**
- [ ] Existing person records displayed in table
- [ ] Records sorted by lastName, then firstName (ascending)
- [ ] Each row has Edit and Delete buttons

---

### TC-003-01-003: Persons List Empty State
**Parent Use Case:** [UC-003-01-01: View Persons List](use-cases.md#uc-003-01-01-view-persons-list)

**Objective:** Verify empty state message when no records exist.

**Precondition:** No person records in database

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`

**Expected:**
- [ ] Message "No persons found" displayed
- [ ] Add button still visible

---

### TC-003-01-004: Persons Access Requires Authentication
**Parent Use Case:** [UC-003-01-01: View Persons List](use-cases.md#uc-003-01-01-view-persons-list)

**Objective:** Verify unauthenticated users cannot access persons page.

**Steps:**
1. Ensure not logged in
2. Navigate to `/persons`

**Expected:**
- [ ] Redirected to `/login` page
- [ ] Cannot access persons list without authentication

---

# US-003-02: Create New Person

### TC-003-02-001: Person Create Form Display
**Parent Use Case:** [UC-003-02-01: Display Create Form](use-cases.md#uc-003-02-01-display-create-form)

**Objective:** Verify create form modal displays correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button

**Expected:**
- [ ] Modal dialog opens
- [ ] Modal title shows "Add Person"
- [ ] firstName input field exists
- [ ] lastName input field exists
- [ ] title dropdown exists with available options
- [ ] email input field exists
- [ ] phone input field exists
- [ ] dateOfBirth date picker exists
- [ ] gender dropdown exists with available options
- [ ] Save and Cancel buttons visible

---

### TC-003-02-002: Person Create Success
**Parent Use Case:** [UC-003-02-02: Submit Create Form](use-cases.md#uc-003-02-02-submit-create-form)

**Objective:** Verify successful person creation.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form:
   - firstName: `John`
   - lastName: `Doe`
   - title: Select "Mr" (if available)
   - email: `john.doe@example.com`
   - phone: `555-1234`
   - dateOfBirth: `1990-01-15`
   - gender: Select an available option
5. Click Save button

**Expected:**
- [ ] Modal closes automatically
- [ ] New entry appears in persons table
- [ ] Email stored as lowercase
- [ ] Title selection saved correctly
- [ ] Gender selection saved correctly

---

### TC-003-02-003: Person Create First Name Required
**Parent Use Case:** [UC-003-02-02: Submit Create Form](use-cases.md#uc-003-02-02-submit-create-form)

**Objective:** Verify first name is required.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form without firstName:
   - firstName: (leave empty)
   - lastName: `Doe`
   - email: `test@example.com`
5. Click Save button

**Expected:**
- [ ] Error message "First name is required." displayed
- [ ] Modal remains open with form

---

### TC-003-02-004: Person Create Last Name Required
**Parent Use Case:** [UC-003-02-02: Submit Create Form](use-cases.md#uc-003-02-02-submit-create-form)

**Objective:** Verify last name is required.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form without lastName:
   - firstName: `Jane`
   - lastName: (leave empty)
   - email: `test@example.com`
5. Click Save button

**Expected:**
- [ ] Error message "Last name is required." displayed
- [ ] Modal remains open with form

---

### TC-003-02-005: Person Create Email Required
**Parent Use Case:** [UC-003-02-02: Submit Create Form](use-cases.md#uc-003-02-02-submit-create-form)

**Objective:** Verify email is required.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form without email:
   - firstName: `Jane`
   - lastName: `Doe`
   - email: (leave empty)
5. Click Save button

**Expected:**
- [ ] Error message "Email is required." displayed
- [ ] Modal remains open with form

---

### TC-003-02-006: Person Create Email Format Validation
**Parent Use Case:** [UC-003-02-02: Submit Create Form](use-cases.md#uc-003-02-02-submit-create-form)

**Objective:** Verify email format validation.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form:
   - firstName: `Jane`
   - lastName: `Doe`
   - email: `invalid-email`
5. Click Save button

**Expected:**
- [ ] Error message "Invalid email format." displayed
- [ ] Modal remains open with form

---

### TC-003-02-007: Person Create Duplicate Email Prevention
**Parent Use Case:** [UC-003-02-02: Submit Create Form](use-cases.md#uc-003-02-02-submit-create-form)

**Objective:** Verify duplicate email is rejected.

**Precondition:** Person with email `existing@example.com` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form:
   - firstName: `Another`
   - lastName: `Person`
   - email: `existing@example.com`
5. Click Save button

**Expected:**
- [ ] Error message "Email already registered." displayed
- [ ] Modal remains open with form

---

# US-003-03: Edit Existing Person

### TC-003-03-001: Person Edit Form Display
**Parent Use Case:** [UC-003-03-01: Display Edit Form](use-cases.md#uc-003-03-01-display-edit-form)

**Objective:** Verify edit form modal displays with pre-populated data.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for an existing entry

**Expected:**
- [ ] Modal dialog opens
- [ ] Modal title shows "Edit Person"
- [ ] All fields pre-populated with current values
- [ ] Title dropdown shows current selection
- [ ] Gender dropdown shows current selection
- [ ] Audit fields displayed (read-only, in collapsible section)
- [ ] Save and Cancel buttons visible

---

### TC-003-03-002: Person Edit Success
**Parent Use Case:** [UC-003-03-02: Submit Edit Form](use-cases.md#uc-003-03-02-submit-edit-form)

**Objective:** Verify successful person update.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for an existing entry
4. Modify lastName to `Updated`
5. Click Save button

**Expected:**
- [ ] Modal closes automatically
- [ ] Updated entry reflects changes in table
- [ ] Audit fields updated (updatedBy, updatedAt)

---

### TC-003-03-003: Person Edit Email Uniqueness
**Parent Use Case:** [UC-003-03-02: Submit Edit Form](use-cases.md#uc-003-03-02-submit-edit-form)

**Objective:** Verify email uniqueness on edit.

**Precondition:** Two person records exist with different emails

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for first person
4. Change email to match second person's email
5. Click Save button

**Expected:**
- [ ] Error message "Email already registered." displayed
- [ ] Modal remains open with form

---

### TC-003-03-004: Person Edit Cancel
**Parent Use Case:** [UC-003-03-03: Cancel Edit](use-cases.md#uc-003-03-03-cancel-edit)

**Objective:** Verify cancel discards changes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for an existing entry
4. Modify fields
5. Click Cancel button

**Expected:**
- [ ] Modal closes
- [ ] Original values unchanged in table

---

# US-003-04: Delete Person

### TC-003-04-001: Person Delete Confirmation Modal
**Parent Use Case:** [UC-003-04-01: Delete Person](use-cases.md#uc-003-04-01-delete-person)

**Objective:** Verify delete confirmation modal appears.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Delete button for an existing entry

**Expected:**
- [ ] Modal dialog opens
- [ ] Modal title shows "Delete Person"
- [ ] Warning message displays person's name
- [ ] Delete and Cancel buttons available

---

### TC-003-04-002: Person Delete Success
**Parent Use Case:** [UC-003-04-01: Delete Person](use-cases.md#uc-003-04-01-delete-person)

**Objective:** Verify successful person deletion.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Delete button for an entry
4. Click Delete button in confirmation modal

**Expected:**
- [ ] Modal closes automatically
- [ ] Entry removed from table immediately

---

### TC-003-04-003: Person Delete Cancel
**Parent Use Case:** [UC-003-04-01: Delete Person](use-cases.md#uc-003-04-01-delete-person)

**Objective:** Verify cancel preserves record.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Delete button for an entry
4. Click Cancel in confirmation modal

**Expected:**
- [ ] Modal closes
- [ ] Entry still exists in table

---

### TC-003-04-004: Person Edit Non-Existent
**Parent Use Case:** [UC-003-03-01: Display Edit Form](use-cases.md#uc-003-03-01-display-edit-form)

**Objective:** Verify editing non-existent person shows error (EntityNotFoundException).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate directly to `/persons/99999/edit` (non-existent ID)

**Expected:**
- [ ] Error displayed: "Person not found"
- [ ] HTTP status 404 Not Found returned

---

### TC-003-04-005: Person Delete Non-Existent
**Parent Use Case:** [UC-003-04-01: Delete Person](use-cases.md#uc-003-04-01-delete-person)

**Objective:** Verify deleting non-existent person shows error (EntityNotFoundException).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Attempt DELETE request to `/persons/99999` (non-existent ID)

**Expected:**
- [ ] Error displayed: "Person not found"
- [ ] HTTP status 404 Not Found returned

---

# US-003-05: Filter People

### TC-003-05-001: Persons Filter by Name
**Parent Use Case:** [UC-003-05-01: Apply Filter](use-cases.md#uc-003-05-01-apply-filter)

**Objective:** Verify filtering by name works.

**Precondition:** Multiple person records exist

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Enter filter text matching a lastName
4. Click Filter button

**Expected:**
- [ ] Only matching records displayed
- [ ] Filter criteria visible in filter field

---

### TC-003-05-002: Persons Filter No Results
**Parent Use Case:** [UC-003-05-01: Apply Filter](use-cases.md#uc-003-05-01-apply-filter)

**Objective:** Verify filter with no matches shows appropriate message.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Enter filter text that matches no records: `ZZZZNONEXISTENT`
4. Click Filter button

**Expected:**
- [ ] Message "No persons match the filter criteria" displayed
- [ ] Filter criteria still visible

---

### TC-003-05-003: Persons Filter Clear
**Parent Use Case:** [UC-003-05-02: Clear Filter](use-cases.md#uc-003-05-02-clear-filter)

**Objective:** Verify clearing filter shows all records.

**Precondition:** Filter is currently applied

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons` with active filter
3. Click Clear button

**Expected:**
- [ ] All records displayed
- [ ] Filter field cleared

---

### TC-003-05-004: Persons Filter Persistence
**Parent Use Case:** [UC-003-05-01: Apply Filter](use-cases.md#uc-003-05-01-apply-filter)

**Objective:** Verify filter persists during session.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Apply filter
4. Navigate away to another page
5. Return to `/persons`

**Expected:**
- [ ] Previous filter still applied
- [ ] Filtered results displayed

---

# US-003-06: Sort People

### TC-003-06-001: Persons Sort by Name
**Parent Use Case:** [UC-003-06-01: Apply Sort](use-cases.md#uc-003-06-01-apply-sort)

**Objective:** Verify sorting by name works.

**Precondition:** Multiple person records exist

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Select sort field: firstName
4. Select sort direction: descending
5. Click Sort button

**Expected:**
- [ ] Records reordered by firstName descending
- [ ] Sort criteria visible in sort panel

---

### TC-003-06-002: Persons Sort Clear
**Parent Use Case:** [UC-003-06-02: Clear Sort](use-cases.md#uc-003-06-02-clear-sort)

**Objective:** Verify clearing sort restores default order.

**Precondition:** Custom sort is currently applied

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons` with active custom sort
3. Click Clear Sort button

**Expected:**
- [ ] Records sorted by lastName, firstName (ascending) - default
- [ ] Sort panel reset

---

### TC-003-06-003: Persons Sort Persistence
**Parent Use Case:** [UC-003-06-01: Apply Sort](use-cases.md#uc-003-06-01-apply-sort)

**Objective:** Verify sort persists during session.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Apply custom sort
4. Navigate away to another page
5. Return to `/persons`

**Expected:**
- [ ] Previous sort still applied
- [ ] Sorted results displayed

---

## Test Summary

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-003-01-001 | Persons Page UI | UC-003-01-01 | [ ] | |
| TC-003-01-002 | Persons List Display | UC-003-01-01 | [ ] | |
| TC-003-01-003 | Persons List Empty | UC-003-01-01 | [ ] | |
| TC-003-01-004 | Persons Auth Required | UC-003-01-01 | [ ] | |
| TC-003-02-001 | Person Create Form | UC-003-02-01 | [ ] | |
| TC-003-02-002 | Person Create Success | UC-003-02-02 | [ ] | |
| TC-003-02-003 | Person First Name Required | UC-003-02-02 | [ ] | |
| TC-003-02-004 | Person Last Name Required | UC-003-02-02 | [ ] | |
| TC-003-02-005 | Person Email Required | UC-003-02-02 | [ ] | |
| TC-003-02-006 | Person Email Format | UC-003-02-02 | [ ] | |
| TC-003-02-007 | Person Duplicate Email | UC-003-02-02 | [ ] | |
| TC-003-03-001 | Person Edit Form | UC-003-03-01 | [ ] | |
| TC-003-03-002 | Person Edit Success | UC-003-03-02 | [ ] | |
| TC-003-03-003 | Person Edit Email Unique | UC-003-03-02 | [ ] | |
| TC-003-03-004 | Person Edit Cancel | UC-003-03-03 | [ ] | |
| TC-003-04-001 | Person Delete Modal | UC-003-04-01 | [ ] | |
| TC-003-04-002 | Person Delete Success | UC-003-04-01 | [ ] | |
| TC-003-04-003 | Person Delete Cancel | UC-003-04-01 | [ ] | |
| TC-003-04-004 | Person Edit Non-Existent | UC-003-03-01 | [ ] | EntityNotFoundException |
| TC-003-04-005 | Person Delete Non-Existent | UC-003-04-01 | [ ] | EntityNotFoundException |
| TC-003-05-001 | Persons Filter Name | UC-003-05-01 | [ ] | |
| TC-003-05-002 | Persons Filter No Results | UC-003-05-01 | [ ] | |
| TC-003-05-003 | Persons Filter Clear | UC-003-05-02 | [ ] | |
| TC-003-05-004 | Persons Filter URL State | UC-003-05-01 | [ ] | |
| TC-003-06-001 | Persons Sort Name | UC-003-06-01 | [ ] | |
| TC-003-06-002 | Persons Sort Clear | UC-003-06-02 | [ ] | |
| TC-003-06-003 | Persons Sort URL State | UC-003-06-01 | [ ] | |

---

## Traceability Matrix

| User Story | Use Cases | Test Cases |
|------------|-----------|------------|
| US-003-01: View Persons List | UC-003-01-01 | TC-003-01-001, TC-003-01-002, TC-003-01-003, TC-003-01-004 |
| US-003-02: Create New Person | UC-003-02-01, UC-003-02-02 | TC-003-02-001, TC-003-02-002, TC-003-02-003, TC-003-02-004, TC-003-02-005, TC-003-02-006, TC-003-02-007 |
| US-003-03: Edit Existing Person | UC-003-03-01, UC-003-03-02, UC-003-03-03 | TC-003-03-001, TC-003-03-002, TC-003-03-003, TC-003-03-004 |
| US-003-04: Delete Person | UC-003-04-01 | TC-003-04-001, TC-003-04-002, TC-003-04-003, TC-003-04-004, TC-003-04-005 |
| US-003-05: Filter People | UC-003-05-01, UC-003-05-02 | TC-003-05-001, TC-003-05-002, TC-003-05-003, TC-003-05-004 |
| US-003-06: Sort People | UC-003-06-01, UC-003-06-02 | TC-003-06-001, TC-003-06-002, TC-003-06-003 |
