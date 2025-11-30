package com.agromarket.ampl_chat.models;

public class ChatItem {
    private int customerId;
    private String email;
    private String name;
    private String lastMessage;
    private String time;
    private int unreadCount;

    public ChatItem(int customerId, String name, String email) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.lastMessage = "Tap to chat";  // default
        this.time = "";
        this.unreadCount = 0;
    }

    public int getCustomerId() { return customerId; }
    public String getEmail() { return email; }

    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public int getUnreadCount() { return unreadCount; }

    public void setLastMessage(String msg) { this.lastMessage = msg; }
    public void setTime(String time) { this.time = time; }
    public void setUnreadCount(int count) { this.unreadCount = count; }
}