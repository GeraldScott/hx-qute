CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    name_first VARCHAR(255),
    name_last VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);
