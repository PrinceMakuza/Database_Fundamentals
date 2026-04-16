package com.ecommerce.controller;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.CartService;
import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.util.PerformanceMonitor;
import com.ecommerce.util.UserContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.util.List;

/**
 * ProductController handles the user-facing product browsing view.
 * Refactored to separate UI structure from business logic using FXML.
 */
public class ProductController {
    private final ProductService productService = new ProductService();
    private final CartService cartService = new CartService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    
    @FXML private TableView<Product> productTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Label pageLabel;
    @FXML private Label resultCountLabel;
    @FXML private Label timingLabel;
    
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private int currentPage = 1;
    private final int pageSize = 500;
    private int totalProducts = 0;
    private List<Category> categories;

    @FXML
    public void initialize() {
        setupTable();
        loadInitialData();
        
        // Listeners for instant filtering
        categoryFilter.setOnAction(e -> handleSearch());
        sortCombo.setOnAction(e -> handleSearch());
        searchField.textProperty().addListener((o, ov, nv) -> handleSearch());
    }

    private void setupTable() {
        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        nameCol.setPrefWidth(220);

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        descCol.setPrefWidth(280);

        TableColumn<Product, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getPrice())));
        priceCol.setPrefWidth(100);
        priceCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Product, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoryName()));
        catCol.setPrefWidth(140);

        TableColumn<Product, String> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStockQuantity())));
        stockCol.setPrefWidth(100);
        stockCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    int stock = Integer.parseInt(item);
                    if (stock <= 0) setStyle("-fx-text-fill: #e05555; -fx-font-weight: bold;");
                    else if (stock < 5) setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #38b86c; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Product, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(140);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button cartBtn = new Button("Add to Cart");
            {
                cartBtn.getStyleClass().add("button-success");
                cartBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                cartBtn.setOnAction(event -> handleAddToCart(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    HBox container = new HBox(cartBtn);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });

        productTable.getColumns().addAll(nameCol, descCol, priceCol, catCol, stockCol, actionCol);
        productTable.setItems(productList);
    }

    private void loadInitialData() {
        try {
            categories = categoryDAO.getAllCategories();
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("All Categories");
            for (Category cat : categories) categoryFilter.getItems().add(cat.getName());
            categoryFilter.setValue("All Categories");
            
            sortCombo.getItems().setAll("Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)");
            sortCombo.setValue("Name (A-Z)");
            
            loadData();
        } catch (SQLException e) {
            showError("Data Error", e.getMessage());
        }
    }

    private void loadData() {
        try {
            String searchTerm = searchField.getText().trim();
            Integer catId = getSelectedCategoryId();
            totalProducts = productService.getSearchCount(searchTerm, catId);

            long startTime = System.nanoTime();
            List<Product> products = productService.searchProducts(searchTerm, catId, currentPage, pageSize, sortCombo.getValue());
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;

            PerformanceMonitor.record("Product Search", elapsed);
            productList.setAll(products);
            updatePaginationUI();

            resultCountLabel.setText(String.format("Showing %d of %d products", products.size(), totalProducts));
            timingLabel.setText(String.format("Query time: %dms", elapsed));
        } catch (SQLException e) {
            showError("Search Error", e.getMessage());
        }
    }

    private Integer getSelectedCategoryId() {
        String selected = categoryFilter.getValue();
        if (selected == null || "All Categories".equals(selected)) return null;
        return categories.stream().filter(c -> c.getName().equals(selected)).findFirst().map(Category::getCategoryId).orElse(null);
    }

    private void updatePaginationUI() {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalProducts / pageSize));
        pageLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
    }

    @FXML
    private void handleSearch() {
        currentPage = 1;
        loadData();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        handleSearch();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadData();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage * pageSize < totalProducts) {
            currentPage++;
            loadData();
        }
    }

    private void handleAddToCart(Product product) {
        if (product.getStockQuantity() <= 0) {
            showError("Out of Stock", "This item is currently unavailable.");
            return;
        }
        try {
            cartService.addToCart(UserContext.getCurrentUserId(), product.getProductId(), 1);
            showInfo("Added", product.getName() + " added to your cart!");
        } catch (SQLException e) {
            showError("Cart Error", e.getMessage());
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
