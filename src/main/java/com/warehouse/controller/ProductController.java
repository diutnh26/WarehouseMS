package com.warehouse.controller;

import com.warehouse.dao.CategoryDAO;
import com.warehouse.model.Category;
import com.warehouse.model.Product;
import com.warehouse.service.ProductService;
import com.warehouse.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    // Table
    @FXML private TableView<Product> tblProducts;
    @FXML private TableColumn<Product, String> colCode;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colUnit;
    @FXML private TableColumn<Product, Integer> colMinStock;
    @FXML private TableColumn<Product, Integer> colCurrentQty;

    // Form fields
    @FXML private TextField txtProductCode;
    @FXML private TextField txtProductName;
    @FXML private ComboBox<Category> cmbCategory;
    @FXML private TextField txtUnit;
    @FXML private TextField txtMinStockLevel;
    @FXML private TextArea txtDescription;

    // Search
    @FXML private TextField txtSearch;

    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private Product selectedProduct = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind table columns
        colCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colMinStock.setCellValueFactory(new PropertyValueFactory<>("minStockLevel"));
        colCurrentQty.setCellValueFactory(new PropertyValueFactory<>("currentQuantity"));

        tblProducts.setItems(productList);

        // Row selection listener
        tblProducts.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) populateForm(newVal);
            }
        );

        // Search on key typed
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        // Load data
        loadCategories();
        loadProducts();
    }

    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productList.setAll(products);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.findAll();
            cmbCategory.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void populateForm(Product product) {
        selectedProduct = product;
        txtProductCode.setText(product.getProductCode());
        txtProductName.setText(product.getProductName());
        txtUnit.setText(product.getUnit());
        txtMinStockLevel.setText(String.valueOf(product.getMinStockLevel()));
        txtDescription.setText(product.getDescription());

        // Select matching category in ComboBox
        for (Category cat : cmbCategory.getItems()) {
            if (cat.getCategoryId() == product.getCategoryId()) {
                cmbCategory.getSelectionModel().select(cat);
                break;
            }
        }
    }

    @FXML
    private void handleAdd() {
        try {
            Product product = buildProductFromForm();
            productService.addProduct(product);
            AlertUtil.showInfo("Success", "Product added successfully.");
            clearForm();
            loadProducts();
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Validation", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to add product: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedProduct == null) {
            AlertUtil.showWarning("Warning", "Please select a product to update.");
            return;
        }
        try {
            Product product = buildProductFromForm();
            product.setProductId(selectedProduct.getProductId());
            productService.updateProduct(product);
            AlertUtil.showInfo("Success", "Product updated successfully.");
            clearForm();
            loadProducts();
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Validation", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to update product: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedProduct == null) {
            AlertUtil.showWarning("Warning", "Please select a product to delete.");
            return;
        }
        if (AlertUtil.showConfirm("Confirm Delete",
                "Are you sure you want to delete product: " + selectedProduct.getProductName() + "?")) {
            try {
                productService.deleteProduct(selectedProduct.getProductId());
                AlertUtil.showInfo("Success", "Product deleted.");
                clearForm();
                loadProducts();
            } catch (Exception e) {
                AlertUtil.showError("Error", "Failed to delete product: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSearch() {
        try {
            String keyword = txtSearch.getText();
            List<Product> results = productService.searchProducts(keyword);
            productList.setAll(results);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private Product buildProductFromForm() {
        Product p = new Product();
        p.setProductCode(txtProductCode.getText().trim());
        p.setProductName(txtProductName.getText().trim());
        p.setUnit(txtUnit.getText().trim().isEmpty() ? "Piece" : txtUnit.getText().trim());
        p.setDescription(txtDescription.getText());

        Category selected = cmbCategory.getSelectionModel().getSelectedItem();
        p.setCategoryId(selected != null ? selected.getCategoryId() : 0);

        try {
            p.setMinStockLevel(Integer.parseInt(txtMinStockLevel.getText().trim()));
        } catch (NumberFormatException e) {
            p.setMinStockLevel(10);
        }

        return p;
    }

    private void clearForm() {
        selectedProduct = null;
        txtProductCode.clear();
        txtProductName.clear();
        cmbCategory.getSelectionModel().clearSelection();
        txtUnit.clear();
        txtMinStockLevel.clear();
        txtDescription.clear();
        tblProducts.getSelectionModel().clearSelection();
    }
}
