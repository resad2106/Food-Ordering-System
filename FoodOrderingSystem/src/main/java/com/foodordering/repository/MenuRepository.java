package com.foodordering.repository;

import com.foodordering.model.MenuItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuRepository extends BaseRepository {

    private static final String INSERT_MENU_ITEM = """
            INSERT INTO menu_items (restaurant_id, name, description, price, category, is_available)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT id, restaurant_id, name, description, price, category, is_available, created_at, updated_at
            FROM menu_items
            WHERE id = ?
            """;

    private static final String SELECT_BY_RESTAURANT_ID = """
            SELECT id, restaurant_id, name, description, price, category, is_available, created_at, updated_at
            FROM menu_items
            WHERE restaurant_id = ?
            ORDER BY category, name
            """;

    private static final String SELECT_AVAILABLE_BY_RESTAURANT_ID = """
            SELECT id, restaurant_id, name, description, price, category, is_available, created_at, updated_at
            FROM menu_items
            WHERE restaurant_id = ? AND is_available = 1
            ORDER BY category, name
            """;

    private static final String SEARCH_BY_RESTAURANT_AND_NAME = """
            SELECT id, restaurant_id, name, description, price, category, is_available, created_at, updated_at
            FROM menu_items
            WHERE restaurant_id = ? AND is_available = 1 AND name LIKE ?
            ORDER BY name
            """;

    private static final String SEARCH_BY_NAME = """
            SELECT id, restaurant_id, name, description, price, category, is_available, created_at, updated_at
            FROM menu_items
            WHERE is_available = 1 AND name LIKE ?
            ORDER BY name
            """;

    private static final String UPDATE_MENU_ITEM = """
            UPDATE menu_items
            SET restaurant_id = ?, name = ?, description = ?, price = ?, category = ?, is_available = ?
            WHERE id = ?
            """;

    private static final String DELETE_MENU_ITEM = """
            DELETE FROM menu_items WHERE id = ?
            """;

    public MenuItem save(MenuItem menuItem) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_MENU_ITEM, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, menuItem.getRestaurantId());
            statement.setString(2, menuItem.getName());
            statement.setString(3, menuItem.getDescription());
            statement.setBigDecimal(4, menuItem.getPrice());
            statement.setString(5, menuItem.getCategory());
            statement.setBoolean(6, menuItem.isAvailable());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    menuItem.setId(generatedKeys.getLong(1));
                }
            }
        }

        return menuItem;
    }

    public Optional<MenuItem> findById(Long id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public List<MenuItem> findByRestaurantId(Long restaurantId) throws SQLException {
        return queryByRestaurant(SELECT_BY_RESTAURANT_ID, restaurantId);
    }

    public List<MenuItem> findAvailableByRestaurantId(Long restaurantId) throws SQLException {
        return queryByRestaurant(SELECT_AVAILABLE_BY_RESTAURANT_ID, restaurantId);
    }

    public List<MenuItem> searchByRestaurantAndName(Long restaurantId, String keyword) throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_BY_RESTAURANT_AND_NAME)) {

            statement.setLong(1, restaurantId);
            statement.setString(2, "%" + keyword.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    menuItems.add(mapRow(resultSet));
                }
            }
        }

        return menuItems;
    }

    public List<MenuItem> searchByName(String keyword) throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_BY_NAME)) {

            statement.setString(1, "%" + keyword.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    menuItems.add(mapRow(resultSet));
                }
            }
        }

        return menuItems;
    }

    public boolean update(MenuItem menuItem) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_MENU_ITEM)) {

            statement.setLong(1, menuItem.getRestaurantId());
            statement.setString(2, menuItem.getName());
            statement.setString(3, menuItem.getDescription());
            statement.setBigDecimal(4, menuItem.getPrice());
            statement.setString(5, menuItem.getCategory());
            statement.setBoolean(6, menuItem.isAvailable());
            statement.setLong(7, menuItem.getId());

            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(Long id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_MENU_ITEM)) {

            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private List<MenuItem> queryByRestaurant(String sql, Long restaurantId) throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, restaurantId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    menuItems.add(mapRow(resultSet));
                }
            }
        }

        return menuItems;
    }

    private MenuItem mapRow(ResultSet resultSet) throws SQLException {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(resultSet.getLong("id"));
        menuItem.setRestaurantId(resultSet.getLong("restaurant_id"));
        menuItem.setName(resultSet.getString("name"));
        menuItem.setDescription(resultSet.getString("description"));
        menuItem.setPrice(resultSet.getBigDecimal("price", BigDecimal.ZERO));
        menuItem.setCategory(resultSet.getString("category"));
        menuItem.setAvailable(resultSet.getBoolean("is_available"));
        menuItem.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        menuItem.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return menuItem;
    }
}
