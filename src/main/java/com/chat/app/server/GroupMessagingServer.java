package com.chat.app.server;

import com.chat.app.model.Member;
import com.chat.app.model.Message;
import com.chat.app.model.MessageType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GroupMessagingServer implements Runnable {

    // clients and membership manager
    private volatile static Map<String, Thread> clients;
    private volatile static Map<String, ObjectOutputStream> outputStreams;
    private volatile static MembershipManager manager;

    // static block
    static {
        manager = MembershipManager.getInstance();
        outputStreams = new HashMap<>();
        clients = new HashMap<>();
    }

    @Override
    public void run() {
        // start main server
        Thread thread = new Thread(new MessagingServer(clients, outputStreams));
        thread.start();
    }

    // remove and notify a member
    public static synchronized void removeAndNotify(Member member) {
        Member coordinator = manager.getCoordinator();
        Message message = new Message(coordinator, member, MessageType.REMOVE, member.getId() + " has been removed by " +
                "coordinator(" + coordinator.getId() + ")!");

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
            clients.remove(member.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // send notification to all active members
    public static synchronized void sendNotificationToEveryone(String messageTxt) {
        // send to all
        Message message = new Message(null, null, MessageType.NOTIFICATION, messageTxt);

        try {
            for (ObjectOutputStream out : outputStreams.values()) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
