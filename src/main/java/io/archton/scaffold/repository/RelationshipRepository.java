package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Relationship;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RelationshipRepository implements PanacheRepository<Relationship> {

    public Relationship findByCode(String code) {
        return find("code", code).firstResult();
    }

    public Relationship findByDescription(String description) {
        return find("description", description).firstResult();
    }

    public List<Relationship> listAllOrdered() {
        return list("ORDER BY code ASC");
    }
}
