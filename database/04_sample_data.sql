-- ============================================
-- Warehouse Management System
-- Script 04: Insert Sample Data
-- ============================================

USE WarehouseMS;
GO

-- ============================================
-- Users (passwords are BCrypt hashes of 'password123')
-- ============================================
INSERT INTO Users (Username, PasswordHash, FullName, Email, Phone, Role) VALUES
('admin',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'System Admin',    'admin@warehouse.com',   '0901000001', 'Admin'),
('staff01',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Nguyen Van A',    'nva@warehouse.com',     '0901000002', 'Staff'),
('staff02',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Tran Thi B',      'ttb@warehouse.com',     '0901000003', 'Staff'),
('manager01','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Le Van C',        'lvc@warehouse.com',     '0901000004', 'Manager');
GO

-- ============================================
-- Categories
-- ============================================
INSERT INTO Categories (CategoryName, Description) VALUES
(N'Electronics',       N'Electronic devices and components'),
(N'Office Supplies',   N'Stationery and office equipment'),
(N'Furniture',         N'Tables, chairs, shelves'),
(N'Packaging',         N'Boxes, tape, wrapping materials'),
(N'Cleaning Supplies', N'Detergents, mops, brooms');
GO

-- ============================================
-- Products
-- ============================================
INSERT INTO Products (ProductCode, ProductName, CategoryID, Unit, MinStockLevel, Description) VALUES
('ELC-001', N'Laptop Dell Inspiron 15',    1, N'Unit',   5,  N'15.6 inch laptop'),
('ELC-002', N'Wireless Mouse Logitech',    1, N'Unit',   20, N'Logitech M331 Silent'),
('ELC-003', N'USB-C Hub 7-in-1',           1, N'Unit',   15, N'Multi-port adapter'),
('OFF-001', N'A4 Paper (500 sheets)',       2, N'Ream',   50, N'80gsm white A4'),
('OFF-002', N'Ballpoint Pen (Box of 50)',   2, N'Box',    30, N'Blue ink ballpoint'),
('OFF-003', N'Stapler Heavy Duty',          2, N'Unit',   10, N'Capacity 100 sheets'),
('FUR-001', N'Office Desk 120x60cm',        3, N'Unit',   5,  N'Wooden office desk'),
('FUR-002', N'Ergonomic Office Chair',       3, N'Unit',   8,  N'Adjustable height with lumbar support'),
('PKG-001', N'Cardboard Box 40x30x20cm',    4, N'Unit',   100,N'Standard shipping box'),
('PKG-002', N'Packing Tape Roll',            4, N'Roll',   80, N'48mm wide, 100m'),
('CLN-001', N'All-Purpose Cleaner 5L',       5, N'Bottle', 10, N'Multi-surface cleaner'),
('CLN-002', N'Microfiber Cloth Pack (10)',    5, N'Pack',   20, N'Reusable cleaning cloths');
GO

-- ============================================
-- Suppliers
-- ============================================
INSERT INTO Suppliers (SupplierName, ContactPerson, Phone, Email, Address) VALUES
(N'TechWorld Co., Ltd',       N'Pham Minh D',  '0281234567', 'contact@techworld.vn',   N'123 Le Loi, District 1, HCMC'),
(N'Office Plus JSC',          N'Hoang Thi E',  '0287654321', 'sales@officeplus.vn',    N'456 Nguyen Hue, District 1, HCMC'),
(N'FurniPro Vietnam',         N'Vo Van F',     '0289876543', 'info@furnipro.vn',       N'789 CMT8, District 3, HCMC'),
(N'PackShip Logistics',       N'Dang Thi G',   '0282345678', 'order@packship.vn',      N'321 Hai Ba Trung, District 3, HCMC'),
(N'CleanMax Supplies',        N'Bui Van H',    '0283456789', 'supply@cleanmax.vn',     N'654 Vo Van Tan, District 3, HCMC');
GO

-- ============================================
-- Sample Stock Imports
-- ============================================
-- Import 1: Electronics from TechWorld
INSERT INTO StockImports (SupplierID, ImportDate, Notes, CreatedByUserID)
VALUES (1, '2025-03-01', N'Initial electronics stock', 2);

INSERT INTO StockImportDetails (ImportID, ProductID, Quantity, UnitPrice) VALUES
(1, 1, 10, 15000000),   -- 10 Laptops
(1, 2, 50, 350000),     -- 50 Mice
(1, 3, 30, 450000);     -- 30 USB-C Hubs

-- Import 2: Office supplies from Office Plus
INSERT INTO StockImports (SupplierID, ImportDate, Notes, CreatedByUserID)
VALUES (2, '2025-03-05', N'Monthly office supplies restock', 2);

INSERT INTO StockImportDetails (ImportID, ProductID, Quantity, UnitPrice) VALUES
(2, 4, 100, 85000),     -- 100 Reams A4
(2, 5, 60,  120000),    -- 60 Pen boxes
(2, 6, 15,  250000);    -- 15 Staplers

-- Import 3: Packaging from PackShip
INSERT INTO StockImports (SupplierID, ImportDate, Notes, CreatedByUserID)
VALUES (4, '2025-03-10', N'Packaging materials order', 3);

INSERT INTO StockImportDetails (ImportID, ProductID, Quantity, UnitPrice) VALUES
(3, 9,  200, 15000),    -- 200 Boxes
(3, 10, 150, 25000);    -- 150 Tape rolls

-- Process all imports to update inventory
EXEC sp_ProcessStockImport @ImportID = 1;
EXEC sp_ProcessStockImport @ImportID = 2;
EXEC sp_ProcessStockImport @ImportID = 3;
GO

-- ============================================
-- Sample Stock Exports
-- ============================================
-- Export 1: Internal distribution
INSERT INTO StockExports (ExportDate, Reason, Notes, CreatedByUserID)
VALUES ('2025-03-15', N'Internal use', N'Office floor restocking', 2);

INSERT INTO StockExportDetails (ExportID, ProductID, Quantity) VALUES
(1, 1, 3),    -- 3 Laptops
(1, 2, 15),   -- 15 Mice
(1, 4, 40);   -- 40 Reams A4

-- Export 2: Customer order
INSERT INTO StockExports (ExportDate, Reason, Notes, CreatedByUserID)
VALUES ('2025-03-20', N'Customer sale', N'Order #ORD-2025-0042', 3);

INSERT INTO StockExportDetails (ExportID, ProductID, Quantity) VALUES
(2, 9,  80),  -- 80 Boxes
(2, 10, 60);  -- 60 Tape rolls

-- Process exports
EXEC sp_ProcessStockExport @ExportID = 1;
EXEC sp_ProcessStockExport @ExportID = 2;
GO

PRINT 'Sample data inserted successfully.';
PRINT 'Default login: admin / password123';
GO
