package com.ecommerce;

import com.ecommerce.controller.LoginController;
import com.ecommerce.controller.DashboardController;
import com.ecommerce.dao.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * MainApp is the entry point for the Smart E-Commerce System.
 * Refactored to use FXML for a professional separation of concerns.
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private Scene mainScene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Smart E-Commerce System");

        showLogin();

        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            LoginController controller = loader.getController();
            controller.setOnLoginSuccess(this::showDashboard);
            
            if (mainScene == null) {
                mainScene = new Scene(root, 1050, 700);
                applyStyles(mainScene);
                primaryStage.setScene(mainScene);
            } else {
                mainScene.setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            
            DashboardController controller = loader.getController();
            controller.setOnLogout(this::showLogin);
            
            mainScene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyStyles(Scene scene) {
        String cssPath = getClass().getResource("/css/styles.css") != null
            ? getClass().getResource("/css/styles.css").toExternalForm()
            : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.closePool();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
