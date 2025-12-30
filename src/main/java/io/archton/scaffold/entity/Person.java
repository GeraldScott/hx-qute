package io.archton.scaffold.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "person", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class Person extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    public String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    public String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    public String email;

    @Column(name = "phone", length = 50)
    public String phone;

    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id")
    public Title title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id")
    public Gender gender;

    @Column(name = "created_at")
    public OffsetDateTime createdAt;

    @Column(name = "updated_at")
    public OffsetDateTime updatedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    public Person() {
    }

    // Display name helper
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (title != null) {
            sb.append(title.code).append(" ");
        }
        sb.append(firstName).append(" ").append(lastName);
        return sb.toString().trim();
    }

    // Static finder methods (Active Record pattern)
    public static Person findByEmail(String email) {
        return find("LOWER(email)", email.toLowerCase().trim()).firstResult();
    }

    public static List<Person> listAllOrdered() {
        return list("ORDER BY lastName ASC, firstName ASC");
    }

    public static List<Person> findByFilter(String filterText, String sortField, String sortDir) {
        String orderBy = buildOrderBy(sortField, sortDir);

        if (filterText != null && !filterText.isBlank()) {
            String pattern = "%" + filterText.toLowerCase().trim() + "%";
            return list(
                "LOWER(firstName) LIKE ?1 OR LOWER(lastName) LIKE ?1 OR LOWER(email) LIKE ?1 " + orderBy,
                pattern
            );
        }
        // For unfiltered list, use find with empty query and ORDER BY
        return find("FROM Person " + orderBy).list();
    }

    private static String buildOrderBy(String sortField, String sortDir) {
        String direction = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        String orderBy = switch (sortField != null ? sortField : "") {
            case "firstName" -> "firstName " + direction + ", lastName ASC";
            case "lastName" -> "lastName " + direction + ", firstName ASC";
            case "email" -> "email " + direction;
            default -> "lastName ASC, firstName ASC";
        };
        return "ORDER BY " + orderBy;
    }

    // Lifecycle callbacks
    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }
}
