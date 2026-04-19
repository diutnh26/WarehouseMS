package com.warehouse.controller;

import com.warehouse.model.Inventory;
import com.warehouse.service.InventoryService;
import com.warehouse.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;

import java.net.URL;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {

    @FXML private TableView<Inventory> tblInventory;
    @FXML private TableColumn<Inventory, String> colCode;
    @FXML private TableColumn<Inventory, String> colName;
    @FXML private TableColumn<Inventory, String> colCategory;
    @FXML private TableColumn<Inventory, String> colUnit;
    @FXML private TableColumn<Inventory, Integer> colCurrentQty;
    @FXML private TableColumn<Inventory, Integer> colMinStock;
    @FXML private TableColumn<Inventory, String> colStatus;
    @FXML private TableColumn<Inventory, Object> colLastUpdated;

    @FXML private TextField txtSearch;
    @FXML private CheckBox chkLowStockOnly;

    private final InventoryService inventoryService = new InventoryService();
    private final ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();

    @Override
        public void initialize(URL location, ResourceBundle resources) {
            colCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
            colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
            colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
            colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
            colCurrentQty.setCellValueFactory(new PropertyValueFactory<>("currentQuantity"));
            colMinStock.setCellValueFactory(new PropertyValueFactory<>("minStockLevel"));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("stockStatus"));
            colLastUpdated.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));

            tblInventory.setItems(inventoryList);

            // Code column: blue text, red for out of stock
            colCode.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                        return;
                    }
                    setText(item);
                    int idx = getIndex();
                    if (idx >= 0 && idx < tblInventory.getItems().size()) {
                        Inventory inv = tblInventory.getItems().get(idx);
                        if (inv.getCurrentQuantity() == 0) {
                            setStyle("-fx-text-fill: #EF4444; -fx-font-family: 'Consolas';");
                        } else {
                            setStyle("-fx-text-fill: #3B82F6; -fx-font-family: 'Consolas';");
                        }
                    }
                }
            });

            // Status column: colored badges
            colStatus.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    String style;
                    if ("Out of Stock".equals(item)) {
                        style = "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #EF4444;";
                    } else if ("Low Stock".equals(item)) {
                        style = "-fx-background-color: rgba(245,158,11,0.15); -fx-text-fill: #F59E0B;";
                    } else {
                        style = "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10B981;";
                    }
                    Label badge = new Label(item);
                    badge.setStyle(style + "-fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
                    setGraphic(badge);
                    setText(null);
                }
            });

            // Last Updated: clean date only
            colLastUpdated.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        return;
                    }
                    String dateStr = item.toString();
                    if (dateStr.length() >= 10) {
                        dateStr = dateStr.substring(0, 10);
                    }
                    setText(dateStr);
                }
            });

            // No row-level background coloring — use badges instead
            tblInventory.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(Inventory item, boolean empty) {
                    super.updateItem(item, empty);
                    setStyle("");
                }
            });

            tblInventory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            txtSearch.textProperty().addListener((obs, o, n) -> handleSearch());
            chkLowStockOnly.selectedProperty().addListener((obs, o, n) -> handleSearch());

            loadInventory();
        }

        private void loadInventory() {
            try { inventoryList.setAll(inventoryService.getAllInventory()); }
            catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
        }

    @FXML
    private void handleSearch() {
        try {
            if (chkLowStockOnly.isSelected()) {
                inventoryList.setAll(inventoryService.getLowStockItems());
            } else {
                inventoryList.setAll(inventoryService.searchInventory(txtSearch.getText()));
            }
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML
    private void handleRefresh() {
        txtSearch.clear();
        chkLowStockOnly.setSelected(false);
        loadInventory();
    }
}
