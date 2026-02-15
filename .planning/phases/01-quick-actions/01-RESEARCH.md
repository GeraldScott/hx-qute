# Phase 1: Quick Actions - Research

**Researched:** 2026-02-14
**Domain:** Template modification, HTMX navigation, UIkit action buttons
**Confidence:** HIGH

## Summary

Phase 1 is a purely presentational change: adding two action links to each row in the existing person list table. The person list already exists at `/persons` with a table fragment (`{#fragment id='table'}`) that renders action buttons (Edit, Delete, Manage Relationships). The graph page already exists at `/graph` showing all relationships, and there is an existing person detail modal that loads via the `/graph/person/{id}` endpoint.

The requirements are:
- **ACT-01**: A link in the person list row to navigate to that person's connection network (`/graph` with person focus)
- **ACT-02**: A link in the person list row to view that person's detail page

Both requirements can be satisfied entirely within the existing template and resource layer. No new database queries, entities, services, or migrations are needed.

**Primary recommendation:** Add two icon buttons to the person table row's action column in `person.html` (both the `table` fragment and the `modal_success_row` fragment). For ACT-01, link to `/graph` (current show-all graph, since person-centered graph is Phase 3). For ACT-02, create a new read-only detail view endpoint on PersonResource and corresponding template fragment (or reuse the existing person modal pattern).

## Standard Stack

### Core

No new libraries or dependencies needed. This phase uses only what is already in the project.

| Library | Version | Purpose | Already Present |
|---------|---------|---------|-----------------|
| Quarkus REST | 3.30.3 | Serves HTML endpoints | Yes |
| Qute Templates | 3.30.3 | Server-side HTML rendering with fragments | Yes |
| HTMX | 2.0.8 | Partial page updates, modal loading | Yes |
| UIkit | 3.25.4 | CSS framework, icons, buttons, tooltips | Yes |

### Supporting

None needed. No new dependencies.

### Alternatives Considered

None. The existing stack fully supports this phase.

**Installation:** No installation needed.

## Architecture Patterns

### Recommended Project Structure

No new files needed except possibly a detail view template fragment. All changes are within existing files:

```
src/main/resources/templates/PersonResource/
    person.html          # MODIFY: Add action buttons to table fragment and modal_success_row fragment
src/main/java/.../router/
    PersonResource.java  # MODIFY: Add detail view endpoint (GET /persons/{id})
```

### Pattern 1: Action Buttons in Table Rows

**What:** Add icon-based navigation links to the existing button group in each person table row.
**When to use:** When adding quick navigation actions to list views.
**Example:**

```html
<!-- Current action buttons in person.html table fragment -->
<div class="uk-button-group">
    <!-- EXISTING: Manage relationships link -->
    <a class="uk-button uk-button-small uk-button-default"
       href="/persons/{p.id}/relationships"
       uk-tooltip="Manage Relationships">
        <span uk-icon="link"></span>
    </a>

    <!-- NEW (ACT-01): View network graph -->
    <a class="uk-button uk-button-small uk-button-default"
       href="/graph"
       uk-tooltip="View Network">
        <span uk-icon="git-fork"></span>
    </a>

    <!-- NEW (ACT-02): View person details -->
    <button class="uk-button uk-button-small uk-button-default"
            hx-get="/persons/{p.id}/detail"
            hx-target="#person-modal-body"
            hx-on::after-request="UIkit.modal('#person-modal').show()"
            uk-tooltip="View Details">
        <span uk-icon="info"></span>
    </button>

    <!-- EXISTING: Edit person -->
    <button class="uk-button uk-button-small uk-button-primary"
            hx-get="/persons/{p.id}/edit"
            hx-target="#person-modal-body"
            hx-on::after-request="UIkit.modal('#person-modal').show()"
            uk-tooltip="Edit Person">
        <span uk-icon="pencil"></span>
    </button>

    <!-- EXISTING: Delete person -->
    <button class="uk-button uk-button-small uk-button-danger"
            hx-get="/persons/{p.id}/delete"
            hx-target="#person-modal-body"
            hx-on::after-request="UIkit.modal('#person-modal').show()"
            uk-tooltip="Delete Person">
        <span uk-icon="trash"></span>
    </button>
</div>
```

Source: Existing codebase patterns in `person.html` lines 113-139.

### Pattern 2: Read-Only Detail View via Modal (ACT-02)

**What:** A read-only modal showing person details, loaded via HTMX into the existing modal shell.
**When to use:** When user needs to view entity details without editing.

The project already has this pattern in the graph page (`GraphResource.getPersonDetails()` renders `personModal.html`). The choice is between:

**Option A: Reuse the existing graph person modal endpoint** (`/graph/person/{id}`)
- Pro: No new code needed
- Con: Semantically belongs to GraphResource; couples PersonResource template to GraphResource endpoint

**Option B: Add a new detail endpoint on PersonResource** (`/persons/{id}/detail` or `/persons/{id}`)
- Pro: Proper separation of concerns; detail view owned by PersonResource
- Con: Small amount of new code (one endpoint + one fragment)

**Recommendation: Option B** -- Add `GET /persons/{id}` (or `/persons/{id}/detail`) to PersonResource with a new `person$modal_detail` fragment. This follows the existing modal pattern and keeps concerns properly separated. The fragment can display more information than the graph modal (e.g., include relationships count, notes, audit info).

### Pattern 3: Dual Update Locations for Row Actions

**What:** When adding action buttons to table rows, the buttons must be added in TWO places in `person.html`:
1. The `table` fragment (main table rendering)
2. The `modal_success_row` fragment (OOB row update after edit)

**When to use:** Always, when modifying person table row contents.
**Why critical:** If you only update the `table` fragment, the row will lose the new buttons after a successful edit operation (because `modal_success_row` re-renders the entire row via OOB swap).

Source: `person.html` lines 106-143 (table) and lines 302-333 (modal_success_row).

### Anti-Patterns to Avoid

- **Adding action buttons only to the table fragment**: The `modal_success_row` fragment also renders the full row and must have identical action buttons. Missing this causes buttons to disappear after edit.
- **Creating a separate detail page with full navigation**: The person list is the primary interface. Detail should be a modal overlay, consistent with the Edit and Delete patterns already in use.
- **Linking to person-centered network view**: Phase 3 builds person-centered networks. In Phase 1, the graph link should go to the existing `/graph` page. Do not try to build person-centered filtering now.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Modal display | Custom popup JS | UIkit modal (`uk-modal`) + HTMX `hx-on::after-request` | Already established pattern in codebase |
| Icon buttons | Custom SVG/CSS | UIkit `uk-icon` attribute | Consistent styling, available icon library |
| Tooltips | Custom tooltip component | UIkit `uk-tooltip` attribute | Already used on all existing action buttons |

**Key insight:** This phase requires zero custom solutions. Every interaction pattern already exists in the codebase.

## Common Pitfalls

### Pitfall 1: Forgetting the modal_success_row fragment

**What goes wrong:** New action buttons appear in the table initially but disappear after editing a person.
**Why it happens:** The `modal_success_row` fragment renders a complete replacement `<tr>` via OOB swap after a successful edit. If this fragment does not include the new buttons, they vanish.
**How to avoid:** Always modify BOTH the `table` fragment AND the `modal_success_row` fragment when changing row content.
**Warning signs:** Buttons work on page load but disappear after editing a person record.

### Pitfall 2: ACT-01 scope creep into person-centered graph

**What goes wrong:** Attempting to build person-centered graph filtering in Phase 1.
**Why it happens:** The requirement says "view that person's connection network" which could be interpreted as needing a filtered/person-centered view.
**How to avoid:** In Phase 1, ACT-01 links to the existing `/graph` page. Person-centered network discovery with configurable depth is Phase 3 (NET-01, NET-02, NET-03). The Phase 1 link provides navigation -- it does not need to implement person-centering yet.
**Warning signs:** Adding query parameters to `/graph` for person filtering, modifying `graph.js`, or adding new graph data endpoints.

### Pitfall 3: Inconsistent button ordering

**What goes wrong:** The new buttons are placed inconsistently between the two fragments, or placed after the Edit/Delete buttons where they break the visual flow.
**How to avoid:** Place navigation buttons (View Network, View Details) before mutation buttons (Edit, Delete). This follows the principle of information-before-action. The existing "Manage Relationships" link already follows this pattern.
**Warning signs:** Different button order in `table` fragment versus `modal_success_row` fragment.

### Pitfall 4: Missing @CheckedTemplate method declaration

**What goes wrong:** Compile error because new template fragment does not have a corresponding `@CheckedTemplate` method.
**Why it happens:** If adding a `modal_detail` fragment, a corresponding `person$modal_detail(Person person)` static native method must be declared in the `Templates` inner class.
**How to avoid:** Always add the `@CheckedTemplate` method when creating a new fragment.
**Warning signs:** Compile-time errors about missing template methods.

## Code Examples

Verified patterns from existing codebase:

### Adding a New @CheckedTemplate Fragment Method

```java
// In PersonResource.java - add to Templates inner class
@CheckedTemplate
public static class Templates {
    // ... existing methods ...

    // NEW: Detail view fragment
    public static native TemplateInstance person$modal_detail(
        Person person
    );
}
```

Source: Existing pattern in `PersonResource.java` lines 46-97.

### New Detail Endpoint on PersonResource

```java
// In PersonResource.java - add new GET endpoint
@GET
@Path("/{id}")
@Produces(MediaType.TEXT_HTML)
public TemplateInstance detail(@PathParam("id") Long id) {
    Person person = personRepository.findById(id);
    if (person == null) {
        return Templates.person$modal_detail(new Person());
    }
    return Templates.person$modal_detail(person);
}
```

Source: Pattern from `PersonResource.editForm()` at line 144.

### New Detail Fragment in person.html

```html
{#fragment id='modal_detail' rendered=false}
{@io.archton.scaffold.entity.Person person}
<h2 class="uk-modal-title">Person Details</h2>
<dl class="uk-description-list">
    <dt>Name</dt>
    <dd>{person.getDisplayName()}</dd>

    <dt>Email</dt>
    <dd>{person.email}</dd>

    {#if person.phone}
    <dt>Phone</dt>
    <dd>{person.phone}</dd>
    {/if}

    {#if person.dateOfBirth}
    <dt>Date of Birth</dt>
    <dd>{person.dateOfBirth.format('dd MMM yyyy')}</dd>
    {/if}

    {#if person.gender??}
    <dt>Gender</dt>
    <dd>{person.gender.description}</dd>
    {/if}

    {#if person.notes}
    <dt>Notes</dt>
    <dd>{person.notes}</dd>
    {/if}
</dl>
<details class="uk-margin">
    <summary class="uk-text-muted">Audit Information</summary>
    <div class="uk-text-small uk-text-muted uk-margin-small-top">
        <div>Created: {person.createdAt} by {person.createdBy}</div>
        <div>Updated: {person.updatedAt} by {person.updatedBy}</div>
    </div>
</details>
<div class="uk-margin uk-text-right">
    <button class="uk-button uk-button-default uk-modal-close" type="button">Close</button>
</div>
{/fragment}
```

Source: Pattern from `GraphResource/personModal.html` and `person.html` modal fragments.

### HTMX Modal Button Pattern

```html
<!-- Pattern for loading content into modal via HTMX -->
<button class="uk-button uk-button-small uk-button-default"
        hx-get="/persons/{p.id}/detail"
        hx-target="#person-modal-body"
        hx-on::after-request="UIkit.modal('#person-modal').show()"
        uk-tooltip="View Details">
    <span uk-icon="info"></span>
</button>
```

Source: Existing edit button pattern in `person.html` line 124-130.

### Standard Navigation Link Pattern

```html
<!-- Pattern for direct navigation (no HTMX, full page load) -->
<a class="uk-button uk-button-small uk-button-default"
   href="/graph"
   uk-tooltip="View Network">
    <span uk-icon="git-fork"></span>
</a>
```

Source: Existing "Manage Relationships" link in `person.html` line 115-121.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Separate detail pages | Modal-based detail views | Established pattern | Detail views load in modals via HTMX, avoiding full page navigation |
| Client-side routing | Server-rendered links | Established pattern | Simple `<a>` tags for navigation, HTMX for partial updates |

**Deprecated/outdated:** None. All patterns used in this phase are current and established in the codebase.

## Open Questions

1. **ACT-01: What should the graph link target?**
   - What we know: The existing `/graph` page shows ALL people and relationships. Phase 3 will build person-centered network views.
   - What's unclear: Should the Phase 1 link go to `/graph` (existing) or should it already filter/highlight the clicked person?
   - Recommendation: Link to `/graph` as-is. Person-centered view is Phase 3 scope. The value of ACT-01 in Phase 1 is quick navigation, not person-centered filtering.

2. **ACT-02: Detail as modal or full page?**
   - What we know: All existing view/edit/delete interactions in the person list use modals. The graph page also uses a modal for person details.
   - What's unclear: Whether the user expects a full-page detail view or a modal.
   - Recommendation: Use modal (consistent with existing patterns). A full-page detail view could be added later in Phase 4 (Evidence Capture) when there is more content to display.

3. **Endpoint path for detail view: `/persons/{id}` vs `/persons/{id}/detail`**
   - What we know: `GET /persons` is the list endpoint. `GET /persons/{id}/edit` and `GET /persons/{id}/delete` exist.
   - What's unclear: Whether `GET /persons/{id}` should be the detail view or a separate `/persons/{id}/detail` path.
   - Recommendation: Use `GET /persons/{id}` for the detail view. It follows REST conventions and does not conflict with existing endpoints. The list endpoint is `GET /persons` (no path param), so there is no collision.

## Sources

### Primary (HIGH confidence)

- Existing codebase: `PersonResource.java` -- established @CheckedTemplate, fragment, and modal patterns
- Existing codebase: `person.html` -- table fragment with action buttons, modal_success_row fragment
- Existing codebase: `GraphResource.java` + `personModal.html` -- existing person detail modal pattern
- Existing codebase: `PersonRelationshipResource.java` -- nested resource pattern with person context
- `docs/ARCHITECTURE.md` -- project architecture patterns, template system, HTMX integration
- UIkit 3.25 docs (https://getuikit.com/docs/icon) -- available icons for buttons

### Secondary (MEDIUM confidence)

- None needed. All findings are from direct codebase inspection.

### Tertiary (LOW confidence)

- None. No external sources needed for this phase.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- no new dependencies, using only existing project stack
- Architecture: HIGH -- following established patterns already in the codebase
- Pitfalls: HIGH -- identified from direct inspection of existing template duplication points

**Research date:** 2026-02-14
**Valid until:** 2026-04-14 (stable; no external dependencies that could change)
