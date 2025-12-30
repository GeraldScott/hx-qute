-- Title table for storing honorifics (Mr, Ms, Mrs, Dr, Prof, Rev, etc.)
CREATE TABLE title (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(5) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

-- Create indexes for efficient lookups
CREATE INDEX idx_title_code ON title(code);
CREATE INDEX idx_title_description ON title(description);
