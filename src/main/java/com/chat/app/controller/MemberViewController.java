package com.chat.app.controller;

import com.chat.app.App;
import com.chat.app.model.Member;
import com.chat.app.model.Message;
import com.chat.app.model.MessageType;
import com.chat.app.server.ClientConnection;
import com.chat.app.server.GroupMessagingServer;
import com.chat.app.server.MembershipManager;
import com.chat.app.util.DTO;
import com.chat.app.util.SendMessage;
import com.chat.app.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 */
public class MemberViewController implements Initializable, DTO, SendMessage {

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
    private volatile ObjectInputStream inputStream;
    private volatile ObjectOutputStream outputStream;

    // private chats
    private final Map<String, SendMessage> sendMessageMap = new HashMap<>();

    // static block
    static {
        manager = MembershipManager.getInstance();
    }

    @Override
    public void transfer(Object... data) {
        mainMember = (Member) data[0];

        // update member details in gui
        memberdetailsLblId.setText(String.format("Member Details: ID: %s,\t Listening Port: %d,\t" +
                        "Coordinator: %s",
                mainMember.getId(), mainMember.getListeningPort(), manager.isCoordinator(mainMember)));

        // init list view with members
        membersListViewId.setCellFactory(param -> new MemberListCell(mainMember, this, sendMessageMap));
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

    @Override
    public void send(Message message) {
        // send message to server
        try {
            outputStream.writeObject(message);
            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sendMsgTxtFldId.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                // code to execute when the "Enter" key is pressed
                groupSendBtnAction(new ActionEvent());
            }
        });

        // chat box will not editable
        groupChatAreaId.setEditable(false);
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
                // write on the output stream
                Message message = new Message(mainMember, null,
                        MessageType.BROADCAST, sendMsgTxtFldId.getText());

                // send message to server
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

    // create and init client server
    private void createClient() throws IOException {

        // establish the connection
        Socket socket = new Socket(Util.IP_ADDRESS, Util.DEFAULT_PORT);
        inputStream = new ObjectInputStream(socket.getInputStream());
        outputStream = new ObjectOutputStream(socket.getOutputStream());

        outputStream.writeObject(mainMember);
        outputStream.flush();

        // read message
        Thread readMessage = new Thread(() -> {
            while (true) {
                try {
                    // read the message sent to this client
                    Message msg = (Message) inputStream.readObject();
//                    System.out.println("READ MESSAGE WHILE(" + mainMember.getId() + "):: " + msg);

                    // read notification
                    if (msg.getMessageType().equals(MessageType.NOTIFICATION)) {
                        addText(msg);
                    }

                    // read broadcast message
                    if (msg.getMessageType().equals(MessageType.BROADCAST)) {
                        // don't show my own message
                        if (!msg.getSender().getId().equals(mainMember.getId()))
                            addText(msg);
                    }

                    // read broadcast message
                    if (msg.getMessageType().equals(MessageType.REMOVE)) {
                        addText(msg);
                    }

                    // read private message
                    if (msg.getMessageType().equals(MessageType.PRIVATE)) {

                        if (sendMessageMap.containsKey(msg.getSender().getId())) {
                            sendMessageMap.get(msg.getSender().getId()).send(msg);
                        } else {
                            addText(new Message(null, null, MessageType.NEW_CHAT,
                                    msg.getSender().getId() + " want to open private chat with!"));
                        }
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        readMessage.start();
    }

    /**
     * Show messaged with tag
     *
     * @param message
     */
    private synchronized void addText(Message message) {

        // show notification in chat area
        if (message.getMessageType().equals(MessageType.NEW_CHAT)) {
            String oldTxt = groupChatAreaId.getText();
            groupChatAreaId.setText(oldTxt + "\n" + String.format(
                    "REQUEST::\t%s", message.getMessage()
            ));
        }

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

        // broadcast remove message in chat area
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

    // close the app
    public void quitButtonAction(ActionEvent event) {
        Stage window = (Stage) notificationLebelID.getScene().getWindow();
        window.close();
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
        private final SendMessage memberViewController;
        private final Map<String, SendMessage> sendMessageMap;

        public MemberListCell(Member mainMember, SendMessage memberViewController, Map<String, SendMessage> sendMessageMap) {
            this.mainMember = mainMember;
            this.memberViewController = memberViewController;
            this.sendMessageMap = sendMessageMap;
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
                GroupMessagingServer.removeAndNotify(member);
            });

            // handle personal details button
            chatButton.setOnAction(event -> {
                try {

                    // should only open one chat box per member
                    if (!sendMessageMap.containsKey(member.getId())) {

                        FXMLLoader loader = new FXMLLoader(App.class.getResource("chat_view.fxml"));
                        Parent layout = loader.load();

                        // transfer sender, receiver, and controller
                        DTO controller = loader.getController();
                        controller.transfer(mainMember, member, memberViewController);

                        // save send message controller
                        SendMessage chatController = loader.getController();
                        sendMessageMap.put(member.getId(), chatController);

                        Stage stage = new Stage();
                        Scene scene = new Scene(layout);
                        stage.setTitle("Personal Chat");
                        stage.setScene(scene);
                        stage.show();

                        // remove after close
                        stage.setOnCloseRequest(event1 -> {
                            sendMessageMap.remove(member.getId());
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return hbox;
        }
    }
}
