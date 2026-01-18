package com.agromarket.ampl_chat.models;

public class VendorProduct {
    private String title;
    private String brandName;
    private String expiry;

    public VendorProduct(String title, String brandName, String expiry) {
        this.title = title;
        this.brandName = brandName;
        this.expiry = expiry;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
}