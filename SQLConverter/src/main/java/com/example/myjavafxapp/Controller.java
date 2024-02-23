package com.example.myjavafxapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.*;
import javafx.scene.image.ImageView;

public class Controller {

    @FXML
    private Label welcomeText;
    @FXML
    private TextField emailInputField;
    @FXML
    private TextField passwordInputField;
    private AnchorPane signInPage;
    private AnchorPane selectAccountPage;
    @FXML
    private AnchorPane enterpriseAccountPage;

    @FXML
    private Connection DatabaseConnection() throws IOException {
        try {
            String url = "jdbc:mysql://127.0.0.1:3306/SQLConverter?user=username&password=password";
            String username = "username";
            String password = "password";

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection dbConnection = DriverManager.getConnection(url, username, password);
                if (dbConnection != null) {
                    System.out.println("Successfully connected to MySQL database SQLConverter");

                    return dbConnection;
                }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("An error occurred while connecting to the MySQL database");
        }
        return null;
    }

    @FXML
    protected void onSignInButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("sign-in-page.fxml"));

        signInPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(signInPage);
        signInPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Sign In");
    }

    @FXML
    protected void onCreateAccountButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("select-account-type-page.fxml"));

        selectAccountPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(selectAccountPage);
        selectAccountPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Create Account");
    }

    @FXML
    protected void onEnterpriseAccountButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("enterprise-account-page.fxml"));

        enterpriseAccountPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(enterpriseAccountPage);
        enterpriseAccountPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Enterprise Account Information");
    }

    @FXML
    protected void onStandardAccountButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("sign-in-page.fxml"));

        signInPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(signInPage);
        signInPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Sign In");
    }

    @FXML
    private String getUserInput(TextField userInputField) throws IOException {
        String userInput = userInputField.getText();
        System.out.println("User Input: " + userInput);

        return userInput;
    }

    @FXML
    private void validateUserLogin() throws IOException {
        String userEmail = getUserInput(emailInputField);
        String userPassword = getUserInput(passwordInputField);

        Connection connection = DatabaseConnection();
         try
         {
             String sql = "SELECT * FROM users WHERE email = ?";
             try (PreparedStatement statement = connection.prepareStatement(sql)) {
                 statement.setString(1, userEmail);

                 try (ResultSet resultSet = statement.executeQuery()) {
                     if (resultSet.next()) {
                         System.out.println("User exists in the database.");
                     } else {
                         System.out.println("User does not exist in the database.");
                     }
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
    }
}
