package io.archton.scaffold.router;

import io.archton.scaffold.entity.Gender;
import io.archton.scaffold.repository.GenderRepository;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/genders")
public class GenderResource {

    @Inject
    GenderRepository genderRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance gender(
            String title,
            String currentPage,
            String userName,
            List<Gender> genders
        );
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
        List<Gender> genders = genderRepository.listAll();
        String userName = securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();
        return Templates.gender("HX-Qute Gender", "gender", userName, genders);
    }

}
