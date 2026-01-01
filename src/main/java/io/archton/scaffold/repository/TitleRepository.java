package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Title;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TitleRepository implements PanacheRepository<Title> {

    @Inject
    PersonRepository personRepository;

    public Optional<Title> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    public Optional<Title> findByDescription(String description) {
        return find("description", description).firstResultOptional();
    }

    public List<Title> listAllOrdered() {
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

    public boolean isReferencedByPerson(Long titleId) {
        return personRepository.count("title.id", titleId) > 0;
    }

    public long countPersonReferences(Long titleId) {
        return personRepository.count("title.id", titleId);
    }
}
