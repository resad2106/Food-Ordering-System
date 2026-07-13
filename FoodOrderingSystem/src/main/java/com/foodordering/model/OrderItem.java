package com.foodordering.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable snapshot of a menu item at the time an order was placed.
 */
public class OrderItem {

    private Long id;
    private Long orderId;
    private Long menuItemId;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;

    public OrderItem() {
    }

    public OrderItem(Long id, Long orderId, Long menuItemId, String itemName, int quantity,
                     BigDecimal unitPrice, BigDecimal subtotal, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        OrderItem orderItem = (OrderItem) object;
        return Objects.equals(id, orderItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "OrderItem{id=%d, itemName='%s', quantity=%d, unitPrice=%s, subtotal=%s}",
                id, itemName, quantity, unitPrice, subtotal
        );
    }
}
