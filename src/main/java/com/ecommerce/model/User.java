package com.ecommerce.model;

/**
 * User model representing a system user with RBAC.
 */
public class User {
    private int userId;
    private String name;
    private String email;
    private String role; // ADMIN or CUSTOMER
    private String password;
    private String location;

    public User() {}

    public User(int userId, String name, String email, String role, String location) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.location = location;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
