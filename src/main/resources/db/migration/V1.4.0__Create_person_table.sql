-- Feature 003: Person Management
-- Create person table with foreign keys to title and gender

CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    date_of_birth DATE,
    title_id BIGINT REFERENCES title(id),
    gender_id BIGINT REFERENCES gender(id),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_person_email UNIQUE (email)
);

CREATE INDEX idx_person_email ON person(email);
CREATE INDEX idx_person_last_name ON person(last_name);
CREATE INDEX idx_person_first_name ON person(first_name);
CREATE INDEX idx_person_title ON person(title_id);
CREATE INDEX idx_person_gender ON person(gender_id);
