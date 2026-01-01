# Test Cases for Feature 004: Relationship Management

## Prerequisites

- Application running at `http://localhost:9080`
- Fresh database state (or known test data)
- Default admin user: `admin@example.com` / `AdminPassword123`

## Test Data

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | AdminPassword123 | admin |

---

# US-004-01: View Relationship Master Data

### TC-004-01-001: Relationship Page UI Elements
**Parent Use Case:** [UC-004-01-01: View Relationship List](use-cases.md#uc-004-01-01-view-relationship-list)

**Objective:** Verify relationship list page renders correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships` (via Maintenance menu)
3. Take a snapshot of the page

**Expected:**
- [ ] Page title contains "Relationship"
- [ ] Table with columns: Code, Description, Actions
- [ ] Add button is visible
- [ ] Navigation shows Maintenance menu item as active

---

### TC-004-01-002: Relationship List Display
**Parent Use Case:** [UC-004-01-01: View Relationship List](use-cases.md#uc-004-01-01-view-relationship-list)

**Objective:** Verify relationship list displays existing records.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Verify table content

**Expected:**
- [ ] Existing relationship records displayed in table
- [ ] Records sorted by code (ascending)
- [ ] Each row has Edit and Delete buttons

---

### TC-004-01-003: Relationship List Empty State
**Parent Use Case:** [UC-004-01-01: View Relationship List](use-cases.md#uc-004-01-01-view-relationship-list)

**Objective:** Verify empty state message when no records exist.

**Precondition:** No relationship records in database

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`

**Expected:**
- [ ] Message "No relationships found" displayed
- [ ] Add button still visible

---

### TC-004-01-004: Relationship Access Requires Admin Role
**Parent Use Case:** [UC-004-01-01: View Relationship List](use-cases.md#uc-004-01-01-view-relationship-list)

**Objective:** Verify only admin users can access relationship management.

**Precondition:** User with "user" role exists

**Steps:**
1. Login as non-admin user
2. Attempt to navigate to `/relationships`

**Expected:**
- [ ] Access denied or redirected
- [ ] Error message indicates insufficient permissions

---

# US-004-02: Create New Relationship

### TC-004-02-001: Relationship Create Form Display
**Parent Use Case:** [UC-004-02-01: Display Create Form](use-cases.md#uc-004-02-01-display-create-form)

**Objective:** Verify create form modal displays correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Add button

**Expected:**
- [ ] Modal dialog opens with title "Add Relationship"
- [ ] Code input field exists in modal (max 10 chars)
- [ ] Description input field exists in modal
- [ ] Save and Cancel buttons visible in modal footer
- [ ] Modal backdrop prevents page interaction

---

### TC-004-02-002: Relationship Create Success
**Parent Use Case:** [UC-004-02-02: Submit Create Form](use-cases.md#uc-004-02-02-submit-create-form)

**Objective:** Verify successful relationship creation via modal.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Add button
4. Fill modal form:
   - code: `cousin`
   - description: `Cousin`
5. Click Save button

**Expected:**
- [ ] Modal closes automatically
- [ ] Success notification displayed
- [ ] New entry appears in list
- [ ] Code displayed as uppercase (`COUSIN`)

---

### TC-004-02-003: Relationship Create Code Uppercase Coercion
**Parent Use Case:** [UC-004-02-02: Submit Create Form](use-cases.md#uc-004-02-02-submit-create-form)

**Objective:** Verify code is coerced to uppercase.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Add button
4. Fill modal form:
   - code: `nephew` (lowercase)
   - description: `Nephew`
5. Click Save button

**Expected:**
- [ ] Modal closes
- [ ] Code stored as `NEPHEW` (uppercase)
- [ ] Entry displays with uppercase code

---

### TC-004-02-004: Relationship Create Duplicate Code Prevention
**Parent Use Case:** [UC-004-02-02: Submit Create Form](use-cases.md#uc-004-02-02-submit-create-form)

**Objective:** Verify duplicate code is rejected.

**Precondition:** Relationship with code `SPOUSE` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Add button
4. Fill modal form:
   - code: `SPOUSE`
   - description: `Another Spouse`
5. Click Save button

**Expected:**
- [ ] Error message "Code already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

### TC-004-02-005: Relationship Create Duplicate Description Prevention
**Parent Use Case:** [UC-004-02-02: Submit Create Form](use-cases.md#uc-004-02-02-submit-create-form)

**Objective:** Verify duplicate description is rejected.

**Precondition:** Relationship with description `Spouse` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Add button
4. Fill modal form:
   - code: `PARTNER`
   - description: `Spouse`
5. Click Save button

**Expected:**
- [ ] Error message "Description already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

### TC-004-02-006: Relationship Create Code Max Length
**Parent Use Case:** [UC-004-02-02: Submit Create Form](use-cases.md#uc-004-02-02-submit-create-form)

**Objective:** Verify code maximum length validation (10 characters).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Add button
4. Fill modal form:
   - code: `GRANDPARENT` (11 characters, exceeds 10)
   - description: `Grandparent`
5. Click Save button

**Expected:**
- [ ] Error message "Code must be at most 10 characters." displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

# US-004-03: Edit Existing Relationship

### TC-004-03-001: Relationship Edit Form Display
**Parent Use Case:** [UC-004-03-01: Display Edit Form](use-cases.md#uc-004-03-01-display-edit-form)

**Objective:** Verify edit modal displays with pre-populated data.

**Precondition:** Relationship record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Edit button for an existing entry

**Expected:**
- [ ] Modal dialog opens with title "Edit Relationship"
- [ ] Code field pre-populated with current value
- [ ] Description field pre-populated with current value
- [ ] Audit fields displayed (read-only) in details section
- [ ] Save and Cancel buttons visible in modal footer
- [ ] Modal backdrop prevents page interaction

---

### TC-004-03-002: Relationship Edit Success
**Parent Use Case:** [UC-004-03-02: Submit Edit Form](use-cases.md#uc-004-03-02-submit-edit-form)

**Objective:** Verify successful relationship update via modal.

**Precondition:** Relationship record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Edit button for an existing entry
4. Modify description to `Updated Description` in modal
5. Click Save button

**Expected:**
- [ ] Modal closes automatically
- [ ] Success notification displayed
- [ ] Updated entry reflects changes in table row
- [ ] Audit fields updated (updatedBy, updatedAt)

---

### TC-004-03-003: Relationship Edit Cancel
**Parent Use Case:** [UC-004-03-03: Cancel Edit](use-cases.md#uc-004-03-03-cancel-edit)

**Objective:** Verify cancel closes modal and discards changes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Edit button for an existing entry
4. Modify description in modal
5. Click Cancel button

**Expected:**
- [ ] Modal closes
- [ ] Original values unchanged in table row
- [ ] No success/error notification displayed

---

# US-004-04: Delete Relationship

### TC-004-04-001: Relationship Delete Confirmation Modal
**Parent Use Case:** [UC-004-04-01: Delete Relationship](use-cases.md#uc-004-04-01-delete-relationship)

**Objective:** Verify delete confirmation modal appears.

**Precondition:** Relationship record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Delete button for an existing entry

**Expected:**
- [ ] Confirmation modal dialog opens
- [ ] Modal displays "Are you sure you want to delete [code] - [description]?"
- [ ] Delete button (danger style) and Cancel button visible
- [ ] Modal backdrop prevents page interaction

---

### TC-004-04-002: Relationship Delete Success
**Parent Use Case:** [UC-004-04-01: Delete Relationship](use-cases.md#uc-004-04-01-delete-relationship)

**Objective:** Verify successful relationship deletion via modal.

**Precondition:** Relationship record exists (not in use)

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Delete button for an entry
4. Click Delete button in confirmation modal

**Expected:**
- [ ] Modal closes automatically
- [ ] Entry removed from list with animation
- [ ] Success notification displayed (optional)

---

### TC-004-04-003: Relationship Delete Cancel
**Parent Use Case:** [UC-004-04-01: Delete Relationship](use-cases.md#uc-004-04-01-delete-relationship)

**Objective:** Verify cancel closes modal and preserves record.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Delete button for an entry
4. Click Cancel button in confirmation modal

**Expected:**
- [ ] Modal closes
- [ ] Entry still exists in list
- [ ] No changes to data

---

## Test Summary

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-004-01-001 | Relationship Page UI | UC-004-01-01 | [ ] | |
| TC-004-01-002 | Relationship List Display | UC-004-01-01 | [ ] | |
| TC-004-01-003 | Relationship List Empty | UC-004-01-01 | [ ] | |
| TC-004-01-004 | Relationship Admin Role | UC-004-01-01 | [ ] | |
| TC-004-02-001 | Relationship Create Form Modal | UC-004-02-01 | [ ] | |
| TC-004-02-002 | Relationship Create Success | UC-004-02-02 | [ ] | |
| TC-004-02-003 | Relationship Code Uppercase | UC-004-02-02 | [ ] | |
| TC-004-02-004 | Relationship Duplicate Code | UC-004-02-02 | [ ] | |
| TC-004-02-005 | Relationship Duplicate Desc | UC-004-02-02 | [ ] | |
| TC-004-02-006 | Relationship Code Max Length | UC-004-02-02 | [ ] | |
| TC-004-03-001 | Relationship Edit Form Modal | UC-004-03-01 | [ ] | |
| TC-004-03-002 | Relationship Edit Success | UC-004-03-02 | [ ] | |
| TC-004-03-003 | Relationship Edit Cancel | UC-004-03-03 | [ ] | |
| TC-004-04-001 | Relationship Delete Confirm Modal | UC-004-04-01 | [ ] | |
| TC-004-04-002 | Relationship Delete Success | UC-004-04-01 | [ ] | |
| TC-004-04-003 | Relationship Delete Cancel | UC-004-04-01 | [ ] | |

---

## Traceability Matrix

| User Story | Use Cases | Test Cases |
|------------|-----------|------------|
| US-004-01: View Relationship Master Data | UC-004-01-01 | TC-004-01-001, TC-004-01-002, TC-004-01-003, TC-004-01-004 |
| US-004-02: Create New Relationship | UC-004-02-01, UC-004-02-02 | TC-004-02-001, TC-004-02-002, TC-004-02-003, TC-004-02-004, TC-004-02-005, TC-004-02-006 |
| US-004-03: Edit Existing Relationship | UC-004-03-01, UC-004-03-02, UC-004-03-03 | TC-004-03-001, TC-004-03-002, TC-004-03-003 |
| US-004-04: Delete Relationship | UC-004-04-01 | TC-004-04-001, TC-004-04-002, TC-004-04-003 |
