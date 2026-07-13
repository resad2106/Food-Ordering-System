package com.foodordering.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Line item within a shopping cart.
 */
public class CartItem {

    private Long id;
    private Long cartId;
    private Long menuItemId;
    private int quantity;
    private BigDecimal unitPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartItem() {
    }

    public CartItem(Long id, Long cartId, Long menuItemId, int quantity, BigDecimal unitPrice,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.cartId = cartId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
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

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        CartItem cartItem = (CartItem) object;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "CartItem{id=%d, menuItemId=%d, quantity=%d, unitPrice=%s, subtotal=%s}",
                id, menuItemId, quantity, unitPrice, getSubtotal()
        );
    }
}
