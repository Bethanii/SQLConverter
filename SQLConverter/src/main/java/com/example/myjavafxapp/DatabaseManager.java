package com.example.myjavafxapp;

import javafx.scene.control.*;
import java.io.IOException;
import javafx.fxml.FXML;
import java.util.List;
import java.sql.*;

public class DatabaseManager {
    @FXML private Label connectionError;
    @FXML private Label welcomeText;
    private Connection userConnection, sqlAppConnection;
    private DatabaseManager databaseManager;
    private String email;

    public Connection connectUserDatabase(String serverName, String databaseName, String username, String password, boolean isLocal) throws IOException {
        String connectionUrl;
        if (isLocal) {
            connectionUrl = "jdbc:sqlserver://" + serverName + ":1433;" +
                    "database=" + databaseName + ";" +
                    "user=" + username + ";" +
                    "password=" + password + ";" +
                     "encrypt=true;" +
                    "trustServerCertificate=true;" +
                    "loginTimeout=30;";
        } else {
            connectionUrl = "jdbc:sqlserver://" + serverName + ":1433;" +
                    "database=" + databaseName + ";" +
                    "user=" + username + ";" +
                    "password=" + password + ";" +
                    "encrypt=true;" +
                    "trustServerCertificate=false;" +
                    "hostNameInCertificate=*.database.windows.net;" +
                    "loginTimeout=30;";
        }

        try {
            Connection connection = DriverManager.getConnection(connectionUrl);
            return connection;
        } catch (Exception e) {
            return null;
        }
    }

    public Connection databaseConnection() throws IOException
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
            return null;
        }
    }

    public void saveUserDetails(Connection connection, String email, String password) {
        String sql = "INSERT INTO Users (Email, Password) VALUES (?, ?);";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveIsLocalValue(Connection connection, String email, int isLocal) {
        String sql = "UPDATE Users SET isLocal = ? WHERE Email = ?;";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, isLocal);
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkIfColumnValueExists(Connection connection, String columnName, String columnValue) {
        String sql = "SELECT * FROM Users WHERE " + columnName + " = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, columnValue);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkForLocalDB(Connection connection, String email) {
        String sql = "SELECT isLocal FROM Users WHERE Email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int isLocal = resultSet.getInt("isLocal");
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkForTempPassword(String emailInput) {
        String sql = "SELECT tempPassword FROM Users WHERE Email = ?";
        boolean tempPW = false;

        try (Connection connection = databaseConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, emailInput);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String tempPassword = resultSet.getString("tempPassword");
                    tempPW = "1".equals(tempPassword);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempPW;
    }

    public void setTempPassword(String email, String tempPassword) {
        String sql = "UPDATE users SET Password = ? WHERE Email = ?";

        try (Connection conn = databaseConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tempPassword);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetTempPassword(String emailInput) {
        String sql = "UPDATE Users SET tempPassword = '0' WHERE Email = ?";

        try (Connection connection = databaseConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, emailInput);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserPassword(Connection connection, String emailInput) {
        String sql = "SELECT Password FROM Users WHERE Email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, emailInput);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("Password");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getUserDBInfo(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = databaseConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String serverName = rs.getString("serverName");
                    String databaseName = rs.getString("databaseName");
                    String username = rs.getString("dbUsername");
                    String password = rs.getString("dbPassword");

                    String[] dbInfo = {serverName, databaseName, username, password};
                    return dbInfo;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveSecurityQuestions(String firstAnswer, String secondAnswer, String firstQuestion, String secondQuestion, String email) {
        try (Connection conn = databaseConnection()) {
            String sql = "UPDATE Users SET securityAnswer1 = ?, securityAnswer2 = ?, securityQuestion1 = ?, securityQuestion2 = ? WHERE Email = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, firstAnswer);
                pstmt.setString(2, secondAnswer);
                pstmt.setString(3, firstQuestion);
                pstmt.setString(4, secondQuestion);
                pstmt.setString(5, email);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getSecurityQuestions(String email) {
        String[] securityQuestions = new String[2];
        String sql = "SELECT securityQuestion1, securityQuestion2 FROM users WHERE email = ?";

        try (Connection conn = databaseConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    securityQuestions[0] = rs.getString("securityQuestion1");
                    securityQuestions[1] = rs.getString("securityQuestion2");
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return securityQuestions;
    }

    public boolean validateSecurityAnswers(String email, String response1, String response2) {
        String sql = "SELECT securityAnswer1, securityAnswer2 FROM Users WHERE Email = ?";

        try (Connection conn = databaseConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedResponse1 = rs.getString("securityAnswer1");
                    String storedResponse2 = rs.getString("securityAnswer2");

                    if (response1.equals(storedResponse1) && response2.equals(storedResponse2)) {
                        return true;
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateNewPassword(String email, String newPassword) throws SQLException, IOException {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = databaseConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void saveEnterpriseSubUserEmails(List<String> emails) {
        String sql = "INSERT INTO Users (email, Password, tempPassword) VALUES (?, ?, ?)";

        try (Connection connection = databaseConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String defaultPassword = " ";
            int tempPasswordValue = 1;

            for (String email : emails) {
                statement.setString(1, email);
                statement.setString(2, defaultPassword);
                statement.setInt(3, tempPasswordValue);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}