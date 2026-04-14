package com.ecommerce.dao;

import com.ecommerce.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO handles database operations for the Users table.
 */
public class UserDAO {

    /**
     * Retrieves all users from the database.
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY user_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getString("location")
                ));
            }
        }
        return users;
    }

    /**
     * Deletes a user by ID.
     */
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves a user by ID.
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("location")
                    );
                    user.setPassword(rs.getString("password_hash"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Adds a new user to the database.
     */
    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (name, email, password_hash, role, location) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail().toLowerCase());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.setString(5, user.getLocation());
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing user's information.
     */
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE Users SET name = ?, email = ?, role = ?, location = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail().toLowerCase());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getLocation());
            pstmt.setInt(5, user.getUserId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates user profile including password if not null.
     */
    public void updateUserProfile(int userId, String name, String email, String location, String passwordHash) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE Users SET name = ?, email = ?, location = ?");
        if (passwordHash != null) sql.append(", password_hash = ?");
        sql.append(" WHERE user_id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            pstmt.setString(idx++, name);
            pstmt.setString(idx++, email.toLowerCase());
            pstmt.setString(idx++, location);
            if (passwordHash != null) pstmt.setString(idx++, passwordHash);
            pstmt.setInt(idx, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves a user by their email address.
     */
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("location")
                    );
                    user.setPassword(rs.getString("password_hash"));
                    return user;
                }
            }
        }
        return null;
    }
}
