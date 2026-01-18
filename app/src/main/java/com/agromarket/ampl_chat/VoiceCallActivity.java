package com.agromarket.ampl_chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.agromarket.ampl_chat.utils.ApiClient;
import com.agromarket.ampl_chat.utils.ApiService;
import com.agromarket.ampl_chat.utils.SessionManager;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
TODO:
1. UI Improvements
2. Call Recording
 */

public class VoiceCallActivity extends AppCompatActivity {

    private static final String TAG = "VoiceCallActivity";

    /* ================= STATIC ================= */
    private static VoiceCallActivity instance;

    public static final String EXTRA_CALL_ID = "call_id";
    public static final String EXTRA_IS_CALLER = "isCaller";
    public static final String EXTRA_CHANNEL = "channel";
    public static final String EXTRA_TOKEN = "token";
    public static final String EXTRA_UID = "uid";

    private static final int PERMISSION_REQ_ID = 101;
    private static final String AGORA_APP_ID = "bbf829300fcb477ea112100a59d0674b";

    /* ================= STATE ================= */
    private int callId;
    private boolean isCaller;
    private boolean callEnded = false;
    private boolean hasJoinedChannel = false;

    /* ================= AGORA ================= */
    private RtcEngine rtcEngine;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    /* ================= UI ================= */
    private TextView tvUserName, tvCallStatus, tvTimer;
    private ImageView btnMute, btnSpeaker, btnEnd;

    private boolean isMuted = false;
    private boolean isSpeakerOn = true; // Default ON for voice calls

    /* ================= TIMER ================= */
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private int seconds = 0;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            seconds++;
            tvTimer.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
            timerHandler.postDelayed(this, 1000);
        }
    };

    private SessionManager session;

    /* ================= STATIC START METHODS ================= */

    /**
     * Start activity for CALLER - shows "Calling..." UI
     * Waits for call_accepted event before joining Agora
     */
    public static void startCalling(Context ctx, int callId, boolean isCaller) {
        if (instance != null) {
            Log.w(TAG, "Call already active");
            return;
        }

        Intent i = new Intent(ctx, VoiceCallActivity.class);
        i.putExtra(EXTRA_CALL_ID, callId);
        i.putExtra(EXTRA_IS_CALLER, isCaller);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    /**
     * Start activity for RECEIVER - immediately joins Agora channel
     */
    public static void startActive(
            Context ctx,
            int callId,
            String channel,
            String token,
            int uid,
            boolean isCaller
    ) {
        if (instance != null) {
            Log.w(TAG, "Call already active");
            return;
        }

        Intent i = new Intent(ctx, VoiceCallActivity.class);
        i.putExtra(EXTRA_CALL_ID, callId);
        i.putExtra(EXTRA_IS_CALLER, isCaller);
        i.putExtra(EXTRA_CHANNEL, channel);
        i.putExtra(EXTRA_TOKEN, token);
        i.putExtra(EXTRA_UID, uid);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    /**
     * Called from ChatScreenActivity when caller receives call_accepted event
     * Triggers existing activity instance to join Agora
     */
    public static void joinChannel(String channel, String token, int uid) {
        if (instance == null) {
            Log.e(TAG, "No active call instance to join channel");
            return;
        }

        if (instance.hasJoinedChannel) {
            Log.w(TAG, "Already joined channel");
            return;
        }

        Log.d(TAG, "Joining channel from static method");
        instance.requestAudioPermission(channel, token, uid);
    }

    /* ================= LIFECYCLE ================= */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        session = new SessionManager(this);

        Intent i = getIntent();
        callId = i.getIntExtra(EXTRA_CALL_ID, 0);
        isCaller = i.getBooleanExtra(EXTRA_IS_CALLER, false);

        if (callId == 0) {
            Log.e(TAG, "Invalid call ID");
            finish();
            return;
        }

        Log.d(TAG, "onCreate - callId: " + callId + ", isCaller: " + isCaller);

        initViews();
        setupButtons();

        // Check if this is receiver (has channel info)
        String channel = i.getStringExtra(EXTRA_CHANNEL);
        String token = i.getStringExtra(EXTRA_TOKEN);
        int uid = i.getIntExtra(EXTRA_UID, 0);

        if (channel != null && token != null && uid > 0) {
            // RECEIVER - join immediately
            Log.d(TAG, "Receiver mode - joining immediately");
            requestAudioPermission(channel, token, uid);
        } else {
            // CALLER - wait for call_accepted event
            Log.d(TAG, "Caller mode - waiting for acceptance");
            showCallingUI();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
        Log.d(TAG, "onStart - instance set");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (instance == this) {
            instance = null;
            Log.d(TAG, "onStop - instance cleared");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        cleanupAgora();
        abandonAudioFocus();
        Log.d(TAG, "onDestroy");
    }

    /* ================= UI ================= */

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        tvTimer = findViewById(R.id.tvTimer);
        btnMute = findViewById(R.id.btnMute);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        btnEnd = findViewById(R.id.btnEnd);

        // Set initial speaker state (ON by default)
        btnSpeaker.setAlpha(isSpeakerOn ? 1f : 0.5f);
    }

    private void showCallingUI() {
        tvUserName.setText("Calling…");
        tvCallStatus.setText("Ringing...");
        tvTimer.setVisibility(View.GONE);
        btnMute.setVisibility(View.GONE);
        btnSpeaker.setVisibility(View.GONE);
    }

    private void showConnectingUI() {
        runOnUiThread(() -> {
            tvUserName.setText("Connecting...");
            tvCallStatus.setText("Joining call...");
            tvTimer.setVisibility(View.GONE);
            btnMute.setVisibility(View.VISIBLE);
            btnSpeaker.setVisibility(View.VISIBLE);
        });
    }

    private void showConnectedUI() {
        runOnUiThread(() -> {
            tvUserName.setText("In Call");
            tvCallStatus.setText("Connected");
            tvTimer.setVisibility(View.VISIBLE);
            btnMute.setVisibility(View.VISIBLE);
            btnSpeaker.setVisibility(View.VISIBLE);
            timerHandler.post(timerRunnable);
        });
    }

    private void setupButtons() {
        btnEnd.setOnClickListener(v -> endCall());

        btnMute.setOnClickListener(v -> {
            if (rtcEngine == null) return;

            isMuted = !isMuted;
            rtcEngine.muteLocalAudioStream(isMuted);
            btnMute.setAlpha(isMuted ? 0.5f : 1f);

            Log.d(TAG, "Mute toggled: " + isMuted);
            Toast.makeText(this, isMuted ? "Muted" : "Unmuted",
                    Toast.LENGTH_SHORT).show();
        });

        btnSpeaker.setOnClickListener(v -> {
            if (rtcEngine == null) return;

            isSpeakerOn = !isSpeakerOn;
            rtcEngine.setEnableSpeakerphone(isSpeakerOn);
            btnSpeaker.setAlpha(isSpeakerOn ? 1f : 0.5f);

            Log.d(TAG, "Speaker toggled: " + isSpeakerOn);
            Toast.makeText(this, isSpeakerOn ? "Speaker ON" : "Speaker OFF",
                    Toast.LENGTH_SHORT).show();
        });
    }

    /* ================= PERMISSIONS ================= */

    private void requestAudioPermission(String channel, String token, int uid) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            joinAgoraChannel(channel, token, uid);
        } else {
            // Store credentials for after permission grant
            getIntent().putExtra(EXTRA_CHANNEL, channel);
            getIntent().putExtra(EXTRA_TOKEN, token);
            getIntent().putExtra(EXTRA_UID, uid);

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQ_ID
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int code, @NonNull String[] permissions, @NonNull int[] results) {

        super.onRequestPermissionsResult(code, permissions, results);

        if (code == PERMISSION_REQ_ID) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                // Retrieve stored credentials
                Intent i = getIntent();
                String channel = i.getStringExtra(EXTRA_CHANNEL);
                String token = i.getStringExtra(EXTRA_TOKEN);
                int uid = i.getIntExtra(EXTRA_UID, 0);

                if (channel != null && token != null && uid > 0) {
                    joinAgoraChannel(channel, token, uid);
                } else {
                    Log.e(TAG, "Missing channel info after permission grant");
                    finish();
                }
            } else {
                Toast.makeText(this, "Microphone permission required",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /* ================= AGORA ================= */

    private void joinAgoraChannel(String channel, String token, int uid) {
        if (hasJoinedChannel) {
            Log.w(TAG, "Already joined channel");
            return;
        }

        Log.d(TAG, "=== JOINING AGORA CHANNEL ===");
        Log.d(TAG, "Channel: " + channel);
        Log.d(TAG, "UID: " + uid);
        Log.d(TAG, "Token length: " + (token != null ? token.length() : "null"));
        Log.d(TAG, "Token first 30 chars: " + (token != null ? token.substring(0, Math.min(30, token.length())) + "..." : "null"));
        Log.d(TAG, "App ID: " + AGORA_APP_ID);

        // CRITICAL: Verify UID is positive integer
        if (uid <= 0) {
            Log.e(TAG, "Invalid UID: " + uid + " - must be positive integer");
            Toast.makeText(this, "Invalid user ID for call", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        hasJoinedChannel = true;
        showConnectingUI();

        try {
            // Initialize audio BEFORE creating RtcEngine
            initAudioFocus();

            rtcEngine = RtcEngine.create(
                    getApplicationContext(),
                    AGORA_APP_ID,
                    rtcHandler
            );

            Log.d(TAG, "RtcEngine created successfully");

            // Configure for voice call
            rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);

            // CRITICAL: Enable audio devices BEFORE enableAudio()
            rtcEngine.enableLocalAudio(true);
            rtcEngine.enableAudio();

            // Audio routing - speaker ON by default
            rtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
            rtcEngine.setEnableSpeakerphone(true);

            // Volume settings
            rtcEngine.adjustRecordingSignalVolume(100);
            rtcEngine.adjustPlaybackSignalVolume(100);

            // Additional audio device settings
            rtcEngine.setAudioProfile(
                    Constants.AUDIO_PROFILE_DEFAULT,
                    Constants.AUDIO_SCENARIO_CHATROOM
            );

            Log.d(TAG, "Calling joinChannel with UID: " + uid);

            // CRITICAL: uid parameter must match the uid used to generate token
            int result = rtcEngine.joinChannel(token, channel, null, uid);

            if (result < 0) {
                Log.e(TAG, "Failed to join channel, error code: " + result);

                String errorMsg;
                switch (result) {
                    case -2:
                        errorMsg = "Invalid argument";
                        break;
                    case -3:
                        errorMsg = "Not ready";
                        break;
                    case -5:
                        errorMsg = "Refused";
                        break;
                    case -7:
                        errorMsg = "Not initialized";
                        break;
                    case -110:
                        errorMsg = "Invalid token or UID mismatch";
                        break;
                    default:
                        errorMsg = "Error code: " + result;
                }

                Toast.makeText(this, "Failed to join call: " + errorMsg, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Log.d(TAG, "joinChannel called successfully, result: " + result);
            }

        } catch (Exception e) {
            Log.e(TAG, "Agora init failed", e);
            Toast.makeText(this, "Failed to initialize call: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private final IRtcEngineEventHandler rtcHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d(TAG, "✅ onJoinChannelSuccess - Channel: " + channel + ", UID: " + uid);

            runOnUiThread(() -> {
                // Ensure audio is unmuted
                if (rtcEngine != null) {
                    rtcEngine.muteLocalAudioStream(false);
                    rtcEngine.adjustRecordingSignalVolume(100);
                    rtcEngine.adjustPlaybackSignalVolume(100);
                }

                showConnectedUI();
                Toast.makeText(VoiceCallActivity.this,
                        "Connected to call", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            Log.d(TAG, "✅ onUserJoined - UID: " + uid);

            runOnUiThread(() -> {
                Toast.makeText(VoiceCallActivity.this,
                        "Other user joined", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Log.d(TAG, "❌ onUserOffline - UID: " + uid + ", Reason: " + reason);

            runOnUiThread(() -> {
                tvCallStatus.setText("Call ended");
                Toast.makeText(VoiceCallActivity.this,
                        "Other user left", Toast.LENGTH_SHORT).show();

                // Wait 1 second before ending
                new Handler(Looper.getMainLooper()).postDelayed(() -> endCall(), 1000);
            });
        }

        @Override
        public void onError(int err) {
            Log.e(TAG, "❌ Agora error: " + err);

            runOnUiThread(() -> {
                Toast.makeText(VoiceCallActivity.this,
                        "Call error: " + err, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onConnectionLost() {
            Log.e(TAG, "❌ Connection lost");

            runOnUiThread(() -> {
                tvCallStatus.setText("Connection lost");
                Toast.makeText(VoiceCallActivity.this,
                        "Connection lost", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onAudioRouteChanged(int routing) {
            Log.d(TAG, "Audio route changed: " + routing);
            // routing: 0=default, 1=headset, 2=earpiece, 3=headset_no_mic,
            //          4=speakerphone, 5=loudspeaker, 6=headset_bluetooth
        }
    };

    /* ================= CALL END ================= */

    private synchronized void endCall() {
        if (callEnded) {
            Log.d(TAG, "Call already ended");
            return;
        }

        Log.d(TAG, "Ending call...");
        callEnded = true;

        // Notify backend
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.endCall("Bearer " + session.getToken(), callId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d(TAG, "End call API success");
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "End call API failed", t);
                    }
                });

        // Cleanup
        timerHandler.removeCallbacks(timerRunnable);
        cleanupAgora();
        abandonAudioFocus();

        finish();
    }

    private void cleanupAgora() {
        if (rtcEngine != null) {
            Log.d(TAG, "Cleaning up Agora engine");
            rtcEngine.leaveChannel();
            RtcEngine.destroy();
            rtcEngine = null;
        }
    }

    /* ================= AUDIO FOCUS ================= */

    private void initAudioFocus() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager == null) return;

        Log.d(TAG, "Requesting audio focus");

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(
                            new android.media.AudioAttributes.Builder()
                                    .setUsage(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                                    .build()
                    )
                    .setOnAudioFocusChangeListener(focusChange -> {
                        Log.d(TAG, "Audio focus changed: " + focusChange);
                    })
                    .build();

            int result = audioManager.requestAudioFocus(audioFocusRequest);
            Log.d(TAG, "Audio focus request result: " + result);
        } else {
            int result = audioManager.requestAudioFocus(
                    focusChange -> Log.d(TAG, "Audio focus changed: " + focusChange),
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            );
            Log.d(TAG, "Audio focus request result: " + result);
        }
    }

    private void abandonAudioFocus() {
        if (audioManager == null) return;

        Log.d(TAG, "Abandoning audio focus");

        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(false);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
                audioFocusRequest = null;
            }
        } else {
            audioManager.abandonAudioFocus(null);
        }
    }

    /* ================= STATIC HELPERS ================= */

    public static boolean isActive() {
        return instance != null;
    }

    public static void finishCall() {
        if (instance != null) {
            Log.d(TAG, "finishCall() called from outside");
            instance.endCall();
        }
    }
}