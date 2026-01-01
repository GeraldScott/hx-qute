package io.archton.scaffold.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "relationship", uniqueConstraints = {
    @UniqueConstraint(columnNames = "code"),
    @UniqueConstraint(columnNames = "description")
})
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
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

    public Relationship() {
    }

    public Relationship(String code, String description) {
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
