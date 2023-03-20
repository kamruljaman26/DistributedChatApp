package com.chat.app.model;

import java.io.Serializable;

// a pojo message object
public class Message implements Serializable {

    private Member sender;
    private Member receiver;
    private MessageType messageType;
    private String message;

    public Message(Member sender, Member receiver, MessageType messageType, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.messageType = messageType;
        this.message = message;
    }

    public Member getSender() {
        return sender;
    }

    public void setSender(Member sender) {
        this.sender = sender;
    }

    public Member getReceiver() {
        return receiver;
    }

    public void setReceiver(Member receiver) {
        this.receiver = receiver;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender=" + sender +
                ", receiver=" + receiver +
                ", messageType=" + messageType +
                ", message='" + message + '\'' +
                '}';
    }
}
