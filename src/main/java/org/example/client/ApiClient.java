package org.example.client;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.example.client.models.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final Gson gson = new Gson();

    // ------------------ PRODUCTS ------------------

    /** Fetch all products from backend */
    public static List<Product> fetchProducts() throws IOException {
        return fetchList("/products", Product[].class);
    }

    /** Fetch all brands from backend */
    public static List<Brand> fetchBrands() throws IOException {
        return fetchList("/brands", Brand[].class);
    }

    /** Generic GET request returning list */
    private static <T> List<T> fetchList(String path, Class<T[]> type) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            return Arrays.asList(gson.fromJson(reader, type));
        }
    }

    // ------------------ CART ------------------
    /** Fetch cart items for a user */
    public static List<CartManager.CartItem> fetchCart(String userId) throws Exception {
        URL url = new URL(BASE_URL + "/cart");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("userId", userId); // set userId as header

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            // Parse JSON object
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

            // Extract the "items" array
            JsonArray itemsArray = obj.getAsJsonArray("items");

            // Convert JSON array to List<CartItem>
            return new Gson().fromJson(itemsArray, new TypeToken<List<CartManager.CartItem>>(){}.getType());
        }
    }




    /** Add product to user cart with quantity */
    public static boolean addToCart(String userId, String productId, int qty) {
        try {
            String endpoint = BASE_URL + "/cart/add?productId=" + productId + "&qty=" + qty;
            URL url = new URL(endpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("userId", userId);

            return con.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }




    /** Clear user cart */
        public static boolean clearCart(String userId) {
            try {
                URL url = new URL(BASE_URL + "/cart/clear?userId=" + URLEncoder.encode(userId, "UTF-8"));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                return con.getResponseCode() == 200;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /** Confirm order for user */
        public static boolean confirmOrder(String userId, List<CartManager.CartItem> items) {
            try {
                URL url = new URL(BASE_URL + "/orders/confirm");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");

                List<Map<String,Object>> orderItems = new ArrayList<>();
                for (CartManager.CartItem item : items) {
                    Map<String,Object> map = new HashMap<>();
                    map.put("productId", item.productId);
                    map.put("quantity", item.quantity);
                    orderItems.add(map);
                }

                Map<String,Object> body = new HashMap<>();
                body.put("userId", userId);
                body.put("items", orderItems);

                String json = gson.toJson(body);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                return con.getResponseCode() == 200;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
}
