package com.foodordering.model;

import com.foodordering.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Customer order placed with a restaurant.
 */
public class Order {

    private Long id;
    private Long userId;
    private Long restaurantId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(Long id, Long userId, Long restaurantId, OrderStatus status, BigDecimal totalAmount,
                 String deliveryAddress, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.notes = notes;
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<OrderItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    public void addItem(OrderItem item) {
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
        Order order = (Order) object;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "Order{id=%d, userId=%d, restaurantId=%d, status=%s, totalAmount=%s, itemCount=%d}",
                id, userId, restaurantId, status, totalAmount, items.size()
        );
    }
}
