package com.example.myjavafxapp;

import javafx.scene.control.*;
import java.io.IOException;
import javafx.fxml.FXML;
import java.util.List;
import java.sql.*;

public class DatabaseManager {
    @FXML private Label connectionError;
    @FXML private Label welcomeText;
    private Connection userConnection;
    private DatabaseManager databaseManager;
    private String email;

    /**
     * Connects to the user's database based on the given connection information.
     * @param serverName The user's server name.
     * @param databaseName The user's database name.
     * @param username The user's database login name.
     * @param password The user's database password.
     * @param isLocal A flag indicating if the user is using a local database.
     * @return A Connection object or null in the case of a failure.
     * @throws IOException If an error occurs during set-up.
     */
    public Connection connectUserDatabase(String serverName, String databaseName, String username,
                                          String password, boolean isLocal) throws IOException {
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

    /**
     * Establishes the connection SQL Converter owner database.
     * @return A Connection object if the connection is successful, null if not.
     * @throws SQLException If a database connection error occurs.
     */
    public Connection databaseConnection() throws SQLException {
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

        Connection connection = DriverManager.getConnection(connectionUrl);
        System.out.println("Connected to Azure SQL Database successfully.");
        return connection;
    }

    /**
     * Saves user details into the database.
     * @param connection A Connection to the database.
     * @param email User's email.
     * @param password User's password.
     */
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

    /**
     * Updates the 'isLocal' flag for a given user in the database.
     * @param connection A Connection to the database.
     * @param email User's email.
     * @param isLocal The value to set for the 'isLocal' flag.
     */
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

    /**
     * Checks if the selected value exists within a given column in the database.
     * @param connection A Connection to the database.
     * @param columnName The column name to validate.
     * @param columnValue The value to look for in the selected column.
     * @return true if the value exists, false if not.
     */
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

    /**
     * Checks if the value of the isLocal flag for a given user.
     * @param connection A Connection to the database.
     * @param email User's email.
     * @return true if the user is set as local, false if not.
     */
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

    /**
     * Checks if the user has a temporary password set.
     * @param emailInput The email of the user to validate.
     * @return true if the user has a temporary password, false if not.
     */
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tempPW;
    }

    /**
     * Sets a temporary password for a selected user.
     * @param email The email of the user to be updated.
     * @param tempPassword The temporary password to set.
     */
    public void setTempPassword(String email, String tempPassword) {
        String sql = "UPDATE users SET Password = ? WHERE Email = ?";
        try (Connection conn = databaseConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tempPassword);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the temporary password flag for the selected user.
     * @param emailInput The email of the user to be updated.
     */
    public void resetTempPassword(String emailInput) {
        String sql = "UPDATE Users SET tempPassword = '0' WHERE Email = ?";
        try (Connection connection = databaseConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, emailInput);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the stored password for the selected user.
     * @param connection A Connection to the database.
     * @param emailInput User's email.
     * @return The user's password, or null if no password is found.
     */
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

    /**
     * Get the database connection details for the selected user.
     * @param email User's email.
     * @return An array containing the user's database connection details or null if not found.
     */
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates security questions and answers for the user in context.
     * @param firstAnswer The answer to the first security question.
     * @param secondAnswer The answer to the second security question.
     * @param firstQuestion The first security question.
     * @param secondQuestion The second security question.
     * @param email The email of the user to which the questions and answers belong.
     */
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
        }
    }

    /**
     * Retrieves the security questions set for the selected user.
     * @param email The user's email.
     * @return An array of strings containing the security questions.
     */
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return securityQuestions;
    }

    /**
     * Validates the input security answers for the user in context
     * compared with those stored in the database.
     * @param email The user's email.
     * @param response1 User-provided answer to the first security question.
     * @param response2 User-provided answer to the second security question.
     * @return true if the input answers match the stored answers, false if not.
     */
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the password for the selected user.
     * @param email The user's email
     * @param newPassword The new password to be set.
     * @return true if the update was successful, false if not.
     * @throws SQLException If a database access error occurs.
     * @throws IOException If an IO error occurs.
     */
    public boolean updateNewPassword(String email, String newPassword) throws SQLException, IOException {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = databaseConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void saveEnterpriseSubUserEmails(List<String> emails, String mainEmail) {
        String sql = "INSERT INTO Users (email, Password, tempPassword, accountOwner, isSubUser) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String defaultPassword = " ";
            int isSubUser = 1;
            int tempPasswordValue = 1;
            for (String email : emails) {
                statement.setString(1, email);
                statement.setString(2, defaultPassword);
                statement.setInt(3, tempPasswordValue);
                statement.setString(4, mainEmail);
                statement.setInt(5, isSubUser);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method saves sub-user emails.
     * @param emails List of sub-user emails to save.
     * @param mainEmail Email of the main user.
     */
    public void saveEnterpriseSubUserDBInfo(List<String> emails, String serverName, String databaseName,
                                            String dbUsername, String dbPassword, String mainEmail) {
        String sql = "UPDATE Users SET serverName = ?, databaseName = ?, dbUsername = ?, dbPassword = ?, accountOwner = ? WHERE email = ?";
        try (Connection connection = databaseConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String email : emails) {
                statement.setString(1, serverName);
                statement.setString(2, databaseName);
                statement.setString(3, dbUsername);
                statement.setString(4, dbPassword);
                statement.setString(5, mainEmail);
                statement.setString(6, email);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkIfSubUser(Connection connection, String emailInput) {
        String sql = "SELECT isSubUser FROM Users WHERE Email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, emailInput);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("isSubUser");
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUsername(Connection connection, String email, String emailInput) {
        String sql = "UPDATE Users SET Email = ? WHERE Email = ?;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, emailInput);
            preparedStatement.setString(2, email);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkIfAccountOwner(Connection connection, String emailInput) {
        String sql = "SELECT isAccountOwner FROM Users WHERE Email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, emailInput);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("isAccountOwner");
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateIsAccountOwnerFlag(Connection connection, String email) {
        String sql = "UPDATE Users SET isAccountOwner = ? WHERE Email = ?;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, '1');
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void saveUserDBInfo(Connection connection, String serverName, String databaseName,
                               String dbUsername, String dbPassword, String email) {
        String insertSQL = "UPDATE Users SET serverName = ?, databaseName = ?, dbUsername = ?, dbPassword = ? WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, serverName);
            pstmt.setString(2, databaseName);
            pstmt.setString(3, dbUsername);
            pstmt.setString(4, dbPassword);
            pstmt.setString(5, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSubUserDBInfo(Connection connection, String serverName, String databaseName,
                                    String dbUsername, String dbPassword, String email) {
        String insertSQL = "UPDATE Users SET serverName = ?, databaseName = ?, dbUsername = ?, dbPassword = ? WHERE accountOwner = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, serverName);
            pstmt.setString(2, databaseName);
            pstmt.setString(3, dbUsername);
            pstmt.setString(4, dbPassword);
            pstmt.setString(5, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSubUserDetails(String email) {
        String sql = "DELETE FROM Users WHERE accountOwner = ?";
        try (Connection connection = databaseConnection();
             PreparedStatement deleteStmt = connection.prepareStatement(sql)) {
            deleteStmt.setString(1, email);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUserDetails(Connection connection, String email) {
        String sql = "DELETE FROM Users WHERE Email = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(sql)) {
            deleteStmt.setString(1, email);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}