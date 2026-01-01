package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Person;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PersonRepository implements PanacheRepository<Person> {

    public Person findByEmail(String email) {
        return find("LOWER(email)", email.toLowerCase().trim()).firstResult();
    }

    public List<Person> listAllOrdered() {
        return list("ORDER BY lastName ASC, firstName ASC");
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
}
