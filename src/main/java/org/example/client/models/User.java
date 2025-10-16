package org.example.client.models;

public class User {
    private String username;
    private String role;
    private String userId; // <-- new field

    public User() {}

    public User(String username, String role, String userId) {
        this.username = username;
        this.role = role;
        this.userId = userId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
