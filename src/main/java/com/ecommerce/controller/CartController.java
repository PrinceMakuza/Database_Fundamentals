package com.ecommerce.controller;

import com.ecommerce.model.CartItem;
import com.ecommerce.service.CartService;
import com.ecommerce.util.UserContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CartController handles shopping cart interactions and layout.
 * Refactored for FXML compatibility and clean orchestration.
 */
public class CartController {
    private final CartService cartService = new CartService();
    
    @FXML private VBox itemsContainer;
    @FXML private Label totalLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    
    private List<CartItem> allItems;

    @FXML
    public void initialize() {
        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList("Name (A-Z)", "Price (Low to High)", "Price (High to Low)"));
        }
        loadCart();
    }

    public void loadCart() {
        try {
            allItems = cartService.getCartItems(UserContext.getCurrentUserId());
            filterAndDisplay();
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }
    }

    @FXML
    private void filterAndDisplay() {
        itemsContainer.getChildren().clear();
        if (allItems == null) return;

        String search = searchField.getText().toLowerCase();
        List<CartItem> filtered = allItems.stream()
            .filter(i -> i.getProductName().toLowerCase().contains(search))
            .collect(Collectors.toList());

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
                showError("Remove Error", ex.getMessage());
            }
        });

        row.getChildren().addAll(details, new Label("Qty:"), qtyField, subtotal, removeBtn);
        return row;
    }

    private void updateQty(int itemId, String qtyStr) {
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                showError("Validation", "Quantity must be greater than zero.");
                return;
            }
            cartService.updateQuantity(itemId, qty);
            loadCart();
        } catch (NumberFormatException | SQLException e) {
            showError("Update Error", "Invalid quantity or update failed.");
        }
    }

    @FXML
    private void handleCheckout() {
        try {
            if (cartService.checkout(UserContext.getCurrentUserId())) {
                showInfo("Success", "Order placed successfully! Check your Order History.");
                loadCart();
            } else {
                showError("Checkout", "Your cart is empty.");
            }
        } catch (SQLException e) {
            showError("Checkout Error", e.getMessage());
        }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.show();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setTitle(title);
        a.show();
    }
}
