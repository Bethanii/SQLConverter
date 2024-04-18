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

    @FXML private Label welcomeText, connectionError, securityQuestionErrorMessage, enterpriseAccountInputError, standardAccountInputError;
    @FXML private AnchorPane securityQuestionPage, sqlConverterPage, userDatabaseSetupPage, enterpriseAccountSubUserPage, confirmationPage;
    @FXML private TextField standardEmailInputField, standardPasswordInputField, standardConfirmPasswordField, serverNameField, dbUsernameField, dbPasswordField, databaseNameField, firstSecurityQuestionInput, secondSecurityQuestionInput, subUserEmailInputField, enterpriseEmailInputField, enterprisePasswordInputField, enterpriseConfirmPasswordField, tempPasswordInputField;
    @FXML private ChoiceBox<String> firstSecurityQuestion, secondSecurityQuestion;
    @FXML private TextArea emailsDisplayArea;

    private String email;
    private List<String> emails = new ArrayList<>();
    private DatabaseManager databaseManager = new DatabaseManager();

    @FXML
    protected void onStandardAccountNextButtonClick() throws IOException {
        accountNextButtonClick("standard");
    }
    @FXML
    protected void onEnterpriseAccountNextButtonClick() throws IOException {
        accountNextButtonClick("enterprise");
    }

    @FXML
    protected void accountNextButtonClick(String accountType) throws IOException {
        TextField emailInputField = accountType.equals("standard") ? standardEmailInputField : enterpriseEmailInputField;
        TextField passwordInputField = accountType.equals("standard") ? standardPasswordInputField : enterprisePasswordInputField;
        TextField confirmPasswordField = accountType.equals("standard") ? standardConfirmPasswordField : enterpriseConfirmPasswordField;
        Label accountInputError = accountType.equals("standard") ? standardAccountInputError : enterpriseAccountInputError;
        String nextPageFxml = accountType.equals("standard") ? "security-question-page.fxml" : "enterprise-account-sub-users.fxml";
        String nextPageTitle = accountType.equals("standard") ? "Standard Account Security Questions" : "Enterprise Account Sub User Information";
        boolean isStandard = accountType.equals("standard");

        String emailInput = emailInputField.getText();
        String passwordInput = passwordInputField.getText();
        String passwordInputConfirmation = confirmPasswordField.getText();

        DatabaseManager databaseManager = new DatabaseManager();
        Connection connection = databaseManager.DatabaseConnection();

        Boolean emailExists = databaseManager.CheckIfColumnValueExists(connection, "Email", emailInput);
        Boolean passwordMatches = validatePasswordsMatch(passwordInput, passwordInputConfirmation);

        if(emailInput.isEmpty() || emailInput.isBlank() || passwordInput.isEmpty() || passwordInput.isBlank() || passwordInputConfirmation.isEmpty() || passwordInputConfirmation.isBlank()) {
            accountInputError.setText("Fields cannot be blank");
            accountInputError.setLayoutX(450);
            accountInputError.setVisible(true);

            emailInputField.getStyleClass().add("text-field-error");
            passwordInputField.getStyleClass().add("text-field-error");
            confirmPasswordField.getStyleClass().add("text-field-error");
        } else {
            emailInputField.getStyleClass().remove("text-field-error");
            passwordInputField.getStyleClass().remove("text-field-error");
            confirmPasswordField.getStyleClass().remove("text-field-error");

            if (!emailExists) {
                if (!passwordMatches) {
                    accountInputError.setVisible(true);
                    accountInputError.setLayoutX(350);
                    accountInputError.setText("Password and Confirmation Password do not match");
                    passwordInputField.getStyleClass().add("text-field-error");
                    confirmPasswordField.getStyleClass().add("text-field-error");
                } else {
                    databaseManager.SaveUserDetails(connection, emailInput, passwordInput);
                    this.email = emailInput;
                    loadPage(nextPageFxml, nextPageTitle, "AccountController", this.email, isStandard);
                }
            } else {
                accountInputError.setVisible(true);
                accountInputError.setLayoutX(400);
                accountInputError.setText("Email already exists, please log in");
                emailInputField.getStyleClass().add("text-field-error");
            }
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean validatePasswordsMatch(String password, String passwordConfirmation) {
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
    protected boolean securityQuestionMissingQuestions()
    {
        if (firstSecurityQuestion.getSelectionModel().getSelectedItem() == null || secondSecurityQuestion.getSelectionModel().getSelectedItem() == null) {
            securityQuestionErrorMessage.setText("You must select a question from the above dropdowns");
            securityQuestionErrorMessage.setVisible(true);
            return true;
        }
        else
        {
            return false;
        }
    }

    @FXML
    protected boolean securityQuestionMissingFields(TextField... fields) throws IOException
    {
        boolean blankFields = false;

        for (TextField field : fields) {
            boolean fieldBlank = field.getText().isBlank() || field.getText().isEmpty();
            if (fieldBlank) {
                field.getStyleClass().add("text-field-error");
                blankFields = true;
            } else {
                field.getStyleClass().remove("text-field-error");
            }
        }

        if (blankFields) {
            for (TextField field : fields) {
                field.getStyleClass().add("text-field-error");
            }
        }
        securityQuestionErrorMessage.setVisible(blankFields);
        return blankFields;
    }

    @FXML
    protected void onSecurityQuestionNextButtonClick() throws IOException
    {
        if (!securityQuestionMissingFields(firstSecurityQuestionInput, secondSecurityQuestionInput))
        {
            String firstAnswer = firstSecurityQuestionInput.getText();
            String secondAnswer = secondSecurityQuestionInput.getText();

            if (!securityQuestionMissingQuestions())
            {
                String firstQuestion = firstSecurityQuestion.getSelectionModel().getSelectedItem().toString();
                String secondQuestion = secondSecurityQuestion.getSelectionModel().getSelectedItem().toString();

                DatabaseManager databaseManager = new DatabaseManager();
                databaseManager.SaveSecurityQuestions(firstAnswer, secondAnswer, firstQuestion, secondQuestion, this.email);

                loadPage("user-database-setup-page.fxml", "Database Setup Information", "AccountController", this.email, false);
            }
        }
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

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sub-users-temporary-password.fxml"));
        AnchorPane subUserTempEmailPage = fxmlLoader.load();
        AccountController controller = fxmlLoader.getController();

        controller.setEmail(this.email);
        controller.setEmails(emails);

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(subUserTempEmailPage);
        subUserTempEmailPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Enterprise Account Sub-User Temporary Password");
    }

    @FXML
    protected void onTempPasswordNextButtonClick() throws IOException {
        String tempPassword = tempPasswordInputField.getText().trim();

        for (String email : emails) {
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.setTempPassword(email, tempPassword);
        }
        loadPage("enterprise-security-question-page.fxml", "Enterprise Account Security Questions", "AccountController", this.email, true);
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
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