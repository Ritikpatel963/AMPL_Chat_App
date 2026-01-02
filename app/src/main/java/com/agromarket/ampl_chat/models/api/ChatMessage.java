package com.agromarket.ampl_chat.models.api;

public class ChatMessage {
    public int id;
    public int sender_id;
    public int receiver_id;
    public String message;
    public String type;       // "text" or "product"
    public ChatMessageData data; // null for text
    public UserShort sender;
    public UserShort receiver;
    public String created_at_formatted;
    public String seen_at;
}
