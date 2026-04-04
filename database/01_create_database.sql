-- ============================================
-- Warehouse Management System
-- Script 01: Create Database
-- ============================================

USE master;
GO

-- Drop database if it exists
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'WarehouseMS')
BEGIN
    ALTER DATABASE WarehouseMS SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE WarehouseMS;
END
GO

-- Create database
CREATE DATABASE WarehouseMS;
GO

USE WarehouseMS;
GO

PRINT 'Database WarehouseMS created successfully.';
GO
