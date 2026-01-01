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
1. System displays create form in modal dialog
2. Form includes fields: firstName, lastName, title (dropdown), email, phone, dateOfBirth, gender (dropdown)
3. Form includes Save and Cancel buttons
4. Title dropdown populated from Title.listAllOrdered()
5. Gender dropdown populated from Gender.listAllOrdered()

**Postcondition:** Create modal is displayed

---

## UC-003-02-02: Submit Create Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has filled create form |
| Trigger | User clicks Save button |

**Main Flow:**
1. System validates firstName is not empty
2. System validates lastName is not empty
3. System validates email is not empty
4. System validates email format
5. System normalizes email to lowercase
6. System validates email uniqueness
7. System links title if titleId provided
8. System links gender if genderId provided
9. System sets audit fields (createdBy, createdAt, updatedBy, updatedAt)
10. System persists new Person record
11. System closes modal
12. System refreshes Persons table via OOB swap

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | firstName empty | Display "First name is required." error |
| 2a | lastName empty | Display "Last name is required." error |
| 3a | Email empty | Display "Email is required." error |
| 4a | Email invalid format | Display "Invalid email format." error |
| 6a | Email already exists | Display "Email already registered." error |

**Postcondition:** New Person record created; modal closed; table updated

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
2. System displays edit form in modal dialog
3. Form pre-populates all fields with current values
4. Title dropdown shows current selection
5. Gender dropdown shows current selection
6. Form displays audit fields (read-only, in collapsible section)
7. Form includes Save and Cancel buttons

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Person not found | Display error message in modal |

**Postcondition:** Edit modal displayed with pre-populated data

---

## UC-003-03-02: Submit Edit Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has modified edit form |
| Trigger | User clicks Save button |

**Main Flow:**
1. System validates firstName is not empty
2. System validates lastName is not empty
3. System validates email is not empty
4. System validates email format
5. System normalizes email to lowercase
6. System validates email uniqueness (excluding current record)
7. System updates title link if changed
8. System updates gender link if changed
9. System updates audit fields (updatedBy, updatedAt)
10. System persists updated Person record
11. System closes modal
12. System updates table row via OOB swap

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | firstName empty | Display "First name is required." error |
| 2a | lastName empty | Display "Last name is required." error |
| 3a | Email empty | Display "Email is required." error |
| 4a | Email invalid format | Display "Invalid email format." error |
| 6a | Email conflicts with another record | Display "Email already registered." error |

**Postcondition:** Person record updated; modal closed; row reflects changes

---

## UC-003-03-03: Cancel Edit

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is viewing edit modal for a Person entry |
| Trigger | User clicks Cancel button |

**Main Flow:**
1. Modal closes (via uk-modal-close class)
2. No changes are saved

**Postcondition:** Modal closed; original values preserved

---

# US-003-04: Delete Person

## UC-003-04-01: Delete Person

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User clicks Delete button for a Person entry |

**Main Flow:**
1. System displays delete confirmation modal with person's name
2. Modal shows warning message and Delete/Cancel buttons
3. User clicks Delete button
4. System deletes Person record
5. System closes modal
6. System removes row from list via OOB swap

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 3a | User clicks Cancel | Modal closes; no action taken |

**Postcondition:** Person record deleted; modal closed; row removed from list

---

# US-003-05: Filter People

## UC-003-05-01: Apply Filter

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User enters filter criteria and clicks Filter button (or types with 300ms debounce) |

**Main Flow:**
1. User enters search text in filter field
2. User clicks Filter button (or waits for debounce)
3. System queries Person records matching criteria (firstName, lastName, or email contains search text)
4. System displays filtered results
5. URL is updated with filter query parameter (hx-push-url)

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 3a | No matching records | Display "No persons match the filter criteria" message |

**Postcondition:** Filtered list displayed; URL reflects filter state

---

## UC-003-05-02: Clear Filter

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has applied a filter |
| Trigger | User clicks Clear button |

**Main Flow:**
1. Browser navigates to `/persons` (no query parameters)
2. System retrieves all Person records with default sort
3. System displays full list

**Postcondition:** Filter cleared; full list displayed; URL clean

---

# US-003-06: Sort People

## UC-003-06-01: Apply Sort

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is on Persons list page |
| Trigger | User selects sort option and clicks Filter button |

**Main Flow:**
1. User selects sort field (lastName, firstName, or email)
2. User selects sort direction (ascending or descending)
3. User clicks Filter button
4. System reorders Person records by selected criteria
5. System displays sorted results
6. URL is updated with sort query parameters (hx-push-url)

**Postcondition:** Sorted list displayed; URL reflects sort state

---

## UC-003-06-02: Clear Sort

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has applied a custom sort |
| Trigger | User clicks Clear button |

**Main Flow:**
1. Browser navigates to `/persons` (no query parameters)
2. System applies default sort (lastName, firstName ascending)
3. System displays sorted list

**Postcondition:** Sort cleared; default sort applied; URL clean

---

# US-003-07: Build Relationships Between People

## UC-003-07-01: View Person Relationships

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is authenticated; Person record exists |
| Trigger | User clicks Link button for a Person entry in the persons table |

**Main Flow:**
1. System retrieves the selected Person record
2. System retrieves all PersonRelationship records where this person is the source
3. System displays a new page with:
   - Header showing the source person's name
   - Filter bar with search input, sort dropdowns
   - Add Relationship button
   - Table of related people showing: Related Person name, Relationship type, Actions (Edit, Delete)
   - Back button to return to persons list
4. System sorts records by related person lastName, then firstName (ascending) by default

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 3a | No relationships exist | Display "No relationships found for this person" message |
| 1a | Person not found | Display error message, redirect to persons list |

**Postcondition:** Person relationships page displayed with list of related people

---

## UC-003-07-02: Display Add Relationship Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is viewing person relationships page |
| Trigger | User clicks Add Relationship button |

**Main Flow:**
1. System displays add relationship form in modal dialog
2. Form includes:
   - Related Person dropdown (all persons except current person)
   - Relationship Type dropdown (from Relationship master data)
   - Save and Cancel buttons
3. Related Person dropdown populated from Person.listAllOrdered() excluding current person
4. Relationship Type dropdown populated from Relationship.listAllOrdered()

**Postcondition:** Add relationship modal is displayed

---

## UC-003-07-03: Submit Add Relationship Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has filled add relationship form |
| Trigger | User clicks Save button |

**Main Flow:**
1. System validates related person is selected
2. System validates relationship type is selected
3. System validates relationship does not already exist (same source person, related person, and relationship type)
4. System sets audit fields (createdBy, createdAt, updatedBy, updatedAt)
5. System persists new PersonRelationship record
6. System closes modal
7. System refreshes relationships table via OOB swap

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Related person not selected | Display "Please select a person." error |
| 2a | Relationship type not selected | Display "Please select a relationship type." error |
| 3a | Relationship already exists | Display "This relationship already exists." error |

**Postcondition:** New relationship created; modal closed; table updated

---

## UC-003-07-04: Display Edit Relationship Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is viewing person relationships page |
| Trigger | User clicks Edit button for a relationship entry |

**Main Flow:**
1. System retrieves PersonRelationship record by ID
2. System displays edit relationship form in modal dialog
3. Form pre-populates with current values:
   - Related Person dropdown with current selection (read-only or changeable based on requirements)
   - Relationship Type dropdown with current selection
4. Form displays audit fields (read-only, in collapsible section)
5. Form includes Save and Cancel buttons

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Relationship not found | Display error message in modal |

**Postcondition:** Edit relationship modal displayed with pre-populated data

---

## UC-003-07-05: Submit Edit Relationship Form

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has modified edit relationship form |
| Trigger | User clicks Save button |

**Main Flow:**
1. System validates related person is selected
2. System validates relationship type is selected
3. System validates no duplicate relationship (excluding current record)
4. System updates audit fields (updatedBy, updatedAt)
5. System persists updated PersonRelationship record
6. System closes modal
7. System updates table row via OOB swap

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Related person not selected | Display "Please select a person." error |
| 2a | Relationship type not selected | Display "Please select a relationship type." error |
| 3a | Duplicate relationship would be created | Display "This relationship already exists." error |

**Postcondition:** Relationship updated; modal closed; row reflects changes

---

## UC-003-07-06: Delete Relationship

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is viewing person relationships page |
| Trigger | User clicks Delete button for a relationship entry |

**Main Flow:**
1. System displays delete confirmation modal with relationship details
2. Modal shows warning message: "Are you sure you want to remove this relationship?"
3. Modal displays related person name and relationship type
4. Modal shows Delete and Cancel buttons
5. User clicks Delete button
6. System deletes PersonRelationship record
7. System closes modal
8. System removes row from list via OOB swap

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 5a | User clicks Cancel | Modal closes; no action taken |

**Postcondition:** Relationship deleted; modal closed; row removed from list

---

## UC-003-07-07: Apply Relationship Filter

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is viewing person relationships page |
| Trigger | User enters filter criteria and clicks Filter button (or types with 300ms debounce) |

**Main Flow:**
1. User enters search text in filter field
2. User clicks Filter button (or waits for debounce)
3. System queries PersonRelationship records matching criteria:
   - Related person firstName, lastName contains search text, OR
   - Relationship type description contains search text
4. System displays filtered results
5. URL is updated with filter query parameter (hx-push-url)

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 3a | No matching records | Display "No relationships match the filter criteria" message |

**Postcondition:** Filtered list displayed; URL reflects filter state

---

## UC-003-07-08: Clear Relationship Filter

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has applied a filter on relationships page |
| Trigger | User clicks Clear button |

**Main Flow:**
1. Browser navigates to relationships page (no filter query parameters, preserves sort if any)
2. System retrieves all PersonRelationship records for this person with current sort
3. System displays full list

**Postcondition:** Filter cleared; full list displayed

---

## UC-003-07-09: Apply Relationship Sort

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User is viewing person relationships page |
| Trigger | User selects sort option and clicks Filter button |

**Main Flow:**
1. User selects sort field (relatedPerson.lastName, relatedPerson.firstName, relationship.description)
2. User selects sort direction (ascending or descending)
3. User clicks Filter button
4. System reorders PersonRelationship records by selected criteria
5. System displays sorted results
6. URL is updated with sort query parameters (hx-push-url)

**Postcondition:** Sorted list displayed; URL reflects sort state

---

## UC-003-07-10: Clear Relationship Sort

| Attribute | Value |
|-----------|-------|
| Actor | User, Administrator |
| Precondition | User has applied a custom sort on relationships page |
| Trigger | User clicks Clear button |

**Main Flow:**
1. Browser navigates to relationships page (clears sort parameters, preserves filter if any)
2. System applies default sort (related person lastName, firstName ascending)
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
| UC-003-07-01 | View Person Relationships | US-003-07 | User, Administrator |
| UC-003-07-02 | Display Add Relationship Form | US-003-07 | User, Administrator |
| UC-003-07-03 | Submit Add Relationship Form | US-003-07 | User, Administrator |
| UC-003-07-04 | Display Edit Relationship Form | US-003-07 | User, Administrator |
| UC-003-07-05 | Submit Edit Relationship Form | US-003-07 | User, Administrator |
| UC-003-07-06 | Delete Relationship | US-003-07 | User, Administrator |
| UC-003-07-07 | Apply Relationship Filter | US-003-07 | User, Administrator |
| UC-003-07-08 | Clear Relationship Filter | US-003-07 | User, Administrator |
| UC-003-07-09 | Apply Relationship Sort | US-003-07 | User, Administrator |
| UC-003-07-10 | Clear Relationship Sort | US-003-07 | User, Administrator |

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
| US-003-07: Build Relationships Between People | UC-003-07-01, UC-003-07-02, UC-003-07-03, UC-003-07-04, UC-003-07-05, UC-003-07-06, UC-003-07-07, UC-003-07-08, UC-003-07-09, UC-003-07-10 |
