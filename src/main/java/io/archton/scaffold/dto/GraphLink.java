package io.archton.scaffold.dto;

import io.archton.scaffold.entity.PersonRelationship;

public record GraphLink(
    String source,
    String target,
    String label
) {
    public static GraphLink from(PersonRelationship pr) {
        return new GraphLink(
            String.valueOf(pr.sourcePerson.id),
            String.valueOf(pr.relatedPerson.id),
            pr.relationship.description
        );
    }
}
