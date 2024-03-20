package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class StandardAccountController {

    @FXML
    private Label welcomeText;
    @FXML
    private Label passwordError;
    @FXML
    private Label confPasswordError;
    @FXML
    private Label emailExistsError;
    @FXML
    private Label connectionError;
    @FXML
    private AnchorPane securityQuestionPage;
    @FXML
    private AnchorPane sqlConverterPage;
    @FXML
    private AnchorPane userDatabaseSetupPage;
    @FXML
    private TextField standardEmailInputField;
    @FXML
    private TextField standardPasswordInputField;
    @FXML
    private TextField standardConfirmPasswordField;
    @FXML
    private TextField serverNameField;
    @FXML
    private TextField dbUsernameField;
    @FXML
    private TextField dbPasswordField;
    @FXML
    private TextField databaseNameField;
    @FXML
    private ChoiceBox<String> firstSecurityQuestion;
    @FXML
    private ChoiceBox<String> secondSecurityQuestion;
    private String email;
    private String password;

    @FXML
    protected void onStandardAccountNextButtonClick() throws IOException {
        String emailInput = standardEmailInputField.getText();
        String passwordInput = standardPasswordInputField.getText();
        String passwordInputConfirmation = standardConfirmPasswordField.getText();

        DatabaseManager databaseManager = new DatabaseManager();
        Connection connection = databaseManager.DatabaseConnection();

        Boolean emailValidation = databaseManager.CheckIfColumnValueExists(connection, "Email", emailInput);
        Boolean passwordValidation = ValidatePasswordEntry(passwordInput, passwordInputConfirmation);

        if (emailValidation == true) {
            emailExistsError.setVisible(true);
            standardEmailInputField.getStyleClass().add("text-field-error");
        } else if (emailValidation != true) {
            if (passwordValidation == false) {
                passwordError.setVisible(true);
                confPasswordError.setVisible(true);
                standardEmailInputField.getStyleClass().add("text-field-error");
                standardConfirmPasswordField.getStyleClass().add("text-field-error");
            } else if (passwordValidation == true) {
                databaseManager.SaveUserDetails(connection, emailInput, passwordInput);
                this.email = emailInput;

                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(SQLApplication.class.getResource("security-question-page.fxml"));
                securityQuestionPage = fxmlLoader.load();

                StandardAccountController controller = fxmlLoader.getController();
                controller.setEmail(this.email);

                Scene currentScene = welcomeText.getScene();
                currentScene.setRoot(securityQuestionPage);
                securityQuestionPage.requestFocus();

                StandardAccountController standardAccountController = fxmlLoader.getController();
                standardAccountController.setSecurityQuestionOptions();

                Stage stage = (Stage) currentScene.getWindow();
                stage.sizeToScene();
                stage.setTitle("Standard Account Security Questions");
            }
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public boolean ValidatePasswordEntry(String password, String passwordConfirmation) {
        if (!Objects.equals(password, passwordConfirmation)) {
            return false;
        } else {
            return true;
        }
    }

    public void setSecurityQuestionOptions() {
        firstSecurityQuestion.getItems().addAll(
                "What was your first pet's name?",
                "What's mother's maiden name?",
                "What city were you born in?",
                "What's your favorite color?"
        );

        secondSecurityQuestion.getItems().addAll(
                "What was your first pet's name?",
                "What's mother's maiden name?",
                "What city were you born in?",
                "What's your favorite color?"
        );
    }

    @FXML
    protected void onSecurityQuestionNextButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("user-database-setup-page.fxml"));

        userDatabaseSetupPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();

        StandardAccountController controller = fxmlLoader.getController();
        controller.setEmail(this.email);

        currentScene.setRoot(userDatabaseSetupPage);
        userDatabaseSetupPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Database Setup Information");
    }

    @FXML
    protected void onDatabaseSetupNextButton() throws IOException
    {
        String serverName = serverNameField.getText();
        String databaseName = databaseNameField.getText();
        String dbUsername = dbUsernameField.getText();
        String dbPassword = dbPasswordField.getText();

        Connection UserConnection = null;
        DatabaseManager databaseManager = new DatabaseManager();

        if (serverName.isEmpty() || databaseName.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty())
        {
            connectionError.setText("Fields cannot be empty");
            connectionError.setVisible(true);
            return;
        }
        try
        {
            UserConnection = databaseManager.ConnectUserDatabase(serverName, databaseName, dbUsername, dbPassword);
            Connection connection = databaseManager.DatabaseConnection();

            if (UserConnection != null)
            {
                String insertSQL = "UPDATE Users SET serverName = ?, databaseName = ?, dbUsername = ?, dbPassword = ? WHERE email = ?";

                try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                    pstmt.setString(1, serverName);
                    pstmt.setString(2, databaseName);
                    pstmt.setString(3, dbUsername);
                    pstmt.setString(4, dbPassword);
                    pstmt.setString(5, this.email);

                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    connectionError.setVisible(true);
                    System.out.println("Failed to insert data, error: " + e.getMessage());
                    e.printStackTrace();
                }
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(SQLApplication.class.getResource("sql-converter.fxml"));

                sqlConverterPage = fxmlLoader.load();
                Scene currentScene = welcomeText.getScene();
                currentScene.setRoot(sqlConverterPage);
                sqlConverterPage.requestFocus();

                Stage stage = (Stage) currentScene.getWindow();
                stage.sizeToScene();
                stage.setTitle("Standard Account Security Questions");
            }
            else
            {
                connectionError.setVisible(true);
            }
        } catch (IOException e) {
            connectionError.setVisible(true);
        }
    }
 }