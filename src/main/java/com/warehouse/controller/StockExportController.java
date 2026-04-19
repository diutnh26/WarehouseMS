package com.warehouse.controller;

import com.warehouse.model.*;
import com.warehouse.service.ProductService;
import com.warehouse.service.StockService;
import com.warehouse.service.InventoryService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class StockExportController implements Initializable {

    @FXML private TableView<StockExport> tblExports;
    @FXML private TableColumn<StockExport, Integer> colExportId;
    @FXML private TableColumn<StockExport, String> colDate;
    @FXML private TableColumn<StockExport, String> colReason;
    @FXML private TableColumn<StockExport, String> colCreatedBy;

    @FXML private TableView<StockExportDetail> tblDetails;
    @FXML private TableColumn<StockExportDetail, String> colDetailProduct;
    @FXML private TableColumn<StockExportDetail, Integer> colDetailQty;

    @FXML private DatePicker dpExportDate;
    @FXML private TextField txtReason;
    @FXML private TextArea txtNotes;
    @FXML private ComboBox<Product> cmbProduct;
    @FXML private TextField txtQuantity;

    private final StockService stockService = new StockService();
    private final ProductService productService = new ProductService();
    private final ObservableList<StockExport> exportList = FXCollections.observableArrayList();
    private final ObservableList<StockExportDetail> detailList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colExportId.setCellValueFactory(new PropertyValueFactory<>("exportId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("exportDate"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colCreatedBy.setCellValueFactory(new PropertyValueFactory<>("createdByName"));
        tblExports.setItems(exportList);

        colDetailProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colDetailQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tblDetails.setItems(detailList);

        tblExports.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) loadExportDetails(n.getExportId());
        });

        dpExportDate.setValue(LocalDate.now());
        tblExports.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblDetails.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadComboBoxes();
        loadExports();
    }

    private void loadComboBoxes() {
        try { cmbProduct.setItems(FXCollections.observableArrayList(productService.getAllProducts())); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    private void loadExports() {
        try { exportList.setAll(stockService.getAllExports()); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    private void loadExportDetails(int exportId) {
        try { detailList.setAll(stockService.getExportDetails(exportId)); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML
    private void handleAddLine() {
        Product product = cmbProduct.getSelectionModel().getSelectedItem();
        if (product == null) { AlertUtil.showWarning("Warning", "Select a product."); return; }
        try {
            int qty = Integer.parseInt(txtQuantity.getText().trim());
            if (qty <= 0) { AlertUtil.showWarning("Warning", "Quantity must be positive."); return; }

            StockExportDetail detail = new StockExportDetail();
            detail.setProductId(product.getProductId());
            detail.setProductCode(product.getProductCode());
            detail.setProductName(product.getProductName());
            detail.setQuantity(qty);
            detailList.add(detail);

            cmbProduct.getSelectionModel().clearSelection();
            txtQuantity.clear();
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Warning", "Enter a valid quantity.");
        }
    }

    @FXML
    private void handleRemoveLine() {
        StockExportDetail selected = tblDetails.getSelectionModel().getSelectedItem();
        if (selected != null) detailList.remove(selected);
    }

    @FXML
    private void handleSaveExport() {
        if (dpExportDate.getValue() == null) { AlertUtil.showWarning("Warning", "Select a date."); return; }
        if (detailList.isEmpty()) { AlertUtil.showWarning("Warning", "Add at least one product line."); return; }

        try {
            StockExport se = new StockExport();
            se.setExportDate(dpExportDate.getValue().atStartOfDay());
            se.setReason(txtReason.getText().trim());
            se.setNotes(txtNotes.getText());
            se.setCreatedByUserId(SessionManager.getCurrentUserId());
            se.setDetails(new java.util.ArrayList<>(detailList));

            int id = stockService.createExport(se);
            AlertUtil.showInfo("Success", "Export #" + id + " saved and inventory updated.");
            clearForm();
            loadExports();
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to save export: " + e.getMessage());
        }
    }

    @FXML private void handleClear() { clearForm(); }

    private void clearForm() {
        dpExportDate.setValue(LocalDate.now());
        txtReason.clear(); txtNotes.clear(); detailList.clear();
        tblExports.getSelectionModel().clearSelection();
    }
}
