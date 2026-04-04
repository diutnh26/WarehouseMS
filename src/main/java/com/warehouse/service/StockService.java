package com.warehouse.service;

import com.warehouse.dao.StockExportDAO;
import com.warehouse.dao.StockImportDAO;
import com.warehouse.model.*;

import java.sql.SQLException;
import java.util.List;

public class StockService {

    private final StockImportDAO importDAO = new StockImportDAO();
    private final StockExportDAO exportDAO = new StockExportDAO();

    // --- Imports ---

    public List<StockImport> getAllImports() throws SQLException {
        return importDAO.findAll();
    }

    public List<StockImportDetail> getImportDetails(int importId) throws SQLException {
        return importDAO.findDetailsByImportId(importId);
    }

    public int createImport(StockImport stockImport) throws SQLException {
        validateImport(stockImport);
        return importDAO.create(stockImport);
    }

    // --- Exports ---

    public List<StockExport> getAllExports() throws SQLException {
        return exportDAO.findAll();
    }

    public List<StockExportDetail> getExportDetails(int exportId) throws SQLException {
        return exportDAO.findDetailsByExportId(exportId);
    }

    public int createExport(StockExport stockExport) throws SQLException {
        validateExport(stockExport);
        return exportDAO.create(stockExport);
    }

    // --- Validation ---

    private void validateImport(StockImport si) {
        if (si.getSupplierId() <= 0)
            throw new IllegalArgumentException("Please select a supplier.");
        if (si.getImportDate() == null)
            throw new IllegalArgumentException("Import date is required.");
        if (si.getDetails() == null || si.getDetails().isEmpty())
            throw new IllegalArgumentException("Please add at least one product line.");
        for (StockImportDetail d : si.getDetails()) {
            if (d.getQuantity() <= 0)
                throw new IllegalArgumentException("Quantity must be greater than 0.");
            if (d.getUnitPrice() == null || d.getUnitPrice().signum() < 0)
                throw new IllegalArgumentException("Unit price cannot be negative.");
        }
    }

    private void validateExport(StockExport se) {
        if (se.getExportDate() == null)
            throw new IllegalArgumentException("Export date is required.");
        if (se.getDetails() == null || se.getDetails().isEmpty())
            throw new IllegalArgumentException("Please add at least one product line.");
        for (StockExportDetail d : se.getDetails()) {
            if (d.getQuantity() <= 0)
                throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
    }
}
