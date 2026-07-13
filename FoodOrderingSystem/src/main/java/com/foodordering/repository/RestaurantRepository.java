package com.foodordering.repository;

import com.foodordering.model.Restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestaurantRepository extends BaseRepository {

    private static final String INSERT_RESTAURANT = """
            INSERT INTO restaurants (name, description, address, phone, owner_id, is_active)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT id, name, description, address, phone, owner_id, is_active, created_at, updated_at
            FROM restaurants
            WHERE id = ?
            """;

    private static final String SELECT_ALL_ACTIVE = """
            SELECT id, name, description, address, phone, owner_id, is_active, created_at, updated_at
            FROM restaurants
            WHERE is_active = 1
            ORDER BY name
            """;

    private static final String SELECT_ALL = """
            SELECT id, name, description, address, phone, owner_id, is_active, created_at, updated_at
            FROM restaurants
            ORDER BY name
            """;

    private static final String SEARCH_BY_NAME = """
            SELECT id, name, description, address, phone, owner_id, is_active, created_at, updated_at
            FROM restaurants
            WHERE is_active = 1 AND name LIKE ?
            ORDER BY name
            """;

    private static final String SELECT_BY_OWNER_ID = """
            SELECT id, name, description, address, phone, owner_id, is_active, created_at, updated_at
            FROM restaurants
            WHERE owner_id = ?
            ORDER BY name
            """;

    private static final String UPDATE_RESTAURANT = """
            UPDATE restaurants
            SET name = ?, description = ?, address = ?, phone = ?, owner_id = ?, is_active = ?
            WHERE id = ?
            """;

    private static final String DELETE_RESTAURANT = """
            DELETE FROM restaurants WHERE id = ?
            """;

    public Restaurant save(Restaurant restaurant) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_RESTAURANT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, restaurant.getName());
            statement.setString(2, restaurant.getDescription());
            statement.setString(3, restaurant.getAddress());
            statement.setString(4, restaurant.getPhone());
            if (restaurant.getOwnerId() == null) {
                statement.setNull(5, java.sql.Types.BIGINT);
            } else {
                statement.setLong(5, restaurant.getOwnerId());
            }
            statement.setBoolean(6, restaurant.isActive());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    restaurant.setId(generatedKeys.getLong(1));
                }
            }
        }

        return restaurant;
    }

    public Optional<Restaurant> findById(Long id) throws SQLException {
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

    public List<Restaurant> findAllActive() throws SQLException {
        return queryList(SELECT_ALL_ACTIVE);
    }

    public List<Restaurant> findAll() throws SQLException {
        return queryList(SELECT_ALL);
    }

    public List<Restaurant> searchByName(String keyword) throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_BY_NAME)) {

            statement.setString(1, "%" + keyword.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    restaurants.add(mapRow(resultSet));
                }
            }
        }

        return restaurants;
    }

    public List<Restaurant> findByOwnerId(Long ownerId) throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_OWNER_ID)) {

            statement.setLong(1, ownerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    restaurants.add(mapRow(resultSet));
                }
            }
        }

        return restaurants;
    }

    public boolean update(Restaurant restaurant) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_RESTAURANT)) {

            statement.setString(1, restaurant.getName());
            statement.setString(2, restaurant.getDescription());
            statement.setString(3, restaurant.getAddress());
            statement.setString(4, restaurant.getPhone());
            if (restaurant.getOwnerId() == null) {
                statement.setNull(5, java.sql.Types.BIGINT);
            } else {
                statement.setLong(5, restaurant.getOwnerId());
            }
            statement.setBoolean(6, restaurant.isActive());
            statement.setLong(7, restaurant.getId());

            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(Long id) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_RESTAURANT)) {

            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private List<Restaurant> queryList(String sql) throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                restaurants.add(mapRow(resultSet));
            }
        }

        return restaurants;
    }

    private Restaurant mapRow(ResultSet resultSet) throws SQLException {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(resultSet.getLong("id"));
        restaurant.setName(resultSet.getString("name"));
        restaurant.setDescription(resultSet.getString("description"));
        restaurant.setAddress(resultSet.getString("address"));
        restaurant.setPhone(resultSet.getString("phone"));
        restaurant.setOwnerId(getNullableLong(resultSet, "owner_id"));
        restaurant.setActive(resultSet.getBoolean("is_active"));
        restaurant.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        restaurant.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return restaurant;
    }
}
