package com.warehouse.model;

import java.math.BigDecimal;

public class StockImportDetail {
    private int importDetailId;
    private int importId;
    private int productId;
    private String productCode; // for display
    private String productName; // for display
    private int quantity;
    private BigDecimal unitPrice;

    public StockImportDetail() {}

    public int getImportDetailId() { return importDetailId; }
    public void setImportDetailId(int id) { this.importDetailId = id; }

    public int getImportId() { return importId; }
    public void setImportId(int importId) { this.importId = importId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
