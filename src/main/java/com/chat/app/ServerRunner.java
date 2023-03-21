package com.chat.app;


import com.chat.app.server.GroupMessingServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class ServerRunner {

    public static void main(String[] args) throws UnknownHostException {


        System.out.println(InetAddress.getLocalHost());
        System.out.println(InetAddress.getByName("192.168.0.100"));
        System.out.println(InetAddress.getLocalHost().getHostAddress());

        // run server
        new GroupMessingServer().run();
    }
}
