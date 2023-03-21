package com.chat.app.util;

import com.chat.app.model.Message;

// used to send messages between 2 controller
public interface SendMessage {
    void send(Message message);
}
