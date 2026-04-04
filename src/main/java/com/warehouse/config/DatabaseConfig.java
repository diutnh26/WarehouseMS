package com.warehouse.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    // Your SQL Server connection settings
    private static final String SERVER   = "127.0.0.1";
    private static final String PORT     = "1434";
    private static final String DATABASE = "WarehouseMS";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "Warehouse@123";

    private static final String CONNECTION_URL = String.format(
        "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true",
        SERVER, PORT, DATABASE
    );

    /**
     * Returns a new database connection.
     * Callers are responsible for closing the connection.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
    }

    /**
     * Tests whether the database is reachable.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
}

