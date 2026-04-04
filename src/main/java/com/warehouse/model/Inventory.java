package com.warehouse.model;

import java.time.LocalDateTime;

public class Inventory {
    private int inventoryId;
    private int productId;
    private String productCode;
    private String productName;
    private String categoryName;
    private String unit;
    private int currentQuantity;
    private int minStockLevel;
    private LocalDateTime lastUpdated;

    public Inventory() {}

    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(int currentQuantity) { this.currentQuantity = currentQuantity; }

    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isLowStock() {
        return currentQuantity < minStockLevel;
    }

    public String getStockStatus() {
        if (currentQuantity == 0) return "Out of Stock";
        if (currentQuantity < minStockLevel) return "Low Stock";
        return "In Stock";
    }
}
