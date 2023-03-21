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

    // clients and membership manager
    private volatile static Map<String, Thread> clients;
    private volatile static Map<String, ObjectOutputStream> outputStreams;
    private volatile static MembershipManager manager;

    // static block
    static {
        try {
            IP_ADDRESS = InetAddress.getLocalHost();
            manager = MembershipManager.getInstance();
            outputStreams = new HashMap<>();
            clients = new HashMap<>();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

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

    // remove object output stream of inactive member
    public static synchronized void removeOutputStream(String id) {
        outputStreams.remove(id);
    }

    // remove and notify a member
    public static synchronized void removeAndNotify(Member member) {
        Member coordinator = manager.getCoordinator();
        Message message = new Message(coordinator, member, MessageType.REMOVE, member.getId() + " has been removed by " +
                "coordinator("+coordinator.getId()+")!");

        try {
            // notify everyone about remove operation
            for (ObjectOutputStream out : outputStreams.values()) {
                out.writeObject(message);
                out.flush();
            }

            // removed from everywhere & stop client handler thread
            manager.removeMember(member);
            outputStreams.remove(member.getId());
            Thread clientHandler = clients.get(member.getId());
            clientHandler.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // send notification to all active members
    public static synchronized void sendNotificationToEveryone(String messageTxt) {
        // send to all
        Message message = new Message(null, null, MessageType.NOTIFICATION, messageTxt);

//        System.out.println("Server: Sending notification to everyone.");
        try {
            for (ObjectOutputStream out : outputStreams.values()) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // create the server for our chat application
    private void createServer() throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT, 50, IP_ADDRESS);
        Socket socket;

        // server details
        System.out.println("Server started on port: " + DEFAULT_PORT);
        System.out.println("Server started on IP: " + IP_ADDRESS);

        while (true) {
            // accept clients
            socket = serverSocket.accept();
            System.out.println("");
            System.out.println("\n ---- Socket Accepted ---- ");

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
                Thread thread = new Thread(clientHandler);
                clients.put(member.getId(), thread);
                outputStreams.put(member.getId(), objectOutputStream);

                System.out.println("New Member Connected In Server: " + member);

                thread.start();
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
                            ObjectOutputStream out = outputStreams.get(receivedMessage.getReceiver().getId());
                            out.writeObject(receivedMessage);
                            out.flush();
                        }

                        // if BROADCAST or NOTIFICATION message send to everyone, except me
                        else if (receivedMessage.getMessageType().equals(MessageType.BROADCAST) ||
                                receivedMessage.getMessageType().equals(MessageType.NOTIFICATION)) {

                            // send to all
                            for (ObjectOutputStream out : outputStreams.values()) {
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
