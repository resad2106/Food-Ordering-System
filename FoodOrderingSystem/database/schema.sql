-- =============================================================================
-- Food Ordering System — Database Schema
-- MySQL 8.x | InnoDB | utf8mb4
-- =============================================================================

CREATE DATABASE IF NOT EXISTS food_ordering_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE food_ordering_db;

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)         NOT NULL,
    email           VARCHAR(255)        NOT NULL,
    password_hash   VARCHAR(255)        NOT NULL,
    role            ENUM('CUSTOMER', 'RESTAURANT_OWNER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- restaurants
-- ---------------------------------------------------------------------------
CREATE TABLE restaurants (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    name            VARCHAR(150)        NOT NULL,
    description     TEXT                NULL,
    address         VARCHAR(255)        NOT NULL,
    phone           VARCHAR(20)         NOT NULL,
    owner_id        BIGINT UNSIGNED     NULL,
    is_active       TINYINT(1)          NOT NULL DEFAULT 1,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_restaurants_name (name),
    INDEX idx_restaurants_owner_id (owner_id),
    INDEX idx_restaurants_is_active (is_active),
    CONSTRAINT fk_restaurants_owner
        FOREIGN KEY (owner_id) REFERENCES users (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- menu_items
-- ---------------------------------------------------------------------------
CREATE TABLE menu_items (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    restaurant_id   BIGINT UNSIGNED     NOT NULL,
    name            VARCHAR(150)        NOT NULL,
    description     TEXT                NULL,
    price           DECIMAL(10, 2)      NOT NULL,
    category        VARCHAR(80)         NOT NULL,
    is_available    TINYINT(1)          NOT NULL DEFAULT 1,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_menu_items_restaurant_id (restaurant_id),
    INDEX idx_menu_items_name (name),
    INDEX idx_menu_items_category (category),
    INDEX idx_menu_items_is_available (is_available),
    CONSTRAINT fk_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_menu_items_price CHECK (price > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- cart (one active cart per user, tied to a single restaurant)
-- ---------------------------------------------------------------------------
CREATE TABLE cart (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED     NOT NULL,
    restaurant_id   BIGINT UNSIGNED     NOT NULL,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_user_id (user_id),
    INDEX idx_cart_restaurant_id (restaurant_id),
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_cart_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- cart_items
-- ---------------------------------------------------------------------------
CREATE TABLE cart_items (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    cart_id         BIGINT UNSIGNED     NOT NULL,
    menu_item_id    BIGINT UNSIGNED     NOT NULL,
    quantity        INT UNSIGNED        NOT NULL DEFAULT 1,
    unit_price      DECIMAL(10, 2)      NOT NULL,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_items_cart_menu (cart_id, menu_item_id),
    INDEX idx_cart_items_menu_item_id (menu_item_id),
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES cart (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_cart_items_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_cart_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_cart_items_unit_price CHECK (unit_price > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- orders
-- ---------------------------------------------------------------------------
CREATE TABLE orders (
    id                  BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    user_id             BIGINT UNSIGNED     NOT NULL,
    restaurant_id       BIGINT UNSIGNED     NOT NULL,
    status              ENUM(
                            'PENDING',
                            'CONFIRMED',
                            'PREPARING',
                            'OUT_FOR_DELIVERY',
                            'DELIVERED',
                            'CANCELLED'
                        )                   NOT NULL DEFAULT 'PENDING',
    total_amount        DECIMAL(10, 2)      NOT NULL,
    delivery_address    VARCHAR(255)        NOT NULL,
    notes               TEXT                NULL,
    created_at          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_restaurant_id (restaurant_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created_at (created_at),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_orders_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_orders_total_amount CHECK (total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- order_items (price/name snapshots preserved at order time)
-- ---------------------------------------------------------------------------
CREATE TABLE order_items (
    id              BIGINT UNSIGNED     NOT NULL AUTO_INCREMENT,
    order_id        BIGINT UNSIGNED     NOT NULL,
    menu_item_id    BIGINT UNSIGNED     NULL,
    item_name       VARCHAR(150)        NOT NULL,
    quantity        INT UNSIGNED        NOT NULL,
    unit_price      DECIMAL(10, 2)      NOT NULL,
    subtotal        DECIMAL(10, 2)      NOT NULL,
    created_at      TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_menu_item_id (menu_item_id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_order_items_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price CHECK (unit_price > 0),
    CONSTRAINT chk_order_items_subtotal CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
