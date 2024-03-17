package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ChoiceBox;

public class EnterpriseAccountController {

    @FXML
    private Label welcomeText;
    @FXML
    private Label emailExistsError;
    @FXML
    private Label passwordError;
    @FXML
    private Label confPasswordError;
    @FXML
    public TextField subUserEmailInputField;
    @FXML
    public TextField enterpriseEmailInputField;
    @FXML
    public TextField enterprisePasswordInputField;
    @FXML
    public TextField enterpriseConfirmPasswordField;
    @FXML
    private AnchorPane enterpriseAccountSubUserPage;
    @FXML
    private AnchorPane securityQuestionPage;
    @FXML
    private List<String> emails = new ArrayList<>();
    @FXML
    private TextArea emailsDisplayArea;
    @FXML
    private ChoiceBox<String> firstSecurityQuestion;
    @FXML
    private ChoiceBox<String> secondSecurityQuestion;
    @FXML
    protected void onEnterpriseAccountNextButtonClick() throws IOException {
        String emailInput = enterpriseEmailInputField.getText();
        String passwordInput = enterprisePasswordInputField.getText();
        String passwordInputConfirmation = enterpriseConfirmPasswordField.getText();

        DatabaseManager databaseManager = new DatabaseManager();
        Connection connection = databaseManager.DatabaseConnection();

        Boolean emailValidation = databaseManager.CheckIfColumnValueExists(connection, "Email", emailInput);
        Boolean passwordValidation = ValidatePasswordEntry(passwordInput, passwordInputConfirmation);

        if (emailValidation == true)
        {
            emailExistsError.setVisible(true);
            enterpriseEmailInputField.getStyleClass().add("text-field-error");
        }

        else if (emailValidation != true) {
            if (passwordValidation == false) {
                passwordError.setVisible(true);
                confPasswordError.setVisible(true);
                enterprisePasswordInputField.getStyleClass().add("text-field-error");
                enterpriseConfirmPasswordField.getStyleClass().add("text-field-error");
            } else if (passwordValidation == true) {
                databaseManager.getUserDetails(connection, emailInput, passwordInput);

                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(SQLApplication.class.getResource("enterprise-account-sub-users.fxml"));

                enterpriseAccountSubUserPage = fxmlLoader.load();
                Scene currentScene = welcomeText.getScene();
                currentScene.setRoot(enterpriseAccountSubUserPage);
                enterpriseAccountSubUserPage.requestFocus();

                Stage stage = (Stage) currentScene.getWindow();
                stage.sizeToScene();
                stage.setTitle("Enterprise Account Sub User Information");
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

    @FXML
    protected void onEnterpriseSubUsersNextButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("security-question-page.fxml"));

        securityQuestionPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(securityQuestionPage);
        securityQuestionPage.requestFocus();

        EnterpriseAccountController enterpriseAccountController = fxmlLoader.getController();
        enterpriseAccountController.setSecurityQuestionOptions();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Enterprise Account Security Questions");
    }

    @FXML
    protected void addEmailField() throws IOException {
        String emailText = subUserEmailInputField.getText().trim();
        emails.add(emailText);
        displayEmails(emails);
        subUserEmailInputField.clear();
    }

    public void displayEmails(List<String> emails) {
        String content = String.join("\n", emails);
        emailsDisplayArea.setText(content);
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
