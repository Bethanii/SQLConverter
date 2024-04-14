package com.example.myjavafxapp;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;

public class SQLApplication extends Application
{
    @FXML
    private AnchorPane landingPage;
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

    public static void main(String[] args) {
        launch();
    }
}