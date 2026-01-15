-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT true,
    account_non_expired BOOLEAN NOT NULL DEFAULT true,
    account_non_locked BOOLEAN NOT NULL DEFAULT true,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_user_enabled ON users(enabled);

-- Create user_roles table for many-to-many relationship
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for user_roles
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role);

-- Insert default admin user (password: admin123)
INSERT INTO users (id, username, email, password, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@example.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfhBdQdK/VBPZx2', -- admin123
    'System',
    'Administrator',
    true,
    true,
    true,
    true
) ON CONFLICT (username) DO NOTHING;

-- Insert admin role for the default admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE username = 'admin'
ON CONFLICT (user_id, role) DO NOTHING;

-- Insert default regular user (password: user123)
INSERT INTO users (id, username, email, password, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES (
    gen_random_uuid(),
    'user',
    'user@example.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- user123
    'Test',
    'User',
    true,
    true,
    true,
    true
) ON CONFLICT (username) DO NOTHING;

-- Insert user role for the default regular user
INSERT INTO user_roles (user_id, role)
SELECT id, 'USER'
FROM users
WHERE username = 'user'
ON CONFLICT (user_id, role) DO NOTHING;

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at on users table
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
