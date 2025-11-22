package com.agromarket.ampl_chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agromarket.ampl_chat.R;
import com.agromarket.ampl_chat.models.ProductItem;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductHolder> {

    Context context;
    ArrayList<ProductItem> list;
    OnProductClick listener;

    public interface OnProductClick {
        void onClick(ProductItem item);
    }

    public ProductAdapter(ArrayList<ProductItem> list, OnProductClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ProductHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHolder holder, int position) {
        ProductItem item = list.get(position);

        if (item.imageRes != 0) {
            holder.image.setImageResource(item.imageRes);
        } else {
            holder.image.setImageResource(R.drawable.ic_product_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ProductHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public ProductHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.productImage);
        }
    }
}