# Codebase Concerns

**Analysis Date:** 2026-02-14

## Tech Debt

**Missing Referential Integrity Checks on Delete:**
- Issue: Delete endpoints in `TitleResource`, `GenderResource`, and `RelationshipResource` don't validate whether the records are in use before deletion. TODOs explicitly state validation is missing.
- Files: `src/main/java/io/archton/scaffold/router/TitleResource.java:232`, `src/main/java/io/archton/scaffold/router/GenderResource.java:232`, `src/main/java/io/archton/scaffold/router/RelationshipResource.java:234`
- Impact: Orphaned foreign key references in `person` and `person_relationship` tables if a referenced Title, Gender, or Relationship is deleted. Database constraints will prevent deletion but users see no helpful error message.
- Fix approach: Query `PersonRepository` and `PersonRelationshipRepository` to check usage before deletion. Return validation error in modal if in-use. Consider cascading delete or restrict-with-error pattern based on business rules.

**Incomplete Test Coverage:**
- Issue: Test directory exists (`src/test/java/`) but contains no test files. Zero automated test coverage for business logic.
- Files: `src/test/java/` (empty)
- Impact: Critical business logic lacks automated validation. Regressions will not be caught. Resource endpoints, repository queries, and entity lifecycle hooks are untested.
- Fix approach: Add JUnit5 + REST Assured tests for:
  1. All CRUD endpoints with valid/invalid inputs
  2. Validation error cases (duplicate emails, malformed dates, required fields)
  3. Authorization/role-based access control
  4. Repository query behavior (filtering, sorting, eager loading)
  5. Transaction boundaries and audit field updates

**Email Regex Pattern Mismatch:**
- Issue: `PersonResource` uses regex `^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$` while `AuthResource` uses `^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$`. Different validation rules create user confusion.
- Files: `src/main/java/io/archton/scaffold/router/PersonResource.java:156`, `src/main/java/io/archton/scaffold/router/AuthResource.java:35`
- Impact: User can create account with email format that fails in Person creation form, or vice versa. Inconsistent data validation reduces reliability.
- Fix approach: Extract email validation to shared utility class. Use stricter pattern with character class limits. Consider RFC 5322 compliant validation.

**Date Parsing Silent Failure:**
- Issue: `PersonResource.create()` and `PersonResource.update()` silently ignore date parsing exceptions. Invalid dates are stored as NULL without user feedback.
- Files: `src/main/java/io/archton/scaffold/router/PersonResource.java:179-185`, `src/main/java/io/archton/scaffold/router/PersonResource.java:272-278`
- Impact: Users believe date was saved but it's NULL. No validation error shown. Silent data loss.
- Fix approach: Catch `DateTimeParseException`, return validation error in modal. Add server-side date validation decorator.

**Manual Entity Graph Usage:**
- Issue: `PersonRelationshipRepository` manually constructs EntityGraph hints instead of using Panache convenience methods.
- Files: `src/main/java/io/archton/scaffold/repository/PersonRelationshipRepository.java:12`, `src/main/java/io/archton/scaffold/repository/PersonRelationshipRepository.java:29`
- Impact: More verbose code, harder to test, potential for NPE if entity graph name is wrong. Couples repository to EntityManager API.
- Fix approach: Verify EntityGraph name matches `@NamedEntityGraph` annotation. Consider creating Panache-specific eager loading method.

**Panache Repository Mismatch in PersonRepository:**
- Issue: `PersonRepository.findByFilter()` uses `.find()` with string queries but most methods use `.list()`. Inconsistent usage pattern.
- Files: `src/main/java/io/archton/scaffold/repository/PersonRepository.java:37-43`
- Impact: Inconsistency makes code harder to maintain. Mixed API styles suggest incomplete refactoring.
- Fix approach: Standardize on either fluent API or string queries. Prefer fluent `find().list()` throughout.

## Known Bugs

**Lazy Loading N+1 in Person List:**
- Symptoms: Person list page loads slowly when rendering many persons with titles. SQL logs show query per person for title.
- Files: `src/main/java/io/archton/scaffold/router/PersonResource.java:107`, `src/main/java/io/archton/scaffold/entity/Person.java:33-35`
- Trigger: Load `/persons` page. SQL profiler reveals SELECT for each row's title.
- Workaround: Person list uses `findByFilter()` which doesn't eagerly load titles, but templates may access `person.title.description` causing lazy loads.
- Solution: Ensure `findByFilter()` uses eager loading via EntityGraph, similar to `findBySourcePersonWithFilter()` in `PersonRelationshipRepository`.

**Email Case Sensitivity Edge Case:**
- Symptoms: Email validation during signup accepts "User@Example.com" but Person creation may reject it if "user@example.com" exists (depends on database collation).
- Files: `src/main/java/io/archton/scaffold/router/PersonResource.java:218`, `src/main/java/io/archton/scaffold/entity/Person.java:73-75`
- Trigger: Create account with mixed case email, then try to create Person with lowercase version.
- Cause: Code normalizes to lowercase but doesn't consistently apply before all queries.
- Workaround: Always use lowercase when testing.

**Missing null Check Before Display Name Render:**
- Symptoms: If `Person.title` is lazy-loaded but entity is detached, `getDisplayName()` may throw exception when accessing `title.description`.
- Files: `src/main/java/io/archton/scaffold/entity/Person.java:59-65`
- Cause: `title` field is lazy-loaded. If accessed outside transaction, raises `LazyInitializationException`.
- Trigger: Rare but possible if template renders person after session closes.

## Security Considerations

**Session Timeout Configuration:**
- Risk: Session timeout set to 30 minutes (`PT30M`). If user walks away, credentials stay valid for extended period.
- Files: `src/main/resources/application.properties:25`
- Current mitigation: `http-only` cookie prevents JavaScript access. `same-site=strict` prevents CSRF.
- Recommendations:
  1. Consider reducing timeout to 15 minutes for admin functions.
  2. Implement server-side session invalidation on logout.
  3. Add "session about to expire" warning at 5-minute mark.

**Email Validation Insufficient:**
- Risk: Simple regex allows syntactically valid but non-existent emails. No confirmation required.
- Files: `src/main/java/io/archton/scaffold/router/AuthResource.java:35`, `src/main/java/io/archton/scaffold/router/PersonResource.java:156`
- Current mitigation: Email must be provided and match regex.
- Recommendations:
  1. Require email verification on signup (send confirmation link).
  2. Add SMTP validation (MX record check) before accept.
  3. Consider rate limiting signup endpoint.

**Password Hashing Cost Factor:**
- Risk: BCrypt cost factor set to 12. Current CPU speeds can crack cost-12 hashes in reasonable time. NIST recommends cost ≥ 13.
- Files: `src/main/java/io/archton/scaffold/service/UserLoginService.java:35`
- Current mitigation: Cost 12 is reasonable for 2024, still requires ~100ms per attempt.
- Recommendations:
  1. Upgrade to cost 13+ when baseline CPU improves.
  2. Monitor password breach databases and force resets if compromised.
  3. Consider adding rate limiting to login endpoint.

**SQL Injection via String Concatenation:**
- Risk: Query strings built with manual concatenation in `PersonRelationshipRepository.buildOrderBy()` and `PersonRepository.buildOrderBy()`.
- Files: `src/main/java/io/archton/scaffold/repository/PersonRelationshipRepository.java:47-54`, `src/main/java/io/archton/scaffold/repository/PersonRepository.java:46-54`
- Current mitigation: Switch expression only allows whitelisted field names. No user input directly in query.
- Recommendations:
  1. Document that `sortField` is validated before use (currently only field names allowed).
  2. Add unit tests confirming invalid field names are rejected.

**No Input Sanitization for XSS:**
- Risk: User-provided data (firstName, lastName, notes) rendered directly in Qute templates without escaping.
- Files: Templates receive strings from `PersonResource`, `PersonRelationshipResource` etc.
- Current mitigation: Qute auto-escapes by default unless marked `{@safe:...}`.
- Recommendations:
  1. Verify Qute escaping is enabled in all templates.
  2. Test with HTML/JavaScript in firstName field (e.g., `<script>alert('xss')</script>`).

## Performance Bottlenecks

**Repeated Dropdown Queries:**
- Problem: Every form render (create, edit, list) calls `titleRepository.listAllOrdered()` and `genderRepository.listAllOrdered()` multiple times per request.
- Files: `src/main/java/io/archton/scaffold/router/PersonResource.java:116-117`, `src/main/java/io/archton/scaffold/router/PersonResource.java:137-138`, `src/main/java/io/archton/scaffold/router/PersonResource.java:150-151`, `src/main/java/io/archton/scaffold/router/PersonResource.java:197-198`, `src/main/java/io/archton/scaffold/router/PersonResource.java:255-256`, `src/main/java/io/archton/scaffold/router/PersonResource.java:282`
- Cause: Stateless REST resources don't cache. Each dropdown fetch is a database query.
- Impact: N request handlers × 2-3 dropdowns = 6-9 queries per page load. With 100+ users, 600-900 dropdown queries per second on high traffic.
- Improvement path:
  1. Short-lived cache (5-10 min) for Title/Gender/Relationship lists in service layer.
  2. Use `@CacheResult` annotation on repository methods.
  3. Cache invalidated on create/update/delete of cached entities.

**Inefficient Filtering with LIKE:**
- Problem: `findByFilter()` and `findBySourcePersonWithFilter()` use `LOWER(...) LIKE ?` pattern matching on indexed columns.
- Files: `src/main/java/io/archton/scaffold/repository/PersonRepository.java:36-40`, `src/main/java/io/archton/scaffold/repository/PersonRelationshipRepository.java:34-39`
- Cause: `LOWER()` function prevents index usage on most databases. `LIKE` pattern match with leading `%` is slow.
- Impact: Full table scan on large datasets. Response time degrades from ms to seconds as person/relationship counts grow.
- Improvement path:
  1. Add database index on `LOWER(firstName)`, `LOWER(lastName)` if supported.
  2. Implement full-text search index (PostgreSQL `tsvector`).
  3. Add pagination/limit to query results.
  4. Move expensive filtering to JavaScript client-side for small datasets.

**Unbounded Query Results:**
- Problem: `findByFilter()` and related queries don't paginate. Loading 100,000 persons into memory as `List<Person>` will exhaust heap.
- Files: `src/main/java/io/archton/scaffold/repository/PersonRepository.java:32-44`, `src/main/java/io/archton/scaffold/repository/PersonRelationshipRepository.java:26-45`
- Impact: Out-of-memory error if dataset exceeds heap. No graceful degradation. UI becomes unresponsive.
- Improvement path:
  1. Add pagination support with `limit` and `offset` parameters.
  2. Implement cursor-based pagination for better performance.
  3. Add total count endpoint for UI pagination controls.

## Fragile Areas

**PersonRelationshipResource - Large CRUD Handler:**
- Files: `src/main/java/io/archton/scaffold/router/PersonRelationshipResource.java` (456 lines)
- Why fragile: Single class handles 456 lines of CRUD logic with minimal abstraction. Hard to modify without introducing bugs. Template methods are tightly coupled to business logic.
- Safe modification:
  1. Extract validation logic into service class.
  2. Create separate methods for fetching choices (DRY principle).
  3. Test each CRUD operation independently.
- Test coverage: Zero automated tests. Manual testing required for each change.

**Entity Relationship Cycles:**
- Files: `src/main/java/io/archton/scaffold/entity/PersonRelationship.java`, `src/main/java/io/archton/scaffold/entity/Person.java`
- Why fragile: `PersonRelationship` has bidirectional relationship (`sourcePerson` and `relatedPerson` both reference `Person`). Circular references risk infinite loops if serialized incorrectly or if cascading deletes are misconfigured.
- Test coverage: No tests validate cascade behavior or JSON serialization.

**Template Fragment Naming Convention:**
- Files: Throughout `PersonResource`, `PersonRelationshipResource`, etc. - `Templates.person$table`, `Templates.person$modal_create`
- Why fragile: Dollar-sign naming convention is not self-documenting. Fragment names tightly couple resource to template structure. Renaming template breaks resource without compiler feedback (some templates are runtime-discovered).
- Safe modification: Extract fragment constant names to class. Document expected Qute template file locations.

## Scaling Limits

**No Pagination:**
- Current capacity: 1000 persons displayable; beyond that page becomes unusable.
- Limit: At 10,000 persons, rendering 10k table rows + fetching all Titles/Genders × 10 = overload.
- Scaling path:
  1. Implement limit/offset pagination (10-50 rows per page).
  2. Add page numbers to UI.
  3. Pre-fetch page size from client preference.

**Single Database Connection:**
- Current capacity: ~10 concurrent requests per database connection pool.
- Limit: At 100 concurrent users, connection pool exhaustion causes request queueing.
- Scaling path:
  1. Increase `quarkus.datasource.max-size` from default (20) to 50-100.
  2. Monitor connection pool metrics.
  3. Add database read replicas for read-heavy endpoints.

**No Caching Layer:**
- Current capacity: Every request hits database. 100 users = 100s of queries/sec.
- Limit: Database CPU maxes out ~1000 queries/sec depending on hardware.
- Scaling path:
  1. Add Redis or Infinispan cache for dropdown lists.
  2. Cache person search results with 30-60 second TTL.
  3. Implement Hibernate second-level cache.

## Dependencies at Risk

**BCrypt Algorithm Aging:**
- Risk: BCrypt designed in 1999. Newer algorithms (Argon2, scrypt) offer better GPU resistance.
- Impact: If password database is breached, Argon2 makes cracking harder than BCrypt.
- Migration plan: Switch to Argon2 (via Bouncy Castle or Quarkus security extension). Implement password re-hashing on login to gradually migrate.

**Flyway Migrations No Rollback:**
- Risk: Flyway doesn't support rollback by default. If a migration fails in production, manual intervention needed.
- Impact: Schema changes are one-way. Bad migration can't be automatically reverted.
- Mitigation:
  1. Test migrations in staging before production.
  2. Plan rollback procedure manually (V1.X.0_Rollback script).
  3. Keep backup of database before running migrate-at-start.

## Missing Critical Features

**No Audit Logging:**
- Problem: `createdBy`/`updatedBy` fields exist but no audit log table. Cannot track who deleted what, when, or why.
- Blocks: Compliance with HIPAA/SOX if handling sensitive data. Cannot debug data corruption.
- Files: Audit fields populated in all entities but no audit trail table exists.

**No Session/JWT Token Refresh:**
- Problem: Session expires after 30 minutes. User loses work with no warning.
- Blocks: Long-form data entry (e.g., importing large relationship graphs) requires completion within session timeout.

**No Delete Cascade Validation:**
- Problem: Cannot delete Title/Gender/Relationship if in use. But user sees generic database error, not helpful message.
- Blocks: Business process incomplete - users don't know why delete failed.

## Test Coverage Gaps

**No Endpoint Testing:**
- What's not tested: All REST endpoints (PersonResource, PersonRelationshipResource, TitleResource, etc.)
- Files: All files in `src/main/java/io/archton/scaffold/router/` have zero test coverage
- Risk: Regressions in endpoint routing, status codes, response content not caught until production.
- Priority: HIGH

**No Repository Testing:**
- What's not tested: Query methods, filtering, sorting, entity graph behavior
- Files: All files in `src/main/java/io/archton/scaffold/repository/` have zero test coverage
- Risk: N+1 queries, incorrect filtering results, eager loading bugs not caught until high load.
- Priority: HIGH

**No Validation Testing:**
- What's not tested: Email duplicate detection, email format validation, required field validation, date parsing
- Files: Validation logic scattered in `PersonResource`, `PersonRelationshipResource`, `AuthResource`
- Risk: Validation can be bypassed with crafted requests. Silent failures (e.g., null dateOfBirth).
- Priority: MEDIUM

**No Authorization Testing:**
- What's not tested: Role-based access control (@RolesAllowed annotations), path-level permissions
- Files: `src/main/resources/application.properties` defines permission rules; no automated tests
- Risk: Unauthorized users may gain access to admin endpoints if rules misconfigured.
- Priority: MEDIUM

**No Transaction Boundary Testing:**
- What's not tested: @Transactional behavior, rollback on exception, audit field updates
- Files: `src/main/java/io/archton/scaffold/service/` has no tests for transaction management
- Risk: Phantom data from failed transactions, audit trail corruption.
- Priority: MEDIUM

---

*Concerns audit: 2026-02-14*
