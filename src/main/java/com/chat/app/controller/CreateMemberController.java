package com.chat.app.controller;

import com.chat.app.App;
import com.chat.app.model.Member;
import com.chat.app.server.GroupMessingServer;
import com.chat.app.util.DTO;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
    private GroupMessingServer server;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        server = GroupMessingServer.getInstance();
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
            else if (server.getManager().isUniqueId(id)) {
                Member member = new Member(id, serverIpAddress, serverPort, listeningPort);
                server.getManager().addMember(member);
                loadMemberView(member, event);
                System.out.println("Member created! = " + member);
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

            stage.setOnCloseRequest(event1 -> {
                System.out.println("IN CHAT VIEW CLOSE");
                // TODO: REMOVE FROM MEMBERSHIP &
                // CLIENT HANDLER
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
