package com.example.myjavafxapp;

import javafx.fxml.FXML;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.util.List;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SQLConverterController {
    @FXML private ChoiceBox<String> searchRow1;
    @FXML private TextField searchField1;
    @FXML private VBox dynamicRowsContainer;
    @FXML private VBox resultsContainer;
    @FXML private Label welcomeText, connectionError;
    @FXML private TextField activeTextField, serverNameField, databaseNameField, dbUsernameField, dbPasswordField;
    @FXML private CheckBox localDBCheckbox;
    @FXML
    private AnchorPane updateUserDBPage, sqlConverterPage;
    private ChoiceBox<String> activeChoiceBox;
    private Connection userConnection;
    private DatabaseManager databaseManager;
    private String email;

    @FXML
    public void initialize() throws IOException
    {
   //     SetConnection();
       // populateStaticRow();
    }

    public Connection SetConnection() throws IOException
    {
        this.databaseManager = new DatabaseManager();
        String[] dbValues = databaseManager.GetUserDBInfo(this.email);

        if (dbValues == null) {
            return null;
        }

        boolean localDb = databaseManager.checkForLocalDB(databaseManager.DatabaseConnection(), this.email);

        if (localDb)
        {
            this.userConnection = databaseManager.ConnectUserDatabase(dbValues[0], dbValues[1], dbValues[2], dbValues[3], true);
            return this.userConnection;
        }
        else
        {
            this.userConnection = databaseManager.ConnectUserDatabase(dbValues[0], dbValues[1], dbValues[2], dbValues[3], false);
            return this.userConnection;
        }
    }

    public void populateStaticRow()
    {
        try
        {
            searchRow1.getItems().addAll(getTableNames(this.userConnection));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSearchButtonClick() throws IOException, SQLException {
        List<String> results = new ArrayList<>();

        if (searchRow1.getValue() != null && !searchField1.getText().isEmpty())
        {
            results.addAll(querySelectedTables(searchRow1.getValue(), searchField1.getText()));
        }

        for (HBox hbox : dynamicRowsContainer.getChildren().stream().map(node -> (HBox) node).toList())
        {
            ChoiceBox<String> choiceBox = (ChoiceBox<String>) hbox.getChildren().get(0);
            TextField textField = (TextField) hbox.getChildren().get(1);

            if (choiceBox.getValue() != null && !textField.getText().isEmpty())
            {
                results.addAll(querySelectedTables(choiceBox.getValue(), textField.getText()));
            }
        }
        displayQueryResults(results);
    }

    public List<String> getTableNames(Connection connection)
    {
        List<String> tableNames = new ArrayList<>();
        try
        {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, "dbo", "%", new String[]{"TABLE"});
            while (tables.next())
            {
                tableNames.add(tables.getString("TABLE_NAME"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return tableNames;
    }

    public List<String> getColumnNames(Connection connection, String tableName)
    {
        List<String> columnNames = new ArrayList<>();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            while (columns.next())
            {
                columnNames.add(columns.getString("COLUMN_NAME"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return columnNames;
    }

    private List<String> querySelectedTables(String tableName, String searchText) throws SQLException {
        List<String> results = new ArrayList<>();
        List<String> columnNames = getColumnNames(this.userConnection, tableName);

        for (String columnName : columnNames)
        {
            String query = "SELECT * FROM " + tableName + " WHERE " + columnName + " LIKE ?";
            try (PreparedStatement statement = this.userConnection.prepareStatement(query))
            {
                statement.setString(1, "%" + searchText + "%");

                try (ResultSet resultSet = statement.executeQuery())
                {
                    while (resultSet.next())
                    {
                        StringBuilder row = new StringBuilder();
                        ResultSetMetaData rsmd = resultSet.getMetaData();
                        int columnCount = rsmd.getColumnCount();
                        for (int i = 1; i <= columnCount; i++)
                        {
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

    public void displayQueryResults(List<String> results)
    {
        resultsContainer.getChildren().clear();
        for (String row : results)
        {
            Label rowLabel = new Label(row);
            rowLabel.setStyle("-fx-padding: 5;");
            resultsContainer.getChildren().add(rowLabel);
        }
    }

    @FXML
    protected void onAddRowButtonClick()
    {
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
            if (isNowFocused)
            {
                activeTextField = newTextField;
            }
        });
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    protected void onExportButtonClick()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("SearchResults.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null)
        {
            try (PrintWriter writer = new PrintWriter(file))
            {
                for (Node node : resultsContainer.getChildren())
                {
                    if (node instanceof Label)
                    {
                        String text = ((Label) node).getText();
                        String csvLine = text.replace(", ", ",");
                        writer.println(csvLine);
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void goToUserDatabaseSetupPage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("update-database-info.fxml"));
        AnchorPane updateUserDBPage = fxmlLoader.load();
        SQLConverterController controller = fxmlLoader.getController();

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(updateUserDBPage);
        updateUserDBPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Update User Database Information");
    }

    public void onUpdateDatabaseInfoButton() throws IOException {
        String serverName = serverNameField.getText();
        String databaseName = databaseNameField.getText();
        String dbUsername = dbUsernameField.getText();
        String dbPassword = dbPasswordField.getText();

        Connection UserConnection = null;
        Connection connection = null;
        DatabaseManager databaseManager = new DatabaseManager();

        if (serverName.isEmpty() || databaseName.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty())
        {
            connectionError.setText("Fields cannot be blank");
            connectionError.setLayoutX(450);
            connectionError.setVisible(true);
            return;
        }
        try
        {
            if (localDBCheckbox.isSelected())
            {
                UserConnection = databaseManager.ConnectUserDatabase(serverName, databaseName, dbUsername, dbPassword, true);
                connection = databaseManager.DatabaseConnection();
                databaseManager.saveIsLocalValue(connection, this.email, 1);
            }
            else
            {
                UserConnection = databaseManager.ConnectUserDatabase(serverName, databaseName, dbUsername, dbPassword, false);
                connection = databaseManager.DatabaseConnection();
                databaseManager.saveIsLocalValue(connection, this.email, 1);
            }

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

            }
            else
            {
                connectionError.setVisible(true);
            }
        } catch (IOException e) {
            connectionError.setVisible(true);
        }
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

    public void onUpdateDBButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sql-converter.fxml"));
        AnchorPane sqlConverterPage = fxmlLoader.load();
        SQLConverterController controller = fxmlLoader.getController();

        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(sqlConverterPage);
        sqlConverterPage.requestFocus();

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("SQL Converter");
    }
}
