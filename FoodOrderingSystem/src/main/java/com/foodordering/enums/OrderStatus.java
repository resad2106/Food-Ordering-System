package com.foodordering.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Order lifecycle states aligned with the {@code orders.status} database column.
 */
public enum OrderStatus {

    PENDING,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    private static final Set<OrderStatus> CANCELLABLE_STATUSES =
            EnumSet.of(PENDING, CONFIRMED);

    public static OrderStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order status must not be blank.");
        }
        return OrderStatus.valueOf(value.trim().toUpperCase());
    }

    public boolean isCancellable() {
        return CANCELLABLE_STATUSES.contains(this);
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
}
