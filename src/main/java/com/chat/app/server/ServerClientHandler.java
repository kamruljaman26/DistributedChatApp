package com.chat.app.server;

import com.chat.app.model.Member;
import com.chat.app.model.Message;
import com.chat.app.model.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

/**
 * A multithreading client handler to manage communication between different clients
 */
public class ServerClientHandler implements Runnable {

    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final Member member;
    private final Map<String, ObjectOutputStream> outputStreams;


    // init input and output
    public ServerClientHandler(Socket socket, Member member,
                               ObjectInputStream inputStream,
                               ObjectOutputStream outputStream,
                               Map<String, ObjectOutputStream> outputStreams) {
        this.socket = socket;
        this.member = member;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.outputStreams = outputStreams;
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