-- Seed Data for Smart E-Commerce System
-- Provides sample data for testing all modules

-- Categories (2+ required)
INSERT INTO Categories (name, description) VALUES
('Electronics', 'Gadgets and electronic devices'),
('Home & Kitchen', 'Appliances and home decor');

-- Users (Admin and Customer demo users)
-- admin123 -> $2a$10$tzwCuVUnlkYOz3ECsng5l.PvkQ2UYl89.yz8oh/iTP0.5iCUT.kya
-- password123 -> $2a$10$IsAwUAs2zQzbYLBPTaAwmeikub506iFXRjG2V2NFILUMy48b4NMme
INSERT INTO Users (user_id, name, email, password_hash, role, location) VALUES
(1, 'John Doe', 'john@example.com', '$2a$10$IsAwUAs2zQzbYLBPTaAwmeikub506iFXRjG2V2NFILUMy48b4NMme', 'CUSTOMER', 'Accra, Ghana'),
(2, 'Jane Smith', 'jane@example.com', '$2a$10$IsAwUAs2zQzbYLBPTaAwmeikub506iFXRjG2V2NFILUMy48b4NMme', 'CUSTOMER', 'Nairobi, Kenya'),
(3, 'Michael Obi', 'michael@example.com', '$2a$10$IsAwUAs2zQzbYLBPTaAwmeikub506iFXRjG2V2NFILUMy48b4NMme', 'CUSTOMER', 'Lagos, Nigeria'),
(4, 'Sarah Müller', 'sarah@example.com', '$2a$10$IsAwUAs2zQzbYLBPTaAwmeikub506iFXRjG2V2NFILUMy48b4NMme', 'CUSTOMER', 'Berlin, Germany'),
(100, 'System Admin', 'admin@ecommerce.com', '$2a$10$tzwCuVUnlkYOz3ECsng5l.PvkQ2UYl89.yz8oh/iTP0.5iCUT.kya', 'ADMIN', NULL);

-- Products (5+ required)
INSERT INTO Products (name, description, price, category_id) VALUES
('Laptop Pro', 'High-performance laptop for professionals', 1299.99, 1),
('Wireless Mouse', 'Ergonomic wireless mouse with 2.4GHz connection', 25.50, 1),
('Coffee Maker', 'Drip coffee maker with programmable timer', 49.99, 2),
('Standing Desk', 'Adjustable height standing desk', 350.00, 2),
('Java Programming', 'Comprehensive guide to Java 21', 55.00, 1);

-- Inventory (one per product)
INSERT INTO Inventory (product_id, quantity_on_hand, reorder_level) VALUES
(1, 50, 10),
(2, 200, 20),
(3, 30, 5),
(4, 15, 2),
(5, 100, 15);

-- Orders (1 order with 2 items required)
INSERT INTO Orders (user_id, order_date, total_amount) VALUES
(1, '2026-04-01 10:30:00', 1325.49);

-- OrderItems (2 items in the order)
INSERT INTO OrderItems (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 1299.99),
(1, 2, 1, 25.50);

-- Reviews (3 required)
INSERT INTO Reviews (product_id, user_id, rating, comment) VALUES
(1, 1, 5, 'Great laptop, very fast!'),
(2, 2, 4, 'Good mouse, but the battery life could be better.'),
(3, 1, 3, 'Average coffee maker, does the job.');

-- Sync sequence for registration
SELECT setval('users_user_id_seq', (SELECT MAX(user_id) FROM Users));
