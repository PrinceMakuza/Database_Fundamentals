package com.ecommerce.service;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUserSuccess() throws SQLException {
        when(userDAO.getUserByEmail(anyString())).thenReturn(null);
        
        authService.register("Test User", "test@example.com", "password123", "CUSTOMER", "Test Location");
        
        verify(userDAO, times(1)).addUser(any(User.class));
    }

    @Test
    void testRegisterUserDuplicateEmail() throws SQLException {
        when(userDAO.getUserByEmail("test@example.com")).thenReturn(new User());
        
        authService.register("Test User", "test@example.com", "password123", "CUSTOMER", "Test Location");
        
        verify(userDAO, times(1)).addUser(any(User.class));
    }

    @Test
    void testLoginSuccess() throws SQLException {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(org.mindrot.jbcrypt.BCrypt.hashpw("password123", org.mindrot.jbcrypt.BCrypt.gensalt()));
        user.setName("Test User");
        user.setRole("CUSTOMER");

        when(userDAO.getUserByEmail("test@example.com")).thenReturn(user);

        assertDoesNotThrow(() -> authService.login("test@example.com", "password123"));
    }

    @Test
    void testLoginFailureNotFound() throws SQLException {
        when(userDAO.getUserByEmail(anyString())).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            authService.login("wrong@example.com", "password"));
        assertEquals("Account not found.", ex.getMessage());
    }

    @Test
    void testLoginFailureWrongPassword() throws SQLException {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(org.mindrot.jbcrypt.BCrypt.hashpw("password123", org.mindrot.jbcrypt.BCrypt.gensalt()));

        when(userDAO.getUserByEmail("test@example.com")).thenReturn(user);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> 
            authService.login("test@example.com", "wrongpass"));
        assertEquals("Incorrect password.", ex.getMessage());
    }
}
