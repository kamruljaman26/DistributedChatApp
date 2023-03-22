package com.chat.app.server;

import com.chat.app.model.Member;
import com.chat.app.util.Util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * Will create server socket
 */
public class MessagingServer implements Runnable {

    private static final MembershipManager manager;
    private final Map<String, Thread> clients;
    private final Map<String, ObjectOutputStream> outputStreams;

    static {
        manager = MembershipManager.getInstance();
    }

    public MessagingServer(Map<String, Thread> clients, Map<String, ObjectOutputStream> outputStreams) {
        this.clients = clients;
        this.outputStreams = outputStreams;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(Util.DEFAULT_PORT, 50, Util.IP_ADDRESS);
            Socket socket;

            // server details
            System.out.println("Server started on port: " + Util.DEFAULT_PORT);
            System.out.println("Server started on IP: " + Util.IP_ADDRESS);

            while (true) {
                // accept clients
                socket = serverSocket.accept();
                System.out.println("");
                System.out.println("\n ---- Socket Accepted ---- ");

                // read member and in membership list
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                // read member
                System.out.println("trying to read member");
                Member member = (Member) objectInputStream.readObject();
                if (member != null) {
                    manager.addMember(member);

                    // create client handler in separate thread
                    System.out.println(socket);
                    System.out.println(serverSocket);

                    // create client connection handler
                    ServerClientHandler serverClientHandler = new ServerClientHandler(socket, member,
                            objectInputStream, objectOutputStream, outputStreams);

                    Thread thread = new Thread(serverClientHandler);

                    // add in clients and streams
                    clients.put(member.getId(), thread);
                    outputStreams.put(member.getId(), objectOutputStream);

                    System.out.println("New Member Connected In Server: " + member);

                    thread.start();
                } else {
                    System.out.println("IN SERVER: Member is null.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
