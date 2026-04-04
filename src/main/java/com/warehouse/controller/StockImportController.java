package com.warehouse.controller;

import com.warehouse.model.*;
import com.warehouse.service.ProductService;
import com.warehouse.service.StockService;
import com.warehouse.service.SupplierService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class StockImportController implements Initializable {

    // Import history table
    @FXML private TableView<StockImport> tblImports;
    @FXML private TableColumn<StockImport, Integer> colImportId;
    @FXML private TableColumn<StockImport, String> colSupplier;
    @FXML private TableColumn<StockImport, String> colDate;
    @FXML private TableColumn<StockImport, BigDecimal> colTotal;
    @FXML private TableColumn<StockImport, String> colCreatedBy;

    // Detail table (line items for selected import or new import)
    @FXML private TableView<StockImportDetail> tblDetails;
    @FXML private TableColumn<StockImportDetail, String> colDetailProduct;
    @FXML private TableColumn<StockImportDetail, Integer> colDetailQty;
    @FXML private TableColumn<StockImportDetail, BigDecimal> colDetailPrice;
    @FXML private TableColumn<StockImportDetail, BigDecimal> colDetailTotal;

    // Form fields for new import
    @FXML private ComboBox<Supplier> cmbSupplier;
    @FXML private DatePicker dpImportDate;
    @FXML private TextArea txtNotes;

    // Line item entry
    @FXML private ComboBox<Product> cmbProduct;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtUnitPrice;

    private final StockService stockService = new StockService();
    private final SupplierService supplierService = new SupplierService();
    private final ProductService productService = new ProductService();
    private final ObservableList<StockImport> importList = FXCollections.observableArrayList();
    private final ObservableList<StockImportDetail> detailList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Import history columns
        colImportId.setCellValueFactory(new PropertyValueFactory<>("importId"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("importDate"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdByName"));
        tblImports.setItems(importList);

        // Detail columns
        colDetailProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colDetailQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDetailPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colDetailTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
        tblDetails.setItems(detailList);

        // Show details when selecting an import
        tblImports.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) loadImportDetails(n.getImportId());
        });

        dpImportDate.setValue(LocalDate.now());

        loadComboBoxes();
        loadImports();
    }

    private void loadComboBoxes() {
        try {
            cmbSupplier.setItems(FXCollections.observableArrayList(supplierService.getAllSuppliers()));
            cmbProduct.setItems(FXCollections.observableArrayList(productService.getAllProducts()));
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    private void loadImports() {
        try { importList.setAll(stockService.getAllImports()); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    private void loadImportDetails(int importId) {
        try { detailList.setAll(stockService.getImportDetails(importId)); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML
    private void handleAddLine() {
        Product product = cmbProduct.getSelectionModel().getSelectedItem();
        if (product == null) { AlertUtil.showWarning("Warning", "Select a product."); return; }

        try {
            int qty = Integer.parseInt(txtQuantity.getText().trim());
            BigDecimal price = new BigDecimal(txtUnitPrice.getText().trim());

            if (qty <= 0 || price.signum() < 0) {
                AlertUtil.showWarning("Warning", "Quantity and price must be positive.");
                return;
            }

            StockImportDetail detail = new StockImportDetail();
            detail.setProductId(product.getProductId());
            detail.setProductCode(product.getProductCode());
            detail.setProductName(product.getProductName());
            detail.setQuantity(qty);
            detail.setUnitPrice(price);
            detailList.add(detail);

            cmbProduct.getSelectionModel().clearSelection();
            txtQuantity.clear();
            txtUnitPrice.clear();
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Warning", "Please enter valid numbers for quantity and price.");
        }
    }

    @FXML
    private void handleRemoveLine() {
        StockImportDetail selected = tblDetails.getSelectionModel().getSelectedItem();
        if (selected != null) detailList.remove(selected);
    }

    @FXML
    private void handleSaveImport() {
        Supplier supplier = cmbSupplier.getSelectionModel().getSelectedItem();
        if (supplier == null) { AlertUtil.showWarning("Warning", "Select a supplier."); return; }
        if (dpImportDate.getValue() == null) { AlertUtil.showWarning("Warning", "Select a date."); return; }
        if (detailList.isEmpty()) { AlertUtil.showWarning("Warning", "Add at least one product line."); return; }

        try {
            StockImport si = new StockImport();
            si.setSupplierId(supplier.getSupplierId());
            si.setImportDate(dpImportDate.getValue().atStartOfDay());
            si.setNotes(txtNotes.getText());
            si.setCreatedByUserId(SessionManager.getCurrentUserId());
            si.setDetails(new java.util.ArrayList<>(detailList));

            int id = stockService.createImport(si);
            AlertUtil.showInfo("Success", "Import #" + id + " saved and inventory updated.");
            clearForm();
            loadImports();
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to save import: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() { clearForm(); }

    private void clearForm() {
        cmbSupplier.getSelectionModel().clearSelection();
        dpImportDate.setValue(LocalDate.now());
        txtNotes.clear();
        detailList.clear();
        tblImports.getSelectionModel().clearSelection();
    }
}
