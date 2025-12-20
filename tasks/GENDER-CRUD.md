# Quarkus + htmx + Native Dialog CRUD Example

A complete example demonstrating CRUD operations using:
- **Quarkus** - Java framework
- **Qute** - Templating engine
- **htmx** - HTML-over-the-wire
- **Native `<dialog>`** - HTML5 dialog element for modals

## Running the Application

```bash
# Development mode with live reload
./mvnw quarkus:dev

# Or package and run
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Then open http://localhost:8080/genders

## Project Structure

```
src/main/java/com/example/
├── model/
│   └── Gender.java              # Entity class
├── repository/
│   └── GenderRepository.java    # In-memory data store
└── resource/
    └── GenderResource.java      # REST endpoints

src/main/resources/templates/
├── base.html                    # Base layout with CSS/JS
├── genders.html                 # Main page
├── genderTable.html             # Table partial (for htmx refresh)
├── genderForm.html              # Add/Edit form modal
├── genderView.html              # View details modal
└── genderDelete.html            # Delete confirmation modal
```

## Key Patterns Used

### 1. Server-Rendered Modals with Native `<dialog>`

Modals are fetched from the server and appended to a container:

```html
<button hx-get="/genders/new"
        hx-target="#dialog-container"
        hx-swap="innerHTML">
    Add Gender
</button>

<div id="dialog-container"></div>
```

The dialog template includes `data-onload-showmodal` attribute:

```html
<dialog data-onload-showmodal>
    <!-- content -->
</dialog>
```

JavaScript automatically shows the dialog after htmx swap:

```javascript
document.body.addEventListener('htmx:afterSettle', function(event) {
    const dialog = event.detail.target.querySelector('dialog[data-onload-showmodal]');
    if (dialog) {
        dialog.showModal();
    }
});
```

### 2. Form Validation with Server Round-Trips

The form targets itself so validation errors replace the form content:

```html
<form hx-post="/genders"
      hx-target="this"
      hx-swap="outerHTML">
```

**Server-side logic:**
- If validation fails → Return the form HTML with errors (stays in modal)
- If validation succeeds → Return HTTP 204 with `HX-Trigger` header

```java
// Validation failed - return form with errors
return Response.ok(genderForm.data("errors", errors)).build();

// Success - return empty response with trigger
return Response.noContent()
    .header("HX-Trigger", "genderCreated")
    .build();
```

### 3. Closing Modal on Success

JavaScript detects empty responses targeting dialogs and closes them:

```javascript
document.body.addEventListener('htmx:beforeSwap', function(event) {
    const targetDialog = event.detail.target.closest('dialog');
    if (targetDialog && !event.detail.xhr.response) {
        targetDialog.close();
        event.detail.shouldSwap = false;
    }
});
```

### 4. Refreshing the Table After CRUD Operations

The table container listens for custom events:

```html
<div id="gender-table-container"
     hx-get="/genders/table"
     hx-trigger="genderCreated from:body, genderUpdated from:body, genderDeleted from:body"
     hx-swap="innerHTML">
```

When the server returns `HX-Trigger: genderCreated`, htmx fires a custom event that triggers the table refresh.

### 5. DOM Cleanup

Dialogs are removed from the DOM when closed:

```javascript
document.body.addEventListener('close', function(event) {
    if (event.target.tagName === 'DIALOG') {
        setTimeout(() => event.target.remove(), 10);
    }
}, true);
```

## Benefits of Native `<dialog>`

1. **Focus trapping** - Built-in, no JavaScript needed
2. **Escape key** - Closes modal automatically
3. **Backdrop** - Styled via `::backdrop` pseudo-element
4. **Accessibility** - Proper ARIA roles built-in
5. **Inert background** - Prevents interaction with content behind modal

## API Endpoints

| Method | Path | Description | Returns |
|--------|------|-------------|---------|
| GET | /genders | Main page | Full HTML page |
| GET | /genders/table | Table partial | Table HTML |
| GET | /genders/new | Add form modal | Dialog HTML |
| GET | /genders/{code} | View modal | Dialog HTML |
| GET | /genders/{code}/edit | Edit form modal | Dialog HTML |
| GET | /genders/{code}/delete | Delete confirmation | Dialog HTML |
| POST | /genders | Create gender | Form HTML or 204 |
| PUT | /genders/{code} | Update gender | Form HTML or 204 |
| DELETE | /genders/{code} | Delete gender | 204 |

## Customization

### Styling the Dialog Backdrop

```css
dialog::backdrop {
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(2px);
}
```

### Adding Animations

```css
dialog[open] {
    animation: fade-in 0.2s ease-out;
}

@keyframes fade-in {
    from { opacity: 0; transform: scale(0.95); }
    to { opacity: 1; transform: scale(1); }
}
```
