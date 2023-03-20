module com.chat.app {

    // javafx and base
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires java.prefs;

    // sqlite and hibernate, google
    requires java.sql;
    requires junit;
    requires java.base;

    exports com.chat.app;
    exports com.chat.app.server;
    exports com.chat.app.controller;
    exports com.chat.app.clients;
    exports com.chat.app.model;
    exports com.chat.app.util;

    opens com.chat.app;

}
