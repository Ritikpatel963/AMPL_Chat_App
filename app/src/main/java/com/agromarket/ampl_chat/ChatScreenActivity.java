package com.agromarket.ampl_chat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class ChatScreenActivity extends AppCompatActivity {

    RecyclerView chatRecycler;
    ChatMessageAdapter chatAdapter;
    ArrayList<MessageItem> messageList;

    EditText messageBox;
    ImageView sendBtn, cartBtn, backBtn;
    String chatName;

    ArrayList<ProductItem> productList = new ArrayList<>(); // sample product images

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        TextView chatTitle = findViewById(R.id.chatName);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    systemBars.bottom
            );
            return insets;
        });

        chatName = getIntent().getStringExtra("name");
        chatTitle.setText(chatName);

        initViews();
        setupChatList();
        loadDummyProducts();

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

        MessageItem item = new MessageItem();
        item.type = MessageItem.TYPE_TEXT;
        item.text = msg;

        messageList.add(item);
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        chatRecycler.scrollToPosition(messageList.size() - 1);
        messageBox.setText("");
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
        MessageItem item = new MessageItem();
        item.type = MessageItem.TYPE_IMAGE;
        item.imageRes = product.imageRes;

        messageList.add(item);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecycler.scrollToPosition(messageList.size() - 1);
    }

    private void loadDummyProducts() {
        productList.add(new ProductItem(0));
        productList.add(new ProductItem(0));
        productList.add(new ProductItem(0));
//        productList.add(new ProductItem(R.drawable.prod4));
//        productList.add(new ProductItem(R.drawable.prod5));
    }
}