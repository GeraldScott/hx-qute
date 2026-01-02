package io.archton.scaffold.router;

import io.archton.scaffold.entity.Person;
import io.archton.scaffold.entity.PersonRelationship;
import io.archton.scaffold.entity.Relationship;
import io.archton.scaffold.repository.PersonRepository;
import io.archton.scaffold.repository.PersonRelationshipRepository;
import io.archton.scaffold.repository.RelationshipRepository;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/graph")
@RolesAllowed({"user", "admin"})
public class GraphResource {

    @Inject
    PersonRepository personRepository;

    @Inject
    PersonRelationshipRepository personRelationshipRepository;

    @Inject
    RelationshipRepository relationshipRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance graph(
            String currentPage, String userName, List<Relationship> relationships);
        public static native TemplateInstance personModal(Person person);
    }

    // Inner DTO classes
    public static class GraphData {
        public List<GraphNode> nodes;
        public List<GraphLink> links;
    }

    public static class GraphNode {
        public Long id;
        public String firstName;
        public String lastName;
        public String email;
        public String genderCode;
        public int relationshipCount;
    }

    public static class GraphLink {
        public Long source;
        public Long target;
        public String relationshipType;
        public String relationshipCode;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showGraph() {
        List<Relationship> relationships = relationshipRepository.listAll(
            Sort.by("description"));
        return Templates.graph("graph", getCurrentUserName(), relationships);
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public GraphData getGraphData() {
        return buildGraphData();
    }

    @GET
    @Path("/person/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getPersonDetails(@PathParam("id") Long id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Templates.personModal(person);
    }

    private String getCurrentUserName() {
        return securityIdentity.isAnonymous() ? null : securityIdentity.getPrincipal().getName();
    }

    private GraphData buildGraphData() {
        GraphData data = new GraphData();
        data.nodes = new ArrayList<>();
        data.links = new ArrayList<>();

        // Build node map for relationship counting
        Map<Long, GraphNode> nodeMap = new HashMap<>();
        Map<Long, Integer> relationshipCounts = new HashMap<>();

        // Count relationships per person
        List<PersonRelationship> allRelationships = personRelationshipRepository.listAll();
        for (PersonRelationship pr : allRelationships) {
            relationshipCounts.merge(pr.sourcePerson.id, 1, Integer::sum);
            relationshipCounts.merge(pr.relatedPerson.id, 1, Integer::sum);
        }

        // Build nodes
        List<Person> persons = personRepository.listAll();
        for (Person p : persons) {
            GraphNode node = new GraphNode();
            node.id = p.id;
            node.firstName = p.firstName;
            node.lastName = p.lastName;
            node.email = p.email;
            node.genderCode = p.gender != null ? p.gender.code : null;
            node.relationshipCount = relationshipCounts.getOrDefault(p.id, 0);
            data.nodes.add(node);
            nodeMap.put(p.id, node);
        }

        // Build links (avoiding duplicates for bidirectional relationships)
        Set<String> processedLinks = new HashSet<>();
        for (PersonRelationship pr : allRelationships) {
            // Create normalized key to avoid duplicate edges
            Long minId = Math.min(pr.sourcePerson.id, pr.relatedPerson.id);
            Long maxId = Math.max(pr.sourcePerson.id, pr.relatedPerson.id);
            String key = minId + "-" + maxId + "-" + pr.relationship.id;

            if (!processedLinks.contains(key)) {
                GraphLink link = new GraphLink();
                link.source = pr.sourcePerson.id;
                link.target = pr.relatedPerson.id;
                link.relationshipType = pr.relationship.description;
                link.relationshipCode = pr.relationship.code;
                data.links.add(link);
                processedLinks.add(key);
            }
        }

        return data;
    }
}
