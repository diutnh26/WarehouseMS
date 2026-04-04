-- ============================================
-- Warehouse Management System
-- Script 03: Stored Procedures
-- ============================================

USE WarehouseMS;
GO

-- ============================================
-- SP: Process Stock Import
-- Records an import and updates inventory
-- ============================================
CREATE OR ALTER PROCEDURE sp_ProcessStockImport
    @ImportID INT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- Update inventory for each product in the import
        MERGE INTO Inventory AS target
        USING (
            SELECT ProductID, SUM(Quantity) AS TotalQty
            FROM StockImportDetails
            WHERE ImportID = @ImportID
            GROUP BY ProductID
        ) AS source
        ON target.ProductID = source.ProductID
        WHEN MATCHED THEN
            UPDATE SET
                CurrentQuantity = target.CurrentQuantity + source.TotalQty,
                LastUpdated = GETDATE()
        WHEN NOT MATCHED THEN
            INSERT (ProductID, CurrentQuantity, LastUpdated)
            VALUES (source.ProductID, source.TotalQty, GETDATE());

        -- Update total amount on the import header
        UPDATE StockImports
        SET TotalAmount = (
            SELECT ISNULL(SUM(Quantity * UnitPrice), 0)
            FROM StockImportDetails
            WHERE ImportID = @ImportID
        )
        WHERE ImportID = @ImportID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- ============================================
-- SP: Process Stock Export
-- Records an export and updates inventory
-- ============================================
CREATE OR ALTER PROCEDURE sp_ProcessStockExport
    @ExportID INT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- Check sufficient stock for all items
        IF EXISTS (
            SELECT 1
            FROM StockExportDetails sed
            INNER JOIN Inventory i ON sed.ProductID = i.ProductID
            WHERE sed.ExportID = @ExportID
              AND i.CurrentQuantity < sed.Quantity
        )
        BEGIN
            RAISERROR('Insufficient stock for one or more products.', 16, 1);
            RETURN;
        END

        -- Decrease inventory
        UPDATE i
        SET i.CurrentQuantity = i.CurrentQuantity - sed.TotalQty,
            i.LastUpdated = GETDATE()
        FROM Inventory i
        INNER JOIN (
            SELECT ProductID, SUM(Quantity) AS TotalQty
            FROM StockExportDetails
            WHERE ExportID = @ExportID
            GROUP BY ProductID
        ) sed ON i.ProductID = sed.ProductID;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- ============================================
-- SP: Get Low Stock Products
-- Returns products below minimum stock level
-- ============================================
CREATE OR ALTER PROCEDURE sp_GetLowStockProducts
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        p.ProductID,
        p.ProductCode,
        p.ProductName,
        c.CategoryName,
        p.Unit,
        p.MinStockLevel,
        ISNULL(i.CurrentQuantity, 0) AS CurrentQuantity,
        p.MinStockLevel - ISNULL(i.CurrentQuantity, 0) AS Deficit
    FROM Products p
    LEFT JOIN Inventory i ON p.ProductID = i.ProductID
    LEFT JOIN Categories c ON p.CategoryID = c.CategoryID
    WHERE p.IsActive = 1
      AND ISNULL(i.CurrentQuantity, 0) < p.MinStockLevel
    ORDER BY Deficit DESC;
END;
GO

-- ============================================
-- SP: Inventory Report by Date Range
-- Shows all stock movements within a period
-- ============================================
CREATE OR ALTER PROCEDURE sp_InventoryReportByDate
    @StartDate DATE,
    @EndDate   DATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Import movements
    SELECT
        'Import' AS TransactionType,
        si.ImportID AS TransactionID,
        si.ImportDate AS TransactionDate,
        s.SupplierName,
        p.ProductCode,
        p.ProductName,
        sid.Quantity,
        sid.UnitPrice,
        sid.Quantity * sid.UnitPrice AS LineTotal,
        u.FullName AS CreatedBy
    FROM StockImports si
    INNER JOIN StockImportDetails sid ON si.ImportID = sid.ImportID
    INNER JOIN Products p ON sid.ProductID = p.ProductID
    INNER JOIN Suppliers s ON si.SupplierID = s.SupplierID
    INNER JOIN Users u ON si.CreatedByUserID = u.UserID
    WHERE CAST(si.ImportDate AS DATE) BETWEEN @StartDate AND @EndDate

    UNION ALL

    -- Export movements
    SELECT
        'Export' AS TransactionType,
        se.ExportID AS TransactionID,
        se.ExportDate AS TransactionDate,
        NULL AS SupplierName,
        p.ProductCode,
        p.ProductName,
        sed.Quantity,
        NULL AS UnitPrice,
        NULL AS LineTotal,
        u.FullName AS CreatedBy
    FROM StockExports se
    INNER JOIN StockExportDetails sed ON se.ExportID = sed.ExportID
    INNER JOIN Products p ON sed.ProductID = p.ProductID
    INNER JOIN Users u ON se.CreatedByUserID = u.UserID
    WHERE CAST(se.ExportDate AS DATE) BETWEEN @StartDate AND @EndDate

    ORDER BY TransactionDate DESC;
END;
GO

-- ============================================
-- SP: Product Movement History
-- Shows all imports/exports for a specific product
-- ============================================
CREATE OR ALTER PROCEDURE sp_ProductMovementHistory
    @ProductID INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        'Import' AS TransactionType,
        si.ImportDate AS TransactionDate,
        s.SupplierName AS RelatedParty,
        sid.Quantity,
        sid.UnitPrice,
        u.FullName AS CreatedBy,
        si.Notes
    FROM StockImportDetails sid
    INNER JOIN StockImports si ON sid.ImportID = si.ImportID
    INNER JOIN Suppliers s ON si.SupplierID = s.SupplierID
    INNER JOIN Users u ON si.CreatedByUserID = u.UserID
    WHERE sid.ProductID = @ProductID

    UNION ALL

    SELECT
        'Export' AS TransactionType,
        se.ExportDate AS TransactionDate,
        se.Reason AS RelatedParty,
        sed.Quantity,
        NULL AS UnitPrice,
        u.FullName AS CreatedBy,
        se.Notes
    FROM StockExportDetails sed
    INNER JOIN StockExports se ON sed.ExportID = se.ExportID
    INNER JOIN Users u ON se.CreatedByUserID = u.UserID
    WHERE sed.ProductID = @ProductID

    ORDER BY TransactionDate DESC;
END;
GO

-- ============================================
-- SP: Dashboard Statistics
-- ============================================
CREATE OR ALTER PROCEDURE sp_GetDashboardStats
AS
BEGIN
    SET NOCOUNT ON;

    -- Total active products
    SELECT COUNT(*) AS TotalProducts FROM Products WHERE IsActive = 1;

    -- Total active suppliers
    SELECT COUNT(*) AS TotalSuppliers FROM Suppliers WHERE IsActive = 1;

    -- Low stock count
    SELECT COUNT(*) AS LowStockCount
    FROM Products p
    LEFT JOIN Inventory i ON p.ProductID = i.ProductID
    WHERE p.IsActive = 1 AND ISNULL(i.CurrentQuantity, 0) < p.MinStockLevel;

    -- Today's imports count
    SELECT COUNT(*) AS TodayImports
    FROM StockImports WHERE CAST(ImportDate AS DATE) = CAST(GETDATE() AS DATE);

    -- Today's exports count
    SELECT COUNT(*) AS TodayExports
    FROM StockExports WHERE CAST(ExportDate AS DATE) = CAST(GETDATE() AS DATE);
END;
GO

PRINT 'All stored procedures created successfully.';
GO
