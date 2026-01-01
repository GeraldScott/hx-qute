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

**Objective:** Verify create form modal displays correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button

**Expected:**
- [ ] Modal dialog opens with title "Add Gender"
- [ ] Code input field exists in modal
- [ ] Description input field exists in modal
- [ ] Save and Cancel buttons visible in modal footer
- [ ] Modal backdrop prevents page interaction

---

### TC-002-02-002: Gender Create Success
**Parent Use Case:** [UC-002-02-02: Submit Create Form](use-cases.md#uc-002-02-02-submit-create-form)

**Objective:** Verify successful gender creation via modal.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill modal form:
   - code: `x`
   - description: `Non-binary`
5. Click Save button

**Expected:**
- [ ] Modal closes automatically
- [ ] Success notification displayed
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
4. Fill modal form:
   - code: `n` (lowercase)
   - description: `Non-binary`
5. Click Save button

**Expected:**
- [ ] Modal closes
- [ ] Code stored as `N` (uppercase)
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
4. Fill modal form:
   - code: `M`
   - description: `Another Male`
5. Click Save button

**Expected:**
- [ ] Error message "Code already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

### TC-002-02-005: Gender Create Duplicate Description Prevention
**Parent Use Case:** [UC-002-02-02: Submit Create Form](use-cases.md#uc-002-02-02-submit-create-form)

**Objective:** Verify duplicate description is rejected.

**Precondition:** Gender with description `Male` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill modal form:
   - code: `Z`
   - description: `Male`
5. Click Save button

**Expected:**
- [ ] Error message "Description already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

### TC-002-02-006: Gender Create Code Max Length
**Parent Use Case:** [UC-002-02-02: Submit Create Form](use-cases.md#uc-002-02-02-submit-create-form)

**Objective:** Verify code maximum length validation (1 character).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Add button
4. Fill modal form:
   - code: `AB` (2 characters, exceeds 1)
   - description: `Test`
5. Click Save button

**Expected:**
- [ ] Error message "Code must be 1 character." displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

# US-002-03: Edit Existing Gender

### TC-002-03-001: Gender Edit Form Display
**Parent Use Case:** [UC-002-03-01: Display Edit Form](use-cases.md#uc-002-03-01-display-edit-form)

**Objective:** Verify edit modal displays with pre-populated data.

**Precondition:** Gender record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for an existing entry

**Expected:**
- [ ] Modal dialog opens with title "Edit Gender"
- [ ] Code field pre-populated with current value
- [ ] Description field pre-populated with current value
- [ ] Audit fields displayed (read-only) in details section
- [ ] Save and Cancel buttons visible in modal footer
- [ ] Modal backdrop prevents page interaction

---

### TC-002-03-002: Gender Edit Success
**Parent Use Case:** [UC-002-03-02: Submit Edit Form](use-cases.md#uc-002-03-02-submit-edit-form)

**Objective:** Verify successful gender update via modal.

**Precondition:** Gender record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for an existing entry
4. Modify description to `Updated Description` in modal
5. Click Save button

**Expected:**
- [ ] Modal closes automatically
- [ ] Success notification displayed
- [ ] Updated entry reflects changes in table row
- [ ] Audit fields updated (updatedBy, updatedAt)

---

### TC-002-03-003: Gender Edit Cancel
**Parent Use Case:** [UC-002-03-03: Cancel Edit](use-cases.md#uc-002-03-03-cancel-edit)

**Objective:** Verify cancel closes modal and discards changes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for an existing entry
4. Modify description in modal
5. Click Cancel button

**Expected:**
- [ ] Modal closes
- [ ] Original values unchanged in table row
- [ ] No success/error notification displayed

---

### TC-002-03-004: Gender Edit Duplicate Code Prevention
**Parent Use Case:** [UC-002-03-02: Submit Edit Form](use-cases.md#uc-002-03-02-submit-edit-form)

**Objective:** Verify duplicate code is rejected when editing (excluding current record).

**Precondition:** Gender codes `M` (Male) and `F` (Female) exist

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for gender `F` (Female)
4. Change code to `M`
5. Click Save button

**Expected:**
- [ ] Error message "Code already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

### TC-002-03-005: Gender Edit Duplicate Description Prevention
**Parent Use Case:** [UC-002-03-02: Submit Edit Form](use-cases.md#uc-002-03-02-submit-edit-form)

**Objective:** Verify duplicate description is rejected when editing (excluding current record).

**Precondition:** Gender descriptions `Male` and `Female` exist

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Edit button for gender `Female`
4. Change description to `Male`
5. Click Save button

**Expected:**
- [ ] Error message "Description already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

# US-002-04: Delete Gender

### TC-002-04-001: Gender Delete Confirmation Modal
**Parent Use Case:** [UC-002-04-01: Delete Gender](use-cases.md#uc-002-04-01-delete-gender)

**Objective:** Verify delete confirmation modal appears.

**Precondition:** Gender record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for an existing entry

**Expected:**
- [ ] Confirmation modal dialog opens
- [ ] Modal displays "Are you sure you want to delete [code] - [description]?"
- [ ] Delete button (danger style) and Cancel button visible
- [ ] Modal backdrop prevents page interaction

---

### TC-002-04-002: Gender Delete Success
**Parent Use Case:** [UC-002-04-01: Delete Gender](use-cases.md#uc-002-04-01-delete-gender)

**Objective:** Verify successful gender deletion via modal.

**Precondition:** Gender record exists (not in use)

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for an entry
4. Click Delete button in confirmation modal

**Expected:**
- [ ] Modal closes automatically
- [ ] Entry removed from list with animation
- [ ] Success notification displayed (optional)

---

### TC-002-04-003: Gender Delete Cancel
**Parent Use Case:** [UC-002-04-01: Delete Gender](use-cases.md#uc-002-04-01-delete-gender)

**Objective:** Verify cancel closes modal and preserves record.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for an entry
4. Click Cancel button in confirmation modal

**Expected:**
- [ ] Modal closes
- [ ] Entry still exists in list
- [ ] No changes to data

---

### TC-002-04-004: Gender Delete Referential Integrity
**Parent Use Case:** [UC-002-04-01: Delete Gender](use-cases.md#uc-002-04-01-delete-gender)

**Objective:** Verify gender in use by Person cannot be deleted (ReferentialIntegrityException).

**Precondition:** Gender record exists and is referenced by at least one Person

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/genders`
3. Click Delete button for a gender that is in use
4. Click Delete button in confirmation modal

**Expected:**
- [ ] Error message displayed: "Cannot delete: Gender is in use by X person(s)."
- [ ] Modal remains open with error
- [ ] Gender record NOT deleted
- [ ] HTTP status 409 Conflict returned

---

### TC-002-04-005: Gender Edit Non-Existent
**Parent Use Case:** [UC-002-03-01: Display Edit Form](use-cases.md#uc-002-03-01-display-edit-form)

**Objective:** Verify editing non-existent gender shows error (EntityNotFoundException).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate directly to `/genders/99999/edit` (non-existent ID)

**Expected:**
- [ ] Error displayed: "Gender not found"
- [ ] HTTP status 404 Not Found returned

---

### TC-002-04-006: Gender Delete Non-Existent
**Parent Use Case:** [UC-002-04-01: Delete Gender](use-cases.md#uc-002-04-01-delete-gender)

**Objective:** Verify deleting non-existent gender shows error (EntityNotFoundException).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Attempt DELETE request to `/genders/99999` (non-existent ID)

**Expected:**
- [ ] Error displayed: "Gender not found"
- [ ] HTTP status 404 Not Found returned

---

# US-002-05: View Title Master Data

### TC-002-05-001: Title Page UI Elements
**Parent Use Case:** [UC-002-05-01: View Title List](use-cases.md#uc-002-05-01-view-title-list)

**Objective:** Verify title list page renders correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles` (via Maintenance menu)
3. Take a snapshot of the page

**Expected:**
- [ ] Page title contains "Title"
- [ ] Table with columns: Code, Description, Actions
- [ ] Add button is visible
- [ ] Navigation shows Maintenance menu item as active

---

### TC-002-05-002: Title List Display
**Parent Use Case:** [UC-002-05-01: View Title List](use-cases.md#uc-002-05-01-view-title-list)

**Objective:** Verify title list displays existing records.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Verify table content

**Expected:**
- [ ] Existing title records displayed in table
- [ ] Records sorted by code (ascending)
- [ ] Each row has Edit and Delete buttons

---

### TC-002-05-003: Title List Empty State
**Parent Use Case:** [UC-002-05-01: View Title List](use-cases.md#uc-002-05-01-view-title-list)

**Objective:** Verify empty state message when no records exist.

**Precondition:** No title records in database

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`

**Expected:**
- [ ] Message "No title entries found" displayed
- [ ] Add button still visible

---

### TC-002-05-004: Title Access Requires Admin Role
**Parent Use Case:** [UC-002-05-01: View Title List](use-cases.md#uc-002-05-01-view-title-list)

**Objective:** Verify only admin users can access title management.

**Precondition:** User with "user" role exists

**Steps:**
1. Login as non-admin user
2. Attempt to navigate to `/titles`

**Expected:**
- [ ] Access denied or redirected
- [ ] Error message indicates insufficient permissions

---

# US-002-06: Create New Title

### TC-002-06-001: Title Create Form Display
**Parent Use Case:** [UC-002-06-01: Display Create Form](use-cases.md#uc-002-06-01-display-create-form)

**Objective:** Verify create form modal displays correctly.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Add button

**Expected:**
- [ ] Modal dialog opens with title "Add Title"
- [ ] Code input field exists in modal (max 5 chars)
- [ ] Description input field exists in modal
- [ ] Save and Cancel buttons visible in modal footer
- [ ] Modal backdrop prevents page interaction

---

### TC-002-06-002: Title Create Success
**Parent Use Case:** [UC-002-06-02: Submit Create Form](use-cases.md#uc-002-06-02-submit-create-form)

**Objective:** Verify successful title creation via modal.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Add button
4. Fill modal form:
   - code: `hon`
   - description: `Honourable`
5. Click Save button

**Expected:**
- [ ] Modal closes automatically
- [ ] Success notification displayed
- [ ] New entry appears in list
- [ ] Code displayed as uppercase (`HON`)

---

### TC-002-06-003: Title Create Code Uppercase Coercion
**Parent Use Case:** [UC-002-06-02: Submit Create Form](use-cases.md#uc-002-06-02-submit-create-form)

**Objective:** Verify code is coerced to uppercase.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Add button
4. Fill modal form:
   - code: `dame` (lowercase)
   - description: `Dame`
5. Click Save button

**Expected:**
- [ ] Modal closes
- [ ] Code stored as `DAME` (uppercase)
- [ ] Entry displays with uppercase code

---

### TC-002-06-004: Title Create Duplicate Code Prevention
**Parent Use Case:** [UC-002-06-02: Submit Create Form](use-cases.md#uc-002-06-02-submit-create-form)

**Objective:** Verify duplicate code is rejected.

**Precondition:** Title with code `MR` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Add button
4. Fill modal form:
   - code: `MR`
   - description: `Another Mister`
5. Click Save button

**Expected:**
- [ ] Error message "Code already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

### TC-002-06-005: Title Create Duplicate Description Prevention
**Parent Use Case:** [UC-002-06-02: Submit Create Form](use-cases.md#uc-002-06-02-submit-create-form)

**Objective:** Verify duplicate description is rejected.

**Precondition:** Title with description `Mister` exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Add button
4. Fill modal form:
   - code: `ZZZ`
   - description: `Mister`
5. Click Save button

**Expected:**
- [ ] Error message "Description already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

### TC-002-06-006: Title Create Code Max Length
**Parent Use Case:** [UC-002-06-02: Submit Create Form](use-cases.md#uc-002-06-02-submit-create-form)

**Objective:** Verify code maximum length validation (5 characters).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Add button
4. Fill modal form:
   - code: `ABCDEF` (6 characters, exceeds 5)
   - description: `Test`
5. Click Save button

**Expected:**
- [ ] Error message "Code must be at most 5 characters." displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

# US-002-07: Edit Existing Title

### TC-002-07-001: Title Edit Form Display
**Parent Use Case:** [UC-002-07-01: Display Edit Form](use-cases.md#uc-002-07-01-display-edit-form)

**Objective:** Verify edit modal displays with pre-populated data.

**Precondition:** Title record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Edit button for an existing entry

**Expected:**
- [ ] Modal dialog opens with title "Edit Title"
- [ ] Code field pre-populated with current value
- [ ] Description field pre-populated with current value
- [ ] Audit fields displayed (read-only) in details section
- [ ] Save and Cancel buttons visible in modal footer
- [ ] Modal backdrop prevents page interaction

---

### TC-002-07-002: Title Edit Success
**Parent Use Case:** [UC-002-07-02: Submit Edit Form](use-cases.md#uc-002-07-02-submit-edit-form)

**Objective:** Verify successful title update via modal.

**Precondition:** Title record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Edit button for an existing entry
4. Modify description to `Updated Description` in modal
5. Click Save button

**Expected:**
- [ ] Modal closes automatically
- [ ] Success notification displayed
- [ ] Updated entry reflects changes in table row
- [ ] Audit fields updated (updatedBy, updatedAt)

---

### TC-002-07-003: Title Edit Cancel
**Parent Use Case:** [UC-002-07-03: Cancel Edit](use-cases.md#uc-002-07-03-cancel-edit)

**Objective:** Verify cancel closes modal and discards changes.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Edit button for an existing entry
4. Modify description in modal
5. Click Cancel button

**Expected:**
- [ ] Modal closes
- [ ] Original values unchanged in table row
- [ ] No success/error notification displayed

---

### TC-002-07-004: Title Edit Duplicate Code Prevention
**Parent Use Case:** [UC-002-07-02: Submit Edit Form](use-cases.md#uc-002-07-02-submit-edit-form)

**Objective:** Verify duplicate code is rejected when editing (excluding current record).

**Precondition:** Title codes `MR` (Mister) and `MS` (Miss) exist

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Edit button for title `MS` (Miss)
4. Change code to `MR`
5. Click Save button

**Expected:**
- [ ] Error message "Code already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

### TC-002-07-005: Title Edit Duplicate Description Prevention
**Parent Use Case:** [UC-002-07-02: Submit Edit Form](use-cases.md#uc-002-07-02-submit-edit-form)

**Objective:** Verify duplicate description is rejected when editing (excluding current record).

**Precondition:** Title descriptions `Mister` and `Miss` exist

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Edit button for title `Miss`
4. Change description to `Mister`
5. Click Save button

**Expected:**
- [ ] Error message "Description already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

# US-002-08: Delete Title

### TC-002-08-001: Title Delete Confirmation Modal
**Parent Use Case:** [UC-002-08-01: Delete Title](use-cases.md#uc-002-08-01-delete-title)

**Objective:** Verify delete confirmation modal appears.

**Precondition:** Title record exists

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Delete button for an existing entry

**Expected:**
- [ ] Confirmation modal dialog opens
- [ ] Modal displays "Are you sure you want to delete [code] - [description]?"
- [ ] Delete button (danger style) and Cancel button visible
- [ ] Modal backdrop prevents page interaction

---

### TC-002-08-002: Title Delete Success
**Parent Use Case:** [UC-002-08-01: Delete Title](use-cases.md#uc-002-08-01-delete-title)

**Objective:** Verify successful title deletion via modal.

**Precondition:** Title record exists (not in use)

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Delete button for an entry
4. Click Delete button in confirmation modal

**Expected:**
- [ ] Modal closes automatically
- [ ] Entry removed from list with animation
- [ ] Success notification displayed (optional)

---

### TC-002-08-003: Title Delete Cancel
**Parent Use Case:** [UC-002-08-01: Delete Title](use-cases.md#uc-002-08-01-delete-title)

**Objective:** Verify cancel closes modal and preserves record.

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Delete button for an entry
4. Click Cancel button in confirmation modal

**Expected:**
- [ ] Modal closes
- [ ] Entry still exists in list
- [ ] No changes to data

---

### TC-002-08-004: Title Delete Referential Integrity
**Parent Use Case:** [UC-002-08-01: Delete Title](use-cases.md#uc-002-08-01-delete-title)

**Objective:** Verify title in use by Person cannot be deleted (ReferentialIntegrityException).

**Precondition:** Title record exists and is referenced by at least one Person

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/titles`
3. Click Delete button for a title that is in use
4. Click Delete button in confirmation modal

**Expected:**
- [ ] Error message displayed: "Cannot delete: Title is in use by X person(s)."
- [ ] Modal remains open with error
- [ ] Title record NOT deleted
- [ ] HTTP status 409 Conflict returned

---

### TC-002-08-005: Title Edit Non-Existent
**Parent Use Case:** [UC-002-07-01: Display Edit Form](use-cases.md#uc-002-07-01-display-edit-form)

**Objective:** Verify editing non-existent title shows error (EntityNotFoundException).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate directly to `/titles/99999/edit` (non-existent ID)

**Expected:**
- [ ] Error displayed: "Title not found"
- [ ] HTTP status 404 Not Found returned

---

### TC-002-08-006: Title Delete Non-Existent
**Parent Use Case:** [UC-002-08-01: Delete Title](use-cases.md#uc-002-08-01-delete-title)

**Objective:** Verify deleting non-existent title shows error (EntityNotFoundException).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Attempt DELETE request to `/titles/99999` (non-existent ID)

**Expected:**
- [ ] Error displayed: "Title not found"
- [ ] HTTP status 404 Not Found returned

---

# US-002-09: View Relationship Master Data

### TC-002-09-001: Relationship Page UI Elements
**Parent Use Case:** [UC-002-09-01: View Relationship List](use-cases.md#uc-002-09-01-view-relationship-list)

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

### TC-002-09-002: Relationship List Display
**Parent Use Case:** [UC-002-09-01: View Relationship List](use-cases.md#uc-002-09-01-view-relationship-list)

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

### TC-002-09-003: Relationship List Empty State
**Parent Use Case:** [UC-002-09-01: View Relationship List](use-cases.md#uc-002-09-01-view-relationship-list)

**Objective:** Verify empty state message when no records exist.

**Precondition:** No relationship records in database

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`

**Expected:**
- [ ] Message "No relationships found" displayed
- [ ] Add button still visible

---

### TC-002-09-004: Relationship Access Requires Admin Role
**Parent Use Case:** [UC-002-09-01: View Relationship List](use-cases.md#uc-002-09-01-view-relationship-list)

**Objective:** Verify only admin users can access relationship management.

**Precondition:** User with "user" role exists

**Steps:**
1. Login as non-admin user
2. Attempt to navigate to `/relationships`

**Expected:**
- [ ] Access denied or redirected
- [ ] Error message indicates insufficient permissions

---

# US-002-10: Create New Relationship

### TC-002-10-001: Relationship Create Form Display
**Parent Use Case:** [UC-002-10-01: Display Create Form](use-cases.md#uc-002-10-01-display-create-form)

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

### TC-002-10-002: Relationship Create Success
**Parent Use Case:** [UC-002-10-02: Submit Create Form](use-cases.md#uc-002-10-02-submit-create-form)

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

### TC-002-10-003: Relationship Create Code Uppercase Coercion
**Parent Use Case:** [UC-002-10-02: Submit Create Form](use-cases.md#uc-002-10-02-submit-create-form)

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

### TC-002-10-004: Relationship Create Duplicate Code Prevention
**Parent Use Case:** [UC-002-10-02: Submit Create Form](use-cases.md#uc-002-10-02-submit-create-form)

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

### TC-002-10-005: Relationship Create Duplicate Description Prevention
**Parent Use Case:** [UC-002-10-02: Submit Create Form](use-cases.md#uc-002-10-02-submit-create-form)

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

### TC-002-10-006: Relationship Create Code Max Length
**Parent Use Case:** [UC-002-10-02: Submit Create Form](use-cases.md#uc-002-10-02-submit-create-form)

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

# US-002-11: Edit Existing Relationship

### TC-002-11-001: Relationship Edit Form Display
**Parent Use Case:** [UC-002-11-01: Display Edit Form](use-cases.md#uc-002-11-01-display-edit-form)

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

### TC-002-11-002: Relationship Edit Success
**Parent Use Case:** [UC-002-11-02: Submit Edit Form](use-cases.md#uc-002-11-02-submit-edit-form)

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

### TC-002-11-003: Relationship Edit Cancel
**Parent Use Case:** [UC-002-11-03: Cancel Edit](use-cases.md#uc-002-11-03-cancel-edit)

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

### TC-002-11-004: Relationship Edit Duplicate Code Prevention
**Parent Use Case:** [UC-002-11-02: Submit Edit Form](use-cases.md#uc-002-11-02-submit-edit-form)

**Objective:** Verify duplicate code is rejected when editing (excluding current record).

**Precondition:** Relationship codes `SPOUSE` and `PARENT` exist

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Edit button for relationship `PARENT`
4. Change code to `SPOUSE`
5. Click Save button

**Expected:**
- [ ] Error message "Code already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

### TC-002-11-005: Relationship Edit Duplicate Description Prevention
**Parent Use Case:** [UC-002-11-02: Submit Edit Form](use-cases.md#uc-002-11-02-submit-edit-form)

**Objective:** Verify duplicate description is rejected when editing (excluding current record).

**Precondition:** Relationship descriptions `Spouse` and `Parent` exist

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate to `/relationships`
3. Click Edit button for relationship `Parent`
4. Change description to `Spouse`
5. Click Save button

**Expected:**
- [ ] Error message "Description already exists" displayed in modal
- [ ] Modal remains open
- [ ] User can correct and retry

---

# US-002-12: Delete Relationship

### TC-002-12-001: Relationship Delete Confirmation Modal
**Parent Use Case:** [UC-002-12-01: Delete Relationship](use-cases.md#uc-002-12-01-delete-relationship)

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

### TC-002-12-002: Relationship Delete Success
**Parent Use Case:** [UC-002-12-01: Delete Relationship](use-cases.md#uc-002-12-01-delete-relationship)

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

### TC-002-12-003: Relationship Delete Cancel
**Parent Use Case:** [UC-002-12-01: Delete Relationship](use-cases.md#uc-002-12-01-delete-relationship)

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

### TC-002-12-004: Relationship Edit Non-Existent
**Parent Use Case:** [UC-002-11-01: Display Edit Form](use-cases.md#uc-002-11-01-display-edit-form)

**Objective:** Verify editing non-existent relationship shows error (EntityNotFoundException).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Navigate directly to `/relationships/99999/edit` (non-existent ID)

**Expected:**
- [ ] Error displayed: "Relationship not found"
- [ ] HTTP status 404 Not Found returned

---

### TC-002-12-005: Relationship Delete Non-Existent
**Parent Use Case:** [UC-002-12-01: Delete Relationship](use-cases.md#uc-002-12-01-delete-relationship)

**Objective:** Verify deleting non-existent relationship shows error (EntityNotFoundException).

**Steps:**
1. Login as `admin@example.com` / `AdminPassword123`
2. Attempt DELETE request to `/relationships/99999` (non-existent ID)

**Expected:**
- [ ] Error displayed: "Relationship not found"
- [ ] HTTP status 404 Not Found returned

---

## Test Summary

### Gender Tests

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-002-01-001 | Gender Page UI | UC-002-01-01 | [ ] | |
| TC-002-01-002 | Gender List Display | UC-002-01-01 | [ ] | |
| TC-002-01-003 | Gender List Empty | UC-002-01-01 | [ ] | |
| TC-002-01-004 | Gender Admin Role | UC-002-01-01 | [ ] | |
| TC-002-02-001 | Gender Create Form Modal | UC-002-02-01 | [ ] | |
| TC-002-02-002 | Gender Create Success | UC-002-02-02 | [ ] | |
| TC-002-02-003 | Gender Code Uppercase | UC-002-02-02 | [ ] | |
| TC-002-02-004 | Gender Duplicate Code | UC-002-02-02 | [ ] | |
| TC-002-02-005 | Gender Duplicate Desc | UC-002-02-02 | [ ] | |
| TC-002-02-006 | Gender Code Max Length | UC-002-02-02 | [ ] | |
| TC-002-03-001 | Gender Edit Form Modal | UC-002-03-01 | [ ] | |
| TC-002-03-002 | Gender Edit Success | UC-002-03-02 | [ ] | |
| TC-002-03-003 | Gender Edit Cancel | UC-002-03-03 | [ ] | |
| TC-002-03-004 | Gender Edit Duplicate Code | UC-002-03-02 | [ ] | |
| TC-002-03-005 | Gender Edit Duplicate Desc | UC-002-03-02 | [ ] | |
| TC-002-04-001 | Gender Delete Confirm Modal | UC-002-04-01 | [ ] | |
| TC-002-04-002 | Gender Delete Success | UC-002-04-01 | [ ] | |
| TC-002-04-003 | Gender Delete Cancel | UC-002-04-01 | [ ] | |
| TC-002-04-004 | Gender Delete Referential Integrity | UC-002-04-01 | [ ] | ReferentialIntegrityException |
| TC-002-04-005 | Gender Edit Non-Existent | UC-002-03-01 | [ ] | EntityNotFoundException |
| TC-002-04-006 | Gender Delete Non-Existent | UC-002-04-01 | [ ] | EntityNotFoundException |

### Title Tests

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-002-05-001 | Title Page UI | UC-002-05-01 | [ ] | |
| TC-002-05-002 | Title List Display | UC-002-05-01 | [ ] | |
| TC-002-05-003 | Title List Empty | UC-002-05-01 | [ ] | |
| TC-002-05-004 | Title Admin Role | UC-002-05-01 | [ ] | |
| TC-002-06-001 | Title Create Form Modal | UC-002-06-01 | [ ] | |
| TC-002-06-002 | Title Create Success | UC-002-06-02 | [ ] | |
| TC-002-06-003 | Title Code Uppercase | UC-002-06-02 | [ ] | |
| TC-002-06-004 | Title Duplicate Code | UC-002-06-02 | [ ] | |
| TC-002-06-005 | Title Duplicate Desc | UC-002-06-02 | [ ] | |
| TC-002-06-006 | Title Code Max Length | UC-002-06-02 | [ ] | |
| TC-002-07-001 | Title Edit Form Modal | UC-002-07-01 | [ ] | |
| TC-002-07-002 | Title Edit Success | UC-002-07-02 | [ ] | |
| TC-002-07-003 | Title Edit Cancel | UC-002-07-03 | [ ] | |
| TC-002-07-004 | Title Edit Duplicate Code | UC-002-07-02 | [ ] | |
| TC-002-07-005 | Title Edit Duplicate Desc | UC-002-07-02 | [ ] | |
| TC-002-08-001 | Title Delete Confirm Modal | UC-002-08-01 | [ ] | |
| TC-002-08-002 | Title Delete Success | UC-002-08-01 | [ ] | |
| TC-002-08-003 | Title Delete Cancel | UC-002-08-01 | [ ] | |
| TC-002-08-004 | Title Delete Referential Integrity | UC-002-08-01 | [ ] | ReferentialIntegrityException |
| TC-002-08-005 | Title Edit Non-Existent | UC-002-07-01 | [ ] | EntityNotFoundException |
| TC-002-08-006 | Title Delete Non-Existent | UC-002-08-01 | [ ] | EntityNotFoundException |

### Relationship Tests

| Test ID | Test Name | Parent UC | Status | Notes |
|---------|-----------|-----------|--------|-------|
| TC-002-09-001 | Relationship Page UI | UC-002-09-01 | [ ] | |
| TC-002-09-002 | Relationship List Display | UC-002-09-01 | [ ] | |
| TC-002-09-003 | Relationship List Empty | UC-002-09-01 | [ ] | |
| TC-002-09-004 | Relationship Admin Role | UC-002-09-01 | [ ] | |
| TC-002-10-001 | Relationship Create Form Modal | UC-002-10-01 | [ ] | |
| TC-002-10-002 | Relationship Create Success | UC-002-10-02 | [ ] | |
| TC-002-10-003 | Relationship Code Uppercase | UC-002-10-02 | [ ] | |
| TC-002-10-004 | Relationship Duplicate Code | UC-002-10-02 | [ ] | |
| TC-002-10-005 | Relationship Duplicate Desc | UC-002-10-02 | [ ] | |
| TC-002-10-006 | Relationship Code Max Length | UC-002-10-02 | [ ] | |
| TC-002-11-001 | Relationship Edit Form Modal | UC-002-11-01 | [ ] | |
| TC-002-11-002 | Relationship Edit Success | UC-002-11-02 | [ ] | |
| TC-002-11-003 | Relationship Edit Cancel | UC-002-11-03 | [ ] | |
| TC-002-11-004 | Relationship Edit Duplicate Code | UC-002-11-02 | [ ] | |
| TC-002-11-005 | Relationship Edit Duplicate Desc | UC-002-11-02 | [ ] | |
| TC-002-12-001 | Relationship Delete Confirm Modal | UC-002-12-01 | [ ] | |
| TC-002-12-002 | Relationship Delete Success | UC-002-12-01 | [ ] | |
| TC-002-12-003 | Relationship Delete Cancel | UC-002-12-01 | [ ] | |
| TC-002-12-004 | Relationship Edit Non-Existent | UC-002-11-01 | [ ] | EntityNotFoundException |
| TC-002-12-005 | Relationship Delete Non-Existent | UC-002-12-01 | [ ] | EntityNotFoundException |

---

## Traceability Matrix

### Gender

| User Story | Use Cases | Test Cases |
|------------|-----------|------------|
| US-002-01: View Gender Master Data | UC-002-01-01 | TC-002-01-001, TC-002-01-002, TC-002-01-003, TC-002-01-004 |
| US-002-02: Create New Gender | UC-002-02-01, UC-002-02-02 | TC-002-02-001, TC-002-02-002, TC-002-02-003, TC-002-02-004, TC-002-02-005, TC-002-02-006 |
| US-002-03: Edit Existing Gender | UC-002-03-01, UC-002-03-02, UC-002-03-03 | TC-002-03-001, TC-002-03-002, TC-002-03-003, TC-002-03-004, TC-002-03-005 |
| US-002-04: Delete Gender | UC-002-04-01 | TC-002-04-001, TC-002-04-002, TC-002-04-003, TC-002-04-004, TC-002-04-005, TC-002-04-006 |

### Title

| User Story | Use Cases | Test Cases |
|------------|-----------|------------|
| US-002-05: View Title Master Data | UC-002-05-01 | TC-002-05-001, TC-002-05-002, TC-002-05-003, TC-002-05-004 |
| US-002-06: Create New Title | UC-002-06-01, UC-002-06-02 | TC-002-06-001, TC-002-06-002, TC-002-06-003, TC-002-06-004, TC-002-06-005, TC-002-06-006 |
| US-002-07: Edit Existing Title | UC-002-07-01, UC-002-07-02, UC-002-07-03 | TC-002-07-001, TC-002-07-002, TC-002-07-003, TC-002-07-004, TC-002-07-005 |
| US-002-08: Delete Title | UC-002-08-01 | TC-002-08-001, TC-002-08-002, TC-002-08-003, TC-002-08-004, TC-002-08-005, TC-002-08-006 |

### Relationship

| User Story | Use Cases | Test Cases |
|------------|-----------|------------|
| US-002-09: View Relationship Master Data | UC-002-09-01 | TC-002-09-001, TC-002-09-002, TC-002-09-003, TC-002-09-004 |
| US-002-10: Create New Relationship | UC-002-10-01, UC-002-10-02 | TC-002-10-001, TC-002-10-002, TC-002-10-003, TC-002-10-004, TC-002-10-005, TC-002-10-006 |
| US-002-11: Edit Existing Relationship | UC-002-11-01, UC-002-11-02, UC-002-11-03 | TC-002-11-001, TC-002-11-002, TC-002-11-003, TC-002-11-004, TC-002-11-005 |
| US-002-12: Delete Relationship | UC-002-12-01 | TC-002-12-001, TC-002-12-002, TC-002-12-003, TC-002-12-004, TC-002-12-005 |
