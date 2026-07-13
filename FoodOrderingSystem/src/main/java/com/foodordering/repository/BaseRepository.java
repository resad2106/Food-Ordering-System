package com.foodordering.repository;

import com.foodordering.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Shared JDBC helpers for repository implementations.
 */
abstract class BaseRepository {

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    protected LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    protected Long getNullableLong(java.sql.ResultSet resultSet, String columnLabel) throws SQLException {
        long value = resultSet.getLong(columnLabel);
        return resultSet.wasNull() ? null : value;
    }
}
