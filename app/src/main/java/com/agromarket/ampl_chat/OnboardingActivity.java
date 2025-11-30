package com.agromarket.ampl_chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // MUST BE FIRST

        sessionManager = new SessionManager(this);

        // USER ALREADY LOGGED IN
        if (sessionManager.getToken() != null && !sessionManager.getToken().isEmpty()) {

            // CUSTOMER → DIRECT CHAT
            if (sessionManager.getUserRole() != null &&
                sessionManager.getUserRole().equalsIgnoreCase("customer")) {
                getCustomerAssignedAgent(); // 👈 fetch agent api & redirect
                return;
            }

            // AGENT → OPEN CUSTOMER LIST
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
            return;
        }

        // FIRST TIME USERS → SWIPE SCREEN
        setContentView(R.layout.activity_onboarding);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new OnboardingAdapter(this));
    }


    // ===== CUSTOMER FLOW - FETCH AGENT API & REDIRECT =====
    private void getCustomerAssignedAgent() {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getAssignedAgent("Bearer " + sessionManager.getToken())
                .enqueue(new Callback<AgentResponse>() {
                    @Override
                    public void onResponse(Call<AgentResponse> call, Response<AgentResponse> res) {
                        int agentId = (res.body() != null) ? res.body().agent_id : 0;

                        Intent i = new Intent(OnboardingActivity.this, ChatScreenActivity.class);
                        i.putExtra("customer_id", sessionManager.getUserId());
                        i.putExtra("agent_id", agentId);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<AgentResponse> call, Throwable t) {
                        // fallback no agent
                        Intent i = new Intent(OnboardingActivity.this, ChatScreenActivity.class);
                        i.putExtra("customer_id", sessionManager.getUserId());
                        i.putExtra("agent_id", 0);
                        startActivity(i);
                        finish();
                    }
                });
    }
}