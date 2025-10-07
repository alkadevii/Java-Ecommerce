package org.example.client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.*;
import java.io.*;
import org.json.*;

public class UserApp extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public UserApp() {
        setTitle("Shop - User");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        String[] cols = {"ID", "Name", "Price", "Stock"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadProducts());

        add(refresh, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadProducts();
    }

    private void loadProducts() {
        model.setRowCount(0);
        try {
            URL url = new URL("http://localhost:8080/api/products");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();

            JSONArray arr = new JSONArray(sb.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject p = arr.getJSONObject(i);

                String id = p.getString("id"); // ID as String
                String name = p.isNull("name") ? "N/A" : p.getString("name"); // handle null names
                double price = p.getDouble("price");
                int stock = p.getInt("stock");

                model.addRow(new Object[]{id, name, price, stock});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserApp().setVisible(true));
    }
}
