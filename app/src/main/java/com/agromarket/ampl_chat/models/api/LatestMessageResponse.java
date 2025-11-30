package com.agromarket.ampl_chat.models.api;

public class LatestMessageResponse {
    public boolean status;
    public LatestMessage message;

    public static class LatestMessage {
        public int id;
        public int sender_id;
        public int receiver_id;
        public String message;     // text or product title
        public String type;        // "text" / "product"
        public String created_at;  // for displaying time
    }
}
