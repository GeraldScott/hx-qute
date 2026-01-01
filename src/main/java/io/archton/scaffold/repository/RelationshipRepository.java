package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Relationship;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RelationshipRepository implements PanacheRepository<Relationship> {

    public Optional<Relationship> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    public Optional<Relationship> findByDescription(String description) {
        return find("description", description).firstResultOptional();
    }

    public List<Relationship> listAllOrdered() {
        return list("ORDER BY code ASC");
    }

    public boolean existsByCode(String code) {
        return count("code", code) > 0;
    }

    public boolean existsByCodeAndIdNot(String code, Long id) {
        return count("code = ?1 AND id != ?2", code, id) > 0;
    }

    public boolean existsByDescription(String description) {
        return count("description", description) > 0;
    }

    public boolean existsByDescriptionAndIdNot(String description, Long id) {
        return count("description = ?1 AND id != ?2", description, id) > 0;
    }
}
