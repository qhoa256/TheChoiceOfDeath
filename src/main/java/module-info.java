module ChooseButton {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;

    // Mở các package để JavaFX và các module khác có thể truy cập
    opens client.GUI to javafx.fxml;

    // Export các package cần thiết
    exports client;
    exports client.GUI;
    exports common;
    exports server;
}