# HX Qute - User Stories

**Product:** HX Qute Reference Application
**Company:** [Archton](https://www.archton.io/)
**Document Version:** 1.0

---

## Executive Summary

This is a reference application designed to provide a base for Quarkus/HTMX applications.

This document outlines the user stories that define the application's functionality from an end-user perspective.

---

## Stakeholder Personas

- Individuals who want to manage people and their contact details and view the relationships between them
- System administrators who want to monitor and administer the system
- The CTO who needs a reusable reference application

---

## Epic 1: User Authentication & Account Management

### US-1.1: User Registration
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

### US-1.2: User Login
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

### US-1.3: User Logout
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

## Epic 2: Master Data Management

### US-2.1: View Gender Master Data
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

### US-2.2: Create New Gender
**As an** authenticated system administrator
**I want to** add a new entry
**So that** I can add new entries

**Acceptance Criteria:**
- Add button is visible on the list page
- Form includes: code and description
- Code and description are unique
- Maximum code length is 7 characters
- Code is coerced to uppercase
- Entry has audit fields to record who created the entry and when
- Successful creation returns user to the list
- New entry appears in the list immediately

**Priority:** High
**Story Points:** 3

---

### US-2.3: Edit Existing Gender
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

### US-2.4: Delete Gender
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

## Epic 3: Persons Management

### US-3.1: View Persons List
**As an** authenticated user
**I want to** view a list of all the people that have been captured in the system
**So that** I can see all the people on the system and search by filtering the data

**Acceptance Criteria:**
- List of people page is accessible from the navigation bar
- People are displayed in a table format
- Each entry shows: firstName, lastName, email, phone, dateOfBirth
- People are sorted by lastName then firstName
- Empty state message displays when no people exist

**Priority:** High
**Story Points:** 3

---

### US-3.2: Create New Person
**As an** authenticated user
**I want to** add a new Person
**So that** I can record the person's details

**Acceptance Criteria:**
- Add person button is visible on the persons page
- Form includes: firstName, lastName, email, phone, dateOfBirth
- Date of birth field uses a date picker
- Email field requires valid email format
- Email must be unique
- Form validates all required fields before submission
- Entry has audit fields to record who created the entry and when
- Successful creation returns user to the list
- New person appears in the list immediately

**Priority:** High
**Story Points:** 3

---

### US-3.3: Edit Existing Person
**As an** authenticated user
**I want to** modify an existing person
**So that** I can correct errors or update personal details

**Acceptance Criteria:**
- Edit button is visible for each person in the list
- Edit form pre-populates with existing data
- User can modify firstName, lastName, email, phone, dateOfBirth
- Email must remain unique
- Audit fields are visible but cannot be amended
- Entry has audit fields to record who updated the entry and when
- Changes are saved upon form submission
- User is returned to the persons list after successful update
- Updated person reflects changes immediately

**Priority:** Medium
**Story Points:** 2

---

### US-3.4: Delete Person
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

### US-3.5: Filter People
**As an** authenticated user
**I want to** filter the people by name
**So that** I can find and display people's details

**Acceptance Criteria:**
- Filter panel is visible on the people page above the table of people
- Filter: lastName or firstName
- Filter button applies all selected filters
- Filtered results update the persons table
- Filters persist during the session
- The filter criteria can be cleared

**Priority:** Medium
**Story Points:** 3

---

## Non-Functional Requirements

### NFR-1: Performance
- Page load time should be under 2 seconds
- HTMX partial updates should be near-instantaneous

### NFR-2: Security
- User passwords must be securely hashed
- Sessions must expire after period of inactivity
- Role-based access control (user/admin roles)

### NFR-3: Usability
- Responsive design for mobile and desktop
- Intuitive navigation with clear labels

### NFR-4: Browser Compatibility
- Support for modern browsers (Chrome, Firefox, Safari, Edge)
- Graceful degradation for older browsers

