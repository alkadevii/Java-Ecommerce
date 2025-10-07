package org.example.client;

import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class AdminApp extends JFrame {

    private JTextField nameField = new JTextField(20);
    private JTextField priceField = new JTextField(8);
    private JTextField stockField = new JTextField(5);
    private JComboBox<BrandItem> brandBox = new JComboBox<>();

    private JComboBox<String> sortBox = new JComboBox<>(new String[]{"Sort by", "Brand", "Price"});
    private JPanel productGallery = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));

    public AdminApp() {
        setTitle("Admin - Manage Products");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top Panel - Add Product
        JPanel addPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);

        c.gridx = 0; c.gridy = 0; addPanel.add(new JLabel("Name:"), c);
        c.gridx = 1; addPanel.add(nameField, c);

        c.gridx = 0; c.gridy = 1; addPanel.add(new JLabel("Price:"), c);
        c.gridx = 1; addPanel.add(priceField, c);

        c.gridx = 0; c.gridy = 2; addPanel.add(new JLabel("Stock:"), c);
        c.gridx = 1; addPanel.add(stockField, c);

        c.gridx = 0; c.gridy = 3; addPanel.add(new JLabel("Brand:"), c);
        c.gridx = 1; addPanel.add(brandBox, c);

        JButton addBtn = new JButton("Add Product");
        addBtn.addActionListener(e -> addProduct());
        c.gridx = 1; c.gridy = 4; addPanel.add(addBtn, c);

        sortBox.addActionListener(e -> loadProducts());
        c.gridx = 2; c.gridy = 0; c.gridheight = 2;
        addPanel.add(sortBox, c);

        add(addPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(productGallery);
        add(scroll, BorderLayout.CENTER);

        loadBrands();   // populate brand dropdown
        loadProducts();
    }

    private void loadBrands() {
        try {
            URL url = new URL("http://localhost:8080/api/brands");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (var reader = new java.io.InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                java.lang.reflect.Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
                List<Map<String, String>> brands = new Gson().fromJson(reader, listType);

                brandBox.removeAllItems();
                for (Map<String, String> b : brands) {
                    brandBox.addItem(new BrandItem(b.get("id"), b.get("name")));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading brands: " + e.getMessage());
        }
    }

    private void addProduct() {
        String name = nameField.getText().trim();
        String price = priceField.getText().trim();
        String stock = stockField.getText().trim();
        BrandItem selectedBrand = (BrandItem) brandBox.getSelectedItem();

        if (name.isEmpty() || price.isEmpty() || stock.isEmpty() || selectedBrand == null) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }

        try {
            double pr = Double.parseDouble(price);
            int st = Integer.parseInt(stock);
            String brandId = selectedBrand.id;

            URL url = new URL("http://localhost:8080/api/products");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("price", pr);
            data.put("stock", st);
            data.put("brandId", brandId);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = new Gson().toJson(data).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = con.getResponseCode();
            if (code == 200 || code == 201) {
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                nameField.setText(""); priceField.setText(""); stockField.setText("");
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed! HTTP: " + code);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadProducts() {
        try {
            URL url = new URL("http://localhost:8080/api/products");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (var reader = new java.io.InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                java.lang.reflect.Type listType = new TypeToken<List<Product>>() {}.getType();
                List<Product> products = new Gson().fromJson(reader, listType);

                String sort = (String) sortBox.getSelectedItem();
                if ("Price".equals(sort)) products.sort(Comparator.comparingDouble(Product::getPrice));
                else if ("Brand".equals(sort)) products.sort(Comparator.comparing(Product::getBrandId, String.CASE_INSENSITIVE_ORDER));

                productGallery.removeAll();
                for (Product p : products) {
                    JPanel card = new JPanel(new BorderLayout());
                    card.setPreferredSize(new Dimension(150, 120));
                    card.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                    JLabel info = new JLabel("<html><b>" + p.getName() + "</b><br>₹" + p.getPrice() +
                            "<br>Brand ID: " + p.getBrandId() + "</html>", SwingConstants.CENTER);
                    card.add(info, BorderLayout.CENTER);

                    // Delete button
                    JButton deleteBtn = new JButton("Delete");
                    deleteBtn.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + p.getName() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteProduct(p.getId());
                        }
                    });
                    card.add(deleteBtn, BorderLayout.SOUTH);

                    productGallery.add(card);
                }

                productGallery.revalidate();
                productGallery.repaint();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }

    private void deleteProduct(String productId) {
        try {
            URL url = new URL("http://localhost:8080/api/products/" + productId);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");

            int code = con.getResponseCode();
            if (code == 200 || code == 204) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete! HTTP: " + code);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminApp().setVisible(true));
    }

    // Classes
    static class Product {
        private String id;
        private String name;
        private double price;
        private int stock;
        private String brandId;

        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
        public String getBrandId() { return brandId; }
    }

    static class BrandItem {
        String id;
        String name;
        public BrandItem(String id, String name) { this.id = id; this.name = name; }
        @Override
        public String toString() { return name; }
    }

    // Layout to wrap product cards
    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override
        public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int maxWidth = target.getParent() != null ? target.getParent().getWidth() : 800;
                int x = 0, y = 0, rowHeight = 0;
                for (Component c : target.getComponents()) {
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x + d.width > maxWidth) { x = 0; y += rowHeight + getVgap(); rowHeight = 0; }
                    x += d.width + getHgap();
                    rowHeight = Math.max(rowHeight, d.height);
                }
                y += rowHeight + getVgap();
                Insets insets = target.getInsets();
                y += insets.top + insets.bottom;
                return new Dimension(maxWidth, y);
            }
        }
    }
}
