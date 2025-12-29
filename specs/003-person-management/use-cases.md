# Use Cases for Feature 003: Person Management

This feature provides CRUD operations for managing Person records.

## Actors

| Actor | Description |
|-------|-------------|
| User | Authenticated user with "user" role |
| Administrator | Authenticated user with "admin" role |

---

# US-003-01: Person Management

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

## UC-003-01-02: Create Person

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User clicks Add button |

**Main Flow:**
1. System displays create form with fields: firstName, lastName, email, phone, dateOfBirth, gender
2. User enters firstName
3. User enters lastName
4. User enters email (valid format)
5. User enters phone (optional)
6. User selects dateOfBirth using date picker
7. User selects gender from dropdown (optional)
8. User submits form
9. System validates required fields
10. System normalizes email to lowercase
11. System validates email uniqueness
12. System sets audit fields (createdBy, createdAt)
13. System persists new Person record
14. System redirects to Persons list

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 4a | Email empty | Display "Email is required" error |
| 4b | Email invalid format | Display "Invalid email format" error |
| 11a | Email already exists | Display "Email already registered" error |

**Postcondition:** New Person record created; list updated

---

## UC-003-01-03: Edit Person

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User clicks Edit button for a Person entry |

**Main Flow:**
1. System retrieves Person record by ID
2. System displays edit form pre-populated with current values
3. System displays audit fields (read-only)
4. User modifies fields as needed
5. User submits form
6. System validates required fields
7. System normalizes email to lowercase
8. System validates email uniqueness (excluding current record)
9. System updates audit fields (updatedBy, updatedAt)
10. System persists updated Person record
11. System redirects to Persons list

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Person not found | Display error; redirect to list |
| 8a | Email conflicts with another record | Display "Email already registered" error |

**Postcondition:** Person record updated; list reflects changes

---

## UC-003-01-04: Delete Person

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User clicks Delete button for a Person entry |

**Main Flow:**
1. System displays confirmation dialog
2. User confirms deletion
3. System deletes Person record
4. System updates Persons list

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | User cancels | Close dialog; no action taken |

**Postcondition:** Person record deleted; list updated

---

## UC-003-01-05: Filter Persons

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User enters filter criteria |

**Main Flow:**
1. User enters search text in filter field (matches firstName or lastName)
2. User clicks Filter button
3. System queries Person records matching criteria
4. System displays filtered results
5. Filter criteria persists during session

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 3a | No matching records | Display "No persons match the filter criteria" |
| 1a | User clicks Clear | Reset filter; display all records |

**Postcondition:** Filtered list displayed; filter criteria persisted

---

## Use Case Summary

| ID | Use Case | Parent Story | Actor(s) |
|----|----------|--------------|----------|
| UC-003-01-01 | View Persons List | US-003-01 | User, Administrator |
| UC-003-01-02 | Create Person | US-003-01 | User, Administrator |
| UC-003-01-03 | Edit Person | US-003-01 | User, Administrator |
| UC-003-01-04 | Delete Person | US-003-01 | User, Administrator |
| UC-003-01-05 | Filter Persons | US-003-01 | User, Administrator |

---

## Traceability Matrix

| User Story | Use Cases |
|------------|-----------|
| US-003-01: Person Management | UC-003-01-01, UC-003-01-02, UC-003-01-03, UC-003-01-04, UC-003-01-05 |
