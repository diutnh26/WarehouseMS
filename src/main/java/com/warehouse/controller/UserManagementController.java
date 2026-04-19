package com.warehouse.controller;

import com.warehouse.dao.UserDAO;
import com.warehouse.model.User;
import com.warehouse.service.AuthService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Boolean> colActive;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cmbRole;
    @FXML private CheckBox chkActive;
    @FXML private TextField txtSearch;

    private final UserDAO userDAO = new UserDAO();
    private final AuthService authService = new AuthService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private User selectedUser = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        // Username: monospace font
        colUsername.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold;"); }
            }
        });

        // Role: colored badges
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                } else {
                    String style;
                    if ("Admin".equals(item)) {
                        style = "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #EF4444;";
                    } else if ("Staff".equals(item)) {
                        style = "-fx-background-color: rgba(59,130,246,0.15); -fx-text-fill: #3B82F6;";
                    } else {
                        style = "-fx-background-color: rgba(245,158,11,0.15); -fx-text-fill: #F59E0B;";
                    }
                    Label badge = new Label(item);
                    badge.setStyle(style + "-fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
                    setGraphic(badge); setText(null);
                }
            }
        });

        // Status: Active badge
        colActive.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setGraphic(null);
                } else {
                    String text = item ? "Active" : "Inactive";
                    String style = item
                        ? "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10B981;"
                        : "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #EF4444;";
                    Label badge = new Label(text);
                    badge.setStyle(style + "-fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
                    setGraphic(badge); setText(null);
                }
            }
        });

        tblUsers.setItems(userList);
        tblUsers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cmbRole.setItems(FXCollections.observableArrayList("Admin", "Staff", "Manager"));
        chkActive.setSelected(true);

        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) populateForm(n);
        });

        txtSearch.textProperty().addListener((obs, o, n) -> handleSearch());
        loadUsers();
    }

    private void loadUsers() {
        try { userList.setAll(userDAO.findAll()); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    private void populateForm(User user) {
        selectedUser = user;
        txtUsername.setText(user.getUsername());
        txtPassword.clear();
        txtFullName.setText(user.getFullName());
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhone());
        cmbRole.getSelectionModel().select(user.getRole());
        chkActive.setSelected(user.isActive());
        txtUsername.setDisable(true); // Can't change username on edit
    }

    @FXML
    private void handleAdd() {
        if (ValidationUtil.isNullOrBlank(txtUsername.getText())) { AlertUtil.showWarning("Validation", "Username is required."); return; }
        if (ValidationUtil.isNullOrBlank(txtPassword.getText())) { AlertUtil.showWarning("Validation", "Password is required for new users."); return; }
        if (ValidationUtil.isNullOrBlank(txtFullName.getText())) { AlertUtil.showWarning("Validation", "Full name is required."); return; }
        if (cmbRole.getSelectionModel().getSelectedItem() == null) { AlertUtil.showWarning("Validation", "Select a role."); return; }

        try {
            User user = new User();
            user.setUsername(txtUsername.getText().trim());
            user.setPasswordHash(authService.hashPassword(txtPassword.getText()));
            user.setFullName(txtFullName.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setPhone(txtPhone.getText().trim());
            user.setRole(cmbRole.getSelectionModel().getSelectedItem());
            user.setActive(chkActive.isSelected());

            userDAO.insert(user);
            AlertUtil.showInfo("Success", "User created.");
            clearForm(); loadUsers();
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML
    private void handleUpdate() {
        if (selectedUser == null) { AlertUtil.showWarning("Warning", "Select a user first."); return; }
        try {
            selectedUser.setFullName(txtFullName.getText().trim());
            selectedUser.setEmail(txtEmail.getText().trim());
            selectedUser.setPhone(txtPhone.getText().trim());
            selectedUser.setRole(cmbRole.getSelectionModel().getSelectedItem());
            selectedUser.setActive(chkActive.isSelected());
            userDAO.update(selectedUser);

            // Update password if entered
            if (!txtPassword.getText().isEmpty()) {
                userDAO.updatePassword(selectedUser.getUserId(), authService.hashPassword(txtPassword.getText()));
            }

            AlertUtil.showInfo("Success", "User updated.");
            clearForm(); loadUsers();
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML
    private void handleDelete() {
        if (selectedUser == null) { AlertUtil.showWarning("Warning", "Select a user first."); return; }
        if (AlertUtil.showConfirm("Confirm", "Deactivate user: " + selectedUser.getUsername() + "?")) {
            try { userDAO.delete(selectedUser.getUserId()); clearForm(); loadUsers(); }
            catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
        }
    }

    @FXML private void handleSearch() {
        try { userList.setAll(userDAO.search(txtSearch.getText())); }
        catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML private void handleClear() { clearForm(); }

    private void clearForm() {
        selectedUser = null;
        txtUsername.clear(); txtUsername.setDisable(false);
        txtPassword.clear(); txtFullName.clear(); txtEmail.clear(); txtPhone.clear();
        cmbRole.getSelectionModel().clearSelection(); chkActive.setSelected(true);
        tblUsers.getSelectionModel().clearSelection();
    }
}
