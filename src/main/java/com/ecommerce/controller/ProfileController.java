package com.ecommerce.controller;

import com.ecommerce.service.AuthService;
import com.ecommerce.util.UserContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * ProfileController manages the user's personal information and security settings.
 * Refactored for FXML compatibility and clear separation of concerns.
 */
public class ProfileController {
    private final AuthService authService = new AuthService();
    
    @FXML private Label nameDisplayLabel;
    @FXML private Label emailDisplayLabel;
    @FXML private Label locationDisplayLabel;
    
    @FXML private VBox editControls;
    @FXML private GridPane infoGrid;
    
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField locationField;
    
    @FXML private PasswordField passField;
    @FXML private TextField passVisible;
    @FXML private PasswordField confirmPassField;
    @FXML private TextField confirmPassVisible;
    
    @FXML private Button editBtn;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Label statusLabel;
    
    private boolean passShown = false;
    
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-z0-9+_.-]+@[a-z0-9.-]+\\.[a-z]{2,}$");

    @FXML
    public void initialize() {
        refreshUserInfo();
        
        // Bind bidirectional for password toggling
        if (passField != null) {
            passVisible.textProperty().bindBidirectional(passField.textProperty());
            confirmPassVisible.textProperty().bindBidirectional(confirmPassField.textProperty());
        }
    }

    private void refreshUserInfo() {
        nameDisplayLabel.setText(UserContext.getCurrentUserName());
        emailDisplayLabel.setText(UserContext.getCurrentUserEmail());
        locationDisplayLabel.setText(UserContext.getCurrentUserLocation() != null ? UserContext.getCurrentUserLocation() : "Not set");
    }

    @FXML
    private void togglePassVisibility() {
        passShown = !passShown;
        passField.setVisible(!passShown);
        passField.setManaged(!passShown);
        passVisible.setVisible(passShown);
        passVisible.setManaged(passShown);
        
        confirmPassField.setVisible(!passShown);
        confirmPassField.setManaged(!passShown);
        confirmPassVisible.setVisible(passShown);
        confirmPassVisible.setManaged(passShown);
    }

    @FXML
    private void enterEditMode() {
        nameField.setText(UserContext.getCurrentUserName());
        emailField.setText(UserContext.getCurrentUserEmail());
        locationField.setText(UserContext.getCurrentUserLocation());
        
        infoGrid.setVisible(false);
        infoGrid.setManaged(false);
        editControls.setVisible(true);
        editControls.setManaged(true);
        
        editBtn.setVisible(false);
        editBtn.setManaged(false);
        saveBtn.setVisible(true);
        saveBtn.setManaged(true);
        cancelBtn.setVisible(true);
        cancelBtn.setManaged(true);
        
        statusLabel.setText("");
    }

    @FXML
    private void exitEditMode() {
        infoGrid.setVisible(true);
        infoGrid.setManaged(true);
        editControls.setVisible(false);
        editControls.setManaged(false);
        
        editBtn.setVisible(true);
        editBtn.setManaged(true);
        saveBtn.setVisible(false);
        saveBtn.setManaged(false);
        cancelBtn.setVisible(false);
        cancelBtn.setManaged(false);
        
        passField.clear();
        confirmPassField.clear();
    }

    @FXML
    private void handleUpdate() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String location = locationField.getText().trim();
        String pass = passField.getText();
        String confirm = confirmPassField.getText();
        
        if (!validateInputs(name, email, location, pass, confirm)) return;

        try {
            authService.updateProfile(UserContext.getCurrentUserId(), name, email, location, pass);
            refreshUserInfo();
            exitEditMode();
            showSuccess("Profile updated successfully!");
        } catch (SQLException e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    private boolean validateInputs(String name, String email, String location, String pass, String confirm) {
        if (!NAME_PATTERN.matcher(name).matches()) { showError("Name can only contain letters and spaces."); return false; }
        if (!EMAIL_PATTERN.matcher(email).matches()) { showError("Invalid email address."); return false; }
        if (location.isEmpty()) { showError("Location cannot be empty."); return false; }
        if (!pass.isEmpty()) {
            if (pass.length() < 6) { showError("Password must be 6+ chars."); return false; }
            if (!pass.equals(confirm)) { showError("Passwords do not match."); return false; }
        }
        return true;
    }

    private void showSuccess(String msg) {
        statusLabel.setText("✅ " + msg);
        statusLabel.setTextFill(Color.web("#2ecc71"));
    }

    private void showError(String msg) {
        statusLabel.setText("❌ " + msg);
        statusLabel.setTextFill(Color.web("#e74c3c"));
    }
}
