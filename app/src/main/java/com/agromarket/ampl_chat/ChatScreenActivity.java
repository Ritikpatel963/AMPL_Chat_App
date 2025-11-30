package com.agromarket.ampl_chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agromarket.ampl_chat.adapters.ChatMessageAdapter;
import com.agromarket.ampl_chat.adapters.ProductAdapter;
import com.agromarket.ampl_chat.models.MessageItem;
import com.agromarket.ampl_chat.models.ProductItem;
import com.agromarket.ampl_chat.models.api.ChatMessage;
import com.agromarket.ampl_chat.models.api.MessageListResponse;
import com.agromarket.ampl_chat.models.api.Product;
import com.agromarket.ampl_chat.models.api.ProductListResponse;
import com.agromarket.ampl_chat.models.api.SendMessageRequest;
import com.agromarket.ampl_chat.models.api.SendMessageResponse;
import com.agromarket.ampl_chat.models.api.SendProductRequest;
import com.agromarket.ampl_chat.utils.ApiClient;
import com.agromarket.ampl_chat.utils.ApiService;
import com.agromarket.ampl_chat.utils.SessionManager;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatScreenActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://amplchat.agromarket.co.in/"; // your base

    RecyclerView chatRecycler;
    ChatMessageAdapter chatAdapter;
    ArrayList<MessageItem> messageList;

    EditText messageBox;
    ImageView sendBtn, cartBtn, backBtn;
    String chatName;
    int receiverId;
    ArrayList<ProductItem> productList = new ArrayList<>();

    SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        TextView chatTitle = findViewById(R.id.chatName);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);

        int customerId  = getIntent().getIntExtra("customer_id", 0);
        int agentId     = getIntent().getIntExtra("agent_id", 0);

        boolean isCustomer = session.getUserRole() != null && session.getUserRole().equals("customer");

        // If CUSTOMER → Chat with AGENT
        // If AGENT → Chat with CUSTOMER
        receiverId = isCustomer ? agentId : customerId;

        chatName = getIntent().getStringExtra("name");
        if (isCustomer) findViewById(R.id.chatName).setVisibility(View.GONE);
        else chatTitle.setText(chatName);

        initViews();
        setupChatList();
        loadMessages();
        loadProducts();

        sendBtn.setOnClickListener(v -> sendTextMessage());
        cartBtn.setOnClickListener(v -> openProductPopup());
        backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        chatRecycler = findViewById(R.id.chatRecycler);
        messageBox = findViewById(R.id.messageBox);
        sendBtn = findViewById(R.id.sendBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);

        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupChatList() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(this, messageList);
        chatRecycler.setAdapter(chatAdapter);
    }

    private void sendTextMessage() {
        String msg = messageBox.getText().toString().trim();
        if (msg.isEmpty()) return;

        String token = session.getToken();
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        SendMessageRequest body = new SendMessageRequest(receiverId, msg);

        api.sendTextMessage("Bearer " + token, body).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                // add to UI as sent
                MessageItem item = new MessageItem();
                item.type = MessageItem.TYPE_TEXT;
                item.text = msg;
                item.isSent = true;
                messageList.add(item);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                chatRecycler.scrollToPosition(messageList.size() - 1);
                messageBox.setText("");
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                Toast.makeText(ChatScreenActivity.this, "Send failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // -----------------------------
    //   PRODUCT POPUP
    // -----------------------------
    private void openProductPopup() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.product_popup, null);

        RecyclerView productGrid = view.findViewById(R.id.productGrid);
        productGrid.setLayoutManager(new GridLayoutManager(this, 3));

        ProductAdapter adapter = new ProductAdapter(productList, selected -> {
            sendProductToChat(selected);
            dialog.dismiss();
        });

        productGrid.setAdapter(adapter);

        view.findViewById(R.id.closePopup).setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    private void sendProductToChat(ProductItem product) {
        String token = session.getToken();

        ApiService api = ApiClient.getClient().create(ApiService.class);
        SendProductRequest body = new SendProductRequest(receiverId, product.id);

        api.sendProductMessage("Bearer " + token, body).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                MessageItem item = new MessageItem();
                item.type = MessageItem.TYPE_IMAGE;
                item.imageUrl = product.imageUrl;
                item.isSent = true;
                messageList.add(item);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                chatRecycler.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                Toast.makeText(ChatScreenActivity.this, "Send failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        String token = session.getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getProducts("Bearer " + token).enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(Call<ProductListResponse> call, Response<ProductListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                productList.clear();
                for (Product p : response.body().products) {
                    // p.image contains relative path like "uploads/products/foo.png"
                    String fullUrl = null;
                    if (p.image != null && !p.image.isEmpty()) {
                        // combine base + relative path (handle leading slash)
                        fullUrl = BASE_URL + (p.image.startsWith("/") ? p.image.substring(1) : p.image);
                    }

                    productList.add(new ProductItem(p.id, p.name, fullUrl, p.sale_price));
                }
            }

            @Override
            public void onFailure(Call<ProductListResponse> call, Throwable t) { }
        });
    }

    private void loadMessages() {
        String token = session.getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        // FETCH CHAT BETWEEN YOU <-> RECEIVER
        api.getMessages("Bearer " + token, receiverId).enqueue(new Callback<MessageListResponse>() {
            @Override
            public void onResponse(Call<MessageListResponse> call, Response<MessageListResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ChatScreenActivity.this, "Failed to load chat", Toast.LENGTH_SHORT).show();
                    return;
                }

                messageList.clear();
                int myId = session.getUserId();  // logged user id

                for (ChatMessage m : response.body().messages) {
                    MessageItem item = new MessageItem();

                    item.isSent = (m.sender_id == myId);  // 👈 this sets left/right alignment

                    if (m.type.equals("text")) {
                        item.type = MessageItem.TYPE_TEXT;
                        item.text = m.message;

                    } else if (m.type.equals("product")) {
                        item.type = MessageItem.TYPE_IMAGE;

                        if (m.data != null && m.data.image != null) {
                            item.imageUrl = BASE_URL + (m.data.image.startsWith("/")
                                    ? m.data.image.substring(1)
                                    : m.data.image);
                        }
                    }

                    messageList.add(item);
                }

                chatAdapter.notifyDataSetChanged();
                chatRecycler.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onFailure(Call<MessageListResponse> call, Throwable t) {
                Toast.makeText(ChatScreenActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        boolean isCustomer = session.getUserRole() != null && session.getUserRole().equals("customer");

        if (isCustomer) {
            session.clear(); // remove token + user details (logout)
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(ChatScreenActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        } else {
            super.onBackPressed(); // Agent → normal back behavior
        }
    }
}