package com.example.myjavafxapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.sql.SQLException;

public class Controller {

    @FXML
    private Label welcomeText;
    private AnchorPane signInPage;

    @FXML
    protected void onSignInButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(SQLApplication.class.getResource("sign-in-page.fxml"));

        signInPage = fxmlLoader.load();
        Scene currentScene = welcomeText.getScene();
        currentScene.setRoot(signInPage);

        Stage stage = (Stage) currentScene.getWindow();
        stage.sizeToScene();
        stage.setTitle("Sign In");
    }

    @FXML
    protected void onCreateAccountButtonClick() {
        welcomeText.setText("");
    }

    @FXML
    protected void onSignInPageButtonClick() throws SQLException {
        DatabaseConnection.getConnection();
    }
}
