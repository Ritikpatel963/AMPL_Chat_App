package com.agromarket.ampl_chat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agromarket.ampl_chat.R;
import com.agromarket.ampl_chat.models.VendorProduct;
import com.agromarket.ampl_chat.models.api.Product;

import java.util.List;

public class VendorProductAdapter extends RecyclerView.Adapter<VendorProductAdapter.ProductViewHolder> {

    private List<VendorProduct> productList;
    private OnMenuClickListener menuClickListener;

    public interface OnMenuClickListener {
        void onMenuClick(View view, int position);
    }

    public VendorProductAdapter(List<VendorProduct> productList, OnMenuClickListener menuClickListener) {
        this.productList = productList;
        this.menuClickListener = menuClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendor_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        VendorProduct product = productList.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.brandName.setText(product.getBrandName());
        holder.productExpiry.setText(product.getExpiry());

        holder.menuIcon.setOnClickListener(v ->
                menuClickListener.onMenuClick(v, holder.getAdapterPosition())
        );
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productTitle, brandName, productExpiry;
        ImageView menuIcon, productImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            brandName = itemView.findViewById(R.id.brandName);
            productExpiry = itemView.findViewById(R.id.productExpiry);
            menuIcon = itemView.findViewById(R.id.menuIcon);
        }
    }
}