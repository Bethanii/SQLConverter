package com.example.myjavafxapp;

import javafx.fxml.FXML;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import javafx.scene.control.TextField;
import java.util.List;
import javafx.scene.control.ChoiceBox;


public class SQLConverterController
{
    @FXML
    private ChoiceBox searchRow1;
    @FXML
    private TextField searchField1;

    @FXML
    protected void onConnectButtonClick() throws IOException
    {
        ConnectToDB();
    }

    @FXML
    protected void onSearchButtonClick() throws IOException
    {
        String searchKeyword = searchField1.getText();
        String dropdownSelection = (String) searchRow1.getValue();

        querySelectedTables(dropdownSelection, searchKeyword);
    }
    protected void ConnectToDB() throws IOException
    {
        String serverName = "sqlservertest-db.database.windows.net";
        String databaseName = "SQLServer_TestDB";
        String dbUsername = "TestUser";
        String dbPassword = "SQLservertest1!";

        DatabaseManager databaseManager = new DatabaseManager();
        Connection connection = databaseManager.ConnectUserDatabase(serverName, databaseName, dbUsername, dbPassword);

        List<String> tableNames = getTableNames(connection);
        searchRow1.getItems().addAll(tableNames);
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

    private void querySelectedTables(String tableName, String searchText) throws IOException
    {
        String serverName = "sqlservertest-db.database.windows.net";
        String databaseName = "SQLServer_TestDB";
        String dbUsername = "TestUser";
        String dbPassword = "SQLservertest1!";

        DatabaseManager databaseManager = new DatabaseManager();

        try (Connection connection = databaseManager.ConnectUserDatabase(serverName, databaseName, dbUsername, dbPassword))
        {
            List<String> columnNames = getColumnNames(connection, tableName);

            StringBuilder whereClause = new StringBuilder();
            for (String columnName : columnNames) {
                if (whereClause.length() > 0) {
                    whereClause.append(" OR ");
                }
                whereClause.append(columnName).append(" LIKE ?");
            }

            String query = "SELECT * FROM " + tableName + (whereClause.length() > 0 ? " WHERE " + whereClause.toString() : "");

            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                for (int i = 1; i <= columnNames.size(); i++)
                {
                    statement.setString(i, "%" + searchText + "%");
                }
                try (ResultSet resultSet = statement.executeQuery())
                {
                    while (resultSet.next())
                    {
                        for (int i = 1; i <= columnNames.size(); i++)
                        {
                            if (i > 1) System.out.print(",  ");
                            String columnValue = resultSet.getString(i);
                            System.out.print(columnNames.get(i - 1) + ": " + columnValue);
                        }
                        System.out.println();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
