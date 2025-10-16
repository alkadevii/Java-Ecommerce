package org.example.client;

import org.example.client.models.Product;

import javax.swing.*;
import java.awt.*;

public class ProductDetailPage extends JFrame {

    public ProductDetailPage(Product p, String brandName, String userId) { // <-- changed parameter
        setTitle(p.getName());
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel(p.getName(), SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel brand = new JLabel("Brand: " + brandName, SwingConstants.CENTER);
        JLabel price = new JLabel("Price: ₹" + p.getPrice(), SwingConstants.CENTER);
        JLabel stock = new JLabel("Stock: " + p.getStock(), SwingConstants.CENTER);
        JLabel desc = new JLabel("<html><center>High quality product suitable for all your needs.</center></html>", SwingConstants.CENTER);

        JButton addToCart = new JButton("🛒 Add to Cart");
        addToCart.setBackground(new Color(72, 201, 176));
        addToCart.setForeground(Color.WHITE);
        addToCart.addActionListener(e -> {
            try {
                String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity:", "1");
                int qty = 1;
                if (qtyStr != null) {
                    try { qty = Integer.parseInt(qtyStr); } catch(Exception ignored) {}
                }

                // Pass userId instead of username
                CartManager.addToCart(userId, p.getId(), qty);
                JOptionPane.showMessageDialog(this, p.getName() + " added to cart!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding to cart: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JPanel bottom = new JPanel();
        bottom.add(addToCart);

        panel.add(title, BorderLayout.NORTH);
        panel.add(desc, BorderLayout.CENTER);
        panel.add(stock, BorderLayout.WEST);
        panel.add(brand, BorderLayout.EAST);
        panel.add(price, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }
}
