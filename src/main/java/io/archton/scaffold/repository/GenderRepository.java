package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Gender;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GenderRepository implements PanacheRepository<Gender> {

    @Inject
    PersonRepository personRepository;

    public Optional<Gender> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    public Optional<Gender> findByDescription(String description) {
        return find("description", description).firstResultOptional();
    }

    public List<Gender> listAllOrdered() {
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

    public boolean isReferencedByPerson(Long genderId) {
        return personRepository.count("gender.id", genderId) > 0;
    }

    public long countPersonReferences(Long genderId) {
        return personRepository.count("gender.id", genderId);
    }
}
