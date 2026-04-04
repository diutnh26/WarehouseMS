package com.warehouse.dao;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.model.StockImport;
import com.warehouse.model.StockImportDetail;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockImportDAO {

    public List<StockImport> findAll() throws SQLException {
        List<StockImport> list = new ArrayList<>();
        String sql = "SELECT si.*, s.SupplierName, u.FullName AS CreatedByName " +
                     "FROM StockImports si " +
                     "INNER JOIN Suppliers s ON si.SupplierID = s.SupplierID " +
                     "INNER JOIN Users u ON si.CreatedByUserID = u.UserID " +
                     "ORDER BY si.ImportDate DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<StockImportDetail> findDetailsByImportId(int importId) throws SQLException {
        List<StockImportDetail> list = new ArrayList<>();
        String sql = "SELECT sid.*, p.ProductCode, p.ProductName " +
                     "FROM StockImportDetails sid " +
                     "INNER JOIN Products p ON sid.ProductID = p.ProductID " +
                     "WHERE sid.ImportID = ? ORDER BY sid.ImportDetailID";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, importId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StockImportDetail d = new StockImportDetail();
                d.setImportDetailId(rs.getInt("ImportDetailID"));
                d.setImportId(rs.getInt("ImportID"));
                d.setProductId(rs.getInt("ProductID"));
                d.setProductCode(rs.getString("ProductCode"));
                d.setProductName(rs.getString("ProductName"));
                d.setQuantity(rs.getInt("Quantity"));
                d.setUnitPrice(rs.getBigDecimal("UnitPrice"));
                list.add(d);
            }
        }
        return list;
    }

    /**
     * Inserts an import header + all detail lines, then calls sp_ProcessStockImport
     * to update inventory. Runs in a single transaction.
     */
    public int create(StockImport stockImport) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Insert header
            String headerSql = "INSERT INTO StockImports (SupplierID, ImportDate, Notes, CreatedByUserID) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(headerSql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, stockImport.getSupplierId());
            ps.setTimestamp(2, Timestamp.valueOf(stockImport.getImportDate()));
            ps.setString(3, stockImport.getNotes());
            ps.setInt(4, stockImport.getCreatedByUserId());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int importId = keys.getInt(1);
            ps.close();

            // Insert detail lines
            String detailSql = "INSERT INTO StockImportDetails (ImportID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
            ps = conn.prepareStatement(detailSql);
            for (StockImportDetail detail : stockImport.getDetails()) {
                ps.setInt(1, importId);
                ps.setInt(2, detail.getProductId());
                ps.setInt(3, detail.getQuantity());
                ps.setBigDecimal(4, detail.getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();

            // Process import (update inventory + total amount)
            CallableStatement cs = conn.prepareCall("{CALL sp_ProcessStockImport(?)}");
            cs.setInt(1, importId);
            cs.execute();
            cs.close();

            conn.commit();
            return importId;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private StockImport mapRow(ResultSet rs) throws SQLException {
        StockImport si = new StockImport();
        si.setImportId(rs.getInt("ImportID"));
        si.setSupplierId(rs.getInt("SupplierID"));
        si.setSupplierName(rs.getString("SupplierName"));
        si.setImportDate(rs.getTimestamp("ImportDate").toLocalDateTime());
        si.setTotalAmount(rs.getBigDecimal("TotalAmount"));
        si.setNotes(rs.getString("Notes"));
        si.setCreatedByUserId(rs.getInt("CreatedByUserID"));
        si.setCreatedByName(rs.getString("CreatedByName"));
        si.setCreatedDate(rs.getTimestamp("CreatedDate").toLocalDateTime());
        return si;
    }
}
