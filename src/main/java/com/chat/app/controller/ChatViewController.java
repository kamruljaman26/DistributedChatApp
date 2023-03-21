package com.chat.app.controller;

import com.chat.app.model.Member;
import com.chat.app.model.Message;
import com.chat.app.model.MessageType;
import com.chat.app.util.DTO;
import com.chat.app.util.SendMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatViewController implements Initializable, DTO, SendMessage {

    @FXML
    public TextField messageTxtFldId;
    @FXML
    public Label senderLblId;
    @FXML
    public Label receiverLblID;
    @FXML
    public TextArea charTxtAreaID;

    private Member sender;
    private Member receiver;
    private SendMessage controller; // attached controller to transfer message

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageTxtFldId.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                // code to execute when the "Enter" key is pressed
                sendButtonAction(new ActionEvent());
            }
        });

        // chat box will not editable
        charTxtAreaID.setEditable(false);
    }

    @Override
    public void transfer(Object... data) {
        sender = (Member) data[0];
        receiver = (Member) data[1];
        controller = (SendMessage) data[2];

        // init fields
        senderLblId.setText("SENDER: " + sender.getId());
        receiverLblID.setText("RECEIVER: " + receiver.getId());
    }

    @FXML
    public void sendButtonAction(ActionEvent event) {
        Message message = new Message(sender, receiver,
                MessageType.PRIVATE, messageTxtFldId.getText());
        controller.send(message);

        String text = charTxtAreaID.getText();
        charTxtAreaID.setText(text + "\n" + String.format(
                "ME::\t%s", message.getMessage()
        ));

        messageTxtFldId.setText("");
    }

    // receive message member view
    @Override
    public void send(Message message) {
        String text = charTxtAreaID.getText();
        charTxtAreaID.setText(text + "\n" + String.format(
                "BY(%s)::\t%s", message.getSender().getId(), message.getMessage()
        ));
    }
}
