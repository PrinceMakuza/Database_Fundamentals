package com.ecommerce.controller;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.service.OrderService;
import com.ecommerce.util.UserContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * OrderHistoryController displays a user's past orders.
 */
public class OrderHistoryController extends VBox {
    private final OrderService orderService;
    private final VBox ordersContainer;
    private final TextField searchField;
    private final ComboBox<String> sortCombo;
    private List<Order> allOrders;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public OrderHistoryController() {
        this.orderService = new OrderService();
        this.setSpacing(20);
        this.setPadding(new Insets(30));
        this.setStyle("-fx-background-color: #1e1e2f;");

        // Header
        Label title = new Label("📜  Order History");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        // Search & Sort Bar
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 0, 10, 0));

        searchField = new TextField();
        searchField.setPromptText("🔍 Search by Order ID...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((o, ov, nv) -> filterAndDisplay());

        sortCombo = new ComboBox<>(FXCollections.observableArrayList("Date (Newest)", "Date (Oldest)", "Amount (High)", "Amount (Low)"));
        sortCombo.setPromptText("Sort By");
        sortCombo.setPrefWidth(180);
        sortCombo.setOnAction(e -> filterAndDisplay());

        bar.getChildren().addAll(searchField, sortCombo);

        ordersContainer = new VBox(15);
        ScrollPane scrollPane = new ScrollPane(ordersContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        this.getChildren().addAll(title, bar, scrollPane);
        
        loadOrders();
    }

    public void loadOrders() {
        try {
            allOrders = orderService.getUserOrderHistory(UserContext.getCurrentUserId());
            filterAndDisplay();
        } catch (SQLException e) {
            showError("Failed to load orders: " + e.getMessage());
        }
    }

    private void filterAndDisplay() {
        ordersContainer.getChildren().clear();
        if (allOrders == null) return;

        String search = searchField.getText().trim();
        List<Order> filtered = allOrders.stream()
            .filter(o -> search.isEmpty() || String.valueOf(o.getOrderId()).contains(search))
            .collect(java.util.stream.Collectors.toList());

        String sort = sortCombo.getValue();
        if (sort != null) {
            if (sort.equals("Date (Newest)")) filtered.sort(java.util.Comparator.comparing(Order::getOrderDate).reversed());
            else if (sort.equals("Date (Oldest)")) filtered.sort(java.util.Comparator.comparing(Order::getOrderDate));
            else if (sort.equals("Amount (High)")) filtered.sort(java.util.Comparator.comparing(Order::getTotalAmount).reversed());
            else if (sort.equals("Amount (Low)")) filtered.sort(java.util.Comparator.comparing(Order::getTotalAmount));
        }

        if (filtered.isEmpty()) {
            Label emptyLabel = new Label(allOrders.isEmpty() ? "No orders found yet." : "No matching orders found.");
            emptyLabel.setTextFill(Color.GRAY);
            ordersContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Order order : filtered) {
            ordersContainer.getChildren().add(createOrderCard(order));
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #2a2a44; -fx-background-radius: 10; -fx-border-color: #3f3f5f; -fx-border-radius: 10;");

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox orderInfo = new VBox(2);
        Label orderIdLabel = new Label("Order #" + order.getOrderId());
        orderIdLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        orderIdLabel.setTextFill(Color.WHITE);
        Label dateLabel = new Label(order.getOrderDate().format(DATE_FORMAT));
        dateLabel.setTextFill(Color.web("#a0a8c0"));
        orderInfo.getChildren().addAll(orderIdLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(String.format("$%.2f", order.getTotalAmount()));
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        amountLabel.setTextFill(Color.web("#4cd137"));

        Button viewDetailsBtn = new Button("View Items");
        viewDetailsBtn.setStyle("-fx-background-color: #3f3f5f; -fx-text-fill: white;");
        
        VBox detailsContainer = new VBox(5);
        detailsContainer.setPadding(new Insets(10, 0, 0, 20));
        detailsContainer.setVisible(false);
        detailsContainer.setManaged(false);

        viewDetailsBtn.setOnAction(e -> {
            boolean isVisible = !detailsContainer.isVisible();
            detailsContainer.setVisible(isVisible);
            detailsContainer.setManaged(isVisible);
            viewDetailsBtn.setText(isVisible ? "Hide Items" : "View Items");
            
            if (isVisible && detailsContainer.getChildren().isEmpty()) {
                fetchOrderDetails(order.getOrderId(), detailsContainer);
            }
        });

        header.getChildren().addAll(orderInfo, spacer, amountLabel, viewDetailsBtn);
        card.getChildren().addAll(header, detailsContainer);
        
        return card;
    }

    private void fetchOrderDetails(int orderId, VBox container) {
        try {
            List<OrderItem> items = orderService.getOrderDetails(orderId);
            for (OrderItem item : items) {
                HBox row = new HBox(10);
                Label name = new Label(item.getProductName());
                name.setTextFill(Color.WHITE);
                name.setPrefWidth(200);
                
                Label qty = new Label("x" + item.getQuantity());
                qty.setTextFill(Color.web("#a0a8c0"));
                qty.setPrefWidth(50);
                
                Label price = new Label(String.format("$%.2f", item.getUnitPrice()));
                price.setTextFill(Color.web("#a0a8c0"));
                price.setPrefWidth(80);
                
                row.getChildren().addAll(name, qty, price);
                container.getChildren().add(row);
            }
        } catch (SQLException e) {
            container.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
