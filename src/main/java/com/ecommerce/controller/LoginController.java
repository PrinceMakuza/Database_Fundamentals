package com.ecommerce.controller;

import com.ecommerce.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * LoginController provides a premium, styled login and registration interface.
 * Features: email validation, password confirmation, show/hide password toggle.
 */
public class LoginController extends StackPane {
    private final AuthService authService = new AuthService();
    private final Runnable onLoginSuccess;

    // Email validation regex (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");

    private VBox loginCard;
    private Label title;
    private Label subtitle;
    private TextField nameField;
    private TextField emailField;
    private TextField locationField;
    private PasswordField passwordField;
    private TextField passwordVisible;
    private PasswordField confirmPasswordField;
    private TextField confirmPasswordVisible;
    private Button mainBtn;
    private Label statusLabel;
    private Hyperlink toggleLink;
    private HBox demoBox;

    // Labels for register-mode fields
    private Label nameLabel;
    private Label locationLabel;
    private Label confirmLabel;
    private HBox confirmPasswordBox;

    private boolean isRegisterMode = false;
    private boolean passwordShown = false;
    private boolean confirmPasswordShown = false;

    public LoginController(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        this.setStyle("-fx-background-color: #121212;");

        loginCard = new VBox(18);
        loginCard.setMaxWidth(420);
        loginCard.setPadding(new Insets(35));
        loginCard.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 18, 0, 0, 10);");

        title = new Label("Welcome Back");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");

        subtitle = new Label("Please sign in to continue");
        subtitle.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 14px;");

        VBox form = new VBox(8);

        // --- Name field (register only) ---
        nameLabel = new Label("Name");
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setVisible(false);
        nameLabel.setManaged(false);

        nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setPrefHeight(35);
        nameField.setVisible(false);
        nameField.setManaged(false);

        // --- Email field ---
        Label emailLabel = new Label("Email");
        emailLabel.setTextFill(Color.WHITE);

        emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setPrefHeight(35);

        // --- Location field (register only) ---
        locationLabel = new Label("Location");
        locationLabel.setTextFill(Color.WHITE);
        locationLabel.setVisible(false);
        locationLabel.setManaged(false);

        locationField = new TextField();
        locationField.setPromptText("City / Country");
        locationField.setPrefHeight(35);
        locationField.setVisible(false);
        locationField.setManaged(false);

        // --- Password field with show/hide toggle ---
        Label passwordLabel = new Label("Password");
        passwordLabel.setTextFill(Color.WHITE);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(35);
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        passwordVisible = new TextField();
        passwordVisible.setPromptText("Password");
        passwordVisible.setPrefHeight(35);
        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);
        HBox.setHgrow(passwordVisible, Priority.ALWAYS);

        // Sync password fields
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());

        Button togglePasswordBtn = new Button("\u25CE"); // eye icon
        togglePasswordBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #aaa; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #444; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 4 8;");
        togglePasswordBtn.setPrefHeight(35);
        togglePasswordBtn.setPrefWidth(38);
        togglePasswordBtn.setOnAction(e -> {
            passwordShown = !passwordShown;
            passwordField.setVisible(!passwordShown);
            passwordField.setManaged(!passwordShown);
            passwordVisible.setVisible(passwordShown);
            passwordVisible.setManaged(passwordShown);
            togglePasswordBtn.setText(passwordShown ? "\u2299" : "\u25CE");
        });

        HBox passwordBox = new HBox(5, passwordField, passwordVisible, togglePasswordBtn);
        passwordBox.setAlignment(Pos.CENTER_LEFT);

        // --- Confirm Password field (register only) ---
        confirmLabel = new Label("Confirm Password");
        confirmLabel.setTextFill(Color.WHITE);
        confirmLabel.setVisible(false);
        confirmLabel.setManaged(false);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setPrefHeight(35);
        HBox.setHgrow(confirmPasswordField, Priority.ALWAYS);

        confirmPasswordVisible = new TextField();
        confirmPasswordVisible.setPromptText("Confirm Password");
        confirmPasswordVisible.setPrefHeight(35);
        confirmPasswordVisible.setVisible(false);
        confirmPasswordVisible.setManaged(false);
        HBox.setHgrow(confirmPasswordVisible, Priority.ALWAYS);

        confirmPasswordVisible.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        Button toggleConfirmBtn = new Button("\u25CE"); // eye icon
        toggleConfirmBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #aaa; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #444; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 4 8;");
        toggleConfirmBtn.setPrefHeight(35);
        toggleConfirmBtn.setPrefWidth(38);
        toggleConfirmBtn.setOnAction(e -> {
            confirmPasswordShown = !confirmPasswordShown;
            confirmPasswordField.setVisible(!confirmPasswordShown);
            confirmPasswordField.setManaged(!confirmPasswordShown);
            confirmPasswordVisible.setVisible(confirmPasswordShown);
            confirmPasswordVisible.setManaged(confirmPasswordShown);
            toggleConfirmBtn.setText(confirmPasswordShown ? "\u2299" : "\u25CE");
        });

        confirmPasswordBox = new HBox(5, confirmPasswordField, confirmPasswordVisible, toggleConfirmBtn);
        confirmPasswordBox.setAlignment(Pos.CENTER_LEFT);
        confirmPasswordBox.setVisible(false);
        confirmPasswordBox.setManaged(false);

        // --- Main action button ---
        mainBtn = new Button("Sign In");
        mainBtn.setMaxWidth(Double.MAX_VALUE);
        mainBtn.setPrefHeight(45);
        mainBtn.getStyleClass().add("button-primary");
        mainBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");

        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setTextAlignment(TextAlignment.CENTER);

        toggleLink = new Hyperlink("Don't have an account? Create one");
        toggleLink.setStyle("-fx-text-fill: #3498db; -fx-underline: false;");
        toggleLink.setOnAction(e -> toggleMode());

        mainBtn.setOnAction(e -> handleAction());

        // Demo login shortcuts
        demoBox = new HBox(10);
        demoBox.setAlignment(Pos.CENTER);
        Button adminDemo = new Button("Admin Demo");
        adminDemo.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-size: 11px;");
        adminDemo.setOnAction(e -> { emailField.setText("admin@ecommerce.com"); passwordField.setText("admin123"); });

        Button userDemo = new Button("User Demo");
        userDemo.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-size: 11px;");
        userDemo.setOnAction(e -> { emailField.setText("john@example.com"); passwordField.setText("password123"); });

        demoBox.getChildren().addAll(adminDemo, userDemo);

        form.getChildren().addAll(
            nameLabel, nameField,
            emailLabel, emailField,
            locationLabel, locationField,
            passwordLabel, passwordBox,
            confirmLabel, confirmPasswordBox,
            mainBtn, statusLabel, toggleLink, demoBox
        );

        loginCard.getChildren().addAll(title, subtitle, form);

        // Final layout for perfect centering
        ScrollPane scrollPane = new ScrollPane(loginCard);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scrollPane.setMaxWidth(450);
        scrollPane.setMaxHeight(650);

        StackPane.setAlignment(scrollPane, Pos.CENTER);
        this.getChildren().add(scrollPane);
        this.setPadding(new Insets(20));
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        statusLabel.setText("");
        if (isRegisterMode) {
            title.setText("Create Account");
            subtitle.setText("Join our high-performance shopping system");
            mainBtn.setText("Sign Up");
            toggleLink.setText("Already have an account? Sign In");
            nameField.setVisible(true);
            nameField.setManaged(true);
            nameLabel.setVisible(true);
            nameLabel.setManaged(true);
            confirmLabel.setVisible(true);
            confirmLabel.setManaged(true);
            confirmPasswordBox.setVisible(true);
            confirmPasswordBox.setManaged(true);
            locationLabel.setVisible(true);
            locationLabel.setManaged(true);
            locationField.setVisible(true);
            locationField.setManaged(true);
            demoBox.setVisible(false);
            demoBox.setManaged(false);
        } else {
            title.setText("Welcome Back");
            subtitle.setText("Please sign in to continue");
            mainBtn.setText("Sign In");
            toggleLink.setText("Don't have an account? Create one");
            nameField.setVisible(false);
            nameField.setManaged(false);
            nameLabel.setVisible(false);
            nameLabel.setManaged(false);
            confirmLabel.setVisible(false);
            confirmLabel.setManaged(false);
            confirmPasswordBox.setVisible(false);
            confirmPasswordBox.setManaged(false);
            locationLabel.setVisible(false);
            locationLabel.setManaged(false);
            locationField.setVisible(false);
            locationField.setManaged(false);
            demoBox.setVisible(true);
            demoBox.setManaged(true);
        }
    }

    private void handleAction() {
        String email = emailField.getText().trim().toLowerCase();
        String pass = passwordField.getText();
        statusLabel.setText("");

        if (isRegisterMode) {
            String name = nameField.getText().trim();
            String location = locationField.getText().trim();
            String confirmPass = confirmPasswordField.getText();

            // Name validation
            if (name.isEmpty()) {
                showError("Please enter your full name.");
                return;
            }
            if (name.length() < 2) {
                showError("Name must be at least 2 characters.");
                return;
            }
            if (!NAME_PATTERN.matcher(name).matches()) {
                showError("Name can only contain letters and spaces (no numbers).");
                return;
            }

            // Email validation
            if (email.isEmpty()) {
                showError("Please enter your email address.");
                return;
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                showError("Please enter a valid email address (e.g. user@example.com).");
                return;
            }

            // Location validation
            if (location.isEmpty()) {
                showError("Please enter your location.");
                return;
            }

            // Password validation
            if (pass.isEmpty()) {
                showError("Please enter a password.");
                return;
            }
            if (pass.length() < 6) {
                showError("Password must be at least 6 characters long.");
                return;
            }

            // Confirm password validation
            if (confirmPass.isEmpty()) {
                showError("Please confirm your password.");
                return;
            }
            if (!pass.equals(confirmPass)) {
                showError("Passwords do not match.");
                return;
            }

            try {
                authService.register(name, email, pass, "CUSTOMER", location);
                statusLabel.setText("Registration successful! You can now sign in.");
                statusLabel.setTextFill(Color.web("#38b86c"));
                toggleMode();
            } catch (SQLException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("duplicate")) {
                    showError("An account with this email already exists.");
                } else {
                    showError("Registration failed: " + ex.getMessage());
                }
            }
        } else {
            // Login validation
            if (email.isEmpty()) {
                showError("Please enter your email address.");
                return;
            }
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                showError("Please enter a valid email address.");
                return;
            }
            if (pass.isEmpty()) {
                showError("Please enter your password.");
                return;
            }
            try {
                authService.login(email, pass);
                onLoginSuccess.run();
            } catch (SQLException ex) {
                showError("Database error: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(Color.web("#e74c3c"));
    }
}
