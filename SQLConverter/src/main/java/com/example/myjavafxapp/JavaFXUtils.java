package com.example.myjavafxapp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class JavaFXUtils
{
    public static <T> T loadFXML(String resourcePath, AnchorPane anchorPane) throws IOException {
        FXMLLoader loader = new FXMLLoader(JavaFXUtils.class.getResource(resourcePath));
        anchorPane.getChildren().setAll((Pane) loader.load());
        return loader.getController();
    }

    public static <T> T getController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(JavaFXUtils.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }

    public static <T> void setController(String fxmlPath, T controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(JavaFXUtils.class.getResource(fxmlPath));
        loader.setController(controller);
    }

    public static FXMLLoader getFXMLLoader(String fxmlPath) {
        return new FXMLLoader(JavaFXUtils.class.getResource(fxmlPath));
    }
}