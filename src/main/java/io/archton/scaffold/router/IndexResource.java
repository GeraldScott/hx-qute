package io.archton.scaffold.router;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class IndexResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    LaunchMode launchMode;

    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance index(
            String title,
            String currentPage,
            String userName,
            boolean devMode
        );
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
        String userName = securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();
        boolean devMode = launchMode == LaunchMode.DEVELOPMENT;
        return Templates.index("HX-Qute Home", "home", userName, devMode);
    }
}
