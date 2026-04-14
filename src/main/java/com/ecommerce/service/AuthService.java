package com.ecommerce.service;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.User;
import com.ecommerce.util.UserContext;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

/**
 * AuthService handles authentication and secure password management.
 */
public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    // Constructor for testing
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user. If successful, populates UserContext.
     */
    public void login(String email, String password) throws SQLException {
        User user = userDAO.getUserByEmail(email.toLowerCase());
        if (user == null) {
            throw new IllegalArgumentException("Account not found.");
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password.");
        }
        
        UserContext.setCurrentUserId(user.getUserId());
        UserContext.setCurrentUserName(user.getName());
        UserContext.setCurrentUserEmail(user.getEmail());
        UserContext.setCurrentUserRole(user.getRole());
        UserContext.setCurrentUserLocation(user.getLocation());
    }

    /**
     * Registers a new user with a hashed password.
     */
    public void register(String name, String email, String password, String role, String location) throws SQLException {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setName(name);
        user.setEmail(email.toLowerCase());
        user.setRole(role != null ? role : "CUSTOMER");
        user.setPassword(hashed);
        user.setLocation(location);
        userDAO.addUser(user);
    }

    public void logout() {
        UserContext.clear();
    }

    /**
     * Updates the current user's profile and hashes the new password if provided.
     */
    public void updateProfile(int userId, String name, String email, String location, String plainPassword) throws SQLException {
        String hashed = null;
        String lowerEmail = email.toLowerCase();
        if (plainPassword != null && !plainPassword.trim().isEmpty()) {
            hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        }
        
        userDAO.updateUserProfile(userId, name, lowerEmail, location, hashed);
        
        // Update local context if it's the current user
        if (UserContext.getCurrentUserId() == userId) {
            UserContext.setCurrentUserName(name);
            UserContext.setCurrentUserEmail(lowerEmail);
            UserContext.setCurrentUserLocation(location);
        }
    }
}
