package com.chat.app;

import com.chat.app.model.Member;
import com.chat.app.model.Message;
import com.chat.app.model.MessageType;
import com.chat.app.server.ClientConnection;
import com.chat.app.server.GroupMessagingServer;
import com.chat.app.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.net.Socket;
import static org.junit.Assert.assertEquals;

public class GroupMessagingServerTest {

    //    private static final int PORT = 9000;
//    private MembershipManager membershipManager;
    private GroupMessagingServer server;
    private Member member1;
    private Member member2;
    private Member member3;
    private ClientConnection connection1;
    private ClientConnection connection2;
    private ClientConnection connection3;

    @Before
    public void setUp() throws InterruptedException, IOException {
        server = GroupMessagingServer.getInstance();

        // connect with server, member 1
        member1 = new Member("id1", Util.IP_ADDRESS.getHostAddress(),
                Util.DEFAULT_PORT, 1234);
        Socket socket1 = new Socket(Util.IP_ADDRESS, Util.DEFAULT_PORT);
        connection1 = new ClientConnection(socket1);
        connection1.sendObject(member1);

        // connect with server, member 2
        member2 = new Member("id2", Util.IP_ADDRESS.getHostAddress(),
                Util.DEFAULT_PORT, 1234);
        Socket socket2 = new Socket(Util.IP_ADDRESS, Util.DEFAULT_PORT);
        connection2 = new ClientConnection(socket2);
        connection2.sendObject(member2);

        // connect with server, member 2
        member3 = new Member("id3", Util.IP_ADDRESS.getHostAddress(),
                Util.DEFAULT_PORT, 1234);
        Socket socket3 = new Socket(Util.IP_ADDRESS, Util.DEFAULT_PORT);
        connection3 = new ClientConnection(socket3);
        connection3.sendObject(member3);
    }

    @After
    public void tearDown() {
        server.close();
    }

    @Test
    public void broadcastMessagingTest() throws IOException, ClassNotFoundException {

        // send to server
        Message message = new Message(null, null, MessageType.BROADCAST, "Hello");
        connection1.sendMessage(message);

        // receive message
        Message message1 = connection1.readMessage();
        Message message2 = connection2.readMessage();
        Message message3 = connection3.readMessage();

        // test
        assertEquals(message1.getMessage(), "Hello");
        assertEquals(message1.getMessageType(), MessageType.BROADCAST);

        assertEquals(message2.getMessage(), "Hello");
        assertEquals(message2.getMessageType(), MessageType.BROADCAST);

        assertEquals(message3.getMessage(), "Hello");
        assertEquals(message3.getMessageType(), MessageType.BROADCAST);
    }

    @Test
    public void notificationTest() throws IOException, ClassNotFoundException {

        // send to server
        Message message = new Message(null, null, MessageType.NOTIFICATION, "NOTIFICATION");
        connection1.sendMessage(message);

        // receive message
        Message message1 = connection1.readMessage();
        Message message2 = connection2.readMessage();
        Message message3 = connection3.readMessage();

        // test
        assertEquals(message1.getMessage(), "NOTIFICATION");
        assertEquals(message1.getMessageType(), MessageType.NOTIFICATION);

        assertEquals(message2.getMessage(), "NOTIFICATION");
        assertEquals(message2.getMessageType(), MessageType.NOTIFICATION);

        assertEquals(message3.getMessage(), "NOTIFICATION");
        assertEquals(message3.getMessageType(), MessageType.NOTIFICATION);
    }

    @Test
    public void personalMessageTest() throws IOException, ClassNotFoundException {

        // send to server
        Message message = new Message(member1, member2, MessageType.PRIVATE, "NOTIFICATION");
        connection1.sendMessage(message);

        // receive message
        Message message1 = connection2.readMessage();

        // test
        assertEquals(message1.getMessage(), "NOTIFICATION");
        assertEquals(message1.getMessageType(), MessageType.PRIVATE);

    }
}
