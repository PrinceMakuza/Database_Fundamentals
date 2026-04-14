-- 0. Cleanup existing tables (Order is important for dependencies)
DROP TABLE IF EXISTS CartItems;
DROP TABLE IF EXISTS Cart;
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS OrderItems;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Inventory;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Categories;

-- 1. Categories Table
CREATE TABLE IF NOT EXISTS Categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- 2. Users Table (Simplified for Demo)
CREATE TABLE IF NOT EXISTS Users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('ADMIN', 'CUSTOMER'))
);

-- 3. Products Table
CREATE TABLE IF NOT EXISTS Products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    category_id INTEGER NOT NULL REFERENCES Categories(category_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- For fast cart item lookups

-- 4. Orders Table
CREATE TABLE IF NOT EXISTS Orders (
    order_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'SHIPPED', 'CANCELLED'))
);

-- Real-world optimized indexes
-- For lookup of orders by user and status
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON Orders(user_id, status);

-- Composite index for optimized product searching (Category + Name)
CREATE INDEX IF NOT EXISTS idx_products_category_name ON Products(category_id, name);

-- 5. OrderItems Table
CREATE TABLE IF NOT EXISTS OrderItems (
    order_item_id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES Orders(order_id) ON DELETE CASCADE ON UPDATE CASCADE,
    product_id INTEGER NOT NULL REFERENCES Products(product_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0)
);

-- 6. Reviews Table
CREATE TABLE IF NOT EXISTS Reviews (
    review_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL REFERENCES Products(product_id) ON DELETE CASCADE ON UPDATE CASCADE,
    user_id INTEGER NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Inventory Table
CREATE TABLE IF NOT EXISTS Inventory (
    product_id INTEGER PRIMARY KEY REFERENCES Products(product_id) ON DELETE CASCADE ON UPDATE CASCADE,
    quantity_on_hand INTEGER NOT NULL CHECK (quantity_on_hand >= 0),
    reorder_level INTEGER NOT NULL DEFAULT 10 CHECK (reorder_level >= 0)
);

-- 8. Cart Table (User Story 4.1)
CREATE TABLE IF NOT EXISTS Cart (
    cart_id SERIAL PRIMARY KEY,
    user_id INTEGER UNIQUE NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 9. CartItems Table
CREATE TABLE IF NOT EXISTS CartItems (
    cart_item_id SERIAL PRIMARY KEY,
    cart_id INTEGER NOT NULL REFERENCES Cart(cart_id) ON DELETE CASCADE ON UPDATE CASCADE,
    product_id INTEGER NOT NULL REFERENCES Products(product_id) ON DELETE CASCADE ON UPDATE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cart_id, product_id) -- Prevent duplicate products in the same cart
);
