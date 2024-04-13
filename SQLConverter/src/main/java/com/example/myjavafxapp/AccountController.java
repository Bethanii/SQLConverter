package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

public class AccountController {

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
    private TextField firstSecurityQuestionInput;
    @FXML
    private TextField secondSecurityQuestionInput;
    @FXML
    private ChoiceBox<String> firstSecurityQuestion;
    @FXML
    private ChoiceBox<String> secondSecurityQuestion;
    private String email;
    private String password;
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
    private AnchorPane confirmationPage;
    @FXML
    private List<String> emails = new ArrayList<>();
    @FXML
    private TextArea emailsDisplayArea;

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

                AccountController controller = fxmlLoader.getController();
                controller.setEmail(this.email);

                Scene currentScene = welcomeText.getScene();
                currentScene.setRoot(securityQuestionPage);
                securityQuestionPage.requestFocus();

                AccountController accountController = fxmlLoader.getController();
                accountController.setSecurityQuestionOptions();

                Stage stage = (Stage) currentScene.getWindow();
                stage.sizeToScene();
                stage.setTitle("Standard Account Security Questions");
            }
        }
    }

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
                databaseManager.SaveUserDetails(connection, emailInput, passwordInput);
                this.email = emailInput;

                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(SQLApplication.class.getResource("enterprise-account-sub-users.fxml"));
                enterpriseAccountSubUserPage = fxmlLoader.load();

                AccountController controller = fxmlLoader.getController();
                controller.setEmail(this.email);

                Scene currentScene = welcomeText.getScene();
                currentScene.setRoot(enterpriseAccountSubUserPage);
                enterpriseAccountSubUserPage.requestFocus();

                Stage stage = (Stage) currentScene.getWindow();
                stage.sizeToScene();
                stage.setTitle("Enterprise Account Sub User Information");
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

        firstSecurityQuestion.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedQuestion = newValue.toString();
        });

        secondSecurityQuestion.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedQuestion = newValue.toString();
        });
    }

    @FXML
    protected void onSecurityQuestionNextButtonClick() throws IOException {

        String firstAnswer = firstSecurityQuestionInput.getText();
        String secondAnswer = secondSecurityQuestionInput.getText();

        String firstQuestion = firstSecurityQuestion.getSelectionModel().getSelectedItem().toString();
        String secondQuestion = secondSecurityQuestion.getSelectionModel().getSelectedItem().toString();

        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.SaveSecurityQuestions(firstAnswer, secondAnswer, firstQuestion, secondQuestion, this.email);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("user-database-setup-page.fxml"));

        userDatabaseSetupPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();

        AccountController controller = fxmlLoader.getController();
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
                fxmlLoader.setLocation(SQLApplication.class.getResource("account-creation-confirmation-page.fxml"));

                confirmationPage = fxmlLoader.load();
                Scene currentScene = welcomeText.getScene();
                currentScene.setRoot(confirmationPage);
                confirmationPage.requestFocus();

                Stage stage = (Stage) currentScene.getWindow();
                stage.sizeToScene();
                stage.setTitle("Account Creation Confirmation");
            }
            else
            {
                connectionError.setVisible(true);
            }
        } catch (IOException e) {
            connectionError.setVisible(true);
        }
    }

    @FXML
    protected void onEnterpriseSubUsersNextButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("security-question-page.fxml"));
        securityQuestionPage = fxmlLoader.load();

        AccountController controller = fxmlLoader.getController();
        controller.setEmail(this.email);

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(securityQuestionPage);
        securityQuestionPage.requestFocus();

        AccountController accountController = fxmlLoader.getController();
        accountController.setSecurityQuestionOptions();

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
}