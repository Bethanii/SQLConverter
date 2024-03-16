package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
import javafx.scene.control.ChoiceBox;

public class StandardAccountController {

    @FXML
    private Label welcomeText;
    @FXML
    private AnchorPane securityQuestionPage;
    @FXML
    private AnchorPane standardAccountPage;

    @FXML
    private ChoiceBox<String> firstSecurityQuestion;
    @FXML
    private ChoiceBox<String> secondSecurityQuestion;


    @FXML
    protected void onStandardAccountNextButtonClick() throws IOException {
        DatabaseConnection databaseController = new DatabaseConnection();
        Connection connection = databaseController.DatabaseConnection();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("security-question-page.fxml"));

        securityQuestionPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(securityQuestionPage);
        securityQuestionPage.requestFocus();

        StandardAccountController securityQuestionController = fxmlLoader.getController();
        securityQuestionController.setSecurityQuestionOptions();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Standard Account Security Questions");
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
