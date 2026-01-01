package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Person;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class PersonRepository implements PanacheRepository<Person> {

    public Optional<Person> findByEmail(String email) {
        return find("LOWER(email)", email.toLowerCase().trim()).firstResultOptional();
    }

    public List<Person> listAllOrdered() {
        return list("ORDER BY lastName ASC, firstName ASC");
    }

    /**
     * List all persons ordered, with title eagerly fetched for display name rendering.
     */
    public List<Person> listAllOrderedWithTitle() {
        return getEntityManager()
            .createQuery(
                "SELECT p FROM Person p LEFT JOIN FETCH p.title ORDER BY p.lastName ASC, p.firstName ASC",
                Person.class
            )
            .getResultList();
    }

    public List<Person> findByFilter(String filterText, String sortField, String sortDir) {
        String orderBy = buildOrderBy(sortField, sortDir);

        if (filterText != null && !filterText.isBlank()) {
            String pattern = "%" + filterText.toLowerCase().trim() + "%";
            return list(
                "LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1 OR LOWER(email) LIKE ?1 " +
                    orderBy,
                pattern
            );
        }
        return find("FROM Person " + orderBy).list();
    }

    private String buildOrderBy(String sortField, String sortDir) {
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        String orderBy = switch (sortField != null ? sortField : "") {
            case "firstName" -> "firstName " + direction + ", lastName ASC";
            case "lastName" -> "lastName " + direction + ", firstName ASC";
            case "email" -> "email " + direction;
            default -> "lastName ASC, firstName ASC";
        };
        return "ORDER BY " + orderBy;
    }

    public boolean existsByEmail(String email) {
        return count("LOWER(email) = LOWER(?1)", email.trim()) > 0;
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        return count("LOWER(email) = LOWER(?1) AND id != ?2", email.trim(), id) > 0;
    }

    /**
     * Count relationships where person is the source.
     */
    public Map<Long, Integer> countRelationshipsByPerson() {
        List<Object[]> results = getEntityManager()
            .createQuery(
                "SELECT pr.sourcePerson.id, COUNT(pr) FROM PersonRelationship pr GROUP BY pr.sourcePerson.id",
                Object[].class
            )
            .getResultList();

        Map<Long, Integer> counts = new HashMap<>();
        for (Object[] row : results) {
            counts.put((Long) row[0], ((Long) row[1]).intValue());
        }
        return counts;
    }
}
