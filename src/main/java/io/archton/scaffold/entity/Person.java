package io.archton.scaffold.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "person", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "name_first")
    public String nameFirst;

    @Column(name = "name_last", nullable = false)
    public String nameLast;

    @Column(name = "email", nullable = false, unique = true)
    public String email;

    public Person() {
    }
}
