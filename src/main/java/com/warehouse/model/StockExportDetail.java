package com.warehouse.model;

public class StockExportDetail {
    private int exportDetailId;
    private int exportId;
    private int productId;
    private String productCode;
    private String productName;
    private int quantity;

    public StockExportDetail() {}

    public int getExportDetailId() { return exportDetailId; }
    public void setExportDetailId(int id) { this.exportDetailId = id; }

    public int getExportId() { return exportId; }
    public void setExportId(int exportId) { this.exportId = exportId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
