package com.warehouse.service;

import com.warehouse.dao.ProductDAO;
import com.warehouse.model.Product;

import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.findAll();
    }

    public List<Product> searchProducts(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) return productDAO.findAll();
        return productDAO.search(keyword.trim());
    }

    public Product getProductById(int id) throws SQLException {
        return productDAO.findById(id);
    }

    public void addProduct(Product product) throws SQLException {
        validate(product, 0);
        productDAO.insert(product);
    }

    public void updateProduct(Product product) throws SQLException {
        validate(product, product.getProductId());
        productDAO.update(product);
    }

    public void deleteProduct(int productId) throws SQLException {
        productDAO.delete(productId);
    }

    private void validate(Product p, int excludeId) throws SQLException {
        if (p.getProductCode() == null || p.getProductCode().isBlank())
            throw new IllegalArgumentException("Product code is required.");
        if (p.getProductName() == null || p.getProductName().isBlank())
            throw new IllegalArgumentException("Product name is required.");
        if (p.getCategoryId() <= 0)
            throw new IllegalArgumentException("Please select a category.");
        if (p.getMinStockLevel() < 0)
            throw new IllegalArgumentException("Minimum stock level cannot be negative.");
        if (productDAO.isCodeExists(p.getProductCode().trim(), excludeId))
            throw new IllegalArgumentException("Product code already exists.");
    }
}
