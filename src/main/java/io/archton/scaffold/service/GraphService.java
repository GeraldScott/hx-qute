package io.archton.scaffold.service;

import io.archton.scaffold.dto.GraphData;
import io.archton.scaffold.dto.GraphLink;
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
     * Build graph data for visualization.
     * Returns a DTO that JSON-B will automatically serialize.
     */
    public GraphData buildGraphData() {
        // Get relationship counts per person
        Map<Long, Integer> relationshipCounts = personRepository.countRelationshipsByPerson();

        // Build nodes from all persons
        List<Person> persons = personRepository.listAll();
        List<GraphNode> nodes = persons.stream()
            .map(p -> GraphNode.from(p, relationshipCounts.getOrDefault(p.id, 0)))
            .toList();

        // Build links from all relationships
        List<PersonRelationship> relationships = personRelationshipRepository.findAllForGraph();
        List<GraphLink> links = relationships.stream()
            .map(GraphLink::from)
            .toList();

        return new GraphData(nodes, links);
    }
}
