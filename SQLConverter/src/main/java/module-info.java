module com.example.myjavafxapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires javax.mail.api;
    requires com.google.api.client;
    requires com.google.api.client.extensions.jetty.auth;
    requires google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.services.gmail;
    requires org.apache.commons.codec;
    requires com.google.api.client.json.gson;


    opens com.example.myjavafxapp to javafx.fxml;
    exports com.example.myjavafxapp;
}