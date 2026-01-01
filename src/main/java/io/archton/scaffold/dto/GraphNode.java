package io.archton.scaffold.dto;

import io.archton.scaffold.entity.Person;

public record GraphNode(
    String id,
    String name,
    int val,      // Node size (based on relationship count)
    String group, // Gender code for coloring (M, F, X)
    String email,
    String phone,
    String dateOfBirth,
    String gender,
    String notes
) {
    public static GraphNode from(Person p, int relationshipCount) {
        return new GraphNode(
            String.valueOf(p.id),
            p.getDisplayName(),
            Math.max(1, relationshipCount), // Minimum size 1
            p.gender != null ? p.gender.code : "X",
            p.email,
            p.phone,
            p.dateOfBirth != null ? p.dateOfBirth.toString() : null,
            p.gender != null ? p.gender.description : null,
            p.notes
        );
    }
}
