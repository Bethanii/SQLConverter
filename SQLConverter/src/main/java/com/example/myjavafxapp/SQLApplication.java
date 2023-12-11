package com.example.myjavafxapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SQLApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SQLApplication.class.getResource("landing-page.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 820, 500);
        stage.setTitle("SQL Converter");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        try {
         //   String url = "jdbc:mysql://localhost:3306/SQLConverter";
            String url = "jdbc:mysql://127.0.0.1:3306/SQLConverter?user=username&password=password";
            String username = "username";
            String password = "password";

            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection dbConnection = DriverManager.getConnection(url, username, password)) {
                if (dbConnection != null) {
                    System.out.println("Successfully connected to MySQL database SQLConverter");
                    launch(args);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("An error occurred while connecting to the MySQL database");
            ex.printStackTrace();
        }
        launch();
    }
}
        //}
 /*   public static void main(String[] args) {
        launch();
    } */