package com.agromarket.ampl_chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
TODO:
1. Retry coming as status after being sent
2. Product sending issue app crash
3. On retry its sending as a new message item
4. On back press its logging out (on customer)

5. Agora Voice Calling
6. Payment Link
 */

public class ChatScreenActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://amplchat.agromarket.co.in/"; // your base

    private String lastSentText = null;
    private long lastSentTime = 0;

    private static final long DUPLICATE_BLOCK_WINDOW = 800; // ms
    private int lastSentProductId = -1;
    private long lastProductSentTime = 0;

    RecyclerView chatRecycler;
    ChatMessageAdapter chatAdapter;
    ArrayList<MessageItem> messageList;

    EditText messageBox;
    ImageView sendBtn, cartBtn, backBtn;
    String chatName;
    int receiverId;

    private int productCurrentPage = 1;
    private boolean productIsLoading = false;
    private boolean productHasMore = true;

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
        markMessagesSeen();

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
        chatAdapter = new ChatMessageAdapter(this, messageList, this::retrySend);
        chatRecycler.setAdapter(chatAdapter);
    }
    private void sendTextMessage() {

        String msg = messageBox.getText().toString().trim();
        if (msg.isEmpty()) return;

        long now = System.currentTimeMillis();

        // Prevent duplicate click sending same message
        if (msg.equals(lastSentText) && (now - lastSentTime) < DUPLICATE_BLOCK_WINDOW) {
            return;
        }

        lastSentText = msg;
        lastSentTime = now;

        String token = session.getToken();
        if (token == null) return;

        // Add message instantly
        MessageItem item = new MessageItem();
        item.type = MessageItem.TYPE_TEXT;
        item.text = msg;
        item.isSent = true;
        item.status = MessageItem.STATUS_SENDING;
        item.localId = String.valueOf(now);

        messageList.add(item);
        int position = messageList.size() - 1;
        chatAdapter.notifyItemInserted(position);
        chatRecycler.scrollToPosition(position);
        messageBox.setText("");

        ApiService api = ApiClient.getClient().create(ApiService.class);
        SendMessageRequest body = new SendMessageRequest(receiverId, msg);

        api.sendTextMessage("Bearer " + token, body).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    item.status = MessageItem.STATUS_SENT;
                    item.time = response.body().message.created_at_formatted;
                } else {
                    item.status = MessageItem.STATUS_FAILED;
                }
                chatAdapter.notifyItemChanged(position);
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                item.status = MessageItem.STATUS_FAILED;
                item.time = "";
                chatAdapter.notifyItemChanged(position);
            }
        });
    }
    private void sendProductToChat(ProductItem product) {

        long now = System.currentTimeMillis();

        // Prevent duplicate product clicks
        if (product.id == lastSentProductId &&
                (now - lastProductSentTime) < DUPLICATE_BLOCK_WINDOW) {
            return;
        }

        lastSentProductId = product.id;
        lastProductSentTime = now;

        String token = session.getToken();
        if (token == null) return;

        MessageItem item = new MessageItem();
        item.type = MessageItem.TYPE_IMAGE;
        item.text = product.name + "\n" + product.price;
        item.imageUrl = product.imageUrl;
        item.productId = product.id;
        item.isSent = true;
        item.status = MessageItem.STATUS_SENDING;
        item.localId = String.valueOf(now);

        messageList.add(item);
        int position = messageList.size() - 1;
        chatAdapter.notifyItemInserted(position);
        chatRecycler.scrollToPosition(position);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        SendProductRequest body = new SendProductRequest(receiverId, product.id);

        api.sendProductMessage("Bearer " + token, body).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                item.status = MessageItem.STATUS_SENT;
                item.time = response.body().message.created_at_formatted;
                chatAdapter.notifyItemChanged(position);
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                item.status = MessageItem.STATUS_FAILED;
                item.time = "";
                chatAdapter.notifyItemChanged(position);
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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        productGrid.setLayoutManager(gridLayoutManager);

        productList.clear();
        productCurrentPage = 1;
        productHasMore = true;

        ProductAdapter adapter = new ProductAdapter(productList, null);
        productGrid.setAdapter(adapter);

        // initial skeleton
        showSkeletons();
        adapter.notifyDataSetChanged();

        loadProducts(adapter);

        // Pagination scroll listener
        productGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = gridLayoutManager.getChildCount();
                int totalItemCount = gridLayoutManager.getItemCount();
                int firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition();

                if (!productIsLoading && productHasMore &&
                        (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 3)) {
                    loadProducts(adapter);
                }
            }
        });

        view.findViewById(R.id.sendSelected).setOnClickListener(v -> {
            for (ProductItem p : productList) {
                if (p.isSelected && !p.isSkeleton) {
                    sendProductToChat(p);
                }
            }
            dialog.dismiss();
        });

        view.findViewById(R.id.closePopup).setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    public void retrySend(MessageItem item, int position) {

        String token = session.getToken();
        if (token == null) return;

        item.status = MessageItem.STATUS_SENDING;
        chatAdapter.notifyItemChanged(position);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        if (item.type == MessageItem.TYPE_TEXT) {

            SendMessageRequest body = new SendMessageRequest(receiverId, item.text);

            api.sendTextMessage("Bearer " + token, body).enqueue(new Callback<SendMessageResponse>() {
                @Override
                public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                    item.status = MessageItem.STATUS_SENT;
                    chatAdapter.notifyItemChanged(position);
                }

                @Override
                public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                    item.status = MessageItem.STATUS_FAILED;
                    chatAdapter.notifyItemChanged(position);
                }
            });

        } else if (item.type == MessageItem.TYPE_IMAGE) {

            SendProductRequest body = new SendProductRequest(receiverId, item.productId);

            api.sendProductMessage("Bearer " + token, body).enqueue(new Callback<SendMessageResponse>() {
                @Override
                public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                    item.status = MessageItem.STATUS_SENT;
                    chatAdapter.notifyItemChanged(position);
                }

                @Override
                public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                    item.status = MessageItem.STATUS_FAILED;
                    chatAdapter.notifyItemChanged(position);
                }
            });
        }
    }
    private void markMessagesSeen() {
        String token = session.getToken();
        if (token == null) return;

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.markSeen("Bearer " + token, receiverId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) { }

            @Override
            public void onFailure(Call<Void> call, Throwable t) { }
        });
    }
    private void loadMessages() {

        String token = session.getToken();
        if (token == null) return;

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getMessages("Bearer " + token, receiverId)
                .enqueue(new Callback<MessageListResponse>() {

                    @Override
                    public void onResponse(Call<MessageListResponse> call,
                                           Response<MessageListResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(ChatScreenActivity.this,
                                    "Failed to load chat", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        messageList.clear();
                        int myId = session.getUserId();

                        for (ChatMessage m : response.body().messages) {

                            MessageItem item = new MessageItem();

                            item.isSent = (m.sender_id == myId);
                            item.time = m.created_at_formatted;

                            if (item.isSent) {
                                item.status = m.seen_at != null
                                        ? MessageItem.STATUS_SEEN
                                        : MessageItem.STATUS_SENT;
                            }

                            if ("text".equals(m.type)) {

                                item.type = MessageItem.TYPE_TEXT;
                                item.text = m.message;

                            }
                            else if ("product".equals(m.type) && m.data != null) {

                                item.type = MessageItem.TYPE_IMAGE;
                                item.text = m.data.name + "\n" + m.data.price;
                                item.productId = m.data.id;

                                if (m.data.image != null && !m.data.image.isEmpty()) {
                                    item.imageUrl = BASE_URL +
                                            (m.data.image.startsWith("/")
                                                    ? m.data.image.substring(1)
                                                    : m.data.image);
                                }
                            }

                            messageList.add(item);
                        }

                        chatAdapter.notifyDataSetChanged();

                        if (!messageList.isEmpty()) {
                            chatRecycler.scrollToPosition(messageList.size() - 1);
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageListResponse> call, Throwable t) {
                        Toast.makeText(ChatScreenActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadProducts(ProductAdapter adapter) {

        if (productIsLoading || !productHasMore) return;

        String token = session.getToken();
        if (token == null) return;

        productIsLoading = true;

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getProducts("Bearer " + token, productCurrentPage)
                .enqueue(new Callback<ProductListResponse>() {

                    @Override
                    public void onResponse(Call<ProductListResponse> call,
                                           Response<ProductListResponse> response) {

                        productIsLoading = false;

                        if (!response.isSuccessful() || response.body() == null) return;

                        // remove skeletons
                        removeSkeletons();

                        for (Product p : response.body().products) {
                            String img = null;
                            if (p.image != null && !p.image.isEmpty()) {
                                img = BASE_URL + (p.image.startsWith("/") ?
                                        p.image.substring(1) : p.image);
                            }

                            productList.add(
                                    new ProductItem(p.id, p.name, img, p.sale_price)
                            );
                        }

                        productHasMore = response.body().has_more;
                        productCurrentPage++;

                        adapter.notifyDataSetChanged();

                        // add bottom skeleton if more pages
                        if (productHasMore) addBottomSkeletons(adapter);
                    }

                    @Override
                    public void onFailure(Call<ProductListResponse> call, Throwable t) {
                        productIsLoading = false;
                    }
                });
    }


    @Override
    public void onBackPressed() {
//        boolean isCustomer = session.getUserRole() != null && session.getUserRole().equals("customer");
//
//        if (isCustomer) {
//            session.clear(); // remove token + user details (logout)
//            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
//
//            Intent i = new Intent(ChatScreenActivity.this, LoginActivity.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(i);
//            finish();
//        } else {
//            super.onBackPressed(); // Agent → normal back behavior
//        }
    }

//    Products loading Helpers
    private void showSkeletons() {
        productList.clear();
        for (int i = 0; i < 6; i++) {
            productList.add(new ProductItem(true));
        }
    }
    private void addBottomSkeletons(ProductAdapter adapter) {
        for (int i = 0; i < 3; i++) {
            productList.add(new ProductItem(true));
        }
        adapter.notifyDataSetChanged();
    }
    private void removeSkeletons() {
        for (int i = productList.size() - 1; i >= 0; i--) {
            if (productList.get(i).isSkeleton) {
                productList.remove(i);
            }
        }
    }

}