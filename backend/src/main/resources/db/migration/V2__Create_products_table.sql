-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL CHECK (price > 0),
    category VARCHAR(100) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    sku VARCHAR(50) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED', 'OUT_OF_STOCK')),
    image_url VARCHAR(500),
    weight DECIMAL(11, 3) CHECK (weight >= 0),
    brand VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_product_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_product_brand ON products(brand);
CREATE INDEX IF NOT EXISTS idx_product_price ON products(price);
CREATE INDEX IF NOT EXISTS idx_product_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_product_stock_quantity ON products(stock_quantity);
CREATE INDEX IF NOT EXISTS idx_product_created_at ON products(created_at);
CREATE INDEX IF NOT EXISTS idx_product_sku ON products(sku);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_product_category_status ON products(category, status);
CREATE INDEX IF NOT EXISTS idx_product_brand_status ON products(brand, status);
CREATE INDEX IF NOT EXISTS idx_product_status_stock ON products(status, stock_quantity);

-- Create trigger to automatically update updated_at on products table
CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert sample products
INSERT INTO products (name, description, price, category, stock_quantity, sku, status, brand, weight) VALUES
(
    'Laptop Pro 15"',
    'High-performance laptop with 15-inch display, 16GB RAM, and 512GB SSD',
    1299.99,
    'Electronics',
    25,
    'LAPTOP-PRO-15',
    'ACTIVE',
    'TechBrand',
    2.1
),
(
    'Wireless Mouse',
    'Ergonomic wireless mouse with precision tracking',
    29.99,
    'Electronics',
    150,
    'MOUSE-WL-001',
    'ACTIVE',
    'TechBrand',
    0.15
),
(
    'Coffee Mug',
    'Ceramic coffee mug with heat-resistant handle',
    12.99,
    'Home & Kitchen',
    200,
    'MUG-COFFEE-001',
    'ACTIVE',
    'HomeBrand',
    0.35
),
(
    'Running Shoes',
    'Lightweight running shoes with advanced cushioning',
    89.99,
    'Sports & Outdoors',
    75,
    'SHOES-RUN-001',
    'ACTIVE',
    'SportsBrand',
    0.8
),
(
    'Smartphone',
    'Latest smartphone with 128GB storage and dual camera',
    699.99,
    'Electronics',
    50,
    'PHONE-SMART-001',
    'ACTIVE',
    'TechBrand',
    0.18
),
(
    'Desk Chair',
    'Ergonomic office chair with lumbar support',
    199.99,
    'Furniture',
    30,
    'CHAIR-DESK-001',
    'ACTIVE',
    'OfficeBrand',
    15.5
),
(
    'Water Bottle',
    'Stainless steel water bottle with insulation',
    24.99,
    'Sports & Outdoors',
    100,
    'BOTTLE-WATER-001',
    'ACTIVE',
    'SportsBrand',
    0.45
),
(
    'Bluetooth Headphones',
    'Noise-cancelling wireless headphones',
    149.99,
    'Electronics',
    60,
    'HEADPHONES-BT-001',
    'ACTIVE',
    'TechBrand',
    0.25
),
(
    'Backpack',
    'Durable backpack with multiple compartments',
    59.99,
    'Fashion',
    80,
    'BACKPACK-001',
    'ACTIVE',
    'FashionBrand',
    1.2
),
(
    'Tablet 10"',
    '10-inch tablet with high-resolution display',
    399.99,
    'Electronics',
    40,
    'TABLET-10-001',
    'ACTIVE',
    'TechBrand',
    0.5
),
(
    'Yoga Mat',
    'Non-slip yoga mat with carrying strap',
    34.99,
    'Sports & Outdoors',
    120,
    'YOGA-MAT-001',
    'ACTIVE',
    'SportsBrand',
    1.8
),
(
    'LED Desk Lamp',
    'Adjustable LED desk lamp with USB charging port',
    45.99,
    'Home & Kitchen',
    90,
    'LAMP-LED-001',
    'ACTIVE',
    'HomeBrand',
    1.1
),
(
    'Gaming Keyboard',
    'Mechanical gaming keyboard with RGB lighting',
    129.99,
    'Electronics',
    35,
    'KEYBOARD-GAMING-001',
    'ACTIVE',
    'TechBrand',
    1.3
),
(
    'Protein Powder',
    'Whey protein powder for muscle building',
    39.99,
    'Health & Wellness',
    200,
    'PROTEIN-WHEY-001',
    'ACTIVE',
    'HealthBrand',
    2.3
),
(
    'Sunglasses',
    'UV protection sunglasses with polarized lenses',
    79.99,
    'Fashion',
    110,
    'SUNGLASSES-001',
    'ACTIVE',
    'FashionBrand',
    0.12
)
ON CONFLICT (sku) DO NOTHING;
