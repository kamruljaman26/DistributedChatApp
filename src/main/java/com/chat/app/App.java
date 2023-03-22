package com.chat.app;


import com.chat.app.server.GroupMessagingServer;
import javafx.application.Application;
import javafx.application.Platform;
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
        for (int i = 0; i < 4; i++) {

            URL resource = App.class.getResource("create_member.fxml");
            Parent root = FXMLLoader.load(resource);
            Scene scene = new Scene(root);

            Stage customStage = new Stage();
            customStage.setTitle("Create New Chat Member");
            customStage.setScene(scene);

            customStage.show();
        }
    }

    public static void main(String[] args) {

        GroupMessagingServer.getInstance();
        launch(args);

    }
}
