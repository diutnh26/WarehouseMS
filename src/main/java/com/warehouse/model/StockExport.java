package com.warehouse.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockExport {
    private int exportId;
    private LocalDateTime exportDate;
    private String reason;
    private String notes;
    private int createdByUserId;
    private String createdByName;
    private LocalDateTime createdDate;
    private List<StockExportDetail> details = new ArrayList<>();

    public StockExport() {}

    public int getExportId() { return exportId; }
    public void setExportId(int exportId) { this.exportId = exportId; }

    public LocalDateTime getExportDate() { return exportDate; }
    public void setExportDate(LocalDateTime exportDate) { this.exportDate = exportDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public List<StockExportDetail> getDetails() { return details; }
    public void setDetails(List<StockExportDetail> details) { this.details = details; }
}
