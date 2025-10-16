package org.example.client;

import java.util.List;

public class CartManager {

    // CartItem class
    public static class CartItem {
        public String productId;
        public String name;
        public double price;
        public int quantity;
    }

    /** Add product to backend cart with quantity */
    public static boolean addToCart(String userId, String productId, int quantity) throws Exception {
        return ApiClient.addToCart(userId, productId, quantity);
    }

    /** Get cart items from backend */
    public static List<CartItem> getItems(String userId) throws Exception {
        return ApiClient.fetchCart(userId);
    }

    /** Get total price */
    public static double getTotal(String userId) throws Exception {
        return getItems(userId).stream()
                .mapToDouble(i -> i.price * i.quantity)
                .sum();
    }

    /** Clear backend cart */
    public static void clear(String userId) throws Exception {
        ApiClient.clearCart(userId);
    }
}
