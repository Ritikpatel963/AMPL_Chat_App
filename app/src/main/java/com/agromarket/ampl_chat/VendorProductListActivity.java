package com.agromarket.ampl_chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agromarket.ampl_chat.adapters.VendorProductAdapter;
import com.agromarket.ampl_chat.models.VendorProduct;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class VendorProductListActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView productRecycler;
    private VendorProductAdapter productAdapter;
    private List<VendorProduct> productList;
    private Button btnAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_product_list);

        initViews();
        setupWindowInsets();
        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        loadProducts();
        setupClickListeners();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        productRecycler = findViewById(R.id.productRecycler);
        btnAddProduct = findViewById(R.id.btnAddProduct);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                finish();
            } else if (id == R.id.nav_products) {
                Toast.makeText(this, "Already on Products page", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new VendorProductAdapter(productList, this::showProductMenu);
        productRecycler.setLayoutManager(new LinearLayoutManager(this));
        productRecycler.setAdapter(productAdapter);
    }

    private void loadProducts() {
        // Sample data - replace with actual data from database/API
        productList.add(new VendorProduct("Product Title", "Brand Name", "Product Expiry"));
        productList.add(new VendorProduct("Product Title", "Brand Name", "Product Expiry"));
        productList.add(new VendorProduct("Product Title", "Brand Name", "Product Expiry"));
        productList.add(new VendorProduct("Product Title", "Brand Name", "Product Expiry"));
        productList.add(new VendorProduct("Product Title", "Brand Name", "Product Expiry"));
        productAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(this, VendorAddProductActivity.class));
        });
    }

    private void showProductMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.product_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_edit) {
                editProduct(position);
                return true;
            } else if (id == R.id.menu_delete) {
                deleteProduct(position);
                return true;
            } else if (id == R.id.menu_share) {
                shareProduct(position);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void editProduct(int position) {
        Toast.makeText(this, "Edit product at position " + position, Toast.LENGTH_SHORT).show();
        // TODO: Navigate to edit product activity
        // Intent intent = new Intent(this, EditProductActivity.class);
        // intent.putExtra("product_id", productList.get(position).getId());
        // startActivity(intent);
    }

    private void deleteProduct(int position) {
        // TODO: Show confirmation dialog before deleting
        productList.remove(position);
        productAdapter.notifyItemRemoved(position);
        productAdapter.notifyItemRangeChanged(position, productList.size());
        Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
    }

    private void shareProduct(int position) {
        VendorProduct product = productList.get(position);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Check out this product: " + product.getTitle() +
                        "\nBrand: " + product.getBrandName());
        startActivity(Intent.createChooser(shareIntent, "Share product via"));
    }

    private void logout() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
}