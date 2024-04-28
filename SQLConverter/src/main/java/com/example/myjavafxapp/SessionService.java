package com.example.myjavafxapp;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;

public class SessionService {
    private static SessionService instance;
    private String email;
    private Connection connection;
    private static Stage loadingStage;

    private SessionService() {}

    public static synchronized SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }

    /**
     * Get user email.
     * @return the user email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user email.
     * @param email the email to be set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the database connection.
     * @return database connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Sets the database connection.
     * @param connection the database connection to be set/
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
