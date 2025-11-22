package com.agromarket.ampl_chat.models;

public class MessageItem {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;

    public int type;
    public String text;
    public int imageRes;
}