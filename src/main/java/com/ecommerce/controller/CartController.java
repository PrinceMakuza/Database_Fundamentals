package com.ecommerce.controller;

import com.ecommerce.model.CartItem;
import com.ecommerce.service.CartService;
import com.ecommerce.util.UserContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import java.sql.SQLException;
import java.util.List;

/**
 * CartController manages the shopping cart UI and user interactions.
 * Fully integrated with UserContext for real-world e-commerce logic.
 */
public class CartController extends VBox {
    private final CartService cartService;
    private final VBox itemsContainer;
    private final Label totalLabel;
    private final TextField searchField;
    private final ComboBox<String> sortCombo;
    private List<CartItem> allItems;

    public CartController() {
        this.cartService = new CartService();
        this.setSpacing(20);
        this.setPadding(new Insets(30));
        this.getStyleClass().add("main-content");

        // Header
        Label title = new Label("🛒  Your Shopping Cart");
        title.getStyleClass().add("content-title");

        // Search & Sort Bar
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(0, 0, 10, 0));

        searchField = new TextField();
        searchField.setPromptText("🔍 Search items in cart...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((o, ov, nv) -> filterAndDisplay());

        sortCombo = new ComboBox<>(FXCollections.observableArrayList("Name (A-Z)", "Price (Low to High)", "Price (High to Low)"));
        sortCombo.setPromptText("Sort By");
        sortCombo.setPrefWidth(180);
        sortCombo.setOnAction(e -> filterAndDisplay());

        actionBar.getChildren().addAll(searchField, sortCombo);

        itemsContainer = new VBox(15);
        ScrollPane scrollPane = new ScrollPane(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Footer / Summary
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 0, 0, 0));

        totalLabel = new Label("Total: $0.00");
        totalLabel.getStyleClass().add("label-bright");
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 22));

        Button checkoutBtn = new Button("Checkout Now");
        checkoutBtn.getStyleClass().add("button-success");
        checkoutBtn.setStyle("-fx-padding: 12 30; -fx-font-size: 16px;");
        checkoutBtn.setOnAction(e -> handleCheckout());

        footer.getChildren().addAll(totalLabel, checkoutBtn);

        this.getChildren().addAll(title, actionBar, scrollPane, footer);
        
        loadCart();
    }

    public void loadCart() {
        try {
            int currentUserId = UserContext.getCurrentUserId();
            allItems = cartService.getCartItems(currentUserId);
            filterAndDisplay();
        } catch (SQLException e) {
            showError("Failed to load cart: " + e.getMessage());
        }
    }

    private void filterAndDisplay() {
        itemsContainer.getChildren().clear();
        if (allItems == null) return;

        String search = searchField.getText().toLowerCase();
        List<CartItem> filtered = allItems.stream()
            .filter(i -> i.getProductName().toLowerCase().contains(search))
            .collect(java.util.stream.Collectors.toList());

        String sort = sortCombo.getValue();
        if (sort != null) {
            if (sort.equals("Name (A-Z)")) filtered.sort(java.util.Comparator.comparing(CartItem::getProductName));
            else if (sort.equals("Price (Low to High)")) filtered.sort(java.util.Comparator.comparing(CartItem::getUnitPrice));
            else if (sort.equals("Price (High to Low)")) filtered.sort(java.util.Comparator.comparing(CartItem::getUnitPrice).reversed());
        }

        double total = 0;
        for (CartItem item : filtered) {
            total += item.getSubtotal();
            itemsContainer.getChildren().add(buildItemRow(item));
        }

        totalLabel.setText(String.format("Total: $%.2f", total));

        if (filtered.isEmpty()) {
            Label emptyLabel = new Label(allItems.isEmpty() ? "Your cart is empty." : "No matching items found.");
            emptyLabel.getStyleClass().add("label-muted");
            emptyLabel.setStyle("-fx-font-size: 16px;");
            itemsContainer.getChildren().add(emptyLabel);
        }
    }

    private HBox buildItemRow(CartItem item) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.getStyleClass().add("card");

        VBox details = new VBox(5);
        Label name = new Label(item.getProductName());
        name.getStyleClass().add("label-bright");
        name.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Label price = new Label(String.format("$%.2f each", item.getUnitPrice()));
        price.getStyleClass().add("label-muted");
        details.getChildren().addAll(name, price);
        HBox.setHgrow(details, Priority.ALWAYS);

        TextField qtyField = new TextField(String.valueOf(item.getQuantity()));
        qtyField.setPrefWidth(60);
        qtyField.getStyleClass().add("form-field");
        qtyField.setOnAction(e -> updateQty(item.getCartItemId(), qtyField.getText()));

        Label subtotal = new Label(String.format("$%.2f", item.getSubtotal()));
        subtotal.getStyleClass().add("label-bright");
        subtotal.setStyle("-fx-text-fill: #38b86c; -fx-font-weight: bold; -fx-font-size: 16px;");
        subtotal.setPrefWidth(120);
        subtotal.setAlignment(Pos.CENTER_RIGHT);

        Button removeBtn = new Button("🗑");
        removeBtn.getStyleClass().add("button-danger");
        removeBtn.setStyle("-fx-font-size: 14px;");
        removeBtn.setOnAction(e -> {
            try {
                cartService.removeFromCart(item.getCartItemId());
                loadCart();
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        });

        row.getChildren().addAll(details, new Label("Qty:"), qtyField, subtotal, removeBtn);
        return row;
    }

    private void updateQty(int itemId, String qtyStr) {
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                showError("Quantity must be greater than zero.");
                return;
            }
            cartService.updateQuantity(itemId, qty);
            loadCart();
        } catch (NumberFormatException | SQLException e) {
            showError("Invalid quantity or update failed.");
        }
    }

    private void handleCheckout() {
        try {
            int userId = UserContext.getCurrentUserId();
            boolean success = cartService.checkout(userId);
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Order placed successfully! Check your Order History.", ButtonType.OK);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.showAndWait();
                loadCart();
            } else {
                showError("Your cart is empty.");
            }
        } catch (SQLException e) {
            showError("Checkout failed: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
