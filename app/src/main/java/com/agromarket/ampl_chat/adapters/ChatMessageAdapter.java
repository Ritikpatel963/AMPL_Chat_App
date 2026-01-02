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
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<MessageItem> list;
    private final RetryListener retryListener;

    private static final int VIEW_SENT_TEXT = 1;
    private static final int VIEW_SENT_IMAGE = 2;
    private static final int VIEW_RECEIVED_TEXT = 3;
    private static final int VIEW_RECEIVED_IMAGE = 4;

    public ChatMessageAdapter(Context context, List<MessageItem> list, RetryListener retryListener) {
        this.context = context;
        this.list = list;
        this.retryListener = retryListener;
    }

    @Override
    public int getItemViewType(int position) {
        MessageItem item = list.get(position);
        boolean isSent = item.isSent;

        if (isSent) {
            return item.type == MessageItem.TYPE_TEXT ? VIEW_SENT_TEXT : VIEW_SENT_IMAGE;
        } else {
            return item.type == MessageItem.TYPE_TEXT ? VIEW_RECEIVED_TEXT : VIEW_RECEIVED_IMAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_SENT_TEXT) {
            View v = inflater.inflate(R.layout.row_sent_text, parent, false);
            return new SentTextHolder(v);
        }

        if (viewType == VIEW_SENT_IMAGE) {
            View v = inflater.inflate(R.layout.row_sent_image, parent, false);
            return new SentImageHolder(v);
        }

        if (viewType == VIEW_RECEIVED_TEXT) {
            View v = inflater.inflate(R.layout.row_received_text, parent, false);
            return new ReceivedTextHolder(v);
        }

        View v = inflater.inflate(R.layout.row_received_image, parent, false);
        return new ReceivedImageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageItem item = list.get(position);

        if (holder instanceof SentTextHolder) {
            SentTextHolder h = (SentTextHolder) holder;
            h.msg.setText(item.text);
            h.time.setText(formatTo12Hour(item.time));
            bindStatus(h.statusIcon, item, position);

        } else if (holder instanceof SentImageHolder) {
            SentImageHolder h = (SentImageHolder) holder;

            Glide.with(context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_product_placeholder)
                    .into(h.img);
            h.msg.setText(item.text);
            h.time.setText(formatTo12Hour(item.time));
            bindStatus(h.statusIcon, item, position);

        } else if (holder instanceof ReceivedTextHolder) {
            ((ReceivedTextHolder) holder).msg.setText(item.text);
            ((ReceivedTextHolder) holder).time.setText(formatTo12Hour(item.time));

        } else if (holder instanceof ReceivedImageHolder) {
            ReceivedImageHolder h = (ReceivedImageHolder) holder;
            Glide.with(context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_product_placeholder)
                    .into(h.img);
            h.msg.setText(item.text);
            h.time.setText(formatTo12Hour(item.time));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ---------- VIEW HOLDERS ----------

    static class SentTextHolder extends RecyclerView.ViewHolder {
        TextView msg;
        ImageView statusIcon;
        TextView time;

        SentTextHolder(@NonNull View itemView) {
            super(itemView);
            msg = itemView.findViewById(R.id.textMessage);
            time = itemView.findViewById(R.id.timeText);
            statusIcon = itemView.findViewById(R.id.statusIcon);
        }
    }

    static class SentImageHolder extends RecyclerView.ViewHolder {
        ImageView img, statusIcon;
        TextView msg;
        TextView time;

        SentImageHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageMessage);
            msg = itemView.findViewById(R.id.textMessage);
            time = itemView.findViewById(R.id.timeText);
            statusIcon = itemView.findViewById(R.id.statusIcon);
        }
    }

    static class ReceivedTextHolder extends RecyclerView.ViewHolder {
        TextView msg;
        TextView time;
        ReceivedTextHolder(@NonNull View itemView) {
            super(itemView);
            msg = itemView.findViewById(R.id.textMessage);
            time = itemView.findViewById(R.id.timeText);
        }
    }

    static class ReceivedImageHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView msg;
        TextView time;
        ReceivedImageHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageMessage);
            msg = itemView.findViewById(R.id.textMessage);
            time = itemView.findViewById(R.id.timeText);
        }
    }
    private void bindStatus(ImageView icon, MessageItem item, int position) {

        if (!item.isSent) {
            icon.setVisibility(View.GONE);
            return;
        }

        icon.setVisibility(View.VISIBLE);

        switch (item.status) {

            case MessageItem.STATUS_SENDING:
                icon.setImageResource(R.drawable.ic_clock);
                icon.setOnClickListener(null);
                break;

            case MessageItem.STATUS_SENT:
                icon.setImageResource(R.drawable.ic_check);
                icon.setOnClickListener(null);
                break;

            case MessageItem.STATUS_FAILED:
                icon.setImageResource(R.drawable.ic_retry);
                icon.setOnClickListener(v -> {
                    if (retryListener != null) {
                        retryListener.onRetry(item, position);
                    }
                });
                break;
            case MessageItem.STATUS_SEEN:
                icon.setImageResource(R.drawable.ic_seen);
                icon.setOnClickListener(null);
                break;
        }
    }

    public static String formatTo12Hour(String serverTime) {
        try {
            SimpleDateFormat serverFormat =
                    new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

            SimpleDateFormat displayFormat =
                    new SimpleDateFormat("hh:mm a", Locale.getDefault());

            Date date = serverFormat.parse(serverTime);
            return displayFormat.format(date);

        } catch (Exception e) {
            return ""; // fallback
        }
    }
}