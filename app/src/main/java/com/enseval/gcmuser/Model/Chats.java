package com.enseval.gcmuser.Model;

import java.util.Date;

public class Chats {
    private String message;
    private int userId;
    private boolean isRead;
    private Date timestamp;
    private String chatroom;
    private String bubbleKey;
    private String tipe;
    private int barangId;
    private int hargaTerakhir;

    public Chats(String message, int userId, boolean isRead, Date timestamp) {
        this.message = message;
        this.userId = userId;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    public Chats(String chatroom, String bubbleKey, String message, int userId, boolean isRead, Date timestamp, String tipe) {
        this.message = message;
        this.userId = userId;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.chatroom = chatroom;
        this.bubbleKey = bubbleKey;
        this.tipe = tipe;
    }

    public Chats(String chatroom, String bubbleKey, String message, int userId, boolean isRead, Date timestamp, String tipe, int barangId) {
        this.message = message;
        this.userId = userId;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.chatroom = chatroom;
        this.bubbleKey = bubbleKey;
        this.tipe = tipe;
        this.barangId = barangId;
    }

    public Chats(String chatroom, String bubbleKey, String message, int userId, boolean isRead, Date timestamp, String tipe, int barangId, int hargaTerakhir) {
        this.message = message;
        this.userId = userId;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.chatroom = chatroom;
        this.bubbleKey = bubbleKey;
        this.tipe = tipe;
        this.barangId = barangId;
        this.hargaTerakhir = hargaTerakhir;
    }

    public int getHargaTerakhir() {
        return hargaTerakhir;
    }

    public String getMessage() {
        return message;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isRead() {
        return isRead;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getChatroom() {
        return chatroom;
    }

    public String getBubbleKey() {
        return bubbleKey;
    }

    public String getTipe() {
        return tipe;
    }

    public int getBarangId() {
        return barangId;
    }
}
