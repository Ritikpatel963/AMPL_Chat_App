package com.agromarket.ampl_chat.models;

public class ProductItem {
    public int id;
    public String name;
    public String imageUrl;
    public String price;
    public boolean isSkeleton;
    public boolean isSelected;

    public ProductItem(boolean isSkeleton) {
        this.isSkeleton = isSkeleton;
    }

    public ProductItem(int id, String name, String imageUrl, String price) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.isSkeleton = false;
    }
}