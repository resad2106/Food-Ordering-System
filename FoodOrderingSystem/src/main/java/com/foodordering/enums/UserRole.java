package com.foodordering.enums;

/**
 * Application roles aligned with the {@code users.role} database column.
 */
public enum UserRole {

    CUSTOMER,
    RESTAURANT_OWNER,
    ADMIN;

    public static UserRole fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role must not be blank.");
        }
        return UserRole.valueOf(value.trim().toUpperCase());
    }
}
