package com.ecommerce.controller;

import com.ecommerce.util.UserContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;

/**
 * DashboardController acts as the main orchestrator for the application's logged-in views.
 * Updated to use standard FXML loading for sub-modules.
 */
public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private StackPane contentHolder;
    
    private Runnable onLogout;

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    @FXML
    public void initialize() {
        userNameLabel.setText(UserContext.getCurrentUserName());
        userRoleLabel.setText(UserContext.getCurrentUserRole());
        
        loadRoleSpecificView();
    }

    private void loadRoleSpecificView() {
        contentHolder.getChildren().clear();
        
        try {
            if (UserContext.isAdmin()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin.fxml"));
                contentHolder.getChildren().add(loader.load());
            } else if (UserContext.isCustomer()) {
                setupCustomerTabs();
            }
        } catch (IOException e) {
            e.printStackTrace();
            contentHolder.getChildren().add(new Label("Error loading view: " + e.getMessage()));
        }
    }

    private void setupCustomerTabs() throws IOException {
        TabPane tabPane = new TabPane();
        
        // Tab 1: Products
        FXMLLoader prodLoader = new FXMLLoader(getClass().getResource("/fxml/products.fxml"));
        Tab browseTab = new Tab("📦  Browse Products", prodLoader.load());
        browseTab.setClosable(false);
        
        // Tab 2: Cart
        FXMLLoader cartLoader = new FXMLLoader(getClass().getResource("/fxml/cart.fxml"));
        Tab cartTab = new Tab("🛒  My Cart", cartLoader.load());
        CartController cartController = cartLoader.getController();
        cartTab.setClosable(false);
        
        // Tab 3: History
        FXMLLoader historyLoader = new FXMLLoader(getClass().getResource("/fxml/orders.fxml"));
        Tab historyTab = new Tab("📜  Order History", historyLoader.load());
        OrderHistoryController historyController = historyLoader.getController();
        historyTab.setClosable(false);

        // Tab 4: Profile
        FXMLLoader profileLoader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
        Tab profileTab = new Tab("👤  Profile", profileLoader.load());
        profileTab.setClosable(false);

        tabPane.getTabs().addAll(browseTab, cartTab, historyTab, profileTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == cartTab) {
                cartController.loadCart();
            } else if (newTab == historyTab) {
                historyController.loadOrders();
            }
        });

        contentHolder.getChildren().add(tabPane);
    }

    @FXML
    private void handleLogout() {
        UserContext.clear();
        if (onLogout != null) onLogout.run();
    }
}
