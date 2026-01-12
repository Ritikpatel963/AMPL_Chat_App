package com.agromarket.ampl_chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agromarket.ampl_chat.R;
import com.agromarket.ampl_chat.models.ProductItem;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductHolder> {

    private Context context;
    private ArrayList<ProductItem> list;
    private OnProductClick listener;

    private static final int TYPE_SKELETON = 0;
    private static final int TYPE_PRODUCT = 1;

    public interface OnProductClick {
        void onClick(ProductItem item);
    }

    public ProductAdapter(ArrayList<ProductItem> list, OnProductClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v;

        if (viewType == TYPE_SKELETON) {
            v = LayoutInflater.from(context)
                    .inflate(R.layout.product_item_skeleton, parent, false);
        } else {
            v = LayoutInflater.from(context)
                    .inflate(R.layout.product_item, parent, false);
        }
        return new ProductHolder(v);
    }

    @Override
    public void onBindViewHolder(ProductHolder holder, int position) {
        ProductItem item = list.get(position);

        if (item.isSkeleton) return;

        Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_product_placeholder)
                .into(holder.image);

        holder.name.setText(item.name);
        holder.price.setText(item.price);

        holder.checkIcon.setVisibility(
                item.isSelected ? View.VISIBLE : View.GONE
        );
        holder.itemView.setOnClickListener(v -> {
            item.isSelected = !item.isSelected;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).isSkeleton ? TYPE_SKELETON : TYPE_PRODUCT;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ProductHolder extends RecyclerView.ViewHolder {
        ImageView image, checkIcon;
        TextView name, price;

        public ProductHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.productImage);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            checkIcon = itemView.findViewById(R.id.checkIcon);
        }
    }
}