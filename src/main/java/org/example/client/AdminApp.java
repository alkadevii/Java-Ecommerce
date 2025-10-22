package org.example.client;

import javax.swing.*;
import javax.swing.border.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.example.client.models.User; // Make sure your User class exists

public class AdminApp extends JFrame {

    private User loggedInUser; // Store logged-in user
    private JComboBox<String> sortBox = new JComboBox<>(new String[]{"Sort by", "Brand", "Price"});
    private JPanel productGallery = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));

    // ---------------- Constructors ----------------
    public AdminApp(User user) {
        this.loggedInUser = user;
        initUI();
    }

    public AdminApp() { // No-arg constructor for testing
        initUI();
    }

    // ---------------- Initialize UI ----------------
    private void initUI() {
        setTitle("Inventory Management Dashboard");
        setSize(950, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new GradientHeader("E-Commerce Inventory Management");

        // Top bar for sorting and actions
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));
        topBar.setBackground(Color.WHITE);

        JLabel heading = new JLabel("Product Inventory");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));

        sortBox.addActionListener(e -> loadProducts());
        sortBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton addBtn = createStyledButton("+ Add Product");
        addBtn.setBackground(new Color(80, 120, 255));
        addBtn.addActionListener(e -> showAddProductDialog());

        JButton ordersBtn = createStyledButton("📦 View Orders");
        ordersBtn.setBackground(new Color(52, 152, 219));
        ordersBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> new AdminOrdersPage().setVisible(true)));

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBar.setOpaque(false);
        rightBar.add(sortBox);
        rightBar.add(addBtn);
        rightBar.add(ordersBtn); // added orders button

        topBar.add(heading, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);

        // Scroll area
        JScrollPane scroll = new JScrollPane(productGallery);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        productGallery.setBackground(new Color(245, 247, 255));

        add(header, BorderLayout.NORTH);
        add(topBar, BorderLayout.BEFORE_FIRST_LINE);
        add(scroll, BorderLayout.CENTER);

        loadProducts();
    }

    // ---------------- Add Product Dialog ----------------
    private void showAddProductDialog() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setSize(400, 380);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(10);
        JComboBox<BrandItem> brandBox = new JComboBox<>();

        loadBrands(brandBox);

        c.gridx = 0; c.gridy = 0; dialog.add(new JLabel("Name:"), c);
        c.gridx = 1; dialog.add(nameField, c);

        c.gridx = 0; c.gridy = 1; dialog.add(new JLabel("Price:"), c);
        c.gridx = 1; dialog.add(priceField, c);

        c.gridx = 0; c.gridy = 2; dialog.add(new JLabel("Stock:"), c);
        c.gridx = 1; dialog.add(stockField, c);

        c.gridx = 0; c.gridy = 3; dialog.add(new JLabel("Brand:"), c);
        c.gridx = 1; dialog.add(brandBox, c);

        JButton saveBtn = createStyledButton("Save Product");
        saveBtn.setBackground(new Color(60, 160, 100));
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String price = priceField.getText().trim();
            String stock = stockField.getText().trim();
            BrandItem brand = (BrandItem) brandBox.getSelectedItem();

            if (name.isEmpty() || price.isEmpty() || stock.isEmpty() || brand == null) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double pr = Double.parseDouble(price);
                int st = Integer.parseInt(stock);
                addProduct(name, pr, st, brand.id);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        c.gridx = 1; c.gridy = 4;
        dialog.add(saveBtn, c);

        dialog.setVisible(true);
    }

    // ---------------- Load Brands ----------------
    private void loadBrands(JComboBox<BrandItem> brandBox) {
        try {
            URL url = new URL("http://localhost:8080/api/brands");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (var reader = new java.io.InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                java.lang.reflect.Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
                List<Map<String, String>> brands = new Gson().fromJson(reader, listType);
                for (Map<String, String> b : brands)
                    brandBox.addItem(new BrandItem(b.get("id"), b.get("name")));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading brands: " + e.getMessage());
        }
    }

    // ---------------- API Actions ----------------
    private void addProduct(String name, double price, int stock, String brandId) {
        try {
            URL url = new URL("http://localhost:8080/api/products");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            Map<String, Object> data = Map.of(
                    "name", name,
                    "price", price,
                    "stock", stock,
                    "brandId", brandId
            );

            try (OutputStream os = con.getOutputStream()) {
                os.write(new Gson().toJson(data).getBytes(StandardCharsets.UTF_8));
            }

            int code = con.getResponseCode();
            if (code == 200 || code == 201) {
                JOptionPane.showMessageDialog(this, "✅ Product added successfully!");
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product (HTTP " + code + ")");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding product: " + ex.getMessage());
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
                    JPanel card = new RoundedPanel();
                    card.setLayout(new BorderLayout());
                    card.setPreferredSize(new Dimension(180, 140));

                    JLabel info = new JLabel(
                            "<html><center><b>" + p.getName() + "</b><br>₹" + p.getPrice() +
                                    "<br>Stock: " + p.getStock() +
                                    "<br></center></html>",
                            SwingConstants.CENTER
                    );
                    card.add(info, BorderLayout.CENTER);

                    JButton deleteBtn = createStyledButton("Delete");
                    deleteBtn.setBackground(new Color(230, 70, 70));
                    deleteBtn.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + p.getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) deleteProduct(p.getId());
                    });
                    card.add(deleteBtn, BorderLayout.SOUTH);

                    card.addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) { card.setBackground(new Color(240, 245, 255)); }
                        public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
                    });

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
                JOptionPane.showMessageDialog(this, "🗑️ Product deleted successfully!");
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete (HTTP " + code + ")");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage());
        }
    }

    // ---------------- UI Helpers ----------------
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(btn.getBackground().darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(btn.getBackground().brighter()); }
        });
        return btn;
    }

    static class RoundedPanel extends JPanel {
        RoundedPanel() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(10, 10, 10, 10)));
        }
    }

    static class GradientHeader extends JPanel {
        private final String title;
        GradientHeader(String title) { this.title = title; setPreferredSize(new Dimension(100, 60)); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, new Color(90, 130, 255), getWidth(), getHeight(), new Color(130, 160, 255));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g2.setColor(Color.WHITE);
            g2.drawString(title, 20, 35);
        }
    }

    // ---------------- Main ----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminApp().setVisible(true));
    }

    // ---------------- Models ----------------
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
        String id, name;
        BrandItem(String id, String name) { this.id = id; this.name = name; }
        public String toString() { return name; }
    }

    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        public Dimension preferredLayoutSize(Container target) { return layoutSize(target); }
        private Dimension layoutSize(Container target) {
            synchronized (target.getTreeLock()) {
                int maxWidth = target.getParent() != null ? target.getParent().getWidth() : 800;
                int x = 0, y = 0, rowHeight = 0;
                for (Component c : target.getComponents()) {
                    Dimension d = c.getPreferredSize();
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
