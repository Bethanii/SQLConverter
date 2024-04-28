package com.example.myjavafxapp;

import javafx.scene.control.ProgressIndicator;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import java.sql.SQLException;
import javafx.stage.Window;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;
import javafx.fxml.FXML;
import java.util.List;

public class AccountController {

    @FXML private Label welcomeText, connectionError, securityQuestionErrorMessage, enterpriseAccountInputError, standardAccountInputError,
            connectionSuccess, errorMessage;
    @FXML private TextField standardUsernameInputField, standardPasswordInputField, standardConfirmPasswordField, serverNameField,
            dbUsernameField, dbPasswordField, databaseNameField, firstSecurityQuestionInput, secondSecurityQuestionInput,
            subUserEmailInputField, enterpriseUsernameInputField, enterprisePasswordInputField, enterpriseConfirmPasswordField,
            tempPasswordInputField;
    @FXML private ChoiceBox<String> firstSecurityQuestion, secondSecurityQuestion;
    @FXML private TextArea emailsDisplayArea;
    @FXML private CheckBox localDBCheckbox;
    @FXML private Button cancelOption, exitOption;
    private Stage loadingStage;
    private String email;
    private List<String> emails = new ArrayList<>();
    private DatabaseManager databaseManager = new DatabaseManager();

    /**
     * Loads a specif page, sets the controller, and updates the current scene.
     * @param pageFile Path to the FXML file.
     * @param pageTitle Title for the page.
     * @param email User's email to set in the controller.
     * @param controllerSelection Sets which controller to configure.
     * @param setSecurityQuestions Determines whether to set security questions.
     * @param emails List of emails to set in the controller.
     * @throws IOException If an error occurs during loading.
     */
    public void loadPage(String pageFile, String pageTitle, String email, String controllerSelection,
                         boolean setSecurityQuestions, List<String> emails) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource(pageFile));
        Parent page = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(page);
        page.requestFocus();
        switch (controllerSelection) {
            case "Account Controller":
                AccountController accountController = fxmlLoader.getController();
                accountController.setEmail(email);
                if (email != null) {
                    accountController.setEmail(email);
                }
                if (emails != null) {
                    accountController.setEmails(emails);
                }
                if (setSecurityQuestions) {
                    accountController.setSecurityQuestionOptions();
                }
                break;
            case "Controller":
                Controller controller = fxmlLoader.getController();
                controller.setEmail(email);
                if (email != null) {
                    controller.setEmail(email);
                }
                break;
        }
        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle(pageTitle);
    }

    /**
     * Sets the user email.
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets user's email.
     * @param emails The emails to set.
     */
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    /**
     * Sets the user email and navigates to the Security Question page.
     * @throws IOException If an I/O error occurs.
     * @throws SQLException If a database access error occurs.
     */
    @FXML
    protected void onStandardAccountNextButtonClick() throws IOException, SQLException {
        SessionService sessionService = SessionService.getInstance();
        sessionService.setEmail(email);
        accountNextButtonClick("standard");
    }

    /**
     * Sets the user email and navigates to the Sub-User Info page.
     * @throws IOException If an I/O error occurs.
     * @throws SQLException If a database access error occurs.
     */
    @FXML
    protected void onEnterpriseAccountNextButtonClick() throws IOException, SQLException {
        SessionService sessionService = SessionService.getInstance();
        sessionService.setEmail(this.email);
        accountNextButtonClick("enterprise");
    }

    /**
     * Saves the sub-users emails and loads the temporary password setup page.
     * @throws IOException If an I/O error occurs.
     */
    @FXML
    protected void onEnterpriseSubUsersNextButtonClick() throws IOException {
        databaseManager.saveEnterpriseSubUserEmails(emails, this.email);
        loadPage("sub-users-temporary-password.fxml", "Enterprise Account Sub-User Temporary Password",
                this.email, "Account Controller", false, emails);
        AccountController controller = new AccountController();
        controller.setEmail(this.email);
        controller.setEmails(emails);
    }

    /**
     * Sets the temporary password for sub-users and navigates to the Enterprise Security Question page.
     * @throws IOException If an error occurs while loading the security question page.
     */
    @FXML
    protected void onTempPasswordNextButtonClick() throws IOException {
        String tempPassword = tempPasswordInputField.getText().trim();
        if (tempPassword.isEmpty() && !emails.isEmpty()) {
            errorMessage.setVisible(true);
            tempPasswordInputField.getStyleClass().add("text-field-error");
        } else {
            errorMessage.setVisible(false);
            for (String email : emails) {
                databaseManager.setTempPassword(email, tempPassword);
            }
            AccountController controller = new AccountController();
            controller.setEmails(emails);
            loadPage("enterprise-security-question-page.fxml", "Enterprise Account Security Questions",
                    this.email, "Account Controller", true, emails);
        }
    }

    /**
     * Validates required account set-uo fields are missing and validates if email already exists
     * If no required fields are missing and email does not exist the next page based on account type will display.
     * @param accountType The account type.
     * @throws IOException If an error occurs while loading the next page.
     * @throws SQLException If a database error occurs.
     */
    @FXML
    protected void accountNextButtonClick(String accountType) throws IOException, SQLException {
        TextField emailInputField = accountType.equals("standard") ? standardUsernameInputField : enterpriseUsernameInputField;
        TextField passwordInputField = accountType.equals("standard") ? standardPasswordInputField : enterprisePasswordInputField;
        TextField confirmPasswordField = accountType.equals("standard") ? standardConfirmPasswordField : enterpriseConfirmPasswordField;
        Label accountInputError = accountType.equals("standard") ? standardAccountInputError : enterpriseAccountInputError;
        CheckBox localDbCheckbox = accountType.equals("standard") ? localDBCheckbox : localDBCheckbox;
        String nextPageFxml = accountType.equals("standard") ? "security-question-page.fxml" : "enterprise-account-sub-users.fxml";
        String nextPageTitle = accountType.equals("standard") ? "Standard Account Security Questions" : "Enterprise Account Sub User Information";
        boolean isStandard = accountType.equals("standard");

        String emailInput = emailInputField.getText();
        String passwordInput = passwordInputField.getText();
        String passwordInputConfirmation = confirmPasswordField.getText();

        Connection connection = databaseManager.databaseConnection();
        Boolean emailExists = databaseManager.checkIfColumnValueExists(connection, "Email", emailInput);
        Boolean passwordMatches = validatePasswordsMatch(passwordInput, passwordInputConfirmation);

        if (emailInput.isEmpty() || emailInput.isBlank() || passwordInput.isEmpty() || passwordInput.isBlank()
                || passwordInputConfirmation.isEmpty() || passwordInputConfirmation.isBlank()) {
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
                    databaseManager.saveUserDetails(connection, emailInput, passwordInput);
                    this.email = emailInput;
                    SessionService sessionService = SessionService.getInstance();
                    sessionService.setEmail(this.email);
                    loadPage(nextPageFxml, nextPageTitle, this.email, "Account Controller",
                            isStandard, null);
                    if (accountType.equals("enterprise")) {
                        databaseManager.updateIsAccountOwnerFlag(connection, this.email);
                    }
                }
            } else {
                accountInputError.setVisible(true);
                accountInputError.setLayoutX(400);
                accountInputError.setText("Email already exists, please log in");
                emailInputField.getStyleClass().add("text-field-error");
            }
        }
    }

    /**
     * Validates that password input match
     * @param password The first password.
     * @param passwordConfirmation The confirmation password.
     * @return true if both passwords match, false if not.
     */
    public boolean validatePasswordsMatch(String password, String passwordConfirmation) {
        if (!Objects.equals(password, passwordConfirmation)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Sets security question dropdowns and listeners to handle selection changes.
     */
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

    /**
     * Validates if security question dropdown selections are missing.
     * @return true if a dropdown is unselected, false if not.
     */
    @FXML
    protected boolean securityQuestionMissingQuestions() {
        if (firstSecurityQuestion.getSelectionModel().getSelectedItem() == null ||
                secondSecurityQuestion.getSelectionModel().getSelectedItem() == null) {
            securityQuestionErrorMessage.setText("You must select a question from the above dropdowns");
            securityQuestionErrorMessage.setVisible(true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if required fields are missing on the Security Question page.
     * @param fields Fields on the Security Question page.
     * @return true if any field is missing, false if not.
     * @throws IOException If an I/O error occurs.
     */
    @FXML
    protected boolean securityQuestionMissingFields(TextField... fields) throws IOException {
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

    /**
     * Validates whether required fields are missing, if not the user questions and answers are saved.
     * @throws IOException If an error occurs while loading.
     * @throws SQLException If an database connection error occurs.
     */
    @FXML
    protected void onSecurityQuestionNextButtonClick() throws IOException, SQLException {
        if (!securityQuestionMissingFields(firstSecurityQuestionInput, secondSecurityQuestionInput)) {
            String firstAnswer = firstSecurityQuestionInput.getText();
            String secondAnswer = secondSecurityQuestionInput.getText();
            if (!securityQuestionMissingQuestions()) {
                String firstQuestion = firstSecurityQuestion.getSelectionModel().getSelectedItem().toString();
                String secondQuestion = secondSecurityQuestion.getSelectionModel().getSelectedItem().toString();
                databaseManager.saveSecurityQuestions(firstAnswer, secondAnswer, firstQuestion, secondQuestion, this.email);
                boolean isAccountOwner = databaseManager.checkIfAccountOwner(databaseManager.databaseConnection(), this.email);
                if (isAccountOwner) {
                    loadPage("user-database-setup-page.fxml", "Database Setup Information", this.email,
                            "Account Controller", false, emails);
                } else {
                    loadPage("user-database-setup-page.fxml", "Database Setup Information", this.email,
                            "Account Controller", false, null);
                }
            }
        }
    }

    /**
     * Checks if required fields are missing from the Database Set-Up page and if the current
     * user is an account owner, then saves the user database information.
     * @throws SQLException If a database access error occurs.
     * @throws IOException  If an I/O error occurs.
     */
    @FXML
    protected void onDatabaseSetupNextButton() throws SQLException, IOException {
        String serverName = serverNameField.getText();
        String databaseName = databaseNameField.getText();
        String dbUsername = dbUsernameField.getText();
        String dbPassword = dbPasswordField.getText();

        if (serverName.isEmpty() || databaseName.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty()) {
            connectionError.setText("Fields cannot be blank");
            connectionError.setLayoutX(440);
            connectionError.setVisible(true);
            serverNameField.getStyleClass().add("text-field-error");
            databaseNameField.getStyleClass().add("text-field-error");
            dbUsernameField.getStyleClass().add("text-field-error");
            dbPasswordField.getStyleClass().add("text-field-error");
        } else {
            Connection connection = databaseManager.databaseConnection();
            boolean isAccountOwner = databaseManager.checkIfAccountOwner(connection, this.email);
            if (isAccountOwner) {
                databaseManager.saveEnterpriseSubUserDBInfo(emails, serverName, databaseName, dbUsername, dbPassword, this.email);
                databaseManager.saveUserDBInfo(connection, serverName, databaseName, dbUsername, dbPassword, this.email);
            } else {
                databaseManager.saveUserDBInfo(connection, serverName, databaseName, dbUsername, dbPassword, this.email);
            }
            loadPage("account-creation-confirmation-page.fxml", "Account Creation Confirmation", this.email,
                    "Controller", false, null);
        }
    }

    /**
     * Gets the input values for server name, database name, database username, and password.
     * If no fields are missing a loading popup will display while attempting to connect to the user database
     * @throws IOException If an I/O error occurs.
     */
    @FXML
    protected void onTestConnectionButton() throws IOException {
        String serverName = serverNameField.getText();
        String databaseName = databaseNameField.getText();
        String dbUsername = dbUsernameField.getText();
        String dbPassword = dbPasswordField.getText();
        final boolean isLocalDB = localDBCheckbox.isSelected();
        final String userEmail = this.email;

        if (serverName.isEmpty() || databaseName.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty()) {
            connectionError.setText("Fields cannot be blank");
            connectionError.setLayoutX(440);
            connectionError.setVisible(true);
            serverNameField.getStyleClass().add("text-field-error");
            databaseNameField.getStyleClass().add("text-field-error");
            dbUsernameField.getStyleClass().add("text-field-error");
            dbPasswordField.getStyleClass().add("text-field-error");
            return;
        }
        Stage primaryStage = (Stage) dbUsernameField.getScene().getWindow();
        showDatabaseLoadingPopup(primaryStage);
        new Thread(() -> {
            Connection userConnection = null;
            try {
                userConnection = databaseManager.connectUserDatabase(serverName, databaseName, dbUsername, dbPassword, isLocalDB);
                final boolean connectionSuccess = userConnection != null;
                Platform.runLater(() -> {
                    if (!connectionSuccess) {
                        this.connectionError.setVisible(true);
                        this.connectionSuccess.setVisible(false);
                    } else {
                        this.connectionSuccess.setVisible(true);
                        this.connectionError.setVisible(false);
                    }
                    hideLoadingPopup();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    connectionError.setVisible(true);
                    this.connectionSuccess.setVisible(false);
                    hideLoadingPopup();
                });
            }
        }).start();
    }

    /**
     * Displays a loading popup while testing the database connection.
     * @param primaryStage The primary stage owning the popup.
     */
    public void showDatabaseLoadingPopup(Stage primaryStage) {
        if (loadingStage != null) {
            loadingStage.close();
        }
        loadingStage = new Stage(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initOwner(primaryStage);

        ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressIndicator.setStyle("-fx-progress-color: blue;");

        Label loadingLabel = new Label("Testing Connection...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label infoLabel = new Label("Please wait while we test the database connection");
        infoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        VBox loadingPane = new VBox(10);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.getChildren().addAll(progressIndicator, loadingLabel, infoLabel);
        loadingPane.setStyle("-fx-padding: 20; -fx-background-color: rgba(0, 0, 0, 0.75);");

        Scene loadingScene = new Scene(loadingPane, 400, 200);
        loadingStage.setScene(loadingScene);
        loadingStage.show();
    }

    /**
     * Hides the loading popup.
     */
    private void hideLoadingPopup() {
        if (loadingStage != null) {
            loadingStage.close();
        }
    }

    @FXML
    protected void addEmailField() throws SQLException {
        String emailText = subUserEmailInputField.getText().trim();
        Connection connection = databaseManager.databaseConnection();
        boolean usernameExists = databaseManager.checkIfColumnValueExists(connection, "Email", emailText);

        if (usernameExists) {
            subUserEmailInputField.getStyleClass().add("text-field-error");
            errorMessage.setVisible(true);
        } else {
            if (emails.contains(emailText)) {
                subUserEmailInputField.getStyleClass().add("text-field-error");
                errorMessage.setText("Cannot enter duplicate usernames");
                errorMessage.setLayoutX(380);
                errorMessage.setVisible(true);
            } else {
                subUserEmailInputField.getStyleClass().remove("text-field-error");
                errorMessage.setVisible(false);
                emails.add(emailText);
                displayEmails(emails);
                subUserEmailInputField.clear();
            }
        }
    }

    public void displayEmails(List<String> emails) {
        String content = String.join("\n", emails);
        emailsDisplayArea.setText(content);
    }

    @FXML
    public void onExitButtonClick() {
        showExitPopup();
    }

    public void showExitPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("exit-confirmation-popup.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.setTitle("Exit Account Creation");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPopUpCancel() {
        Stage stage = (Stage) cancelOption.getScene().getWindow();
        stage.close();
    }

    public void onPopUpExit() throws IOException, SQLException {
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage && !"Landing Page".equals(((Stage) window).getTitle())) {
                ((Stage) window).close();
            }
        }
        SessionService sessionService = SessionService.getInstance();
        String email = sessionService.getEmail();
        Connection connection = databaseManager.databaseConnection();
        if(databaseManager.checkIfAccountOwner(connection, email)) {
            databaseManager.deleteSubUserDetails(email);
        }
        databaseManager.deleteUserDetails(connection, email);
        loadPage("landing-page.fxml", "Welcome", null,
                "Controller", false, null);
    }
}