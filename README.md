# Warehouse Management System (WMS)

A JavaFX desktop application for managing warehouse products, suppliers, inventory, and stock transactions.

## Tech Stack
- **Language:** Java 17+
- **UI Framework:** JavaFX 21 (FXML + CSS)
- **Build Tool:** Apache Maven
- **Database:** SQL Server (SSMS 22)
- **Architecture:** MVC + Service + DAO

## Prerequisites
1. **Java JDK 17+** — [Download](https://adoptium.net/)
2. **Apache Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
3. **SQL Server** (Express or Developer Edition)
4. **SQL Server Management Studio 22**

## Setup Instructions

### 1. Database Setup
Open SSMS and run the SQL scripts in order:
```
database/01_create_database.sql
database/02_create_tables.sql
database/03_stored_procedures.sql
database/04_insert_sample_data.sql
```

### 2. Configure Connection
Edit `src/main/java/com/warehouse/config/DatabaseConfig.java`:
- Update `SERVER`, `PORT`, `USERNAME`, `PASSWORD` to match your SQL Server setup.

### 3. Build and Run
```bash
cd WarehouseMS
mvn clean javafx:run
```

## Default Login Credentials
| Username  | Password     | Role    |
|-----------|-------------|---------|
| admin     | password123 | Admin   |
| staff01   | password123 | Staff   |
| staff02   | password123 | Staff   |
| manager01 | password123 | Manager |

## Features
- CRUD for Products, Suppliers, Categories, Users
- Stock Import / Export with inventory auto-update
- Low stock alerts (color-coded)
- Inventory reports by date range
- Product movement history tracking
- Role-based access control (Admin, Staff, Manager)
- Search and filtering across all screens

## Project Structure
```
WarehouseMS/
├── pom.xml
├── database/              SQL scripts
├── src/main/java/com/warehouse/
│   ├── App.java           Entry point
│   ├── config/            Database connection
│   ├── model/             POJOs
│   ├── dao/               Data access (SQL)
│   ├── service/           Business logic
│   ├── controller/        FXML controllers
│   └── util/              Helpers
└── src/main/resources/com/warehouse/
    ├── view/              FXML files
    └── css/               Stylesheets
```
