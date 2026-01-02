package io.archton.scaffold.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "person_relationship", uniqueConstraints = {
    @UniqueConstraint(name = "uk_person_relationship",
        columnNames = {"source_person_id", "related_person_id", "relationship_id"})
})
@NamedEntityGraph(
    name = "PersonRelationship.withDetails",
    attributeNodes = {
        @NamedAttributeNode(value = "relatedPerson", subgraph = "person-title"),
        @NamedAttributeNode("relationship")
    },
    subgraphs = @NamedSubgraph(name = "person-title", attributeNodes = @NamedAttributeNode("title"))
)
public class PersonRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_person_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_person_rel_source"))
    public Person sourcePerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_person_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_person_rel_related"))
    public Person relatedPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_person_rel_type"))
    public Relationship relationship;

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    public PersonRelationship() {}

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
