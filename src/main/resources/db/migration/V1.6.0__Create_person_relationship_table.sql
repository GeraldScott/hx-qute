-- Person Relationship join table for US-003-07
CREATE TABLE person_relationship (
    id BIGSERIAL PRIMARY KEY,
    source_person_id BIGINT NOT NULL,
    related_person_id BIGINT NOT NULL,
    relationship_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_person_rel_source FOREIGN KEY (source_person_id) REFERENCES person(id) ON DELETE CASCADE,
    CONSTRAINT fk_person_rel_related FOREIGN KEY (related_person_id) REFERENCES person(id) ON DELETE CASCADE,
    CONSTRAINT fk_person_rel_type FOREIGN KEY (relationship_id) REFERENCES relationship(id),
    CONSTRAINT uk_person_relationship UNIQUE (source_person_id, related_person_id, relationship_id),
    CONSTRAINT chk_not_self_relationship CHECK (source_person_id != related_person_id)
);

CREATE INDEX idx_person_rel_source ON person_relationship(source_person_id);
CREATE INDEX idx_person_rel_related ON person_relationship(related_person_id);
CREATE INDEX idx_person_rel_type ON person_relationship(relationship_id);
