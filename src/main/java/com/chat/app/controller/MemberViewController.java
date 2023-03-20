package com.chat.app.controller;

import com.chat.app.model.Member;
import com.chat.app.server.GroupMessingServer;
import com.chat.app.server.MembershipManager;
import com.chat.app.util.DTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class MemberViewController implements Initializable, DTO {

    @FXML
    public ListView<Member> membersListViewId;
    @FXML
    public TextArea groupChatAreaId;
    @FXML
    public TextField sendMsgTxtFldId;
    @FXML
    public Label memberdetailsLblId;

    // properties
    private Member member;
    private MembershipManager manager;
    private GroupMessingServer server;

    @Override
    public void transfer(Object... data) {
        member = (Member) data[0];

        server = GroupMessingServer.getInstance();
        manager = server.getManager();

        System.out.println("\nmembers");
        System.out.println("curr = " + member);
        for (Member member : manager.getMembers()) {
            System.out.println(member);
        }

        // update member details
        memberdetailsLblId.setText(String.format("Member Details: ID: %s, Listening Port: %d",
                member.getId(), member.getListeningPort()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void refreshStateButtonAction(ActionEvent event) {

    }

    @FXML
    public void groupSendBtnAction(ActionEvent event) {

    }
}
