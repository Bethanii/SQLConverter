package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import javax.mail.PasswordAuthentication;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;
import java.util.Random;

public class Controller
{

    @FXML
    private Label welcomeText;
    @FXML
    private Label emailExistsError;
    @FXML
    private TextField signInEmailInputField;
    @FXML
    private TextField signInPasswordInputField;
    @FXML
    private AnchorPane standardAccountPage;
    private AnchorPane signInPage;
    private AnchorPane selectAccountPage;
    @FXML
    private AnchorPane enterpriseAccountPage;
    @FXML
    private AnchorPane resetPasswordPage;
    @FXML
    private AnchorPane sqlConverterPage;
    private String email;

    @FXML
    protected void onSignInSelectionClick() throws IOException
    {
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
                 //   databaseManager.GetUserDBInfo(emailInput);
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
    protected void onStandardAccountButtonClick() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("standard-account-page.fxml"));

        standardAccountPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(standardAccountPage);
        standardAccountPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Standard Account Information");
    }

    @FXML
    protected void onCreateAccountButtonClick() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("select-account-type-page.fxml"));

        selectAccountPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(selectAccountPage);
        selectAccountPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Create Account");
    }

    @FXML
    protected void onResetPasswordButton() {
        String userEmail = "bethanihampton@gmail.com";
        String temporaryPassword = generateTemporaryPassword();
        String emailSubject = "SQL Converter Password Reset";
        String emailBody = "Your temporary password is: " + temporaryPassword;

        String fromEmail = "SQLConverterApp@gmail.com";
        String emailPassword = "SQLConverter1!";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.mail.yahoo.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "smtp.mail.yahoo.com");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, emailPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
            message.setSubject(emailSubject);
            message.setText(emailBody);

            Transport.send(message);

            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Failed to send email.");
        }
    }

 /*   private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = Gm.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential; */
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
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("reset-password-page.fxml"));

        resetPasswordPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(resetPasswordPage);
        resetPasswordPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Reset Password");
    }
}
