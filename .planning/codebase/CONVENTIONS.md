# Conventions

## Code Style

### Entity Pattern
- Public fields (Panache convention) - no getters/setters
- `@PrePersist` / `@PreUpdate` lifecycle callbacks for audit timestamps and email normalization
- Named constraints: `@UniqueConstraint(name = "uk_person_email", ...)`, `@ForeignKey(name = "fk_person_gender")`
- Display helper methods on entities: `getDisplayName()`

```java
@Entity
@Table(name = "person",
    uniqueConstraints = { @UniqueConstraint(name = "uk_person_email", columnNames = "email") })
public class Person {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String firstName;
    // ...
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
```

### Repository Pattern
- Implements `PanacheRepository<Entity>` (repository pattern, not active record)
- `@ApplicationScoped`
- Common methods: `listAllOrdered()`, `existsByCode()`, `existsByCodeAndIdNot()`
- Custom queries use Panache shorthand HQL

### Resource Pattern
- Inner `@CheckedTemplate` class `Templates` with `static native` method declarations
- HTMX detection: `@HeaderParam("HX-Request") String hxRequest` with `"true".equals(hxRequest)` check
- Full page vs fragment: returns full template for browser, fragment for HTMX
- `@FormParam` for form data (not `@BeanParam` or request body objects)
- Manual validation in resource methods (not Bean Validation on form params)
- `SecurityIdentity` injected for current user name and audit fields

### Template Pattern
- Qute type-safe templates with `{@Type varName}` parameter declarations
- `{#include base}...{/include}` for layout inheritance
- `{#fragment id='name' rendered=false}` for HTMX-swappable fragments
- Null-safe access: `{value ?: ''}`, `{#if value??}`
- Date formatting: `{date.format('dd MMM yyyy')}`

## HTMX Conventions

### Request/Response Pattern
```html
<!-- Trigger: button with hx-get targeting modal body -->
<button hx-get="/persons/{id}/edit"
        hx-target="#person-modal-body"
        hx-on::after-request="UIkit.modal('#person-modal').show()">

<!-- Form submit: hx-post/hx-put targeting same modal body -->
<form hx-post="/persons" hx-target="#person-modal-body">
```

### Modal Pattern
1. Static modal shell in full page template with `uk-modal`
2. Content loaded dynamically via HTMX into `#*-modal-body`
3. Modal shown via `hx-on::after-request="UIkit.modal('#*-modal').show()"`
4. Modal closed via `hx-on::load="UIkit.modal('#*-modal').hide()"` in success fragment

### OOB Update Pattern
- Create success: full table refresh via `hx-swap-oob="innerHTML"` on container
- Update success: single row via `hx-swap-oob="outerHTML"` on `#*-row-{id}`
- Delete success: row removal via `hx-swap-oob="delete"` on `#*-row-{id}`
- OOB content wrapped in `<template>` tags to prevent premature rendering

### Search/Filter Pattern
- `hx-trigger="input changed delay:300ms, search"` for live search
- `hx-include="closest form"` to send all form inputs
- `hx-push-url="true"` for URL state management

## Validation

- Manual validation in resource methods, not Bean Validation annotations on params
- Validation errors return the same modal fragment with error message
- Email uniqueness checked at repository level with case-insensitive comparison
- Entity-level `@NotBlank`, `@Email`, `@Size` on `UserLogin` only (security JPA requirement)

## Error Handling

- Custom domain exceptions in `service/exception/`: `EntityNotFoundException`, `UniqueConstraintException`, `ReferentialIntegrityException`
- `GlobalExceptionMapper` maps exceptions to HTTP status codes with HTML/JSON responses
- UUID reference IDs in error responses for troubleshooting
- Stack traces only in dev mode

## Audit Fields

All main entities have:
- `createdAt` / `updatedAt` (Instant) - set via `@PrePersist` / `@PreUpdate`
- `createdBy` / `updatedBy` (String) - set from `SecurityIdentity` in resource methods
