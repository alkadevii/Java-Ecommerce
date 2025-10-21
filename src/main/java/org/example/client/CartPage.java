package org.example.client;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CartPage extends JFrame {

    private JTable cartTable;
    private JLabel totalLabel = new JLabel("Total: ₹0.00", SwingConstants.RIGHT);
    private String userId;
    private CartTableModel tableModel;

    public CartPage(String userId) {
        this.userId = userId;
        setTitle("🛒 Cart");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Root panel
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        root.setBackground(Color.WHITE);

        // Title
        JLabel title = new JLabel("🛒 Shopping Cart", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        root.add(title, BorderLayout.NORTH);

        // Table model
        tableModel = new CartTableModel();
        cartTable = new JTable(tableModel);
        cartTable.setRowHeight(30);

        // Remove button renderer and editor
        cartTable.getColumn("Remove").setCellRenderer(new ButtonRenderer());
        cartTable.getColumn("Remove").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(cartTable);
        root.add(scroll, BorderLayout.CENTER);

        // Bottom panel
        JButton confirmBtn = new JButton("✅ Confirm Order");
        confirmBtn.setBackground(new Color(46, 204, 113));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.addActionListener(e -> confirmOrder());

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        bottom.add(totalLabel, BorderLayout.CENTER);
        bottom.add(confirmBtn, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        add(root);
        refreshCart();
    }

    // Getter for userId
    public String getUserId() {
        return userId;
    }

    public void refreshCart() {
        try {
            List<CartManager.CartItem> items = CartManager.getItems(userId);
            tableModel.setItems(items);
            totalLabel.setText("Total: ₹" + CartManager.getTotal(userId));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading cart: " + e.getMessage());
        }
    }

    public void removeItem(String productId) {
        try {
            boolean success = ApiClient.removeFromCart(userId, productId);
            if (success) {
                JOptionPane.showMessageDialog(this, "🗑️ Item removed successfully!");
                refreshCart();
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Failed to remove item.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error removing item: " + e.getMessage());
        }
    }

    private void confirmOrder() {
        try {
            if (CartManager.getItems(userId).isEmpty()) {
                JOptionPane.showMessageDialog(this, "Your cart is empty!");
                return;
            }
            boolean success = ApiClient.confirmOrder(userId, CartManager.getItems(userId));
            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Order placed successfully!");
                CartManager.clear(userId);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Failed to confirm order.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // ---------------- Table Model ----------------
    class CartTableModel extends AbstractTableModel {
        private List<CartManager.CartItem> items;
        private final String[] columns = {"Product Name", "Quantity", "Price", "Remove"};

        public void setItems(List<CartManager.CartItem> items) {
            this.items = items;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CartManager.CartItem item = items.get(rowIndex);
            switch (columnIndex) {
                case 0: return item.name;
                case 1: return item.quantity;
                case 2: return "₹" + (item.price * item.quantity);
                case 3: return "Remove";
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 3;
        }
    }

    // ---------------- Button Renderer ----------------
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(231, 76, 60));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // ---------------- Button Editor ----------------
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String productId;
        private boolean clicked;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(new Color(231, 76, 60));
            button.setForeground(Color.WHITE);

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            try {
                productId = CartManager.getItems(getUserId()).get(row).productId;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            button.setText("Remove");
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                removeItem(productId);
            }
            clicked = false;
            return "Remove";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
}
