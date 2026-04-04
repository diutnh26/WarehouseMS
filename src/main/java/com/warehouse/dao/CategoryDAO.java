package com.warehouse.dao;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM Categories ORDER BY CategoryName";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void insert(Category category) throws SQLException {
        String sql = "INSERT INTO Categories (CategoryName, Description) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) category.setCategoryId(keys.getInt(1));
        }
    }

    public void update(Category category) throws SQLException {
        String sql = "UPDATE Categories SET CategoryName = ?, Description = ? WHERE CategoryID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getCategoryId());
            ps.executeUpdate();
        }
    }

    public void delete(int categoryId) throws SQLException {
        String sql = "DELETE FROM Categories WHERE CategoryID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.executeUpdate();
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("CategoryID"));
        c.setCategoryName(rs.getString("CategoryName"));
        c.setDescription(rs.getString("Description"));
        return c;
    }
}
