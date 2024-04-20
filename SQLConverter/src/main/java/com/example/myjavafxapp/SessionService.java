package com.example.myjavafxapp;

import java.sql.Connection;

public class SessionService {
    private static SessionService instance;
    private String email;
    private Connection connection;

    private SessionService() {}

    public static synchronized SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
