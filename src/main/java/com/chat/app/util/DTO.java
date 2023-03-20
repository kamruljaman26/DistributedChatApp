package com.chat.app.util;

import com.chat.app.model.Message;

/**
 * A data traveler object between 2 controller,
 * Implemented class able to send multiple objects
 */
public interface DTO {
    void transfer(Object... data);
}
