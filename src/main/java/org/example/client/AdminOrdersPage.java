package org.example.client;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;

public class AdminOrdersPage extends JFrame {

    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private List<Map<String, Object>> orders; // store orders for item view

    public AdminOrdersPage() {
        setTitle("📦 Customer Orders");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 250));

        // Header
        JLabel header = new JLabel("📦 Customer Orders", SwingConstants.LEFT);
        header.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(header, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(
                new Object[]{"Order ID", "User ID", "Date", "Total (₹)", "Status"}, 0
        ) {
            public boolean isCellEditable(int row, int column) {
                return false; // non-editable
            }
        };
        ordersTable = new JTable(tableModel);
        ordersTable.setFillsViewportHeight(true);
        ordersTable.setRowHeight(28);
        ordersTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        ordersTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        ordersTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Row striping
        ordersTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 245, 250));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom refresh button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);
        JButton refreshBtn = new JButton("🔄 Refresh Orders");
        refreshBtn.setBackground(new Color(52, 152, 219));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        refreshBtn.addActionListener(e -> loadOrders());
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        // Row click listener to show order items
        ordersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = ordersTable.getSelectedRow();
                if (row >= 0 && orders != null) {
                    Map<String, Object> order = orders.get(row);
                    List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");

                    StringBuilder sb = new StringBuilder("<html><body style='width:300px;'>");
                    for (Map<String, Object> item : items) {
                        sb.append("• <b>").append(item.get("name")).append("</b>")
                                .append(" - Qty: ").append(item.get("quantity"))
                                .append(", ₹").append(item.get("price")).append("<br>");
                    }
                    sb.append("</body></html>");

                    JLabel label = new JLabel(sb.toString());
                    label.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    JOptionPane.showMessageDialog(null, label,
                            "Items in Order " + order.get("id"), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        loadOrders();
    }

    private void loadOrders() {
        tableModel.setRowCount(0);

        try {
            URL url = new URL("http://localhost:8080/api/orders/all"); // API returns all orders for admin
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                JOptionPane.showMessageDialog(this, "Failed to fetch orders. Code: " + responseCode);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();

            com.google.gson.reflect.TypeToken<List<Map<String, Object>>> token =
                    new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {};
            java.lang.reflect.Type listType = token.getType();

            Gson gson = new Gson();
            orders = gson.fromJson(response.toString(), listType);

            for (Map<String, Object> order : orders) {
                String orderId = (String) order.get("id");
                String userId = (String) order.get("userId");
                String date = order.get("orderDate").toString();
                double total = ((Number) order.get("totalAmount")).doubleValue();
                String status = (String) order.get("status");
                tableModel.addRow(new Object[]{orderId, userId, date, total, status});
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
    }

    // Quick test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminOrdersPage().setVisible(true));
    }
}
