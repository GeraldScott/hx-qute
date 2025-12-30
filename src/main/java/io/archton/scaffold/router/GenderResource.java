package io.archton.scaffold.router;

import io.archton.scaffold.entity.Gender;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
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
}
