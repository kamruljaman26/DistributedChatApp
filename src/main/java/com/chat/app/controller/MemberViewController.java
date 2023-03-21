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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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

    // properties
    private Member mainMember;
    private static final MembershipManager manager;
    //    private volatile static GroupMessingServer server;
    private ObjectOutputStream outputStream;
    private volatile ObjectInputStream inputStream;

    // static block
    static {
        manager = MembershipManager.getInstance();
//        server = GroupMessingServer.getInstance();
    }

    @Override
    public void transfer(Object... data) {

        mainMember = (Member) data[0];

        System.out.println("Current Member: " + mainMember);

        // update member details
        memberdetailsLblId.setText(String.format("Member Details: ID: %s,\t Listening Port: %d,\t" +
                        "Coordinator: %s",
                mainMember.getId(), mainMember.getListeningPort(), manager.isCoordinator(mainMember)));

        // init list view with members
        membersListViewId.setCellFactory(param -> new MemberListCell(mainMember));
        updateMemberListState();

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
        System.out.println("In create client(" + mainMember.getId() + ") = " + mainMember.getId() + "\n");

        // establish the connection
        InetAddress IP_ADDRESS = InetAddress.getLocalHost();
        Socket socket = new Socket(IP_ADDRESS, 9000);
        System.out.println(socket);

        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());

//        Member member = new Member("kkk", "192.168.0.100", 9000, 8000);
        System.out.println("Pass Member View to server: " + mainMember);
        outputStream.writeObject(mainMember);
        outputStream.flush();

        System.out.println("OUTPUT S: " + outputStream);

        // readMessage thread
        Thread readMessage = new Thread(() -> {
            while (true) {
                System.out.println("In read msg view while (" + mainMember.getId() + ")");
                try {
                    // read the message sent to this client
                    Message msg = (Message) inputStream.readObject();

                    // don't show my own message
                    System.out.println("");
                    System.out.println("send:" + msg.getSender().getId());
                    System.out.println("main:" + mainMember.getId());
                    if (!msg.getSender().getId().equals(mainMember.getId()))
                        addText(msg);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        readMessage.start();
    }

    // add text in server chat box
    private synchronized void addText(Message message) {
        System.out.println("In add message");
        String oldTxt = groupChatAreaId.getText();
        if (groupChatAreaId.getText().equals("")) {
            groupChatAreaId.setText(String.format(
                    "FROM (ID:%s) :: %s", message.getSender().getId(), message.getMessage()
            ));
        } else {
            groupChatAreaId.setText(oldTxt + "\n" + String.format(
                    "FROM (ID:%s) :: %s", message.getSender().getId(), message.getMessage()
            ));
        }
    }

    // update state list without current member
    private synchronized void updateMemberListState() {
        ObservableList<Member> items = FXCollections.observableArrayList(manager.getMembers());
        items.remove(mainMember);
        membersListViewId.setItems(items);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void refreshStateButtonAction(ActionEvent event) {
        updateMemberListState();
    }

    @FXML
    public void groupSendBtnAction(ActionEvent event) {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("In send message (" + mainMember.getId() + ")");

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
        private final Button settingButton = new Button();
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
            lPort.setText("Listening Port: " + member.getListeningPort());

            settingButton.setText("Setting");
            chatButton.setText("Chat");

            setGraphic(getHBox(member));
        }

        // build horizonal box
        private HBox getHBox(Member member) {

            // if member is coordinator add setting button, else not add setting button
            HBox hbox;
            if (manager.isCoordinator(mainMember)) {
                hbox = new HBox(id, lPort, settingButton, chatButton);
            } else {
                hbox = new HBox(id, lPort, chatButton);
            }

            hbox.setSpacing(20);
            hbox.setAlignment(Pos.CENTER);

            settingButton.setOnAction(event -> {
                // Handle setting button action
                System.out.println("In setting chat");
            });

            chatButton.setOnAction(event -> {
                // handle personal details button
                System.out.println("In personal chat");
            });

            return hbox;
        }
    }
}
