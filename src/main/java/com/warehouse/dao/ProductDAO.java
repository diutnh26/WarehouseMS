package com.warehouse.dao;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final String SELECT_WITH_CATEGORY =
        "SELECT p.*, c.CategoryName, ISNULL(i.CurrentQuantity, 0) AS CurrentQuantity " +
        "FROM Products p " +
        "LEFT JOIN Categories c ON p.CategoryID = c.CategoryID " +
        "LEFT JOIN Inventory i ON p.ProductID = i.ProductID";

    public List<Product> findAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_WITH_CATEGORY + " WHERE p.IsActive = 1 ORDER BY p.ProductCode";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Product> search(String keyword) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_WITH_CATEGORY +
            " WHERE p.IsActive = 1 AND (p.ProductCode LIKE ? OR p.ProductName LIKE ?)" +
            " ORDER BY p.ProductCode";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Product findById(int productId) throws SQLException {
        String sql = SELECT_WITH_CATEGORY + " WHERE p.ProductID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }

    public void insert(Product product) throws SQLException {
        String sql = "INSERT INTO Products (ProductCode, ProductName, CategoryID, Unit, MinStockLevel, Description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getProductCode());
            ps.setString(2, product.getProductName());
            ps.setInt(3, product.getCategoryId());
            ps.setString(4, product.getUnit());
            ps.setInt(5, product.getMinStockLevel());
            ps.setString(6, product.getDescription());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                product.setProductId(keys.getInt(1));
            }

            // Ensure inventory row exists
            ensureInventoryRow(conn, product.getProductId());
        }
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE Products SET ProductCode = ?, ProductName = ?, CategoryID = ?, Unit = ?, MinStockLevel = ?, Description = ? WHERE ProductID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductCode());
            ps.setString(2, product.getProductName());
            ps.setInt(3, product.getCategoryId());
            ps.setString(4, product.getUnit());
            ps.setInt(5, product.getMinStockLevel());
            ps.setString(6, product.getDescription());
            ps.setInt(7, product.getProductId());
            ps.executeUpdate();
        }
    }

    public void delete(int productId) throws SQLException {
        String sql = "UPDATE Products SET IsActive = 0 WHERE ProductID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        }
    }

    public boolean isCodeExists(String productCode, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Products WHERE ProductCode = ? AND ProductID != ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productCode);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private void ensureInventoryRow(Connection conn, int productId) throws SQLException {
        String sql = "IF NOT EXISTS (SELECT 1 FROM Inventory WHERE ProductID = ?) " +
                     "INSERT INTO Inventory (ProductID, CurrentQuantity) VALUES (?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("ProductID"));
        p.setProductCode(rs.getString("ProductCode"));
        p.setProductName(rs.getString("ProductName"));
        p.setCategoryId(rs.getInt("CategoryID"));
        p.setCategoryName(rs.getString("CategoryName"));
        p.setUnit(rs.getString("Unit"));
        p.setMinStockLevel(rs.getInt("MinStockLevel"));
        p.setDescription(rs.getString("Description"));
        p.setActive(rs.getBoolean("IsActive"));
        p.setCreatedDate(rs.getTimestamp("CreatedDate").toLocalDateTime());
        p.setCurrentQuantity(rs.getInt("CurrentQuantity"));
        return p;
    }
}
