package io.archton.scaffold.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "gender", uniqueConstraints = {
    @UniqueConstraint(name = "uk_gender_code", columnNames = "code"),
    @UniqueConstraint(name = "uk_gender_description", columnNames = "description")
})
public class Gender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", nullable = false, unique = true, length = 1)
    public String code;

    @Column(name = "description", nullable = false, unique = true, length = 255)
    public String description;

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    public Gender() {
    }

    public Gender(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // Lifecycle callbacks
    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
