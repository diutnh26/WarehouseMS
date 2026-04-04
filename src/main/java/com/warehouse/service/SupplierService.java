package com.warehouse.service;

import com.warehouse.dao.SupplierDAO;
import com.warehouse.model.Supplier;

import java.sql.SQLException;
import java.util.List;

public class SupplierService {

    private final SupplierDAO supplierDAO = new SupplierDAO();

    public List<Supplier> getAllSuppliers() throws SQLException {
        return supplierDAO.findAll();
    }

    public List<Supplier> searchSuppliers(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) return supplierDAO.findAll();
        return supplierDAO.search(keyword.trim());
    }

    public void addSupplier(Supplier supplier) throws SQLException {
        validate(supplier);
        supplierDAO.insert(supplier);
    }

    public void updateSupplier(Supplier supplier) throws SQLException {
        validate(supplier);
        supplierDAO.update(supplier);
    }

    public void deleteSupplier(int supplierId) throws SQLException {
        supplierDAO.delete(supplierId);
    }

    private void validate(Supplier s) {
        if (s.getSupplierName() == null || s.getSupplierName().isBlank())
            throw new IllegalArgumentException("Supplier name is required.");
    }
}
