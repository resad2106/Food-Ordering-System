package com.foodordering.repository;

import com.foodordering.model.Cart;
import com.foodordering.model.CartItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartRepository extends BaseRepository {

    private static final String INSERT_CART = """
            INSERT INTO cart (user_id, restaurant_id)
            VALUES (?, ?)
            """;

    private static final String SELECT_CART_BY_USER_ID = """
            SELECT id, user_id, restaurant_id, created_at, updated_at
            FROM cart
            WHERE user_id = ?
            """;

    private static final String UPDATE_CART_RESTAURANT = """
            UPDATE cart SET restaurant_id = ? WHERE id = ?
            """;

    private static final String DELETE_CART_BY_ID = """
            DELETE FROM cart WHERE id = ?
            """;

    private static final String DELETE_CART_BY_USER_ID = """
            DELETE FROM cart WHERE user_id = ?
            """;

    private static final String SELECT_CART_ITEMS = """
            SELECT id, cart_id, menu_item_id, quantity, unit_price, created_at, updated_at
            FROM cart_items
            WHERE cart_id = ?
            ORDER BY id
            """;

    private static final String INSERT_CART_ITEM = """
            INSERT INTO cart_items (cart_id, menu_item_id, quantity, unit_price)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_CART_ITEM_QUANTITY = """
            UPDATE cart_items SET quantity = ? WHERE id = ?
            """;

    private static final String DELETE_CART_ITEM = """
            DELETE FROM cart_items WHERE id = ?
            """;

    private static final String DELETE_ALL_CART_ITEMS = """
            DELETE FROM cart_items WHERE cart_id = ?
            """;

    private static final String SELECT_CART_ITEM_BY_MENU = """
            SELECT id, cart_id, menu_item_id, quantity, unit_price, created_at, updated_at
            FROM cart_items
            WHERE cart_id = ? AND menu_item_id = ?
            """;

    public Cart save(Cart cart) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_CART, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, cart.getUserId());
            statement.setLong(2, cart.getRestaurantId());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cart.setId(generatedKeys.getLong(1));
                }
            }
        }

        return cart;
    }

    public Optional<Cart> findByUserId(Long userId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CART_BY_USER_ID)) {

            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Cart cart = mapCartRow(resultSet);
                    cart.setItems(findItemsByCartId(cart.getId()));
                    return Optional.of(cart);
                }
            }
        }

        return Optional.empty();
    }

    public List<CartItem> findItemsByCartId(Long cartId) throws SQLException {
        List<CartItem> items = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CART_ITEMS)) {

            statement.setLong(1, cartId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapCartItemRow(resultSet));
                }
            }
        }

        return items;
    }

    public CartItem addItem(Long cartId, Long menuItemId, int quantity, BigDecimal unitPrice) throws SQLException {
        Optional<CartItem> existingItem = findCartItemByMenuItemId(cartId, menuItemId);
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            updateItemQuantity(cartItem.getId(), cartItem.getQuantity());
            return cartItem;
        }

        CartItem cartItem = new CartItem();
        cartItem.setCartId(cartId);
        cartItem.setMenuItemId(menuItemId);
        cartItem.setQuantity(quantity);
        cartItem.setUnitPrice(unitPrice);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_CART_ITEM, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, cartId);
            statement.setLong(2, menuItemId);
            statement.setInt(3, quantity);
            statement.setBigDecimal(4, unitPrice);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cartItem.setId(generatedKeys.getLong(1));
                }
            }
        }

        return cartItem;
    }

    public boolean updateItemQuantity(Long cartItemId, int quantity) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_CART_ITEM_QUANTITY)) {

            statement.setInt(1, quantity);
            statement.setLong(2, cartItemId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean removeItem(Long cartItemId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_CART_ITEM)) {

            statement.setLong(1, cartItemId);
            return statement.executeUpdate() == 1;
        }
    }

    public void clearItems(Long cartId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_ALL_CART_ITEMS)) {

            statement.setLong(1, cartId);
            statement.executeUpdate();
        }
    }

    public void clearCartForUser(Long userId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_CART_BY_USER_ID)) {

            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    public boolean updateRestaurant(Long cartId, Long restaurantId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_CART_RESTAURANT)) {

            statement.setLong(1, restaurantId);
            statement.setLong(2, cartId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(Long cartId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_CART_BY_ID)) {

            statement.setLong(1, cartId);
            return statement.executeUpdate() == 1;
        }
    }

    public Optional<CartItem> findCartItemByMenuItemId(Long cartId, Long menuItemId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CART_ITEM_BY_MENU)) {

            statement.setLong(1, cartId);
            statement.setLong(2, menuItemId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCartItemRow(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    private Cart mapCartRow(ResultSet resultSet) throws SQLException {
        Cart cart = new Cart();
        cart.setId(resultSet.getLong("id"));
        cart.setUserId(resultSet.getLong("user_id"));
        cart.setRestaurantId(resultSet.getLong("restaurant_id"));
        cart.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        cart.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return cart;
    }

    private CartItem mapCartItemRow(ResultSet resultSet) throws SQLException {
        CartItem cartItem = new CartItem();
        cartItem.setId(resultSet.getLong("id"));
        cartItem.setCartId(resultSet.getLong("cart_id"));
        cartItem.setMenuItemId(resultSet.getLong("menu_item_id"));
        cartItem.setQuantity(resultSet.getInt("quantity"));
        cartItem.setUnitPrice(resultSet.getBigDecimal("unit_price", BigDecimal.ZERO));
        cartItem.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        cartItem.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
        return cartItem;
    }
}
