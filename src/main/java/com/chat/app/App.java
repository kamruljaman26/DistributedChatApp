package com.chat.app;


import com.chat.app.server.GroupMessingServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        /*
         * Create 3 new member register window for demonstration
         */
        for (int i = 0; i < 3; i++) {
            URL resource = App.class.getResource("create_member.fxml");
            Parent root = FXMLLoader.load(resource);
            Scene scene = new Scene(root);

            stage = new Stage();
            stage.setTitle("Create New Chat Member");
            stage.setResizable(false);
            stage.setScene(scene);

            stage.setOnCloseRequest(event1 -> {
                System.out.println("IN CREATE MEMBER CLOSE");
            });

            stage.show();
        }

        // run server
        GroupMessingServer.getInstance();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
