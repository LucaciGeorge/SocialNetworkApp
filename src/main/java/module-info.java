module org.example.socialnetworkapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.socialnetworkapp to javafx.fxml;
    exports org.example.socialnetworkapp;
    exports controller;
    opens controller to javafx.fxml;

}