package io.archton.scaffold.router;

import io.archton.scaffold.dto.GraphData;
import io.archton.scaffold.service.GraphService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/graph")
@RolesAllowed({"user", "admin"})
public class GraphResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    GraphService graphService;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance graph(
            String title,
            String currentPage,
            String userName
        );
    }

    /**
     * Display the network graph page.
     * Graph data is loaded asynchronously via /graph/data endpoint.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showGraph() {
        String userName = securityIdentity.isAnonymous()
            ? null
            : securityIdentity.getPrincipal().getName();

        return Templates.graph(
            "Relationship Graph",
            "graph",
            userName
        );
    }

    /**
     * Return graph data as JSON.
     * JSON-B automatically serializes the GraphData record.
     */
    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public GraphData getGraphData() {
        return graphService.buildGraphData();
    }
}
