package io.archton.scaffold.repository;

import io.archton.scaffold.entity.Person;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class PersonRepository implements PanacheRepository<Person> {

    public Person findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public List<Person> findByNameLast(String nameLast) {
        return list("nameLast", nameLast);
    }

    public List<Person> findByNameFirst(String nameFirst) {
        return list("nameFirst", nameFirst);
    }

    public List<Person> findByName(String nameFirst, String nameLast) {
        return list("nameFirst = ?1 and nameLast = ?2", nameFirst, nameLast);
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}
