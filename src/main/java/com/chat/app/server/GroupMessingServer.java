package com.chat.app.server;

import com.chat.app.model.Member;
import com.chat.app.model.Message;
import com.chat.app.model.MessageType;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class GroupMessingServer implements Runnable {

    public static final int DEFAULT_PORT = 9000;
    public static InetAddress IP_ADDRESS;
    private static final int MAX_CLIENTS = 10;
    private static final int TIMEOUT_MS = 10000;

    // clients and membership manager
    private static Map<String, ClientHandler> clients;
    private static MembershipManager manager;
    private static final GroupMessingServer server = new GroupMessingServer();

    // default constructor
    private GroupMessingServer() {
        try {
            IP_ADDRESS = InetAddress.getLocalHost();
            clients = new HashMap<>();
            manager = MembershipManager.getInstance();
        } catch (UnknownHostException e) {
            System.out.println("Error while init group messaging server");
        }
    }

    // return singleton server
    public static GroupMessingServer getInstance() {
        return server;
    }

    // return membership manager
    public MembershipManager getManager() {
        return manager;
    }

    // run thread
    @Override
    public void run() {
        try {
            createServer();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // create the server for our chat application
    private void createServer() throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT, 50, IP_ADDRESS);
        Socket socket;

        System.out.println("Server started on port " + DEFAULT_PORT);
        while (true) {
            // accept clients
            socket = serverSocket.accept();
//            System.out.println("accept client");

            // read member and in membership list
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

//            System.out.println("objectInputStream");
            Member member = (Member) objectInputStream.readObject();
//            System.out.println("read mm" + member);

            if (member != null) {
                manager.addMember(member);

                // create client handler in separate thread
                System.out.println(socket);
                System.out.println(serverSocket);

                ClientHandler clientHandler = new ClientHandler(socket, member, objectInputStream, objectOutputStream);
                clients.put(member.getId(), clientHandler);

//                System.out.println("start thread");
                new Thread(clientHandler).start();

                System.out.println(member); // dbug
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
                    System.out.println(receivedMessage);

                    if (receivedMessage != null) {

                        // if PRIVATE MESSAGE send to specific member
                        if (receivedMessage.getMessageType().equals(MessageType.PRIVATE)) {
                            clients.get(receivedMessage.getReceiver().getId()).getOutputStream().writeObject(receivedMessage);
                        }

                        // if BROADCAST or NOTIFICATION message send to everyone, except me
                        else if (receivedMessage.getMessageType().equals(MessageType.BROADCAST) ||
                                receivedMessage.getMessageType().equals(MessageType.NOTIFICATION)) {

                            // send to all
                            final Message finalReceivedMessage = receivedMessage;
                            clients.values().forEach(clientHandler -> {
                                try {
                                    // ignore sender
                                    if (clientHandler.getMember().getId().equals(member.getId())) {
                                        clientHandler.getOutputStream().writeObject(finalReceivedMessage);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                    } else {
                        System.out.println("error message is null");
                    }

                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Exception while sending message");
                }
            }
        }
    }

    public static void main(String[] args) {
        GroupMessingServer instance = GroupMessingServer.getInstance();
        instance.run();
    }
}
