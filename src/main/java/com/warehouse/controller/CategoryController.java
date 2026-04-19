package com.warehouse.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.warehouse.dao.CategoryDAO;
import com.warehouse.model.Category;
import com.warehouse.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class CategoryController implements Initializable {

    @FXML private TableView<Category> tblCategories;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDescription;
    @FXML private TextField txtCategoryName;
    @FXML private TextArea txtDescription;

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private Category selectedCategory = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colName.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tblCategories.setItems(categoryList);
        tblCategories.getSelectionModel().selectedItemProperty().addListener(
            (obs, o, n) -> { if (n != null) { selectedCategory = n; txtCategoryName.setText(n.getCategoryName()); txtDescription.setText(n.getDescription()); } }
        );
        tblCategories.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadCategories();
    }

    private void loadCategories() {
        try { categoryList.setAll(categoryDAO.findAll()); }
        catch (SQLException e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @SuppressWarnings("unused") @FXML private void handleAdd() {
        String name = txtCategoryName.getText().trim();
        if (name.isEmpty()) { AlertUtil.showWarning("Validation", "Category name is required."); return; }
        try {
            Category c = new Category(); c.setCategoryName(name); c.setDescription(txtDescription.getText());
            categoryDAO.insert(c);
            AlertUtil.showInfo("Success", "Category added.");
            clearForm(); loadCategories();
        } catch (SQLException e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @SuppressWarnings("unused") @FXML private void handleUpdate() {
        if (selectedCategory == null) { AlertUtil.showWarning("Warning", "Select a category first."); return; }
        try {
            selectedCategory.setCategoryName(txtCategoryName.getText().trim());
            selectedCategory.setDescription(txtDescription.getText());
            categoryDAO.update(selectedCategory);
            AlertUtil.showInfo("Success", "Category updated.");
            clearForm(); loadCategories();
        } catch (SQLException e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @SuppressWarnings("unused") @FXML private void handleDelete() {
        if (selectedCategory == null) { AlertUtil.showWarning("Warning", "Select a category first."); return; }
        if (AlertUtil.showConfirm("Confirm", "Delete category: " + selectedCategory.getCategoryName() + "?")) {
            try { categoryDAO.delete(selectedCategory.getCategoryId()); clearForm(); loadCategories(); }
            catch (SQLException e) { AlertUtil.showError("Error", "Cannot delete: category may be in use."); }
        }
    }

    @SuppressWarnings("unused") @FXML private void handleClear() { clearForm(); }

    private void clearForm() {
        selectedCategory = null; txtCategoryName.clear(); txtDescription.clear();
        tblCategories.getSelectionModel().clearSelection();
    }
}
