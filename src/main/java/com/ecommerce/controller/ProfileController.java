package com.ecommerce.controller;

import com.ecommerce.service.AuthService;
import com.ecommerce.util.UserContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * ProfileController allows users to manage their personal information.
 */
public class ProfileController extends VBox {
    private final AuthService authService = new AuthService();
    
    private final TextField nameField, emailField, locationField, passVisible, confirmPassVisible;
    private final PasswordField passField, confirmPassField;
    private final Label nameLabel, emailLabel, locationLabel, statusLabel;
    private final Button editBtn, saveBtn, cancelBtn;
    private final VBox editControls;
    private final HBox passBox, confirmBox;
    
    private boolean isEditMode = false;
    private boolean passShown = false;
    
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-z0-9+_.-]+@[a-z0-9.-]+\\.[a-z]{2,}$");

    public ProfileController() {
        this.setSpacing(25);
        this.setPadding(new Insets(40));
        this.setAlignment(Pos.TOP_CENTER);
        this.getStyleClass().add("main-content");

        VBox card = new VBox(20);
        card.setMaxWidth(500);
        card.setPadding(new Insets(35));
        card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label title = new Label("👤 My Profile");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label subtitle = new Label("Manage your personal information and security");
        subtitle.setStyle("-fx-text-fill: #b0b0b0;");

        // --- View Mode Displays ---
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20); infoGrid.setVgap(15);
        
        nameLabel = createInfoLabel(UserContext.getCurrentUserName());
        emailLabel = createInfoLabel(UserContext.getCurrentUserEmail());
        locationLabel = createInfoLabel(UserContext.getCurrentUserLocation() != null ? UserContext.getCurrentUserLocation() : "Not set");

        addInfoRow(infoGrid, "Full Name", nameLabel, 0);
        addInfoRow(infoGrid, "Email Address", emailLabel, 1);
        addInfoRow(infoGrid, "Location", locationLabel, 2);

        // --- Edit Mode Controls ---
        editControls = new VBox(15);
        editControls.setVisible(false);
        editControls.setManaged(false);

        nameField = createEditField(UserContext.getCurrentUserName());
        emailField = createEditField(UserContext.getCurrentUserEmail());
        locationField = createEditField(UserContext.getCurrentUserLocation());
        
        passField = new PasswordField(); passField.setPromptText("New Password"); passField.setPrefHeight(40); HBox.setHgrow(passField, Priority.ALWAYS);
        passVisible = new TextField(); passVisible.setPromptText("New Password"); passVisible.setPrefHeight(40); passVisible.setVisible(false); passVisible.setManaged(false); HBox.setHgrow(passVisible, Priority.ALWAYS);
        passVisible.textProperty().bindBidirectional(passField.textProperty());
        
        Button togglePass = new Button("\u25CE");
        togglePass.getStyleClass().add("button-secondary");
        togglePass.setOnAction(e -> togglePassVisibility());
        passBox = new HBox(5, passField, passVisible, togglePass);

        confirmPassField = new PasswordField(); confirmPassField.setPromptText("Confirm New Password"); confirmPassField.setPrefHeight(40); HBox.setHgrow(confirmPassField, Priority.ALWAYS);
        confirmPassVisible = new TextField(); confirmPassVisible.setPromptText("Confirm New Password"); confirmPassVisible.setPrefHeight(40); confirmPassVisible.setVisible(false); confirmPassVisible.setManaged(false); HBox.setHgrow(confirmPassVisible, Priority.ALWAYS);
        confirmPassVisible.textProperty().bindBidirectional(confirmPassField.textProperty());
        
        Button toggleConfirm = new Button("\u25CE");
        toggleConfirm.getStyleClass().add("button-secondary");
        toggleConfirm.setOnAction(e -> togglePassVisibility());
        confirmBox = new HBox(5, confirmPassField, confirmPassVisible, toggleConfirm);

        editControls.getChildren().addAll(
            createLabel("Full Name"), nameField,
            createLabel("Email Address"), emailField,
            createLabel("Location"), locationField,
            createLabel("Change Password (optional)"), passBox,
            createLabel("Confirm New Password"), confirmBox
        );

        // Buttons
        editBtn = new Button("Edit Profile");
        editBtn.getStyleClass().add("button-primary");
        editBtn.setPrefWidth(150);
        editBtn.setOnAction(e -> enterEditMode());

        saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("button-success");
        saveBtn.setVisible(false); saveBtn.setManaged(false);
        saveBtn.setOnAction(e -> handleUpdate());

        cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-danger");
        cancelBtn.setVisible(false); cancelBtn.setManaged(false);
        cancelBtn.setOnAction(e -> exitEditMode());

        HBox buttonBar = new HBox(15, editBtn, saveBtn, cancelBtn);
        buttonBar.setAlignment(Pos.CENTER);

        statusLabel = new Label();
        statusLabel.setWrapText(true);

        card.getChildren().addAll(title, subtitle, new Separator() {{ setStyle("-fx-background-color: #333;"); }}, infoGrid, editControls, buttonBar, statusLabel);
        
        // Wrap card in a StackPane then ScrollPane to ensure perfect centering and scrolling
        StackPane centeredWrapper = new StackPane(card);
        centeredWrapper.setPadding(new Insets(20));
        centeredWrapper.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(centeredWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        this.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void addInfoRow(GridPane grid, String label, Label value, int row) {
        Label l = new Label(label + ":");
        l.setStyle("-fx-text-fill: #b0b0b0; -fx-font-weight: bold;");
        grid.add(l, 0, row);
        grid.add(value, 1, row);
    }

    private Label createInfoLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        return l;
    }

    private Label createLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private TextField createEditField(String value) {
        TextField f = new TextField(value);
        f.setPrefHeight(40);
        f.getStyleClass().add("form-field");
        return f;
    }

    private void togglePassVisibility() {
        passShown = !passShown;
        passField.setVisible(!passShown); passField.setManaged(!passShown);
        passVisible.setVisible(passShown); passVisible.setManaged(passShown);
        confirmPassField.setVisible(!passShown); confirmPassField.setManaged(!passShown);
        confirmPassVisible.setVisible(passShown); confirmPassVisible.setManaged(passShown);
    }

    private void enterEditMode() {
        isEditMode = true;
        nameField.setText(UserContext.getCurrentUserName());
        emailField.setText(UserContext.getCurrentUserEmail());
        locationField.setText(UserContext.getCurrentUserLocation());
        
        editBtn.setVisible(false); editBtn.setManaged(false);
        saveBtn.setVisible(true); saveBtn.setManaged(true);
        cancelBtn.setVisible(true); cancelBtn.setManaged(true);
        editControls.setVisible(true); editControls.setManaged(true);
        statusLabel.setText("");
    }

    private void exitEditMode() {
        isEditMode = false;
        editBtn.setVisible(true); editBtn.setManaged(true);
        saveBtn.setVisible(false); saveBtn.setManaged(false);
        cancelBtn.setVisible(false); cancelBtn.setManaged(false);
        editControls.setVisible(false); editControls.setManaged(false);
        passField.clear(); confirmPassField.clear();
    }

    private void handleUpdate() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String location = locationField.getText().trim();
        String pass = passField.getText();
        String confirm = confirmPassField.getText();
        
        if (!NAME_PATTERN.matcher(name).matches()) { showError("Name can only contain letters and spaces."); return; }
        if (!EMAIL_PATTERN.matcher(email).matches()) { showError("Invalid email address."); return; }
        if (location.isEmpty()) { showError("Location cannot be empty."); return; }
        if (!pass.isEmpty()) {
            if (pass.length() < 6) { showError("Password must be 6+ chars."); return; }
            if (!pass.equals(confirm)) { showError("Passwords do not match."); return; }
        }

        try {
            authService.updateProfile(UserContext.getCurrentUserId(), name, email, location, pass);
            nameLabel.setText(name);
            emailLabel.setText(email);
            locationLabel.setText(location);
            exitEditMode();
            statusLabel.setText("✅ Profile updated successfully!");
            statusLabel.setTextFill(Color.web("#2ecc71"));
        } catch (SQLException e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        statusLabel.setText("❌ " + msg);
        statusLabel.setTextFill(Color.web("#e74c3c"));
    }
}
