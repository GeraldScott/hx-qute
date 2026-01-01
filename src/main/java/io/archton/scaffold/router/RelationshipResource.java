package io.archton.scaffold.router;

import io.archton.scaffold.entity.Relationship;
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

@Path("/relationships")
@RolesAllowed("admin")
public class RelationshipResource {

    @Inject
    SecurityIdentity securityIdentity;

    @CheckedTemplate
    public static class Templates {
        // Full page
        public static native TemplateInstance relationship(
            String title,
            String currentPage,
            String userName,
            List<Relationship> relationships
        );

        // Fragments (type-safe, compile-time validated)
        public static native TemplateInstance relationship$table(List<Relationship> relationships);

        // Modal content fragments
        public static native TemplateInstance relationship$modal_create(Relationship relationship, String error);
        public static native TemplateInstance relationship$modal_edit(Relationship relationship, String error);

        // Success response fragments (close modal + OOB updates)
        public static native TemplateInstance relationship$modal_success(String message, List<Relationship> relationships);
        public static native TemplateInstance relationship$modal_success_row(String message, Relationship relationship);

        // Delete modal fragments
        public static native TemplateInstance relationship$modal_delete(Relationship relationship, String error);
        public static native TemplateInstance relationship$modal_delete_success(Long deletedId);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
        List<Relationship> relationships = Relationship.listAllOrdered();

        // If HTMX request, return only the table fragment
        if ("true".equals(hxRequest)) {
            return Templates.relationship$table(relationships);
        }

        // Full page request
        String userName = securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();
        return Templates.relationship("Relationship Management", "relationship", userName, relationships);
    }


    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createForm() {
        // Return empty relationship object for form binding
        return Templates.relationship$modal_create(new Relationship(), null);
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        Relationship relationship = Relationship.findById(id);
        if (relationship == null) {
            // Return error in modal if relationship not found
            Relationship emptyRelationship = new Relationship();
            emptyRelationship.id = id;
            return Templates.relationship$modal_edit(emptyRelationship, "Relationship not found.");
        }
        return Templates.relationship$modal_edit(relationship, null);
    }


    @POST
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance create(
            @FormParam("code") String code,
            @FormParam("description") String description) {

        Relationship relationship = new Relationship();
        relationship.code = code;
        relationship.description = description;

        // Validation
        if (code == null || code.isBlank()) {
            return Templates.relationship$modal_create(relationship, "Code is required.");
        }

        if (code.length() > 10) {
            return Templates.relationship$modal_create(relationship, "Code must be at most 10 characters.");
        }

        if (description == null || description.isBlank()) {
            return Templates.relationship$modal_create(relationship, "Description is required.");
        }

        // Coerce code to uppercase
        relationship.code = code.toUpperCase();

        // Check uniqueness
        Relationship existingByCode = Relationship.findByCode(relationship.code);
        if (existingByCode != null) {
            return Templates.relationship$modal_create(relationship, "Code already exists.");
        }

        Relationship existingByDescription = Relationship.findByDescription(description);
        if (existingByDescription != null) {
            return Templates.relationship$modal_create(relationship, "Description already exists.");
        }

        // Set audit fields
        String userName = securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName();
        relationship.createdBy = userName;
        relationship.updatedBy = userName;

        // Persist
        relationship.persist();

        // Return success with OOB table refresh
        List<Relationship> relationships = Relationship.listAllOrdered();
        return Templates.relationship$modal_success("Relationship created successfully.", relationships);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance update(
            @PathParam("id") Long id,
            @FormParam("code") String code,
            @FormParam("description") String description) {

        // Create a transient object for form validation/re-display (don't modify managed entity yet)
        Relationship formData = new Relationship();
        formData.id = id;
        formData.code = code;
        formData.description = description;

        // Validation - use transient object for error display
        if (code == null || code.isBlank()) {
            return Templates.relationship$modal_edit(formData, "Code is required.");
        }

        if (code.length() > 10) {
            return Templates.relationship$modal_edit(formData, "Code must be at most 10 characters.");
        }

        if (description == null || description.isBlank()) {
            return Templates.relationship$modal_edit(formData, "Description is required.");
        }

        // Coerce code to uppercase for validation
        String upperCode = code.toUpperCase();
        formData.code = upperCode;

        // Check uniqueness (excluding current record) - before loading managed entity
        Relationship existingByCode = Relationship.findByCode(upperCode);
        if (existingByCode != null && !existingByCode.id.equals(id)) {
            return Templates.relationship$modal_edit(formData, "Code already exists.");
        }

        Relationship existingByDescription = Relationship.findByDescription(description);
        if (existingByDescription != null && !existingByDescription.id.equals(id)) {
            return Templates.relationship$modal_edit(formData, "Description already exists.");
        }

        // Now load the managed entity and update it (all validations passed)
        Relationship relationship = Relationship.findById(id);
        if (relationship == null) {
            return Templates.relationship$modal_edit(formData, "Relationship not found.");
        }

        // Update managed entity
        relationship.code = upperCode;
        relationship.description = description;

        // Set audit fields
        String userName = securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName();
        relationship.updatedBy = userName;

        // Persist changes (entity is already managed, changes will be flushed)

        // Return success with OOB single row update
        return Templates.relationship$modal_success_row("Relationship updated successfully.", relationship);
    }

    @GET
    @Path("/{id}/delete")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance deleteConfirm(@PathParam("id") Long id) {
        Relationship relationship = Relationship.findById(id);
        if (relationship == null) {
            Relationship emptyRelationship = new Relationship();
            emptyRelationship.id = id;
            return Templates.relationship$modal_delete(emptyRelationship, "Relationship not found.");
        }
        return Templates.relationship$modal_delete(relationship, null);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance delete(@PathParam("id") Long id) {
        Relationship relationship = Relationship.findById(id);
        if (relationship == null) {
            Relationship emptyRelationship = new Relationship();
            emptyRelationship.id = id;
            return Templates.relationship$modal_delete(emptyRelationship, "Relationship not found.");
        }

        // TODO: Check if relationship is in use by Person records
        // For now, just delete - Person entity doesn't have relationship field yet
        // Long personCount = Person.count("relationship", relationship);
        // if (personCount > 0) {
        //     return Templates.relationship$modal_delete(relationship,
        //         "Cannot delete: Relationship is in use by " + personCount + " person(s).");
        // }

        relationship.delete();

        // Return success with OOB row removal
        return Templates.relationship$modal_delete_success(id);
    }
}
