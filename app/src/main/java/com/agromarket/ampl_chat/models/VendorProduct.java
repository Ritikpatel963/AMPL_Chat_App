package com.agromarket.ampl_chat.models;

import java.util.List;

public class VendorProduct {
    private int id;
    private String title;
    private String brandName;
    private String expiry;
    private double productRate;
    private int quantity;
    private List<String> images;

    public VendorProduct(String title, String brandName, String expiry) {
        this.title = title;
        this.brandName = brandName;
        this.expiry = expiry;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getExpiry() {
        return expiry;
    }

    public double getProductRate() {
        return productRate;
    }

    public int getQuantity() {
        return quantity;
    }

    public List<String> getImages() {
        return images;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public void setProductRate(double productRate) {
        this.productRate = productRate;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}