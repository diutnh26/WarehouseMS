package com.warehouse.dao;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> findAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM Suppliers WHERE IsActive = 1 ORDER BY SupplierName";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Supplier> search(String keyword) throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM Suppliers WHERE IsActive = 1 AND (SupplierName LIKE ? OR ContactPerson LIKE ? OR Phone LIKE ?) ORDER BY SupplierName";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void insert(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO Suppliers (SupplierName, ContactPerson, Phone, Email, Address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getContactPerson());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) supplier.setSupplierId(keys.getInt(1));
        }
    }

    public void update(Supplier supplier) throws SQLException {
        String sql = "UPDATE Suppliers SET SupplierName = ?, ContactPerson = ?, Phone = ?, Email = ?, Address = ? WHERE SupplierID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getContactPerson());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setInt(6, supplier.getSupplierId());
            ps.executeUpdate();
        }
    }

    public void delete(int supplierId) throws SQLException {
        String sql = "UPDATE Suppliers SET IsActive = 0 WHERE SupplierID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.executeUpdate();
        }
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("SupplierID"));
        s.setSupplierName(rs.getString("SupplierName"));
        s.setContactPerson(rs.getString("ContactPerson"));
        s.setPhone(rs.getString("Phone"));
        s.setEmail(rs.getString("Email"));
        s.setAddress(rs.getString("Address"));
        s.setActive(rs.getBoolean("IsActive"));
        s.setCreatedDate(rs.getTimestamp("CreatedDate").toLocalDateTime());
        return s;
    }
}
