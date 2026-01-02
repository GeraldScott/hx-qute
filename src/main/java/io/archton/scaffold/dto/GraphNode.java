package io.archton.scaffold.dto;

import io.archton.scaffold.entity.Person;

/**
 * DTO for Cytoscape.js node data.
 * Wrapped in { data: {...} } structure by GraphData.CyNode.
 */
public record GraphNode(
    String id,
    String name,
    String email,
    String phone,
    String dateOfBirth,
    String gender,
    String genderCode,  // M, F, X - for coloring
    String notes,
    int weight          // Relationship count - for sizing
) {
    public static GraphNode from(Person p, int relationshipCount) {
        return new GraphNode(
            String.valueOf(p.id),
            p.getDisplayName(),
            p.email,
            p.phone,
            p.dateOfBirth != null ? p.dateOfBirth.toString() : null,
            p.gender != null ? p.gender.description : null,
            p.gender != null ? p.gender.code : "X",
            p.notes,
            Math.max(1, relationshipCount)
        );
    }
}
