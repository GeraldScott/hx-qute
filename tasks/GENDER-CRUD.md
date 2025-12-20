# Quarkus + htmx + CRUD Example

A complete example demonstrating CRUD operations using:
- **Quarkus** - Java framework
- **Qute** - Templating engine
- **htmx** - HTML-over-the-wire


## API Endpoints

| Method | Path | Description | Returns |
|--------|------|-------------|---------|
| GET | /genders | Main page | Full HTML page |
| GET | /genders/table | Table partial | Table HTML |
| GET | /genders/new | Add form | Form HTML |
| GET | /genders/{code}/edit | Edit form | Form HTML |
| GET | /genders/{code}/delete | Delete confirmation | Form HTML |
| POST | /genders | Create gender | Form HTML or 204 |
| PUT | /genders/{code} | Update gender | Form HTML or 204 |
| DELETE | /genders/{code} | Delete gender | 204 |
