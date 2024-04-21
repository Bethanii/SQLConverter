package com.example.myjavafxapp;

import javafx.scene.control.ProgressIndicator;
import javafx.application.Platform;
import java.sql.PreparedStatement;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import java.sql.SQLException;
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

    @FXML private Label welcomeText, connectionError, securityQuestionErrorMessage, enterpriseAccountInputError, standardAccountInputError, connectionSuccess;
    @FXML private TextField standardEmailInputField, standardPasswordInputField, standardConfirmPasswordField, serverNameField, dbUsernameField, dbPasswordField, databaseNameField,
            firstSecurityQuestionInput, secondSecurityQuestionInput, subUserEmailInputField, enterpriseEmailInputField, enterprisePasswordInputField, enterpriseConfirmPasswordField,
            tempPasswordInputField;
    @FXML private ChoiceBox<String> firstSecurityQuestion, secondSecurityQuestion;
    @FXML private TextArea emailsDisplayArea;
    @FXML private CheckBox localDBCheckbox;
    private Stage loadingStage;
    private String email;
    private List<String> emails = new ArrayList<>();
    private DatabaseManager databaseManager = new DatabaseManager();

    public void loadPage(String pageFile, String pageTitle, String email, String controllerSelection, boolean setSecurityQuestions, List<String> emails) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource(pageFile));
        Parent page = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(page);
        page.requestFocus();

        switch(controllerSelection) {
            case"Account Controller":
                AccountController accountController = fxmlLoader.getController();
                accountController.setEmail(email);
                if (email != null) {
                    accountController.setEmail(email);
                }
                if(emails != null)
                {
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

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    @FXML
    public void onEnterpriseAccountBackButtonClick() throws IOException {
        loadPage("select-account-type-page.fxml", "Select Account Type", "", "Controller", false, null);
    }

    @FXML
    public void onStandardAccountBackButtonClick() throws IOException {
        loadPage("select-account-type-page.fxml", "Select Account Type", "", "Controller", false, null);
    }

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
                    databaseManager.saveUserDetails(connection, emailInput, passwordInput);
                    this.email = emailInput;
                    loadPage(nextPageFxml, nextPageTitle, this.email, "Account Controller", isStandard, null);
                }
            } else {
                accountInputError.setVisible(true);
                accountInputError.setLayoutX(400);
                accountInputError.setText("Email already exists, please log in");
                emailInputField.getStyleClass().add("text-field-error");
            }
        }
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
    protected boolean securityQuestionMissingQuestions() {
        if (firstSecurityQuestion.getSelectionModel().getSelectedItem() == null || secondSecurityQuestion.getSelectionModel().getSelectedItem() == null) {
            securityQuestionErrorMessage.setText("You must select a question from the above dropdowns");
            securityQuestionErrorMessage.setVisible(true);
            return true;
        } else {
            return false;
        }
    }

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

    @FXML
    protected void onSecurityQuestionNextButtonClick() throws IOException {
        if (!securityQuestionMissingFields(firstSecurityQuestionInput, secondSecurityQuestionInput))
        {
            String firstAnswer = firstSecurityQuestionInput.getText();
            String secondAnswer = secondSecurityQuestionInput.getText();
            if (!securityQuestionMissingQuestions()) {
                String firstQuestion = firstSecurityQuestion.getSelectionModel().getSelectedItem().toString();
                String secondQuestion = secondSecurityQuestion.getSelectionModel().getSelectedItem().toString();
                databaseManager.saveSecurityQuestions(firstAnswer, secondAnswer, firstQuestion, secondQuestion, this.email);
                loadPage("user-database-setup-page.fxml", "Database Setup Information", this.email, "Account Controller", false, null);
            }
        }
    }

    @FXML
    protected void onDatabaseSetupNextButton() throws IOException {
        String serverName = serverNameField.getText();
        String databaseName = databaseNameField.getText();
        String dbUsername = dbUsernameField.getText();
        String dbPassword = dbPasswordField.getText();
        Connection connection = null;

        if (serverName.isEmpty() || databaseName.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty()) {
            connectionError.setText("Fields cannot be blank");
            connectionError.setLayoutX(440);
            connectionError.setVisible(true);
            serverNameField.getStyleClass().add("text-field-error");
            databaseNameField.getStyleClass().add("text-field-error");
            dbUsernameField.getStyleClass().add("text-field-error");
            dbPasswordField.getStyleClass().add("text-field-error");
        } else {
            connection = databaseManager.databaseConnection();
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
            loadPage("account-creation-confirmation-page.fxml", "Account Creation Confirmation", this.email, "Controller", false, null);
        }
    }

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

    private void hideLoadingPopup() {
        if (loadingStage != null) {
            loadingStage.close();
        }
    }

    @FXML
    protected void onEnterpriseSubUsersNextButtonClick() throws IOException {
        databaseManager.saveEnterpriseSubUserEmails(emails);
        loadPage("sub-users-temporary-password.fxml", "Enterprise Account Sub-User Temporary Password", this.email, "Account Controller", false, this.emails);
        AccountController controller = new AccountController();
        controller.setEmail(this.email);
        controller.setEmails(emails);
    }

    @FXML
    protected void onTempPasswordNextButtonClick() throws IOException {
        String tempPassword = tempPasswordInputField.getText().trim();
        for (String email : emails) {
            databaseManager.setTempPassword(email, tempPassword);
        }
        loadPage("enterprise-security-question-page.fxml", "Enterprise Account Security Questions", this.email, "Account Controller", true, null);
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