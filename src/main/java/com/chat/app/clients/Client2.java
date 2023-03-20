package com.chat.app.clients;

import com.chat.app.model.Member;
import com.chat.app.model.Message;
import com.chat.app.model.MessageType;
import com.chat.app.server.GroupMessingServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

// Note that this is just a basic example and doesn't include all of the requirements
// listed in the original question. You would need to modify the code to fit those requirements.
public class Client2 {
    public static void main(String[] args) {
        try {
            InetAddress IP_ADDRESS = GroupMessingServer.IP_ADDRESS;

            Socket socket = new Socket(IP_ADDRESS, 9000);
            System.out.println(socket);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            Member member = new Member("kkk", "localhost", 9000, 8000);
            out.writeObject(member);
            out.flush();

            System.out.println("sc");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter message to send: ");
                String message = scanner.nextLine();

                Message message1 = new Message(member, member, MessageType.BROADCAST, message);
                out.writeObject(message1);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

