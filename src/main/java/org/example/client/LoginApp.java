package org.example.client;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import com.google.gson.*;

public class LoginApp extends JFrame {

    private JTextField loginUsername = new JTextField(15);
    private JPasswordField loginPassword = new JPasswordField(15);
    private JTextField regUsername = new JTextField(15);
    private JPasswordField regPassword = new JPasswordField(15);
    private JPasswordField regConfirm = new JPasswordField(15);

    public LoginApp() {
        setTitle("E-Commerce Portal");
        setSize(420, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Apply a flat, clean look
        UIManager.put("TabbedPane.selected", new Color(80, 120, 255));
        UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
        UIManager.put("TabbedPane.borderHightlightColor", Color.WHITE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("Register", createRegisterPanel());

        JPanel bgPanel = new GradientPanel();
        bgPanel.setLayout(new BorderLayout());
        bgPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        bgPanel.add(tabs, BorderLayout.CENTER);

        add(bgPanel);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new RoundedPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Login to Your Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(60, 60, 60));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(title, c);

        c.gridwidth = 1; c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Username:"), c);
        c.gridx = 1; panel.add(loginUsername, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Password:"), c);
        c.gridx = 1; panel.add(loginPassword, c);

        JButton loginBtn = createStyledButton("Login");
        loginBtn.addActionListener(e -> handleLogin());
        c.gridx = 1; c.gridy = 3;
        c.anchor = GridBagConstraints.EAST;
        panel.add(loginBtn, c);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new RoundedPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create New Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(60, 60, 60));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(title, c);

        c.gridwidth = 1; c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Username:"), c);
        c.gridx = 1; panel.add(regUsername, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Password:"), c);
        c.gridx = 1; panel.add(regPassword, c);

        c.gridx = 0; c.gridy = 3; panel.add(new JLabel("Confirm:"), c);
        c.gridx = 1; panel.add(regConfirm, c);

        JButton regBtn = createStyledButton("Register");
        regBtn.addActionListener(e -> handleRegister());
        c.gridx = 1; c.gridy = 4;
        c.anchor = GridBagConstraints.EAST;
        panel.add(regBtn, c);

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(80, 120, 255));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(60, 100, 240));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(80, 120, 255));
            }
        });
        return btn;
    }

    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/api/auth/login");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
            }

            int code = con.getResponseCode();
            if (code == 200) {
                InputStreamReader reader = new InputStreamReader(con.getInputStream());
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                String role = obj.get("role").getAsString();

                JOptionPane.showMessageDialog(this, "Welcome " + role + "!");
                dispose();

                SwingUtilities.invokeLater(() -> {
                    if (role.equalsIgnoreCase("admin")) {
                        new AdminApp().setVisible(true);
                    } else {
                        new UserApp().setVisible(true);
                    }
                });
            } else {
                BufferedReader err = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                JOptionPane.showMessageDialog(this, "Login failed: " + err.readLine(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String username = regUsername.getText().trim();
        String password = new String(regPassword.getPassword());
        String confirm = new String(regConfirm.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/api/auth/register");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"user\"}", username, password);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
            }

            int code = con.getResponseCode();
            if (code == 200) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                regUsername.setText("");
                regPassword.setText("");
                regConfirm.setText("");
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                JOptionPane.showMessageDialog(this, "Registration failed: " + br.readLine(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginApp().setVisible(true));
    }

    // ============ Utility Inner Classes ============

    // Gradient background
    static class GradientPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, new Color(240, 245, 255), getWidth(), getHeight(), new Color(200, 220, 255));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Rounded white panel with soft shadow effect
    static class RoundedPanel extends JPanel {
        RoundedPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(15, 15, 15, 15));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(180, 180, 180, 80));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            super.paintComponent(g);
        }
    }
}

