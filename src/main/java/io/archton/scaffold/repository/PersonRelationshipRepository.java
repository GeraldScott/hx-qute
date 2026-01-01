package io.archton.scaffold.repository;

import io.archton.scaffold.entity.PersonRelationship;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PersonRelationshipRepository implements PanacheRepository<PersonRelationship> {

    /**
     * Find all relationships where the given person is the source.
     */
    public List<PersonRelationship> findBySourcePersonId(Long sourcePersonId) {
        return list("sourcePerson.id = ?1 ORDER BY relatedPerson.lastName ASC, relatedPerson.firstName ASC",
            sourcePersonId);
    }

    /**
     * Find relationships with filter and sort for a given source person.
     * Uses JOIN FETCH to eagerly load related entities for template rendering.
     */
    public List<PersonRelationship> findBySourcePersonWithFilter(
            Long sourcePersonId, String filterText, String sortField, String sortDir) {

        String orderBy = buildOrderBy(sortField, sortDir);

        StringBuilder jpql = new StringBuilder(
            "SELECT pr FROM PersonRelationship pr " +
            "JOIN FETCH pr.relatedPerson rp " +
            "JOIN FETCH pr.relationship r " +
            "LEFT JOIN FETCH rp.title " +
            "WHERE pr.sourcePerson.id = ?1"
        );

        if (filterText != null && !filterText.isBlank()) {
            String pattern = "%" + filterText.toLowerCase().trim() + "%";
            jpql.append(" AND (LOWER(rp.firstName) LIKE ?2 OR LOWER(rp.lastName) LIKE ?2 OR LOWER(r.description) LIKE ?2)");
            return getEntityManager()
                .createQuery(jpql.append(" ").append(orderBy).toString(), PersonRelationship.class)
                .setParameter(1, sourcePersonId)
                .setParameter(2, pattern)
                .getResultList();
        }

        return getEntityManager()
            .createQuery(jpql.append(" ").append(orderBy).toString(), PersonRelationship.class)
            .setParameter(1, sourcePersonId)
            .getResultList();
    }

    private String buildOrderBy(String sortField, String sortDir) {
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        String orderBy = switch (sortField != null ? sortField : "") {
            case "firstName" -> "relatedPerson.firstName " + direction + ", relatedPerson.lastName ASC";
            case "lastName" -> "relatedPerson.lastName " + direction + ", relatedPerson.firstName ASC";
            case "relationship" -> "relationship.description " + direction;
            default -> "relatedPerson.lastName ASC, relatedPerson.firstName ASC";
        };
        return "ORDER BY " + orderBy;
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
}
