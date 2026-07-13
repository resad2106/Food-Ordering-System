-- =============================================================================
-- Food Ordering System — Seed Data
-- Run after schema.sql
-- =============================================================================

USE food_ordering_db;

-- Password for all demo users: password123
-- BCrypt hash (cost 10) — replace with application hashing in production
SET @demo_password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------
INSERT INTO users (username, email, password_hash, role) VALUES
    ('admin',       'admin@foodorder.local',       @demo_password_hash, 'ADMIN'),
    ('owner_pizza', 'owner.pizza@foodorder.local', @demo_password_hash, 'RESTAURANT_OWNER'),
    ('owner_sushi', 'owner.sushi@foodorder.local', @demo_password_hash, 'RESTAURANT_OWNER'),
    ('alice',       'alice@example.com',           @demo_password_hash, 'CUSTOMER'),
    ('bob',         'bob@example.com',             @demo_password_hash, 'CUSTOMER');

-- ---------------------------------------------------------------------------
-- restaurants
-- ---------------------------------------------------------------------------
INSERT INTO restaurants (name, description, address, phone, owner_id, is_active) VALUES
    (
        'Mario''s Pizza House',
        'Authentic wood-fired pizzas and Italian classics.',
        '12 Main Street, Downtown',
        '+1-555-0101',
        (SELECT id FROM users WHERE username = 'owner_pizza'),
        1
    ),
    (
        'Tokyo Sushi Bar',
        'Fresh sushi, sashimi, and Japanese specialties.',
        '88 Harbor Road, Waterfront',
        '+1-555-0202',
        (SELECT id FROM users WHERE username = 'owner_sushi'),
        1
    ),
    (
        'Green Bowl Kitchen',
        'Healthy salads, bowls, and plant-based meals.',
        '45 Oak Avenue, Midtown',
        '+1-555-0303',
        NULL,
        1
    );

-- ---------------------------------------------------------------------------
-- menu_items — Mario's Pizza House
-- ---------------------------------------------------------------------------
INSERT INTO menu_items (restaurant_id, name, description, price, category, is_available)
SELECT r.id, item.name, item.description, item.price, item.category, 1
FROM restaurants r
CROSS JOIN (
    SELECT 'Margherita Pizza'   AS name, 'Tomato, mozzarella, basil'        AS description, 12.99 AS price, 'Pizza'    AS category UNION ALL
    SELECT 'Pepperoni Pizza',           'Classic pepperoni with cheese',             14.99,              'Pizza'    UNION ALL
    SELECT 'Garlic Bread',              'Toasted bread with garlic butter',           5.49,              'Sides'    UNION ALL
    SELECT 'Caesar Salad',              'Romaine, parmesan, croutons',                8.99,              'Salads'   UNION ALL
    SELECT 'Tiramisu',                  'Classic Italian dessert',                    6.99,              'Desserts'
) AS item
WHERE r.name = 'Mario''s Pizza House';

-- ---------------------------------------------------------------------------
-- menu_items — Tokyo Sushi Bar
-- ---------------------------------------------------------------------------
INSERT INTO menu_items (restaurant_id, name, description, price, category, is_available)
SELECT r.id, item.name, item.description, item.price, item.category, 1
FROM restaurants r
CROSS JOIN (
    SELECT 'Salmon Nigiri'      AS name, 'Fresh salmon over seasoned rice'  AS description,  6.50 AS price, 'Nigiri'   AS category UNION ALL
    SELECT 'California Roll',           'Crab, avocado, cucumber',                   8.99,              'Rolls'    UNION ALL
    SELECT 'Spicy Tuna Roll',           'Tuna with spicy mayo',                      9.99,              'Rolls'    UNION ALL
    SELECT 'Miso Soup',                 'Traditional soybean soup',                   3.99,              'Soups'    UNION ALL
    SELECT 'Edamame',                   'Steamed soybeans with sea salt',             4.99,              'Appetizers'
) AS item
WHERE r.name = 'Tokyo Sushi Bar';

-- ---------------------------------------------------------------------------
-- menu_items — Green Bowl Kitchen
-- ---------------------------------------------------------------------------
INSERT INTO menu_items (restaurant_id, name, description, price, category, is_available)
SELECT r.id, item.name, item.description, item.price, item.category, 1
FROM restaurants r
CROSS JOIN (
    SELECT 'Quinoa Power Bowl'  AS name, 'Quinoa, chickpeas, avocado, greens' AS description, 11.99 AS price, 'Bowls'    AS category UNION ALL
    SELECT 'Kale Caesar Salad',         'Kale, tahini dressing, croutons',           10.49,              'Salads'   UNION ALL
    SELECT 'Veggie Wrap',               'Grilled vegetables in whole wheat wrap',     9.49,              'Wraps'    UNION ALL
    SELECT 'Green Smoothie',              'Spinach, apple, ginger',                     5.99,              'Drinks'
) AS item
WHERE r.name = 'Green Bowl Kitchen';

-- ---------------------------------------------------------------------------
-- sample cart for alice (Mario's Pizza House)
-- ---------------------------------------------------------------------------
INSERT INTO cart (user_id, restaurant_id)
SELECT u.id, r.id
FROM users u, restaurants r
WHERE u.username = 'alice' AND r.name = 'Mario''s Pizza House';

INSERT INTO cart_items (cart_id, menu_item_id, quantity, unit_price)
SELECT
    c.id,
    mi.id,
    2,
    mi.price
FROM cart c
JOIN users u ON c.user_id = u.id
JOIN menu_items mi ON mi.restaurant_id = c.restaurant_id AND mi.name = 'Margherita Pizza'
WHERE u.username = 'alice';

INSERT INTO cart_items (cart_id, menu_item_id, quantity, unit_price)
SELECT
    c.id,
    mi.id,
    1,
    mi.price
FROM cart c
JOIN users u ON c.user_id = u.id
JOIN menu_items mi ON mi.restaurant_id = c.restaurant_id AND mi.name = 'Garlic Bread'
WHERE u.username = 'alice';

-- ---------------------------------------------------------------------------
-- sample completed order for bob (Tokyo Sushi Bar)
-- ---------------------------------------------------------------------------
INSERT INTO orders (user_id, restaurant_id, status, total_amount, delivery_address, notes)
SELECT
    u.id,
    r.id,
    'DELIVERED',
    28.47,
    '7 Elm Street, Apt 4B',
    'Extra wasabi please'
FROM users u, restaurants r
WHERE u.username = 'bob' AND r.name = 'Tokyo Sushi Bar';

INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, unit_price, subtotal)
SELECT
    o.id,
    mi.id,
    mi.name,
    2,
    mi.price,
    mi.price * 2
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN restaurants r ON o.restaurant_id = r.id
JOIN menu_items mi ON mi.restaurant_id = r.id AND mi.name = 'California Roll'
WHERE u.username = 'bob' AND r.name = 'Tokyo Sushi Bar'
ORDER BY o.id DESC
LIMIT 1;

INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, unit_price, subtotal)
SELECT
    o.id,
    mi.id,
    mi.name,
    1,
    mi.price,
    mi.price
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN restaurants r ON o.restaurant_id = r.id
JOIN menu_items mi ON mi.restaurant_id = r.id AND mi.name = 'Spicy Tuna Roll'
WHERE u.username = 'bob' AND r.name = 'Tokyo Sushi Bar'
ORDER BY o.id DESC
LIMIT 1;

-- ---------------------------------------------------------------------------
-- sample pending order for alice
-- ---------------------------------------------------------------------------
INSERT INTO orders (user_id, restaurant_id, status, total_amount, delivery_address, notes)
SELECT
    u.id,
    r.id,
    'PENDING',
    14.99,
    '22 Pine Lane',
    NULL
FROM users u, restaurants r
WHERE u.username = 'alice' AND r.name = 'Mario''s Pizza House';

INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, unit_price, subtotal)
SELECT
    o.id,
    mi.id,
    mi.name,
    1,
    mi.price,
    mi.price
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN restaurants r ON o.restaurant_id = r.id
JOIN menu_items mi ON mi.restaurant_id = r.id AND mi.name = 'Pepperoni Pizza'
WHERE u.username = 'alice'
  AND r.name = 'Mario''s Pizza House'
  AND o.status = 'PENDING'
ORDER BY o.id DESC
LIMIT 1;
