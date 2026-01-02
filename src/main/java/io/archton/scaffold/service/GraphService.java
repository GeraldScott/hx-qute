package io.archton.scaffold.service;

import io.archton.scaffold.dto.GraphData;
import io.archton.scaffold.dto.GraphData.CyEdge;
import io.archton.scaffold.dto.GraphData.CyNode;
import io.archton.scaffold.dto.GraphEdge;
import io.archton.scaffold.dto.GraphNode;
import io.archton.scaffold.entity.Person;
import io.archton.scaffold.entity.PersonRelationship;
import io.archton.scaffold.repository.PersonRelationshipRepository;
import io.archton.scaffold.repository.PersonRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GraphService {

    @Inject
    PersonRepository personRepository;

    @Inject
    PersonRelationshipRepository personRelationshipRepository;

    /**
     * Build Cytoscape.js compatible graph data.
     */
    public GraphData buildGraphData() {
        // Get relationship counts per person for node sizing
        Map<Long, Integer> relationshipCounts = personRepository.countRelationshipsByPerson();

        // Build nodes from all persons
        List<Person> persons = personRepository.listAll();
        List<CyNode> nodes = persons.stream()
            .map(p -> GraphNode.from(p, relationshipCounts.getOrDefault(p.id, 0)))
            .map(CyNode::from)
            .toList();

        // Build edges from all relationships
        List<PersonRelationship> relationships = personRelationshipRepository.findAllForGraph();
        List<CyEdge> edges = relationships.stream()
            .map(GraphEdge::from)
            .map(CyEdge::from)
            .toList();

        // Get relationship types for filter dropdown
        List<String> relationshipTypes = personRelationshipRepository.findDistinctRelationshipTypes();

        return new GraphData(nodes, edges, relationshipTypes);
    }
}
