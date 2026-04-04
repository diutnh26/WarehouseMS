package com.warehouse.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockImport {
    private int importId;
    private int supplierId;
    private String supplierName; // for display
    private LocalDateTime importDate;
    private BigDecimal totalAmount;
    private String notes;
    private int createdByUserId;
    private String createdByName; // for display
    private LocalDateTime createdDate;
    private List<StockImportDetail> details = new ArrayList<>();

    public StockImport() {}

    public int getImportId() { return importId; }
    public void setImportId(int importId) { this.importId = importId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public LocalDateTime getImportDate() { return importDate; }
    public void setImportDate(LocalDateTime importDate) { this.importDate = importDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public List<StockImportDetail> getDetails() { return details; }
    public void setDetails(List<StockImportDetail> details) { this.details = details; }
}
