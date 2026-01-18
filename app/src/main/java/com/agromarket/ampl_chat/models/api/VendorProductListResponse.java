package com.agromarket.ampl_chat.models.api;

import java.util.List;

public class VendorProductListResponse {
    public boolean status;
    public List<VendorProduct> products;

    public static class VendorProduct {
        public int id;
        public int vendor_id;
        public String product_name;
        public int category_id;
        public String brand_name;
        public String unit_type;
        public String unit_size;
        public double product_rate;
        public String product_expiry;
        public int quantity;
        public List<String> images;
        public String created_at;
        public String updated_at;
        public String product_expiry_formatted;
    }
}