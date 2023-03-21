package com.chat.app.controller;

import com.chat.app.model.Member;
import com.chat.app.model.Message;
import com.chat.app.model.MessageType;
import com.chat.app.server.GroupMessingServer;
import com.chat.app.server.MembershipManager;
import com.chat.app.util.DTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class MemberViewController implements Initializable, DTO {

    @FXML
    public ListView<Member> membersListViewId;
    @FXML
    public volatile TextArea groupChatAreaId;
    @FXML
    public volatile TextField sendMsgTxtFldId;
    @FXML
    public Label memberdetailsLblId;
    @FXML
    public volatile Label notificationLebelID;

    // properties
    private Member mainMember;
    private static final MembershipManager manager;
    //    private volatile static GroupMessingServer server;
    private volatile ObjectOutputStream outputStream;
    private volatile ObjectInputStream inputStream;

    // static block
    static {
        manager = MembershipManager.getInstance();
//        server = GroupMessingServer.getInstance();
    }

    @Override
    public void transfer(Object... data) {

        mainMember = (Member) data[0];

//        System.out.println("Current Member: " + mainMember);

        // update member details in gui
        memberdetailsLblId.setText(String.format("Member Details: ID: %s,\t Listening Port: %d,\t" +
                        "Coordinator: %s",
                mainMember.getId(), mainMember.getListeningPort(), manager.isCoordinator(mainMember)));

        // init list view with members
        membersListViewId.setCellFactory(param -> new MemberListCell(mainMember));
        updateMemberListState();

        // inform user she/he is the coordinator
        if (manager.isCoordinator(mainMember)) {
            notificationLebelID.setText("Congratulation you are the group coordinator.");
        }

        // create client server
        Thread thread = new Thread(() -> {
            try {
                createClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    // create and init client server
    private void createClient() throws IOException {
//        System.out.println("In create client(" + mainMember.getId() + ") = " + mainMember.getId() + "\n");

        // establish the connection
        InetAddress IP_ADDRESS = InetAddress.getLocalHost();
        Socket socket = new Socket(IP_ADDRESS, 9000);
        System.out.println(socket);

        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());

//        System.out.println("Pass Member View to server: " + mainMember);
        outputStream.writeObject(mainMember);
        outputStream.flush();

//        System.out.println("OUTPUT S: " + outputStream);

        // read message
        Thread readMessage = new Thread(() -> {
            while (true) {
                System.out.println("In read msg view while: (" + mainMember.getId() + ")");
                try {
                    // read the message sent to this client
                    Message msg = (Message) inputStream.readObject();
                    System.out.println("READ MESSAGE:: " + msg);

                    // read notification
                    if (msg.getMessageType().equals(MessageType.NOTIFICATION)) {
                        addText(msg);
//                        System.out.println("Notification:: " + msg);
                    }

                    // read broadcast message
                    if (msg.getMessageType().equals(MessageType.BROADCAST)) {
                        // don't show my own message
//                        System.out.println("");
//                        System.out.println("send:" + msg.getSender().getId());
//                        System.out.println("main:" + mainMember.getId());
                        if (!msg.getSender().getId().equals(mainMember.getId()))
                            addText(msg);
                    }

                    // read broadcast message
                    if (msg.getMessageType().equals(MessageType.REMOVE)) {
                        // don't show my own message
                        System.out.println("REMOVED MESSAGE" + msg);
                        addText(msg);
                    }


                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        readMessage.start();
    }

    // add text in group chat box
    private synchronized void addText(Message message) {
        System.out.println("In add message");

        // show notification in chat area
        if (message.getMessageType().equals(MessageType.NOTIFICATION)) {
            String oldTxt = groupChatAreaId.getText();
            groupChatAreaId.setText(oldTxt + "\n" + String.format(
                    "Notification::\t%s", message.getMessage()
            ));
        }

        // broadcast message in chat area
        else if (message.getMessageType().equals(MessageType.BROADCAST)) {

            String oldTxt = groupChatAreaId.getText();
            if (message.getSender().equals(mainMember)) {
                groupChatAreaId.setText(oldTxt + "\n" + String.format(
                        "ME::\t%s", message.getMessage()
                ));
            } else {
                groupChatAreaId.setText(oldTxt + "\n" + String.format(
                        "BY (%s)::\t%s", message.getSender().getId(), message.getMessage()
                ));
            }
        }

        // broadcast message in chat area
        else if (message.getMessageType().equals(MessageType.REMOVE)) {
            String oldTxt = groupChatAreaId.getText();
            groupChatAreaId.setText(oldTxt + "\n" + String.format(
                    "REMOVED::\t%s", message.getMessage()));
        }
    }

    // update state list without current member
    private synchronized void updateMemberListState() {
        ObservableList<Member> items = FXCollections.observableArrayList(manager.getMembers());
        items.remove(mainMember);
        membersListViewId.setItems(items);
        notificationLebelID.setText("");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sendMsgTxtFldId.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    // code to execute when the "Enter" key is pressed
                    groupSendBtnAction(new ActionEvent());
                }
            }
        });

    }

    @FXML
    public void refreshStateButtonAction(ActionEvent event) {
        updateMemberListState();
    }

    @FXML
    public void groupSendBtnAction(ActionEvent event) {
        // send group message to server, in separate thread
        Thread thread = new Thread(() -> {
            try {
//                System.out.println("In send message (" + mainMember.getId() + ")");

                // write on the output stream
                Message message = new Message(mainMember, null,
                        MessageType.BROADCAST, sendMsgTxtFldId.getText());

                outputStream.writeObject(message);
                outputStream.flush();

                addText(message);
                sendMsgTxtFldId.setText("");

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    /**
     * Custom designed cell for members list
     */
    public static class MemberListCell extends ListCell<Member> {

        private final Label id = new Label();
        private final Label lPort = new Label();
        private final Label ipAddress = new Label();
        private final Label coordinator = new Label();
        private final Button remove = new Button();
        private final Button chatButton = new Button();
        private final Member mainMember;

        public MemberListCell(Member mainMember) {
            this.mainMember = mainMember;
        }

        @Override
        protected void updateItem(Member member, boolean empty) {
            super.updateItem(member, empty);

            if (empty || member == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            id.setText("ID: " + member.getId());
            lPort.setText("Port: " + member.getListeningPort());
            ipAddress.setText("IP: " + member.getServerIpAddress());
            coordinator.setText("Coordinator: " + manager.isCoordinator(member));

            remove.setText("REMOVE");
            chatButton.setText("CHAT");

            setGraphic(getHBox(member));
        }

        // build horizontal box
        private HBox getHBox(Member member) {

            // if member is coordinator add setting button, else not add setting button
            HBox hbox;
            if (manager.isCoordinator(mainMember)) {
                hbox = new HBox(id, ipAddress, lPort, remove, chatButton);
            } else {
                hbox = new HBox(id, ipAddress, lPort, coordinator, chatButton);
            }

            hbox.setSpacing(10);
            hbox.setAlignment(Pos.CENTER);

            // Handle setting button action
            remove.setOnAction(event -> {
//                System.out.println("In remove chat");
                GroupMessingServer.removeAndNotify(member);
            });

            // handle personal details button
            chatButton.setOnAction(event -> {
                System.out.println("In personal chat");
            });

            return hbox;
        }
    }
}
