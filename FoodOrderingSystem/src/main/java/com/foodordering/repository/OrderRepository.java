package com.foodordering.repository;

import com.foodordering.enums.OrderStatus;
import com.foodordering.model.Order;
import com.foodordering.model.OrderItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderRepository extends BaseRepository {

    private static final String INSERT_ORDER = """
            INSERT INTO orders (user_id, restaurant_id, status, total_amount, delivery_address, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_ORDER_ITEM = """
            INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, unit_price, subtotal)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT id, user_id, restaurant_id, status, total_amount, delivery_address, notes, created_at, updated_at
            FROM orders
            WHERE id = ?
            """;

    private static final String SELECT_BY_USER_ID = """
            SELECT id, user_id, restaurant_id, status, total_amount, delivery_address, notes, created_at, updated_at
            FROM orders
            WHERE user_id = ?
            ORDER BY created_at DESC
            """;

    private static final String SELECT_LATEST_BY_USER_ID = """
            SELECT id, user_id, restaurant_id, status, total_amount, delivery_address, notes, created_at, updated_at
            FROM orders
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT 1
            """;

    private static final String SELECT_ORDER_ITEMS = """
            SELECT id, order_id, menu_item_id, item_name, quantity, unit_price, subtotal, created_at
            FROM order_items
            WHERE order_id = ?
            ORDER BY id
            """;

    private static final String UPDATE_STATUS = """
            UPDATE orders SET status = ? WHERE id = ?
            """;

    public Order save(Order order, List<OrderItem> items) throws SQLException {
        Connection connection = getConnection();

        try {
            connection.setAutoCommit(false);
            insertOrder(connection, order);
            insertOrderItems(connection, order.getId(), items);
            connection.commit();
            order.setItems(items);
            return order;
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    public Optional<Order> findById(Long id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Order order = mapOrderRow(resultSet);
                    order.setItems(findItemsByOrderId(order.getId()));
                    return Optional.of(order);
                }
            }
        }

        return Optional.empty();
    }

    public List<Order> findByUserId(Long userId) throws SQLException {
        List<Order> orders = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(mapOrderRow(resultSet));
                }
            }
        }

        attachItemsToOrders(orders);
        return orders;
    }

    public Optional<Order> findLatestByUserId(Long userId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_LATEST_BY_USER_ID)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Order order = mapOrderRow(resultSet);
                    order.setItems(findItemsByOrderId(order.getId()));
                    return Optional.of(order);
                }
            }
        }

        return Optional.empty();
    }

    public boolean updateStatus(Long orderId, OrderStatus status) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS)) {

            statement.setString(1, status.name());
            statement.setLong(2, orderId);
            return statement.executeUpdate() == 1;
        }
    }

    public List<OrderItem> findItemsByOrderId(Long orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ORDER_ITEMS)) {

            statement.setLong(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapOrderItemRow(resultSet));
                }
            }
        }

        return items;
    }

    private void insertOrder(Connection connection, Order order) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_ORDER, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, order.getUserId());
            statement.setLong(2, order.getRestaurantId());
            statement.setString(3, order.getStatus().name());
            statement.setBigDecimal(4, order.getTotalAmount());
            statement.setString(5, order.getDeliveryAddress());
            statement.setString(6, order.getNotes());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    private void insertOrderItems(Connection connection, Long orderId, List<OrderItem> items) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ORDER_ITEM)) {
            for (OrderItem item : items) {
                statement.setLong(1, orderId);
                if (item.getMenuItemId() == null) {
                    statement.setNull(2, java.sql.Types.BIGINT);
                } else {
                    statement.setLong(2, item.getMenuItemId());
                }
                statement.setString(3, item.getItemName());
                statement.setInt(4, item.getQuantity());
                statement.setBigDecimal(5, item.getUnitPrice());
                statement.setBigDecimal(6, item.getSubtotal());
                statement.addBatch();
                item.setOrderId(orderId);
            }

            statement.executeBatch();
        }
    }

    private void attachItemsToOrders(List<Order> orders) throws SQLException {
        Map<Long, List<OrderItem>> itemsByOrderId = new HashMap<>();

        for (Order order : orders) {
            itemsByOrderId.put(order.getId(), findItemsByOrderId(order.getId()));
        }

        for (Order order : orders) {
            order.setItems(itemsByOrderId.getOrDefault(order.getId(), new ArrayList<>()));
        }
    }

    private Order mapOrderRow(ResultSet resultSet) throws SQLException {
        Order order = new Order();
        order.setId(resultSet.getLong("id"));
        order.setUserId(resultSet.getLong("user_id"));
        order.setRestaurantId(resultSet.getLong("restaurant_id"));
        order.setStatus(OrderStatus.fromString(resultSet.getString("status")));
        order.setTotalAmount(resultSet.getBigDecimal("total_amount", BigDecimal.ZERO));
        order.setDeliveryAddress(resultSet.getString("delivery_address"));
        order.setNotes(resultSet.getString("notes"));
        order.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        order.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return order;
    }

    private OrderItem mapOrderItemRow(ResultSet resultSet) throws SQLException {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(resultSet.getLong("id"));
        orderItem.setOrderId(resultSet.getLong("order_id"));
        orderItem.setMenuItemId(getNullableLong(resultSet, "menu_item_id"));
        orderItem.setItemName(resultSet.getString("item_name"));
        orderItem.setQuantity(resultSet.getInt("quantity"));
        orderItem.setUnitPrice(resultSet.getBigDecimal("unit_price", BigDecimal.ZERO));
        orderItem.setSubtotal(resultSet.getBigDecimal("subtotal", BigDecimal.ZERO));
        orderItem.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        return orderItem;
    }
}
