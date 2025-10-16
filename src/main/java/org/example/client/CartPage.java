package org.example.client;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CartPage extends JFrame {
    private JPanel itemPanel = new JPanel(new GridLayout(0, 3, 5, 5));
    private JLabel totalLabel = new JLabel("Total: ₹0.00", SwingConstants.RIGHT);
    private String userId; // <-- changed from username

    public CartPage(String userId) { // <-- constructor now takes userId
        this.userId = userId;
        setTitle("🛒 Cart");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(15,20,15,20));
        root.setBackground(Color.WHITE);

        JLabel title = new JLabel("🛒 Shopping Cart", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(title, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(itemPanel);
        root.add(scroll, BorderLayout.CENTER);

        JButton confirmBtn = new JButton("✅ Confirm Order");
        confirmBtn.setBackground(new Color(46,204,113));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.addActionListener(e -> confirmOrder());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(totalLabel, BorderLayout.CENTER);
        bottom.add(confirmBtn, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        add(root);
        refreshCart();
    }

    private void refreshCart() {
        itemPanel.removeAll();
        try {
            List<CartManager.CartItem> items = CartManager.getItems(userId); // <-- use userId

            if (items.isEmpty()) {
                itemPanel.add(new JLabel("Your cart is empty."));
            } else {
                for (CartManager.CartItem item : items) {
                    itemPanel.add(new JLabel(item.name));
                    itemPanel.add(new JLabel("Qty: " + item.quantity, SwingConstants.CENTER));
                    itemPanel.add(new JLabel("₹" + (item.price * item.quantity), SwingConstants.RIGHT));
                }
            }

            totalLabel.setText("Total: ₹" + CartManager.getTotal(userId)); // <-- use userId
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading cart: " + e.getMessage());
        }
        itemPanel.revalidate();
        itemPanel.repaint();
    }

    private void confirmOrder() {
        try {
            if (CartManager.getItems(userId).isEmpty()) { // <-- use userId
                JOptionPane.showMessageDialog(this, "Your cart is empty!");
                return;
            }
            boolean success = ApiClient.confirmOrder(userId, CartManager.getItems(userId)); // <-- use userId
            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Order placed successfully!");
                CartManager.clear(userId); // <-- use userId
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Failed to confirm order.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
