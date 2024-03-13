package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ChoiceBox;

public class AccountController {

    @FXML
    private Label welcomeText;
    private AnchorPane signInPage;
    @FXML
    public TextField subUserEmailInputField;
    @FXML
    private AnchorPane enterpriseAccountPage;
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
    protected void onEnterpriseAccountNextButtonClick() throws IOException {
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

    @FXML
    protected void onEnterpriseSubUsersNextButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("security-question-page.fxml"));

        securityQuestionPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(securityQuestionPage);
        securityQuestionPage.requestFocus();

        AccountController securityQuestionController = fxmlLoader.getController();
        securityQuestionController.setFirstSecurityQuestionOptions();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Enterprise Account Security Questions");
    }

    public void setFirstSecurityQuestionOptions() {
        firstSecurityQuestion.getItems().addAll(
                "What was your first pet's name?",
                "What's mother's maiden name?",
                "What city were you born in?",
                "What's your favorite color?"
        );
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
}
