package com.warehouse.dao;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.model.Inventory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryDAO {

    public List<Inventory> findAll() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT i.*, p.ProductCode, p.ProductName, p.Unit, p.MinStockLevel, c.CategoryName " +
                     "FROM Inventory i " +
                     "INNER JOIN Products p ON i.ProductID = p.ProductID " +
                     "LEFT JOIN Categories c ON p.CategoryID = c.CategoryID " +
                     "WHERE p.IsActive = 1 ORDER BY p.ProductCode";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Inventory> findLowStock() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT i.*, p.ProductCode, p.ProductName, p.Unit, p.MinStockLevel, c.CategoryName " +
                     "FROM Inventory i " +
                     "INNER JOIN Products p ON i.ProductID = p.ProductID " +
                     "LEFT JOIN Categories c ON p.CategoryID = c.CategoryID " +
                     "WHERE p.IsActive = 1 AND i.CurrentQuantity < p.MinStockLevel " +
                     "ORDER BY (p.MinStockLevel - i.CurrentQuantity) DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Inventory> search(String keyword) throws SQLException {
        List<Inventory> list = new ArrayList<>();
        String sql = "SELECT i.*, p.ProductCode, p.ProductName, p.Unit, p.MinStockLevel, c.CategoryName " +
                     "FROM Inventory i " +
                     "INNER JOIN Products p ON i.ProductID = p.ProductID " +
                     "LEFT JOIN Categories c ON p.CategoryID = c.CategoryID " +
                     "WHERE p.IsActive = 1 AND (p.ProductCode LIKE ? OR p.ProductName LIKE ?) " +
                     "ORDER BY p.ProductCode";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Calls sp_InventoryReportByDate and returns results as a list of maps.
     */
    public List<Map<String, Object>> getMovementReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_InventoryReportByDate(?, ?)}")) {
            cs.setDate(1, Date.valueOf(startDate));
            cs.setDate(2, Date.valueOf(endDate));
            ResultSet rs = cs.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        return results;
    }

    /**
     * Calls sp_ProductMovementHistory for a single product.
     */
    public List<Map<String, Object>> getProductHistory(int productId) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_ProductMovementHistory(?)}")) {
            cs.setInt(1, productId);
            ResultSet rs = cs.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        return results;
    }

    /**
     * Returns dashboard statistics as a map.
     */
    public Map<String, Integer> getDashboardStats() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_GetDashboardStats}")) {
            boolean hasResults = cs.execute();

            // Result set 1: TotalProducts
            if (hasResults) {
                ResultSet rs = cs.getResultSet();
                if (rs.next()) stats.put("totalProducts", rs.getInt(1));
                hasResults = cs.getMoreResults();
            }
            // Result set 2: TotalSuppliers
            if (hasResults) {
                ResultSet rs = cs.getResultSet();
                if (rs.next()) stats.put("totalSuppliers", rs.getInt(1));
                hasResults = cs.getMoreResults();
            }
            // Result set 3: LowStockCount
            if (hasResults) {
                ResultSet rs = cs.getResultSet();
                if (rs.next()) stats.put("lowStockCount", rs.getInt(1));
                hasResults = cs.getMoreResults();
            }
            // Result set 4: TodayImports
            if (hasResults) {
                ResultSet rs = cs.getResultSet();
                if (rs.next()) stats.put("todayImports", rs.getInt(1));
                hasResults = cs.getMoreResults();
            }
            // Result set 5: TodayExports
            if (hasResults) {
                ResultSet rs = cs.getResultSet();
                if (rs.next()) stats.put("todayExports", rs.getInt(1));
            }
        }
        return stats;
    }

    private Inventory mapRow(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setInventoryId(rs.getInt("InventoryID"));
        inv.setProductId(rs.getInt("ProductID"));
        inv.setProductCode(rs.getString("ProductCode"));
        inv.setProductName(rs.getString("ProductName"));
        inv.setCategoryName(rs.getString("CategoryName"));
        inv.setUnit(rs.getString("Unit"));
        inv.setCurrentQuantity(rs.getInt("CurrentQuantity"));
        inv.setMinStockLevel(rs.getInt("MinStockLevel"));
        inv.setLastUpdated(rs.getTimestamp("LastUpdated").toLocalDateTime());
        return inv;
    }
}
