package io.archton.scaffold.dto;

import io.archton.scaffold.entity.PersonRelationship;

/**
 * DTO for Cytoscape.js edge data.
 * Wrapped in { data: {...} } structure by GraphData.CyEdge.
 */
public record GraphEdge(
    String id,
    String source,
    String target,
    String label
) {
    public static GraphEdge from(PersonRelationship pr) {
        return new GraphEdge(
            "e" + pr.sourcePerson.id + "-" + pr.relatedPerson.id + "-" + pr.relationship.id,
            String.valueOf(pr.sourcePerson.id),
            String.valueOf(pr.relatedPerson.id),
            pr.relationship.description
        );
    }
}
