package com.foodordering.repository;

import com.foodordering.enums.UserRole;
import com.foodordering.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository {

    private static final String INSERT_USER = """
            INSERT INTO users (username, email, password_hash, role)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT id, username, email, password_hash, role, created_at, updated_at
            FROM users
            WHERE id = ?
            """;

    private static final String SELECT_BY_USERNAME = """
            SELECT id, username, email, password_hash, role, created_at, updated_at
            FROM users
            WHERE username = ?
            """;

    private static final String SELECT_BY_EMAIL = """
            SELECT id, username, email, password_hash, role, created_at, updated_at
            FROM users
            WHERE email = ?
            """;

    private static final String SELECT_ALL = """
            SELECT id, username, email, password_hash, role, created_at, updated_at
            FROM users
            ORDER BY username
            """;

    private static final String UPDATE_USER = """
            UPDATE users
            SET username = ?, email = ?, password_hash = ?, role = ?
            WHERE id = ?
            """;

    private static final String EXISTS_BY_USERNAME = """
            SELECT 1 FROM users WHERE username = ? LIMIT 1
            """;

    private static final String EXISTS_BY_EMAIL = """
            SELECT 1 FROM users WHERE email = ? LIMIT 1
            """;

    public User save(User user) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
            }
        }

        return user;
    }

    public Optional<User> findById(Long id) throws SQLException {
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

    public Optional<User> findByUsername(String username) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USERNAME)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_EMAIL)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
        }

        return users;
    }

    public boolean update(User user) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USER)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());
            statement.setLong(5, user.getId());

            return statement.executeUpdate() == 1;
        }
    }

    public boolean existsByUsername(String username) throws SQLException {
        return exists(EXISTS_BY_USERNAME, username);
    }

    public boolean existsByEmail(String email) throws SQLException {
        return exists(EXISTS_BY_EMAIL, email);
    }

    private boolean exists(String sql, String value) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setRole(UserRole.fromString(resultSet.getString("role")));
        user.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        user.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return user;
    }
}
