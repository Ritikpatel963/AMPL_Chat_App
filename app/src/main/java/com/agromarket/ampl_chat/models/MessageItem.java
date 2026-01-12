package com.agromarket.ampl_chat.models;

public class MessageItem {

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;

    public static final int STATUS_SENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_FAILED = 2;
    public static final int STATUS_SEEN = 3;

    public int type;
    public String text;
    public String imageUrl;
    public int productId;

    public boolean isSent;

    public int status;

    public String time;

    public String localId;
}