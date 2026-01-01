package io.archton.scaffold.router;

import io.archton.scaffold.entity.Gender;
import io.archton.scaffold.repository.GenderRepository;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/genders")
@RolesAllowed("admin")
public class GenderResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    GenderRepository genderRepository;

    @CheckedTemplate
    public static class Templates {
        // Full page
        public static native TemplateInstance gender(
            String title,
            String currentPage,
            String userName,
            List<Gender> genders
        );

        // Fragments (type-safe, compile-time validated)
        public static native TemplateInstance gender$table(List<Gender> genders);
        
        // Modal content fragments
        public static native TemplateInstance gender$modal_create(Gender gender, String error);
        public static native TemplateInstance gender$modal_edit(Gender gender, String error);

        // Success response fragments (close modal + OOB updates)
        public static native TemplateInstance gender$modal_success(String message, List<Gender> genders);
        public static native TemplateInstance gender$modal_success_row(String message, Gender gender);

        // Delete modal fragments
        public static native TemplateInstance gender$modal_delete(Gender gender, String error);
        public static native TemplateInstance gender$modal_delete_success(Long deletedId);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
        List<Gender> genders = genderRepository.listAllOrdered();

        // If HTMX request, return only the table fragment
        if ("true".equals(hxRequest)) {
            return Templates.gender$table(genders);
        }

        // Full page request
        String userName = securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();
        return Templates.gender("Gender Management", "gender", userName, genders);
    }


    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        // Return empty gender object for form binding
        return Templates.gender$modal_create(new Gender(), null);
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        Gender gender = genderRepository.findById(id);
        if (gender == null) {
            // Return error in modal if gender not found
            Gender emptyGender = new Gender();
            emptyGender.id = id;
            return Templates.gender$modal_edit(emptyGender, "Gender not found.");
        }
        return Templates.gender$modal_edit(gender, null);
    }


    @POST
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance create(
            @FormParam("code") String code,
            @FormParam("description") String description) {
        
        Gender gender = new Gender();
        gender.code = code;
        gender.description = description;
        
        // Validation
        if (code == null || code.isBlank()) {
            return Templates.gender$modal_create(gender, "Code is required.");
        }
        
        if (code.length() > 1) {
            return Templates.gender$modal_create(gender, "Code must be 1 character.");
        }
        
        if (description == null || description.isBlank()) {
            return Templates.gender$modal_create(gender, "Description is required.");
        }
        
        // Coerce code to uppercase
        gender.code = code.toUpperCase();
        
        // Check uniqueness
        if (genderRepository.findByCode(gender.code).isPresent()) {
            return Templates.gender$modal_create(gender, "Code already exists.");
        }

        if (genderRepository.findByDescription(description).isPresent()) {
            return Templates.gender$modal_create(gender, "Description already exists.");
        }
        
        // Set audit fields
        String userName = securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName();
        gender.createdBy = userName;
        gender.updatedBy = userName;
        
        // Persist
        genderRepository.persist(gender);
        
        // Return success with OOB table refresh
        List<Gender> genders = genderRepository.listAllOrdered();
        return Templates.gender$modal_success("Gender created successfully.", genders);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance update(
            @PathParam("id") Long id,
            @FormParam("code") String code,
            @FormParam("description") String description) {

        Gender gender = genderRepository.findById(id);
        if (gender == null) {
            Gender emptyGender = new Gender();
            emptyGender.id = id;
            return Templates.gender$modal_edit(emptyGender, "Gender not found.");
        }

        // Store original values for validation
        String originalCode = gender.code;
        String originalDescription = gender.description;

        // Update gender object with form values for re-display
        gender.code = code;
        gender.description = description;

        // Validation
        if (code == null || code.isBlank()) {
            return Templates.gender$modal_edit(gender, "Code is required.");
        }

        if (code.length() > 1) {
            return Templates.gender$modal_edit(gender, "Code must be 1 character.");
        }

        if (description == null || description.isBlank()) {
            return Templates.gender$modal_edit(gender, "Description is required.");
        }

        // Coerce code to uppercase
        gender.code = code.toUpperCase();

        // Check uniqueness (excluding current record)
        var existingByCode = genderRepository.findByCode(gender.code);
        if (existingByCode.isPresent() && !existingByCode.get().id.equals(id)) {
            return Templates.gender$modal_edit(gender, "Code already exists.");
        }

        var existingByDescription = genderRepository.findByDescription(description);
        if (existingByDescription.isPresent() && !existingByDescription.get().id.equals(id)) {
            return Templates.gender$modal_edit(gender, "Description already exists.");
        }

        // Set audit fields
        String userName = securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName();
        gender.updatedBy = userName;

        // Persist changes (entity is already managed, changes will be flushed)

        // Return success with OOB single row update
        return Templates.gender$modal_success_row("Gender updated successfully.", gender);
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance deleteConfirm(@PathParam("id") Long id) {
        Gender gender = genderRepository.findById(id);
        if (gender == null) {
            Gender emptyGender = new Gender();
            emptyGender.id = id;
            return Templates.gender$modal_delete(emptyGender, "Gender not found.");
        }
        return Templates.gender$modal_delete(gender, null);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance delete(@PathParam("id") Long id) {
        Gender gender = genderRepository.findById(id);
        if (gender == null) {
            Gender emptyGender = new Gender();
            emptyGender.id = id;
            return Templates.gender$modal_delete(emptyGender, "Gender not found.");
        }

        // TODO: Check if gender is in use by Person records
        // For now, just delete - Person entity doesn't exist yet
        // Long personCount = Person.count("gender", gender);
        // if (personCount > 0) {
        //     return Templates.gender$modal_delete(gender,
        //         "Cannot delete: Gender is in use by " + personCount + " person(s).");
        // }

        genderRepository.delete(gender);

        // Return success with OOB row removal
        return Templates.gender$modal_delete_success(id);
    }
}
