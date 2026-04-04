-- ============================================
-- Warehouse Management System
-- Script 02: Create Tables
-- ============================================

USE WarehouseMS;
GO

-- ============================================
-- 1. Users
-- ============================================
CREATE TABLE Users (
    UserID          INT IDENTITY(1,1) PRIMARY KEY,
    Username        NVARCHAR(50)  NOT NULL UNIQUE,
    PasswordHash    NVARCHAR(255) NOT NULL,
    FullName        NVARCHAR(100) NOT NULL,
    Email           NVARCHAR(100),
    Phone           NVARCHAR(20),
    Role            NVARCHAR(20)  NOT NULL CHECK (Role IN ('Admin', 'Staff', 'Manager')),
    IsActive        BIT           NOT NULL DEFAULT 1,
    CreatedDate     DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================
-- 2. Categories
-- ============================================
CREATE TABLE Categories (
    CategoryID      INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName    NVARCHAR(100) NOT NULL UNIQUE,
    Description     NVARCHAR(255)
);
GO

-- ============================================
-- 3. Products
-- ============================================
CREATE TABLE Products (
    ProductID       INT IDENTITY(1,1) PRIMARY KEY,
    ProductCode     NVARCHAR(20)  NOT NULL UNIQUE,
    ProductName     NVARCHAR(150) NOT NULL,
    CategoryID      INT           NOT NULL,
    Unit            NVARCHAR(30)  NOT NULL DEFAULT N'Piece',
    MinStockLevel   INT           NOT NULL DEFAULT 10,
    Description     NVARCHAR(500),
    IsActive        BIT           NOT NULL DEFAULT 1,
    CreatedDate     DATETIME      NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Products_Categories
        FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID)
);
GO

-- ============================================
-- 4. Suppliers
-- ============================================
CREATE TABLE Suppliers (
    SupplierID      INT IDENTITY(1,1) PRIMARY KEY,
    SupplierName    NVARCHAR(150) NOT NULL,
    ContactPerson   NVARCHAR(100),
    Phone           NVARCHAR(20),
    Email           NVARCHAR(100),
    Address         NVARCHAR(300),
    IsActive        BIT           NOT NULL DEFAULT 1,
    CreatedDate     DATETIME      NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================
-- 5. Inventory (current stock per product)
-- ============================================
CREATE TABLE Inventory (
    InventoryID     INT IDENTITY(1,1) PRIMARY KEY,
    ProductID       INT NOT NULL UNIQUE,
    CurrentQuantity INT NOT NULL DEFAULT 0,
    LastUpdated     DATETIME NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Inventory_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);
GO

-- ============================================
-- 6. Stock Imports (header)
-- ============================================
CREATE TABLE StockImports (
    ImportID        INT IDENTITY(1,1) PRIMARY KEY,
    SupplierID      INT           NOT NULL,
    ImportDate      DATETIME      NOT NULL DEFAULT GETDATE(),
    TotalAmount     DECIMAL(18,2) NOT NULL DEFAULT 0,
    Notes           NVARCHAR(500),
    CreatedByUserID INT           NOT NULL,
    CreatedDate     DATETIME      NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_StockImports_Suppliers
        FOREIGN KEY (SupplierID) REFERENCES Suppliers(SupplierID),
    CONSTRAINT FK_StockImports_Users
        FOREIGN KEY (CreatedByUserID) REFERENCES Users(UserID)
);
GO

-- ============================================
-- 7. Stock Import Details (line items)
-- ============================================
CREATE TABLE StockImportDetails (
    ImportDetailID  INT IDENTITY(1,1) PRIMARY KEY,
    ImportID        INT           NOT NULL,
    ProductID       INT           NOT NULL,
    Quantity        INT           NOT NULL CHECK (Quantity > 0),
    UnitPrice       DECIMAL(18,2) NOT NULL CHECK (UnitPrice >= 0),

    CONSTRAINT FK_ImportDetails_Imports
        FOREIGN KEY (ImportID) REFERENCES StockImports(ImportID) ON DELETE CASCADE,
    CONSTRAINT FK_ImportDetails_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);
GO

-- ============================================
-- 8. Stock Exports (header)
-- ============================================
CREATE TABLE StockExports (
    ExportID        INT IDENTITY(1,1) PRIMARY KEY,
    ExportDate      DATETIME      NOT NULL DEFAULT GETDATE(),
    Reason          NVARCHAR(100),
    Notes           NVARCHAR(500),
    CreatedByUserID INT           NOT NULL,
    CreatedDate     DATETIME      NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_StockExports_Users
        FOREIGN KEY (CreatedByUserID) REFERENCES Users(UserID)
);
GO

-- ============================================
-- 9. Stock Export Details (line items)
-- ============================================
CREATE TABLE StockExportDetails (
    ExportDetailID  INT IDENTITY(1,1) PRIMARY KEY,
    ExportID        INT NOT NULL,
    ProductID       INT NOT NULL,
    Quantity        INT NOT NULL CHECK (Quantity > 0),

    CONSTRAINT FK_ExportDetails_Exports
        FOREIGN KEY (ExportID) REFERENCES StockExports(ExportID) ON DELETE CASCADE,
    CONSTRAINT FK_ExportDetails_Products
        FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);
GO

-- ============================================
-- Indexes for performance
-- ============================================
CREATE INDEX IX_Products_CategoryID    ON Products(CategoryID);
CREATE INDEX IX_Products_ProductCode   ON Products(ProductCode);
CREATE INDEX IX_Products_ProductName   ON Products(ProductName);
CREATE INDEX IX_StockImports_Date      ON StockImports(ImportDate);
CREATE INDEX IX_StockExports_Date      ON StockExports(ExportDate);
CREATE INDEX IX_Inventory_ProductID    ON Inventory(ProductID);
GO

PRINT 'All tables and indexes created successfully.';
GO
