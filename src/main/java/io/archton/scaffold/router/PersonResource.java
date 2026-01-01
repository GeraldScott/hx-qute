package io.archton.scaffold.router;

import io.archton.scaffold.entity.Gender;
import io.archton.scaffold.entity.Person;
import io.archton.scaffold.entity.Title;
import io.archton.scaffold.repository.GenderRepository;
import io.archton.scaffold.repository.PersonRepository;
import io.archton.scaffold.repository.TitleRepository;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/persons")
@RolesAllowed({"user", "admin"})
public class PersonResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    PersonRepository personRepository;

    @Inject
    TitleRepository titleRepository;

    @Inject
    GenderRepository genderRepository;

    @CheckedTemplate
    public static class Templates {
        // Full page
        public static native TemplateInstance person(
            String title,
            String currentPage,
            String userName,
            List<Person> persons,
            List<Title> titleChoices,
            List<Gender> genderChoices,
            String filterText,
            String sortField,
            String sortDir
        );

        // Fragments (type-safe, compile-time validated)
        public static native TemplateInstance person$table(
            List<Person> persons,
            String filterText
        );

        // Modal content fragments (for future use cases)
        public static native TemplateInstance person$modal_create(
            Person person,
            List<Title> titleChoices,
            List<Gender> genderChoices,
            String error
        );
        public static native TemplateInstance person$modal_edit(
            Person person,
            List<Title> titleChoices,
            List<Gender> genderChoices,
            String error
        );
        public static native TemplateInstance person$modal_delete(
            Person person,
            String error
        );

        // Success response fragments (for future use cases)
        public static native TemplateInstance person$modal_success(
            String message,
            List<Person> persons,
            String filterText
        );
        public static native TemplateInstance person$modal_success_row(
            String message,
            Person person
        );
        public static native TemplateInstance person$modal_delete_success(
            Long deletedId
        );
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(
            @HeaderParam("HX-Request") String hxRequest,
            @QueryParam("filter") String filter,
            @QueryParam("sortField") String sortField,
            @QueryParam("sortDir") String sortDir) {

        List<Person> persons = personRepository.findByFilter(filter, sortField, sortDir);

        // If HTMX request, return only the table fragment
        if ("true".equals(hxRequest)) {
            return Templates.person$table(persons, filter);
        }

        // Full page request
        String userName = securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();
        List<Title> titleChoices = titleRepository.listAllOrdered();
        List<Gender> genderChoices = genderRepository.listAllOrdered();

        return Templates.person(
            "Person Management",
            "persons",
            userName,
            persons,
            titleChoices,
            genderChoices,
            filter,
            sortField,
            sortDir
        );
    }


    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        List<Title> titleChoices = titleRepository.listAllOrdered();
        List<Gender> genderChoices = genderRepository.listAllOrdered();
        return Templates.person$modal_create(new Person(), titleChoices, genderChoices, null);
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            return Templates.person$modal_edit(new Person(), List.of(), List.of(), "Person not found.");
        }
        List<Title> titleChoices = titleRepository.listAllOrdered();
        List<Gender> genderChoices = genderRepository.listAllOrdered();
        return Templates.person$modal_edit(person, titleChoices, genderChoices, null);
    }


    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    @POST
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance create(
            @FormParam("firstName") String firstName,
            @FormParam("lastName") String lastName,
            @FormParam("titleId") Long titleId,
            @FormParam("email") String email,
            @FormParam("phone") String phone,
            @FormParam("dateOfBirth") String dateOfBirth,
            @FormParam("genderId") Long genderId) {

        Person person = new Person();
        person.firstName = firstName;
        person.lastName = lastName;
        person.email = email;
        person.phone = phone;

        // Parse dateOfBirth if provided
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            try {
                person.dateOfBirth = java.time.LocalDate.parse(dateOfBirth);
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }

        // Link title if provided
        if (titleId != null) {
            person.title = titleRepository.findById(titleId);
        }

        // Link gender if provided
        if (genderId != null) {
            person.gender = genderRepository.findById(genderId);
        }

        List<Title> titleChoices = titleRepository.listAllOrdered();
        List<Gender> genderChoices = genderRepository.listAllOrdered();

        // Validation
        if (firstName == null || firstName.isBlank()) {
            return Templates.person$modal_create(person, titleChoices, genderChoices, "First name is required.");
        }

        if (lastName == null || lastName.isBlank()) {
            return Templates.person$modal_create(person, titleChoices, genderChoices, "Last name is required.");
        }

        if (email == null || email.isBlank()) {
            return Templates.person$modal_create(person, titleChoices, genderChoices, "Email is required.");
        }

        if (!email.matches(EMAIL_REGEX)) {
            return Templates.person$modal_create(person, titleChoices, genderChoices, "Invalid email format.");
        }

        // Check email uniqueness (case-insensitive)
        Person existingByEmail = personRepository.findByEmail(email);
        if (existingByEmail != null) {
            return Templates.person$modal_create(person, titleChoices, genderChoices, "Email already registered.");
        }

        // Set audit fields
        String userName = securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName();
        person.createdBy = userName;
        person.updatedBy = userName;

        // Persist
        personRepository.persist(person);

        // Return success with OOB table refresh
        List<Person> persons = personRepository.findByFilter(null, null, null);
        return Templates.person$modal_success("Person created successfully.", persons, null);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance update(
            @PathParam("id") Long id,
            @FormParam("firstName") String firstName,
            @FormParam("lastName") String lastName,
            @FormParam("titleId") Long titleId,
            @FormParam("email") String email,
            @FormParam("phone") String phone,
            @FormParam("dateOfBirth") String dateOfBirth,
            @FormParam("genderId") Long genderId) {

        Person person = personRepository.findById(id);
        if (person == null) {
            return Templates.person$modal_edit(new Person(), List.of(), List.of(), "Person not found.");
        }

        List<Title> titleChoices = titleRepository.listAllOrdered();
        List<Gender> genderChoices = genderRepository.listAllOrdered();

        // Create a detached copy for form display on validation errors
        Person formPerson = new Person();
        formPerson.id = id;
        formPerson.firstName = firstName;
        formPerson.lastName = lastName;
        formPerson.email = email;
        formPerson.phone = phone;
        formPerson.createdAt = person.createdAt;
        formPerson.createdBy = person.createdBy;
        formPerson.updatedAt = person.updatedAt;
        formPerson.updatedBy = person.updatedBy;

        // Parse dateOfBirth for form display
        if (dateOfBirth != null && !dateOfBirth.isBlank()) {
            try {
                formPerson.dateOfBirth = java.time.LocalDate.parse(dateOfBirth);
            } catch (Exception e) {
                // Invalid date format
            }
        }

        // Link title for form display
        if (titleId != null) {
            formPerson.title = titleRepository.findById(titleId);
        }

        // Link gender for form display
        if (genderId != null) {
            formPerson.gender = genderRepository.findById(genderId);
        }

        // Validation BEFORE modifying the managed entity
        if (firstName == null || firstName.isBlank()) {
            return Templates.person$modal_edit(formPerson, titleChoices, genderChoices, "First name is required.");
        }

        if (lastName == null || lastName.isBlank()) {
            return Templates.person$modal_edit(formPerson, titleChoices, genderChoices, "Last name is required.");
        }

        if (email == null || email.isBlank()) {
            return Templates.person$modal_edit(formPerson, titleChoices, genderChoices, "Email is required.");
        }

        if (!email.matches(EMAIL_REGEX)) {
            return Templates.person$modal_edit(formPerson, titleChoices, genderChoices, "Invalid email format.");
        }

        // Check email uniqueness (case-insensitive, excluding current record)
        Person existingByEmail = personRepository.findByEmail(email);
        if (existingByEmail != null && !existingByEmail.id.equals(id)) {
            return Templates.person$modal_edit(formPerson, titleChoices, genderChoices, "Email already registered.");
        }

        // All validations passed - now update the managed entity
        person.firstName = firstName;
        person.lastName = lastName;
        person.email = email;
        person.phone = phone;
        person.dateOfBirth = formPerson.dateOfBirth;
        person.title = formPerson.title;
        person.gender = formPerson.gender;

        // Set audit field
        String userName = securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName();
        person.updatedBy = userName;
        // Note: updatedAt is set automatically by @PreUpdate callback

        // Return success with OOB row update
        return Templates.person$modal_success_row("Person updated successfully.", person);
    }


    /**
     * Display delete confirmation modal.
     */
    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance deleteForm(@PathParam("id") Long id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            return Templates.person$modal_delete(new Person(), "Person not found.");
        }
        return Templates.person$modal_delete(person, null);
    }

    /**
     * Delete person.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance delete(@PathParam("id") Long id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            return Templates.person$modal_delete(new Person(), "Person not found.");
        }

        personRepository.deleteById(id);
        return Templates.person$modal_delete_success(id);
    }
}
