# HX Qute - User Stories

**Product:** HX Qute Reference Application
**Company:** [Archton](https://www.archton.io/)
**Document Version:** 1.0

---

## Executive Summary

This is a reference application designed to provide a base for Quarkus/HTMX applications.

This document outlines the user stories that define the application's functionality from an end-user perspective.

The user stories are grouped into domain-specific features that deliver business value.

---

## Stakeholder Personas

- Individuals who want to manage people and their contact details and view the relationships between them
- System administrators who want to monitor and administer the system
- The CTO who needs a reusable reference application

---

## Feature 000: Foundation

### US-000-01: Establish Authentication Infrastructure
**As a** senior developer
**I want to** establish the database schema, entity classes, and services required for user authentication
**So that** the Identity and Access Management feature can be implemented on a solid foundation

**Acceptance Criteria:**
- UserLogin database table is created via Flyway migration with proper schema (id, email, password, role, first_name, last_name, created_at, updated_at, active)
- Email column has unique constraint and index for efficient lookups
- UserLogin entity is annotated with Quarkus Security JPA annotations (@UserDefinition, @Username, @Password, @Roles)
- Password hashing uses BCrypt with cost factor 12
- Email addresses are normalized to lowercase and trimmed
- PasswordValidator service enforces NIST SP 800-63B-4 requirements (minimum 15 characters, maximum 128 characters)
- Default admin user is seeded for testing (admin@example.com)
- Backend server starts successfully with all migrations applied

**Priority:** High
**Story Points:** 5

---

## Feature 001: Identity and Access Management

### US-001-01: User Registration
**As a** new user
**I want to** create an account with my email and a password
**So that** I can securely access and manage my data

**Acceptance Criteria:**
- User can access the signup page from the homepage
- User registers with their email address
- Email field requires valid email format
- Password field requires minimum 15 characters (NIST SP 800-63B-4)
- System prevents duplicate email addresses
- User receives clear error messages for validation failures
- Successful registration redirects user to login page

**Priority:** High
**Story Points:** 3

---

### US-001-02: User Login
**As a** registered user
**I want to** log in with my email and password
**So that** I can access my dashboard

**Acceptance Criteria:**
- Login page is accessible from the homepage navigation
- User can enter email and password
- Invalid credentials display an error message
- Successful login redirects to the home page with personalized greeting
- Navigation updates to show authenticated user options

**Priority:** High
**Story Points:** 2

---

### US-001-03: User Logout
**As an** authenticated user
**I want to** log out of my account
**So that** I can secure my data when I'm done using the application

**Acceptance Criteria:**
- Logout option is visible in the navigation bar when authenticated
- Clicking logout terminates the user session
- User is redirected to the homepage after logout
- Navigation updates to show unauthenticated user options

**Priority:** High
**Story Points:** 1

---

## Feature 002: Master Data Management

### US-002-01: View Gender Master Data
**As an** authenticated system administrator
**I want to** view a list of all codes and descriptions that describe a person's gender (Female, Male, Not specified)
**So that** I can view and update the list

**Acceptance Criteria:**
- Page is accessible from the navigation bar under the heading Maintenance
- Entries are displayed in a table format
- Each row shows: code, description
- Entries are sorted by code
- Empty state message displays when no entries exist

**Priority:** High
**Story Points:** 3

---

### US-002-02: Create New Gender
**As an** authenticated system administrator
**I want to** add a new entry
**So that** I can add new entries

**Acceptance Criteria:**
- Add button is visible on the list page
- Form includes: code and description
- Code and description are unique
- Maximum code length is 1 character
- Code is coerced to uppercase
- Entry has audit fields to record who created the entry and when
- Successful creation returns user to the list
- New entry appears in the list immediately

**Priority:** High
**Story Points:** 3

---

### US-002-03: Edit Existing Gender
**As an** authenticated system administrator
**I want to** modify an existing entry
**So that** I can correct errors or update details

**Acceptance Criteria:**
- Edit button is visible for each entry in the list
- Edit form pre-populates with existing data
- User can modify code and description
- Audit data fields are displayed but cannot be amended
- Entry has audit fields to record who updated the entry and when
- Changes are saved upon form submission
- User is returned to the  list after successful update
- Updated entry reflects changes immediately

**Priority:** High
**Story Points:** 2

---

### US-002-04: Delete Gender
**As an** authenticated system administrator
**I want to** delete an entry
**So that** I can remove incorrect or duplicate entries

**Acceptance Criteria:**
- Delete button is visible for each entry in the list
- Confirmation dialog appears before deletion
- Entry is removed from the list upon confirmation


**Priority:** High
**Story Points:** 2

---

### US-002-05: View Title Master Data
**As an** authenticated system administrator
**I want to** view a list of all codes and descriptions that describe a person's title or honorific (Mr, Ms, Mrs, Dr, Prof, Rev)
**So that** I can view and update the list

**Acceptance Criteria:**
- Page is accessible from the navigation bar under the heading Maintenance
- Entries are displayed in a table format
- Each row shows: code, description
- Entries are sorted by code
- Empty state message displays when no entries exist

**Priority:** High
**Story Points:** 3

---

### US-002-06: Create New Title
**As an** authenticated system administrator
**I want to** add a new entry
**So that** I can add new entries

**Acceptance Criteria:**
- Add button is visible on the list page
- Form includes: code and description
- Code and description are unique
- Maximum code length is 5 characters
- Code is coerced to uppercase
- Entry has audit fields to record who created the entry and when
- Successful creation returns user to the list
- New entry appears in the list immediately

**Priority:** High
**Story Points:** 3

---

### US-002-07: Edit Existing Title
**As an** authenticated system administrator
**I want to** modify an existing entry
**So that** I can correct errors or update details

**Acceptance Criteria:**
- Edit button is visible for each entry in the list
- Edit form pre-populates with existing data
- User can modify code and description
- Audit data fields are displayed but cannot be amended
- Entry has audit fields to record who updated the entry and when
- Changes are saved upon form submission
- User is returned to the list after successful update
- Updated entry reflects changes immediately

**Priority:** High
**Story Points:** 2

---

### US-002-08: Delete Title
**As an** authenticated system administrator
**I want to** delete an entry
**So that** I can remove incorrect or duplicate entries

**Acceptance Criteria:**
- Delete button is visible for each entry in the list
- Confirmation dialog appears before deletion
- Entry is removed from the list upon confirmation

**Priority:** High
**Story Points:** 2

---

### US-002-09: View Relationship Master Data
**As an** authenticated system administrator
**I want to** view a list of all codes and descriptions that describe a relationship between people (Spouse, Parent, Child, Sibling, Colleague, Friend)
**So that** I can view and update the list

**Acceptance Criteria:**
- Page is accessible from the navigation bar under the heading Maintenance
- Entries are displayed in a table format
- Each row shows: code, description
- Entries are sorted by code
- Empty state message displays when no entries exist

**Priority:** High
**Story Points:** 3

---

### US-002-10: Create New Relationship
**As an** authenticated system administrator
**I want to** add a new entry
**So that** I can add new entries

**Acceptance Criteria:**
- Add button is visible on the list page
- Form includes: code and description
- Code and description are unique
- Maximum code length is 10 characters
- Code is coerced to uppercase
- Entry has audit fields to record who created the entry and when
- Successful creation returns user to the list
- New entry appears in the list immediately

**Priority:** High
**Story Points:** 3

---

### US-002-11: Edit Existing Relationship
**As an** authenticated system administrator
**I want to** modify an existing entry
**So that** I can correct errors or update details

**Acceptance Criteria:**
- Edit button is visible for each entry in the list
- Edit form pre-populates with existing data
- User can modify code and description
- Audit data fields are displayed but cannot be amended
- Entry has audit fields to record who updated the entry and when
- Changes are saved upon form submission
- User is returned to the list after successful update
- Updated entry reflects changes immediately

**Priority:** High
**Story Points:** 2

---

### US-002-12: Delete Relationship
**As an** authenticated system administrator
**I want to** delete an entry
**So that** I can remove incorrect or duplicate entries

**Acceptance Criteria:**
- Delete button is visible for each entry in the list
- Confirmation dialog appears before deletion
- Entry is removed from the list upon confirmation

**Priority:** High
**Story Points:** 2

---

## Feature 003: Person Management

### US-003-01: View Persons List
**As an** authenticated user
**I want to** view a list of all the people that have been captured in the system
**So that** I can see all the people on the system and search by filtering the data

**Acceptance Criteria:**
- List of people page is accessible from the navigation bar
- People are displayed in a table format
- Each entry shows: lastName, firstName, email
- People are sorted by lastName then firstName
- Empty state message displays when no people exist

**Priority:** High
**Story Points:** 3

---

### US-003-02: Create New Person
**As an** authenticated user
**I want to** add a new Person
**So that** I can record the person's details

**Acceptance Criteria:**
- Add person button is visible on the persons page
- Form includes: lastName, firstName, title, email, phone, dateOfBirth, gender
- Date of birth field uses a date picker
- Gender and Title have dropdown select lists
- lastName and firstName are required
- Email field requires valid email format
- Email must be unique
- Form validates all required fields before submission
- Entry has audit fields to record who created the entry and when
- Successful creation returns user to the list
- New person appears in the list immediately

**Priority:** High
**Story Points:** 3

---

### US-003-03: Edit Existing Person
**As an** authenticated user
**I want to** modify an existing person
**So that** I can correct errors or update personal details

**Acceptance Criteria:**
- Edit button is visible for each person in the list
- Edit form pre-populates with existing data
- User can modify lastName, firstName, title, email, phone, dateOfBirth, gender
- Gender and Title have dropdown select lists
- lastName and firstName are required
- Email must remain unique
- Audit fields are visible but cannot be amended
- Entry has audit fields to record who updated the entry and when
- Changes are saved upon form submission
- User is returned to the persons list after successful update
- Updated person reflects changes immediately

**Priority:** Medium
**Story Points:** 2

---

### US-003-04: Delete Person
**As an** authenticated user
**I want to** delete a person
**So that** I can remove incorrect or duplicate entries

**Acceptance Criteria:**
- Delete button is visible for each person in the list
- Confirmation dialog appears before deletion
- Person is removed from the list upon confirmation

**Priority:** Medium
**Story Points:** 2

---

### US-003-05: Filter People
**As an** authenticated user
**I want to** filter the people by name or email
**So that** I can find and display people's details

**Acceptance Criteria:**
- Filter panel is visible on the people page above the table of people
- Filter: lastName, firstName, or email
- Filter button applies all selected filters
- Filtered results update the persons table
- Filters persist during the session
- The filter criteria can be cleared

**Priority:** Medium
**Story Points:** 3

---

### US-003-06: Sort People
**As an** authenticated user
**I want to** sort the people by name
**So that** I can organize and display people's details

**Acceptance Criteria:**
- Sort panel is visible on the people page above the table of people
- Sort: lastName or firstName
- Sort button applies all selected sorts
- Sorted results update the persons table
- Sorts persist during the session
- The sort criteria can be cleared

**Priority:** Medium
**Story Points:** 3

---

### US-003-07: Build relationships between people 
**As an** authenticated user
**I want to** connect one person to another
**So that** I can display the nature of relationships between people

**Acceptance Criteria:**
- Link button is visible on the table of people 
- Link button opens a new screen to show relationships between the selected person and their related people
- I can see the related people in a table with the nature of the relationship
- I can sort the list of related people
- I can filter the table to restrict the rerlationships
- I can add, edit and remove related people
- Sorted results update the persons table
- Sorts persist during the session
- The sort criteria can be cleared

**Priority:** Medium
**Story Points:** 3

