# HX Qute - Use Cases

**Product:** HX Qute Reference Application
**Document Version:** 1.0
**Last Updated:** 2025-12-27

---

## Overview

This document defines the use cases derived from the user stories and technical specifications. Each use case represents a discrete interaction between an actor and the system.

### Document References

| Document | Description |
|----------|-------------|
| USER-STORIES.md | User stories organized by epic |
| LOGIN-PHASED.md | Authentication technical specification (Phase 1 implementation) |
| SYSTEM-SPECIFICATION.md | Detailed technical requirements |

---

## UC-2: Gender Master Data Management

### UC-2.1: View Gender List
**Parent Story:** [US-2.1: View Gender Master Data](USER-STORIES.md#us-21-view-gender-master-data)

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

### UC-2.2: Create Gender
**Parent Story:** [US-2.2: Create New Gender](USER-STORIES.md#us-22-create-new-gender)

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is on Gender list page |
| Trigger | User clicks Add button |

**Main Flow:**
1. System displays create form with fields: code, description
2. User enters code (max 7 characters)
3. User enters description
4. User submits form
5. System coerces code to uppercase
6. System validates uniqueness of code and description
7. System sets audit fields (createdBy, createdAt)
8. System persists new Gender record
9. System redirects to Gender list

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | Code empty | Display validation error |
| 2b | Code > 7 characters | Display "Code must be 7 characters or less" |
| 3a | Description empty | Display validation error |
| 6a | Code already exists | Display "Code already exists" error |
| 6b | Description already exists | Display "Description already exists" error |

**Postcondition:** New Gender record created; list updated

---

### UC-2.3: Edit Gender
**Parent Story:** [US-2.3: Edit Existing Gender](USER-STORIES.md#us-23-edit-existing-gender)

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is on Gender list page |
| Trigger | User clicks Edit button for a Gender entry |

**Main Flow:**
1. System retrieves Gender record by ID
2. System displays edit form pre-populated with current values
3. System displays audit fields (read-only)
4. User modifies code and/or description
5. User submits form
6. System coerces code to uppercase
7. System validates uniqueness of code and description
8. System updates audit fields (updatedBy, updatedAt)
9. System persists updated Gender record
10. System redirects to Gender list

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 1a | Gender not found | Display error; redirect to list |
| 7a | Code conflicts with another record | Display "Code already exists" error |
| 7b | Description conflicts with another record | Display "Description already exists" error |

**Postcondition:** Gender record updated; list reflects changes

---

### UC-2.4: Delete Gender
**Parent Story:** [US-2.4: Delete Gender](USER-STORIES.md#us-24-delete-gender)

| Attribute | Value |
|-----------|-------|
| Actor | Administrator |
| Precondition | User is on Gender list page |
| Trigger | User clicks Delete button for a Gender entry |

**Main Flow:**
1. System displays confirmation dialog
2. User confirms deletion
3. System deletes Gender record
4. System updates Gender list

**Alternative Flows:**

| ID | Condition | Action |
|----|-----------|--------|
| 2a | User cancels | Close dialog; no action taken |
| 3a | Gender in use by Person records | Display "Cannot delete: Gender is in use" error |

**Postcondition:** Gender record deleted; list updated

---

## UC-3: Persons Management

### UC-3.1: View Persons List
**Parent Story:** [US-3.1: View Persons List](USER-STORIES.md#us-31-view-persons-list)

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

### UC-3.2: Create Person
**Parent Story:** [US-3.2: Create New Person](USER-STORIES.md#us-32-create-new-person)

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
| 10a | Email already exists | Display "Email already registered" error |

**Postcondition:** New Person record created; list updated

---

### UC-3.3: Edit Person
**Parent Story:** [US-3.3: Edit Existing Person](USER-STORIES.md#us-33-edit-existing-person)

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

### UC-3.4: Delete Person
**Parent Story:** [US-3.4: Delete Person](USER-STORIES.md#us-34-delete-person)

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

### UC-3.5: Filter Persons
**Parent Story:** [US-3.5: Filter People](USER-STORIES.md#us-35-filter-people)

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
| UC-1.1 | Display Signup Page | US-1.1 | Guest |
| UC-1.2 | Register New User | US-1.1 | Guest |
| UC-1.3 | Display Login Page | US-1.2 | Guest |
| UC-1.4 | Authenticate User | US-1.2 | Guest |
| UC-1.5 | Logout User | US-1.3 | User, Administrator |
| UC-1.6 | Access Protected Route | US-1.2 | Guest |
| UC-2.1 | View Gender List | US-2.1 | Administrator |
| UC-2.2 | Create Gender | US-2.2 | Administrator |
| UC-2.3 | Edit Gender | US-2.3 | Administrator |
| UC-2.4 | Delete Gender | US-2.4 | Administrator |
| UC-3.1 | View Persons List | US-3.1 | User, Administrator |
| UC-3.2 | Create Person | US-3.2 | User, Administrator |
| UC-3.3 | Edit Person | US-3.3 | User, Administrator |
| UC-3.4 | Delete Person | US-3.4 | User, Administrator |
| UC-3.5 | Filter Persons | US-3.5 | User, Administrator |

---

## Traceability Matrix

| User Story | Use Cases |
|------------|-----------|
| US-1.1: User Registration | UC-1.1, UC-1.2 |
| US-1.2: User Login | UC-1.3, UC-1.4, UC-1.6 |
| US-1.3: User Logout | UC-1.5 |
| US-2.1: View Gender Master Data | UC-2.1 |
| US-2.2: Create New Gender | UC-2.2 |
| US-2.3: Edit Existing Gender | UC-2.3 |
| US-2.4: Delete Gender | UC-2.4 |
| US-3.1: View Persons List | UC-3.1 |
| US-3.2: Create New Person | UC-3.2 |
| US-3.3: Edit Existing Person | UC-3.3 |
| US-3.4: Delete Person | UC-3.4 |
| US-3.5: Filter People | UC-3.5 |
