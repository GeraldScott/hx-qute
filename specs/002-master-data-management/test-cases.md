# Test Cases for Feature 002: Master Data Management

## Prerequisites

- Application running at `http://localhost:9080`
- Fresh database state (or known test data)
- Default admin user: `admin@example.com` / `AdminPassword123`

## Test Data

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | AdminPassword123 | admin |

---

# US-002-01: View Gender Master Data

### TC-002-01-001: Gender Page UI Elements
**Parent Use Case:** [UC-002-01-01: View Gender List](use-cases.md#uc-002-01-01-view-gender-list)

**Objective:** Verify gender list page renders correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders` (via Maintenance menu)
3. Take a snapshot of the page

**Expected:**
- [ ] Page title contains "Gender"
- [ ] Table with columns: Code, Description, Actions
- [ ] Add button is visible
- [ ] Navigation shows Maintenance menu item as active

---

### TC-002-01-002: Gender List Display
**Parent Use Case:** [UC-002-01-01: View Gender List](use-cases.md#uc-002-01-01-view-gender-list)

**Objective:** Verify gender list displays existing records.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Verify table content

**Expected:**
- [ ] Existing gender records displayed in table
- [ ] Records sorted by code (ascending)
- [ ] Each row has Edit and Delete buttons

---

### TC-002-01-003: Gender List Empty State
**Parent Use Case:** [UC-002-01-01: View Gender List](use-cases.md#uc-002-01-01-view-gender-list)

**Objective:** Verify empty state message when no records exist.

**Precondition:** No gender records in database

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`

**Expected:**
- [ ] Message "No gender entries found" displayed
- [ ] Add button still visible

---

### TC-002-01-004: Gender Access Requires Admin Role
**Parent Use Case:** [UC-002-01-01: View Gender List](use-cases.md#uc-002-01-01-view-gender-list)

**Objective:** Verify only admin users can access gender management.

**Precondition:** User with "user" role exists

**Steps:**
1. Login as non-admin user
2. Attempt to navigate to `/genders`

**Expected:**
- [ ] Access denied or redirected
- [ ] Error message indicates insufficient permissions

---

# US-002-02: Create New Gender

### TC-002-02-001: Gender Create Form Display
**Parent Use Case:** [UC-002-02-01: Display Create Form](use-cases.md#uc-002-02-01-display-create-form)

**Objective:** Verify create form displays correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button

**Expected:**
- [ ] Create form displayed
- [ ] Code input field exists
- [ ] Description input field exists
- [ ] Save and Cancel buttons visible

---

### TC-002-02-002: Gender Create Success
**Parent Use Case:** [UC-002-02-02: Submit Create Form](use-cases.md#uc-002-02-02-submit-create-form)

**Objective:** Verify successful gender creation.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `x`
   - description: `Non-binary`
5. Click Save button

**Expected:**
- [ ] Redirected to gender list
- [ ] New entry appears in list
- [ ] Code displayed as uppercase (`X`)

---

### TC-002-02-003: Gender Create Code Uppercase Coercion
**Parent Use Case:** [UC-002-02-02: Submit Create Form](use-cases.md#uc-002-02-02-submit-create-form)

**Objective:** Verify code is coerced to uppercase.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `abc` (lowercase)
   - description: `Test Gender`
5. Click Save button

**Expected:**
- [ ] Code stored as `ABC` (uppercase)
- [ ] Entry displays with uppercase code

---

### TC-002-02-004: Gender Create Duplicate Code Prevention
**Parent Use Case:** [UC-002-02-02: Submit Create Form](use-cases.md#uc-002-02-02-submit-create-form)

**Objective:** Verify duplicate code is rejected.

**Precondition:** Gender with code `M` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `M`
   - description: `Another Male`
5. Click Save button

**Expected:**
- [ ] Error message "Code already exists" displayed
- [ ] User remains on create form

---

### TC-002-02-005: Gender Create Duplicate Description Prevention
**Parent Use Case:** [UC-002-02-02: Submit Create Form](use-cases.md#uc-002-02-02-submit-create-form)

**Objective:** Verify duplicate description is rejected.

**Precondition:** Gender with description `Male` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `Z`
   - description: `Male`
5. Click Save button

**Expected:**
- [ ] Error message "Description already exists" displayed
- [ ] User remains on create form

---

### TC-002-02-006: Gender Create Code Max Length
**Parent Use Case:** [UC-002-02-02: Submit Create Form](use-cases.md#uc-002-02-02-submit-create-form)

**Objective:** Verify code maximum length validation.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill form:
   - code: `ABCDEFGH` (8 characters, exceeds 7)
   - description: `Test`
5. Click Save button

**Expected:**
- [ ] Error message about code length displayed
- [ ] User remains on create form

---

# US-002-03: Edit Existing Gender

### TC-002-03-001: Gender Edit Form Display
**Parent Use Case:** [UC-002-03-01: Display Edit Form](use-cases.md#uc-002-03-01-display-edit-form)

**Objective:** Verify edit form displays with pre-populated data.

**Precondition:** Gender record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for an existing entry

**Expected:**
- [ ] Edit form displayed
- [ ] Code field pre-populated with current value
- [ ] Description field pre-populated with current value
- [ ] Audit fields displayed (read-only)
- [ ] Save and Cancel buttons visible

---

### TC-002-03-002: Gender Edit Success
**Parent Use Case:** [UC-002-03-02: Submit Edit Form](use-cases.md#uc-002-03-02-submit-edit-form)

**Objective:** Verify successful gender update.

**Precondition:** Gender record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for an existing entry
4. Modify description to `Updated Description`
5. Click Save button

**Expected:**
- [ ] Redirected to gender list
- [ ] Updated entry reflects changes
- [ ] Audit fields updated (updatedBy, updatedAt)

---

### TC-002-03-003: Gender Edit Cancel
**Parent Use Case:** [UC-002-03-03: Cancel Edit](use-cases.md#uc-002-03-03-cancel-edit)

**Objective:** Verify cancel discards changes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for an existing entry
4. Modify description
5. Click Cancel button

**Expected:**
- [ ] Redirected to gender list
- [ ] Original values unchanged

---

# US-002-04: Delete Gender

### TC-002-04-001: Gender Delete Confirmation
**Parent Use Case:** [UC-002-04-01: Delete Gender](use-cases.md#uc-002-04-01-delete-gender)

**Objective:** Verify delete confirmation dialog appears.

**Precondition:** Gender record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for an existing entry

**Expected:**
- [ ] Confirmation dialog appears
- [ ] Dialog asks "Are you sure you want to delete this entry?"
- [ ] Confirm and Cancel options available

---

### TC-002-04-002: Gender Delete Success
**Parent Use Case:** [UC-002-04-01: Delete Gender](use-cases.md#uc-002-04-01-delete-gender)

**Objective:** Verify successful gender deletion.

**Precondition:** Gender record exists (not in use)

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for an entry
4. Confirm deletion

**Expected:**
- [ ] Entry removed from list
- [ ] List updated immediately

---

### TC-002-04-003: Gender Delete Cancel
**Parent Use Case:** [UC-002-04-01: Delete Gender](use-cases.md#uc-002-04-01-delete-gender)

**Objective:** Verify cancel preserves record.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for an entry
4. Click Cancel in confirmation dialog

**Expected:**
- [ ] Dialog closes
- [ ] Entry still exists in list

---

## Test Summary

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-002-01-001 | Gender Page UI | UC-002-01-01 | [ ] | |
| TC-002-01-002 | Gender List Display | UC-002-01-01 | [ ] | |
| TC-002-01-003 | Gender List Empty | UC-002-01-01 | [ ] | |
| TC-002-01-004 | Gender Admin Role | UC-002-01-01 | [ ] | |
| TC-002-02-001 | Gender Create Form | UC-002-02-01 | [ ] | |
| TC-002-02-002 | Gender Create Success | UC-002-02-02 | [ ] | |
| TC-002-02-003 | Gender Code Uppercase | UC-002-02-02 | [ ] | |
| TC-002-02-004 | Gender Duplicate Code | UC-002-02-02 | [ ] | |
| TC-002-02-005 | Gender Duplicate Desc | UC-002-02-02 | [ ] | |
| TC-002-02-006 | Gender Code Max Length | UC-002-02-02 | [ ] | |
| TC-002-03-001 | Gender Edit Form | UC-002-03-01 | [ ] | |
| TC-002-03-002 | Gender Edit Success | UC-002-03-02 | [ ] | |
| TC-002-03-003 | Gender Edit Cancel | UC-002-03-03 | [ ] | |
| TC-002-04-001 | Gender Delete Confirm | UC-002-04-01 | [ ] | |
| TC-002-04-002 | Gender Delete Success | UC-002-04-01 | [ ] | |
| TC-002-04-003 | Gender Delete Cancel | UC-002-04-01 | [ ] | |

---

## Traceability Matrix

| User Story | Use Cases | Test Cases |
|------------|-----------|------------|
| US-002-01: View Gender Master Data | UC-002-01-01 | TC-002-01-001, TC-002-01-002, TC-002-01-003, TC-002-01-004 |
| US-002-02: Create New Gender | UC-002-02-01, UC-002-02-02 | TC-002-02-001, TC-002-02-002, TC-002-02-003, TC-002-02-004, TC-002-02-005, TC-002-02-006 |
| US-002-03: Edit Existing Gender | UC-002-03-01, UC-002-03-02, UC-002-03-03 | TC-002-03-001, TC-002-03-002, TC-002-03-003 |
| US-002-04: Delete Gender | UC-002-04-01 | TC-002-04-001, TC-002-04-002, TC-002-04-003 |
