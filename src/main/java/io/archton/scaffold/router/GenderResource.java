package io.archton.scaffold.router;

import io.archton.scaffold.entity.Gender;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/genders")
@RolesAllowed("admin")
public class GenderResource {

    @Inject
    SecurityIdentity securityIdentity;

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
        
        // Success response fragments (close modal + OOB updates)
        public static native TemplateInstance gender$modal_success(String message, List<Gender> genders);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list(@HeaderParam("HX-Request") String hxRequest) {
        List<Gender> genders = Gender.listAllOrdered();

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
        Gender existingByCode = Gender.findByCode(gender.code);
        if (existingByCode != null) {
            return Templates.gender$modal_create(gender, "Code already exists.");
        }
        
        Gender existingByDescription = Gender.findByDescription(description);
        if (existingByDescription != null) {
            return Templates.gender$modal_create(gender, "Description already exists.");
        }
        
        // Set audit fields
        String userName = securityIdentity.isAnonymous() ? "system" : securityIdentity.getPrincipal().getName();
        gender.createdBy = userName;
        gender.updatedBy = userName;
        
        // Persist
        gender.persist();
        
        // Return success with OOB table refresh
        List<Gender> genders = Gender.listAllOrdered();
        return Templates.gender$modal_success("Gender created successfully.", genders);
    }
}
