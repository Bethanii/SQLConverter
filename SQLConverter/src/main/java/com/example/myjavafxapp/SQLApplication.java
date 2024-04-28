package com.example.myjavafxapp;

import javafx.application.Application;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;

public class SQLApplication extends Application
{
    @FXML
    private AnchorPane landingPage;

    /**
     * Starts the application by loading the landing page.
     * @param stage The primary stage of the application.
     * @throws IOException If an I/O exception occurs while loading.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SQLApplication.class.getResource("landing-page.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
        stage.setTitle("SQL Converter");
        stage.setScene(scene);
        stage.show();
        stage.setOnShown(event -> landingPage.requestFocus());
        stage.show();
    }

    /**
     * Main method to launch the application.
     */
    public static void main(String[] args) {
        launch();
    }
}