package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Gender;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class GenderRepository implements PanacheRepository<Gender> {

    public Gender findByCode(String code) {
        return find("code", code).firstResult();
    }

    public Gender findByDescription(String description) {
        return find("description", description).firstResult();
    }

    public List<Gender> listAllOrdered() {
        return list("ORDER BY code ASC");
    }
}
