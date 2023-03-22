package com.chat.app.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Util {
    public static final int DEFAULT_PORT = 9000;
    public static InetAddress IP_ADDRESS;

    static {
        try {
            IP_ADDRESS = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
