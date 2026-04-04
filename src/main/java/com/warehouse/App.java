package com.warehouse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        Parent root = FXMLLoader.load(
            getClass().getResource("/com/warehouse/view/login.fxml")
        );

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(
            getClass().getResource("/com/warehouse/css/styles.css").toExternalForm()
        );

        stage.setTitle("Warehouse Management System");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
