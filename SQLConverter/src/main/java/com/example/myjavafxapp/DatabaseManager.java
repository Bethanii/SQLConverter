package com.example.myjavafxapp;

import java.io.IOException;
import java.sql.*;

public class DatabaseManager {

    public Connection ConnectUserDatabase(String serverName, String databaseName, String username, String password) throws IOException
    {
  //      serverName = "sqlservertest-db.database.windows.net";
    //    databaseName = "SQLServer_TestDB";
   //     username = "TestUser";
    //    password = "SQLservertest1!";

        String connectionUrl = "jdbc:sqlserver://" + serverName + ":1433;"
                + "database=" + databaseName + ";"
                + "user=" + username + ";"
                + "password=" + password + ";"
                + "encrypt=true;"
                + "trustServerCertificate=false;"
                + "loginTimeout=30;";
        try
        {
            Connection connection = DriverManager.getConnection(connectionUrl);
            System.out.println("Connected to SQL Server User Database successfully.");
            return connection;
        }
        catch (SQLException e)
        {
            System.out.println("Could not connect, error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Connection DatabaseConnection() throws IOException
    {
        String serverName = "sqlconverterserver.database.windows.net";
        String databaseName = "SQLConverterDB";
        String username = "Bethany";
        String password = "SQLConverter1!";

        String connectionUrl = "jdbc:sqlserver://" + serverName + ":1433;"
                + "database=" + databaseName + ";"
                + "user=" + username + ";"
                + "password=" + password + ";"
                + "encrypt=true;"
                + "trustServerCertificate=false;"
                + "hostNameInCertificate=*.database.windows.net;"
                + "loginTimeout=30;";

        try
        {
            Connection connection = DriverManager.getConnection(connectionUrl);
            System.out.println("Connected to Azure SQL Database successfully.");
            return connection;
        }
        catch (SQLException e)
        {
            System.out.println("Could not connect, error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void SaveUserDetails(Connection connection, String email, String password)
    {
        String sql = "INSERT INTO Users (Email, password) VALUES (?, ?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println("Could not insert user, error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean CheckIfColumnValueExists(Connection connection, String columnName, String columnValue)
    {
        String sql = "SELECT * FROM Users WHERE " + columnName + " = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, columnValue);

            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                return resultSet.next();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String GetUserPassword(Connection connection, String emailInput)
    {
        String sql = "SELECT Password FROM Users WHERE Email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, emailInput);

            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                {
                    return resultSet.getString("Password");
                }
                else
                {
                    return null;
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void GetUserDBInfo(String email)
    {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery())
            {
                if (rs.next())
                {
                    String serverName = rs.getString("serverName");
                    String databaseName = rs.getString("databaseName");
                    String username = rs.getString("dbUsername");
                    String password = rs.getString("dbPassword");

                    System.out.println("Server Name: " + serverName + ", Database Name: " + databaseName + ", Username: " + username + ", Password: " + password);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}