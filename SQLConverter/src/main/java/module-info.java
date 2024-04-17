module com.example.myjavafxapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.httpserver;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
 //   requires mssql.jdbc;
    //requires com.microsoft.sqlserver.jdbc;

    opens com.example.myjavafxapp to javafx.fxml;
    exports com.example.myjavafxapp;
}