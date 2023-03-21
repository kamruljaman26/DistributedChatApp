package com.chat.app.server;

import com.chat.app.model.Member;
import com.chat.app.model.Message;
import com.chat.app.model.MessageType;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupMessingServer implements Runnable {

    public static final int DEFAULT_PORT = 9000;
    public static InetAddress IP_ADDRESS;
    private static final int MAX_CLIENTS = 10;
    private static final int TIMEOUT_MS = 10000;

    // clients and membership manager
//    private volatile static Map<String, ClientHandler> clients;
    private volatile static Map<String, ObjectOutputStream> outputStreams;
    private volatile static MembershipManager manager;
    private volatile static GroupMessingServer server;

    // static block
    static {
        try {
            IP_ADDRESS = InetAddress.getLocalHost();
//            clients = new HashMap<>();
            manager = MembershipManager.getInstance();
            outputStreams = new HashMap<>();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    // default constructor
    public GroupMessingServer() {
    }

    // return singleton server
    public static synchronized GroupMessingServer getInstance() {
        if (server == null) {
            server = new GroupMessingServer();
            return server;
        } else
            return server;
    }

    // return membership manager
    public synchronized MembershipManager getManager() {
        return manager;
    }

/*    // get client handeler
    public synchronized ClientHandler getClientHandler(String id) {
        return clients.get(id);
    }*/

    @Override
    public void run() {
        // create server in separate thread
        Thread thread = new Thread(() -> {
            try {
                createServer();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error while init group messaging server");
                e.printStackTrace();
            }
        });
        thread.start();
    }

    // create the server for our chat application
    private void createServer() throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT, 50, IP_ADDRESS);
        Socket socket;

        // server details
        System.out.println("Server started on port: " + DEFAULT_PORT);
        System.out.println("Server started on ip: " + IP_ADDRESS);

        while (true) {
            // accept clients
            socket = serverSocket.accept();
            System.out.println("");
            System.out.println(" ---- Socket Accept ---- ");

            // read member and in membership list
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            // read member
            Member member = (Member) objectInputStream.readObject();
            if (member != null) {
                manager.addMember(member);

                // create client handler in separate thread
                System.out.println(socket);
                System.out.println(serverSocket);

                ClientHandler clientHandler = new ClientHandler(socket, member, objectInputStream, objectOutputStream);

//                clients.put(member.getId(), clientHandler);
                outputStreams.put(member.getId(), objectOutputStream);

                System.out.println("New Member Connected In Server: " + member);

                new Thread(clientHandler).start();
            } else {
                System.out.println("IN SERVER: Member is null.");
            }
        }
    }

    /**
     * A multithreading client handler to manage communication between different clients
     */
    public static class ClientHandler implements Runnable {

        private final Socket socket;
        private final ObjectInputStream inputStream;
        private final ObjectOutputStream outputStream;
        private final Member member;

        // init input and output
        public ClientHandler(Socket socket, Member member,
                             ObjectInputStream inputStream, ObjectOutputStream outputStream) {
            this.socket = socket;
            this.member = member;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        public Socket getSocket() {
            return socket;
        }

        public ObjectOutputStream getOutputStream() {
            return outputStream;
        }

        public ObjectInputStream getInputStream() {
            return inputStream;
        }

        public Member getMember() {
            return member;
        }

        // thread run method to start, a starting point
        public void run() {
            while (true) {
                try {
                    // receive the message
                    Message receivedMessage = (Message) inputStream.readObject();

                    if (receivedMessage != null) {

                        System.out.printf("Message Server: (to: , %s from: %s, Msg: %s\n",
                                receivedMessage.getReceiver(), receivedMessage.getSender(),
                                receivedMessage.getMessage());

                        // if PRIVATE MESSAGE send to specific member
                        if (receivedMessage.getMessageType().equals(MessageType.PRIVATE)) {
                            System.out.println("PRIVATE");
                        }

                        // if BROADCAST or NOTIFICATION message send to everyone, except me
                        else if (receivedMessage.getMessageType().equals(MessageType.BROADCAST) ||
                                receivedMessage.getMessageType().equals(MessageType.NOTIFICATION)) {
                            System.out.println("BROADCAST OR NOTIFICATION");
                            // send to all
                            for (ObjectOutputStream out : outputStreams.values()) {
                                System.out.println("Object :: SEND ::" + member.getId());
                                out.writeObject(receivedMessage);
                                out.flush();
                            }
                        }

                    } else {
                        System.out.println("error message is null!");
                    }

                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Exception while sending message");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }
    }
}
