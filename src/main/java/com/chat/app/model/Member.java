package com.chat.app.model;

import java.io.Serializable;
import java.util.Objects;

// a pojo member class
public class Member implements Serializable {

    private String id;
    private String serverIpAddress;
    private int serverPort;
    private int listeningPort;

    public Member(String id, String serverIpAddress, int serverPort, int listeningPort) {
        this.id = id;
        this.serverIpAddress = serverIpAddress;
        this.serverPort = serverPort;
        this.listeningPort = listeningPort;
    }

    public String getId() {
        return id;
    }

    public String getServerIpAddress() {
        return serverIpAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setServerIpAddress(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setListeningPort(int listeningPort) {
        this.listeningPort = listeningPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id='" + id + '\'' +
                ", serverIpAddress='" + serverIpAddress + '\'' +
                ", serverPort=" + serverPort +
                ", listeningPort=" + listeningPort +
                '}';
    }
}
