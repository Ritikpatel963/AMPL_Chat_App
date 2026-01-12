package com.agromarket.ampl_chat.models;

public class ChatItem {
    private int customerId;
    private String email;
    private String name;
    private String lastMessage;
    private String time;
    private int unreadCount;
    private long lastMessageTimestamp;
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
    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }
    public void setLastMessageTimestamp(long ts) {
        this.lastMessageTimestamp = ts;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChatItem)) return false;
        ChatItem other = (ChatItem) obj;
        return customerId == other.customerId;
    }

    @Override
    public int hashCode() {
        return customerId;
    }
}