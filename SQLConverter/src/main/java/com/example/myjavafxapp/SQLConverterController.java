package com.example.myjavafxapp;

import javafx.fxml.FXML;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.util.Collections;
import java.util.List;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class SQLConverterController
{
    @FXML
    private ChoiceBox searchRow1;
    @FXML
    private TextField searchField1;
    @FXML
    private VBox dynamicRowsContainer;
    @FXML
    private VBox resultsContainer;

    private ChoiceBox<String> activeChoiceBox;

    @FXML
    public void initialize() {
        searchRow1.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                activeChoiceBox = searchRow1;
            }
        });
    }
    @FXML
    protected void onSearchButtonClick() throws IOException
    {
        if (activeChoiceBox == null || activeChoiceBox.getValue() == null) {
            System.out.println("Please select a table first.");
            return;
        }

        String searchKeyword = searchField1.getText();
        String dropdownSelection = activeChoiceBox.getValue();

        querySelectedTables(dropdownSelection, Collections.singletonList(searchKeyword));
    }

    public List<String> getTableNames(Connection connection)
    {
        List<String> tableNames = new ArrayList<>();
        try
        {
            DatabaseMetaData metaData = connection.getMetaData();
            String[] types = {"TABLE"};
            ResultSet tables = metaData.getTables(null, "dbo", "%", types);
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
        try
        {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(null, null, tableName, null))
            {
                while (columns.next())
                {
                    columnNames.add(columns.getString("COLUMN_NAME"));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return columnNames;
    }

    private void querySelectedTables(String tableName, List<String> searchTexts) {
        String serverName = "sqlservertest-db.database.windows.net";
        String databaseName = "SQLServer_TestDB";
        String dbUsername = "TestUser";
        String dbPassword = "SQLservertest1!";

        DatabaseManager databaseManager = new DatabaseManager();
        List<String> results = new ArrayList<>();

        try (Connection connection = databaseManager.ConnectUserDatabase(serverName, databaseName, dbUsername, dbPassword)) {
            List<String> columnNames = getColumnNames(connection, tableName);

            for (String columnName : columnNames) {
                String query = "SELECT * FROM " + tableName + " WHERE " + columnName + " LIKE ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (String searchText : searchTexts) {
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
            }
            displayQueryResults(results);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
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

        newChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                activeChoiceBox = newChoiceBox;
            }
        });

        DatabaseManager databaseManager = new DatabaseManager();
        try {
            Connection connection = databaseManager.ConnectUserDatabase("sqlservertest-db.database.windows.net", "SQLServer_TestDB", "TestUser", "SQLservertest1!");
            newChoiceBox.getItems().addAll(getTableNames(connection));
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextField newTextField = new TextField();
        newTextField.setPrefWidth(250.0);
        newTextField.setPrefHeight(33.0);
        newTextField.setPromptText("Enter value");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(event -> {
            String selectedTable = newChoiceBox.getValue();
            String searchText = newTextField.getText();
            querySelectedTables(selectedTable, Collections.singletonList(searchText));
        });

        HBox newRow = new HBox(10);
        newRow.getChildren().addAll(newChoiceBox, newTextField, searchButton);

        dynamicRowsContainer.getChildren().add(newRow);
    }
}