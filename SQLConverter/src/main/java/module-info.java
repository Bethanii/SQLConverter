module com.example.myjavafxapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.mail;
    requires javax.mail.api;
    requires com.google.api.client;
    requires com.google.api.client.extensions.jetty.auth;
    requires google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;

    opens com.example.myjavafxapp to javafx.fxml;
    exports com.example.myjavafxapp;
}