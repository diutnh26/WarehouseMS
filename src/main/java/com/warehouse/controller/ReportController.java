package com.warehouse.controller;

import com.warehouse.model.Product;
import com.warehouse.service.ProductService;
import com.warehouse.service.ReportService;
import com.warehouse.util.AlertUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportController implements Initializable {

    // Movement report
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TableView<Map<String, Object>> tblMovementReport;

    // Product history
    @FXML private ComboBox<Product> cmbProduct;
    @FXML private TableView<Map<String, Object>> tblProductHistory;

    private final ReportService reportService = new ReportService();
    private final ProductService productService = new ProductService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dpStartDate.setValue(LocalDate.now().minusMonths(1));
        dpEndDate.setValue(LocalDate.now());

        // Movement report columns (dynamic)
        setupMovementColumns();
        setupHistoryColumns();

        try {
            cmbProduct.setItems(FXCollections.observableArrayList(productService.getAllProducts()));
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    private void setupMovementColumns() {
        tblMovementReport.getColumns().clear();
        addStringColumn(tblMovementReport, "Type", "TransactionType", 80);
        addStringColumn(tblMovementReport, "Date", "TransactionDate", 140);
        addStringColumn(tblMovementReport, "Supplier", "SupplierName", 140);
        addStringColumn(tblMovementReport, "Product Code", "ProductCode", 100);
        addStringColumn(tblMovementReport, "Product Name", "ProductName", 160);
        addStringColumn(tblMovementReport, "Quantity", "Quantity", 80);
        addStringColumn(tblMovementReport, "Unit Price", "UnitPrice", 100);
        addStringColumn(tblMovementReport, "Line Total", "LineTotal", 100);
        addStringColumn(tblMovementReport, "Created By", "CreatedBy", 120);
    }

    private void setupHistoryColumns() {
        tblProductHistory.getColumns().clear();
        addStringColumn(tblProductHistory, "Type", "TransactionType", 80);
        addStringColumn(tblProductHistory, "Date", "TransactionDate", 140);
        addStringColumn(tblProductHistory, "Related Party", "RelatedParty", 160);
        addStringColumn(tblProductHistory, "Quantity", "Quantity", 80);
        addStringColumn(tblProductHistory, "Unit Price", "UnitPrice", 100);
        addStringColumn(tblProductHistory, "Created By", "CreatedBy", 120);
        addStringColumn(tblProductHistory, "Notes", "Notes", 180);
    }

    @SuppressWarnings("unchecked")
    private void addStringColumn(TableView<Map<String, Object>> table, String header, String key, double width) {
        TableColumn<Map<String, Object>, String> col = new TableColumn<>(header);
        col.setPrefWidth(width);
        col.setCellValueFactory(cellData -> {
            Object val = cellData.getValue().get(key);
            return new SimpleStringProperty(val != null ? val.toString() : "");
        });
        table.getColumns().add(col);
    }

    @FXML
    private void handleGenerateMovementReport() {
        if (dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
            AlertUtil.showWarning("Warning", "Please select both start and end dates.");
            return;
        }
        try {
            List<Map<String, Object>> results = reportService.getMovementReport(
                dpStartDate.getValue(), dpEndDate.getValue());
            tblMovementReport.setItems(FXCollections.observableArrayList(results));

            if (results.isEmpty()) {
                AlertUtil.showInfo("Report", "No transactions found in this date range.");
            }
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Validation", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleGenerateProductHistory() {
        Product product = cmbProduct.getSelectionModel().getSelectedItem();
        if (product == null) {
            AlertUtil.showWarning("Warning", "Please select a product.");
            return;
        }
        try {
            List<Map<String, Object>> results = reportService.getProductHistory(product.getProductId());
            tblProductHistory.setItems(FXCollections.observableArrayList(results));

            if (results.isEmpty()) {
                AlertUtil.showInfo("Report", "No movement history found for this product.");
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }
}
