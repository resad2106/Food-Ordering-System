package com.foodordering.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads JDBC settings and provides database connections.
 */
public final class DatabaseConfig {

    private static final String PROPERTIES_FILE = "db.properties";
    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        String url = requireProperty("db.url");
        String username = requireProperty("db.username");
        String password = PROPERTIES.getProperty("db.password", "");
        return DriverManager.getConnection(url, username, password);
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException(
                        "Missing database configuration file: " + PROPERTIES_FILE
                );
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load database configuration.", exception);
        }

        return properties;
    }

    private static String requireProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required database property is missing: " + key);
        }
        return value.trim();
    }

    public static void validateConfiguration() {
        Objects.requireNonNull(getProperty("db.url"), "db.url must be configured");
        Objects.requireNonNull(getProperty("db.username"), "db.username must be configured");
    }
}
