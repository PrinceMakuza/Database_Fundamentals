package com.ecommerce.controller;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Order;
import com.ecommerce.model.Review;
import com.ecommerce.service.ProductService;
import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.ReviewDAO;
import com.ecommerce.dao.DatabaseConnection;
import com.ecommerce.service.ReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminController manages all back-office operations.
 * Updated to fully implement search and sort logic across all management panels.
 */
public class AdminController {
    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final UserDAO userDAO = new UserDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final ReportService reportService = new ReportService();

    @FXML private HBox toggleBar;
    @FXML private StackPane contentPane;
    @FXML private Button prodBtn, catBtn, userBtn, orderBtn, invBtn, reviewBtn, reportBtn;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();
    private final ObservableList<Review> reviewList = FXCollections.observableArrayList();

    private TextField productSearch, catSearch, userSearch, orderSearch, reviewSearch, invSearch;
    private ComboBox<String> productSort, userSort, orderSort, reviewSort, invSort;
    private List<Category> categories;

    private VBox prodPanel, catPanel, userPanel, orderPanel, reviewPanel, invPanel, reportPanel;

    @FXML
    public void initialize() {
        prodPanel = buildProductPanel();
        catPanel = buildCategoryPanel();
        userPanel = buildUserPanel();
        orderPanel = buildOrderPanel();
        reviewPanel = buildReviewPanel();
        invPanel = buildInventoryPanel();
        reportPanel = buildReportPanel();
        
        contentPane.getChildren().addAll(prodPanel, catPanel, userPanel, orderPanel, reviewPanel, invPanel, reportPanel);
        showProducts();
        loadInitialData();
    }

    @FXML private void showProducts() { switchTab(prodPanel, prodBtn); loadInitialData(); }
    @FXML private void showCategories() { switchTab(catPanel, catBtn); loadInitialData(); }
    @FXML private void showUsers() { loadUsers(); switchTab(userPanel, userBtn); }
    @FXML private void showOrders() { loadOrders(); switchTab(orderPanel, orderBtn); }
    @FXML private void showInventory() { loadInventory(); switchTab(invPanel, invBtn); }
    @FXML private void showReviews() { loadReviews(); switchTab(reviewPanel, reviewBtn); }
    @FXML private void showReports() { switchTab(reportPanel, reportBtn); }

    private void switchTab(VBox panel, Button btn) {
        contentPane.getChildren().forEach(c -> c.setVisible(false));
        panel.setVisible(true);
        toggleBar.getChildren().forEach(b -> b.getStyleClass().remove("button-primary"));
        btn.getStyleClass().add("button-primary");
    }

    // --- PANEL BUILDERS ---

    private VBox buildProductPanel() {
        VBox panel = new VBox(15);
        HBox bar = new HBox(10); bar.setAlignment(Pos.CENTER_LEFT);
        Button add = new Button("➕ Add New Product"); add.getStyleClass().add("button-success");
        Button edit = new Button("✏ Edit Selected"); edit.getStyleClass().add("button-primary");
        Button del = new Button("🗑 Delete Selected"); del.getStyleClass().add("button-danger");
        Button seed = new Button("🌱 Seed Samples"); seed.getStyleClass().add("button-warning");
        
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        productSearch = new TextField(); productSearch.setPromptText("🔍 Search products...");
        productSearch.textProperty().addListener((o, ov, nv) -> loadInitialData());

        productSort = new ComboBox<>(FXCollections.observableArrayList("ID (Oldest First)", "ID (Newest First)", "Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)"));
        productSort.setValue("ID (Oldest First)");
        productSort.setOnAction(e -> loadInitialData());

        bar.getChildren().addAll(add, edit, del, seed, spacer, productSearch, productSort);
        TableView<Product> table = createProductTable();
        add.setOnAction(e -> handleProductDialog(null));
        edit.setOnAction(e -> { Product s = table.getSelectionModel().getSelectedItem(); if (s != null) handleProductDialog(s); });
        del.setOnAction(e -> { Product s = table.getSelectionModel().getSelectedItem(); if (s != null) handleDeleteProduct(s); });
        seed.setOnAction(e -> handleSeedData());

        panel.getChildren().addAll(bar, table);
        return panel;
    }

    private VBox buildCategoryPanel() {
        VBox panel = new VBox(15);
        HBox bar = new HBox(10); bar.setAlignment(Pos.CENTER_LEFT);
        Button add = new Button("➕ Add Category"); add.getStyleClass().add("button-success");
        Button edit = new Button("✏ Edit Selected"); edit.getStyleClass().add("button-primary");
        Button del = new Button("🗑 Delete Selected"); del.getStyleClass().add("button-danger");
        
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        catSearch = new TextField(); catSearch.setPromptText("🔍 Search categories...");
        bar.getChildren().addAll(add, edit, del, spacer, catSearch);

        TableView<Category> table = createCategoryTable();
        add.setOnAction(e -> handleCategoryDialog(null));
        edit.setOnAction(e -> { Category s = table.getSelectionModel().getSelectedItem(); if (s != null) handleCategoryDialog(s); });
        del.setOnAction(e -> { Category s = table.getSelectionModel().getSelectedItem(); if (s != null) handleDeleteCategory(s); });

        panel.getChildren().addAll(bar, table);
        return panel;
    }

    private VBox buildUserPanel() {
        VBox panel = new VBox(15);
        HBox bar = new HBox(10); bar.setAlignment(Pos.CENTER_LEFT);
        Button add = new Button("➕ Add New User"); add.getStyleClass().add("button-success");
        Button edit = new Button("✏ Edit Selected"); edit.getStyleClass().add("button-primary");
        Button del = new Button("🗑 Delete Selected"); del.getStyleClass().add("button-danger");
        
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        userSearch = new TextField(); userSearch.setPromptText("🔍 Search users by name/email...");
        userSort = new ComboBox<>(FXCollections.observableArrayList("ID ASC", "ID DESC", "Name A-Z", "Role"));
        userSort.setValue("ID ASC");

        bar.getChildren().addAll(add, edit, del, spacer, userSearch, userSort);

        TableView<User> table = createUserTable();
        add.setOnAction(e -> handleUserDialog(null));
        edit.setOnAction(e -> { User s = table.getSelectionModel().getSelectedItem(); if (s != null) handleUserDialog(s); });
        del.setOnAction(e -> { User s = table.getSelectionModel().getSelectedItem(); if (s != null) handleDeleteUser(s); });

        panel.getChildren().addAll(bar, table);
        return panel;
    }

    private VBox buildOrderPanel() {
        VBox panel = new VBox(15);
        HBox bar = new HBox(10); bar.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label("System Orders History"); lbl.getStyleClass().add("label-bright");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        orderSearch = new TextField(); orderSearch.setPromptText("🔍 Search by Order ID or Customer...");
        orderSearch.textProperty().addListener((o, ov, nv) -> applyOrderFilters());
        
        orderSort = new ComboBox<>(FXCollections.observableArrayList("Date (Newest)", "Date (Oldest)", "Amount (High)", "Status"));
        orderSort.setValue("Date (Newest)");
        orderSort.setOnAction(e -> applyOrderFilters());

        bar.getChildren().addAll(lbl, spacer, orderSearch, orderSort);
        panel.getChildren().addAll(bar, createOrderTable());
        return panel;
    }

    private VBox buildReviewPanel() {
        VBox panel = new VBox(15);
        HBox bar = new HBox(10); bar.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label("Product Reviews Moderation"); lbl.getStyleClass().add("label-bright");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        reviewSearch = new TextField(); reviewSearch.setPromptText("🔍 Search reviews text or product...");
        reviewSearch.textProperty().addListener((o, ov, nv) -> applyReviewFilters());

        reviewSort = new ComboBox<>(FXCollections.observableArrayList("Rating (High)", "Rating (Low)", "Product Name"));
        reviewSort.setValue("Rating (High)");
        reviewSort.setOnAction(e -> applyReviewFilters());

        bar.getChildren().addAll(lbl, spacer, reviewSearch, reviewSort);
        panel.getChildren().addAll(bar, createReviewTable());
        return panel;
    }

    private VBox buildInventoryPanel() {
        VBox panel = new VBox(15);
        HBox bar = new HBox(10); bar.setAlignment(Pos.CENTER_LEFT);
        Button edit = new Button("🔧 Edit Stock Level"); edit.getStyleClass().add("button-primary");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        invSearch = new TextField(); invSearch.setPromptText("🔍 Search stock...");
        invSearch.textProperty().addListener((o, ov, nv) -> loadInventory());

        invSort = new ComboBox<>(FXCollections.observableArrayList("ID (Oldest First)", "Stock (Low to High)", "Stock (High to Low)"));
        invSort.setValue("ID (Oldest First)");
        invSort.setOnAction(e -> loadInventory());

        bar.getChildren().addAll(edit, spacer, invSearch, invSort);
        TableView<Product> table = createInventoryTable();
        edit.setOnAction(e -> { Product s = table.getSelectionModel().getSelectedItem(); if (s != null) handleInventoryDialog(s); });
        panel.getChildren().addAll(bar, table);
        return panel;
    }

    private VBox buildReportPanel() {
        VBox panel = new VBox(25); panel.setAlignment(Pos.CENTER); panel.setPadding(new Insets(50));
        VBox reportBox = new VBox(20); reportBox.setAlignment(Pos.CENTER); reportBox.setMaxWidth(500); reportBox.setPadding(new Insets(40));
        reportBox.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 15; -fx-border-color: #333; -fx-border-radius: 15;");

        Button perfBtn = new Button("🚀 Generate Performance Report"); perfBtn.getStyleClass().add("button-primary");
        Button valBtn = new Button("✅ Generate Validation Report"); valBtn.getStyleClass().add("button-success");
        ProgressIndicator progress = new ProgressIndicator(); progress.setVisible(false);

        perfBtn.setOnAction(e -> runReportTask("Performance Report", () -> reportService.generatePerformanceReport(), progress));
        valBtn.setOnAction(e -> runReportTask("Validation Report", () -> reportService.generateValidationReport(), progress));
        reportBox.getChildren().addAll(perfBtn, valBtn, progress);
        panel.getChildren().addAll(reportBox);
        return panel;
    }

    private void runReportTask(String title, ReportTask task, ProgressIndicator progress) {
        progress.setVisible(true);
        new Thread(() -> {
            try {
                String path = task.execute();
                javafx.application.Platform.runLater(() -> {
                    progress.setVisible(false);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Report saved to: " + path);
                    alert.setTitle(title); alert.show();
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> { progress.setVisible(false); showAlert(Alert.AlertType.ERROR, "Failed", ex.getMessage()); });
            }
        }).start();
    }

    @FunctionalInterface interface ReportTask { String execute() throws Exception; }

    // --- DIALOGS & HANDLERS ---

    private void handleProductDialog(Product existing) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Product" : "Edit Product");
        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField name = new TextField(existing != null ? existing.getName() : "");
        TextArea desc = new TextArea(existing != null ? existing.getDescription() : ""); desc.setPrefRowCount(3);
        TextField price = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "");
        TextField stock = new TextField(existing != null ? String.valueOf(existing.getStockQuantity()) : "10");
        ComboBox<String> cat = new ComboBox<>(FXCollections.observableArrayList(categories.stream().map(Category::getName).toList()));
        if (existing != null) cat.setValue(existing.getCategoryName());

        grid.add(new Label("Name:"), 0, 0); grid.add(name, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(desc, 1, 1);
        grid.add(new Label("Price:"), 0, 2); grid.add(price, 1, 2);
        grid.add(new Label("Stock:"), 0, 3); grid.add(stock, 1, 3);
        grid.add(new Label("Category:"), 0, 4); grid.add(cat, 1, 4);
        dialog.getDialogPane().setContent(grid);
        
        Button okButton = (Button) dialog.getDialogPane().lookupButton(saveBtnType);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                if (name.getText().trim().isEmpty()) throw new Exception("Name is required.");
                Double.parseDouble(price.getText());
                Integer.parseInt(stock.getText());
                if (cat.getValue() == null) throw new Exception("Category is required.");
            } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Validation Error", ex.getMessage()); event.consume(); }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                Product p = existing != null ? existing : new Product();
                p.setName(name.getText().trim()); p.setDescription(desc.getText().trim());
                p.setPrice(Double.parseDouble(price.getText()));
                p.setStockQuantity(Integer.parseInt(stock.getText()));
                categories.stream().filter(c -> c.getName().equals(cat.getValue())).findFirst().ifPresent(c -> p.setCategoryId(c.getCategoryId()));
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            try { if (existing == null) productService.addProduct(p); else productService.updateProduct(p); loadInitialData(); }
            catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
        });
    }

    private void handleDeleteProduct(Product selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { productService.deleteProduct(selected.getProductId()); loadInitialData(); }
                catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
            }
        });
    }

    private void handleCategoryDialog(Category existing) {
        TextInputDialog dialog = new TextInputDialog(existing != null ? existing.getName() : "");
        dialog.setTitle(existing == null ? "Add Category" : "Edit Category");
        dialog.setHeaderText("Enter category name:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                if (existing == null) { Category c = new Category(); c.setName(name); categoryDAO.addCategory(c); }
                else { existing.setName(name); categoryDAO.updateCategory(existing); }
                loadInitialData();
            } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
        });
    }

    private void handleDeleteCategory(Category selected) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove category " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { categoryDAO.deleteCategory(selected.getCategoryId()); loadInitialData(); }
                catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
            }
        });
    }

    private void handleInventoryDialog(Product product) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(product.getStockQuantity()));
        dialog.setTitle("Update Stock");
        dialog.setHeaderText("Update stock for: " + product.getName());
        dialog.showAndWait().ifPresent(qty -> {
            try { product.setStockQuantity(Integer.parseInt(qty)); productService.updateProduct(product); loadInventory(); }
            catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", "Invalid quantity"); }
        });
    }

    private void handleUserDialog(User existing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New User" : "Edit User");
        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField name = new TextField(existing != null ? existing.getName() : "");
        TextField email = new TextField(existing != null ? existing.getEmail() : "");
        TextField location = new TextField(existing != null ? existing.getLocation() : "");
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("CUSTOMER", "ADMIN"));
        role.setValue(existing != null ? existing.getRole() : "CUSTOMER");
        grid.add(new Label("Name:"), 0, 0); grid.add(name, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(email, 1, 1);
        grid.add(new Label("Location:"), 0, 2); grid.add(location, 1, 2);
        grid.add(new Label("Role:"), 0, 3); grid.add(role, 1, 3);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                User u = existing != null ? existing : new User();
                u.setName(name.getText().trim()); u.setEmail(email.getText().trim().toLowerCase());
                u.setRole(role.getValue()); u.setLocation(location.getText().trim());
                if (existing == null) { u.setPassword(BCrypt.hashpw(u.getName().split("\\s+")[0] + "@123", BCrypt.gensalt())); }
                return u;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(u -> {
            try { if (existing == null) userDAO.addUser(u); else userDAO.updateUser(u); loadUsers(); }
            catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
        });
    }

    private void handleDeleteUser(User selected) {
        if ("ADMIN".equalsIgnoreCase(selected.getRole())) { showAlert(Alert.AlertType.WARNING, "Guard", "Admins cannot be removed."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove user " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try { userDAO.deleteUser(selected.getUserId()); loadUsers(); }
                catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
            }
        });
    }

    private void handleSeedData() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO Categories (name) VALUES ('Smartphones'), ('Laptops'), ('Audio'), ('Wearables'), ('Accessories') ON CONFLICT DO NOTHING");
            stmt.execute("INSERT INTO Products (name, description, price, category_id) SELECT 'Seed Product', 'Sample', 99.99, category_id FROM Categories LIMIT 1 ON CONFLICT DO NOTHING");
            loadInitialData(); showAlert(Alert.AlertType.INFORMATION, "Success", "Sample data populated!");
        } catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Seed Failed", e.getMessage()); }
    }

    // --- TABLE GENERATORS ---

    private TableView<Product> createProductTable() {
        TableView<Product> t = new TableView<>(productList);
        addColumn(t, "ID", 60, d -> new SimpleObjectProperty<Object>(d.getProductId()));
        addColumn(t, "Name", 200, d -> new SimpleStringProperty(d.getName()));
        addColumn(t, "Price", 100, d -> new SimpleStringProperty(String.format("$%.2f", d.getPrice())));
        addColumn(t, "Category", 150, d -> new SimpleStringProperty(d.getCategoryName()));
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return t;
    }

    private TableView<Category> createCategoryTable() {
        FilteredList<Category> filtered = new FilteredList<>(categoryList, p -> true);
        if (catSearch != null) catSearch.textProperty().addListener((o, ov, nv) -> filtered.setPredicate(c -> nv == null || nv.isEmpty() || c.getName().toLowerCase().contains(nv.toLowerCase())));
        TableView<Category> t = new TableView<>(filtered);
        addColumn(t, "ID", 60, d -> new SimpleObjectProperty<Object>(d.getCategoryId()));
        addColumn(t, "Name", 300, d -> new SimpleStringProperty(d.getName()));
        return t;
    }

    private TableView<User> createUserTable() {
        FilteredList<User> filtered = new FilteredList<>(userList, p -> true);
        userSearch.textProperty().addListener((o, ov, nv) -> filtered.setPredicate(u -> {
            if (nv == null || nv.isEmpty()) return true;
            String lower = nv.toLowerCase();
            return u.getName().toLowerCase().contains(lower) || u.getEmail().toLowerCase().contains(lower);
        }));

        SortedList<User> sorted = new SortedList<>(filtered);
        userSort.setOnAction(e -> applyUserSorting(sorted));
        
        TableView<User> t = new TableView<>(sorted);
        addColumn(t, "ID", 60, d -> new SimpleObjectProperty<Object>(d.getUserId()));
        addColumn(t, "Name", 200, d -> new SimpleStringProperty(d.getName()));
        addColumn(t, "Email", 250, d -> new SimpleStringProperty(d.getEmail()));
        addColumn(t, "Role", 100, d -> new SimpleStringProperty(d.getRole()));
        return t;
    }

    private void applyUserSorting(SortedList<User> sorted) {
        String mode = userSort.getValue();
        if ("ID ASC".equals(mode)) sorted.setComparator(Comparator.comparing(User::getUserId));
        else if ("ID DESC".equals(mode)) sorted.setComparator(Comparator.comparing(User::getUserId).reversed());
        else if ("Name A-Z".equals(mode)) sorted.setComparator(Comparator.comparing(User::getName));
        else if ("Role".equals(mode)) sorted.setComparator(Comparator.comparing(User::getRole));
    }

    private TableView<Order> createOrderTable() {
        FilteredList<Order> filtered = new FilteredList<>(orderList, p -> true);
        TableView<Order> t = new TableView<>(new SortedList<>(filtered)); // Initial empty sorted list
        // Note: The actual sorted logic is handled in applyOrderFilters() which updates orderList
        addColumn(t, "Order ID", 80, d -> new SimpleObjectProperty<Object>(d.getOrderId()));
        addColumn(t, "Customer", 200, d -> new SimpleStringProperty(d.getUserName()));
        addColumn(t, "Total", 100, d -> new SimpleStringProperty(String.format("$%.2f", d.getTotalAmount())));
        addColumn(t, "Status", 100, d -> new SimpleStringProperty(d.getStatus()));
        return t;
    }

    private void applyOrderFilters() {
        try {
            List<Order> all = orderDAO.getAllOrders();
            String search = orderSearch.getText().toLowerCase();
            List<Order> filtered = all.stream().filter(o -> 
                String.valueOf(o.getOrderId()).contains(search) || 
                o.getUserName().toLowerCase().contains(search)
            ).collect(Collectors.toList());

            String sort = orderSort.getValue();
            if ("Date (Newest)".equals(sort)) filtered.sort(Comparator.comparing(Order::getOrderDate).reversed());
            else if ("Date (Oldest)".equals(sort)) filtered.sort(Comparator.comparing(Order::getOrderDate));
            else if ("Amount (High)".equals(sort)) filtered.sort(Comparator.comparing(Order::getTotalAmount).reversed());
            else if ("Status".equals(sort)) filtered.sort(Comparator.comparing(Order::getStatus));

            orderList.setAll(filtered);
        } catch (SQLException e) {}
    }

    private TableView<Review> createReviewTable() {
        TableView<Review> t = new TableView<>(reviewList);
        addColumn(t, "Product", 200, d -> new SimpleStringProperty(d.getProductName()));
        addColumn(t, "Rating", 80, d -> new SimpleStringProperty("\u2B50".repeat(d.getRating())));
        addColumn(t, "Comment", 400, d -> new SimpleStringProperty(d.getComment()));
        return t;
    }

    private void applyReviewFilters() {
        try {
            List<Review> all = reviewDAO.getAllReviews();
            String search = reviewSearch.getText().toLowerCase();
            List<Review> filtered = all.stream().filter(r -> 
                r.getProductName().toLowerCase().contains(search) || 
                r.getComment().toLowerCase().contains(search)
            ).collect(Collectors.toList());

            String sort = reviewSort.getValue();
            if ("Rating (High)".equals(sort)) filtered.sort(Comparator.comparing(Review::getRating).reversed());
            else if ("Rating (Low)".equals(sort)) filtered.sort(Comparator.comparing(Review::getRating));
            else if ("Product Name".equals(sort)) filtered.sort(Comparator.comparing(Review::getProductName));

            reviewList.setAll(filtered);
        } catch (SQLException e) {}
    }

    private TableView<Product> createInventoryTable() {
        TableView<Product> t = new TableView<>(productList);
        addColumn(t, "ID", 60, d -> new SimpleObjectProperty<>(d.getProductId()));
        addColumn(t, "Product Name", 300, d -> new SimpleStringProperty(d.getName()));
        addColumn(t, "In Stock", 120, d -> new SimpleObjectProperty<>(d.getStockQuantity()));
        return t;
    }

    // --- UTILS ---

    private void loadInitialData() { 
        try { 
            categories = categoryDAO.getAllCategories(); 
            categoryList.setAll(categories);
            String search = (productSearch != null) ? productSearch.getText() : "";
            String sort = (productSort != null) ? productSort.getValue() : "ID (Oldest First)";
            productList.setAll(productService.searchProducts(search, null, 1, 500, sort));
        } catch (SQLException e) {} 
    }

    private void loadUsers() { 
        try { 
            userList.setAll(userDAO.getAllUsers().stream().filter(u -> !"ADMIN".equals(u.getRole())).toList()); 
        } catch (SQLException e) {} 
    }

    private void loadOrders() { applyOrderFilters(); }
    private void loadReviews() { applyReviewFilters(); }
    
    private void loadInventory() { 
        try {
            String search = (invSearch != null) ? invSearch.getText() : "";
            String sort = (invSort != null) ? invSort.getValue() : "ID (Oldest First)";
            productList.setAll(productService.searchProducts(search, null, 1, 500, sort)); 
        } catch (SQLException e) {}
    }

    private <T> void addColumn(TableView<T> t, String name, double width, java.util.function.Function<T, ObservableValue<?>> f) {
        TableColumn<T, Object> col = new TableColumn<>(name); col.setPrefWidth(width);
        col.setCellValueFactory(data -> (ObservableValue<Object>) f.apply(data.getValue()));
        t.getColumns().add(col);
    }
    private void showAlert(Alert.AlertType t, String ti, String m) { Alert a = new Alert(t, m); a.setTitle(ti); a.show(); }
}
