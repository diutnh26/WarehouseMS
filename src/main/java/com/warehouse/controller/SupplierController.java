package com.warehouse.controller;

import com.warehouse.model.Supplier;
import com.warehouse.service.SupplierService;
import com.warehouse.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class SupplierController implements Initializable {

    @FXML private TableView<Supplier> tblSuppliers;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colContact;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colEmail;
    @FXML private TableColumn<Supplier, String> colAddress;

    @FXML private TextField txtSupplierName;
    @FXML private TextField txtContactPerson;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextArea txtAddress;
    @FXML private TextField txtSearch;

    private final SupplierService supplierService = new SupplierService();
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    private Supplier selectedSupplier = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        tblSuppliers.setItems(supplierList);
        tblSuppliers.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> { if (newVal != null) populateForm(newVal); }
        );

        txtSearch.textProperty().addListener((obs, o, n) -> handleSearch());
        loadSuppliers();
    }

    private void loadSuppliers() {
        try { supplierList.setAll(supplierService.getAllSuppliers()); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    private void populateForm(Supplier s) {
        selectedSupplier = s;
        txtSupplierName.setText(s.getSupplierName());
        txtContactPerson.setText(s.getContactPerson());
        txtPhone.setText(s.getPhone());
        txtEmail.setText(s.getEmail());
        txtAddress.setText(s.getAddress());
    }

    @FXML private void handleAdd() {
        try {
            Supplier s = buildFromForm();
            supplierService.addSupplier(s);
            AlertUtil.showInfo("Success", "Supplier added.");
            clearForm(); loadSuppliers();
        } catch (IllegalArgumentException e) { AlertUtil.showWarning("Validation", e.getMessage()); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML private void handleUpdate() {
        if (selectedSupplier == null) { AlertUtil.showWarning("Warning", "Select a supplier first."); return; }
        try {
            Supplier s = buildFromForm();
            s.setSupplierId(selectedSupplier.getSupplierId());
            supplierService.updateSupplier(s);
            AlertUtil.showInfo("Success", "Supplier updated.");
            clearForm(); loadSuppliers();
        } catch (IllegalArgumentException e) { AlertUtil.showWarning("Validation", e.getMessage()); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML private void handleDelete() {
        if (selectedSupplier == null) { AlertUtil.showWarning("Warning", "Select a supplier first."); return; }
        if (AlertUtil.showConfirm("Confirm", "Delete supplier: " + selectedSupplier.getSupplierName() + "?")) {
            try {
                supplierService.deleteSupplier(selectedSupplier.getSupplierId());
                AlertUtil.showInfo("Success", "Supplier deleted.");
                clearForm(); loadSuppliers();
            } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
        }
    }

    @FXML private void handleSearch() {
        try { supplierList.setAll(supplierService.searchSuppliers(txtSearch.getText())); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML private void handleClear() { clearForm(); }

    private Supplier buildFromForm() {
        Supplier s = new Supplier();
        s.setSupplierName(txtSupplierName.getText().trim());
        s.setContactPerson(txtContactPerson.getText().trim());
        s.setPhone(txtPhone.getText().trim());
        s.setEmail(txtEmail.getText().trim());
        s.setAddress(txtAddress.getText());
        return s;
    }

    private void clearForm() {
        selectedSupplier = null;
        txtSupplierName.clear(); txtContactPerson.clear();
        txtPhone.clear(); txtEmail.clear(); txtAddress.clear();
        tblSuppliers.getSelectionModel().clearSelection();
    }
}
