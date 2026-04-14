package com.ecommerce.controller;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import com.ecommerce.util.PerformanceMonitor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;
import java.sql.SQLException;
import java.util.List;

/**
 * ProductController handles the user-facing product browsing view.
 * Implements User Story 2.2 (view products with pagination)
 * and User Story 3.1 (case-insensitive search with measurable improvement).
 *
 * All data is retrieved via parameterized JDBC queries through the service layer.
 * Search results are cached in a HashMap (CacheService) for O(1) subsequent lookups.
 */
public class ProductController extends VBox {
    private final ProductService productService;
    private final com.ecommerce.service.CartService cartService;
    private final com.ecommerce.dao.CategoryDAO categoryDAO;
    private final TableView<Product> productTable;
    private final TextField searchField;
    private final ComboBox<String> categoryFilter;
    private final ComboBox<String> sortCombo;
    private final Label pageLabel;
    private final Label resultCountLabel;
    private final Label timingLabel;
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    private int currentPage = 1;
    private final int pageSize = 500;
    private int totalProducts = 0;
    private List<Category> categories;

    public ProductController() {
        this.productService = new ProductService();
        this.cartService = new com.ecommerce.service.CartService();
        this.categoryDAO = new com.ecommerce.dao.CategoryDAO();
        this.setSpacing(20);
        this.setPadding(new Insets(0));
        this.getStyleClass().add("main-content");

        // Title Section
        VBox header = new VBox(5);
        Label title = new Label("📦  Browse Products");
        title.getStyleClass().add("content-title");
        Label subtitle = new Label("Search, filter, and explore our product catalog");
        subtitle.getStyleClass().add("content-subtitle");
        header.getChildren().addAll(title, subtitle);

        // Search & Filter Bar (Card style)
        VBox searchCard = new VBox(15);
        searchCard.getStyleClass().add("card");

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search products by name...");
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        categoryFilter = new ComboBox<>();
        categoryFilter.setPromptText("All Categories");
        categoryFilter.setPrefWidth(180);

        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)");
        sortCombo.setValue("Name (A-Z)");
        sortCombo.setPrefWidth(180);

        // Reactive Filters: Instant updates on selection change
        categoryFilter.setOnAction(e -> { currentPage = 1; loadData(); });
        sortCombo.setOnAction(e -> { currentPage = 1; loadData(); });
        searchField.textProperty().addListener((o, ov, nv) -> { currentPage = 1; loadData(); });

        Button searchBtn = new Button("🔍  Search");
        searchBtn.getStyleClass().add("button-primary");
        searchBtn.setOnAction(e -> {
            currentPage = 1;
            loadData();
        });

        // Pressing Enter in search field triggers search
        searchField.setOnAction(e -> {
            currentPage = 1;
            loadData();
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            categoryFilter.setValue("All Categories");
            currentPage = 1;
            loadData();
        });

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.BOTTOM_LEFT);
        topRow.getChildren().addAll(
            createFieldGroup("Search:", searchField),
            createFieldGroup("Category:", categoryFilter),
            createFieldGroup("Sort:", sortCombo)
        );

        HBox bottomRow = new HBox(12);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.getChildren().addAll(searchBtn, clearBtn);

        // Result info row
        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        resultCountLabel = new Label("Showing 0 products");
        resultCountLabel.getStyleClass().add("label-muted");
        timingLabel = new Label("");
        timingLabel.getStyleClass().add("label-muted");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        infoRow.getChildren().addAll(resultCountLabel, spacer, timingLabel);

        searchCard.getChildren().addAll(topRow, bottomRow, infoRow);

        // Product Table
        productTable = new TableView<>();
        productTable.setMinHeight(400);
        VBox.setVgrow(productTable, Priority.ALWAYS);
        setupTable();

        // Pagination Bar
        HBox paginationBar = new HBox(15);
        paginationBar.setAlignment(Pos.CENTER);
        paginationBar.getStyleClass().add("pagination-bar");

        Button prevBtn = new Button("◀  Previous");
        prevBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadData();
            }
        });

        Button nextBtn = new Button("Next  ▶");
        nextBtn.setOnAction(e -> {
            if (currentPage * pageSize < totalProducts) {
                currentPage++;
                loadData();
            }
        });

        pageLabel = new Label("Page 1 of 1");
        pageLabel.getStyleClass().add("page-label");

        paginationBar.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(header, searchCard, productTable, paginationBar);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        this.getChildren().add(scrollPane);
        loadInitialData();
    }

    private VBox createFieldGroup(String labelText, javafx.scene.Node field) {
        VBox group = new VBox(4);
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        group.getChildren().addAll(label, field);
        return group;
    }

    /**
     * Sets up the TableView with columns: Name, Description, Price, Category, Stock.
     * All columns render data from the Product model.
     */
    private void setupTable() {
        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(220);

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        descCol.setPrefWidth(280);

        TableColumn<Product, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.format("$%.2f", data.getValue().getPrice()))
        );
        priceCol.setPrefWidth(100);
        priceCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Product, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        catCol.setPrefWidth(140);

        TableColumn<Product, String> stockStatusCol = new TableColumn<>("Stock");
        stockStatusCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStockQuantity())));
        stockStatusCol.setPrefWidth(140);
        stockStatusCol.setCellFactory(column -> new TableCell<Product, String>() {
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

        TableColumn<Product, String> ratingCol = new TableColumn<>("Rating");
        ratingCol.setPrefWidth(120);
        ratingCol.setCellValueFactory(data -> new SimpleStringProperty("⭐".repeat(3 + (data.getValue().getProductId() % 3)))); // Demo rating
        ratingCol.setCellFactory(column -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Hyperlink link = new Hyperlink(item);
                    link.setStyle("-fx-text-fill: #fbbf24; -fx-underline: false;");
                    link.setOnAction(e -> {
                        Product p = getTableView().getItems().get(getIndex());
                        new ReviewDialog(p.getProductId(), p.getName()).show();
                    });
                    setGraphic(link);
                }
            }
        });

        TableColumn<Product, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(140);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button cartBtn = new Button("Add to Cart");
            private final HBox container = new HBox(5, cartBtn);
            {
                cartBtn.getStyleClass().add("button-success");
                cartBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                cartBtn.setOnAction(event -> handleAddToCart(getTableView().getItems().get(getIndex())));
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(container);
            }
        });

        productTable.getColumns().addAll(nameCol, descCol, priceCol, catCol, stockStatusCol, actionCol, ratingCol);
        productTable.setItems(productList);
        productTable.setPlaceholder(new Label("No products found. Try adjusting your search."));
    }

    /**
     * Loads categories for the filter dropdown and then loads the first page of products.
     */
    private void loadInitialData() {
        try {
            categories = categoryDAO.getAllCategories();
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("All Categories");
            for (Category cat : categories) {
                categoryFilter.getItems().add(cat.getName());
            }
            categoryFilter.setValue("All Categories");
            loadData();
        } catch (SQLException e) {
            showError("Database Error", "Failed to load initial data: " + e.getMessage());
        }
    }

    /**
     * Loads product data based on current search/filter/page state.
     * Uses CacheService for fast repeated lookups.
     * Measures and displays query timing for performance awareness.
     */
    private void loadData() {
        try {
            String searchTerm = searchField.getText().trim();
            Integer catId = getSelectedCategoryId();

            totalProducts = productService.getSearchCount(searchTerm, catId);

            long startTime = System.nanoTime();
            List<Product> products = productService.searchProducts(
                searchTerm, catId, currentPage, pageSize, sortCombo.getValue()
            );
            long elapsed = (System.nanoTime() - startTime) / 1_000_000;

            // Record for performance monitoring
            PerformanceMonitor.record("Product Search", elapsed);

            productList.setAll(products);
            updatePaginationUI();

            resultCountLabel.setText(String.format("Showing %d of %d products", products.size(), totalProducts));
            timingLabel.setText(String.format("Query time: %dms", elapsed));
        } catch (SQLException e) {
            showError("Search Error", "Failed to load products: " + e.getMessage());
        }
    }

    /**
     * Gets the category ID from the selected category name.
     */
    private Integer getSelectedCategoryId() {
        String selectedCat = categoryFilter.getValue();
        if (selectedCat != null && !selectedCat.equals("All Categories") && categories != null) {
            for (Category cat : categories) {
                if (cat.getName().equals(selectedCat)) {
                    return cat.getCategoryId();
                }
            }
        }
        return null;
    }

    private void updatePaginationUI() {
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
        if (totalPages == 0) totalPages = 1;
        pageLabel.setText(String.format("Page %d of %d", currentPage, totalPages));
    }

    /**
     * Public method to refresh data (called after admin operations).
     */
    public void refreshData() {
        loadData();
    }

    private void handleAddToCart(Product product) {
        if (product.getStockQuantity() <= 0) {
            showError("Out of Stock", "This product is currently unavailable.");
            return;
        }
        try {
            cartService.addToCart(com.ecommerce.util.UserContext.getCurrentUserId(), product.getProductId(), 1);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(product.getName() + " added to your cart!");
            alert.show();
        } catch (java.sql.SQLException e) {
            showError("Cart Error", "Failed to add item to cart: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
