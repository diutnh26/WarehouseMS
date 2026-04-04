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
    @FXML private TableColumn<Inventory, String> colLastUpdated;

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

        // Color-code rows by stock status
        tblInventory.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Inventory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getCurrentQuantity() == 0) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else if (item.isLowStock()) {
                    setStyle("-fx-background-color: #fff3cd;");
                } else {
                    setStyle("");
                }
            }
        });

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
