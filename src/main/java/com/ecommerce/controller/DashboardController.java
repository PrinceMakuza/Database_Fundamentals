package com.ecommerce.controller;

import com.ecommerce.util.UserContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * DashboardController is the main shell after login.
 * Manages view switching and ensures data (like the Cart) is refreshed on navigation.
 */
public class DashboardController extends BorderPane {

    private CartController cartController;

    public DashboardController(Runnable onLogout) {
        // Base styling handled by CSS

        // Top Bar
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #121212; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label logo = new Label("🚀  Smart E-Commerce");
        logo.getStyleClass().add("content-title");
        logo.setStyle("-fx-font-size: 20px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userProfile = new VBox(2);
        userProfile.setAlignment(Pos.CENTER_RIGHT);
        Label userName = new Label(UserContext.getCurrentUserName());
        userName.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label userRole = new Label(UserContext.getCurrentUserRole());
        userRole.setStyle("-fx-text-fill: #3498db; -fx-font-size: 10px;");
        userProfile.getChildren().addAll(userName, userRole);

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("button-danger");
        logoutBtn.setStyle("-fx-font-size: 12px; -fx-padding: 5 15;");
        logoutBtn.setOnAction(e -> {
            UserContext.clear();
            onLogout.run();
        });

        topBar.getChildren().addAll(logo, spacer, userProfile, logoutBtn);
        this.setTop(topBar);

        // Content Area
        StackPane contentHolder = new StackPane();
        contentHolder.getStyleClass().add("main-content");
        
        if (UserContext.isAdmin()) {
            contentHolder.getChildren().add(new AdminController());
        } else {
            TabPane userTabs = new TabPane();
            
            ProductController productController = new ProductController();
            cartController = new CartController();
            OrderHistoryController orderHistoryController = new OrderHistoryController();

            Tab browseTab = new Tab("📦  Browse Products", productController);
            browseTab.setClosable(false);
            
            Tab cartTab = new Tab("🛒  My Cart", cartController);
            cartTab.setClosable(false);
            
            Tab historyTab = new Tab("📜  Order History", orderHistoryController);
            historyTab.setClosable(false);

            Tab profileTab = new Tab("👤  Profile", new ProfileController());
            profileTab.setClosable(false);

            userTabs.getTabs().addAll(browseTab, cartTab, historyTab, profileTab);

            // AUTO-REFRESH LOGIC: Refresh cart or history when their tabs are selected
            userTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab == cartTab) {
                    cartController.loadCart();
                } else if (newTab == historyTab) {
                    orderHistoryController.loadOrders();
                }
            });

            contentHolder.getChildren().add(userTabs);
        }

        // We wrap everything in a ScrollPane for safety, though children often have their own ScrollPanes
        ScrollPane scrollPane = new ScrollPane(contentHolder);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("content-scroll-pane");
        
        this.setCenter(scrollPane);
    }
}
