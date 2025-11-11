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

    public boolean existsByCode(String code) {
        return count("code", code) > 0;
    }

    public boolean existsByDescription(String description) {
        return count("description", description) > 0;
    }

    public List<Gender> listAllOrderedByDescription() {
        return listAll(io.quarkus.panache.common.Sort.by("description"));
    }
}
