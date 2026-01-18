package com.agromarket.ampl_chat.models;

import androidx.annotation.NonNull;

import com.agromarket.ampl_chat.models.api.ChatMessage;
import com.agromarket.ampl_chat.models.api.SendMessageResponse;
import com.agromarket.ampl_chat.models.api.SendProductRequest;
import com.agromarket.ampl_chat.models.api.SendMessageRequest;
import com.agromarket.ampl_chat.utils.ApiClient;
import com.agromarket.ampl_chat.utils.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageItem {

    /* ================= TYPES ================= */

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;

    /* ================= STATUS ================= */

    public static final int STATUS_SENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_FAILED = 2;
    public static final int STATUS_SEEN = 3;

    /* ================= DATA ================= */

    public int type;
    public String text;
    public String imageUrl;
    public int productId;

    public boolean isSent;      // true = outgoing, false = incoming
    public int status;
    public String time;

    // Used to avoid duplicate UI messages
    public String localId;

    /* ================= FACTORY METHODS ================= */

    /** Create optimistic text message (sender side) */
    public static MessageItem createLocalText(String text, long timestamp) {
        MessageItem item = new MessageItem();
        item.type = TYPE_TEXT;
        item.text = text;
        item.isSent = true;
        item.status = STATUS_SENDING;
        item.localId = String.valueOf(timestamp);
        return item;
    }

    /** Create optimistic product message (sender side) */
    public static MessageItem createLocalProduct(@NonNull ProductItem product) {
        MessageItem item = new MessageItem();
        item.type = TYPE_IMAGE;
        item.text = product.name + "\n" + product.price;
        item.imageUrl = product.imageUrl;
        item.productId = product.id;
        item.isSent = true;
        item.status = STATUS_SENDING;
        item.localId = String.valueOf(System.currentTimeMillis());
        return item;
    }

    /** Convert API chat message → UI model */
    public static MessageItem fromChatMessage(ChatMessage m, String baseUrl) {
        MessageItem item = new MessageItem();

        item.isSent = false;
        item.time = m.created_at_formatted;
        item.status = STATUS_SENT;

        if ("text".equals(m.type)) {
            item.type = TYPE_TEXT;
            item.text = m.message;
        } else if ("product".equals(m.type) && m.data != null) {
            item.type = TYPE_IMAGE;
            item.text = m.data.name + "\n" + m.data.price;
            item.productId = m.data.id;

            if (m.data.image != null && !m.data.image.isEmpty()) {
                item.imageUrl = baseUrl +
                        (m.data.image.startsWith("/")
                                ? m.data.image.substring(1)
                                : m.data.image);
            }
        }

        return item;
    }

    /* ================= NETWORK HELPERS ================= */

    /** Retry sending failed message */
    public void retrySend(
            int receiverId,
            String token,
            androidx.recyclerview.widget.RecyclerView.Adapter<?> adapter
    ) {
        status = STATUS_SENDING;
        adapter.notifyDataSetChanged();

        ApiService api = ApiClient.getClient().create(ApiService.class);

        if (type == TYPE_TEXT) {
            SendMessageRequest body = new SendMessageRequest(receiverId, text);
            api.sendTextMessage("Bearer " + token, body)
                    .enqueue(sendCallback(this, adapter));
        } else if (type == TYPE_IMAGE) {
            SendProductRequest body = new SendProductRequest(receiverId, productId);
            api.sendProductMessage("Bearer " + token, body)
                    .enqueue(sendCallback(this, adapter));
        }
    }

    /** Unified callback for send / retry */
    public static Callback<SendMessageResponse> sendCallback(
            MessageItem item,
            androidx.recyclerview.widget.RecyclerView.Adapter<?> adapter
    ) {
        return new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call,
                                   Response<SendMessageResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    item.status = STATUS_SENT;
                    item.time = response.body().message.created_at_formatted;
                } else {
                    item.status = STATUS_FAILED;
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                item.status = STATUS_FAILED;
                adapter.notifyDataSetChanged();
            }
        };
    }
}