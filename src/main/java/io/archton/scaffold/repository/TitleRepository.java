package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Title;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class TitleRepository implements PanacheRepository<Title> {

    public Title findByCode(String code) {
        return find("code", code).firstResult();
    }

    public Title findByDescription(String description) {
        return find("description", description).firstResult();
    }

    public List<Title> listAllOrdered() {
        return list("ORDER BY code ASC");
    }
}
