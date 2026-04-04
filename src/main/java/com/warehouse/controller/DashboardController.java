package com.warehouse.controller;
 
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.warehouse.service.InventoryService;
import com.warehouse.service.ReportService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SceneUtil;
import com.warehouse.util.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
 
public class DashboardController implements Initializable {
 
    @FXML private BorderPane contentArea;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @SuppressWarnings("unused") @FXML private VBox navMenu;
 
    // Dashboard stat labels
    @FXML private Label lblTotalProducts;
    @FXML private Label lblTotalSuppliers;
    @FXML private Label lblLowStock;
    @FXML private Label lblTodayImports;
    @FXML private Label lblTodayExports;
 
    // Recent Activity table
    @FXML private TableView<Map<String, Object>> tblRecentActivity;
 
    // Nav buttons (for role-based visibility)
    @SuppressWarnings("unused") @FXML private Button btnProducts;
    @SuppressWarnings("unused") @FXML private Button btnSuppliers;
    @SuppressWarnings("unused") @FXML private Button btnCategories;
    @SuppressWarnings("unused") @FXML private Button btnStockImport;
    @SuppressWarnings("unused") @FXML private Button btnStockExport;
    @SuppressWarnings("unused") @FXML private Button btnInventory;
    @FXML private Button btnReports;
    @FXML private Button btnUsers;
 
    private final InventoryService inventoryService = new InventoryService();
    private final ReportService reportService = new ReportService();
 
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblUserName.setText(SessionManager.getCurrentUser().getFullName());
        lblUserRole.setText(SessionManager.getCurrentRole().toUpperCase());
 
        configureMenuByRole();
        setupRecentActivityTable();
        refreshDashboardStats();
        loadRecentActivity();
    }
 
    @SuppressWarnings("unchecked")
    private void setupRecentActivityTable() {
        if (tblRecentActivity != null && tblRecentActivity.getColumns().size() >= 5) {
            TableColumn<Map<String, Object>, String> colType = (TableColumn<Map<String, Object>, String>) tblRecentActivity.getColumns().get(0);
            TableColumn<Map<String, Object>, String> colDate = (TableColumn<Map<String, Object>, String>) tblRecentActivity.getColumns().get(1);
            TableColumn<Map<String, Object>, String> colProducts = (TableColumn<Map<String, Object>, String>) tblRecentActivity.getColumns().get(2);
            TableColumn<Map<String, Object>, String> colCreatedBy = (TableColumn<Map<String, Object>, String>) tblRecentActivity.getColumns().get(3);
            TableColumn<Map<String, Object>, String> colStatus = (TableColumn<Map<String, Object>, String>) tblRecentActivity.getColumns().get(4);
 
            colType.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().get("TransactionType") != null ? c.getValue().get("TransactionType").toString() : ""));
            colDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().get("TransactionDate") != null ? c.getValue().get("TransactionDate").toString() : ""));
            colProducts.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().get("ProductName") != null ? c.getValue().get("ProductName").toString() : ""));
            colCreatedBy.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().get("CreatedBy") != null ? c.getValue().get("CreatedBy").toString() : ""));
            colStatus.setCellValueFactory(c -> new SimpleStringProperty("Completed"));
        }
    }
 
    private void loadRecentActivity() {
        try {
            if (tblRecentActivity != null) {
                List<Map<String, Object>> recent = reportService.getMovementReport(
                    LocalDate.now().minusMonths(3), LocalDate.now());
                // Show max 10 recent items
                int limit = Math.min(recent.size(), 10);
                tblRecentActivity.setItems(FXCollections.observableArrayList(recent.subList(0, limit)));
            }
        } catch (SQLException e) {
            AlertUtil.showError("Error", "Failed to load recent activity: " + e.getMessage());
        }
    }
 
    private void configureMenuByRole() {
        String role = SessionManager.getCurrentRole();
 
        switch (role) {
            case "Staff" -> {
                // Staff can't access reports or user management
                btnReports.setVisible(false);
                btnReports.setManaged(false);
                btnUsers.setVisible(false);
                btnUsers.setManaged(false);
            }
            case "Manager" -> {
                // Manager can view but not manage stock transactions
                btnUsers.setVisible(false);
                btnUsers.setManaged(false);
            }
            case "Admin" -> {
                // Admin sees everything
            }
            default -> {
                // Default case for unexpected roles
            }
        }
    }
 
    public void refreshDashboardStats() {
        try {
            Map<String, Integer> stats = inventoryService.getDashboardStats();
            lblTotalProducts.setText(String.valueOf(stats.getOrDefault("totalProducts", 0)));
            lblTotalSuppliers.setText(String.valueOf(stats.getOrDefault("totalSuppliers", 0)));
            lblLowStock.setText(String.valueOf(stats.getOrDefault("lowStockCount", 0)));
            lblTodayImports.setText(String.valueOf(stats.getOrDefault("todayImports", 0)));
            lblTodayExports.setText(String.valueOf(stats.getOrDefault("todayExports", 0)));
        } catch (SQLException e) {
            AlertUtil.showError("Error", "Failed to refresh dashboard stats: " + e.getMessage());
        }
    }
 
    // --- Navigation handlers ---
 
    @SuppressWarnings("unused") @FXML
    private void showDashboard() {
        refreshDashboardStats();
        contentArea.setCenter(null); // Show the default dashboard content
    }
 
    @SuppressWarnings("unused") @FXML
    private void showProducts() {
        loadPage("product.fxml");
    }
 
    @SuppressWarnings("unused") @FXML
    private void showSuppliers() {
        loadPage("supplier.fxml");
    }
 
    @SuppressWarnings("unused") @FXML
    private void showCategories() {
        loadPage("category.fxml");
    }
 
    @SuppressWarnings("unused") @FXML
    private void showStockImport() {
        loadPage("stock-import.fxml");
    }
 
    @SuppressWarnings("unused") @FXML
    private void showStockExport() {
        loadPage("stock-export.fxml");
    }
 
    @SuppressWarnings("unused") @FXML
    private void showInventory() {
        loadPage("inventory.fxml");
    }
 
    @SuppressWarnings("unused") @FXML
    private void showReports() {
        loadPage("report.fxml");
    }
 
    @SuppressWarnings("unused") @FXML
    private void showUsers() {
        loadPage("user-management.fxml");
    }
 
    @SuppressWarnings("unused") @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirm("Logout", "Are you sure you want to logout?")) {
            SessionManager.logout();
            try {
                SceneUtil.switchScene("login.fxml", "Login", 400, 500);
            } catch (IOException e) {
                AlertUtil.showError("Error", "Failed to logout: " + e.getMessage());
            }
        }
    }
 
    private void loadPage(String fxml) {
        try {
            SceneUtil.loadContent(contentArea, fxml);
        } catch (IOException e) {
            AlertUtil.showError("Error", "Failed to load page: " + e.getMessage());
        }
    }
}