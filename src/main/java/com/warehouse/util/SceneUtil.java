package com.warehouse.util;

import com.warehouse.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneUtil {

    private static final String VIEW_PATH = "/com/warehouse/view/";
    private static final String CSS_PATH = "/com/warehouse/css/styles.css";

    /**
     * Switches the entire stage to a new FXML scene (used for login -> main transition).
     */
    public static void switchScene(String fxmlFile, String title, double width, double height) throws IOException {
        Stage stage = App.getPrimaryStage();
        Parent root = FXMLLoader.load(SceneUtil.class.getResource(VIEW_PATH + fxmlFile));
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(SceneUtil.class.getResource(CSS_PATH).toExternalForm());
        stage.setScene(scene);
        stage.setTitle("WMS - " + title);
        stage.setResizable(true);
        stage.centerOnScreen();
    }

    /**
     * Loads an FXML file into the center region of a BorderPane (used for main layout content swapping).
     */
    public static void loadContent(BorderPane contentArea, String fxmlFile) throws IOException {
        Parent content = FXMLLoader.load(SceneUtil.class.getResource(VIEW_PATH + fxmlFile));
        contentArea.setCenter(content);
    }

    /**
     * Loads an FXML and returns its controller for programmatic access.
     */
    public static <T> T loadContentWithController(BorderPane contentArea, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(VIEW_PATH + fxmlFile));
        Parent content = loader.load();
        contentArea.setCenter(content);
        return loader.getController();
    }
}
