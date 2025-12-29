# Use Cases for Feature 003: Person Management

This feature provides CRUD operations for managing Person records.

## Actors

| Actor | Description |
|-------|-------------|
| User | Authenticated user with "user" role |
| Administrator | Authenticated user with "admin" role |

---

# US-003-01: View Persons List

## UC-003-01-01: View Persons List

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is authenticated |
| Trigger | User navigates to Persons page |

**Main Flow:**
1. System retrieves all Person records
2. System sorts records by lastName, then firstName (ascending)
3. System displays records in table format (firstName, lastName, email, phone, dateOfBirth, gender)
4. System displays Add button
5. System displays filter panel

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | No records exist | Display "No persons found" message |

**Postcondition:** Persons list displayed

---

# US-003-02: Create New Person

## UC-003-02-01: Display Create Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User clicks Add button |

**Main Flow:**
1. System displays inline create form above table
2. Form includes fields: firstName, lastName, email, phone, dateOfBirth, gender
3. Form includes Save and Cancel buttons

**Postcondition:** Create form is displayed

---

## UC-003-02-02: Submit Create Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has filled create form |
| Trigger | User clicks Save button |

**Main Flow:**
1. System validates email is not empty
2. System validates email format
3. System normalizes email to lowercase
4. System validates email uniqueness
5. System sets audit fields (createdBy, createdAt, updatedBy, updatedAt)
6. System persists new Person record
7. System displays success message
8. System refreshes Persons list

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Email empty | Display "Email is required." error |
| 2a | Email invalid format | Display "Invalid email format." error |
| 4a | Email already exists | Display "Email already registered." error |

**Postcondition:** New Person record created; list updated

---

# US-003-03: Edit Existing Person

## UC-003-03-01: Display Edit Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User clicks Edit button for a Person entry |

**Main Flow:**
1. System retrieves Person record by ID
2. System replaces table row with inline edit form
3. Form pre-populates all fields
4. Form displays audit fields (read-only)
5. Form includes Save and Cancel buttons

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Person not found | Display error; refresh list |

**Postcondition:** Edit form displayed in place of row

---

## UC-003-03-02: Submit Edit Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has modified edit form |
| Trigger | User clicks Save button |

**Main Flow:**
1. System validates email is not empty
2. System validates email format
3. System normalizes email to lowercase
4. System validates email uniqueness (excluding current record)
5. System updates audit fields (updatedBy, updatedAt)
6. System persists updated Person record
7. System replaces edit form with updated display row

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Email empty | Display "Email is required." error |
| 2a | Email invalid format | Display "Invalid email format." error |
| 4a | Email conflicts with another record | Display "Email already registered." error |

**Postcondition:** Person record updated; row reflects changes

---

## UC-003-03-03: Cancel Edit

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is in edit mode for a Person entry |
| Trigger | User clicks Cancel button |

**Main Flow:**
1. System discards any changes
2. System replaces edit form with original display row

**Postcondition:** Original values preserved; edit mode exited

---

# US-003-04: Delete Person

## UC-003-04-01: Delete Person

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User clicks Delete button for a Person entry |

**Main Flow:**
1. System displays browser confirmation dialog
2. User confirms deletion
3. System deletes Person record
4. System removes row from list with animation

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | User cancels | Close dialog; no action taken |

**Postcondition:** Person record deleted; list updated

---

# US-003-05: Filter People

## UC-003-05-01: Apply Filter

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User enters filter criteria and clicks Filter button |

**Main Flow:**
1. User enters search text in filter field
2. User clicks Filter button
3. System queries Person records matching criteria (firstName or lastName contains search text)
4. System displays filtered results
5. System persists filter criteria in session

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 3a | No matching records | Display "No persons match the filter criteria" |

**Postcondition:** Filtered list displayed; filter criteria persisted

---

## UC-003-05-02: Clear Filter

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has applied a filter |
| Trigger | User clicks Clear button |

**Main Flow:**
1. System clears filter criteria from session
2. System retrieves all Person records
3. System displays full list

**Postcondition:** Filter cleared; full list displayed

---

# US-003-06: Sort People

## UC-003-06-01: Apply Sort

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User selects sort option |

**Main Flow:**
1. User selects sort field (lastName or firstName)
2. User selects sort direction (ascending or descending)
3. User clicks Sort button
4. System reorders Person records by selected criteria
5. System displays sorted results
6. System persists sort criteria in session

**Postcondition:** Sorted list displayed; sort criteria persisted

---

## UC-003-06-02: Clear Sort

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has applied a sort |
| Trigger | User clicks Clear Sort button |

**Main Flow:**
1. System clears sort criteria from session
2. System applies default sort (lastName, firstName ascending)
3. System displays sorted list

**Postcondition:** Sort cleared; default sort applied

---

## Use Case Summary

| ID | Use Case | Parent Story | Actor(s) |
|----|----------|--------------|----------|
| UC-003-01-01 | View Persons List | US-003-01 | User, Administrator |
| UC-003-02-01 | Display Create Form | US-003-02 | User, Administrator |
| UC-003-02-02 | Submit Create Form | US-003-02 | User, Administrator |
| UC-003-03-01 | Display Edit Form | US-003-03 | User, Administrator |
| UC-003-03-02 | Submit Edit Form | US-003-03 | User, Administrator |
| UC-003-03-03 | Cancel Edit | US-003-03 | User, Administrator |
| UC-003-04-01 | Delete Person | US-003-04 | User, Administrator |
| UC-003-05-01 | Apply Filter | US-003-05 | User, Administrator |
| UC-003-05-02 | Clear Filter | US-003-05 | User, Administrator |
| UC-003-06-01 | Apply Sort | US-003-06 | User, Administrator |
| UC-003-06-02 | Clear Sort | US-003-06 | User, Administrator |

---

## Traceability Matrix

| User Story | Use Cases |
|------------|-----------|
| US-003-01: View Persons List | UC-003-01-01 |
| US-003-02: Create New Person | UC-003-02-01, UC-003-02-02 |
| US-003-03: Edit Existing Person | UC-003-03-01, UC-003-03-02, UC-003-03-03 |
| US-003-04: Delete Person | UC-003-04-01 |
| US-003-05: Filter People | UC-003-05-01, UC-003-05-02 |
| US-003-06: Sort People | UC-003-06-01, UC-003-06-02 |
