-- Insert sample admin user (password: admin123)
INSERT INTO users (username, email, password, active) VALUES 
    ('admin', 'admin@example.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', true);

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- Insert sample regular user (password: user123)
INSERT INTO users (username, email, password, active) VALUES 
    ('user', 'user@example.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', true);

-- Assign user role to regular user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'user' AND r.name = 'ROLE_USER';

-- Insert sample products
INSERT INTO products (name, description, price, quantity, category, active) VALUES 
    ('Laptop', 'High-performance laptop for professionals', 1299.99, 50, 'Electronics', true),
    ('Smartphone', 'Latest smartphone with advanced features', 699.99, 100, 'Electronics', true),
    ('Office Chair', 'Ergonomic office chair for comfortable work', 249.99, 25, 'Furniture', true),
    ('Desk Lamp', 'LED desk lamp with adjustable brightness', 39.99, 75, 'Furniture', true),
    ('Coffee Mug', 'Ceramic coffee mug with company logo', 12.99, 200, 'Office Supplies', true),
    ('Notebook', 'Professional notebook for meetings', 8.99, 150, 'Office Supplies', true),
    ('Wireless Mouse', 'Ergonomic wireless mouse', 29.99, 80, 'Electronics', true),
    ('Keyboard', 'Mechanical keyboard for programmers', 89.99, 40, 'Electronics', true),
    ('Monitor', '27-inch 4K monitor', 399.99, 30, 'Electronics', true),
    ('Headphones', 'Noise-cancelling headphones', 199.99, 60, 'Electronics', true);