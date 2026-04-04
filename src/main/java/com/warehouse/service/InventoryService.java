package com.warehouse.service;

import com.warehouse.dao.InventoryDAO;
import com.warehouse.model.Inventory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class InventoryService {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public List<Inventory> getAllInventory() throws SQLException {
        return inventoryDAO.findAll();
    }

    public List<Inventory> getLowStockItems() throws SQLException {
        return inventoryDAO.findLowStock();
    }

    public List<Inventory> searchInventory(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) return inventoryDAO.findAll();
        return inventoryDAO.search(keyword.trim());
    }

    public Map<String, Integer> getDashboardStats() throws SQLException {
        return inventoryDAO.getDashboardStats();
    }
}
