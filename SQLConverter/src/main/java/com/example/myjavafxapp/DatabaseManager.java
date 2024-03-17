package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.sql.*;

public class DatabaseManager {

    public Connection DatabaseConnection() throws IOException {
        String serverName = "sqlconverterserver.database.windows.net";
        String databaseName = "SQLConverterDB";
        String username = "Bethany";
        String password = "SQLConverter1!";

        String connectionUrl = "jdbc:sqlserver://" + serverName + ":1433;"
                + "database=" + databaseName + ";"
                + "user=" + username + ";"
                + "password=" + password + ";"
                + "encrypt=true;"
                + "trustServerCertificate=false;"
                + "hostNameInCertificate=*.database.windows.net;"
                + "loginTimeout=30;";

        try {
            Connection connection = DriverManager.getConnection(connectionUrl);
            System.out.println("Connected to Azure SQL Database successfully.");
            return connection;
        } catch (SQLException e) {
            System.out.println("Could not connect, error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void getUserDetails(Connection connection, String email, String password) {
        String sql = "INSERT INTO Users (Email, password) VALUES (?, ?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
            System.out.println("User email inserted successfully.");
        } catch (SQLException e) {
            System.out.println("Could not insert user, error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean CheckIfColumnValueExists(Connection connection, String columnName, String columnValue)
    {
        String sql = "SELECT * FROM Users WHERE " + columnName + " = (?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, columnValue);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private String getUserInput(TextField userInputField) throws IOException {
        String userInput = userInputField.getText();
        System.out.println("User Input: " + userInput);

        return userInput;
    }
}