package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Controller {

    @FXML
    private Label welcomeText;
    private AnchorPane signInPage;

    @FXML
    private void DatabaseConnection() throws IOException {
        try {
            String url = "jdbc:mysql://127.0.0.1:3306/SQLConverter?user=username&password=password";
            String username = "username";
            String password = "password";

            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection dbConnection = DriverManager.getConnection(url, username, password)) {
                if (dbConnection != null) {
                    System.out.println("Successfully connected to MySQL database SQLConverter");
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("An error occurred while connecting to the MySQL database");
        }
    }

    @FXML
    protected void onSignInButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("sign-in-page.fxml"));

        signInPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(signInPage);

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Sign In");
    }

    @FXML
    protected void onCreateAccountButtonClick() {
        welcomeText.setText("");
    }

    @FXML
    protected void onSignInPageButtonClick() throws IOException {
        DatabaseConnection();
    }
}
