-- Insert admin UserLogin (password: AdminPassword123, BCrypt hash with cost 12)
INSERT INTO user_login (email, password, role, first_name, last_name, created_at, updated_at, active)
VALUES (
    'admin@example.com',
    '$2a$12$LKOvSguji19sgh76WP4tiefIT2xWcbccb0nJy2cK3GdvCww8Bvsua',
    'admin',
    'Admin',
    'User',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    TRUE
);
