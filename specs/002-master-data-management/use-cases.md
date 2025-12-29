# Use Cases for Feature 002: Master Data Management

This feature provides CRUD operations for master data entities used throughout the application.

## Actors

| Actor | Description |
|-------|-------------|
| Administrator | Authenticated user with "admin" role |

---

# US-002-01: View Gender Master Data

## UC-002-01-01: View Gender List

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is authenticated with admin role |
| Trigger | User navigates to Gender page via Maintenance menu |

**Main Flow:**
1. System retrieves all Gender records
2. System sorts records by code (ascending)
3. System displays records in table format (code, description)
4. System displays Add button

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | No records exist | Display "No gender entries found" message |

**Postcondition:** Gender list displayed

---

# US-002-02: Create New Gender

## UC-002-02-01: Display Create Form

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is on Gender list page |
| Trigger | User clicks Add button |

**Main Flow:**
1. System displays inline create form above table
2. Form includes fields: code (max 1 char), description
3. Form includes Save and Cancel buttons

**Postcondition:** Create form is displayed

---

## UC-002-02-02: Submit Create Form

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User has filled create form |
| Trigger | User clicks Save button |

**Main Flow:**
1. System coerces code to uppercase
2. System validates code is not empty
3. System validates description is not empty
4. System validates code is max 1 character
5. System validates uniqueness of code
6. System validates uniqueness of description
7. System sets audit fields (createdBy, createdAt, updatedBy, updatedAt)
8. System persists new Gender record
9. System displays success message
10. System refreshes Gender list

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | Code empty | Display "Code is required." error |
| 3a | Description empty | Display "Description is required." error |
| 4a | Code > 1 character | Display "Code must be 1 character." error |
| 5a | Code already exists | Display "Code already exists." error |
| 6a | Description already exists | Display "Description already exists." error |

**Postcondition:** New Gender record created; list updated

---

# US-002-03: Edit Existing Gender

## UC-002-03-01: Display Edit Form

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is on Gender list page |
| Trigger | User clicks Edit button for a Gender entry |

**Main Flow:**
1. System retrieves Gender record by ID
2. System replaces table row with inline edit form
3. Form pre-populates code and description fields
4. Form displays audit fields (read-only)
5. Form includes Save and Cancel buttons

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Gender not found | Display error; refresh list |

**Postcondition:** Edit form displayed in place of row

---

## UC-002-03-02: Submit Edit Form

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User has modified edit form |
| Trigger | User clicks Save button |

**Main Flow:**
1. System coerces code to uppercase
2. System validates code is not empty
3. System validates description is not empty
4. System validates uniqueness of code (excluding current record)
5. System validates uniqueness of description (excluding current record)
6. System updates audit fields (updatedBy, updatedAt)
7. System persists updated Gender record
8. System replaces edit form with updated display row

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | Code empty | Display "Code is required." error |
| 3a | Description empty | Display "Description is required." error |
| 4a | Code conflicts with another record | Display "Code already exists." error |
| 5a | Description conflicts with another record | Display "Description already exists." error |

**Postcondition:** Gender record updated; row reflects changes

---

## UC-002-03-03: Cancel Edit

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is in edit mode for a Gender entry |
| Trigger | User clicks Cancel button |

**Main Flow:**
1. System discards any changes
2. System replaces edit form with original display row

**Postcondition:** Original values preserved; edit mode exited

---

# US-002-04: Delete Gender

## UC-002-04-01: Delete Gender

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is on Gender list page |
| Trigger | User clicks Delete button for a Gender entry |

**Main Flow:**
1. System displays browser confirmation dialog
2. User confirms deletion
3. System checks if Gender is in use by Person records
4. System deletes Gender record
5. System removes row from list with animation

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | User cancels | Close dialog; no action taken |
| 3a | Gender in use by Person records | Display "Cannot delete: Gender is in use by X person(s)." error |

**Postcondition:** Gender record deleted; list updated

---

## Use Case Summary

| ID | Use Case | Parent Story | Actor |
|----|----------|--------------|-------|
| UC-002-01-01 | View Gender List | US-002-01 | Administrator |
| UC-002-02-01 | Display Create Form | US-002-02 | Administrator |
| UC-002-02-02 | Submit Create Form | US-002-02 | Administrator |
| UC-002-03-01 | Display Edit Form | US-002-03 | Administrator |
| UC-002-03-02 | Submit Edit Form | US-002-03 | Administrator |
| UC-002-03-03 | Cancel Edit | US-002-03 | Administrator |
| UC-002-04-01 | Delete Gender | US-002-04 | Administrator |

---

## Traceability Matrix

| User Story | Use Cases |
|------------|-----------|
| US-002-01: View Gender Master Data | UC-002-01-01 |
| US-002-02: Create New Gender | UC-002-02-01, UC-002-02-02 |
| US-002-03: Edit Existing Gender | UC-002-03-01, UC-002-03-02, UC-002-03-03 |
| US-002-04: Delete Gender | UC-002-04-01 |
