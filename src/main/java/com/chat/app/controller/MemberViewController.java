package com.chat.app.controller;

import com.chat.app.model.Member;
import com.chat.app.util.DTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class MemberViewController implements Initializable, DTO {

    @FXML public ListView<Member> membersListViewId;
    @FXML public TextArea groupChatAreaId;
    @FXML public TextField sendMsgTxtFldId;

    @Override
    public void transfer(Object... data) {

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
