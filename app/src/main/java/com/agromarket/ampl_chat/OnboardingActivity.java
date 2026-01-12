package com.agromarket.ampl_chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.agromarket.ampl_chat.adapters.OnboardingAdapter;
import com.agromarket.ampl_chat.models.api.AgentResponse;
import com.agromarket.ampl_chat.utils.ApiClient;
import com.agromarket.ampl_chat.utils.ApiService;
import com.agromarket.ampl_chat.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        if (isUserLoggedIn()) {
            handleLoggedInUser();
            return;
        }

        setupOnboarding();
    }

    // =======================
    // LOGIN STATE HANDLING
    // =======================
    private boolean isUserLoggedIn() {
        return sessionManager.getToken() != null && !sessionManager.getToken().isEmpty();
    }

    private void handleLoggedInUser() {
        String role = sessionManager.getUserRole();

        if ("customer".equalsIgnoreCase(role)) {
            fetchAssignedAgent();
        } else {
            openMainActivity();
        }
    }

    // =======================
    // ONBOARDING
    // =======================
    private void setupOnboarding() {
        setContentView(R.layout.activity_onboarding);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new OnboardingAdapter(this));
    }

    // =======================
    // CUSTOMER FLOW
    // =======================
    private void fetchAssignedAgent() {
        apiService.getAssignedAgent("Bearer " + sessionManager.getToken())
                .enqueue(new Callback<AgentResponse>() {

                    @Override
                    public void onResponse(Call<AgentResponse> call, Response<AgentResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            navigateToChat(0);
                            return;
                        }

                        navigateToChat(response.body().agent_id);
                    }

                    @Override
                    public void onFailure(Call<AgentResponse> call, Throwable t) {
                        navigateToChat(0);
                    }
                });
    }

    // =======================
    // NAVIGATION
    // =======================
    private void navigateToChat(int agentId) {
        Intent intent = new Intent(this, ChatScreenActivity.class);
        intent.putExtra("customer_id", sessionManager.getUserId());
        intent.putExtra("agent_id", agentId);
        startActivity(intent);
        finish();
    }

    private void openMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}