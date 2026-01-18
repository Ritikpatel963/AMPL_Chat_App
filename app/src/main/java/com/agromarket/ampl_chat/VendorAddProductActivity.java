package com.agromarket.ampl_chat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class VendorAddProductActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private LinearLayout imageUploadContainer;
    private ImageView uploadIcon;
    private ImageView productImagePreview;
    private AutoCompleteTextView productNameInput;
    private AutoCompleteTextView productCategoryInput;
    private TextInputEditText brandNameInput;
    private AutoCompleteTextView productWeightInput;
    private TextInputEditText productQtyInput;
    private TextInputEditText productExpiryInput;
    private Button btnUploadProduct;
    private Button btnDownloadCsv;
    private Button btnAddProduct;

    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_add_product);

        initViews();
        setupWindowInsets();
        setupToolbar();
        setupNavigationDrawer();
        setupDropdowns();
        setupImagePicker();
        setupClickListeners();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        imageUploadContainer = findViewById(R.id.imageUploadContainer);
        uploadIcon = findViewById(R.id.uploadIcon);
        productImagePreview = findViewById(R.id.productImagePreview);
        productNameInput = findViewById(R.id.productNameInput);
        productCategoryInput = findViewById(R.id.productCategoryInput);
        brandNameInput = findViewById(R.id.brandNameInput);
        productWeightInput = findViewById(R.id.productWeightInput);
        productQtyInput = findViewById(R.id.productQtyInput);
        productExpiryInput = findViewById(R.id.productExpiryInput);
        btnUploadProduct = findViewById(R.id.btnUploadProduct);
        btnDownloadCsv = findViewById(R.id.btnDownloadCsv);
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
                startActivity(new Intent(this, VendorProductListActivity.class));
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupDropdowns() {
        // Product Names
        String[] productNames = {"Wheat", "Rice", "Corn", "Barley", "Soybean"};
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, productNames);
        productNameInput.setAdapter(nameAdapter);

        // Product Categories
        String[] categories = {"Grains", "Vegetables", "Fruits", "Seeds", "Fertilizers"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        productCategoryInput.setAdapter(categoryAdapter);

        // Product Weights
        String[] weights = {"1 Kg", "5 Kg", "10 Kg", "25 Kg", "50 Kg", "1 Liter", "5 Liter", "10 Liter"};
        ArrayAdapter<String> weightAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, weights);
        productWeightInput.setAdapter(weightAdapter);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        productImagePreview.setImageURI(selectedImageUri);
                        productImagePreview.setVisibility(View.VISIBLE);
                        uploadIcon.setVisibility(View.GONE);
                    }
                }
        );
    }

    private void setupClickListeners() {
        imageUploadContainer.setOnClickListener(v -> openImagePicker());

        productExpiryInput.setOnClickListener(v -> showDatePicker());

        btnUploadProduct.setOnClickListener(v -> {
            Toast.makeText(this, "Upload Product feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnDownloadCsv.setOnClickListener(v -> {
            Toast.makeText(this, "Download CSV feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnAddProduct.setOnClickListener(v -> addProduct());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    productExpiryInput.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void addProduct() {
        String productName = productNameInput.getText().toString().trim();
        String category = productCategoryInput.getText().toString().trim();
        String brandName = brandNameInput.getText().toString().trim();
        String weight = productWeightInput.getText().toString().trim();
        String qty = productQtyInput.getText().toString().trim();
        String expiry = productExpiryInput.getText().toString().trim();

        // Validation
        if (productName.isEmpty()) {
            productNameInput.setError("Please select product name");
            return;
        }
        if (category.isEmpty()) {
            productCategoryInput.setError("Please select category");
            return;
        }
        if (brandName.isEmpty()) {
            brandNameInput.setError("Please enter brand name");
            return;
        }
        if (weight.isEmpty()) {
            productWeightInput.setError("Please select weight");
            return;
        }
        if (qty.isEmpty()) {
            productQtyInput.setError("Please enter quantity");
            return;
        }
        if (expiry.isEmpty()) {
            productExpiryInput.setError("Please select expiry date");
            return;
        }

        // TODO: Save product to database/API
        Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to product list
        startActivity(new Intent(this, VendorProductListActivity.class));
        finish();
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