package com.agromarket.ampl_chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agromarket.ampl_chat.adapters.ChatAdapter;
import com.agromarket.ampl_chat.models.ChatItem;
import com.agromarket.ampl_chat.models.Customer;
import com.agromarket.ampl_chat.models.api.CustomerListResponse;
import com.agromarket.ampl_chat.utils.ApiClient;
import com.agromarket.ampl_chat.utils.ApiService;
import com.agromarket.ampl_chat.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private ArrayList<ChatItem> list;
    private EditText searchEdit;
    private TextView txtEmpty, txtNotFound;

    private SwipeRefreshLayout swipeRefresh;

    // Global instances to save memory
    private SessionManager session;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Services once
        session = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupWindowInsets();
        setupRecyclerView();
        setupSearch();

        loadCustomers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomers();
    }

    private void initViews() {
        ImageView btnLogout = findViewById(R.id.btnLogout);
        recyclerView = findViewById(R.id.recyclerChats);
        searchEdit = findViewById(R.id.searchEdit);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtNotFound = findViewById(R.id.txtNotFound);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::loadCustomers);

        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new ChatAdapter(this);
        recyclerView.setAdapter(adapter);

        // Handle Search Results from Adapter
        adapter.setOnSearchResultListener(count -> updateEmptyState(searchEdit.getText().toString()));
    }

    private void setupSearch() {
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void updateEmptyState(String searchQuery) {
        if (adapter.getItemCount() == 0 && searchQuery.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            txtNotFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
        else if (adapter.getItemCount() == 0) {
            txtEmpty.setVisibility(View.GONE);
            txtNotFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else {
            txtEmpty.setVisibility(View.GONE);
            txtNotFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadCustomers() {
        String token = session.getToken();
        if (token == null) {
            goToLogin();
            return;
        }

        apiService.getAssignedCustomers("Bearer " + token)
                .enqueue(new Callback<CustomerListResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CustomerListResponse> call,
                                           @NonNull Response<CustomerListResponse> response) {
                        if (isFinishing()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            list.clear();

                            for (Customer c : response.body().customers) {
                                ChatItem item = new ChatItem(c.id, c.name, c.email);

                                item.setLastMessage(
                                        c.latest_message != null
                                                ? c.latest_message
                                                : "Tap to chat"
                                );

                                item.setTime(formatTime(c.latest_message_time));
                                item.setUnreadCount(c.unread_count);
                                item.setLastMessageTimestamp(
                                        parseBackendTime(c.latest_message_time)
                                );

                                list.add(item);
                            }

                            // Backend already sorted, but safe to ensure
                            list.sort((a, b) ->
                                    Long.compare(b.getLastMessageTimestamp(),
                                            a.getLastMessageTimestamp())
                            );

                            adapter.submitList(list);
                            updateEmptyState(searchEdit.getText().toString());
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Failed to load chats",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(@NonNull Call<CustomerListResponse> call,
                                          @NonNull Throwable t) {
                        if (!isFinishing()) {
                            Toast.makeText(MainActivity.this,
                                    "Error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateEmptyState("");
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void performLogout() {
        String token = session.getToken();
        if (token == null) {
            goToLogin();
            return;
        }

        apiService.logout("Bearer " + token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                handleLogout();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                handleLogout(); // Logout locally even if server fails
            }
        });
    }

    private void handleLogout() {
        session.clear();
        Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish();
    }

    private long parseBackendTime(String rawTime) {
        if (rawTime == null) return 0;

        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(rawTime);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatTime(String rawTime) {
        if (rawTime == null || rawTime.isEmpty()) return "";

        // ISO 8601 Format (adjust pattern based on your actual API response)
        // Assuming format like: "2023-10-05T14:30:00.000000Z"
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // API usually returns UTC

        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // 02:30 PM

        try {
            Date date = inputFormat.parse(rawTime);
            return (date != null) ? outputFormat.format(date) : "";
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}