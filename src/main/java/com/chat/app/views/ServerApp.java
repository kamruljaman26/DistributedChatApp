package com.chat.app.views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class ServerApp extends Application {

    private static final String TITLE = "Multi-threaded Server";
    private static TextArea chatBox;
    private static ArrayList<ClientHandler> clientArr;

    // constructor
    public ServerApp() {
        // server chat box
        ServerApp.chatBox = new TextArea();
        chatBox.setPrefHeight(500);

        // init client array
        clientArr = new ArrayList<>();
    }

    // application starter
    @Override
    public void start(Stage primaryStage) throws Exception {
        // vertical box and add chat txt area
        VBox root = new VBox();
        root.setPrefSize(500, 500);
        root.getChildren().addAll(chatBox);

        // show stage/app
        primaryStage.setTitle(TITLE);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // create server far from main application thread
        Thread thread = new Thread(() -> {
            try {
                createServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        System.exit(0);
    }

    // create the server for our chat application
    private void createServer() throws IOException {

        // server is listening on port 7878
        ServerSocket serverSocket = new ServerSocket(1234);
        Socket socket;

        // start notification
        addText(TITLE + " Started at: " + LocalDateTime.now());

        // todo
        while (true) {
            socket = serverSocket.accept();

            // obtain input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // notify in GUI
            addText(String.format("Connection from %s at %s", socket.toString(), LocalDateTime.now()));

            // Create a new handler object for handling this request.
            String[] data = dis.readUTF().split("#");
            System.out.println(Arrays.toString(data));
            ClientHandler match = new ClientHandler(socket, data[0], dis, dos);

            // Create a new Thread with this object.
            Thread thread = new Thread(match);

            // add this client to active clients list
            clientArr.add(match);

            // start the thread.
            thread.start();
        }
    }

    // add text in server chat box
    private static void addText(String text) {
        String oldTxt = chatBox.getText();
        if(chatBox.getText().equals("")){
            chatBox.setText(text);
        }else {
            chatBox.setText(oldTxt + "\n\n" + text);
        }
    }

    // ClientHandler class
    public static class ClientHandler implements Runnable {

        private final String name;
        private final DataInputStream dis;
        private final DataOutputStream dos;
        private Socket socket;

        // constructor
        public ClientHandler(Socket socket, String name,
                             DataInputStream dis, DataOutputStream dos) {
            this.dis = dis;
            this.dos = dos;
            this.name = name;
            this.socket = socket;
        }

        @Override
        public void run() {

            String received;
            while (true) {
                try {
                    // receive the string
                    received = dis.readUTF();
                    System.out.println(received);
                    // break the string into message and recipient part
                    StringTokenizer st = new StringTokenizer(received, "#");
                    String senderName = st.nextToken().trim();// skip username
                    String MsgToSend = st.nextToken().trim();
                    String recipient = st.nextToken().trim();

                    System.out.println(senderName+"::senderName");
                    System.out.println(recipient+"::recipient");

                    // add message to gui
                    addText(name + ": " + MsgToSend);

                    // search for the recipient in the connected devices list.
                    // ar is the vector storing client of active users
                    for (ClientHandler mc : ServerApp.clientArr) {
                        // if the recipient is found, write on its
                        // output stream
                        if (mc.name.equals(recipient)) {
                            mc.dos.writeUTF(senderName + ": " + MsgToSend);
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

/*            try {
                // closing resources
                this.dis.close();
                this.dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }
}
