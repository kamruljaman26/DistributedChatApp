package com.chat.app.controller;

import com.chat.app.util.DTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatViewController implements Initializable, DTO {

    @FXML
    public Label memberLblId;
    @FXML
    public TextField messageTxtFldId;
    @FXML
    public TextField memberIdTxtFldId;
    @FXML
    public TextField serverPortTxtFldID;
    @FXML
    public TextField serverIpAddressTxtFldID;
    @FXML
    public TextField listeningPortIpAddressTxtFld;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void transfer(Object... data) {

    }

    @FXML
    public void quitButtonAction(ActionEvent event) {

    }

    @FXML
    public void sendButtonAction(ActionEvent event) {
    }

    @FXML
    public void startChatButtonAction(ActionEvent event) {

    }

}
