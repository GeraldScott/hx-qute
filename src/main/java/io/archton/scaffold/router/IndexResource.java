package io.archton.scaffold.router;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

@Path("/")
public class IndexResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(String title, String description, String name);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("name") String name) {
        return Templates.index(
            "Welcome to HX-Qute", 
            "A Quarkus + HTMX prototype for building modern web applications",
            name != null ? name : "Developer"
        );
    }
}