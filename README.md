# Database Setup

This folder contains the MySQL schema and seed data for the **Food Ordering System**.

## Prerequisites

- MySQL 8.0 or later
- MySQL client (`mysql` CLI) or MySQL Workbench

## Quick Start

From the project root, run:

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

Or inside the MySQL shell:

```sql
SOURCE /path/to/FoodOrderingSystem/database/schema.sql;
SOURCE /path/to/FoodOrderingSystem/database/seed.sql;
```

## Database Name

| Setting   | Value              |
|-----------|--------------------|
| Database  | `food_ordering_db` |
| Charset   | `utf8mb4`          |
| Collation | `utf8mb4_unicode_ci` |

## Tables

| Table         | Description                                      |
|---------------|--------------------------------------------------|
| `users`       | Registered users (customers, owners, admins)   |
| `restaurants` | Restaurant listings                            |
| `menu_items`  | Food items per restaurant                        |
| `cart`        | One active cart per user (single restaurant)     |
| `cart_items`  | Line items in a cart                             |
| `orders`      | Placed orders with status tracking               |
| `order_items` | Order line items with price/name snapshots       |

## Entity Relationships

```
users ──┬──< restaurants (owner_id)
        ├──< cart (user_id, unique)
        └──< orders (user_id)

restaurants ──┬──< menu_items
              ├──< cart (restaurant_id)
              └──< orders (restaurant_id)

cart ──< cart_items ──> menu_items
orders ──< order_items ──> menu_items (nullable after menu deletion)
```

## Demo Credentials

All seed users share the same demo password:

| Username      | Email                         | Role              |
|---------------|-------------------------------|-------------------|
| `admin`       | admin@foodorder.local         | ADMIN             |
| `owner_pizza` | owner.pizza@foodorder.local   | RESTAURANT_OWNER  |
| `owner_sushi` | owner.sushi@foodorder.local   | RESTAURANT_OWNER  |
| `alice`       | alice@example.com             | CUSTOMER          |
| `bob`         | bob@example.com               | CUSTOMER          |

**Password:** `password123`

> The seed file stores a BCrypt hash. The Java application will implement password hashing in the service layer.

## Reset Database

To drop and recreate everything:

```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS food_ordering_db;"
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

## Application Configuration

Connection settings will be defined in `src/main/resources/db.properties` (added in a later stage):

```properties
db.url=jdbc:mysql://localhost:3306/food_ordering_db?useSSL=false&serverTimezone=UTC
db.username=root
db.password=your_password
```

## Design Notes

- **Cart constraint:** Each user has at most one cart, tied to a single restaurant. Adding items from another restaurant requires clearing or switching the cart in the service layer.
- **Order snapshots:** `order_items` stores `item_name`, `unit_price`, and `subtotal` at order time so historical orders remain accurate if menu prices change.
- **Referential integrity:** Orders use `ON DELETE RESTRICT` to preserve audit history. Cart and cart items cascade on delete.
- **Validation:** Check constraints enforce positive prices and quantities at the database level.
