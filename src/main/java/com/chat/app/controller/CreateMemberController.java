package com.chat.app.controller;

import com.chat.app.App;
import com.chat.app.model.Member;
import com.chat.app.server.GroupMessingServer;
import com.chat.app.server.MembershipManager;
import com.chat.app.util.DTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CreateMemberController implements Initializable {

    @FXML
    public TextField memberIdTxtFldId;
    @FXML
    public TextField serverPortTxtFldID;
    @FXML
    public TextField serverIpAddressTxtFldID;
    @FXML
    public TextField listeningPortTxtFld;
    @FXML
    public Label errorTxtMsgId;

    // server and manager
    private volatile static MembershipManager manager;

    // static block
    static {
        manager = MembershipManager.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    public void startChatButtonAction(ActionEvent event) {
        try {
            String id = memberIdTxtFldId.getText();
            int serverPort = Integer.parseInt(serverPortTxtFldID.getText());
            String serverIpAddress = serverIpAddressTxtFldID.getText();
            int listeningPort = Integer.parseInt(listeningPortTxtFld.getText());

            // check if given information match with our server or not
            if (serverPort != GroupMessingServer.DEFAULT_PORT ||
                    !serverIpAddress.equals(GroupMessingServer.IP_ADDRESS.getHostAddress())) {
                errorTxtMsgId.setText("Server not found, with your given information.");
            }

            // check id is unique or not
            else if (manager.isUniqueId(id)) {
                Member member = new Member(id, serverIpAddress, serverPort, listeningPort);
                manager.addMember(member);
                loadMemberView(member, event);
            } else {
                errorTxtMsgId.setText(id + " id is already registered in server, please try with a different id.");
            }
        } catch (Exception e) {
            errorTxtMsgId.setText("Server port and listening port should be an number. Also, all filed should be filled. ");
//            e.printStackTrace();
        }
    }

    // load Member chat interface and send member data
    private void loadMemberView(Member member, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("member_view.fxml"));
            Parent layout = loader.load();
            // transfer data to the controller

            DTO controller = loader.getController();
            controller.transfer(member);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(layout);
            stage.setTitle("Chat Home");
            stage.setScene(scene);

            // inform server so server can inform others
            GroupMessingServer.sendNotificationToEveryone(member.getId()
                    + " is joined in server!");

            // if a member close or disconnected
            stage.setOnCloseRequest(event1 -> {
                System.out.println("IN CHAT VIEW CLOSE");
                manager.removeMember(member);

                // inform server so server can inform others
                GroupMessingServer.sendNotificationToEveryone(member.getId()
                        + " is discounted from server!");
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
