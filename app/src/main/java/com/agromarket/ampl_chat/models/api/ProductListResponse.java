package com.agromarket.ampl_chat.models.api;

import java.util.List;

public class ProductListResponse {
    public boolean status;
    public List<Product> products;
    public int current_page;
    public int last_page;
    public boolean has_more;
}