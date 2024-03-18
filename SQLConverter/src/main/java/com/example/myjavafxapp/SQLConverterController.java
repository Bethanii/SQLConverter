package com.example.myjavafxapp;

import javafx.fxml.FXML;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.util.List;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

public class SQLConverterController {
    @FXML
    private ChoiceBox<String> searchRow1;
    @FXML
    private TextField searchField1;
    @FXML
    private VBox dynamicRowsContainer;
    @FXML
    private VBox resultsContainer;
    private ChoiceBox<String> activeChoiceBox;
    private TextField activeTextField;

    @FXML
    public void initialize()
    {
        populateStaticRow();
    }

    private void populateStaticRow()
    {
        DatabaseManager databaseManager = new DatabaseManager();
        try
        {
            Connection connection = databaseManager.ConnectUserDatabase("sqlservertest-db.database.windows.net", "SQLServer_TestDB", "TestUser", "SQLservertest1!");
            searchRow1.getItems().addAll(getTableNames(connection));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSearchButtonClick() throws IOException
    {
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

    private List<String> querySelectedTables(String tableName, String searchText)
    {
        String serverName = "sqlservertest-db.database.windows.net";
        String databaseName = "SQLServer_TestDB";
        String dbUsername = "TestUser";
        String dbPassword = "SQLservertest1!";
        DatabaseManager databaseManager = new DatabaseManager();
        List<String> results = new ArrayList<>();

        try (Connection connection = databaseManager.ConnectUserDatabase(serverName, databaseName, dbUsername, dbPassword))
        {
            List<String> columnNames = getColumnNames(connection, tableName);

            for (String columnName : columnNames)
            {
                String query = "SELECT * FROM " + tableName + " WHERE " + columnName + " LIKE ?";
                try (PreparedStatement statement = connection.prepareStatement(query))
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
        }
        catch (SQLException | IOException e)
        {
            e.printStackTrace();
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

        DatabaseManager databaseManager = new DatabaseManager();
        try
        {
            Connection connection = databaseManager.ConnectUserDatabase("sqlservertest-db.database.windows.net", "SQLServer_TestDB", "TestUser", "SQLservertest1!");
            newChoiceBox.getItems().addAll(getTableNames(connection));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
