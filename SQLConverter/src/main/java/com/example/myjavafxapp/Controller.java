package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.*;

public class Controller {

    @FXML
    private Label welcomeText;
    @FXML
    private TextField emailInputField;
    @FXML
    private TextField passwordInputField;
    private AnchorPane signInPage;


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
    protected void onCreateAccountButtonClick() {
        welcomeText.setText("");
    }

    @FXML
    protected void onSignInPageButtonClick() throws IOException {
        validateUserLogin();
    }

    @FXML
    private String getUserInput(TextField userInputField) throws IOException {
        String userInput = userInputField.getText();
        System.out.println("User Input: " + userInput);

        return userInput;
    }

    @FXML
    private void displayLoginValidationError () throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(SQLApplication.class.getResource("login-validation-error.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 820, 500);
        stage.setTitle("Login Validation Error");
        stage.setScene(scene);
        stage.show();
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
                         displayLoginValidationError();
                     }
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
    }
}
