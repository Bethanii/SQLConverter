package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class JavaFXUtils<T>
{
    @FXML
    private static Label welcomeText;

    private AnchorPane anchorPane;
    private T controller;

    public JavaFXUtils(AnchorPane anchorPane, T controller) {
        this.anchorPane = anchorPane;
        this.controller = controller;
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public T getController() {
        return controller;
    }
}