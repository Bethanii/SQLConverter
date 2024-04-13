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
