package io.archton.scaffold.router;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class IndexResource {

    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance index(
            String title,
            String currentPage
        );
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
        return Templates.index("HX-Qute Home", "home");
    }
}
