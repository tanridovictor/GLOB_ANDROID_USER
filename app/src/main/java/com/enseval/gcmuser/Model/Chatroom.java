package com.enseval.gcmuser.Model;

import java.util.Date;

public class Chatroom {
    private String roomId;
    private int userId;
    private int companyId;
    private String companyName;
    private String lastMessage;
    private int lastSenderId;
    private boolean isRead;
    private Date timestamp;
    private boolean isEmpty;
    private int count_message_read_false;

    public Chatroom(String roomId, int userId, int companyId, String companyName, String lastMessage, int lastSenderId, boolean isRead, Date timestamp) {
        this.roomId = roomId;
        this.userId = userId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.lastMessage = lastMessage;
        this.lastSenderId = lastSenderId;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    public Chatroom(String roomId, int companyId, String companyName, boolean isEmpty) {
        this.roomId = roomId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.isEmpty = isEmpty;
    }

    public Chatroom(String roomId, int companyId, String companyName, Date timestamp, String lastMessage, int count_message_read_false) {
        this.roomId = roomId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.timestamp = timestamp;
        this.lastMessage = lastMessage;
        this.count_message_read_false = count_message_read_false;
    }



    public String getRoomId() {
        return roomId;
    }

    public int getCount_message_read_false() {
        return count_message_read_false;
    }

    public int getUserId() {
        return userId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public int getLastSenderId() {
        return lastSenderId;
    }

    public boolean isRead() {
        return isRead;
    }

    public Date getDate() {
        return timestamp;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastSenderId(int lastSenderId) {
        this.lastSenderId = lastSenderId;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setDate(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}
