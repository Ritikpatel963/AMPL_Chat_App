package com.agromarket.ampl_chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.agromarket.ampl_chat.models.api.LoginRequest;
import com.agromarket.ampl_chat.models.api.LoginResponse;
import com.agromarket.ampl_chat.utils.ApiClient;
import com.agromarket.ampl_chat.utils.ApiService;
import com.agromarket.ampl_chat.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private Button btnLogin;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sessionManager = new SessionManager(this);

        // 🔥 Auto-login check
        if (!TextUtils.isEmpty(sessionManager.getToken())) {
            openMain();
            return;
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        initViews();
        apiService = ApiClient.getClient().create(ApiService.class);

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void initViews() {
        emailInput = findViewById(R.id.text_input_email_edit);
        passwordInput = findViewById(R.id.text_input_pwd_edit);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void loginUser() {

        String email = getText(emailInput);
        String password = getText(passwordInput);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            toast("Please enter email and password");
            return;
        }

        btnLogin.setEnabled(false);

        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    toast("Invalid server response");
                    return;
                }

                LoginResponse data = response.body();

                if (!data.status) {
                    toast(data.message);
                    return;
                }

                handleLoginSuccess(data);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                toast("Network error: " + t.getLocalizedMessage());
            }
        });
    }

    private void handleLoginSuccess(LoginResponse data) {

        sessionManager.saveToken(data.token);

        int userId = data.user != null ? data.user.id : 0;
        String role = data.user != null ? data.user.role : "";
        String name = data.user != null ? data.user.name : "";
        int agentId = data.agent_id;

        sessionManager.saveUserId(userId);
        sessionManager.saveUserRole(role);

        if ("customer".equalsIgnoreCase(role)) {
            openChat(userId, agentId, name);
        } else {
            openMain();
        }
    }

    private void openChat(int customerId, int agentId, String name) {
        Intent intent = new Intent(this, ChatScreenActivity.class);
        intent.putExtra("customer_id", customerId);
        intent.putExtra("agent_id", agentId);
        intent.putExtra("name", name);
        startActivity(intent);
        finish();
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}