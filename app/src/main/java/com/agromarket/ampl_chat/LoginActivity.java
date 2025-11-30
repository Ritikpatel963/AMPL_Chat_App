package com.agromarket.ampl_chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.agromarket.ampl_chat.utils.ApiClient;
import com.agromarket.ampl_chat.utils.ApiService;
import com.agromarket.ampl_chat.models.api.LoginRequest;
import com.agromarket.ampl_chat.models.api.LoginResponse;
import com.agromarket.ampl_chat.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText emailInput, passwordInput;
    Button btnLogin;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);

        if (sessionManager.getToken() != null && !sessionManager.getToken().isEmpty()) {
            // Already logged in → go to main
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailInput = findViewById(R.id.inputEmail).findViewById(R.id.text_input_email_edit);
        passwordInput = findViewById(R.id.inputPassword).findViewById(R.id.text_input_pwd_edit);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email & password", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.status) {
                        sessionManager.saveToken(loginResponse.token);
                        int userId = 0;
                        String role = "";
                        int agentId = 0;

                        if (loginResponse.user != null) {
                            try {
                                userId = loginResponse.user.id;
                                role = loginResponse.user.role;
                                agentId = loginResponse.agent_id;
                                sessionManager.saveUserId(userId);
                                sessionManager.saveUserRole(role);
                            } catch (Exception ignored) {}
                        }

                        // -------------------------
                        // CHECK ROLE HERE
                        // -------------------------
                        if (role.equalsIgnoreCase("customer")) {
                            // 🔥 For customer directly open chat screen
                            Intent intent = new Intent(LoginActivity.this, ChatScreenActivity.class);
                            intent.putExtra("customer_id", userId);  // or customer_id field from API
                            intent.putExtra("agent_id", agentId);    // important for chat
                            intent.putExtra("name", loginResponse.user.name);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        // 🔥 If not a customer → normal app dashboard
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this, loginResponse.message, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}