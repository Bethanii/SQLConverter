package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.*;
import java.util.Random;

public class Controller
{

    @FXML
    private Label welcomeText;
    @FXML
    private Label emailExistsError;
    @FXML
    private Label question1;
    @FXML
    private Label question2;
    @FXML
    private Label tempPasswordMessage;
    @FXML
    private TextField signInEmailInputField;
    @FXML
    private TextField signInPasswordInputField;
    @FXML
    private TextField resetEmailField;
    @FXML
    private TextField response1;
    @FXML
    private TextField response2;
    @FXML
    private AnchorPane standardAccountPage;
    private AnchorPane signInPage;
    private AnchorPane selectAccountPage;
    @FXML
    private AnchorPane enterpriseAccountPage;
    @FXML
    private AnchorPane resetPasswordPage;
    @FXML
    private AnchorPane newPasswordPage;
    @FXML
    private AnchorPane validateSecurityQuestionsPage;
    @FXML
    private AnchorPane sqlConverterPage;
    private String email;

    @FXML
    protected void onSignInSelectionClick() throws IOException
    {
        loadPage("sign-in-page.fxml", "Sign In", "");
    }

    @FXML
    protected void onSignInButtonClick() throws IOException
    {
        boolean missingFields = RequiredFieldsMissing();

        if (missingFields == false)
        {
            boolean validLogin = LoginValidation();
            if (validLogin == true)
            {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(SQLApplication.class.getResource("sql-converter.fxml"));

                sqlConverterPage = fxmlLoader.load();

                SQLConverterController controller = fxmlLoader.getController();
                controller.setEmail(this.email);

                controller.SetConnection();
                controller.populateStaticRow();

                Scene currentScene = welcomeText.getScene();
                currentScene.setRoot(sqlConverterPage);
                sqlConverterPage.requestFocus();

                Stage stage = (Stage) currentScene.getWindow();
                stage.sizeToScene();
                stage.setTitle("SQL Converter");
            }
        }
    }

    public boolean LoginValidation()
    {
        String emailInput = signInEmailInputField.getText();
        String passwordInput = signInPasswordInputField.getText();

        DatabaseManager databaseManager = new DatabaseManager();

        try (Connection connection = databaseManager.DatabaseConnection())
        {
            boolean emailExists = UserExists(emailInput);
            if (emailExists)
            {
                String storedPassword = databaseManager.GetUserPassword(connection, emailInput);

                if (storedPassword != null && storedPassword.equals(passwordInput))
                {
                    this.email = emailInput;
                    return true;
                }
                else
                {
                    emailExistsError.setText("Invalid email or password, please try again");
                    return false;
                }
            }
            else
            {
                emailExistsError.setText("Invalid email or password, please try again");
                return false;
            }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            emailExistsError.setText("Invalid email or password, please try again");
            return false;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean UserExists(String emailInput) throws IOException
    {
        DatabaseManager databaseManager = new DatabaseManager();
        Connection connection = databaseManager.DatabaseConnection();

        Boolean emailExists = databaseManager.CheckIfColumnValueExists(connection, "Email", emailInput);

        if (emailExists == true)
        {
            return true;
        }
        else
        {
            emailExistsError.setVisible(true);
            return false;
        }
    }

    public boolean RequiredFieldsMissing()
    {
        String emailInput = signInEmailInputField.getText();
        String passwordInput = signInPasswordInputField.getText();

        signInEmailInputField.getStyleClass().remove("text-field-error-rounded");
        signInPasswordInputField.getStyleClass().remove("text-field-error-rounded");

        if (emailInput.isBlank() && passwordInput.isBlank() == true)
        {
            signInEmailInputField.getStyleClass().add("text-field-error-rounded");
            signInPasswordInputField.getStyleClass().add("text-field-error-rounded");
            return true;
        }
        if (emailInput.isBlank() == true)
        {
            signInEmailInputField.getStyleClass().add("text-field-error-rounded");
            return true;
        }
        if (passwordInput.isBlank() == true)
        {
            signInPasswordInputField.getStyleClass().add("text-field-error-rounded");
            return true;
        }
        return false;
    }

    @FXML
    protected void onEnterpriseAccountButtonClick() throws IOException
    {
        loadPage("enterprise-account-page.fxml", "Enterprise Account Information", "");
    }


    @FXML
    protected void onStandardAccountButtonClick() throws IOException
    {
        loadPage("standard-account-page.fxml", "Standard Account Information", "");
    }

    @FXML
    protected void onCreateAccountButtonClick() throws IOException
    {
        loadPage("select-account-type-page.fxml", "Select Account Type", "");
    }

    @FXML
    protected void onResetPasswordButton() throws Exception
    {
        String emailInput = resetEmailField.getText();
        this.email = emailInput;

        DatabaseManager databaseManager = new DatabaseManager();
        Connection connection = databaseManager.DatabaseConnection();

        if (UserExists(emailInput) == true) {
            databaseManager.DatabaseConnection();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("validate-security-questions-page.fxml"));
            AnchorPane validateSecurityQuestionsPage = fxmlLoader.load();
            Controller controller = fxmlLoader.getController();

            Scene currentScene = welcomeText.getScene();
            currentScene.setRoot(validateSecurityQuestionsPage);
            validateSecurityQuestionsPage.requestFocus();

            String[] securityQuestions = databaseManager.getSecurityQuestions(this.email);
            String firstQuestionString = securityQuestions[0];
            String secondQuestionString = securityQuestions[1];

            controller.question1.setText(firstQuestionString);
            controller.question2.setText(secondQuestionString);

            Stage stage = (Stage) currentScene.getWindow();
            stage.sizeToScene();
            stage.setTitle("Sign In");
        }
    }

    @FXML
    protected void onValidateSecurityQuestionsNextClick() throws Exception
    {
        DatabaseManager databaseManager = new DatabaseManager();

        String response1Input = response1.getText();
        String response2Input = response2.getText();

        Connection connection = databaseManager.DatabaseConnection();
    }

    private String generateTemporaryPassword()
    {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder tempPassword = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            tempPassword.append(chars.charAt(random.nextInt(chars.length())));
        }
        return tempPassword.toString();
    }

    @FXML
    protected void onForgotPasswordLinkClick() throws IOException
    {
        loadPage("reset-password-page.fxml", "Reset Password", "");
    }

    public void loadPage(String pageFile, String pageTitle, String email) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource(pageFile));

        Parent page = fxmlLoader.load();

        if (email != null && !email.isEmpty()) {
            AccountController controller = fxmlLoader.getController();
            controller.setEmail(email);
        }

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(page);
        page.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle(pageTitle);
    }
}
