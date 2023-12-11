package com.example.myjavafxapp;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String JDBC_URL = "jdbc:mysql://GWTC71427:3306/SQLConverter?useSSL=false";
    private static final String USERNAME = "owner2";
    private static final String PASSWORD = "passwordInfo";

    //username = ownerAccount
    //user = owner2
    //password = passwordInfo
    public static void getConnection() throws SQLException {
        DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    }
}