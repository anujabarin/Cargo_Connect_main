CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    is_google_account BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Email lookup index
CREATE INDEX idx_users_email ON users (email);

-- Test account
INSERT INTO users (
    id, 
    email, 
    password_hash, 
    full_name, 
    is_google_account
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000', 
    'demo@cargolive.com', 
    'placeholder_hash', 
    'Demo User', 
    FALSE
);