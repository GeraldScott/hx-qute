# Use Cases for Feature 004: Relationship Management

This feature provides CRUD operations for the Relationship master data entity, which describes how people are related to each other.

## Actors

| Actor | Description |
|-------|-------------|
| Administrator | Authenticated user with "admin" role |

---

# US-004-01: View Relationship Master Data

## UC-004-01-01: View Relationship List

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is authenticated with admin role |
| Trigger | User navigates to Relationship page via Maintenance menu |

**Main Flow:**
1. System retrieves all Relationship records
2. System sorts records by code (ascending)
3. System displays records in table format (code, description)
4. System displays Add button

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | No records exist | Display "No relationship entries found" message |

**Postcondition:** Relationship list displayed

---

# US-004-02: Create New Relationship

## UC-004-02-01: Display Create Form

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is on Relationship list page |
| Trigger | User clicks Add button |

**Main Flow:**
1. System opens modal dialog titled "Add Relationship"
2. Modal includes fields: code (max 10 chars), description
3. Modal includes Save and Cancel buttons in footer
4. Modal backdrop prevents interaction with underlying page

**Postcondition:** Create form modal is displayed

---

## UC-004-02-02: Submit Create Form

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User has filled create form in modal |
| Trigger | User clicks Save button |

**Main Flow:**
1. System coerces code to uppercase
2. System validates code is not empty
3. System validates description is not empty
4. System validates code is max 10 characters
5. System validates uniqueness of code
6. System validates uniqueness of description
7. System sets audit fields (createdBy, createdAt, updatedBy, updatedAt)
8. System persists new Relationship record
9. System closes modal
10. System displays success notification
11. System refreshes Relationship list (OOB swap)

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | Code empty | Display "Code is required." error in modal |
| 3a | Description empty | Display "Description is required." error in modal |
| 4a | Code > 10 characters | Display "Code must be at most 10 characters." error in modal |
| 5a | Code already exists | Display "Code already exists." error in modal |
| 6a | Description already exists | Display "Description already exists." error in modal |

**Postcondition:** New Relationship record created; modal closed; list updated

---

# US-004-03: Edit Existing Relationship

## UC-004-03-01: Display Edit Form

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is on Relationship list page |
| Trigger | User clicks Edit button for a Relationship entry |

**Main Flow:**
1. System retrieves Relationship record by ID
2. System opens modal dialog titled "Edit Relationship"
3. Modal pre-populates code and description fields
4. Modal displays audit fields (read-only) in details section
5. Modal includes Save and Cancel buttons in footer
6. Modal backdrop prevents interaction with underlying page

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Relationship not found | Display error notification; do not open modal |

**Postcondition:** Edit form modal displayed with current values

---

## UC-004-03-02: Submit Edit Form

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User has modified edit form in modal |
| Trigger | User clicks Save button |

**Main Flow:**
1. System coerces code to uppercase
2. System validates code is not empty
3. System validates description is not empty
4. System validates uniqueness of code (excluding current record)
5. System validates uniqueness of description (excluding current record)
6. System updates audit fields (updatedBy, updatedAt)
7. System persists updated Relationship record
8. System closes modal
9. System displays success notification
10. System updates the affected row via OOB swap

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | Code empty | Display "Code is required." error in modal |
| 3a | Description empty | Display "Description is required." error in modal |
| 4a | Code conflicts with another record | Display "Code already exists." error in modal |
| 5a | Description conflicts with another record | Display "Description already exists." error in modal |

**Postcondition:** Relationship record updated; modal closed; row reflects changes

---

## UC-004-03-03: Cancel Edit

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User has edit modal open for a Relationship entry |
| Trigger | User clicks Cancel button or modal backdrop or presses Escape |

**Main Flow:**
1. System discards any unsaved changes
2. System closes modal
3. Table row remains unchanged

**Postcondition:** Original values preserved; modal closed

---

# US-004-04: Delete Relationship

## UC-004-04-01: Delete Relationship

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is on Relationship list page |
| Trigger | User clicks Delete button for a Relationship entry |

**Main Flow:**
1. System opens confirmation modal dialog
2. Modal displays warning: "Are you sure you want to delete [code] - [description]?"
3. Modal includes Delete (danger) and Cancel buttons
4. User clicks Delete button to confirm
5. System checks if Relationship is in use by Person records
6. System deletes Relationship record
7. System closes modal
8. System removes row from list with animation (OOB swap)

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 4a | User clicks Cancel or presses Escape | Close modal; no action taken |
| 5a | Relationship in use by Person records | Display "Cannot delete: Relationship is in use by X person(s)." error in modal |

**Postcondition:** Relationship record deleted; modal closed; list updated

---

## Use Case Summary

| ID | Use Case | Parent Story | Actor |
|----|----------|--------------|-------|
| UC-004-01-01 | View Relationship List | US-004-01 | Administrator |
| UC-004-02-01 | Display Create Form | US-004-02 | Administrator |
| UC-004-02-02 | Submit Create Form | US-004-02 | Administrator |
| UC-004-03-01 | Display Edit Form | US-004-03 | Administrator |
| UC-004-03-02 | Submit Edit Form | US-004-03 | Administrator |
| UC-004-03-03 | Cancel Edit | US-004-03 | Administrator |
| UC-004-04-01 | Delete Relationship | US-004-04 | Administrator |

---

## Traceability Matrix

| User Story | Use Cases |
|------------|-----------|
| US-004-01: View Relationship Master Data | UC-004-01-01 |
| US-004-02: Create New Relationship | UC-004-02-01, UC-004-02-02 |
| US-004-03: Edit Existing Relationship | UC-004-03-01, UC-004-03-02, UC-004-03-03 |
| US-004-04: Delete Relationship | UC-004-04-01 |
