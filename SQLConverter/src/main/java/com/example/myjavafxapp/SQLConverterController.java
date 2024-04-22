package com.example.myjavafxapp;

import javafx.scene.layout.AnchorPane;
import java.io.FileNotFoundException;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import java.util.ArrayList;
import javafx.geometry.Pos;
import java.io.IOException;
import java.io.PrintWriter;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXML;
import java.util.List;
import java.io.File;
import java.sql.*;

public class SQLConverterController {
    @FXML private ChoiceBox<String> searchRow1;
    @FXML private TextField searchField1;
    @FXML private VBox dynamicRowsContainer;
    @FXML private VBox resultsContainer;
    @FXML private Label welcomeText, connectionError, connectionSuccess, updateSuccessMessage, errorMessage;
    @FXML private TextField activeTextField, serverNameField, databaseNameField, dbUsernameField, dbPasswordField, updateUsernameInput;
    @FXML private CheckBox localDBCheckbox;
    @FXML private AnchorPane updateUserDBPage, sqlConverterPage, resetPasswordPage, standardAccountPage;
    private Stage loadingStage;
    @FXML MenuItem updateDatabase;
    private ChoiceBox<String> activeChoiceBox;
    private Connection userConnection;
    private DatabaseManager databaseManager;
    private String email;
    private SessionService sessionService;

    @FXML
    public void initialize() throws IOException, SQLException {
        SessionService sessionService = SessionService.getInstance();
        if (sessionService != null) {
            setEmail(sessionService.getEmail());
            setConnection(sessionService.getConnection());
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void disableUpdateDatabaseMenuItem() {
        if (updateDatabase != null) {
            updateDatabase.setDisable(true);
        }
    }

    public Connection setConnection() throws SQLException, IOException {
        this.databaseManager = new DatabaseManager();
        String[] dbValues = databaseManager.getUserDBInfo(this.email);

        if (dbValues == null) {
            return null;
        }
        boolean localDb = databaseManager.checkForLocalDB(databaseManager.databaseConnection(), this.email);
        if (localDb) {
            this.userConnection = databaseManager.connectUserDatabase(dbValues[0], dbValues[1], dbValues[2], dbValues[3], true);
            return this.userConnection;
        } else {
            this.userConnection = databaseManager.connectUserDatabase(dbValues[0], dbValues[1], dbValues[2], dbValues[3], false);
            return this.userConnection;
        }
    }

    public Connection setConnection(Connection userConnection) throws IOException, SQLException {
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

    @FXML
    protected void populateStaticRow(Connection userConnection)
    {
        try {
            searchRow1.getItems().addAll(getTableNames(userConnection));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSearchButtonClick() throws IOException, SQLException {
        List<String> results = new ArrayList<>();

        if (searchRow1.getValue() != null && !searchField1.getText().isEmpty()) {
            results.addAll(querySelectedTables(searchRow1.getValue(), searchField1.getText()));
        }

        for (HBox hbox : dynamicRowsContainer.getChildren().stream().map(node -> (HBox) node).toList()) {
            ChoiceBox<String> choiceBox = (ChoiceBox<String>) hbox.getChildren().get(0);
            TextField textField = (TextField) hbox.getChildren().get(1);

            if (choiceBox.getValue() != null && !textField.getText().isEmpty()) {
                results.addAll(querySelectedTables(choiceBox.getValue(), textField.getText()));
            }
        }
        displayQueryResults(results);
    }

    public List<String> getTableNames(Connection connection) {
        List<String> tableNames = new ArrayList<>();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, "dbo", "%", new String[]{"TABLE"});
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tableNames;
    }

    public List<String> getColumnNames(Connection connection, String tableName) {
        List<String> columnNames = new ArrayList<>();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            while (columns.next()) {
                columnNames.add(columns.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columnNames;
    }

    private List<String> querySelectedTables(String tableName, String searchText) throws SQLException, IOException {
        List<String> results = new ArrayList<>();
        SessionService sessionService = SessionService.getInstance();

        SQLConverterController sqlController = new SQLConverterController();
        sqlController.setEmail(sessionService.getEmail());
        this.userConnection = setConnection(sessionService.getConnection());
        List<String> columnNames = getColumnNames(this.userConnection, tableName);

        for (String columnName : columnNames) {
            String query = "SELECT * FROM " + tableName + " WHERE " + columnName + " LIKE ?";
            try (PreparedStatement statement = this.userConnection.prepareStatement(query)) {
                statement.setString(1, "%" + searchText + "%");
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        StringBuilder row = new StringBuilder();
                        ResultSetMetaData rsmd = resultSet.getMetaData();
                        int columnCount = rsmd.getColumnCount();
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) row.append(", ");
                            row.append(rsmd.getColumnName(i)).append(": ").append(resultSet.getString(i));
                        }
                            results.add(row.toString());
                        }
                    }
                }
            }
        return results;
    }

    public void displayQueryResults(List<String> results) {
        resultsContainer.getChildren().clear();
        for (String row : results) {
            Label rowLabel = new Label(row);
            rowLabel.setStyle("-fx-padding: 5;");
            resultsContainer.getChildren().add(rowLabel);
        }
    }

    @FXML
    protected void onAddRowButtonClick() {
        ChoiceBox<String> newChoiceBox = new ChoiceBox<>();
        newChoiceBox.setPrefWidth(167.0);
        newChoiceBox.setPrefHeight(33.0);

        TextField newTextField = new TextField();
        newTextField.setPrefWidth(250.0);
        newTextField.setPrefHeight(33.0);
        newTextField.setPromptText("Enter value");

        newChoiceBox.getItems().addAll(getTableNames(this.userConnection));
        HBox newRow = new HBox(10);
        newRow.getChildren().addAll(newChoiceBox, newTextField);

        dynamicRowsContainer.getChildren().add(newRow);
        newChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> activeChoiceBox = newChoiceBox);
        newTextField.focusedProperty().addListener((obs, wasFocused, isNowFocused) ->
        {
            if (isNowFocused) {
                activeTextField = newTextField;
            }
        });
    }

    @FXML
    protected void onExportButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("SearchResults.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                for (Node node : resultsContainer.getChildren()) {
                    if (node instanceof Label) {
                        String text = ((Label) node).getText();
                        String csvLine = text.replace(", ", ",");
                        writer.println(csvLine);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void goToUserDatabaseSetupPage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("update-database-info.fxml"));
        AnchorPane updateUserDBPage = fxmlLoader.load();
        SQLConverterController controller = fxmlLoader.getController();
        controller.setEmail(this.email);

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(updateUserDBPage);
        updateUserDBPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Update User Database Information");
    }

    public void onUpdateDatabaseInfoButton() throws IOException, SQLException {
        String serverName = serverNameField.getText();
        String databaseName = databaseNameField.getText();
        String dbUsername = dbUsernameField.getText();
        String dbPassword = dbPasswordField.getText();

        if (serverName.isEmpty() || databaseName.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty()) {
            connectionError.setText("Fields cannot be blank");
            connectionError.setLayoutX(450);
            connectionError.setVisible(true);
            serverNameField.getStyleClass().add("text-field-error");
            databaseNameField.getStyleClass().add("text-field-error");
            dbUsernameField.getStyleClass().add("text-field-error");
            dbPasswordField.getStyleClass().add("text-field-error");
        } else {
            connectionError.setVisible(false);
            serverNameField.getStyleClass().remove("text-field-error");
            databaseNameField.getStyleClass().remove("text-field-error");
            dbUsernameField.getStyleClass().remove("text-field-error");
            dbPasswordField.getStyleClass().remove("text-field-error");
            Connection connection = databaseManager.databaseConnection();

            if (localDBCheckbox.isSelected()) {
                databaseManager.saveIsLocalValue(connection, this.email, 1);
            } else {
                databaseManager.saveIsLocalValue(connection, this.email, 0);
            }
            boolean isAccountOwner = databaseManager.checkIfAccountOwner(connection, this.email);
            if(isAccountOwner) {
                databaseManager.updateSubUserDBInfo(connection, serverName, databaseName, dbUsername, dbPassword, this.email);
            }
            databaseManager.saveUserDBInfo(connection, serverName, databaseName, dbUsername, dbPassword, this.email);
        }
    }

    public void onUpdateDBBackButtonClick() throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sql-converter.fxml"));
        AnchorPane sqlConverterPage = fxmlLoader.load();
        SQLConverterController sqlController = fxmlLoader.getController();

        SessionService sessionService = SessionService.getInstance();
        sqlController.setEmail(sessionService.getEmail());
        Connection connection = sqlController.setConnection(sessionService.getConnection());
        sqlController.populateStaticRow(connection);

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(sqlConverterPage);
        sqlConverterPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("SQL Converter");
    }

    public void onUpdateDBTestConnectionButton() {
        String serverName = serverNameField.getText();
        String databaseName = databaseNameField.getText();
        String dbUsername = dbUsernameField.getText();
        String dbPassword = dbPasswordField.getText();

        if (serverName.isEmpty() || databaseName.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty()) {
            connectionError.setText("Fields cannot be blank");
            connectionError.setLayoutX(450);
            connectionError.setVisible(true);
            return;
        }
        Stage primaryStage = (Stage) dbUsernameField.getScene().getWindow();
        showDatabaseLoadingPopup(primaryStage);

        new Thread(() -> {
            DatabaseManager databaseManager = new DatabaseManager();
            final Connection[] userConnection = new Connection[1];
            try {
                userConnection[0] = databaseManager.connectUserDatabase(serverName, databaseName, dbUsername,
                        dbPassword, localDBCheckbox.isSelected());

                Platform.runLater(() -> {
                    if (userConnection[0] == null) {
                        connectionError.setVisible(true);
                        connectionSuccess.setVisible(false);
                    } else {
                        connectionSuccess.setVisible(true);
                        connectionError.setVisible(false);
                    }
                    hideLoadingPopup();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    connectionError.setVisible(true);
                    connectionSuccess.setVisible(false);
                    hideLoadingPopup();
                });
            }
        }).start();
    }

    private void hideLoadingPopup() {
        if (loadingStage != null) {
            loadingStage.close();
        }
    }

    private void showDatabaseLoadingPopup(Stage primaryStage) {
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

    public void goToResetPasswordPage() throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("reset-password-page.fxml"));
        AnchorPane resetPasswordPage = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();

        SessionService sessionService = SessionService.getInstance();
        controller.setEmail(sessionService.getEmail());
        this.userConnection = setConnection(sessionService.getConnection());

        Button backToHomeButton = (Button) resetPasswordPage.lookup("#backToHomeButton");
        backToHomeButton.setVisible(true);

        Button updatePasswordButton = (Button) resetPasswordPage.lookup("#updatePasswordButton");
        updatePasswordButton.setVisible(true);

        Button resetPasswordButton = (Button) resetPasswordPage.lookup("#resetPasswordButton");
        resetPasswordButton.setVisible(false);

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(resetPasswordPage);
        resetPasswordPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Update Password");
    }

    public void goToSignInPage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sign-in-page.fxml"));
        AnchorPane signInPage = fxmlLoader.load();

        Button backButton = (Button) signInPage.lookup("#backButton");
        backButton.setText("Create New Account");
        backButton.setPrefWidth(150);

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(signInPage);
        signInPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Sign In");
    }

    public void goToUserUpdatePage() throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("update-username-page.fxml"));
        AnchorPane updateUsernamePage = fxmlLoader.load();
        SQLConverterController controller = fxmlLoader.getController();

        SessionService sessionService = SessionService.getInstance();
        controller.setEmail(sessionService.getEmail());
        this.userConnection = setConnection(sessionService.getConnection());

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(updateUsernamePage);
        updateUsernamePage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Update Username");
    }

    @FXML
    public void onUpdateUsernameBackButtonClick() throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sql-converter.fxml"));
        AnchorPane sqlConverterPage = fxmlLoader.load();
        SQLConverterController controller = fxmlLoader.getController();

        SessionService sessionService = SessionService.getInstance();
        controller.setEmail(sessionService.getEmail());
        SQLConverterController sqlController = new SQLConverterController();
        Connection connection = controller.setConnection(sessionService.getConnection());
        controller.setEmail(this.email);
        controller.populateStaticRow(connection);

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(sqlConverterPage);
        sqlConverterPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("SQL Converter");
    }

    @FXML
    public void onUpdateUsernameUpdateButtonClick() throws IOException, SQLException {
        String emailInput = updateUsernameInput.getText();
        Connection connection = databaseManager.databaseConnection();

        boolean usernameExists = databaseManager.checkIfColumnValueExists(connection, "Email", emailInput);

        if(usernameExists)
        {
            updateUsernameInput.getStyleClass().add("text-field-error");
            errorMessage.setVisible(true);
            updateSuccessMessage.setVisible(false);
        }
        else
        {
            if (databaseManager.updateUsername(connection, this.email, emailInput))
            {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("username-update-confirmation.fxml"));
                AnchorPane usernameUpdateConfirmation = fxmlLoader.load();
                SQLConverterController controller = fxmlLoader.getController();

                Scene currentScene = welcomeText.getScene();
                currentScene.setRoot(usernameUpdateConfirmation);
                usernameUpdateConfirmation.requestFocus();

                Stage stage = (Stage) currentScene.getWindow();
                stage.sizeToScene();
                stage.setTitle("Username Successfully Updated");
            }
        }
    }

    @FXML
    protected void onSignInSelectionClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sign-in-page.fxml"));
        AnchorPane signInPage = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(signInPage);
        signInPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Log In");
    }
}