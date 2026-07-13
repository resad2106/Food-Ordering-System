package com.foodordering.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Shopping cart for a user, scoped to a single restaurant.
 */
public class Cart {

    private Long id;
    private Long userId;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItem> items = new ArrayList<>();

    public Cart() {
    }

    public Cart(Long id, Long userId, Long restaurantId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<CartItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    public void addItem(CartItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Cart cart = (Cart) object;
        return Objects.equals(id, cart.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "Cart{id=%d, userId=%d, restaurantId=%d, itemCount=%d}",
                id, userId, restaurantId, items.size()
        );
    }
}
