package com.agromarket.ampl_chat;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.agromarket.ampl_chat.models.api.CallResponse;
import com.agromarket.ampl_chat.utils.ApiClient;
import com.agromarket.ampl_chat.utils.ApiService;
import com.agromarket.ampl_chat.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingCallActivity extends AppCompatActivity {

    private static final int AUTO_END_SECONDS = 30;

    private int callId;
    private String callerName;

    private Ringtone ringtone;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_incoming_call);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        callId = getIntent().getIntExtra("call_id", 0);
        callerName = getIntent().getStringExtra("caller_name");

        TextView tvCaller = findViewById(R.id.tvCallerName);
        ImageView btnAccept = findViewById(R.id.btnAccept);
        ImageView btnReject = findViewById(R.id.btnReject);

        tvCaller.setText(callerName != null ? callerName : "Incoming Call");

        playRingtone();

        btnAccept.setOnClickListener(v -> acceptCall());
        btnReject.setOnClickListener(v -> rejectCall());

        // Auto-timeout (missed call)
        timeoutHandler.postDelayed(this::rejectCall, AUTO_END_SECONDS * 1000L);
    }

    /* ================= RINGTONE ================= */

    private void playRingtone() {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, uri);
            ringtone.play();
        } catch (Exception ignored) {}
    }

    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    /* ================= CALL ACTIONS ================= */

    private void acceptCall() {
        stopRingtone();
        timeoutHandler.removeCallbacksAndMessages(null);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.acceptCall("Bearer " + session.getToken(), callId)
                .enqueue(new Callback<CallResponse>() {
                    @Override
                    public void onResponse(Call<CallResponse> call,
                                           Response<CallResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            finish();
                            return;
                        }

                        CallResponse data = response.body();

                        VoiceCallActivity.startActive(
                                IncomingCallActivity.this,
                                callId,
                                data.channel,
                                data.token,
                                data.uid,
                                false
                        );

                        finish();
                    }

                    @Override
                    public void onFailure(Call<CallResponse> call, Throwable t) {
                        finish();
                    }
                });
    }

    private void rejectCall() {
        stopRingtone();
        timeoutHandler.removeCallbacksAndMessages(null);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.rejectCall("Bearer " + session.getToken(), callId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                finish();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}