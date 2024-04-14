package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    Controller controller = new Controller();

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
                loadPage("security-question-page.fxml", "Standard Account Security Questions", "AccountController", this.email, true);
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
                loadPage("enterprise-account-sub-users.fxml", "Enterprise Account Sub User Information", "AccountController", this.email, false);
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
                "What's your mother's maiden name?",
                "What city were you born in?",
                "What's your favorite color?"
        );

        secondSecurityQuestion.getItems().addAll(
                "What was your first pet's name?",
                "What's your mother's maiden name?",
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

        loadPage("user-database-setup-page.fxml", "Database Setup Information", "AccountController", this.email, false);
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
                loadPage("account-creation-confirmation-page.fxml", "Account Creation Confirmation", "Controller", this.email, false);
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
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.saveEnterpriseSubUserEmails(emails);
        loadPage("security-question-page.fxml", "Enterprise Account Security Questions", "AccountController", this.email, true);
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

    public void loadPage(String pageFile, String pageTitle, String controllerSelection, String email, boolean setSecurityQuestions) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource(pageFile));

        Parent page = fxmlLoader.load();

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(page);
        page.requestFocus();

        switch(controllerSelection)
        {
            case "AccountController":
                if (email != null) {
                    AccountController controller = fxmlLoader.getController();
                    controller.setEmail(email);
                }
                if (setSecurityQuestions)
                {
                    AccountController accountController = fxmlLoader.getController();
                    accountController.setSecurityQuestionOptions();
                }
                break;
            case "Controller":
                Controller controller = fxmlLoader.getController();
        }
        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle(pageTitle);
    }

    @FXML
    public void onEnterpriseAccountBackButtonClick() throws IOException
    {
        loadPage("select-account-type-page.fxml", "Select Account Type", "Controller", "", false);
    }

    @FXML
    public void onStandardAccountBackButtonClick() throws IOException
    {
        loadPage("select-account-type-page.fxml", "Select Account Type", "Controller", "", false);
    }
}