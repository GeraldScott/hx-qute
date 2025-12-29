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

# US-003-01: Person Management

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

**Precondition:** No person records in database (except admin)

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`

**Expected:**
- [ ] Message "No persons found" displayed
- [ ] Add button still visible

---

### TC-003-01-004: Person Create Form Display
**Parent Use Case:** [UC-003-01-02: Create Person](use-cases.md#uc-003-01-02-create-person)

**Objective:** Verify create form displays correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button

**Expected:**
- [ ] Create form displayed
- [ ] firstName input field exists
- [ ] lastName input field exists
- [ ] email input field exists
- [ ] phone input field exists
- [ ] dateOfBirth date picker exists
- [ ] gender dropdown exists with available options
- [ ] Save and Cancel buttons visible

---

### TC-003-01-005: Person Create Success
**Parent Use Case:** [UC-003-01-02: Create Person](use-cases.md#uc-003-01-02-create-person)

**Objective:** Verify successful person creation.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form:
   - firstName: `John`
   - lastName: `Doe`
   - email: `john.doe@example.com`
   - phone: `555-1234`
   - dateOfBirth: `1990-01-15`
   - gender: Select an available option
5. Click Save button

**Expected:**
- [ ] Redirected to persons list
- [ ] New entry appears in list
- [ ] Email stored as lowercase
- [ ] Gender selection saved correctly

---

### TC-003-01-006: Person Create Email Required
**Parent Use Case:** [UC-003-01-02: Create Person](use-cases.md#uc-003-01-02-create-person)

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
- [ ] Error message "Email is required" displayed
- [ ] User remains on create form

---

### TC-003-01-007: Person Create Email Format Validation
**Parent Use Case:** [UC-003-01-02: Create Person](use-cases.md#uc-003-01-02-create-person)

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
- [ ] Error message about invalid email format displayed
- [ ] User remains on create form

---

### TC-003-01-008: Person Create Duplicate Email Prevention
**Parent Use Case:** [UC-003-01-02: Create Person](use-cases.md#uc-003-01-02-create-person)

**Objective:** Verify duplicate email is rejected.

**Precondition:** Person with email `admin@example.com` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Add button
4. Fill form:
   - firstName: `Another`
   - lastName: `Admin`
   - email: `admin@example.com`
5. Click Save button

**Expected:**
- [ ] Error message "Email already registered" displayed
- [ ] User remains on create form

---

### TC-003-01-009: Person Edit Form Display
**Parent Use Case:** [UC-003-01-03: Edit Person](use-cases.md#uc-003-01-03-edit-person)

**Objective:** Verify edit form displays with pre-populated data.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for an existing entry

**Expected:**
- [ ] Edit form displayed
- [ ] All fields pre-populated with current values
- [ ] Gender dropdown shows current selection
- [ ] Audit fields displayed (read-only)
- [ ] Save and Cancel buttons visible

---

### TC-003-01-010: Person Edit Success
**Parent Use Case:** [UC-003-01-03: Edit Person](use-cases.md#uc-003-01-03-edit-person)

**Objective:** Verify successful person update.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for an existing entry
4. Modify lastName to `Updated`
5. Click Save button

**Expected:**
- [ ] Redirected to persons list
- [ ] Updated entry reflects changes
- [ ] Audit fields updated (updatedBy, updatedAt)

---

### TC-003-01-011: Person Edit Email Uniqueness
**Parent Use Case:** [UC-003-01-03: Edit Person](use-cases.md#uc-003-01-03-edit-person)

**Objective:** Verify email uniqueness on edit.

**Precondition:** Two person records exist with different emails

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for first person
4. Change email to match second person's email
5. Click Save button

**Expected:**
- [ ] Error message "Email already registered" displayed
- [ ] User remains on edit form

---

### TC-003-01-012: Person Edit Cancel
**Parent Use Case:** [UC-003-01-03: Edit Person](use-cases.md#uc-003-01-03-edit-person)

**Objective:** Verify cancel discards changes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Edit button for an existing entry
4. Modify fields
5. Click Cancel button

**Expected:**
- [ ] Redirected to persons list
- [ ] Original values unchanged

---

### TC-003-01-013: Person Delete Confirmation
**Parent Use Case:** [UC-003-01-04: Delete Person](use-cases.md#uc-003-01-04-delete-person)

**Objective:** Verify delete confirmation dialog appears.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Delete button for an existing entry

**Expected:**
- [ ] Confirmation dialog appears
- [ ] Dialog asks "Are you sure you want to delete this person?"
- [ ] Confirm and Cancel options available

---

### TC-003-01-014: Person Delete Success
**Parent Use Case:** [UC-003-01-04: Delete Person](use-cases.md#uc-003-01-04-delete-person)

**Objective:** Verify successful person deletion.

**Precondition:** Person record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Delete button for an entry
4. Confirm deletion

**Expected:**
- [ ] Entry removed from list
- [ ] List updated immediately

---

### TC-003-01-015: Person Delete Cancel
**Parent Use Case:** [UC-003-01-04: Delete Person](use-cases.md#uc-003-01-04-delete-person)

**Objective:** Verify cancel preserves record.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/persons`
3. Click Delete button for an entry
4. Click Cancel in confirmation dialog

**Expected:**
- [ ] Dialog closes
- [ ] Entry still exists in list

---

### TC-003-01-016: Persons Filter by Name
**Parent Use Case:** [UC-003-01-05: Filter Persons](use-cases.md#uc-003-01-05-filter-persons)

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

### TC-003-01-017: Persons Filter No Results
**Parent Use Case:** [UC-003-01-05: Filter Persons](use-cases.md#uc-003-01-05-filter-persons)

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

### TC-003-01-018: Persons Filter Clear
**Parent Use Case:** [UC-003-01-05: Filter Persons](use-cases.md#uc-003-01-05-filter-persons)

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

### TC-003-01-019: Persons Filter Persistence
**Parent Use Case:** [UC-003-01-05: Filter Persons](use-cases.md#uc-003-01-05-filter-persons)

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

### TC-003-01-020: Persons Access Requires Authentication
**Parent Use Case:** [UC-003-01-01: View Persons List](use-cases.md#uc-003-01-01-view-persons-list)

**Objective:** Verify unauthenticated users cannot access persons page.

**Steps:**
1. Ensure not logged in
2. Navigate to `/persons`

**Expected:**
- [ ] Redirected to `/login` page
- [ ] Cannot access persons list without authentication

---

## Test Summary

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-003-01-001 | Persons Page UI | UC-003-01-01 | [ ] | |
| TC-003-01-002 | Persons List Display | UC-003-01-01 | [ ] | |
| TC-003-01-003 | Persons List Empty | UC-003-01-01 | [ ] | |
| TC-003-01-004 | Person Create Form | UC-003-01-02 | [ ] | |
| TC-003-01-005 | Person Create Success | UC-003-01-02 | [ ] | |
| TC-003-01-006 | Person Email Required | UC-003-01-02 | [ ] | |
| TC-003-01-007 | Person Email Format | UC-003-01-02 | [ ] | |
| TC-003-01-008 | Person Duplicate Email | UC-003-01-02 | [ ] | |
| TC-003-01-009 | Person Edit Form | UC-003-01-03 | [ ] | |
| TC-003-01-010 | Person Edit Success | UC-003-01-03 | [ ] | |
| TC-003-01-011 | Person Edit Email Unique | UC-003-01-03 | [ ] | |
| TC-003-01-012 | Person Edit Cancel | UC-003-01-03 | [ ] | |
| TC-003-01-013 | Person Delete Confirm | UC-003-01-04 | [ ] | |
| TC-003-01-014 | Person Delete Success | UC-003-01-04 | [ ] | |
| TC-003-01-015 | Person Delete Cancel | UC-003-01-04 | [ ] | |
| TC-003-01-016 | Persons Filter Name | UC-003-01-05 | [ ] | |
| TC-003-01-017 | Persons Filter No Results | UC-003-01-05 | [ ] | |
| TC-003-01-018 | Persons Filter Clear | UC-003-01-05 | [ ] | |
| TC-003-01-019 | Persons Filter Persist | UC-003-01-05 | [ ] | |
| TC-003-01-020 | Persons Auth Required | UC-003-01-01 | [ ] | |

---

## Traceability Matrix

| Use Case | Test Cases |
|----------|------------|
| UC-003-01-01: View Persons List | TC-003-01-001, TC-003-01-002, TC-003-01-003, TC-003-01-020 |
| UC-003-01-02: Create Person | TC-003-01-004, TC-003-01-005, TC-003-01-006, TC-003-01-007, TC-003-01-008 |
| UC-003-01-03: Edit Person | TC-003-01-009, TC-003-01-010, TC-003-01-011, TC-003-01-012 |
| UC-003-01-04: Delete Person | TC-003-01-013, TC-003-01-014, TC-003-01-015 |
| UC-003-01-05: Filter Persons | TC-003-01-016, TC-003-01-017, TC-003-01-018, TC-003-01-019 |
