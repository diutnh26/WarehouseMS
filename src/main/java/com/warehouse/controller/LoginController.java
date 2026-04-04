package com.warehouse.controller;

import com.warehouse.model.User;
import com.warehouse.service.AuthService;
import com.warehouse.util.AlertUtil;
import com.warehouse.util.SceneUtil;
import com.warehouse.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter both username and password.");
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                SceneUtil.switchScene("main-layout.fxml", "Dashboard", 1200, 750);
            } else {
                lblError.setText("Invalid username or password.");
                txtPassword.clear();
            }
        } catch (Exception e) {
            lblError.setText("Connection error. Please check database settings.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}
