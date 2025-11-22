package com.agromarket.ampl_chat.models;

public class ChatItem {
    private String name;
    private String message;
    private String time;
    private int unreadCount;

    public ChatItem(String name, String message, String time, int unreadCount) {
        this.name = name;
        this.message = message;
        this.time = time;
        this.unreadCount = unreadCount;
    }

    public String getName() { return name; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public int getUnreadCount() { return unreadCount; }
}