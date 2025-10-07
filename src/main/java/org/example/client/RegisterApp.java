package org.example.client;

import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import com.google.gson.*;

public class RegisterApp extends JFrame {

    private JTextField usernameField = new JTextField(15);
    private JPasswordField passwordField = new JPasswordField(15);
    private JPasswordField confirmField = new JPasswordField(15);

    public RegisterApp() {
        setTitle("User Registration");
        setSize(350, 220);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);

        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Username:"), c);
        c.gridx = 1; p.add(usernameField, c);

        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Password:"), c);
        c.gridx = 1; p.add(passwordField, c);

        c.gridx = 0; c.gridy = 2; p.add(new JLabel("Confirm:"), c);
        c.gridx = 1; p.add(confirmField, c);

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> handleRegister());
        c.gridx = 1; c.gridy = 3; p.add(registerBtn, c);

        add(p);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields");
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }

        try {
            URL url = new URL("http://localhost:8080/api/auth/register");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            // role = "user" for normal signup
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"user\"}", username, password);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
            }

            int code = con.getResponseCode();
            InputStreamReader reader;
            if (code == 200) {
                reader = new InputStreamReader(con.getInputStream());
                JOptionPane.showMessageDialog(this, "Registration successful!");
                this.dispose(); // close registration window
            } else {
                reader = new InputStreamReader(con.getErrorStream());
                BufferedReader br = new BufferedReader(reader);
                String msg = br.readLine();
                JOptionPane.showMessageDialog(this, "Registration failed: " + msg);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterApp().setVisible(true));
    }
}
