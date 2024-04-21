package com.example.myjavafxapp;

import javafx.scene.layout.AnchorPane;
import javafx.scene.input.MouseButton;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.geometry.Pos;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import java.sql.*;

public class Controller
{
    @FXML private Label welcomeText, errorMessage, emailExistsError, newPasswordErrorMessage, question1, question2, serverNameLocationAnswer, dbConnectFailureAnswer, passwordChangeAnswer,
            accountDifferencesAnswer, updateDatebaseInfoAnswer, emptyDropdownAnswer, updateConfirmationLabel;
    @FXML private TextField signInEmailInputField, signInPasswordInputField, resetEmailField, response1, response2, newPasswordInputField, newConfirmationPasswordField;
    @FXML private ComboBox<String> serverNameLocation, dbConnectFailure, passwordChange, accountDifferences, updateDatebaseInfo, emptyDropdown;
    private DatabaseManager databaseManager = new DatabaseManager();
    private boolean isAnswerVisible = false;
    private Stage loadingStage;
    private String email;

    private FXMLLoader loadPage(String page) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(page));
        fxmlLoader.load();
        return fxmlLoader;
    }

    public Scene setRootAndGetScene(AnchorPane anchorPane) {
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(anchorPane);
        anchorPane.requestFocus();
        return currentScene;
    }

    public void setStage(Scene currentScene, String title) {
        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle(title);
    }

    public <T> T setupPage(String pagePath, String title) throws IOException {
        FXMLLoader fxmlLoader = loadPage(pagePath);
        AnchorPane pageRoot = fxmlLoader.getRoot();
        T controller = fxmlLoader.getController();
        Scene currentScene = setRootAndGetScene(pageRoot);
        setStage(currentScene, title);
        return controller;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    public void onSignInBackButtonClick() throws IOException {
        setupPage("landing-page.fxml", "Welcome");
    }

    @FXML
    public void onSignInFAQLink() throws Exception {
        goToFAQPage();
    }

    @FXML
    protected void onSignInSelectionClick() throws IOException {
        setupPage("sign-in-page.fxml", "Log In");
    }

    @FXML
    protected void onForgotPasswordLinkClick() throws IOException {
        setupPage("reset-password-page.fxml", "Reset Password");
    }

    @FXML
    protected void onEnterpriseAccountButtonClick() throws IOException {
        setupPage("enterprise-account-page.fxml", "Enterprise Account Information");
    }

    @FXML
    protected void onStandardAccountButtonClick() throws IOException {
        setupPage("standard-account-page.fxml", "Standard Account Information");
    }

    @FXML
    protected void onCreateAccountButtonClick() throws IOException {
        setupPage("select-account-type-page.fxml", "Select Account Type");
    }

    @FXML
    protected void onPasswordUpdateSignInButtonClick() throws IOException, SQLException {
        setupPage("sign-in-page.fxml", "Log In");
    }

    @FXML
    public void goToFAQPage() throws IOException {
        setupPage("faq-page.fxml", "Frequently Asked Questions");
    }

    public void onSignInButtonClick() throws IOException {
        boolean missingFields = requiredFieldsMissing();

        if (missingFields == false) {
            boolean validLogin = loginValidation();
            if (validLogin == true) {
                DatabaseManager databaseManager = new DatabaseManager();
                if(databaseManager.checkForTempPassword(this.email)) {
                    Controller controller = setupPage("enter-new-password-page.fxml", "Update Temporary Password");
                    controller.setEmail(this.email);
                    databaseManager.resetTempPassword(this.email);
                } else {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(SQLApplication.class.getResource("sql-converter.fxml"));
                    Stage primaryStage = (Stage) welcomeText.getScene().getWindow();
                    showLoadingPopup(primaryStage);
                    new Thread(() -> {
                        try {
                            Pane sqlConverterPage = fxmlLoader.load();
                            SQLConverterController controller = fxmlLoader.getController();
                            controller.setEmail(this.email);
                            Connection connection = controller.setConnection();
                            controller.setConnection(connection);

                            SessionService sessionService = SessionService.getInstance();
                            sessionService.setEmail(this.email);

                            Platform.runLater(() -> {
                                controller.populateStaticRow(connection);

                                Scene currentScene = welcomeText.getScene();
                                currentScene.setRoot(sqlConverterPage);
                                sqlConverterPage.requestFocus();

                                Stage stage = (Stage) currentScene.getWindow();
                                stage.sizeToScene();
                                stage.setTitle("SQL Converter");

                                hideLoadingPopup();
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(this::hideLoadingPopup);
                        }
                    }).start();
                }
            }
        }
    }

    public Connection setConnection(Connection userConnection) throws IOException {
        this.databaseManager = new DatabaseManager();
        String[] dbValues = databaseManager.getUserDBInfo(this.email);
        if (dbValues == null) {
            return null;
        }
        boolean localDb = databaseManager.checkForLocalDB(databaseManager.databaseConnection(), this.email);
        if (localDb) {
            userConnection = databaseManager.connectUserDatabase(dbValues[0], dbValues[1], dbValues[2], dbValues[3], true);
            return userConnection;
        } else {
            userConnection = databaseManager.connectUserDatabase(dbValues[0], dbValues[1], dbValues[2], dbValues[3], false);
            return userConnection;
        }
    }

    private void showLoadingPopup(Stage primaryStage) {
        loadingStage = new Stage(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initOwner(primaryStage);

        ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressIndicator.setStyle("-fx-progress-color: blue;");

        Label loadingLabel = new Label("Loading...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label infoLabel = new Label("Depending on your set up this can take up to 30 seconds.");
        infoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        VBox loadingPane = new VBox(10);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.getChildren().addAll(progressIndicator, loadingLabel, infoLabel);
        loadingPane.setStyle("-fx-padding: 20; -fx-background-color: rgba(0, 0, 0, 0.75);");

        Scene loadingScene = new Scene(loadingPane, 400, 250);
        loadingStage.setScene(loadingScene);
        loadingStage.show();
    }

    private void hideLoadingPopup() {
        if (loadingStage != null) {
            loadingStage.close();
        }
    }

    @FXML
    protected void onResetPasswordButton() throws Exception {
        this.email = resetEmailField.getText();
        DatabaseManager databaseManager = new DatabaseManager();
        Connection connection = databaseManager.databaseConnection();

        if (this.email.isBlank() || this.email.isEmpty()) {
            errorMessage.setVisible(true);
            errorMessage.setText("Field cannot be blank");
            errorMessage.setLayoutX(440);
            errorMessage.setLayoutY(430);
            resetEmailField.getStyleClass().add("text-field-error");
        } else {
            if (userExists(this.email) == true) {
                databaseManager.databaseConnection();
                Controller controller = setupPage("validate-security-questions-page.fxml", "Account Confirmation");

                String[] securityQuestions = databaseManager.getSecurityQuestions(this.email);
                String firstQuestionString = securityQuestions[0];
                String secondQuestionString = securityQuestions[1];

                controller.setEmail(this.email);
                controller.question1.setText(firstQuestionString);
                controller.question2.setText(secondQuestionString);
            }
        }
    }

    @FXML
    protected void onNewPasswordResetButtonClick() throws IOException, SQLException {
        if (!newPasswordFieldsValidation()) {
            databaseManager.updateNewPassword(this.email, newPasswordInputField.getText());
            setupPage("password-update-confirmation-page.fxml", "Password Successfully Updated");
            if(databaseManager.checkForTempPassword(this.email))
            {
                databaseManager.resetTempPassword(this.email);
            }
        }
    }

    @FXML
    public boolean newPasswordFieldsValidation() {
        String newPasswordInput = newPasswordInputField.getText();
        String newPasswordConfirmationInput = newConfirmationPasswordField.getText();
        if (newPasswordInput.isEmpty() || newPasswordInput.isBlank() || newPasswordConfirmationInput.isEmpty() || newPasswordConfirmationInput.isBlank()) {
            if (newPasswordInput.isEmpty() || newPasswordInput.isBlank()) {
                newPasswordErrorMessage.setText("Fields cannot be blank");
                newPasswordErrorMessage.setLayoutX(440);
                newPasswordErrorMessage.setLayoutY(430);
                newPasswordErrorMessage.setVisible(true);
                newPasswordInputField.getStyleClass().add("text-field-error");
                newConfirmationPasswordField.getStyleClass().add("text-field-error");
                return true;
            }
        }
        if(!newPasswordInput.equals(newPasswordConfirmationInput)) {
            newPasswordErrorMessage.setText("Password and Confirmation Password don't match");
            newPasswordErrorMessage.setLayoutX(440);
            newPasswordErrorMessage.setVisible(true);
            newPasswordInputField.getStyleClass().add("text-field-error");
            newConfirmationPasswordField.getStyleClass().add("text-field-error");
            return true;
        } else {
            return false;
        }
    }

    public boolean loginValidation() {
        String emailInput = signInEmailInputField.getText();
        String passwordInput = signInPasswordInputField.getText();
        try (Connection connection = databaseManager.databaseConnection()) {
            boolean emailExists = userExists(emailInput);
            if (emailExists) {
                String storedPassword = databaseManager.getUserPassword(connection, emailInput);
                if (storedPassword != null && storedPassword.equals(passwordInput)) {
                    this.email = emailInput;
                    return true;
                } else {
                    emailExistsError.setText("Invalid email or password, please try again");
                    emailExistsError.setVisible(true);
                    return false;
                }
            } else {
                emailExistsError.setText("Invalid email or password, please try again");
                emailExistsError.setVisible(true);
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            emailExistsError.setText("Invalid email or password, please try again");
            emailExistsError.setVisible(true);
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean userExists(String emailInput) throws IOException {
        Connection connection = databaseManager.databaseConnection();
        Boolean emailExists = databaseManager.checkIfColumnValueExists(connection, "Email", emailInput);

        if (emailExists == true) {
            return true;
        } else {
            errorMessage.setVisible(true);
            errorMessage.setLayoutX(440);
            errorMessage.setText("Account doesn't exist");
            return false;
        }
    }

    public boolean requiredFieldsMissing() {
        String emailInput = signInEmailInputField.getText();
        String passwordInput = signInPasswordInputField.getText();

        signInEmailInputField.getStyleClass().remove("text-field-error-rounded");
        signInPasswordInputField.getStyleClass().remove("text-field-error-rounded");

        if (emailInput.isBlank() && passwordInput.isBlank() == true) {
            signInEmailInputField.getStyleClass().add("text-field-error-rounded");
            signInPasswordInputField.getStyleClass().add("text-field-error-rounded");
            return true;
        }
        if (emailInput.isBlank() == true) {
            signInEmailInputField.getStyleClass().add("text-field-error-rounded");
            return true;
        }
        if (passwordInput.isBlank() == true) {
            signInPasswordInputField.getStyleClass().add("text-field-error-rounded");
            return true;
        }
        return false;
    }


    @FXML
    protected void displayServerNameAnswer(MouseEvent event) {
        String message = " There are a few different ways to find your server name. Here are a few suggestions:" +
                "\n \n Database Management Settings: " +
                "\n If you are using a database management system then the server name will specified in your connection settings. This will differ " +
                "\n based on the exact management system you have. For example, if you are using SQL Server Management Studio, you can find this " +
                "\n by referencing the ‘Connect to Server' window that populates when you first start the application." +
                "\n \n Application Settings: " +
                "\n If you are using a specific application that is not a database management system, then the application should have this" +
                "\n information stored somewhere such as in preferences or settings." +
                "\n \n IT Department: " +
                "\n If you are connecting to a company database then it is recommended that you reach out to the IT " +
                "\n department of your company as they will be able to assist you.";

        if (event.getButton() == MouseButton.PRIMARY) {
            displayAnswer(event, message);
        }
    }

    @FXML
    protected void displayDBFailureAnswer(MouseEvent event) {
        String message = " Please see the following for a list of possible reason your database connection is failing." +
                "\n \n Incorrect Login Information: " +
                "\n One common reason is due to incorrect login credentials. If the your database username or password are incorrect you can update" +
                "\n both in your profile." +
                "\n \n Network Restrictions: " +
                "\n Though this application is equip to handle database connections, your database may have settings in place that prevent a successful" +
                "\n connection from happening. Be sure that you do not have any firewall restrictions in place would cause the database to be inaccessible." +
                "\n information stored somewhere such as in preferences or settings." +
                "\n \n Incorrect Database Selection: " +
                "\n In the process of creating your account, did you select the checkbox indicating that you were using a local database? If this checkbox" +
                "\n was checked and you are not using a local database or vice versa, then the connection will fail. If you beleive this is the case, try " +
                "\n updating this accordingly in your profile section.";

        if (event.getButton() == MouseButton.PRIMARY) {
            displayAnswer(event, message);
        }
    }

    @FXML
    protected void displayEmptyDropdownAnswer(MouseEvent event) {
        String message = " Incorrect Permissions: " +
                "\n The main reason for this is because user you have set for your account likely does not have select permissions enabled. In order to interact " +
                "\n with the main SQL Converter you must have select permissions enabled. ";

        if (event.getButton() == MouseButton.PRIMARY) {
            displayAnswer(event, message);
        }
    }

    @FXML
    protected void displayPasswordChangeAnswer(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            displayAnswer(event, "password update");
        }
    }

    @FXML
    protected void displayAccountDifferencesAnswer(MouseEvent event) {
        String message = " Standard Account: " +
                "\n Standard Accounts are meant for those who only need to provide access to a single user. Users in this group include students and hobbyists" +
                "\n both in your profile." +
                "\n \n Enterprise Account: " +
                "\n Enterprise Accounts are meant for use in organizational settings whether that is with a large company or a small start-up. These accounts" +
                "\n allow one main user to establish sub-accounts in order for those within the organization to get access to the database.";

        if (event.getButton() == MouseButton.PRIMARY) {
            displayAnswer(event, message);
        }
    }

    @FXML
    protected void updateDatebaseInfoAnswer(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            displayAnswer(event, "db info update");
        }
    }

    @FXML
    protected void displayAnswer(MouseEvent event, String message) {
        ComboBox<String> comboBox = (ComboBox<String>) event.getSource();
        Label answerLabel = null;

        if (comboBox == serverNameLocation) {
            answerLabel = serverNameLocationAnswer;
        } else if (comboBox == dbConnectFailure) {
            answerLabel = dbConnectFailureAnswer;
        } else if (comboBox == passwordChange) {
            answerLabel = passwordChangeAnswer;
        } else if (comboBox == accountDifferences) {
            answerLabel = accountDifferencesAnswer;
        } else if (comboBox == updateDatebaseInfo) {
            answerLabel = updateDatebaseInfoAnswer;
        } else if (comboBox == emptyDropdown) {
            answerLabel = emptyDropdownAnswer;
        }

        if (event.getButton() == MouseButton.PRIMARY && answerLabel != null) {
            comboBox.hide();
            isAnswerVisible = !isAnswerVisible;
            answerLabel.setText(message);
            answerLabel.setVisible(isAnswerVisible);
            answerLabel.setManaged(isAnswerVisible);
            int rotation = isAnswerVisible ? 0 : 90;
            comboBox.lookup(".arrow-button").setStyle("-fx-rotate: " + rotation + ";");
        }
    }

    public void onBackToHomeButtonClick() throws IOException {
        SQLConverterController sqlController = setupPage("sql-converter.fxml", "SQL Converter");
        SessionService sessionService = SessionService.getInstance();
        sqlController.setEmail(sessionService.getEmail());
        Connection connection = sqlController.setConnection(sessionService.getConnection());
        sqlController.populateStaticRow(connection);
    }

    public void onUpdatePasswordButtonClick() throws IOException {
        this.email = resetEmailField.getText();
        if (this.email.isBlank() || this.email.isEmpty()) {
            errorMessage.setVisible(true);
            errorMessage.setText("Fields cannot be blank");
            resetEmailField.getStyleClass().add("text-field-error");
        } else {
            if (userExists(this.email) == true) {
                FXMLLoader fxmlLoader = loadPage("validate-security-questions-page.fxml");
                AnchorPane validateSecurityQuestionsPage = fxmlLoader.getRoot();
                Controller controller = fxmlLoader.getController();

                SessionService sessionService = SessionService.getInstance();
                controller.setEmail(sessionService.getEmail());
                Connection userConnection = controller.setConnection(sessionService.getConnection());

                Scene currentScene = setRootAndGetScene(validateSecurityQuestionsPage);
                setStage(currentScene, "Security Question Confirmation");

                Button backToHomeButton = (Button) validateSecurityQuestionsPage.lookup("#backToHomeButton");
                backToHomeButton.setVisible(true);

                String[] securityQuestions = databaseManager.getSecurityQuestions(this.email);
                controller.question1.setText(securityQuestions[0]);
                controller.question2.setText(securityQuestions[1]);
                controller.setEmail(this.email);
            }
        }
    }

    @FXML
    protected void onEnterPasswordUpdateButtonClick() throws IOException, SQLException {
        if (!newPasswordFieldsValidation()) {
            databaseManager.updateNewPassword(this.email, newPasswordInputField.getText());
            setupPage("password-update-confirmation-page.fxml", "Password Successfully Updated");
            FXMLLoader fxmlLoader = loadPage("password-update-confirmation-page.fxml");
            AnchorPane passwordUpdateConfirmationPage = fxmlLoader.getRoot();
            Label updateConfirmationLabel = (Label) passwordUpdateConfirmationPage.lookup("#updateConfirmationLabel");
            updateConfirmationLabel.setText("You must login again");

            if(databaseManager.checkForTempPassword(this.email)) {
                databaseManager.resetTempPassword(this.email);
            }
        }
    }

    @FXML
    protected void onValidateSecurityQuestionsUpdateClick() throws Exception {
        String response1Input = response1.getText();
        String response2Input = response2.getText();
        boolean validateResponse = databaseManager.validateSecurityAnswers(email, response1Input, response2Input);

        if (response1Input.isEmpty() || response2Input.isEmpty()) {
            response1.getStyleClass().add("text-field-error");
            response2.getStyleClass().add("text-field-error");
            errorMessage.setText("Fields cannot be blank");
            errorMessage.setVisible(true);
            errorMessage.setLayoutX(415);
            errorMessage.setLayoutY(460);
        } else {
            if (validateResponse) {
                Controller controller = setupPage("enter-new-password-page.fxml", "Enter New Password");
                FXMLLoader fxmlLoader = loadPage("enter-new-password-page.fxml");
                AnchorPane newPasswordPage = fxmlLoader.getRoot();
                Button backToHomeButton = (Button) newPasswordPage.lookup("#backToHomeButton");
                backToHomeButton.setVisible(true);
                controller.setEmail(this.email);
            } else {
                errorMessage.setText("Incorrect security question response. Please try again");
                errorMessage.setVisible(true);
                errorMessage.setLayoutX(310);
                errorMessage.setLayoutY(460);
            }
        }
    }
}