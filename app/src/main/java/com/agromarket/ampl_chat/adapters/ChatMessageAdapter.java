package com.agromarket.ampl_chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agromarket.ampl_chat.R;
import com.agromarket.ampl_chat.models.MessageItem;

import java.util.ArrayList;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<MessageItem> list;

    private static final int VIEW_SENT_TEXT = 1;
    private static final int VIEW_SENT_IMAGE = 2;
    private static final int VIEW_RECEIVED_TEXT = 3;
    private static final int VIEW_RECEIVED_IMAGE = 4;

    public ChatMessageAdapter(Context context, ArrayList<MessageItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        MessageItem item = list.get(position);

        // For demo → all messages are "sent"
        // If you want real sender logic, add a senderId check here
        boolean isSent = true;

        if (isSent) {
            if (item.type == MessageItem.TYPE_TEXT) return VIEW_SENT_TEXT;
            else return VIEW_SENT_IMAGE;
        } else {
            if (item.type == MessageItem.TYPE_TEXT) return VIEW_RECEIVED_TEXT;
            else return VIEW_RECEIVED_IMAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_SENT_TEXT) {
            View v = LayoutInflater.from(context).inflate(R.layout.row_sent_text, parent, false);
            return new SentTextHolder(v);
        }

        if (viewType == VIEW_SENT_IMAGE) {
            View v = LayoutInflater.from(context).inflate(R.layout.row_sent_image, parent, false);
            return new SentImageHolder(v);
        }

        if (viewType == VIEW_RECEIVED_TEXT) {
            View v = LayoutInflater.from(context).inflate(R.layout.row_received_text, parent, false);
            return new ReceivedTextHolder(v);
        }

        View v = LayoutInflater.from(context).inflate(R.layout.row_received_image, parent, false);
        return new ReceivedImageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageItem item = list.get(position);

        if (holder instanceof SentTextHolder) {
            ((SentTextHolder) holder).msg.setText(item.text);
        }

        if (holder instanceof SentImageHolder) {
            if (item.imageRes == 0) {
                ((SentImageHolder) holder).img.setBackground(null); // remove bubble
                ((SentImageHolder) holder).img.setImageResource(R.drawable.ic_product_placeholder);
            } else {
                ((SentImageHolder) holder).img.setBackground(null); // or your bubble bg if you want
                ((SentImageHolder) holder).img.setImageResource(item.imageRes);
            }
        }


        if (holder instanceof ReceivedTextHolder) {
            ((ReceivedTextHolder) holder).msg.setText(item.text);
        }

        if (holder instanceof ReceivedImageHolder) {
            ((ReceivedImageHolder) holder).img.setImageResource(item.imageRes);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ---------- VIEW HOLDERS ----------

    class SentTextHolder extends RecyclerView.ViewHolder {
        TextView msg;
        SentTextHolder(@NonNull View itemView) {
            super(itemView);
            msg = itemView.findViewById(R.id.textMessage);
        }
    }

    class SentImageHolder extends RecyclerView.ViewHolder {
        ImageView img;
        SentImageHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageMessage);
        }
    }

    class ReceivedTextHolder extends RecyclerView.ViewHolder {
        TextView msg;
        ReceivedTextHolder(@NonNull View itemView) {
            super(itemView);
            msg = itemView.findViewById(R.id.textMessage);
        }
    }

    class ReceivedImageHolder extends RecyclerView.ViewHolder {
        ImageView img;
        ReceivedImageHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageMessage);
        }
    }
}