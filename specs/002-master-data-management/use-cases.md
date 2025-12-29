# Use Cases for Feature 002: Master Data Management

This feature provides CRUD operations for master data entities used throughout the application.

## Actors

| Actor | Description |
|-------|-------------|
| Administrator | Authenticated user with "admin" role |

---

# US-002-01: Gender Master Data

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

## UC-002-01-02: Create Gender

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

## UC-002-01-03: Edit Gender

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

## UC-002-01-04: Delete Gender

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

## Use Case Summary

| ID | Use Case | Parent Story | Actor |
|----|----------|--------------|-------|
| UC-002-01-01 | View Gender List | US-002-01 | Administrator |
| UC-002-01-02 | Create Gender | US-002-01 | Administrator |
| UC-002-01-03 | Edit Gender | US-002-01 | Administrator |
| UC-002-01-04 | Delete Gender | US-002-01 | Administrator |

---

## Traceability Matrix

| User Story | Use Cases |
|------------|-----------|
| US-002-01: Gender Master Data | UC-002-01-01, UC-002-01-02, UC-002-01-03, UC-002-01-04 |
