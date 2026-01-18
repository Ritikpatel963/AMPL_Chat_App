package com.agromarket.ampl_chat.models.api;

import java.util.List;

public class VendorProductMetricsResponse {
    public boolean status;
    public Metrics metrics;
    public List<RecentProduct> recent_products;

    public static class Metrics {
        public int total_products;
        public int active_products;
        public int near_expiry_products;
    }

    public static class RecentProduct {
        public int id;
        public String product_name;
        public double product_rate;
        public int quantity;
        public String product_expiry_formatted;
        public List<String> images;
        public String created_at;
        public String brand_name;
    }
}