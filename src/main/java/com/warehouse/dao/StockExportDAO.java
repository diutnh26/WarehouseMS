package com.warehouse.dao;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.model.StockExport;
import com.warehouse.model.StockExportDetail;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockExportDAO {

    public List<StockExport> findAll() throws SQLException {
        List<StockExport> list = new ArrayList<>();
        String sql = "SELECT se.*, u.FullName AS CreatedByName " +
                     "FROM StockExports se " +
                     "INNER JOIN Users u ON se.CreatedByUserID = u.UserID " +
                     "ORDER BY se.ExportDate DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<StockExportDetail> findDetailsByExportId(int exportId) throws SQLException {
        List<StockExportDetail> list = new ArrayList<>();
        String sql = "SELECT sed.*, p.ProductCode, p.ProductName " +
                     "FROM StockExportDetails sed " +
                     "INNER JOIN Products p ON sed.ProductID = p.ProductID " +
                     "WHERE sed.ExportID = ? ORDER BY sed.ExportDetailID";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, exportId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StockExportDetail d = new StockExportDetail();
                d.setExportDetailId(rs.getInt("ExportDetailID"));
                d.setExportId(rs.getInt("ExportID"));
                d.setProductId(rs.getInt("ProductID"));
                d.setProductCode(rs.getString("ProductCode"));
                d.setProductName(rs.getString("ProductName"));
                d.setQuantity(rs.getInt("Quantity"));
                list.add(d);
            }
        }
        return list;
    }

    /**
     * Inserts an export header + detail lines, then calls sp_ProcessStockExport.
     */
    public int create(StockExport stockExport) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Insert header
            String headerSql = "INSERT INTO StockExports (ExportDate, Reason, Notes, CreatedByUserID) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(headerSql, Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, Timestamp.valueOf(stockExport.getExportDate()));
            ps.setString(2, stockExport.getReason());
            ps.setString(3, stockExport.getNotes());
            ps.setInt(4, stockExport.getCreatedByUserId());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int exportId = keys.getInt(1);
            ps.close();

            // Insert detail lines
            String detailSql = "INSERT INTO StockExportDetails (ExportID, ProductID, Quantity) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(detailSql);
            for (StockExportDetail detail : stockExport.getDetails()) {
                ps.setInt(1, exportId);
                ps.setInt(2, detail.getProductId());
                ps.setInt(3, detail.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();

            // Process export (update inventory)
            CallableStatement cs = conn.prepareCall("{CALL sp_ProcessStockExport(?)}");
            cs.setInt(1, exportId);
            cs.execute();
            cs.close();

            conn.commit();
            return exportId;

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

    private StockExport mapRow(ResultSet rs) throws SQLException {
        StockExport se = new StockExport();
        se.setExportId(rs.getInt("ExportID"));
        se.setExportDate(rs.getTimestamp("ExportDate").toLocalDateTime());
        se.setReason(rs.getString("Reason"));
        se.setNotes(rs.getString("Notes"));
        se.setCreatedByUserId(rs.getInt("CreatedByUserID"));
        se.setCreatedByName(rs.getString("CreatedByName"));
        se.setCreatedDate(rs.getTimestamp("CreatedDate").toLocalDateTime());
        return se;
    }
}
