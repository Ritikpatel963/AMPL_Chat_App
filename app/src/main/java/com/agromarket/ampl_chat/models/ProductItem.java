package com.agromarket.ampl_chat.models;

import androidx.annotation.Nullable;

import com.agromarket.ampl_chat.models.api.Product;

public class ProductItem {

    /* ================= DATA ================= */

    public int id;
    public String name;
    public String imageUrl;
    public String price;

    public boolean isSkeleton;
    public boolean isSelected;

    /* ================= CONSTRUCTORS ================= */

    /** Skeleton placeholder */
    public ProductItem(boolean isSkeleton) {
        this.isSkeleton = isSkeleton;
    }

    /** Normal product */
    public ProductItem(int id, String name, @Nullable String imageUrl, String price) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.isSkeleton = false;
        this.isSelected = false;
    }

    /* ================= FACTORY ================= */

    /** Convert API product → UI model */
    public static ProductItem fromApi(Product p, String baseUrl) {
        String img = null;

        if (p.image != null && !p.image.isEmpty()) {
            img = baseUrl +
                    (p.image.startsWith("/")
                            ? p.image.substring(1)
                            : p.image);
        }

        return new ProductItem(
                p.id,
                p.name,
                img,
                p.sale_price
        );
    }

    /* ================= UI HELPERS ================= */

    public void toggleSelection() {
        if (isSkeleton) return;
        isSelected = !isSelected;
    }

    public void clearSelection() {
        isSelected = false;
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    /* ================= UTILS ================= */

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProductItem)) return false;
        return ((ProductItem) obj).id == this.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
