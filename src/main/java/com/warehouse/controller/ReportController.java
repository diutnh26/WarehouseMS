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

            // Type column with colored badges
            TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Type");
            colType.setPrefWidth(100);
            colType.setCellValueFactory(cellData -> {
                Object val = cellData.getValue().get("TransactionType");
                return new SimpleStringProperty(val != null ? val.toString() : "");
            });
            colType.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label badge = new Label(item);
                        badge.setStyle(
                            "-fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;" +
                            (item.equalsIgnoreCase("Import")
                                ? "-fx-background-color: rgba(59,130,246,0.15); -fx-text-fill: #3B82F6;"
                                : "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #EF4444;")
                        );
                        setGraphic(badge);
                        setText(null);
                    }
                }
            });
            tblMovementReport.getColumns().add(colType);

            // Date column — clean format
            TableColumn<Map<String, Object>, String> colDate = new TableColumn<>("Date");
            colDate.setPrefWidth(110);
            colDate.setCellValueFactory(cellData -> {
                Object val = cellData.getValue().get("TransactionDate");
                if (val != null) {
                    String s = val.toString();
                    return new SimpleStringProperty(s.length() >= 10 ? s.substring(0, 10) : s);
                }
                return new SimpleStringProperty("");
            });
            tblMovementReport.getColumns().add(colDate);

            addStringColumn(tblMovementReport, "Supplier", "SupplierName", 160);
            addStringColumn(tblMovementReport, "Code", "ProductCode", 100);
            addStringColumn(tblMovementReport, "Product", "ProductName", 200);
            addStringColumn(tblMovementReport, "Qty", "Quantity", 70);
            addStringColumn(tblMovementReport, "Unit Price", "UnitPrice", 120);
            addStringColumn(tblMovementReport, "Total", "LineTotal", 130);
            addStringColumn(tblMovementReport, "By", "CreatedBy", 130);
        }

        private void setupHistoryColumns() {
        tblProductHistory.getColumns().clear();

        // Type column with colored badges
        TableColumn<Map<String, Object>, String> colType = new TableColumn<>("Type");
        colType.setPrefWidth(100);
        colType.setCellValueFactory(cellData -> {
            Object val = cellData.getValue().get("TransactionType");
            return new SimpleStringProperty(val != null ? val.toString() : "");
        });
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(
                        "-fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        (item.equalsIgnoreCase("Import")
                            ? "-fx-background-color: rgba(59,130,246,0.15); -fx-text-fill: #3B82F6;"
                            : "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #EF4444;")
                    );
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        tblProductHistory.getColumns().add(colType);

        // Date column — clean format
        TableColumn<Map<String, Object>, String> colDate = new TableColumn<>("Date");
        colDate.setPrefWidth(110);
        colDate.setCellValueFactory(cellData -> {
            Object val = cellData.getValue().get("TransactionDate");
            if (val != null) {
                String s = val.toString();
                return new SimpleStringProperty(s.length() >= 10 ? s.substring(0, 10) : s);
            }
            return new SimpleStringProperty("");
        });
        tblProductHistory.getColumns().add(colDate);

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
