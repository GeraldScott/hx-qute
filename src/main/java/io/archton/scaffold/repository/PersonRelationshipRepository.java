package io.archton.scaffold.repository;

import io.archton.scaffold.entity.PersonRelationship;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityGraph;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PersonRelationshipRepository implements PanacheRepository<PersonRelationship> {

    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    /**
     * Find all relationships where the given person is the source.
     */
    public List<PersonRelationship> findBySourcePersonId(Long sourcePersonId) {
        return list("sourcePerson.id = ?1 ORDER BY relatedPerson.lastName, relatedPerson.firstName",
            sourcePersonId);
    }

    /**
     * Find relationships with filter and sort for a given source person.
     * Uses Entity Graph to eagerly load related entities for template rendering.
     */
    public List<PersonRelationship> findBySourcePersonWithFilter(
            Long sourcePersonId, String filterText, String sortField, String sortDir) {

        EntityGraph<?> graph = getEntityManager().getEntityGraph("PersonRelationship.withDetails");
        String orderBy = buildOrderBy(sortField, sortDir);

        if (filterText != null && !filterText.isBlank()) {
            String pattern = "%" + filterText.toLowerCase().trim() + "%";
            return find(
                "sourcePerson.id = ?1 AND (LOWER(relatedPerson.firstName) LIKE ?2 " +
                "OR LOWER(relatedPerson.lastName) LIKE ?2 OR LOWER(relationship.description) LIKE ?2) " +
                orderBy,
                sourcePersonId, pattern
            ).withHint(FETCH_GRAPH_HINT, graph).list();
        }

        return find("sourcePerson.id = ?1 " + orderBy, sourcePersonId)
            .withHint(FETCH_GRAPH_HINT, graph)
            .list();
    }

    private String buildOrderBy(String sortField, String sortDir) {
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        return switch (sortField != null ? sortField : "") {
            case "firstName" -> "ORDER BY relatedPerson.firstName " + direction + ", relatedPerson.lastName";
            case "lastName" -> "ORDER BY relatedPerson.lastName " + direction + ", relatedPerson.firstName";
            case "relationship" -> "ORDER BY relationship.description " + direction;
            default -> "ORDER BY relatedPerson.lastName, relatedPerson.firstName";
        };
    }

    /**
     * Check if relationship already exists (for unique constraint validation).
     */
    public boolean exists(Long sourcePersonId, Long relatedPersonId, Long relationshipId) {
        return count("sourcePerson.id = ?1 AND relatedPerson.id = ?2 AND relationship.id = ?3",
            sourcePersonId, relatedPersonId, relationshipId) > 0;
    }

    /**
     * Check if relationship exists excluding a specific record (for update validation).
     */
    public boolean existsExcluding(Long sourcePersonId, Long relatedPersonId, Long relationshipId, Long excludeId) {
        return count("sourcePerson.id = ?1 AND relatedPerson.id = ?2 AND relationship.id = ?3 AND id != ?4",
            sourcePersonId, relatedPersonId, relationshipId, excludeId) > 0;
    }

    /**
     * Count relationships for a source person.
     */
    public long countBySourcePerson(Long sourcePersonId) {
        return count("sourcePerson.id", sourcePersonId);
    }

    /**
     * Find all relationships where any of the given person IDs appear as source or related person.
     * Used for BFS network traversal: each depth level queries the current frontier.
     */
    public List<PersonRelationship> findConnectionsForPersonIds(Set<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            return Collections.emptyList();
        }

        EntityGraph<?> graph = getEntityManager().getEntityGraph("PersonRelationship.withFullDetails");

        return find("sourcePerson.id IN ?1 OR relatedPerson.id IN ?1", List.copyOf(personIds))
            .withHint(FETCH_GRAPH_HINT, graph)
            .list();
    }
}
