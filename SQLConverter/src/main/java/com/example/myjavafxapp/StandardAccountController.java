package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
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
    private AnchorPane securityQuestionPage;
    @FXML
    private TextField standardEmailInputField;
    @FXML
    private TextField standardPasswordInputField;
    @FXML
    private TextField standardConfirmPasswordField;
    @FXML
    private ChoiceBox<String> firstSecurityQuestion;
    @FXML
    private ChoiceBox<String> secondSecurityQuestion;

    @FXML
    protected void onStandardAccountNextButtonClick() throws IOException {
        String emailInput = standardEmailInputField.getText();
        String passwordInput = standardPasswordInputField.getText();
        String passwordInputConfirmation = standardConfirmPasswordField.getText();

        DatabaseManager databaseManager = new DatabaseManager();
        Connection connection = databaseManager.DatabaseConnection();

        Boolean emailValidation = databaseManager.CheckIfColumnValueExists(connection, "Email", emailInput);
        Boolean passwordValidation = ValidatePasswordEntry(passwordInput, passwordInputConfirmation);

        if (emailValidation == true)
        {
            emailExistsError.setVisible(true);
            standardEmailInputField.getStyleClass().add("text-field-error");
        }

        else if (emailValidation != true)
        {
            if (passwordValidation == false)
            {
                passwordError.setVisible(true);
                confPasswordError.setVisible(true);
                standardEmailInputField.getStyleClass().add("text-field-error");
                standardConfirmPasswordField.getStyleClass().add("text-field-error");
            }
            else if (passwordValidation == true)
            {
                databaseManager.SaveUserDetails(connection, emailInput, passwordInput);

                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(SQLApplication.class.getResource("security-question-page.fxml"));

                securityQuestionPage = fxmlLoader.load();
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

    public boolean ValidatePasswordEntry(String password, String passwordConfirmation)
    {
        if (!Objects.equals(password, passwordConfirmation))
        {
            return false;
        }
        else
        {
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

}
