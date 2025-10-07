package org.example.client;

import javax.swing.*;
import java.util.List;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * UserApp - Product gallery with brand lookup and product detail view.
 */
public class UserApp extends JFrame {

    private JComboBox<String> sortBox = new JComboBox<>(new String[]{"Sort by", "Price", "Brand"});
    private JPanel productGallery = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
    private Map<String, String> brandMap = new HashMap<>(); // brandId -> brandName

    public UserApp() {
        setTitle("🛍️ Shop - Browse Products");
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(250, 250, 255));

        // --- Top Panel ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(245, 245, 250));

        JLabel title = new JLabel("🛒 Product Gallery");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(title);

        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(sortBox);

        JButton refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBackground(new Color(70, 130, 180));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> loadProducts());
        topPanel.add(refreshBtn);

        add(topPanel, BorderLayout.NORTH);

        // --- Product Gallery ---
        JScrollPane scroll = new JScrollPane(productGallery);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        sortBox.addActionListener(e -> loadProducts());

        // --- Initial Load ---
        loadBrands();   // fetch brand list first
        loadProducts(); // then products
    }

    /** Load all brands into a map for easy lookup */
    private void loadBrands() {
        try {
            URL url = new URL("http://localhost:8080/api/brands");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                java.lang.reflect.Type listType = new TypeToken<List<Brand>>() {}.getType();
                List<Brand> brands = new Gson().fromJson(reader, listType);
                brandMap.clear();
                for (Brand b : brands) brandMap.put(b.getId(), b.getName());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading brands: " + e.getMessage());
        }
    }

    /** Load products and render gallery */
    private void loadProducts() {
        try {
            URL url = new URL("http://localhost:8080/api/products");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                java.lang.reflect.Type listType = new TypeToken<List<Product>>() {}.getType();
                List<Product> products = new Gson().fromJson(reader, listType);

                String sort = (String) sortBox.getSelectedItem();
                if ("Price".equals(sort)) products.sort(Comparator.comparingDouble(Product::getPrice));
                else if ("Brand".equals(sort)) products.sort(Comparator.comparing(Product::getBrandId, String.CASE_INSENSITIVE_ORDER));

                productGallery.removeAll();
                for (Product p : products) {
                    JPanel card = createProductCard(p);
                    productGallery.add(card);
                }

                productGallery.revalidate();
                productGallery.repaint();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }

    /** Create card UI for a product */
    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new Dimension(180, 180));
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
        card.setBackground(Color.WHITE);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String brandName = brandMap.getOrDefault(p.getBrandId(), "Unknown");

        JLabel nameLabel = new JLabel("<html><center><b>" + p.getName() + "</b></center></html>", SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel priceLabel = new JLabel("₹" + p.getPrice(), SwingConstants.CENTER);
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        priceLabel.setForeground(new Color(0, 128, 64));

        JLabel details = new JLabel("<html><center>Stock: " + p.getStock() +
                "<br>Brand: " + brandName + "</center></html>", SwingConstants.CENTER);
        details.setFont(new Font("SansSerif", Font.PLAIN, 12));

        card.add(nameLabel, BorderLayout.NORTH);
        card.add(priceLabel, BorderLayout.CENTER);
        card.add(details, BorderLayout.SOUTH);

        // Hover + click effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(240, 248, 255));
                card.setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 2, true));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new ProductDetailPage(p, brandName).setVisible(true);
            }
        });

        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserApp().setVisible(true));
    }

    // --- Product class ---
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

    // --- Brand class ---
    static class Brand {
        private String id;
        private String name;
        public String getId() { return id; }
        public String getName() { return name; }
    }

    // --- Product Detail Window ---
    static class ProductDetailPage extends JFrame {
        public ProductDetailPage(Product p, String brandName) {
            setTitle("Product Details - " + p.getName());
            setSize(480, 420);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            panel.setBackground(Color.WHITE);

            JLabel title = new JLabel(p.getName(), SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 20));

            JLabel price = new JLabel("Price: ₹" + p.getPrice(), SwingConstants.CENTER);
            price.setFont(new Font("SansSerif", Font.PLAIN, 16));
            price.setForeground(new Color(0, 128, 64));

            JLabel brand = new JLabel("Brand: " + brandName, SwingConstants.CENTER);
            brand.setFont(new Font("SansSerif", Font.PLAIN, 14));

            JLabel stock = new JLabel("Available Stock: " + p.getStock(), SwingConstants.CENTER);
            stock.setFont(new Font("SansSerif", Font.PLAIN, 14));

            JTextArea desc = new JTextArea(
                    "This product is made with high-quality materials, ensuring durability, comfort, and reliability.\n" +
                            "Ideal for daily use and crafted with precision to meet your expectations.");
            desc.setWrapStyleWord(true);
            desc.setLineWrap(true);
            desc.setEditable(false);
            desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
            desc.setBackground(new Color(248, 248, 248));
            desc.setBorder(BorderFactory.createTitledBorder("Description"));

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton addToCart = new JButton("🛒 Add to Cart");
            addToCart.setBackground(new Color(72, 201, 176));
            addToCart.setForeground(Color.WHITE);
            addToCart.setFocusPainted(false);
            buttonPanel.add(addToCart);

            panel.add(title, BorderLayout.NORTH);

            JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.add(price);
            infoPanel.add(brand);
            infoPanel.add(stock);
            panel.add(infoPanel, BorderLayout.CENTER);

            panel.add(desc, BorderLayout.SOUTH);
            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    // --- WrapLayout reused ---
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
