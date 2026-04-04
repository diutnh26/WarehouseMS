package com.warehouse.service;

import com.warehouse.dao.InventoryDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportService {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public List<Map<String, Object>> getMovementReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Both start date and end date are required.");
        if (startDate.isAfter(endDate))
            throw new IllegalArgumentException("Start date cannot be after end date.");
        return inventoryDAO.getMovementReport(startDate, endDate);
    }

    public List<Map<String, Object>> getProductHistory(int productId) throws SQLException {
        if (productId <= 0)
            throw new IllegalArgumentException("Please select a product.");
        return inventoryDAO.getProductHistory(productId);
    }
}
